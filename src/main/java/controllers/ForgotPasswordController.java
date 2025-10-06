package controllers;

import  tn.esprit.models.User;
import tn.esprit.services.CrudUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class ForgotPasswordController {

    @FXML
    private TextField emailOrPhoneField;

    @FXML
    private Label feedbackLabel;

    @FXML
    void handleSearch(ActionEvent event) {
        String input = emailOrPhoneField.getText().trim();
        if (input.isEmpty()) {
            feedbackLabel.setText("Veuillez entrer une adresse email ou un numéro.");
            feedbackLabel.setVisible(true);
            return;
        }

        CrudUser crudUser = new CrudUser();
        User user = crudUser.findByEmailOrPhone(input);

        if (user != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/renitialiser.fxml"));
                Parent root = loader.load();

                RenitialiserMotDePasseController controller = loader.getController();
                controller.setUser(user);

                Stage stage = (Stage) emailOrPhoneField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Réinitialiser le mot de passe");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            feedbackLabel.setText("Aucun résultat de recherche.");
            feedbackLabel.setVisible(true);
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailOrPhoneField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
