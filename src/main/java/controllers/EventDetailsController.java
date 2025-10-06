package controllers;

import tn.esprit.models.ProposerEvent;
import tn.esprit.models.ReserverEvent;
import tn.esprit.services.ServiceReservationEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EventDetailsController {

    @FXML private Label titleLabel;
    @FXML private Label dateLabel;
    @FXML private Label locationLabel;
    @FXML private Label priceLabel;
    @FXML private Label reservationsLabel;
    @FXML private TableView<ReserverEvent> reservationsTable;

    private final ServiceReservationEvent reservationService = new ServiceReservationEvent();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public void setEventData(ProposerEvent event) {
        titleLabel.setText(event.getTitle());
        dateLabel.setText(dateFormat.format(event.getStartDate()));
        locationLabel.setText(event.getLocation());
        priceLabel.setText(String.format("%.2f TND", event.getPrice()));

        int count = reservationService.getAll().stream()
                .filter(r -> r.getEventTitle().equals(event.getTitle()))
                .toList()
                .size();
        reservationsLabel.setText(count + " réservation(s)");

        setupReservationsTable(event.getTitle());
    }

    private void setupReservationsTable(String eventTitle) {
        reservationsTable.getColumns().clear();

        TableColumn<ReserverEvent, Date> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        dateCol.setCellFactory(column -> new TableCell<ReserverEvent, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : dateFormat.format(item));
            }
        });

        TableColumn<ReserverEvent, Double> amountCol = new TableColumn<>("Montant");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        amountCol.setCellFactory(column -> new TableCell<ReserverEvent, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("%.2f TND", item));
            }
        });

        TableColumn<ReserverEvent, Integer> quantityCol = new TableColumn<>("Quantité");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<ReserverEvent, Integer> userCol = new TableColumn<>("Utilisateur");
        userCol.setCellValueFactory(new PropertyValueFactory<>("userId"));

        reservationsTable.getColumns().addAll(dateCol, quantityCol, amountCol, userCol);

        reservationsTable.getItems().setAll(
                reservationService.getAll().stream()
                        .filter(r -> r.getEventTitle().equals(eventTitle))
                        .toList()
        );
    }

}