package com.complai.coldsales.agents.base.client;

import com.complai.coldsales.agents.base.core.Agent;
import com.complai.coldsales.agents.base.result.LLMResult;
import com.complai.coldsales.agents.base.tools.AgentTool;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.*;
import com.complai.coldsales.utils.ServiceError;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * LLMClient for executing agents using real OpenAI API calls.
 * 
 * Instance-based design for better performance, testability, and resource management.
 * The OpenAI client is cached and reused across all agent executions.
 */
public class LLMClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(LLMClient.class);
    
    private final String apiKey;
    private final String defaultModel;
    private final OpenAIClient client; // Cached client instance
    private final ObjectMapper objectMapper;
    private volatile boolean closed = false;
    private static final int MAX_TOOL_ITERATIONS = 10;
    
    /**
     * Create a LLMClient instance with API key and default model.
     * 
     * @param apiKey OpenAI API key
     * @param defaultModel Default model to use if agent doesn't specify one
     */
    @Builder
    public LLMClient(String apiKey, String defaultModel) {
        this.apiKey = apiKey;
        this.defaultModel = defaultModel;
        this.client = OpenAIOkHttpClient.builder().apiKey(this.apiKey).build();
        this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    /**
     * Create a LLMClient instance from Settings.
     */
    public static LLMClient fromSettings(com.complai.coldsales.config.Settings settings) {
        return LLMClient.builder()
                .apiKey(settings.getOpenaiApiKey())
                .defaultModel(settings.getModel())
                .build();
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true; // OpenAI client doesn't have close() method in this version. Resource cleanup is handled by the HTTP client internally
    }
    
    /**
     * Run an agent with a prompt using OpenAI API calls.
     *
     * @param agent The agent to run
     * @param prompt The input prompt
     * @return A future containing the result
     */
    public CompletableFuture<LLMResult> run(Agent agent, String prompt) {
        validateInputs(agent, prompt);

        if (agent.hasTools()) {
            log.info("🔧 Agent '{}' has {} tool(s) available for agent-of-agents execution", 
                    agent.getName(), agent.getTools().size());
            return runWithTools(agent, prompt);
        } else {
            return runSimple(agent, prompt);
        }
    }
    
    private void validateInputs(Agent agent, String prompt) {
        if (agent == null) {
            throw new IllegalArgumentException("Agent cannot be null");
        }
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be null or empty. Received: " + prompt);
        }
    }
    
    /**
     * Run a simple agent without tools (standard execution).
     */
    private CompletableFuture<LLMResult> runSimple(Agent agent, String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String modelName = getModel(agent);
                List<ChatCompletionMessageParam> messages = buildMessages(agent, prompt);
                ChatCompletionCreateParams request = buildRequest(agent, modelName, messages);
                ChatCompletion response = client.chat().completions().create(request);

                String rawOutput = extractRawOutput(response);
                Object output = parseOutput(agent, rawOutput);
                int tokensUsed = extractTokenUsage(response);
                
                log.info("✅ OpenAI API call successful ({} chars, {} tokens)", 
                        output.toString().length(), tokensUsed);

                return buildSimpleResult(agent, modelName, output, tokensUsed);

            } catch (Exception e) {
                log.error("❌ Error calling OpenAI API for agent {}", agent.getName(), e);
                return createErrorResult(agent.getName(), e);
            }
        });
    }

    private String extractRawOutput(ChatCompletion response) {
        return response.choices().get(0).message().content()
                .orElseThrow(() -> new IllegalStateException("OpenAI returned empty response"));
    }

    private int extractTokenUsage(ChatCompletion response) {
        return response.usage()
                .map(CompletionUsage::totalTokens)
                .orElse(0L).intValue();
    }

    private LLMResult buildSimpleResult(Agent agent, String modelName, Object output, int tokensUsed) {
        return LLMResult.builder()
                        .finalOutput(output)
                        .model(modelName)
                        .tokensUsed(tokensUsed)
                        .isStructured(agent.hasStructuredOutput())
                        .toolCallsMade(0)
                        .toolsUsed(Collections.emptyList())
                        .build();
    }

    private LLMResult createErrorResult(String agentName, Exception e) {
        String errorOutput = "Error calling OpenAI API for " + agentName + ": " + e.getMessage() + 
                "\n\nPlease check your OPENAI_API_KEY in .env file";
        return new LLMResult(errorOutput);
    }

    private Object parseOutput(Agent agent, String rawOutput) {
        if (!agent.hasStructuredOutput()) {
            return rawOutput;
        }
        
        try {
            log.debug("🔍 Raw LLM output (first 200 chars): {}{}",
                    rawOutput.substring(0, Math.min(rawOutput.length(), 200)),
                    rawOutput.length() > 200 ? "..." : "");
            
            Object parsed = objectMapper.readValue(rawOutput, agent.getOutputType());
            
            if (isAllNulls(parsed)) {
                log.warn("⚠️  Structured output parsed but all fields are null. " +
                        "Possible empty JSON/null values or tools not executed. Falling back to raw output.");
                return rawOutput;
            }
            
            log.info("✅ Structured output parsed successfully for {}", agent.getOutputTypeName());
            return parsed;
        } catch (Exception e) {
            log.warn("⚠️ Failed to parse structured output for {}. Falling back to raw text. Error: {}", 
                    agent.getName(), e.getMessage());
            log.debug("Raw output (first 200 chars): {}{}", 
                    rawOutput.substring(0, Math.min(rawOutput.length(), 200)),
                    rawOutput.length() > 200 ? "..." : "");
            return rawOutput;
        }
    }
    
    private boolean isAllNulls(Object obj) {
        if (obj == null) return true;
        try {
            java.lang.reflect.Field[] fields = obj.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value != null) {
                    if (value instanceof java.util.List) {
                        if (!((java.util.List<?>) value).isEmpty()) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false; // If we can't check, assume not all nulls
        }
    }

    private ChatCompletionCreateParams buildRequest(Agent agent, String modelName, 
                                                     List<ChatCompletionMessageParam> messages) {
        ChatCompletionCreateParams.Builder requestBuilder = ChatCompletionCreateParams.builder()
                .model(ChatModel.of(modelName))
                .messages(messages)
                .maxTokens(1500L)
                .temperature(0.7)
                .topP(1.0)
                .frequencyPenalty(0.0)
                .presencePenalty(0.0);

        if (agent.hasStructuredOutput()) {
            requestBuilder.responseFormat(
                    ChatCompletionCreateParams.ResponseFormat.ofResponseFormatJsonObject(
                            ResponseFormatJsonObject.builder()
                                    .type(ResponseFormatJsonObject.Type.JSON_OBJECT)
                                    .build()));
        }
        return requestBuilder.build();
    }

    private List<ChatCompletionMessageParam> buildMessages(Agent agent, String prompt) {
        validateMessageInputs(agent, prompt);

        ChatCompletionSystemMessageParam instructionMessage = createInstructionMessage(agent);
        Optional<ChatCompletionSystemMessageParam> structuredOutputMessage = 
                createStructuredOutputMessage(agent);
        ChatCompletionUserMessageParam promptMessage = createPromptMessage(prompt);

        return Stream.concat(
                        Stream.of(instructionMessage, promptMessage),
                        structuredOutputMessage.stream())
                .map(this::convertToMessageParam)
                .toList();
    }

    private void validateMessageInputs(Agent agent, String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be null or empty");
        }
        if (agent == null) {
            throw new IllegalArgumentException("Agent cannot be null");
        }
        if (agent.getInstructions() == null || agent.getInstructions().trim().isEmpty()) {
            throw new IllegalArgumentException("Agent instructions cannot be null or empty");
        }
    }

    private ChatCompletionSystemMessageParam createInstructionMessage(Agent agent) {
        return ChatCompletionSystemMessageParam.builder()
                .role(ChatCompletionSystemMessageParam.Role.SYSTEM)
                .content(ChatCompletionSystemMessageParam.Content.ofTextContent(agent.getInstructions()))
                .build();
    }

    private Optional<ChatCompletionSystemMessageParam> createStructuredOutputMessage(Agent agent) {
        if (!agent.hasStructuredOutput()) {
            return Optional.empty();
        }
        
        log.debug("📊 Using structured output schema: {}", agent.getOutputTypeName());
        String structuredOutputInstruction = "Return your response as valid JSON matching this schema: " + 
                agent.getOutputTypeName();
        
        return Optional.of(ChatCompletionSystemMessageParam.builder()
                .role(ChatCompletionSystemMessageParam.Role.SYSTEM)
                .content(ChatCompletionSystemMessageParam.Content.ofTextContent(structuredOutputInstruction))
                .build());
    }

    private ChatCompletionUserMessageParam createPromptMessage(String prompt) {
        return ChatCompletionUserMessageParam.builder()
                .role(ChatCompletionUserMessageParam.Role.USER)
                .content(ChatCompletionUserMessageParam.Content.ofTextContent(prompt))
                .build();
    }

    private ChatCompletionMessageParam convertToMessageParam(Object message) {
        return switch (message) {
            case ChatCompletionSystemMessageParam system -> 
                    ChatCompletionMessageParam.ofChatCompletionSystemMessageParam(system);
            case ChatCompletionUserMessageParam user -> 
                    ChatCompletionMessageParam.ofChatCompletionUserMessageParam(user);
            default -> throw new IllegalArgumentException(
                    "Unsupported message param type: " + message.getClass().getSimpleName());
        };
    }

    private String getModel(Agent agent) {
        String modelName = agent.getModel() != null ? agent.getModel() : defaultModel;
        log.info("🔄 Calling OpenAI API with model '{}' for agent '{}'", modelName, agent.getName());
        return modelName;
    }
     
    /**
     * Convert agent tools to OpenAI function definitions.
     * 
     * TODO: This needs to be implemented based on your OpenAI Java client (0.8.1) API.
     * The class name and structure may differ. Check the OpenAI Java client documentation
     * for the correct way to create function definitions and tools.
     */
    private List<ChatCompletionTool> convertToolsToOpenAIFunctions(List<AgentTool> tools) {
        List<ChatCompletionTool> openAITools = new ArrayList<>();
        
        for (AgentTool tool : tools) {
            try {
                Map<String, Object> parametersSchema = buildToolParametersSchema();
                String parametersJson = objectMapper.writeValueAsString(parametersSchema);
                
                log.warn("⚠️  Function definition creation needs implementation. " +
                        "Check OpenAI Java client docs for function/tool creation. " +
                        "Tool: {}, Parameters: {}", tool.getName(), parametersJson);
                
            } catch (Exception e) {
                log.error("Failed to convert tool {} to OpenAI function: {}", 
                        tool.getName(), e.getMessage(), e);
            }
        }
        
        log.warn("⚠️  Returning empty tools list - function conversion not yet implemented");
        return openAITools;
    }

    private Map<String, Object> buildToolParametersSchema() {
        Map<String, Object> parametersSchema = new HashMap<>();
        parametersSchema.put("type", "object");
        parametersSchema.put("properties", Map.of(
                "prompt", Map.of(
                        "type", "string",
                        "description", "The prompt to execute with this tool"
                )
        ));
        parametersSchema.put("required", List.of("prompt"));
        parametersSchema.put("additionalProperties", false);
        return parametersSchema;
    }
    
    /**
     * Run an agent with tools (agent-of-agents pattern).
     * 
     * This implements the true agent-of-agents pattern where the LLM dynamically decides:
     * - Which tools to call
     * - In what order
     * - How many times
     * - When to stop
     * 
     * Uses OpenAI's function calling API for real dynamic tool selection.
     */
    private CompletableFuture<LLMResult> runWithTools(Agent agent, String prompt) {
        List<ChatCompletionTool> openAITools = convertToolsToOpenAIFunctions(agent.getTools());
        
        if (openAITools.isEmpty()) {
            log.warn("⚠️  No tools available - function definition conversion not implemented. " +
                    "Falling back to simple execution without tool calling.");
            return runSimple(agent, prompt).thenApply(this::convertToToolResult);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            log.info("🤖 Running agent-of-agents for '{}'", agent.getName());
            logAvailableTools(agent);
            
            try {
                String modelName = getModel(agent);
                List<ChatCompletionMessageParam> messages = createInitialToolMessages(agent, prompt);
                ToolExecutionState state = new ToolExecutionState();
                
                for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
                    ChatCompletion response = executeToolCallIteration(agent, modelName, messages, openAITools);
                    ChatCompletionMessage message = response.choices().get(0).message();
                    
                    state.addTokens(extractTokenUsage(response));
                    messages.add(createAssistantMessage(message));
                    
                    List<ChatCompletionMessageToolCall> toolCalls = message.toolCalls()
                            .orElse(Collections.emptyList());
                    
                    if (toolCalls.isEmpty()) {
                        return buildFinalResult(agent, modelName, message, state);
                    }
                    
                    processToolCalls(agent, prompt, toolCalls, messages, state);
                }
                
                return createMaxIterationsResult(agent.getName());
                
            } catch (Exception e) {
                log.error("❌ Error in agent-of-agents execution for '{}'", agent.getName(), e);
                return createExecutionErrorResult(agent.getName(), e);
            }
        });
    }

    private LLMResult convertToToolResult(LLMResult simpleResult) {
        return LLMResult.builder()
                .finalOutput(simpleResult.getFinalOutput())
                .model(simpleResult.getModel())
                .tokensUsed(simpleResult.getTokensUsed())
                .isStructured(simpleResult.isStructured())
                .toolCallsMade(0)
                .toolsUsed(Collections.emptyList())
                .build();
    }

    private void logAvailableTools(Agent agent) {
        String availableTools = agent.getTools().stream()
                .map(AgentTool::getName)
                .collect(java.util.stream.Collectors.joining(", "));
        log.info("   Available tools: {}", availableTools);
    }

    private List<ChatCompletionMessageParam> createInitialToolMessages(Agent agent, String prompt) {
        List<ChatCompletionMessageParam> messages = new ArrayList<>();
        messages.add(ChatCompletionMessageParam.ofChatCompletionSystemMessageParam(
                ChatCompletionSystemMessageParam.builder()
                        .role(ChatCompletionSystemMessageParam.Role.SYSTEM)
                        .content(ChatCompletionSystemMessageParam.Content.ofTextContent(agent.getInstructions()))
                        .build()));
        messages.add(ChatCompletionMessageParam.ofChatCompletionUserMessageParam(
                ChatCompletionUserMessageParam.builder()
                        .role(ChatCompletionUserMessageParam.Role.USER)
                        .content(ChatCompletionUserMessageParam.Content.ofTextContent(prompt))
                        .build()));
        return messages;
    }

    private ChatCompletion executeToolCallIteration(Agent agent, String modelName,
                                                     List<ChatCompletionMessageParam> messages,
                                                     List<ChatCompletionTool> openAITools) {
        ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                .model(ChatModel.of(modelName))
                .messages(messages)
                .tools(openAITools)
                .maxTokens(1500L)
                .temperature(0.7)
                .topP(1.0)
                .frequencyPenalty(0.0)
                .presencePenalty(0.0)
                .build();
        
        return client.chat().completions().create(request);
    }

    private ChatCompletionMessageParam createAssistantMessage(ChatCompletionMessage message) {
        ChatCompletionAssistantMessageParam.Builder assistantBuilder = 
                ChatCompletionAssistantMessageParam.builder()
                        .role(ChatCompletionAssistantMessageParam.Role.ASSISTANT)
                        .content(ChatCompletionAssistantMessageParam.Content.ofTextContent(
                                message.content().orElse("")));
        
        message.toolCalls().ifPresent(assistantBuilder::toolCalls);
        
        return ChatCompletionMessageParam.ofChatCompletionAssistantMessageParam(
                assistantBuilder.build());
    }

    private void processToolCalls(Agent agent, String originalPrompt,
                                   List<ChatCompletionMessageToolCall> toolCalls,
                                   List<ChatCompletionMessageParam> messages,
                                   ToolExecutionState state) {
        for (ChatCompletionMessageToolCall toolCall : toolCalls) {
            String toolName = toolCall.function().name();
            log.info("   → Calling tool '{}'", toolName);
            
            String toolPrompt = extractToolPrompt(toolCall, originalPrompt);
            Optional<AgentTool> toolOpt = findTool(agent, toolName);
            
            if (toolOpt.isPresent()) {
                executeToolAndAddResult(toolOpt.get(), toolCall, toolPrompt, messages, state);
            } else {
                addToolNotFoundError(toolCall, toolName, messages);
            }
        }
    }

    private String extractToolPrompt(ChatCompletionMessageToolCall toolCall, String defaultPrompt) {
        try {
            Map<String, Object> args = objectMapper.readValue(
                    toolCall.function().arguments(),
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            if (args.containsKey("prompt")) {
                return args.get("prompt").toString();
            }
        } catch (Exception e) {
            log.warn("      ⚠️  Failed to parse tool arguments, using original prompt: {}", 
                    e.getMessage());
        }
        return defaultPrompt;
    }

    private Optional<AgentTool> findTool(Agent agent, String toolName) {
        return agent.getTools().stream()
                .filter(t -> t.getName().equals(toolName))
                .findFirst();
    }

    private void executeToolAndAddResult(AgentTool tool, ChatCompletionMessageToolCall toolCall,
                                         String toolPrompt, List<ChatCompletionMessageParam> messages,
                                         ToolExecutionState state) {
        try {
            com.complai.coldsales.utils.Result<String, ServiceError> toolResult = 
                    tool.execute(this, toolPrompt).join();
            
            if (toolResult.isErr()) {
                addToolErrorResult(toolCall, toolResult.unwrapErr(), messages);
                return;
            }
            
            String toolOutput = toolResult.unwrap();
            if (toolOutput == null || toolOutput.trim().isEmpty()) {
                toolOutput = "No result returned";
                log.warn("      ⚠️  Tool '{}' returned null or empty result", tool.getName());
            }
            
            addToolSuccessResult(toolCall, toolOutput, messages, state);
            log.info("      ✅ Tool '{}' executed successfully", tool.getName());
            
        } catch (Exception e) {
            log.error("      ❌ Tool '{}' execution failed: {}", tool.getName(), e.getMessage(), e);
            addToolExceptionResult(toolCall, e, messages);
        }
    }

    private void addToolErrorResult(ChatCompletionMessageToolCall toolCall, ServiceError error,
                                     List<ChatCompletionMessageParam> messages) {
        log.warn("      ❌ Tool '{}' execution failed: {}", 
                toolCall.function().name(), error.format());
        messages.add(createToolErrorMessage(toolCall, "Error: " + error.format()));
    }

    private void addToolSuccessResult(ChatCompletionMessageToolCall toolCall, String toolOutput,
                                       List<ChatCompletionMessageParam> messages,
                                       ToolExecutionState state) {
        messages.add(createToolSuccessMessage(toolCall, toolOutput));
        state.incrementToolCalls();
        state.addToolUsed(toolCall.function().name());
    }

    private void addToolExceptionResult(ChatCompletionMessageToolCall toolCall, Exception e,
                                         List<ChatCompletionMessageParam> messages) {
        messages.add(createToolErrorMessage(toolCall, "Error: " + e.getMessage()));
    }

    private void addToolNotFoundError(ChatCompletionMessageToolCall toolCall, String toolName,
                                       List<ChatCompletionMessageParam> messages) {
        log.warn("      ⚠️  Tool not found: {}", toolName);
        messages.add(createToolErrorMessage(toolCall, "Error: Tool " + toolName + " not found"));
    }

    private ChatCompletionMessageParam createToolSuccessMessage(ChatCompletionMessageToolCall toolCall,
                                                                 String content) {
        return ChatCompletionMessageParam.ofChatCompletionToolMessageParam(
                ChatCompletionToolMessageParam.builder()
                        .role(ChatCompletionToolMessageParam.Role.TOOL)
                        .toolCallId(toolCall.id())
                        .content(ChatCompletionToolMessageParam.Content.ofTextContent(content))
                        .build());
    }

    private ChatCompletionMessageParam createToolErrorMessage(ChatCompletionMessageToolCall toolCall,
                                                               String errorMessage) {
        return ChatCompletionMessageParam.ofChatCompletionToolMessageParam(
                ChatCompletionToolMessageParam.builder()
                        .role(ChatCompletionToolMessageParam.Role.TOOL)
                        .toolCallId(toolCall.id())
                        .content(ChatCompletionToolMessageParam.Content.ofTextContent(errorMessage))
                        .build());
    }

    private LLMResult buildFinalResult(Agent agent, String modelName, ChatCompletionMessage message,
                                     ToolExecutionState state) {
        String finalContent = message.content().orElse("");
        Object output = extractFinalOutput(agent, finalContent);
        
        log.info("✅ Agent-of-agents execution complete");
        log.info("   Tools used: {}", state.getToolCallsMade());
        
        return LLMResult.builder()
                .finalOutput(output)
                .model(modelName)
                .tokensUsed(state.getTotalTokens())
                .isStructured(agent.hasStructuredOutput() && 
                        output.getClass().equals(agent.getOutputType()))
                .toolCallsMade(state.getToolCallsMade())
                .toolsUsed(state.getToolsUsed())
                .build();
    }

    private Object extractFinalOutput(Agent agent, String finalContent) {
        if (agent.hasStructuredOutput() && !finalContent.isEmpty()) {
            try {
                Object output = objectMapper.readValue(finalContent, agent.getOutputType());
                log.info("✅ Structured output parsed successfully for {}", agent.getOutputTypeName());
                return output;
            } catch (Exception e) {
                log.warn("⚠️  Failed to parse structured output, using raw text: {}", e.getMessage());
                return finalContent;
            }
        }
        return finalContent;
    }

    private LLMResult createMaxIterationsResult(String agentName) {
        log.error("❌ Max iterations reached for '{}'", agentName);
        return new LLMResult("Error: Max iterations reached for " + agentName);
    }

    private LLMResult createExecutionErrorResult(String agentName, Exception e) {
        return new LLMResult("Error in agent-of-agents execution for " + agentName + 
                ": " + e.getMessage());
    }

    /**
     * Helper class to track tool execution state during agent-of-agents pattern.
     */
    private static class ToolExecutionState {
        private int totalTokens = 0;
        private int toolCallsMade = 0;
        private final List<String> toolsUsed = new ArrayList<>();

        void addTokens(int tokens) {
            this.totalTokens += tokens;
        }

        void incrementToolCalls() {
            this.toolCallsMade++;
        }

        void addToolUsed(String toolName) {
            if (!toolsUsed.contains(toolName)) {
                toolsUsed.add(toolName);
            }
        }

        int getTotalTokens() {
            return totalTokens;
        }

        int getToolCallsMade() {
            return toolCallsMade;
        }

        List<String> getToolsUsed() {
            return toolsUsed;
        }
    }
}

