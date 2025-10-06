package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import tn.esprit.models.User;
import tn.esprit.services.CrudUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

public class AjouterUtilisateurController {

    // Champs FXML
    @FXML private TextField nomField, prenomField, emailField, telephoneField, vehiculeField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private TextField passwordVisibleField, confirmPasswordVisibleField;
    @FXML private ComboBox<String> roleField;
    @FXML private CheckBox verifiedCheckBox;
    @FXML private Button enregistrerButton;
    @FXML private VBox passwordFieldsContainer;
    @FXML private Label formTitle; // Correction: Ajout de la déclaration manquante
    @FXML private VBox securitySection; // Correction: Ajout de la déclaration manquante

    private final CrudUser crudUser = new CrudUser();
    private User userToUpdate = null;

    @FXML
    public void initialize() {
        roleField.getItems().addAll("ADMIN", "UTILISATEUR_NORMAL");
        setAddMode();
    }

    private void togglePasswordFields(boolean visible) {
        if (passwordFieldsContainer != null) {
            passwordFieldsContainer.setVisible(visible);
            passwordFieldsContainer.setManaged(visible);
        }
    }

    public void setUserToUpdate(User user) {
        this.userToUpdate = user;
        remplirChamps(user);
        setEditMode();
    }

    private void setAddMode() {
        formTitle.setText("👤 Ajouter un Utilisateur");
        enregistrerButton.setText("Ajouter");
        securitySection.setVisible(true);
        passwordField.setText("");
        confirmPasswordField.setText("");
    }

    private void setEditMode() {
        formTitle.setText("👤 Modifier un Utilisateur");
        enregistrerButton.setText("Modifier");
        securitySection.setVisible(false);
    }

    private void remplirChamps(User user) {
        nomField.setText(user.getFirst_name());
        prenomField.setText(user.getLast_name());
        emailField.setText(user.getEmail());
        telephoneField.setText(user.getTelephone());
        roleField.setValue(user.getRole());
        vehiculeField.setText(user.getVehicule());
        verifiedCheckBox.setSelected(user.isVerified());
    }

    @FXML
    public void enregistrerUtilisateur() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleField.getValue();
        String telephone = telephoneField.getText().trim();
        String vehicule = vehiculeField.getText().trim();
        boolean verified = verifiedCheckBox.isSelected();

        // Validation des champs
        if (!validerChamps(nom, prenom, email, role, password, confirmPassword)) {
            return;
        }

        try {
            if (userToUpdate != null) {
                modifierUtilisateur(nom, prenom, email, telephone, role, vehicule, verified, password);
            } else {
                creerUtilisateur(email, password, role, verified, nom, prenom, telephone, vehicule);
            }
            retourALaListe();
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validerChamps(String nom, String prenom, String email, String role,
                                  String password, String confirmPassword) {
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || role == null) {
            showAlert("Champs requis", "Tous les champs obligatoires doivent être remplis", Alert.AlertType.ERROR);
            return false;
        }

        if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showAlert("Email invalide", "Veuillez entrer une adresse email valide", Alert.AlertType.ERROR);
            return false;
        }

        if (userToUpdate == null && (password.isEmpty() || confirmPassword.isEmpty())) {
            showAlert("Mot de passe requis", "Veuillez saisir et confirmer le mot de passe", Alert.AlertType.ERROR);
            return false;
        }

        if (!password.isEmpty() && password.length() < 8) {
            showAlert("Mot de passe faible", "Le mot de passe doit contenir au moins 8 caractères", Alert.AlertType.ERROR);
            return false;
        }

        if (!password.isEmpty() && !password.equals(confirmPassword)) {
            showAlert("Erreur de confirmation", "Les mots de passe ne correspondent pas", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void modifierUtilisateur(String nom, String prenom, String email, String telephone,
                                     String role, String vehicule, boolean verified, String password) {
        userToUpdate.setFirst_name(nom);
        userToUpdate.setLast_name(prenom);
        userToUpdate.setEmail(email);
        userToUpdate.setTelephone(telephone);
        userToUpdate.setRole(role);
        userToUpdate.setVehicule(vehicule);
        userToUpdate.setVerified(verified);

        if (!password.isEmpty()) {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            userToUpdate.setPassword_hash(hashedPassword);
        }

        crudUser.update(userToUpdate);
        showAlert("Succès", "Utilisateur modifié avec succès", Alert.AlertType.INFORMATION);
    }

    private void creerUtilisateur(String email, String password, String role, boolean verified,
                                  String nom, String prenom, String telephone, String vehicule) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User newUser = new User(email, hashedPassword, role, verified, nom, prenom, telephone, vehicule);
        crudUser.add(newUser);
        showAlert("Succès", "Utilisateur ajouté avec succès", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        retourALaListe();
    }

    public void retourALaListe() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ListeUtilisateurs.fxml"));
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la liste des utilisateurs", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void togglePasswordVisibility() {
        toggleFieldVisibility(passwordField, passwordVisibleField);
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        toggleFieldVisibility(confirmPasswordField, confirmPasswordVisibleField);
    }

    private void toggleFieldVisibility(PasswordField passwordField, TextField visibleField) {
        if (visibleField.isVisible()) {
            passwordField.setText(visibleField.getText());
            visibleField.setVisible(false);
            visibleField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
        } else {
            visibleField.setText(passwordField.getText());
            visibleField.setVisible(true);
            visibleField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}