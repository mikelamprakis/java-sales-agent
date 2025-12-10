package com.complai.coldsales;

import com.complai.coldsales.models.pipeline.email.EmailPipelineResult;
import com.complai.coldsales.models.pipeline.hybrid.EmailHybridResult;
import com.complai.coldsales.models.result.EmailResult;
import com.complai.coldsales.models.result.ErrorResult;
import com.complai.coldsales.models.result.HybridResult;
import com.complai.coldsales.models.result.PipelineResult;

import java.util.Map;

import static com.complai.coldsales.EnhancedDemo.*;

public class Logs {

    public static void logStartForHybridFlow(String targetRole, String companyName){
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🎯 HYBRID AGENTIC WORKFLOW");
        System.out.println("=".repeat(70));
        System.out.println("Target: " + targetRole + " at " + companyName);
        System.out.println();
    }

    public static void logPhase2(){
        System.out.println("📧 PHASE 2: Email Generation (Manual Orchestration Pattern)");
        System.out.println("-".repeat(70));
        System.out.println("Following deterministic pipeline:");
        System.out.println("  1. Generate 3 email variations");
        System.out.println("  2. Analyze all emails");
        System.out.println("  3. Select best email");
        System.out.println("  4. Generate subject lines");
        System.out.println("  5. Convert to HTML & send");
        System.out.println();
    }

    public static void logEndOfHybridFlow(EmailHybridResult hybrid){
        System.out.println("\n" + "=".repeat(70));
        System.out.println("✅ HYBRID WORKFLOW COMPLETE");
        System.out.println("=".repeat(70));
        System.out.println("Pattern 1 (Agent-of-Agents): Used " + hybrid.getResearchPhase().getToolsUsed() + " tools");
        System.out.println("Pattern 2 (Manual Orchestration): Executed 6 steps");
        System.out.println("Result: " + hybrid.getEmailResult().getStatus());
        System.out.println("=".repeat(70));
    }

    static void startingLogs(){
        System.out.println("🚀 Cold Sales Agent - Hybrid Workflow");
        System.out.println("=".repeat(70));
        if (USE_RESEARCH) {
            System.out.println("🔧 Mode: HYBRID WORKFLOW (Agent-of-Agents + Manual Orchestration)");
            System.out.println("🎯 Target: " + TARGET_ROLE + " at " + COMPANY_NAME);
        } else {
            System.out.println("🔧 Mode: MANUAL ORCHESTRATION ONLY");
            System.out.println("📝 Using direct prompt (no research phase)");
        }
        System.out.println("=".repeat(70)+ "\n");
    }

    public static void displayResult(PipelineResult result) {
        if (result == null) return;
        switch (result.getClass().getSimpleName()) {
            case "HybridResult":
                displayHybridResults((HybridResult) result);
                break;
            case "ErrorResult":
                displayError((ErrorResult) result);
                break;
            case "EmailResult":
                displayManualResults();
                break;
            default:
                throw new IllegalArgumentException("Unknown result type: " + result.getClass().getSimpleName());
        }
        displayEmailResults(result);
    }

    private static void displayHybridResults(HybridResult result) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📊 HYBRID WORKFLOW RESULTS");
        System.out.println("=".repeat(70));
        System.out.println();

        // Research phase summary - TYPE-SAFE!
        System.out.println("1️⃣  RESEARCH PHASE (Agent-of-Agents)");
        System.out.println("   Pattern: agent-of-agents");
        System.out.println("   Tools Used: " + result.getResearchToolsUsed());
        System.out.println("   Tool Names: " + String.join(", ", result.getResearchToolNames()));
        System.out.println("   AI Decision: AI dynamically selected research sources");
        System.out.println();

        // Email phase summary - TYPE-SAFE!
        System.out.println("2️⃣  EMAIL PHASE (Manual Orchestration)");
        System.out.println("   Pattern: " + result.getEmailPhasePattern());
        System.out.println("   Steps: " + String.join(" → ", result.getEmailPhaseSteps()));
        System.out.println();
    }

    private static void displayManualResults() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📊 RESULTS (Manual Orchestration)");
        System.out.println("=".repeat(70));
        System.out.println();
    }

    private static void displayEmailResults(PipelineResult result) {
        // Handle error case
        if (result instanceof ErrorResult) {
            ErrorResult error = (ErrorResult) result;
            System.out.println("❌ Error: " + error.getMessage());
            System.out.println("=".repeat(70));
            return;
        }

        // Extract email result (works for both EmailResult and HybridResult)
        EmailPipelineResult emailPipelineResult;
        if (result instanceof HybridResult hybrid) {
            emailPipelineResult = hybrid.getEmailPipelineResult();
        } else if (result instanceof EmailResult email) {
            emailPipelineResult = email.getEmailPipelineResult();
        } else {
            System.out.println("⚠️  Unknown result type: " + result.getClass().getSimpleName());
            return;
        }

        // TYPE-SAFE access to email data!
        var selectedEmail = emailPipelineResult.getSelectedEmail();
        var analysis = emailPipelineResult.getAnalysis();
        var subjectOptions = emailPipelineResult.getSubjectOptions();

        System.out.println("📧 Generated Email:");
        System.out.println("   Subject: " + selectedEmail.getSubject());
        System.out.println("   Tone: " + selectedEmail.getTone());
        System.out.println("   Expected Response Rate: " + selectedEmail.getExpectedResponseRate() + "%");
        System.out.println();

        System.out.println("📈 Email Analysis:");
        System.out.println("   Effectiveness Score: " + analysis.getEffectivenessScore() + "/10");
        System.out.println("   Personalization: " + analysis.getPersonalizationLevel());
        System.out.println();

        // Status - TYPE-SAFE!
        if (result.isSuccess()) {
            System.out.println("✅ Email sent successfully!");
            System.out.println("📬 Check your inbox!");
        } else {
            Map<String, String> emailResultMap = emailPipelineResult.getEmailResult();
            System.out.println("❌ Error: " +
                    emailResultMap.getOrDefault("message", "Unknown error"));
        }

        System.out.println();
        System.out.println("=".repeat(70));
    }

    private static void displayError(ErrorResult error) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("❌ ERROR");
        System.out.println("=".repeat(70));
        System.out.println("Message: " + error.getMessage());
        if (error.getCause() != null) {
            System.out.println("Type: " + error.getCause().getClass().getSimpleName());
        }
        System.out.println("=".repeat(70));
    }
}
