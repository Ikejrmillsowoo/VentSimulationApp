package com.ventsim.ventsim.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ventsim.ventsim.model.Abg;
import com.ventsim.ventsim.model.Scenario;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class FeedbackEngine {

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

//    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";
//    private static final String MODEL = "claude-3-5-sonnet-20241022";
private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";

    private static final Logger log = LoggerFactory.getLogger(FeedbackEngine.class); // ← add this


    public FeedbackEngine(@Value("${openai.api_key}") String apiKey) {
        this.apiKey = apiKey;
        System.err.println(">>> API KEY STARTS WITH: " + apiKey.substring(0, 10));

        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public String feedback(Scenario sc, Abg a) {
        if (ValidationRules.isFatal(a)) return "Patient did not make it.";

        String clinicalContext = buildClinicalContext(sc, a);

        try {
            return callLlm(clinicalContext);
        } catch (Exception e) {
            return fallbackFeedback(sc, a);
        }
    }

    public String status(Abg a) {
        if (ValidationRules.isFatal(a)) return "critical";
        if (ValidationRules.isAbnormal(a)) return "warning";
        return "ok";
    }

    private String buildClinicalContext(Scenario sc, Abg a) {
        StringBuilder ctx = new StringBuilder();
        ctx.append("Scenario: ").append(sc.name()).append("\n");
        ctx.append("ABG Results:\n");
        ctx.append("  pH:     ").append(a.pH()).append("\n");
        ctx.append("  PaCO2:  ").append(a.paCO2()).append(" mmHg\n");
        ctx.append("  PaO2:   ").append(a.paO2()).append(" mmHg\n");
        ctx.append("\nPreliminary observations:\n");
        if (a.paCO2() > 55) ctx.append("- Hypoventilation detected (PaCO2 > 55)\n");
        if (a.paCO2() < 30) ctx.append("- Hyperventilation detected (PaCO2 < 30)\n");
        if (a.paO2()  < 60) ctx.append("- Hypoxemia detected (PaO2 < 60)\n");
        if (a.pH()    < 7.25) ctx.append("- Significant acidosis (pH < 7.25)\n");
        if (a.pH()    > 7.55) ctx.append("- Significant alkalemia (pH > 7.55)\n");
        return ctx.toString();
    }

    private String callLlm(String clinicalContext) throws Exception {
        String systemPrompt = """
                You are a critical care respiratory specialist assisting clinicians managing
                mechanically ventilated patients. Given ABG results and the clinical scenario,
                provide concise, actionable feedback (2–4 sentences). Use clinical language.
                Do not repeat the raw numbers back; focus on interpretation and next steps.
                """;

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", MODEL);
        body.put("max_tokens", 300);

        var messages = objectMapper.createArrayNode();
        messages.add(objectMapper.createObjectNode()
                .put("role", "system")
                .put("content", systemPrompt));
        messages.add(objectMapper.createObjectNode()
                .put("role", "user")
                .put("content", clinicalContext));
        body.set("messages", messages);

        String requestBody = objectMapper.writeValueAsString(body);
        System.out.println(">>> ANTHROPIC REQUEST BODY: " + requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(">>> :" + response.body());
        if (response.statusCode() != 200) {
            System.out.println(">>> OPENAI ERROR BODY: " + response.body());
            throw new RuntimeException("Openai API error: " + response.statusCode());
        }



        Map<?, ?> parsed = objectMapper.readValue(response.body(), Map.class);
        List<?> choices = (List<?>) parsed.get("choices");
        Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
        Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
//        System.out.println("LLM response: " + firstBlock.get("text"));
        return ((String) message.get("content")).trim();
    }

    private String fallbackFeedback(Scenario sc, Abg a) {
        StringBuilder sb = new StringBuilder();
        if (a.paCO2() > 55) sb.append("Hypoventilation: consider ↑RR or ↑Vt (watch plateau/auto-PEEP). ");
        if (a.paCO2() < 30) sb.append("Hyperventilation: risk of alkalosis; consider ↓RR or ↓Vt. ");
        if (a.paO2()  < 60) sb.append("Hypoxemia: consider ↑FiO2 or ↑PEEP. ");
        if (a.pH()    < 7.25) sb.append("Acidotic: correct ventilation; evaluate metabolic status. ");
        if (a.pH()    > 7.55) sb.append("Alkalemia: reduce minute ventilation if appropriate. ");
        if (sc == Scenario.ARDS)   sb.append("ARDS: favor low Vt (4–6 mL/kg), higher PEEP strategy. ");
        if (sc == Scenario.COPD)   sb.append("COPD: avoid air trapping; allow longer TE and moderate PEEP. ");
        if (sc == Scenario.ASTHMA) sb.append("Asthma: long expiration, permissive hypercapnia may be acceptable. ");
        if (sb.length() == 0) sb.append("Parameters within acceptable range. Continue monitoring.");
        return sb.toString().trim();
    }
}