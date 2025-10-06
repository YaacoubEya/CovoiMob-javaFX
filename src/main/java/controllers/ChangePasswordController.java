package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.models.User;
import tn.esprit.services.CrudUser;

import java.io.IOException;

public class ChangePasswordController {

    @FXML
    private PasswordField oldPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button toggleOldPassword;
    @FXML
    private Button toggleNewPassword;
    @FXML
    private Button toggleConfirmPassword;

    private TextField visibleOldPasswordField;
    private TextField visibleNewPasswordField;
    private TextField visibleConfirmPasswordField;

    private final CrudUser crudUser = new CrudUser();

    @FXML
    public void initialize() {
        // Créer les champs texte visibles
        createVisiblePasswordFields();

        // Configurer les boutons œil
        setupToggleButton(toggleOldPassword, oldPasswordField, visibleOldPasswordField);
        setupToggleButton(toggleNewPassword, newPasswordField, visibleNewPasswordField);
        setupToggleButton(toggleConfirmPassword, confirmPasswordField, visibleConfirmPasswordField);
    }

    private void createVisiblePasswordFields() {
        visibleOldPasswordField = new TextField();
        visibleNewPasswordField = new TextField();
        visibleConfirmPasswordField = new TextField();

        // Configurer les champs visibles pour qu'ils ressemblent aux PasswordField
        styleVisibleField(visibleOldPasswordField, oldPasswordField);
        styleVisibleField(visibleNewPasswordField, newPasswordField);
        styleVisibleField(visibleConfirmPasswordField, confirmPasswordField);

        // Ajouter les champs visibles à la scène
        addVisibleFieldsToScene();
    }

    private void styleVisibleField(TextField visibleField, PasswordField passwordField) {
        visibleField.setManaged(false);
        visibleField.setVisible(false);
        visibleField.setStyle(passwordField.getStyle());
        visibleField.setPrefWidth(passwordField.getWidth());
        visibleField.setPrefHeight(passwordField.getHeight());
    }

    private void addVisibleFieldsToScene() {
        // Ajouter les champs visibles au même parent que les PasswordField
        HBox oldPasswordContainer = (HBox) oldPasswordField.getParent();
        HBox newPasswordContainer = (HBox) newPasswordField.getParent();
        HBox confirmPasswordContainer = (HBox) confirmPasswordField.getParent();

        oldPasswordContainer.getChildren().add(visibleOldPasswordField);
        newPasswordContainer.getChildren().add(visibleNewPasswordField);
        confirmPasswordContainer.getChildren().add(visibleConfirmPasswordField);

        // Lier les propriétés text
        visibleOldPasswordField.textProperty().bindBidirectional(oldPasswordField.textProperty());
        visibleNewPasswordField.textProperty().bindBidirectional(newPasswordField.textProperty());
        visibleConfirmPasswordField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
    }

    private void setupToggleButton(Button toggleButton, PasswordField passwordField, TextField visibleField) {
        toggleButton.setOnAction(event -> {
            if (passwordField.isVisible()) {
                // Afficher le texte en clair
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                visibleField.setVisible(true);
                visibleField.setManaged(true);
                toggleButton.setText("🙈");
            } else {
                // Masquer le texte
                passwordField.setVisible(true);
                passwordField.setManaged(true);
                visibleField.setVisible(false);
                visibleField.setManaged(false);
                toggleButton.setText("👁");
            }
        });
    }

    @FXML
    private void handleChangePassword() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Les nouveaux mots de passe ne correspondent pas");
            return;
        }

        if (newPassword.length() < 8) {
            showError("Le mot de passe doit contenir au moins 8 caractères");
            return;
        }

        Integer iduser = SessionManager.getInstance().getUserId();
        CrudUser s_user = new CrudUser();
        User currentUser = s_user.getById(iduser);
        if (currentUser == null) {
            showError("Session invalide");
            return;
        }

        // Vérifier l'ancien mot de passe
        if (!BCrypt.checkpw(oldPassword, currentUser.getPassword_hash())) {
            showError("Ancien mot de passe incorrect");
            return;
        }

        // Mettre à jour le mot de passe
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        currentUser.setPassword_hash(hashedPassword);
        crudUser.update(currentUser);

        // Rediriger vers le profil
        redirectToProfile();
    }

    @FXML
    private void handleCancel() {
        // Ferme simplement la fenêtre actuelle (changement de mot de passe)
        Stage stage = (Stage) oldPasswordField.getScene().getWindow();
        stage.close();
    }

// Vous pouvez supprimer la méthode redirectToProfile() si elle n'est plus utilisée ailleurs

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void redirectToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Profile.fxml"));
            Parent root = loader.load();

            Stage profileStage = new Stage();
            profileStage.setTitle("Mon Profil");
            profileStage.setScene(new Scene(root));

            // Mode modal avec fenêtre actuelle comme parent
            profileStage.initModality(Modality.WINDOW_MODAL);
            profileStage.initOwner(oldPasswordField.getScene().getWindow());

            profileStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement du profil");
        }
    }

}