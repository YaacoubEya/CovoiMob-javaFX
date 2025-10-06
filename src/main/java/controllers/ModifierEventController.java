package controllers;

import tn.esprit.services.ServicePropositionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.models.ProposerEvent;

import java.io.File;

public class ModifierEventController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField priceField;
    @FXML private TextField locationField;
    @FXML private Label fileLabel;
    @FXML private ImageView eventImageView;

    private ProposerEvent currentEvent;
    private ServicePropositionEvent eventService = new ServicePropositionEvent();

    public void setEventData(tn.esprit.models.ProposerEvent event) {
        this.currentEvent = event;

        // Remplir les champs avec les données de l'événement
        titleField.setText(event.getTitle());
        descriptionField.setText(event.getDescription());
        typeCombo.getSelectionModel().select(event.getEventType());

        // Conversion de Date vers LocalDate
        startDatePicker.setValue(event.getStartDate().toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate());

        endDatePicker.setValue(event.getEndDate().toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate());

        priceField.setText(String.valueOf(event.getPrice()));
        locationField.setText(event.getLocation());
        fileLabel.setText(event.getImageUrl());

        // Charger l'image si elle existe
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            try {
                eventImageView.setImage(new Image(event.getImageUrl()));
            } catch (Exception e) {
                System.err.println("Erreur de chargement de l'image");
            }
        }
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(fileLabel.getScene().getWindow());
        if (selectedFile != null) {
            fileLabel.setText(selectedFile.getAbsolutePath());
            eventImageView.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    private void handleUpdateEvent() {
        try {
            // Mettre à jour l'objet currentEvent avec les nouvelles valeurs
            currentEvent.setTitle(titleField.getText());
            currentEvent.setDescription(descriptionField.getText());
            currentEvent.setLocation(locationField.getText());
            currentEvent.setEventType(typeCombo.getValue());
            currentEvent.setStartDate(java.sql.Timestamp.valueOf(startDatePicker.getValue().atStartOfDay()));
            currentEvent.setEndDate(java.sql.Timestamp.valueOf(endDatePicker.getValue().atStartOfDay()));
            currentEvent.setPrice(Double.parseDouble(priceField.getText()));
            currentEvent.setImageUrl(fileLabel.getText());

            // Appeler le service de mise à jour
            eventService.update(currentEvent);

            // Fermer la fenêtre
            ((Stage) titleField.getScene().getWindow()).close();

        } catch (Exception e) {
            showAlert("Erreur", "Veuillez vérifier tous les champs obligatoires");
        }
    }

    @FXML
    private void handleAnnuler() {
        ((Stage) titleField.getScene().getWindow()).close();
    }

    @FXML
    private void handleRetourListe() {
        handleAnnuler();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}