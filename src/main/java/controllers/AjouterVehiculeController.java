package controllers;

import tn.esprit.models.Vehicule;
import tn.esprit.services.ServiceVehicule;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AjouterVehiculeController implements Initializable {

    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField modeleField;
    @FXML private TextField prixHeureField;
    @FXML private TextField prixJourField;
    @FXML private ComboBox<String> dispoCombo;
    @FXML private TextField lieuRetraitField;
    @FXML private Label imagePathLabel;

    private String imagePath;
    private Vehicule vehiculeToEdit;
    private final ServiceVehicule serviceVehicule = new ServiceVehicule();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation des ComboBox
        typeCombo.getItems().addAll("Voiture", "Moto", "Vélo", "Camion");
        dispoCombo.getItems().addAll("Matin", "Nuit", "Journée complète");

        // Validation numérique pour les prix
        prixHeureField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([,]\\d*)?")) {
                prixHeureField.setText(oldValue);
            }
        });

        prixJourField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([,]\\d*)?")) {
                prixJourField.setText(oldValue);
            }
        });
    }

    public void setVehiculeToEdit(Vehicule vehicule) {
        this.vehiculeToEdit = vehicule;
        // Pré-remplir les champs avec les données du véhicule à modifier
        typeCombo.setValue(vehicule.getType_vehicule());
        modeleField.setText(vehicule.getModele());
        prixHeureField.setText(vehicule.getPrix_par_heure());
        prixJourField.setText(vehicule.getPrix_par_jour());
        dispoCombo.setValue(vehicule.getDisponibilite());
        lieuRetraitField.setText(vehicule.getLieu_retrait());
        if (vehicule.getImageUrl() != null && !vehicule.getImageUrl().isEmpty()) {
            imagePathLabel.setText(vehicule.getImageUrl());
            imagePath = vehicule.getImageUrl();
        }
    }

    @FXML
    void handleAddVehicule(ActionEvent event) {
        try {
            if (typeCombo.getValue() == null
                    || modeleField.getText().isEmpty()
                    || prixHeureField.getText().isEmpty()
                    || prixJourField.getText().isEmpty()
                    || dispoCombo.getValue() == null
                    || lieuRetraitField.getText().isEmpty()) {

                throw new IllegalArgumentException("Tous les champs obligatoires doivent être remplis");
            }

            if (vehiculeToEdit == null) {
                // Mode ajout
                Vehicule vehicule = new Vehicule(
                        typeCombo.getValue(),
                        modeleField.getText(),
                        "Non spécifié",
                        prixHeureField.getText(),
                        prixJourField.getText(),
                        dispoCombo.getValue(),
                        lieuRetraitField.getText()
                );

                if (imagePath != null && !imagePath.isEmpty()) {
                    vehicule.setImageUrl(imagePath);
                }

                serviceVehicule.add(vehicule);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Véhicule ajouté avec succès!");
            } else {
                // Mode modification
                vehiculeToEdit.setType_vehicule(typeCombo.getValue());
                vehiculeToEdit.setModele(modeleField.getText());
                vehiculeToEdit.setPrix_par_heure(prixHeureField.getText());
                vehiculeToEdit.setPrix_par_jour(prixJourField.getText());
                vehiculeToEdit.setDisponibilite(dispoCombo.getValue());
                vehiculeToEdit.setLieu_retrait(lieuRetraitField.getText());

                if (imagePath != null && !imagePath.isEmpty()) {
                    vehiculeToEdit.setImageUrl(imagePath);
                }

                serviceVehicule.update(vehiculeToEdit);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Véhicule modifié avec succès!");
            }

            clearFields();
            // Retour à la vue principale
            returnToBackVehiculeView(event);

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", e.getMessage());
        } catch (Exception e) {
            System.out.println("Erreur lors de l'opération: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'opération: " + e.getMessage());

            e.printStackTrace();
        }
    }

    @FXML
    void handleBrowseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Images (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            imagePath = file.getAbsolutePath();
            imagePathLabel.setText(file.getName());
        }
    }

    private void clearFields() {
        typeCombo.getSelectionModel().clearSelection();
        modeleField.clear();
        prixHeureField.clear();
        prixJourField.clear();
        dispoCombo.getSelectionModel().clearSelection();
        lieuRetraitField.clear();
        imagePathLabel.setText("Aucun fichier sélectionné");
        imagePath = null;
        vehiculeToEdit = null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void returnToBackVehiculeView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackVehiculeView.fxml"));
            Parent root = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner à la vue principale");
        }
    }

    @FXML
    void handleReservationsView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackReservationView.fxml"));
            Parent root = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier FXML introuvable: " + e.getMessage());
        }
    }
    @FXML
    public void handleAllVehiculesView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackVehiculeView.fxml"));
            Parent root = loader.load();
            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }
}