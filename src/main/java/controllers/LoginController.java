package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.models.User;
import tn.esprit.services.CrudUser;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Hyperlink signupLink;

    private final CrudUser crudUser = new CrudUser();

    public static User connectedUser;
    public static String token;

    @FXML
    void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            errorLabel.setVisible(true);
            return;
        }

        List<User> users = crudUser.getAll();

        for (User user : users) {
            if (user.getEmail().equals(email)) {
                if (BCrypt.checkpw(password, user.getPassword_hash())) {
                    try {
                        connectedUser = user;
                        System.out.println("✅ user : " + user);
                        token = UUID.randomUUID().toString();
                        System.out.println("✅ Token généré : " + token); // Removed duplicate line

                        // Create UserTokenData
                        TokenStorage.UserTokenData userTokenData = new TokenStorage.UserTokenData(
                                connectedUser.getId(),
                                connectedUser.getEmail(),
                                connectedUser.getFirst_name(),
                                connectedUser.getLast_name(),
                                connectedUser.getTelephone(),
                                connectedUser.getRole(), // Use getter
                                connectedUser.isVerified(), // Use actual verified value
                                connectedUser.getVehicule(), // Use getter
                                token
                        );

                        // Store the user data and token
                        long expirationTime = System.currentTimeMillis() + TokenStorage.TOKEN_VALIDITY_MS;
                        TokenStorage.storeUserToken(userTokenData, expirationTime);
                        System.out.println("✅ User data and token stored successfully for user ID: " + user.getId());
                        SessionManager.getInstance().setUserId(connectedUser.getId());


                        String role = user.getRole();
                        String fxmlPath;
                        String windowTitle;

                        if ("UTILISATEUR_NORMAL".equalsIgnoreCase(role)) {
                            fxmlPath = "/Acceuil.fxml";
                            windowTitle = "Bienvenue Utilisateur";
                        } else {
                            fxmlPath = "/AllOffre.fxml";
                            windowTitle = "Dashboard Admin";
                        }

                        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                        Parent root = loader.load();

                        Stage stage = (Stage) emailField.getScene().getWindow();
                        stage.setScene(new Scene(root, 1500, 765));
                        stage.setTitle(windowTitle);
                        stage.show();

                    } catch (IOException e) {
                        e.printStackTrace();
                        errorLabel.setText("Erreur de chargement de la page.");
                        errorLabel.setVisible(true);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }
            }
        }

        errorLabel.setText("Email ou mot de passe incorrect.");
        errorLabel.setVisible(true);
    }

    @FXML
    private void switchToSignup(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SignUp.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Créer un compte");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forgotpassword.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mot de passe oublié");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
