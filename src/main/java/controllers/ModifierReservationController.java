package controllers;

import tn.esprit.models.ReserverEvent;
import tn.esprit.services.ServiceReservationEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

public class ModifierReservationController {

    @FXML private Label eventTitleLabel;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Label totalAmountLabel;

    private ReserverEvent reservation;
    private final ServiceReservationEvent reservationService = new ServiceReservationEvent();

    public void setReservationData(ReserverEvent reservation) {
        this.reservation = reservation;

        // Initialiser les champs avec les données de la réservation
        eventTitleLabel.setText(reservation.getEventTitle());

        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, reservation.getQuantity());
        quantitySpinner.setValueFactory(valueFactory);

        updateTotalAmount();

        // Écouteur pour mettre à jour le montant total quand la quantité change
        quantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateTotalAmount();
        });
    }

    private void updateTotalAmount() {
        double pricePerTicket = reservation.getTotalAmount() / reservation.getQuantity();
        double newTotal = pricePerTicket * quantitySpinner.getValue();
        totalAmountLabel.setText(String.format("%.2f TND", newTotal));
    }

    @FXML
    private void handleSave() {
        try {
            // Mettre à jour la réservation
            reservation.setQuantity(quantitySpinner.getValue());

            double pricePerTicket = reservation.getTotalAmount() / reservation.getQuantity();
            reservation.setTotalAmount(pricePerTicket * quantitySpinner.getValue());

            reservationService.update(reservation);

            // Fermer la fenêtre
            ((Stage) eventTitleLabel.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) eventTitleLabel.getScene().getWindow()).close();
    }
}