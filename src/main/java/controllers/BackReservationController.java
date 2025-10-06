package controllers;

import tn.esprit.models.ReservationVehicule;
import tn.esprit.models.Vehicule;
import tn.esprit.services.NotificationService;
import tn.esprit.services.ServiceReservationVehicule;
import tn.esprit.services.ServiceVehicule;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class BackReservationController implements Initializable {

    @FXML private TableView<ReservationVehicule> reservationsTable;
    @FXML private TableColumn<ReservationVehicule, String> dateDebutCol;
    @FXML private TableColumn<ReservationVehicule, String> dateFinCol;
    @FXML private TableColumn<ReservationVehicule, String> prixCol;
    @FXML private TableColumn<ReservationVehicule, String> statusCol;
    @FXML private TableColumn<ReservationVehicule, Void> actionsCol;

    @FXML private Label totalReservationsLabel;
    @FXML private Label confirmedReservationsLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label mostReservedVehicleLabel;
    @FXML private VBox chartContainer;
    @FXML private Button btnReservations;

    private final ServiceReservationVehicule reservationService = new ServiceReservationVehicule();
    private final ObservableList<ReservationVehicule> reservations = FXCollections.observableArrayList();
    private final Timer notificationTimer = new Timer(true);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadReservations();
        startNotificationChecker();
    }

    private void setupTable() {
        dateDebutCol.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(new SimpleDateFormat("yyyy-MM-dd")
                        .format(java.sql.Date.valueOf(cellData.getValue().getDate_debut())));
            } catch (Exception e) {
                return new SimpleStringProperty("");
            }
        });

        dateFinCol.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(new SimpleDateFormat("yyyy-MM-dd")
                        .format(java.sql.Date.valueOf(cellData.getValue().getDate_fin())));
            } catch (Exception e) {
                return new SimpleStringProperty("");
            }
        });

        prixCol.setCellValueFactory(new PropertyValueFactory<>("prix_total"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    setStyle("-fx-text-fill: " + ("CONFIRMEE".equals(status) ? "green" : "orange"));
                }
            }
        });

        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Supprimer");
            {
                deleteBtn.setOnAction(event -> {
                    ReservationVehicule r = getTableView().getItems().get(getIndex());
                    confirmAndDelete(r);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });
    }

    private void loadReservations() {
        reservations.setAll(reservationService.getAll());
        reservationsTable.setItems(reservations);
        updateStatistics();
        updateSeasonStats();
    }

    private void updateSeasonStats() {
        chartContainer.getChildren().clear();

        Map<String, Long> seasonCounts = reservationService.getReservationsBySeason();

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Saisons");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nombre de réservations");

        BarChart<String, Number> seasonChart = new BarChart<>(xAxis, yAxis);
        seasonChart.setTitle("Réservations par saison");
        seasonChart.setLegendVisible(false);
        seasonChart.setAnimated(false);

        XYChart.Series<String, Number> seasonSeries = new XYChart.Series<>();
        seasonSeries.setName("Réservations");

        seasonCounts.forEach((season, count) ->
                seasonSeries.getData().add(new XYChart.Data<>(season, count))
        );

        seasonChart.getData().add(seasonSeries);
        seasonChart.setPrefWidth(600);
        seasonChart.setPrefHeight(400);
        seasonChart.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15;");

        seasonChart.getData().forEach(series -> {
            series.getData().forEach(data -> {
                String season = data.getXValue();
                Node node = data.getNode();

                switch(season) {
                    case "Hiver":
                        node.setStyle("-fx-bar-fill: #5DADE2;");
                        break;
                    case "Printemps":
                        node.setStyle("-fx-bar-fill: #58D68D;");
                        break;
                    case "Été":
                        node.setStyle("-fx-bar-fill: #F39C12;");
                        break;
                    case "Automne":
                        node.setStyle("-fx-bar-fill: #A569BD;");
                        break;
                }
            });
        });

        chartContainer.getChildren().add(seasonChart);
    }

    private void confirmAndDelete(ReservationVehicule reservation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la réservation");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réservation ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                reservationService.delete(reservation);
                loadReservations();
            }
        });
    }

    @FXML
    private void handleGestionVehicules(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackVehiculeView.fxml"));
            Parent root = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la gestion des véhicules");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void startNotificationChecker() {
        notificationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkNewReservations();
            }
        }, 0, 30000);
    }

    private void checkNewReservations() {
        List<ReservationVehicule> currentReservations = reservationService.getAll();

        Platform.runLater(() -> {
            if (!reservations.equals(currentReservations)) {
                int newCount = currentReservations.size() - reservations.size();
                if (newCount > 0) {
                    NotificationService.showNotification("Nouvelle réservation",
                            newCount + " nouvelle(s) réservation(s) a(ont) été ajoutée(s)!");
                    loadReservations();
                }
                updateNotificationCount();
            }
        });
    }

    public void stopNotificationChecker() {
        notificationTimer.cancel();
    }

    private void updateNotificationCount() {
        long newReservations = reservationService.getAll().stream()
                .filter(r -> "CONFIRMEE".equals(r.getStatus()))
                .filter(r -> r.getDate_debut().isAfter(LocalDate.now().minusDays(1)))
                .count();

        Platform.runLater(() -> {
            btnReservations.setText("📅 Réservations (" + newReservations + ")");

            if (newReservations > 0) {
                btnReservations.setStyle("-fx-background-color: #e74c3c; " +
                        btnReservations.getStyle().replace("-fx-background-color: #3498db;", ""));
            } else {
                btnReservations.setStyle(btnReservations.getStyle().replace(
                        "-fx-background-color: #e74c3c;", "-fx-background-color: #3498db;"));
            }
        });
    }

    private void updateStatistics() {
        List<ReservationVehicule> allReservations = reservationService.getAll();

        int totalReservations = allReservations.size();
        totalReservationsLabel.setText(String.valueOf(totalReservations));

        long confirmedCount = allReservations.stream()
                .filter(r -> "CONFIRMEE".equals(r.getStatus()))
                .count();
        confirmedReservationsLabel.setText(String.valueOf(confirmedCount));

        double totalRevenue = allReservations.stream()
                .mapToDouble(ReservationVehicule::getPrix_total)
                .sum();
        totalRevenueLabel.setText(String.format("%.2f DT", totalRevenue));

        String mostReserved = reservationService.getMostReservedVehicle();
        mostReservedVehicleLabel.setText(mostReserved != null ? mostReserved : "-");
    }

    private void handleReservationsView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackReservationView.fxml"));
            Parent root = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la vue des réservations");
        }
    }

    private void handleAddVehicule(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterVehicule.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un véhicule");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout");
        }
    }

    @FXML
    private void handleAllOffresView(ActionEvent event) {
        System.out.println("handleAllOffresView called in BackReservationController");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AllOffre.fxml"));
            Parent root = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la vue des offres : " + e.getMessage());
        }
    }
}