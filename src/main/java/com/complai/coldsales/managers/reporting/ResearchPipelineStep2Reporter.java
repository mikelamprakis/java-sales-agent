package com.complai.coldsales.managers.reporting;

public class ResearchPipelineStep2Reporter extends StepReporter<String> {
    
    private final String companyName;
    
    public ResearchPipelineStep2Reporter(String companyName) {
        this.companyName = companyName;
    }
    
    @Override
    protected void startLog() {
        System.out.println("📝 Building research summary for " + companyName + "...");
    }
    
    @Override
    protected void completeLogWithDuration(String result, long durationMs) {
        System.out.println("✅ Research summary ready (" + durationMs + " ms)");
        System.out.println();
    }
}
