package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.models.User;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FrontController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Token actuel : " + LoginController.token);
        User user = LoginController.connectedUser;

        if (user != null) {
            String message = "Bienvenue " + user.getFirst_name() + " " + user.getLast_name();
            welcomeLabel.setText(message);
        } else {
            welcomeLabel.setText("Bienvenue !");
            System.out.println("Aucun utilisateur connecté !");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Clear the connected user and token
            LoginController.connectedUser = null;
            LoginController.token = null;

            // Load the login view
            URL loginView = getClass().getResource("Login.fxml");
            if (loginView == null) {
                throw new IOException("Login.fxml not found");
            }
            Parent root = FXMLLoader.load(loginView);
            Scene scene = new Scene(root);

            // Get the current stage
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();

            // Set the new scene
            stage.setScene(scene);
            stage.show();

            System.out.println("Déconnexion réussie");
        } catch (IOException e) {
            System.err.println("Erreur lors de la déconnexion : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
