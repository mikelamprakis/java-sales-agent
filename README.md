# Java Sales Agent

An intelligent cold sales email generation system powered by OpenAI's GPT models. This project demonstrates advanced AI agent patterns including **Agent-of-Agents** and **Manual Orchestration** for creating personalized, compliant, and effective sales emails.

## 🎯 Overview

The Java Sales Agent is a sophisticated system that:
- **Researches prospects** using multiple data sources (websites, LinkedIn, news)
- **Generates personalized sales emails** in multiple styles (professional, engaging, busy-executive)
- **Validates content** using AI-powered guardrails (safety, compliance, privacy)
- **Sends emails** via SMTP with HTML formatting

## 🏗️ Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    EnhancedDemo (Entry Point)                │
└───────────────────────┬─────────────────────────────────────┘
                        │
        ┌───────────────┴───────────────┐
        │                               │
┌───────▼────────┐            ┌────────▼────────┐
│ Research Phase │            │  Email Phase    │
│ (Agent-of-     │            │  (Manual       │
│  Agents)       │            │   Orchestration)│
└───────┬────────┘            └────────┬────────┘
        │                               │
        │                               │
┌───────▼──────────────────────────────▼────────┐
│         EnhancedSalesManager                    │
│  - Orchestrates workflows                      │
│  - Manages agents and pipelines                │
│  - Handles guardrails                          │
└────────────────────────────────────────────────┘
```

### Core Components

#### 1. **LLMClient** (`agents/base/client/`)
- Centralized OpenAI API client
- Manages API connections and caching
- Handles both simple and tool-enabled agent execution
- Implements agent-of-agents pattern with dynamic tool calling

#### 2. **Agent System** (`agents/`)
- **Core Agents** (`agents/base/core/`): Base classes for all AI agents
- **Sales Agents** (`agents/sales/`): Generate emails in different styles
- **Email Agents** (`agents/email/`): Analyze and process emails
- **Research Agents** (`agents/research/`): Gather prospect information

#### 3. **Guardrails** (`agents/base/guardrails/`)
- **Content Safety Checker**: Detects spam, inappropriate content
- **Business Context Checker**: Ensures brand compliance, competitor mentions
- **Personal Data Checker**: Identifies sensitive information

#### 4. **Pipelines** (`pipelines/`)
- **EmailPipeline**: 5-stage email generation pipeline
- **ResearchPipeline**: Multi-source prospect research pipeline

#### 5. **Services** (`services/`)
- **WebScraperService**: Fetches and parses company websites
- **LinkedInScraperService**: Extracts LinkedIn company data
- **NewsSearchService**: Finds recent news and press releases
- **EmailService**: Sends emails via SMTP

## 📁 Project Structure

```
java-sales-agent/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/complai/coldsales/
│   │   │       ├── agents/
│   │   │       │   ├── base/
│   │   │       │   │   ├── client/          # LLM client implementation
│   │   │       │   │   │   └── LLMClient.java
│   │   │       │   │   ├── core/            # Core agent classes
│   │   │       │   │   │   ├── Agent.java
│   │   │       │   │   │   └── AIAgentComponent.java
│   │   │       │   │   ├── guardrails/     # Guardrail system
│   │   │       │   │   │   ├── GuardrailFunction.java
│   │   │       │   │   │   └── GuardrailResult.java
│   │   │       │   │   ├── result/         # Result models
│   │   │       │   │   │   └── LLMResult.java
│   │   │       │   │   └── tools/          # Agent tools
│   │   │       │   │       ├── AgentTool.java
│   │   │       │   │       └── ServiceBackedAgentTool.java
│   │   │       │   ├── email/              # Email processing agents
│   │   │       │   ├── research/            # Research agents
│   │   │       │   │   └── analyzer/       # Research analyzers
│   │   │       │   └── sales/              # Sales email agents
│   │   │       ├── config/                 # Configuration
│   │   │       │   ├── Settings.java
│   │   │       │   └── EnhancedGuardrailManager.java
│   │   │       ├── managers/               # Business logic
│   │   │       │   ├── EnhancedSalesManager.java
│   │   │       │   └── reporting/         # Pipeline reporters
│   │   │       ├── models/                 # Data models
│   │   │       │   ├── guardrails/        # Guardrail models
│   │   │       │   ├── pipeline/         # Pipeline models
│   │   │       │   ├── result/           # Result models
│   │   │       │   ├── structured/      # Structured outputs
│   │   │       │   └── types/           # Type definitions
│   │   │       ├── pipelines/            # Workflow pipelines
│   │   │       │   ├── EmailPipeline.java
│   │   │       │   └── ResearchPipeline.java
│   │   │       ├── services/              # External services
│   │   │       ├── tools/                # Service-backed tools
│   │   │       ├── utils/                # Utilities
│   │   │       ├── EnhancedDemo.java      # Main entry point
│   │   │       └── Logs.java             # Logging utilities
│   │   └── resources/
│   │       ├── prompts/                  # AI prompts organized by category
│   │       │   ├── email/
│   │       │   ├── guardrails/
│   │       │   ├── research/
│   │       │   │   └── analyzers/
│   │       │   └── sales/
│   │       └── logback.xml               # Logging configuration
│   └── test/                            # Test suite
│       └── java/
│           └── com/complai/coldsales/
│               ├── agents/               # Agent tests
│               ├── integration/         # Integration tests
│               ├── pipelines/           # Pipeline tests
│               └── realapi/             # Real API tests
├── pom.xml                              # Maven configuration
├── .gitignore
└── README.md
```

## 🚀 Features

### 1. **Agent-of-Agents Pattern**
The research phase uses an agent-of-agents pattern where a main research agent dynamically decides:
- Which research tools to use (website, LinkedIn, news, competitor analysis)
- In what order to execute them
- How many times to call each tool
- When it has gathered enough information

### 2. **Manual Orchestration Pattern**
The email generation phase uses explicit orchestration:
1. **Generate** - Create 3 email variants (professional, engaging, busy-executive)
2. **Analyze** - Evaluate each email's effectiveness
3. **Select** - Choose the best email
4. **Subject** - Generate subject line options
5. **Convert** - Transform to HTML
6. **Send** - Deliver via SMTP

### 3. **Structured Outputs**
All agents return strongly-typed Java objects:
- `SalesEmail` - Structured email content
- `EmailAnalysis` - Email quality metrics
- `EmailSubject` - Subject line options
- `ProspectResearch` - Research findings

### 4. **AI Guardrails**
Three-layer validation system:
- **Content Safety**: Prevents spam, inappropriate content
- **Business Context**: Ensures brand compliance, no competitor mentions
- **Personal Data**: Detects and prevents sensitive data leakage

### 5. **Multi-Source Research**
- Company website scraping
- LinkedIn company data extraction
- News and press release search
- Competitive landscape analysis

## 🛠️ Setup

### Prerequisites

- **Java 21+** (project uses Java 21 features)
- **Maven 3.6+**
- **OpenAI API Key** (get one from [OpenAI](https://platform.openai.com/))

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/mikelamprakis/java-sales-agent.git
   cd java-sales-agent
   ```

2. **Create environment file**
   ```bash
   cp env.example .env
   ```

3. **Configure environment variables**
   Edit `.env` file:
   ```properties
   OPENAI_API_KEY=your-api-key-here
   MODEL=gpt-4o-mini
   
   # Email Configuration
   FROM_EMAIL=your-email@gmail.com
   FROM_NAME=Your Name
   TO_EMAIL=recipient@example.com
   SMTP_HOST=smtp.gmail.com
   SMTP_PORT=587
   SMTP_USER=your-email@gmail.com
   SMTP_PASSWORD=your-app-password
   ```

4. **Build the project**
   ```bash
   mvn clean package
   ```

## 💻 Usage

### Running the Application

```bash
# Run with research (agent-of-agents pattern)
java -jar target/java-sales-agent-1.0.0.jar

# Or run directly with Maven
mvn exec:java -Dexec.mainClass="com.complai.coldsales.EnhancedDemo"
```

### Configuration Options

Edit `EnhancedDemo.java` to customize:

```java
// Toggle research phase
static final boolean USE_RESEARCH = true;

// Configure research target
static final String COMPANY_NAME = "Stripe";
static final String TARGET_ROLE = "CTO";

// Or provide direct prompt (when USE_RESEARCH = false)
private static final String DIRECT_PROMPT = "...";
```

### Two Workflow Modes

#### 1. **Hybrid Workflow** (USE_RESEARCH = true)
```
Research Phase (Agent-of-Agents)
  ↓
Email Phase (Manual Orchestration)
  ↓
Send Email
```

#### 2. **Direct Email** (USE_RESEARCH = false)
```
Direct Prompt
  ↓
Email Phase (Manual Orchestration)
  ↓
Send Email
```

## 🔑 Key Components

### LLMClient
The central client for all OpenAI API interactions:
- Caches client instances for performance
- Handles both simple and tool-enabled executions
- Manages token usage and error handling

### Agent System
- **AIAgentComponent**: Base class for all agents
- **Agent**: Core agent definition with instructions, model, tools
- **AgentTool**: Wraps agents as tools for agent-of-agents pattern

### Guardrails
- **GuardrailFunction**: Functional interface for validation
- **EnhancedGuardrailManager**: Manages all guardrail checks
- Runs in parallel for performance

### Pipelines
- **EmailPipeline**: 5-stage email generation
- **ResearchPipeline**: Multi-source research with tool orchestration

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Categories
```bash
# Unit tests only
mvn test -Dtest=*Test

# Integration tests
mvn test -Dtest=*IntegrationTest

# Real API tests (requires OPENAI_API_KEY)
mvn test -Dtest=*RealApi*
```

### Test Structure
- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test pipeline workflows
- **Real API Tests**: Test against actual OpenAI API (requires API key)

## 📊 Architecture Patterns

### 1. Agent-of-Agents Pattern
The research agent uses tools that are themselves agents:
```java
ProspectResearchAgent
  ├── CompanyWebsiteAnalyzerAgent (tool)
  ├── LinkedInCompanyAnalyzer (tool)
  ├── NewsAndPressAnalyzerAgent (tool)
  └── CompetitorAnalyzerAgent (tool)
```

The LLM dynamically decides which tools to call and when.

### 2. Manual Orchestration Pattern
Explicit control flow for email generation:
```java
generateEmails() → analyzeEmails() → selectBest() → generateSubject() → convertAndSend()
```

### 3. Service-Backed Tools
Tools that fetch real data before LLM processing:
- `CompanyWebsiteTool` - Scrapes websites
- `LinkedInCompanyTool` - Fetches LinkedIn data
- `NewsAndPressTool` - Searches news

## 🔧 Configuration

### Settings Class
Centralized configuration management:
- Loads from `.env` file
- Validates required fields
- Provides defaults

### Prompt Management
Prompts are organized in `src/main/resources/prompts/`:
- `email/` - Email-related prompts
- `guardrails/` - Guardrail validation prompts
- `research/` - Research agent prompts
- `sales/` - Sales email generation prompts

## 📝 Code Quality

- **Java 21** features (records, pattern matching, etc.)
- **Lombok** for reducing boilerplate
- **Structured outputs** with Jackson
- **Comprehensive error handling**
- **Logging** with SLF4J/Logback
- **Type-safe** result types

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 🙏 Acknowledgments

- OpenAI for the GPT models
- All open-source libraries used in this project

## 📞 Support

For issues and questions, please open an issue on GitHub.

---

**Built with ❤️ using Java, OpenAI GPT, and modern AI agent patterns**

