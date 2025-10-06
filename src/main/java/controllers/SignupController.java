package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import  tn.esprit.models.User;
import tn.esprit.services.CrudUser;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

public class SignupController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;

    @FXML private TextField vehiculeField;
    @FXML private CheckBox showPasswordCheckBox;  // CheckBox pour afficher/masquer le mot de passe

    @FXML private Label firstNameError;
    @FXML private Label lastNameError;
    @FXML private Label emailError;
    @FXML private Label telephoneError;
    @FXML private Label passwordError;
    @FXML private Label vehiculeError;
    @FXML private Label confirmPasswordError;

    @FXML private Hyperlink signInBtn;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;

    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordVisibleField;



    private final CrudUser crudUser = new CrudUser();

    @FXML
    private void handleSignup() {
        clearErrors();
        boolean isValid = true;

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String password = passwordField.isVisible() ? passwordField.getText() : passwordVisibleField.getText();
        String confirmPassword = confirmPasswordField.isVisible() ? confirmPasswordField.getText() : confirmPasswordVisibleField.getText();

        String vehicule = vehiculeField.getText().trim();

       /* if (firstName.isEmpty()) {
            firstNameError.setText("Le prénom est obligatoire.");
            isValid = false;
        }

        if (lastName.isEmpty()) {
            lastNameError.setText("Le nom est obligatoire.");
            isValid = false;
        }

        if (email.isEmpty() || !email.matches("^\\S+@\\S+\\.\\S+$")) {
            emailError.setText("Adresse email invalide.");
            isValid = false;
        }

        if (telephone.isEmpty() || !telephone.matches("^\\d{8}$")) {
            telephoneError.setText("Le téléphone doit contenir exactement 8 chiffres.");
            isValid = false;
        }

        if (password.isEmpty() || password.length() < 6 || !password.matches(".*[A-Z].*") || !password.matches(".*\\d.*")) {
            passwordError.setText("Mot de passe : min 6 caractères, 1 majuscule et 1 chiffre.");
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordError.setText("Les mots de passe ne correspondent pas.");
            isValid = false;
        }

        if (vehicule.length() > 50) {
            vehiculeError.setText("La description du véhicule est trop longue.");
            isValid = false;
        }

        if (!isValid) return;
*/
        // Hash du mot de passe
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Création d'un nouvel utilisateur
        String Role="";
        User newUser = new User(firstName, lastName, email, telephone, hashedPassword, vehicule);

        crudUser.add(newUser);

        showAlert("Succès", "Utilisateur inscrit avec succès !");
        clearFields();

        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.setTitle("Inscription réussie");
    }

    @FXML
    private void switchToSignIn(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) signInBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Sign In");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        telephoneField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        vehiculeField.clear();
    }

    private void clearErrors() {
        firstNameError.setText("");
        lastNameError.setText("");
        emailError.setText("");
        telephoneError.setText("");
        passwordError.setText("");
        vehiculeError.setText("");
        confirmPasswordError.setText("");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void togglePasswordVisibility() {
        if (passwordField.isVisible()) {
            passwordVisibleField.setText(passwordField.getText());
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
        } else {
            passwordField.setText(passwordVisibleField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
        }
    }
    @FXML
    private void toggleConfirmPasswordVisibility() {
        if (confirmPasswordField.isVisible()) {
            confirmPasswordVisibleField.setText(confirmPasswordField.getText());
            confirmPasswordVisibleField.setVisible(true);
            confirmPasswordVisibleField.setManaged(true);
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
        } else {
            confirmPasswordField.setText(confirmPasswordVisibleField.getText());
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            confirmPasswordVisibleField.setVisible(false);
            confirmPasswordVisibleField.setManaged(false);
        }
    }


}
