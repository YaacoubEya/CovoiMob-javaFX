package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.models.User;
import tn.esprit.services.CrudUser;

import java.io.IOException;

public class EditProfileController {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField vehicleField;
    @FXML
    private Label errorLabel;

    private final CrudUser crudUser = new CrudUser();

    @FXML
    public void initialize() {
        User currentUser = LoginController.connectedUser;
        if (currentUser != null) {
            firstNameField.setText(currentUser.getFirst_name());
            lastNameField.setText(currentUser.getLast_name());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getTelephone());
            vehicleField.setText(currentUser.getVehicule());
        }
    }

    @FXML
    private void handleSave() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String vehicle = vehicleField.getText().trim();

        // Validation
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showError("Veuillez remplir tous les champs obligatoires");
            return;
        }

        if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showError("Email invalide");
            return;
        }

        if (!phone.matches("^[0-9]{8,15}$")) {
            showError("Numéro de téléphone invalide");
            return;
        }

        User currentUser = LoginController.connectedUser;
        if (currentUser == null) {
            showError("Session invalide");
            return;
        }

        // Mettre à jour l'utilisateur
        currentUser.setFirst_name(firstName);
        currentUser.setLast_name(lastName);
        currentUser.setEmail(email);
        currentUser.setTelephone(phone);
        currentUser.setVehicule(vehicle);

        crudUser.update(currentUser);

        // Rediriger vers le profil
        redirectToProfile();
    }

    @FXML
    private void handleCancel() {
        redirectToProfile();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void closeWindow() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }

    private void redirectToProfile() {
        try {
            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) firstNameField.getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            currentStage.close();

            // Charger la page de profil
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Profile.fxml"));
            Parent root = loader.load();

            // Ouvrir la nouvelle fenêtre
            Stage profileStage = new Stage();
            profileStage.setScene(new Scene(root, width, height));
            profileStage.setTitle("Mon Profil");
            profileStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement du profil");
        }
    }
}