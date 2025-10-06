package controllers;
import javafx.scene.Node;
import tn.esprit.models.ProposerEvent;
import tn.esprit.services.ServicePropositionEvent;
import tn.esprit.models.ReserverEvent;
import tn.esprit.services.ServiceReservationEvent;
import tn.esprit.models.PDFGenerator1;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.stage.Modality;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class EventController implements Initializable {

    @FXML
    private FlowPane eventsFlowPane;
    @FXML
    private HBox paginationContainer;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private Label pageInfo;
    @FXML
    private ComboBox<String> sortComboBox;
    @FXML
    private TextField searchField;
    @FXML
    private Button addEventButton;
    @FXML
    private MenuItem myReservationsItem;
    @FXML
    private CheckBox allEventsCheck;
    @FXML
    private CheckBox myReservationsCheck;
    @FXML
    private ToggleButton darkModeToggle;
    @FXML
    private BorderPane mainContainer;
    private boolean darkMode = false;
    @FXML
    private void handleMyReservations(ActionEvent event) {
        showingReservations = true;
        myReservationsCheck.setSelected(true);
        allEventsCheck.setSelected(false);
        showUserReservations(1); // 1 est l'ID de l'utilisateur (à remplacer par l'ID réel)
    }
    @FXML
    void handleMesOffres(ActionEvent event) {
        loadScene("/MesOffres.fxml", event);
    }
    @FXML
    void handleListeAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListesReservationRecus.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1500, 765);
            stage.setScene(scene);
            stage.setTitle("Ajouter une demande/offre");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    void handleListeVehicules(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeVehicules.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1500, 765);
            stage.setScene(scene);
            stage.setTitle("Ajouter une demande/offre");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    void handleAutresOffres(ActionEvent event) {
        loadScene("/Acceuil.fxml", event);
    }
    @FXML
    public void handleAjouterAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterOffre.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1500, 765);
            stage.setScene(scene);
            stage.setTitle("Ajouter une demande/offre");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadScene(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1500, 765);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page: " + fxmlPath);
        }
    }
    @FXML
    private void handleLogoutAction(ActionEvent event) throws IOException {
        Integer sessionId = SessionManager.getInstance().getUserId();
        if (sessionId != null) {
            System.out.println("✅ Logging out session ID: " + sessionId);
            SessionManager.getInstance().clearSession();

        } else {
            System.out.println("⚠️ No active session to logout");
        }
        loadScene("/Login.fxml", event);

    }
    @FXML
    private void handleProfile(ActionEvent event) throws IOException {

        loadScene("/Profile.fxml", event);

    }

    @FXML
    private void handleShowAllEvents(ActionEvent event) {
        showingReservations = false;
        allEventsCheck.setSelected(true);
        myReservationsCheck.setSelected(false);
        allEvents = eventService.getAll().stream()
                .filter(this::isEventUpcoming)
                .collect(Collectors.toList());
        setupPagination();
        pageInfo.setText(String.format("Page %d/%d", currentPage + 1, totalPages));
    }
    private final ServicePropositionEvent eventService = new ServicePropositionEvent();
    private final ServiceReservationEvent reservationService = new ServiceReservationEvent();
    private final Image defaultImage = new Image(getClass().getResourceAsStream("/img/pic5.jpg"));
    private List<tn.esprit.models.ProposerEvent> allEvents;
    private static final int ITEMS_PER_PAGE = 4;
    private int currentPage = 0;
    private int totalPages = 0;
    private boolean showingReservations = false;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (mainContainer == null) {
            throw new IllegalStateException("mainContainer n'a pas été injecté par FXML");
        }
        if (darkModeToggle == null) {
            throw new IllegalStateException("darkModeToggle n'a pas été injecté par FXML");
        }
        initializeCheckBoxes();
        ObservableList<String> sortOptions = FXCollections.observableArrayList(
                "Trier par",
                "Date (croissant)",
                "Date (décroissant)",
                "Prix (croissant)",
                "Prix (décroissant)",
                "Titre (A-Z)",
                "Titre (Z-A)"
        );
        darkModeToggle.setOnAction(event -> {
            darkMode = darkModeToggle.isSelected();
            applyDarkMode();
        });
        sortComboBox.setItems(sortOptions);
        sortComboBox.getSelectionModel().selectFirst();

        // Charger uniquement les événements à venir
        allEvents = eventService.getAll().stream()
                .filter(this::isEventUpcoming)
                .collect(Collectors.toList());

        if (allEvents.isEmpty()) {
            eventsFlowPane.getChildren().clear();
            Label noData = new Label("Aucun événement à venir pour le moment.");
            noData.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
            eventsFlowPane.getChildren().add(noData);
            paginationContainer.setVisible(false);
        } else {
            setupPagination();
        }

        Platform.runLater(() -> {
            MenuButton eventsMenu = (MenuButton) eventsFlowPane.getScene().lookup("#eventsMenu");
            if (eventsMenu != null) {
                for (MenuItem item : eventsMenu.getItems()) {
                    if (item.getText().equals("📋 Mes réservations")) {
                        item.setOnAction(e -> showUserReservations(1)); // 1 pour user_id par défaut
                    }
                }
            }
        });
    }
    private void applyDarkMode() {
        if (mainContainer == null) {
            System.err.println("Erreur: mainContainer n'est pas initialisé!");
            return;
        }
        if (darkMode) {
            // Style dark mode
            mainContainer.setStyle("-fx-background-color: #2b2b2b;");
            pageInfo.setStyle("-fx-text-fill: white;");
            sortComboBox.setStyle("-fx-background-color: #3c3f41; -fx-text-fill: white;");
            searchField.setStyle("-fx-background-color: #3c3f41; -fx-text-fill: white;");
            darkModeToggle.setText("☀️Light Mode");

            // Mettre à jour le style du HBox en haut
            HBox topBox = (HBox) mainContainer.getTop();
            topBox.setStyle("-fx-background-color: linear-gradient(to right, #3c3f41, #555555);" +
                    "-fx-padding: 15;" +
                    "-fx-border-width: 0 0 2 0;" +
                    "-fx-border-color: #555555;" +
                    "-fx-background-radius: 12;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4);");
        } else {
            // Style light mode
            mainContainer.setStyle("-fx-background-color: white;");
            pageInfo.setStyle("-fx-text-fill: #0b2e4a;");
            sortComboBox.setStyle("-fx-background-color: white; -fx-text-fill: black;");
            searchField.setStyle("-fx-background-color: white; -fx-text-fill: black;");
            darkModeToggle.setText("🌙 Dark Mode");

            // Rétablir le style original du HBox en haut
            HBox topBox = (HBox) mainContainer.getTop();
            topBox.setStyle("-fx-background-color: linear-gradient(to right, #c9eaff, #eaf6ff);" +
                    "-fx-padding: 15;" +
                    "-fx-border-width: 0 0 2 0;" +
                    "-fx-border-color: #b3d1f5;" +
                    "-fx-background-radius: 12;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        }

        // Mettre à jour les cartes existantes
        currentPage = 0;
        setupPagination();
    }
    private void showUserReservations(int userId) {
        showingReservations = true;
        eventsFlowPane.getChildren().clear();
        eventsFlowPane.setAlignment(Pos.TOP_CENTER); // Alignement au centre
        eventsFlowPane.setPrefWrapLength(1200); // Largeur fixe pour une seule carte par ligne

        List<ReserverEvent> reservations = reservationService.getAll().stream()
                .filter(r -> r.getUserId() == userId)
                .collect(Collectors.toList());

        if (reservations.isEmpty()) {
            Label noData = new Label("Vous n'avez aucune réservation pour le moment.");
            noData.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
            eventsFlowPane.getChildren().add(noData);
            paginationContainer.setVisible(false);
        } else {
            for (ReserverEvent reservation : reservations) {
                HBox card = createReservationCard(reservation);
                card.setMaxWidth(1000); // Largeur maximale de la carte
                eventsFlowPane.getChildren().add(card);
            }
            paginationContainer.setVisible(false);
        }
        pageInfo.setText("Mes réservations");
    }
    private HBox createReservationCard(ReserverEvent reservation) {
        HBox card = new HBox(15);
        if (darkMode) {
            card.setStyle("""
            -fx-background-color: #3c3f41;
            -fx-border-color: #555555;
            -fx-border-width: 1.5;
            -fx-border-radius: 15;
            -fx-background-radius: 15;
            -fx-padding: 15;
            -fx-min-width: 1000;
            -fx-max-width: 1000;
        """);
        } else {
            card.setStyle("""
            -fx-background-color: rgba(255, 255, 255, 0.92);
            -fx-border-color: #00acc1;
            -fx-border-width: 1.5;
            -fx-border-radius: 15;
            -fx-background-radius: 15;
            -fx-padding: 15;
            -fx-min-width: 1000;
            -fx-max-width: 1000;
        """);
        }
        card.setAlignment(Pos.CENTER_LEFT);

        // Effet d'ombre
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.2));
        shadow.setRadius(10);
        shadow.setOffsetX(4);
        shadow.setOffsetY(4);
        card.setEffect(shadow);

        // Partie gauche - Détails de la réservation
        VBox details = new VBox(5);
        details.setAlignment(Pos.CENTER_LEFT);
        details.setPrefWidth(800);

        // Titre
        Label titleLabel = new Label("Événement: " + reservation.getEventTitle());
        titleLabel.setStyle(darkMode ?
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #a8dadc;" :
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #005b4f;");

        // Détails
        Label dateLabel = new Label("📅 Date: " + new SimpleDateFormat("dd/MM/yyyy").format(reservation.getBookingDate()));
        dateLabel.setStyle(darkMode ?
                "-fx-font-size: 14px; -fx-text-fill: #a8dadc;" :
                "-fx-font-size: 14px; -fx-text-fill: #00695c;");

        Label quantityLabel = new Label("🎟 Quantité: " + reservation.getQuantity());
        quantityLabel.setStyle(darkMode ?
                "-fx-font-size: 14px; -fx-text-fill: #a8dadc;" :
                "-fx-font-size: 14px; -fx-text-fill: #00695c;");

        Label amountLabel = new Label("💸 Montant: " + reservation.getTotalAmount() + " TND");
        amountLabel.setStyle(darkMode ?
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #a8dadc;" :
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #005b4f;");

        details.getChildren().addAll(titleLabel, dateLabel, quantityLabel, amountLabel);

        // Partie droite - Boutons verticaux
        VBox buttonsBox = new VBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        // Bouton Modifier
        Button editButton = new Button("Modifier");
        editButton.setStyle(darkMode ?
                "-fx-background-color: #FFC107; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-padding: 6 12; -fx-min-width: 100;" :
                "-fx-background-color: #FFC107; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-padding: 6 12; -fx-min-width: 100;");
        editButton.setOnAction(e -> handleEditReservation(reservation));

        // Bouton PDF
        Button pdfButton = new Button("PDF");
        pdfButton.setStyle(darkMode ?
                "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-padding: 6 12; -fx-min-width: 100;" :
                "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-padding: 6 12; -fx-min-width: 100;");
        pdfButton.setOnAction(e -> generateReservationPDF(reservation));

        // Bouton Annuler
        Button cancelButton = new Button("Annuler");
        cancelButton.setStyle(darkMode ?
                "-fx-background-color: #F44336; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-padding: 6 12; -fx-min-width: 100;" :
                "-fx-background-color: #F44336; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-padding: 6 12; -fx-min-width: 100;");

        if (isSameDay(new Date(), reservation.getBookingDate())) {
            cancelButton.setDisable(true);
            cancelButton.setTooltip(new Tooltip("Annulation impossible le jour de l'événement"));
            cancelButton.setStyle("""
            -fx-background-color: #9E9E9E;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-font-weight: bold;
            -fx-padding: 6 12;
            -fx-min-width: 100;
        """);
        }

        cancelButton.setOnAction(e -> cancelReservation(reservation));
        buttonsBox.getChildren().addAll(editButton, pdfButton, cancelButton);

        // Ajout des deux parties à la carte
        card.getChildren().addAll(details, buttonsBox);

        // Animation hover
        setupHoverAnimation(card, shadow);
        return card;
    }
    private void initializeCheckBoxes() {
        allEventsCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal && !showingReservations) {
                handleShowAllEvents(null);
            }
        });

        myReservationsCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal && showingReservations) {
                handleMyReservations(null);
            }
        });
    }

    private void setupHoverAnimation(HBox card, DropShadow shadow) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), card);
        scaleUp.setToX(1.02);
        scaleUp.setToY(1.02);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), card);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        DropShadow hoverShadow = new DropShadow();
        hoverShadow.setColor(Color.color(0, 0, 0, 0.3));
        hoverShadow.setRadius(15);
        hoverShadow.setOffsetX(6);
        hoverShadow.setOffsetY(6);

        card.setOnMouseEntered(event -> {
            scaleUp.playFromStart();
            card.setEffect(hoverShadow);
        });

        card.setOnMouseExited(event -> {
            scaleDown.playFromStart();
            card.setEffect(shadow);
        });
    }
    private void cancelReservation(ReserverEvent reservation) {
        if (isSameDay(new Date(), reservation.getBookingDate())) {
            showAlert("Annulation impossible", "Vous ne pouvez pas annuler une réservation le jour même de l'événement.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Annuler la réservation");
        alert.setContentText("Êtes-vous sûr de vouloir annuler cette réservation ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            reservationService.delete(reservation);
            showUserReservations(1);
        }
    }
    private void handleEditReservation(ReserverEvent reservation) {
        try {
            // Chargez le formulaire de modification
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierReservation.fxml"));
            Parent root = loader.load();

            // Passez la réservation au contrôleur
            ModifierReservationController controller = loader.getController();
            controller.setReservationData(reservation);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier la réservation");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Rafraîchir après modification
            showUserReservations(1);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'interface de modification");
        }
    }

    private void generateReservationPDF(ReserverEvent reservation) {
        try {
            // Créer le PDF (implémentez cette méthode selon votre besoin)
            PDFGenerator1.generateReservationPDF(reservation);
            showAlert("Succès", "PDF généré avec succès pour la réservation");
        } catch (Exception e) {
            showAlert("Erreur", "Échec de la génération du PDF: " + e.getMessage());
        }
    }
    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterEvents(newValue);
        });
    }

    private void filterEvents(String searchText) {
        List<ProposerEvent> filteredEvents = eventService.getAll().stream()
                .filter(this::isEventUpcoming)
                .collect(Collectors.toList());

        if (searchText != null && !searchText.isEmpty()) {
            String lowerCaseSearch = searchText.toLowerCase();
            filteredEvents = filteredEvents.stream()
                    .filter(event ->
                            event.getTitle().toLowerCase().contains(lowerCaseSearch) ||
                                    event.getDescription().toLowerCase().contains(lowerCaseSearch))
                    .collect(Collectors.toList());
        }

        allEvents = filteredEvents;
        handleSort();
        currentPage = 0;
        setupPagination();
    }
    private void setupPagination() {
        // Filtrer les événements à venir
        allEvents = eventService.getAll().stream()
                .filter(this::isEventUpcoming)
                .collect(Collectors.toList());

        if (allEvents.isEmpty()) {
            Label noData = new Label("Aucun événement à venir pour le moment.");
            noData.setStyle("""
            -fx-font-size: 16px; 
            -fx-text-fill: #005b4f;
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.7), 2, 0.5, 0, 1);
        """);
            eventsFlowPane.getChildren().add(noData);
            paginationContainer.setVisible(false);
            return;
        }

        eventsFlowPane.setPrefWrapLength(1200);
        eventsFlowPane.setAlignment(Pos.TOP_CENTER);

        totalPages = (int) Math.ceil((double) allEvents.size() / ITEMS_PER_PAGE);
        updatePage();
    }

    private void updatePage() {
        eventsFlowPane.getChildren().clear();

        if (allEvents == null || allEvents.isEmpty()) {
            Label noData = new Label("Aucun événement disponible pour le moment.");
            noData.setStyle(darkMode ?
                    "-fx-font-size: 16px; -fx-text-fill: #cccccc;" :
                    "-fx-font-size: 16px; -fx-text-fill: gray;");
            eventsFlowPane.getChildren().add(noData);
            paginationContainer.setVisible(false);
            return;
        }

        int fromIndex = currentPage * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allEvents.size());

        for (int i = fromIndex; i < toIndex; i++) {
            ProposerEvent event = allEvents.get(i);
            VBox card = createEventCard(event);
            applyEntranceAnimation(card, (i % ITEMS_PER_PAGE) * 100);
            eventsFlowPane.getChildren().add(card);
        }

        // Mettre à jour les boutons et l'info de page
        pageInfo.setText(String.format("Page %d/%d", currentPage + 1, totalPages));
        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(currentPage == totalPages - 1);
        paginationContainer.setVisible(true); // S'assurer que la pagination est visible
    }

    @FXML
    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            updatePage();
        }
    }

    @FXML
    private void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            updatePage();
        }
    }
    private List<ProposerEvent> filterValidEvents(List<ProposerEvent> events) {
        return events.stream()
                .filter(e -> e.getTitle() != null && !e.getTitle().isEmpty())
                .filter(e -> e.getDescription() != null && !e.getDescription().isEmpty())
                .collect(Collectors.toList());
    }
    private void loadEvents() {
        eventsFlowPane.getChildren().clear();
        List<tn.esprit.models.ProposerEvent> events = eventService.getAll().stream()
                .filter(this::isEventUpcoming)
                .collect(Collectors.toList());

        if (events.isEmpty()) {
            Label noData = new Label("Aucun événement à venir pour le moment.");
            noData.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
            eventsFlowPane.getChildren().add(noData);
        } else {
            int index = 0;
            for (ProposerEvent event : events) {
                VBox card = createEventCard(event);
                applyEntranceAnimation(card, index * 100);
                eventsFlowPane.getChildren().add(card);
                index++;
            }
        }
    }

    private void applyEntranceAnimation(VBox card, int delay) {
        card.setOpacity(0);
        card.setTranslateX(-20);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), card);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), card);
        slideIn.setFromX(-20);
        slideIn.setToX(0);

        PauseTransition delayTransition = new PauseTransition(Duration.millis(delay));
        SequentialTransition sequence = new SequentialTransition(delayTransition, fadeIn, slideIn);
        sequence.play();
    }
    @FXML
    private void handleSearch() {
        filterEvents(searchField.getText());
    }
    @FXML
    private void handleSort() {
        String selectedOption = sortComboBox.getSelectionModel().getSelectedItem();

        if (selectedOption == null || "Trier par".equals(selectedOption)) {
            return;
        }

        switch (selectedOption) {
            case "Date (croissant)":
                allEvents.sort(Comparator.comparing(ProposerEvent::getStartDate));
                break;
            case "Date (décroissant)":
                allEvents.sort(Comparator.comparing(ProposerEvent::getStartDate).reversed());
                break;
            case "Prix (croissant)":
                allEvents.sort(Comparator.comparing(ProposerEvent::getPrice));
                break;
            case "Prix (décroissant)":
                allEvents.sort(Comparator.comparing(ProposerEvent::getPrice).reversed());
                break;
            case "Titre (A-Z)":
                allEvents.sort(Comparator.comparing(ProposerEvent::getTitle));
                break;
            case "Titre (Z-A)":
                allEvents.sort(Comparator.comparing(ProposerEvent::getTitle).reversed());
                break;
        }

        // Réinitialiser à la première page après le tri
        currentPage = 0;
        updatePage();
    }

    @FXML
    private void handleAddEvent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterEvent.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un Événement");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Recharger les données
            showingReservations = false;
            allEvents = eventService.getAll();
            setupPagination();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'interface d'ajout");
        }
    }

    private VBox createEventCard(ProposerEvent event) {
        VBox card = new VBox(8);
        card.setPrefSize(280, 350); // Taille similaire à l'exemple
        if (darkMode) {
            card.setStyle("""
            -fx-background-color: #3c3f41;
            -fx-border-color: #555555;
            -fx-border-width: 1.5;
            -fx-border-radius: 15;
            -fx-background-radius: 15;
            -fx-padding: 15;
        """);
        } else {
            card.setStyle("""
            -fx-background-color: rgba(255, 255, 255, 0.92);
            -fx-border-color: #00acc1;
            -fx-border-width: 1.5;
            -fx-border-radius: 15;
            -fx-background-radius: 15;
            -fx-padding: 15;
        """);
        }

        // Effet d'ombre
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.2));
        shadow.setRadius(10);
        shadow.setOffsetX(4);
        shadow.setOffsetY(4);
        card.setEffect(shadow);

        // Image de l'événement
        ImageView imageView = new ImageView(loadImage(event.getImageUrl()));
        imageView.setFitWidth(220);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");

        // Titre
        Label title = new Label(event.getTitle());
        title.setStyle(darkMode ?
                "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;" :
                """
                -fx-font-size: 18px; 
                -fx-font-weight: bold; 
                -fx-text-fill: #005b4f;
                -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.7), 2, 0.5, 0, 1);
                """);
        // Prix
        Label priceLabel = new Label("💸 Prix: " + String.format("%.2f TND", event.getPrice()));
        priceLabel.setStyle(darkMode ?
                "-fx-font-size: 14px; -fx-text-fill: #a8dadc;" :
                """
                -fx-font-size: 14px;
                -fx-text-fill: #00695c;
                -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.7), 1, 0.3, 0, 1);
                """);
        // Date
        Label dateLabel = new Label("📅 Date: " + formatDate(event.getStartDate()));
        dateLabel.setStyle(darkMode ?
                "-fx-font-size: 14px; -fx-text-fill: #a8dadc;" :
                """
                -fx-font-size: 14px;
                -fx-text-fill: #00695c;
                -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.7), 1, 0.3, 0, 1);
                """);
        // Lieu
        Label locationLabel = new Label("📍 Lieu: " + event.getLocation());
        locationLabel.setStyle(darkMode ?
                "-fx-font-size: 14px; -fx-text-fill: #a8dadc;" :
                """
                -fx-font-size: 14px;
                -fx-text-fill: #00695c;
                -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.7), 1, 0.3, 0, 1);
                """);
        // Boutons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        // Bouton Réserver
        Button reserveButton = new Button("Réserver");
        reserveButton.setStyle("""
            -fx-background-color: #4CAF50;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-font-weight: bold;
            -fx-padding: 6 12;
        """);
        reserveButton.setOnAction(e -> showReservationDialog(event));

        // Bouton Modifier
        Button editButton = new Button("Modifier");
        editButton.setStyle("""
            -fx-background-color: #FFC107;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-font-weight: bold;
            -fx-padding: 6 12;
        """);
        editButton.setOnAction(e -> handleEditEvent(event));

        // Bouton Supprimer
        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("""
            -fx-background-color: #F44336;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-font-weight: bold;
            -fx-padding: 6 12;
        """);
        deleteButton.setOnAction(e -> handleDeleteEvent(event));

        buttonsBox.getChildren().addAll(reserveButton, editButton, deleteButton);

        // Disposition
        VBox details = new VBox(5, title, priceLabel, dateLabel, locationLabel);
        details.setPadding(new Insets(5, 0, 10, 0));

        card.getChildren().addAll(imageView, details, buttonsBox);
        card.setAlignment(Pos.TOP_CENTER);

        // Animation hover
        setupHoverAnimation(card, shadow);

        return card;
    }
    private void handleEditEvent(ProposerEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierEvent.fxml"));
            Parent root = loader.load();

            ModifierEventController controller = loader.getController();
            controller.setEventData(event);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier l'événement");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Après modification :
            allEvents = eventService.getAll();
            setupPagination();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'interface de modification");
        }
    }

    private void handleDeleteEvent(ProposerEvent event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer l'événement");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer l'événement \"" + event.getTitle() + "\" ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    eventService.delete(event);
                    allEvents = eventService.getAll();
                    setupPagination();
                    showAlert("Succès", "Événement supprimé avec succès");
                } catch (Exception e) {
                    showAlert("Erreur", "Échec de la suppression: " + e.getMessage());
                }
            }
        });
    }

    private void setupHoverAnimation(VBox card, DropShadow shadow) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), card);
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), card);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        TranslateTransition liftUp = new TranslateTransition(Duration.millis(200), card);
        liftUp.setToY(-5);

        TranslateTransition liftDown = new TranslateTransition(Duration.millis(200), card);
        liftDown.setToY(0);

        DropShadow hoverShadow = new DropShadow();
        hoverShadow.setColor(Color.color(0, 0, 0, 0.3));
        hoverShadow.setRadius(15);
        hoverShadow.setOffsetX(6);
        hoverShadow.setOffsetY(6);

        card.setOnMouseEntered(event -> {
            scaleUp.playFromStart();
            liftUp.playFromStart();
            card.setEffect(hoverShadow);
        });

        card.setOnMouseExited(event -> {
            scaleDown.playFromStart();
            liftDown.playFromStart();
            card.setEffect(shadow);
        });
    }

    private Image loadImage(String imagePath) {
        try {
            InputStream is = getClass().getResourceAsStream(imagePath);
            if (is != null) {
                return new Image(is);
            }
            if (imagePath != null && !imagePath.isEmpty()) {
                return new Image("file:" + imagePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
        return defaultImage;
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("dd MMMM yyyy - HH'h'mm").format(date);
    }
    private boolean isEventUpcoming(ProposerEvent event) {
        Date now = new Date();
        return event.getStartDate().after(now);
    }
    private void showReservationDialog(ProposerEvent event) {
        Stage reservationStage = new Stage();
        VBox root = new VBox(15);
        root.setPadding(new Insets(25));

        if (darkMode) {
            root.setStyle("""
        -fx-background-color: #2b2b2b;
        -fx-background-radius: 25;
        -fx-border-radius: 25;
        -fx-border-color: #555555;
        -fx-border-width: 1;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0.3, 0, 5);
        -fx-font-family: 'Segoe UI', sans-serif;
        """);
        } else {
            root.setStyle("""
        -fx-background-color: linear-gradient(to bottom, #c2e4fb, #eff6ff);
        -fx-background-radius: 25;
        -fx-border-radius: 25;
        -fx-border-color: linear-gradient(to right, #001f4d, #003366);
        -fx-border-width: 1;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0.3, 0, 5);
        -fx-font-family: 'Segoe UI', sans-serif;
        """);
        }
        root.setAlignment(Pos.CENTER);

        // Titre avec icône
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Réserver Événement");
        titleLabel.setStyle("""
        -fx-font-size: 32px;
        -fx-font-weight: bold;
        -fx-text-fill: linear-gradient(to right, #001133, #003366);
    """);

        titleBox.getChildren().addAll(titleLabel);

        // Conteneur principal avec formulaire et GIF
        HBox contentBox = new HBox(20);
        contentBox.setAlignment(Pos.CENTER);

        // Partie gauche - Formulaire
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-padding: 20;");

        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setHgrow(Priority.NEVER);
        labelCol.setPrefWidth(100);

        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        fieldCol.setPrefWidth(150);

        grid.getColumnConstraints().addAll(labelCol, fieldCol);

        String fieldStyle = """
        -fx-background-radius: 12;
        -fx-border-radius: 12;
        -fx-border-color: #003366;
        -fx-border-width: 1.5;
        -fx-padding: 8 12;
        -fx-background-color: white;
    """;

        // Événement
        Label eventLabel = new Label("🎭 Événement");
        eventLabel.setStyle("-fx-font-weight: bold;");
        TextField eventField = new TextField(event.getTitle());
        eventField.setEditable(false);
        eventField.setStyle(fieldStyle);
        grid.addRow(0, eventLabel, eventField);

        // Quantité
        Label quantityLabel = new Label("🎟 Quantité *");
        quantityLabel.setStyle("-fx-font-weight: bold;");
        Spinner<Integer> quantitySpinner = new Spinner<>(1, 10, 1);
        quantitySpinner.setStyle(fieldStyle);
        grid.addRow(1, quantityLabel, quantitySpinner);

        // Prix
        Label priceLabel = new Label("💰 Prix (DT)");
        priceLabel.setStyle("-fx-font-weight: bold;");
        TextField priceField = new TextField(String.format("%.2f", event.getPrice()));
        priceField.setEditable(false);
        priceField.setStyle(fieldStyle);
        grid.addRow(2, priceLabel, priceField);

        // Partie droite - GIF et message
        VBox gifBox = new VBox(10);
        gifBox.setAlignment(Pos.CENTER);
        gifBox.setStyle("-fx-padding: 20;");

        ImageView gifView = new ImageView(new Image(getClass().getResourceAsStream("/images/gif.gif")));
        gifView.setFitWidth(200);
        gifView.setFitHeight(200);
        gifView.setPreserveRatio(true);

        Label gifLabel = new Label("Réservez votre événement en quelques clics!");
        gifLabel.setStyle("""
        -fx-font-size: 16px;
        -fx-font-weight: bold;
        -fx-text-fill: #005b4f;
    """);

        Label smileyLabel = new Label("😊");
        smileyLabel.setStyle("-fx-font-size: 24px;");

        gifBox.getChildren().addAll(gifView, gifLabel, smileyLabel);

        contentBox.getChildren().addAll(grid, gifBox);

        // Boutons
        HBox buttonsBox = new HBox(15);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setStyle("-fx-padding: 20 0 0 0;");

        Button confirmButton = new Button("💾 Confirmer");
        confirmButton.setStyle("""
        -fx-background-radius: 25;
        -fx-text-fill: white;
        -fx-font-size: 16px;
        -fx-font-weight: bold;
        -fx-padding: 10 25;
        -fx-background-color: linear-gradient(to right, #001f4d, #003366);
        -fx-cursor: hand;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0.3, 0, 4);
    """);

        Button cancelButton = new Button("Annuler");
        cancelButton.setStyle("""
        -fx-background-color: #F44336;
        -fx-text-fill: white;
        -fx-background-radius: 8;
        -fx-font-weight: bold;
        -fx-padding: 6 12;
        -fx-min-width: 100;
    """);

        // Vérification pour désactiver le bouton si c'est le jour J
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String todayStr = sdf.format(today);
        String eventDateStr = sdf.format(event.getStartDate());

        if (todayStr.equals(eventDateStr)) {
            cancelButton.setDisable(true);
            cancelButton.setTooltip(new Tooltip("Annulation impossible le jour de l'événement"));
            cancelButton.setStyle("""
            -fx-background-color: #9E9E9E;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-font-weight: bold;
            -fx-padding: 6 12;
            -fx-min-width: 100;
        """);
        }

        buttonsBox.getChildren().addAll(confirmButton, cancelButton);

        // Conteneur de confirmation
        VBox confirmationContainer = new VBox(10);
        confirmationContainer.setAlignment(Pos.CENTER);
        confirmationContainer.setVisible(false);

        ImageView confirmationGif = new ImageView(new Image(getClass().getResourceAsStream("/images/gif.gif")));
        confirmationGif.setFitWidth(200);
        confirmationGif.setFitHeight(200);

        Label confirmationLabel = new Label("Réservation confirmée ! À bientôt !");
        confirmationLabel.setStyle("""
        -fx-font-size: 18px;
        -fx-font-weight: bold;
        -fx-text-fill: #4CAF50;
    """);

        confirmationContainer.getChildren().addAll(confirmationGif, confirmationLabel);

        // Mise à jour du prix
        quantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            priceField.setText(String.format("%.2f", newVal * event.getPrice()));
        });

        // Actions des boutons
        confirmButton.setOnAction(e -> {
            try {
                // Animation de transition
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), contentBox);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);

                FadeTransition fadeOutButtons = new FadeTransition(Duration.seconds(0.5), buttonsBox);
                fadeOutButtons.setFromValue(1.0);
                fadeOutButtons.setToValue(0.0);

                FadeTransition fadeInConfirmation = new FadeTransition(Duration.seconds(0.5), confirmationContainer);
                fadeInConfirmation.setFromValue(0.0);
                fadeInConfirmation.setToValue(1.0);

                ParallelTransition parallelTransition = new ParallelTransition(fadeOut, fadeOutButtons);
                SequentialTransition sequence = new SequentialTransition(
                        parallelTransition,
                        new PauseTransition(Duration.seconds(0.1)),
                        fadeInConfirmation,
                        new PauseTransition(Duration.seconds(2))
                );

                sequence.setOnFinished(ev -> {
                    ReserverEvent newReservation = new ReserverEvent(
                            quantitySpinner.getValue(),
                            quantitySpinner.getValue() * event.getPrice(),
                            1, // userId
                            event.getTitle(),
                            event.getImageUrl()
                    );

                    reservationService.add(newReservation);
                    reservationService.addWithNotification(newReservation);


                    reservationStage.close();
                });

                confirmationContainer.setVisible(true);
                sequence.play();

            } catch (Exception ex) {
                showAlert("Erreur", ex.getMessage());
            }
        });

        cancelButton.setOnAction(e -> {
            // Vérifier à nouveau au cas où la date aurait changé
            if (isSameDay(new Date(), event.getStartDate())) {
                showAlert("Annulation impossible", "Vous ne pouvez pas annuler une réservation le jour même de l'événement.");
            } else {
                reservationStage.close();
            }
        });

        // Ajouter tous les éléments au root
        root.getChildren().addAll(titleBox, contentBox, buttonsBox, confirmationContainer);

        // Configuration de la scène
        Scene scene = new Scene(root);
        reservationStage.setScene(scene);
        reservationStage.setTitle("Réservation d'événement");
        reservationStage.initModality(Modality.APPLICATION_MODAL);
        reservationStage.show();
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(date1).equals(sdf.format(date2));
    }

}