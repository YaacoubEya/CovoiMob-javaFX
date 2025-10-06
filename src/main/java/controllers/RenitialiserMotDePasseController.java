package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import java.io.IOException;

import  tn.esprit.models.User; // ← adapte ce chemin selon l'emplacement de ta classe User

public class RenitialiserMotDePasseController {

    @FXML
    private RadioButton emailOption;

    @FXML
    private RadioButton smsOption;

    @FXML
    private Label usernameLabel;

    @FXML
    private ToggleGroup choiceGroup;

    private User user;

    public void setUser(User user) {
        this.user = user;
        usernameLabel.setText(user.getFirst_name() + " " + user.getLast_name());
    }

    @FXML
    void handleCancel(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailOption.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleContinue(ActionEvent event) {
        if (emailOption.isSelected()) {
            System.out.println("Code envoyé par email à : " + user.getEmail());
        } else if (smsOption.isSelected()) {
            System.out.println("Code envoyé par SMS à : " + user.getTelephone());
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Veuillez sélectionner une option.");
            alert.show();
        }
    }
}
