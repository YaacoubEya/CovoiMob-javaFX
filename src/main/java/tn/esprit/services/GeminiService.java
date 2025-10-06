package tn.esprit.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class GeminiService {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String API_KEY = "AIzaSyA1ijKWS3dmkRNL4hRP1YOnobE2TOBh8M0"; // Remplace ici avec ta vraie clé
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent?key=" + API_KEY;

    public static String getChatResponse(String message) throws IOException {
        if (!isQuestionAllowed(message)) {
            return "❌ Désolé, je ne peux répondre qu'aux questions concernant les voitures, le covoiturage ou comment utiliser l'application.";
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(API_URL);
            request.setHeader("Content-Type", "application/json");

            ObjectNode body = mapper.createObjectNode();
            ObjectNode content = mapper.createObjectNode();
            ObjectNode part = mapper.createObjectNode();
            part.put("text", message);
            content.putArray("parts").add(part);
            body.putArray("contents").add(content);

            request.setEntity(new StringEntity(mapper.writeValueAsString(body)));

            String response = EntityUtils.toString(httpClient.execute(request).getEntity());

            System.out.println("Réponse brute de l'API : " + response);

            JsonNode jsonResponse = mapper.readTree(response);

            if (jsonResponse.has("candidates") && jsonResponse.get("candidates").isArray() && jsonResponse.get("candidates").size() > 0) {
                JsonNode candidate = jsonResponse.get("candidates").get(0);
                JsonNode parts = candidate.path("content").path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }
            return "Aucune réponse générée par Gemini.";
        }
    }

    // 👉👉 Ajouter cette méthode ici
    private static boolean isQuestionAllowed(String message) {
        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("voiture") ||
                lowerMessage.contains("covoiturage") ||
                lowerMessage.contains("application") ||
                lowerMessage.contains("trajet") ||
                lowerMessage.contains("réserver") ||
                lowerMessage.contains("annuler") ||
                lowerMessage.contains("conducteur") ||
                lowerMessage.contains("passager");
    }
}
