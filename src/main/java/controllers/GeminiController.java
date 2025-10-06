package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.services.GeminiService;
import java.util.Map;
import java.util.HashMap;

public class GeminiController {
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField userInput;

    // Réponses par défaut
    private static final String[][] DEFAULT_RESPONSES = {
            {"bonjour", "Salut ! Comment puis-je vous aider aujourd'hui ? 😊"},
            {"salut", "Bonjour ! En quoi puis-je vous être utile ?"},
            {"merci", "Je vous en prie ! N'hésitez pas si vous avez d'autres questions."},
            {"aide", "Je peux vous aider avec des informations sur notre application, le covoiturage et bien plus. Posez-moi votre question !"},
            {"covoiturage", "Nous offrons un service de covoiturage sécurisé. Vous pouvez publier un trajet ou en rechercher un dans notre section dédiée."},
            {"prix", "Les prix des trajets varient selon la distance et le conducteur. Vous verrez les détails lors de la recherche."},
            {"réservation", "Pour réserver un trajet, sélectionnez-le dans la liste et cliquez sur 'Réserver'."}
    };

    // Cache des réponses
    private final Map<String, String> responseCache = new HashMap<>();

    @FXML
    private void initialize() {
        chatArea.appendText("Assistant : Bonjour ! Je suis votre assistant virtuel. Posez-moi vos questions sur notre service de covoiturage.\n\n");
    }

    @FXML
    private void handleSendMessage() {
        final String message = userInput.getText().trim().toLowerCase(); // Rendue effectivement finale
        if (message.isEmpty()) return;

        chatArea.appendText("Vous : " + message + "\n\n");
        userInput.clear();

        // 1. Vérifier les réponses par défaut
        String defaultResponse = checkDefaultResponses(message);
        if (defaultResponse != null) {
            chatArea.appendText("Assistant : " + defaultResponse + "\n\n");
            return;
        }

        // 2. Vérifier le cache
        if (responseCache.containsKey(message)) {
            chatArea.appendText("Assistant : " + responseCache.get(message) + "\n\n");
            return;
        }

        // 3. Utiliser Gemini si aucune réponse disponible
        new Thread(() -> {
            try {
                javafx.application.Platform.runLater(() ->
                        chatArea.appendText("Assistant : Je réfléchis...\n")
                );

                final String frenchPrompt = "[Réponds en français] " + message; // Rendue effectivement finale
                final String geminiResponse = GeminiService.getChatResponse(frenchPrompt); // Rendue effectivement finale

                // Nettoyage de la réponse
                String response = geminiResponse.trim()
                        .replace("\n", " ")
                        .replaceAll(" +", " ");

                if (response.length() > 500) {
                    response = response.substring(0, 497) + "...";
                }

                if (!response.endsWith(".") && !response.endsWith("!") && !response.endsWith("?")) {
                    response += ".";
                }

                responseCache.put(message, response);

                final String finalResponse = response; // Variable finale pour l'expression lambda
                javafx.application.Platform.runLater(() -> {
                    chatArea.setText(chatArea.getText().replace("Assistant : Je réfléchis...\n", ""));
                    chatArea.appendText("Assistant : " + finalResponse + "\n\n");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    chatArea.setText(chatArea.getText().replace("Assistant : Je réfléchis...\n", ""));
                    chatArea.appendText("Assistant : Désolé, service temporairement indisponible\n\n");
                });
                e.printStackTrace();
            }
        }).start();
    }

    private String checkDefaultResponses(String message) {
        for (String[] pair : DEFAULT_RESPONSES) {
            if (message.contains(pair[0])) {
                return pair[1];
            }
        }
        return null;
    }
}