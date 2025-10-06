package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import tn.esprit.models.OffreCovoiturage;
import tn.esprit.services.ServiceOffreCovoiturage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class EditOffreController {

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField departField;

    @FXML
    private TextField destinationField;

    @FXML
    private Label fileLabel;

    @FXML
    private TextField matriculeField;

    @FXML
    private TextField placesField;

    @FXML
    private TextField prixField;

    @FXML
    private Button submitButton;

    @FXML
    private TextField timeField;

    @FXML
    private Button uploadButton;

    private OffreCovoiturage offre; // Offre à modifier
    private ServiceOffreCovoiturage serviceOffreCovoiturage = new ServiceOffreCovoiturage();

    // Méthode pour initialiser les champs avec les données de l'offre
    public void setOffre(OffreCovoiturage offre) {
        this.offre = offre;
        // Pré-remplir les champs
        departField.setText(offre.getDepart());
        destinationField.setText(offre.getDestination());
        matriculeField.setText(String.valueOf(offre.getMatVehicule()));
        placesField.setText(String.valueOf(offre.getPlacesDispo()));
        prixField.setText(String.valueOf(offre.getPrix()));
        fileLabel.setText(offre.getImg() != null ? offre.getImg() : "Aucun fichier choisi");

        // Gérer la date et l'heure
        LocalDateTime dateTime = offre.getDate();
        datePicker.setValue(dateTime.toLocalDate());
        timeField.setText(dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    @FXML
    void modifierOffreAction(ActionEvent event) {
        try {
            // Récupérer les nouvelles valeurs
            String depart = departField.getText();
            String destination = destinationField.getText();
            int matricule = Integer.parseInt(matriculeField.getText());
            int places = Integer.parseInt(placesField.getText());
            float prix = Float.parseFloat(prixField.getText());

            // Combiner date et heure
            LocalDate date = datePicker.getValue();
            LocalTime time = LocalTime.parse(timeField.getText()); // Format HH:mm attendu
            LocalDateTime dateTime = LocalDateTime.of(date, time);

            String img = fileLabel.getText();

            // Mettre à jour les données de l'offre
            offre.setDepart(depart);
            offre.setDestination(destination);
            offre.setMatVehicule(matricule);
            offre.setPlacesDispo(places);
            offre.setDate(dateTime);
            offre.setPrix(prix);
            offre.setImg(img.isEmpty() ? null : img);
            offre.setStatut("active"); // Conserver le statut comme dans AjouterOffreController
            offre.setConducteurId(123); // Conserver le placeholder comme dans AjouterOffreController

            // Appeler la méthode de mise à jour du service
            serviceOffreCovoiturage.update(offre);

            // Afficher une confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Offre modifiée avec succès !");
            alert.show();

            // Rediriger vers la même page que dans AjouterOffreController
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailPersonne.fxml"));
            // loader.load(); // Décommentez si vous avez besoin de charger la scène

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Échec de la modification de l'offre");
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }
}