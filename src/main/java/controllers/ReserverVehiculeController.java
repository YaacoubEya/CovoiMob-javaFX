package controllers;

import tn.esprit.models.Vehicule;
import tn.esprit.models.ReservationVehicule;
import tn.esprit.services.ServiceReservationVehicule;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.scene.image.ImageView;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.net.URL;
import javafx.scene.image.Image;
import java.io.InputStream;
public class ReserverVehiculeController {

    @FXML private Label vehiculeLabel;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private Label prixTotalLabel;
    @FXML private Button confirmerButton;
    @FXML private GridPane calendarGrid;
    @FXML private VBox calendarContainer;
    @FXML private Button prevMonthButton;
    @FXML private Button nextMonthButton;
    @FXML private Label monthYearLabel;
    @FXML
    private ImageView bikeGif;
    @FXML
    private void previousMonth() {
        currentDisplayedMonth = currentDisplayedMonth.minusMonths(1);
        updateCalendar();
    }

    @FXML
    private void nextMonth() {
        currentDisplayedMonth = currentDisplayedMonth.plusMonths(1);
        updateCalendar();
    }
    private Vehicule vehicule;
    private final ServiceReservationVehicule reservationService = new ServiceReservationVehicule();
    private LocalDate currentDisplayedMonth;
    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
        this.currentDisplayedMonth = LocalDate.now();
        updateUI();
    }

    private void updateUI() {
        vehiculeLabel.setText("Réservation pour: " + vehicule.getModele());
        setupDatePickers();
        initializeCalendar();
        updateCalendar();
    }
    private void updateCalendar() {
        calendarGrid.getChildren().clear();

        // Affichage du mois en français
        String[] moisFrancais = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        monthYearLabel.setText(moisFrancais[currentDisplayedMonth.getMonthValue()-1] + " " + currentDisplayedMonth.getYear());

        // En-têtes des jours
        String[] joursSemaine = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < joursSemaine.length; i++) {
            Label header = new Label(joursSemaine[i]);
            header.getStyleClass().add("calendar-day-header");
            calendarGrid.add(header, i, 0);
        }

        List<ReservationVehicule> reservations = reservationService.getReservationsForVehicule(vehicule.getId());
        LocalDate date = currentDisplayedMonth.withDayOfMonth(1);
        int row = 1;

        // Jours vides avant le 1er du mois
        int firstDayOfWeek = date.getDayOfWeek().getValue(); // 1 (Lundi) à 7 (Dimanche)
        for (int i = 1; i < firstDayOfWeek; i++) {
            Label empty = new Label("");
            empty.getStyleClass().add("calendar-day");
            calendarGrid.add(empty, i-1, row);
        }

        // Remplissage des jours du mois
        while (date.getMonth() == currentDisplayedMonth.getMonth()) {
            int column = date.getDayOfWeek().getValue() - 1;

            Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
            dayLabel.getStyleClass().add("calendar-day");

            if (date.equals(LocalDate.now())) {
                dayLabel.getStyleClass().add("calendar-day-today");
            }

            if (isDateReserved(date, reservations)) {
                dayLabel.getStyleClass().add("calendar-day-reserved");
            }

            calendarGrid.add(dayLabel, column, row);
            date = date.plusDays(1);

            if (date.getDayOfWeek().getValue() == 1) {
                row++;
            }
        }

        // Contrôle des boutons de navigation
        prevMonthButton.setDisable(currentDisplayedMonth.getMonth().equals(LocalDate.now().getMonth()) &&
                currentDisplayedMonth.getYear() == LocalDate.now().getYear());
    }

    private void setupDatePickers() {
        dateDebutPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        dateDebutPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            dateFinPicker.setValue(null);
            if (newVal != null) {
                dateFinPicker.setDisable(false);
                setupFinDatePicker(newVal);
            } else {
                dateFinPicker.setDisable(true);
            }
        });

        dateFinPicker.setDisable(true);
    }
    private void initializeCalendar() {
        try {
            InputStream gifStream = getClass().getResourceAsStream("/images/gif.gif");
            if (gifStream != null) {
                Image gifImage = new Image(gifStream);
                bikeGif.setImage(gifImage);
            } else {
                System.err.println("La GIF n'a pas pu être chargée");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la GIF: " + e.getMessage());
        }
        calendarGrid.getChildren().clear();

        // Ajouter les en-têtes de jours
        String[] joursSemaine = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < joursSemaine.length; i++) {
            Label header = new Label(joursSemaine[i]);
            header.getStyleClass().add("calendar-day-header");
            calendarGrid.add(header, i, 0);
        }

        // Obtenir les réservations existantes pour ce véhicule
        List<ReservationVehicule> reservations = reservationService.getReservationsForVehicule(vehicule.getId());

        // Créer le calendrier
        LocalDate today = LocalDate.now();
        LocalDate date = today.withDayOfMonth(1);
        int row = 1;

        while (date.getMonth() == today.getMonth()) {
            for (int i = 0; i < 7; i++) {
                if (date.getMonth() == today.getMonth()) {
                    Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
                    dayLabel.getStyleClass().add("calendar-day");

                    // Marquer aujourd'hui
                    if (date.equals(today)) {
                        dayLabel.getStyleClass().add("calendar-day-today");
                    }

                    // Marquer les jours réservés
                    if (isDateReserved(date, reservations)) {
                        dayLabel.getStyleClass().add("calendar-day-reserved");
                    }

                    calendarGrid.add(dayLabel, i, row);
                    date = date.plusDays(1);
                }
            }
            row++;
        }
    }

    private void setupFinDatePicker(LocalDate minDate) {
        dateFinPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(minDate));
            }
        });
    }
    private boolean isDateReserved(LocalDate date, List<ReservationVehicule> reservations) {
        for (ReservationVehicule res : reservations) {
            LocalDate debut = res.getDate_debut(); // Déjà en LocalDate
            LocalDate fin = res.getDate_fin();     // Déjà en LocalDate

            if (!date.isBefore(debut) && !date.isAfter(fin)) {
                return true;
            }
        }
        return false;
    }    @FXML
    private void calculerPrixTotal() {
        if (dateDebutPicker.getValue() != null && dateFinPicker.getValue() != null) {
            LocalDate debut = dateDebutPicker.getValue();
            LocalDate fin = dateFinPicker.getValue();

            if (fin.isBefore(debut)) {
                showError("Date fin avant date début");
                return;
            }

            try {
                long jours = ChronoUnit.DAYS.between(debut, fin) + 1;
                double prixTotal = jours * Double.parseDouble(vehicule.getPrix_par_jour());
                prixTotalLabel.setText(String.format("Prix total: %.2f DT pour %d jours", prixTotal, jours));
            } catch (NumberFormatException e) {
                showError("Prix invalide");
            }
        }
    }

    @FXML
    private void handleConfirmer() {
        try {
            if (!validateDates()) return;

            LocalDate debut = dateDebutPicker.getValue();
            LocalDate fin = dateFinPicker.getValue();
            double prixTotal = calculerPrix(debut, fin);

            ReservationVehicule reservation = createReservation(debut, fin, prixTotal);

            if (trySaveReservation(reservation)) {
                // Ajouter une notification
                reservationService.addNotification(reservation.getId(),
                        "Nouvelle réservation pour le véhicule " + vehicule.getId());

                showSuccess();
                closeWindow();
            }
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }
    private boolean validateDates() {
        if (dateDebutPicker.getValue() == null || dateFinPicker.getValue() == null) {
            showError("Dates requises");
            return false;
        }

        LocalDate debut = dateDebutPicker.getValue();
        LocalDate fin = dateFinPicker.getValue();

        if (fin.isBefore(debut)) {
            showError("Date fin invalide");
            return false;
        }

        if (debut.isBefore(LocalDate.now())) {
            showError("Date début passée");
            return false;
        }

        return true;
    }

    private double calculerPrix(LocalDate debut, LocalDate fin) {
        long jours = ChronoUnit.DAYS.between(debut, fin) + 1;
        return jours * Double.parseDouble(vehicule.getPrix_par_jour());
    }

    private ReservationVehicule createReservation(LocalDate debut, LocalDate fin, double prix) {
        int idClient = 1; // À remplacer par l'ID du client connecté
        return new ReservationVehicule(
                idClient,
                vehicule.getId(),
                debut,  // Utilisation directe de LocalDate
                fin,    // Utilisation directe de LocalDate
                prix,
                "CONFIRMEE"
        );
    }
    private boolean trySaveReservation(ReservationVehicule reservation) {
        try {
            reservationService.add(reservation);
            return true;
        } catch (RuntimeException e) {
            showError(e.getMessage());
            return false;
        }
    }

    private void showError(String message) {
        showAlert(AlertType.ERROR, "Erreur", message);
    }

    private void showSuccess() {
        showAlert(AlertType.INFORMATION, "Succès", "Réservation confirmée");
    }

    private void showAlert(AlertType type, String title, String message) {
        new Alert(type, message, ButtonType.OK).showAndWait();
    }

    private void closeWindow() {
        confirmerButton.getScene().getWindow().hide();
    }
}