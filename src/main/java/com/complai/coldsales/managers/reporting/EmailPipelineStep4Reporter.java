package com.complai.coldsales.managers.reporting;

import com.complai.coldsales.models.structured.EmailSubject;

public class EmailPipelineStep4Reporter extends StepReporter<EmailSubject> {
    @Override
    protected void startLog() {
        System.out.println("⏳ Step 4/5: Generating structured subject lines...");
    }

    @Override
    protected void completeLogWithDuration(EmailSubject subjectData, long durationMs) {
        System.out.println("✅ Step 4/5: Complete - Subject: '" + subjectData.getPrimarySubject() + "' (predicted " + subjectData.getPredictedOpenRate() + "% open rate)");
        System.out.println("   - duration: " + durationMs + " ms");
        System.out.println();
    }
    
}
