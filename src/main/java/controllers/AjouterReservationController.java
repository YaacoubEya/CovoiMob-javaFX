package controllers;

import tn.esprit.models.Vehicule;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class AjouterReservationController {

    @FXML
    private TextField nomClientField;

    @FXML
    private TextField destinationField;

    @FXML
    private DatePicker dateReservationPicker;

    @FXML
    private Button btnAjouter;

    @FXML
    private void initialize() {
        btnAjouter.setOnAction(e -> ajouterReservation());
    }

    private class VehiculeListCell extends ListCell<Vehicule> {
        @Override
        protected void updateItem(Vehicule vehicule, boolean empty) {
            super.updateItem(vehicule, empty);
            if (empty || vehicule == null) {
                setGraphic(null);
            } else {
                setGraphic(createVehiculeCard(vehicule));
            }
        }

        private AnchorPane createVehiculeCard(Vehicule vehicule) {
            AnchorPane card = new AnchorPane();
            card.getStyleClass().add("vehicule-card");

            HBox content = new HBox(15);
            content.getStyleClass().add("vehicule-content");

            // Détails du véhicule
            VBox details = new VBox(5);
            details.getStyleClass().add("vehicule-details");

            Label typeLabel = new Label("Type: " + vehicule.getType_vehicule());
            typeLabel.getStyleClass().add("vehicule-type");

            Label modeleLabel = new Label("Modèle: " + vehicule.getModele());
            modeleLabel.getStyleClass().add("vehicule-modele");

            Label prixLabel = new Label(String.format("Prix: %s DT/heure - %s DT/jour",
                    vehicule.getPrix_par_heure(), vehicule.getPrix_par_jour()));
            prixLabel.getStyleClass().add("vehicule-prix");

            Label dispoLabel = new Label("Disponibilité: " +
                    (vehicule.getDisponibilite() != null ? vehicule.getDisponibilite() : "Disponible"));
            dispoLabel.getStyleClass().add("vehicule-dispo");

            details.getChildren().addAll(typeLabel, modeleLabel, prixLabel, dispoLabel);
            content.getChildren().addAll(details);

            // Bouton Réserver
            Button reserverButton = new Button("Réserver");
            reserverButton.getStyleClass().add("reserve-button");
            reserverButton.setOnAction(e -> handleReservation(vehicule));

            card.getChildren().addAll(content, reserverButton);
            AnchorPane.setRightAnchor(reserverButton, 20.0);
            AnchorPane.setTopAnchor(reserverButton, 30.0);
            AnchorPane.setLeftAnchor(content, 15.0);
            AnchorPane.setTopAnchor(content, 15.0);
            AnchorPane.setBottomAnchor(content, 15.0);

            return card;
        }

        private void handleReservation(Vehicule vehicule) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReserverVehicule.fxml"));
                Parent root = loader.load();

                ReserverVehiculeController controller = loader.getController();
                controller.setVehicule(vehicule);

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Réservation - " + vehicule.getModele());
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible d'ouvrir l'interface de réservation");
            }
        }
    }

    private void ajouterReservation() {
        String nom = nomClientField.getText();
        String destination = destinationField.getText();
        LocalDate date = dateReservationPicker.getValue();

        if (nom.isEmpty() || destination.isEmpty() || date == null) {
            showAlert(Alert.AlertType.ERROR, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        System.out.println("Réservation ajoutée : " + nom + ", " + destination + ", " + date);
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Réservation ajoutée avec succès !");
        clearForm();
    }

    private void clearForm() {
        nomClientField.clear();
        destinationField.clear();
        dateReservationPicker.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}