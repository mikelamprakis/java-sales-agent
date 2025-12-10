package com.complai.coldsales.utils;

import com.complai.coldsales.agents.base.client.LLMClient;
import com.complai.coldsales.agents.base.result.LLMResult;
import com.complai.coldsales.models.structured.EmailAnalysis;
import com.complai.coldsales.models.structured.EmailSubject;
import com.complai.coldsales.models.structured.ProspectResearch;
import com.complai.coldsales.models.structured.SalesEmail;

public class ExtractorUtils {


    public static SalesEmail extractSalesEmail(LLMResult result) {
        Object output = result.getFinalOutput();
        SalesEmail email = output instanceof SalesEmail ? (SalesEmail) output : SalesEmail.fallback(output);
        // Defensive: ensure body is never null
        if (email.getBody() == null || email.getBody().isBlank()) {
            String fallbackBody = output != null ? output.toString() : "Email body not available";
            email.setBody(fallbackBody);
        }
        return email;
    }

    public static EmailAnalysis extractEmailAnalysis(LLMResult result) {
        Object output = result.getFinalOutput();
        return output instanceof EmailAnalysis ? (EmailAnalysis) output : EmailAnalysis.fallback(output);
    }

    public static EmailSubject extractSubjectData(Object output) {
        return output instanceof EmailSubject ? (EmailSubject) output : EmailSubject.fallback(output);
    }

    public static ProspectResearch extractProspectResearch(Object data) {
        if (data instanceof ProspectResearch research) {
            // Check if the ProspectResearch has all null fields (parsing failed)
            if (isNullProspectResearch(research)) {
                System.out.println("⚠️  ProspectResearch has null fields, using fallback");
                return ProspectResearch.fallback(data.toString());
            }
            return research;
        } else {
            return ProspectResearch.fallback(data.toString());
        }
    }

    private static boolean isNullProspectResearch(ProspectResearch research) {
        return research.getCompanyOverview() == null &&
                research.getCompanySize() == null &&
                research.getIndustry() == null &&
                research.getKeyPainPoints() == null &&
                research.getPersonalizationOpportunities() == null &&
                research.getRecommendedApproach() == null;
    }
}
