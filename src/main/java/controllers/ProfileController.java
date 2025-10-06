package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.models.User;

import java.io.IOException;

public class ProfileController {

    @FXML private Label firstNameLabel;
    @FXML private Label lastNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label roleLabel;
    @FXML private Label vehicleLabel;
    @FXML private Label verifiedLabel;

    // Suppression de mainContentPane car nous allons changer toute la scène
    // private Pane mainContentPane;

    public void initialize() {
        User user = LoginController.connectedUser;
        if (user != null) {
            firstNameLabel.setText(user.getFirst_name());
            lastNameLabel.setText(user.getLast_name());
            emailLabel.setText(user.getEmail());
            phoneLabel.setText(user.getTelephone());
            roleLabel.setText(user.getRole());
            vehicleLabel.setText(user.getVehicule() != null ? user.getVehicule() : "Aucun véhicule");
            verifiedLabel.setText(user.isVerified() ? "Oui" : "Non");
        } else {
            redirectToLogin();
        }
    }

    @FXML
    private void handleBack() {
        // Ferme simplement la fenêtre actuelle (page de profil)
        Stage stage = (Stage) firstNameLabel.getScene().getWindow();
        stage.close();
    }  private void redirectToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) firstNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Erreur lors de la redirection vers le login: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditProfile() {
        loadView("/EditProfile.fxml", "Modifier le profil");
    }

    @FXML
    private void handleChangePassword() {
        loadView("/ChangePassword.fxml", "Changer le mot de passe");
    }

    private void loadView(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) firstNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la vue: " + fxmlPath);
        }
    }
}