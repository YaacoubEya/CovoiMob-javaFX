package controllers;

import tn.esprit.models.ProposerEvent;
import tn.esprit.models.ReserverEvent;
import tn.esprit.services.NotificationService;
import tn.esprit.services.ServicePropositionEvent;
import tn.esprit.services.ServiceReservationEvent;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;

public class EventsTableController {


    private boolean notificationsVisible = false;
    private Timer notificationTimer;
    private List<ReserverEvent> lastKnownReservations = new ArrayList<>();
    @FXML private Label notificationBadge;
    @FXML private VBox notificationsContainer;

    private static final String ACTIVE_TAB_STYLE =
            "-fx-background-color: #3498db; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 14px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 10 20; " +
                    "-fx-background-radius: 5; " +
                    "-fx-border-radius: 5; " +
                    "-fx-border-color: #dee2e6; " +
                    "-fx-border-width: 1; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1); " +
                    "-fx-cursor: hand;";

    private static final String INACTIVE_TAB_STYLE =
            "-fx-background-color: #f8f9fa; " +
                    "-fx-text-fill: #2c3e50; " +
                    "-fx-font-size: 14px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 10 20; " +
                    "-fx-background-radius: 5; " +
                    "-fx-border-radius: 5; " +
                    "-fx-border-color: #dee2e6; " +
                    "-fx-border-width: 1; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1); " +
                    "-fx-cursor: hand;";
    @FXML
    private BorderPane borderPane;
    @FXML
    private VBox centerContent;
    @FXML
    private Button btnEvents;
    @FXML
    private Button btnStats;
    @FXML
    private TableView<ProposerEvent> eventsTable;
    @FXML
    private Button addButton;

    // Table columns
    @FXML
    private TableColumn<ProposerEvent, String> titleColumn;
    @FXML
    private TableColumn<ProposerEvent, Date> dateColumn;
    @FXML
    private TableColumn<ProposerEvent, String> locationColumn;
    @FXML
    private TableColumn<ProposerEvent, Double> priceColumn;
    @FXML
    private TableColumn<ProposerEvent, Integer> reservationsColumn;
    @FXML
    private TableColumn<ProposerEvent, Void> actionsColumn;
    @FXML private Button notificationButton;

    private final ServicePropositionEvent eventService = new ServicePropositionEvent();
    private final ServiceReservationEvent reservationService = new ServiceReservationEvent();

    @FXML
    public void showStatistics() {
        setActiveTab(btnStats);
        centerContent.getChildren().clear();

        VBox statsContainer = createStatisticsContainer();
        ScrollPane scrollPane = new ScrollPane(statsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        centerContent.getChildren().add(scrollPane);
        animateContentFadeIn(statsContainer);
    }

    @FXML
    private void toggleNotifications() {
        if (notificationsContainer == null) {
            System.err.println("Erreur: notificationsContainer est null dans toggleNotifications");
            return;
        }

        notificationsVisible = !notificationsVisible;

        if (notificationsVisible) {
            // Charger les notifications existantes
            loadExistingNotifications();

            // Réinitialiser le badge
            notificationBadge.setVisible(false);

            // Positionnement absolu
            notificationsContainer.setTranslateX(notificationButton.getScene().getWindow().getWidth() - 320);
            notificationsContainer.setTranslateY(50);

            // Animation d'ouverture
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notificationsContainer);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        } else {
            // Animation de fermeture
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), notificationsContainer);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                notificationsContainer.setVisible(false);
                notificationsContainer.setManaged(false);
            });
            fadeOut.play();
        }

        notificationsContainer.setVisible(notificationsVisible);
        notificationsContainer.setManaged(notificationsVisible);
    }

    private void loadExistingNotifications() {
        notificationsContainer.getChildren().clear();

        // Ajouter ici le code pour charger les notifications existantes
        // Par exemple :
        List<ReserverEvent> reservations = reservationService.getAll();
        for (ReserverEvent reservation : reservations) {
            HBox notificationBox = createNotificationBox(reservation);
            notificationsContainer.getChildren().add(notificationBox);
        }
    }
    private HBox createNotificationBox(ReserverEvent reservation) {
        HBox notificationBox = new HBox(10);
        notificationBox.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 5; " +
                "-fx-padding: 10; -fx-border-color: #bbdefb; -fx-border-width: 1; " +
                "-fx-border-radius: 5;");
        notificationBox.setAlignment(Pos.CENTER_LEFT);
        notificationBox.setMaxWidth(300);

        Label icon = new Label("🔔");
        icon.setStyle("-fx-font-size: 16px;");

        VBox content = new VBox(3);
        Label titleLabel = new Label("Réservation");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0d47a1;");

        Label eventLabel = new Label("Événement: " + reservation.getEventTitle());
        Label dateLabel = new Label("Date: " + new SimpleDateFormat("dd/MM/yyyy HH:mm")
                .format(reservation.getBookingDate()));

        content.getChildren().addAll(titleLabel, eventLabel, dateLabel);
        notificationBox.getChildren().addAll(icon, content);

        return notificationBox;
    }
    private void animateContentFadeIn(Node node) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), node);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void setActiveTab(Button activeButton) {
        btnEvents.setStyle(activeButton == btnEvents ? ACTIVE_TAB_STYLE : INACTIVE_TAB_STYLE);
        btnStats.setStyle(activeButton == btnStats ? ACTIVE_TAB_STYLE : INACTIVE_TAB_STYLE);
    }

    private BarChart<String, Number> createEventsChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Mois");
        xAxis.setTickLabelRotation(45); // Pour une meilleure lisibilité
        xAxis.setTickLabelFont(Font.font(12));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nombre d'événements");
        yAxis.setTickLabelFont(Font.font(12));

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Événements par mois");
        chart.setLegendVisible(false);

        // Style amélioré
        chart.setStyle("-fx-background-color: transparent; -fx-padding: 15;");
        chart.getXAxis().setStyle("-fx-tick-label-fill: black; -fx-font-size: 12px;");
        chart.getYAxis().setStyle("-fx-tick-label-fill: black; -fx-font-size: 12px;");
        // Données
        Map<String, Long> eventsData = eventService.getAll().stream()
                .collect(Collectors.groupingBy(
                        e -> {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(e.getStartDate());
                            return String.format("%02d/%d", cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR));
                        },
                        TreeMap::new, // Garde l'ordre chronologique
                        Collectors.counting()
                ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        eventsData.forEach((month, count) -> {
            XYChart.Data<String, Number> data = new XYChart.Data<>(month, count);
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: #3498db;");
                    Tooltip.install(newNode, new Tooltip(month + ": " + count));
                }
            });
            series.getData().add(data);
        });

        chart.getData().add(series);
        return chart;
    }

    private PieChart createReservationsChart() {
        PieChart chart = new PieChart();
        chart.setTitle("Réservations par événement");
        chart.setLegendVisible(true); // Activer la légende
        chart.setLabelsVisible(true); // Afficher les labels

        // Style amélioré
        chart.setStyle("-fx-background-color: transparent; -fx-padding: 15;");
        chart.setLegendSide(Side.BOTTOM);
        Map<String, Long> reservationsData = reservationService.getAll().stream()
                .collect(Collectors.groupingBy(
                        ReserverEvent::getEventTitle,
                        Collectors.counting()
                ));

        AtomicInteger colorIndex = new AtomicInteger(0);
        // Remplacer par cette version simplifiée et sécurisée
        List<String> colors = List.of(
                "#3498db", "#2ecc71", "#e74c3c",
                "#f39c12", "#9b59b6", "#1abc9c",
                "#d35400"
        );

        reservationsData.forEach((event, count) -> {
            PieChart.Data slice = new PieChart.Data(event + " (" + count + ")", count);
            chart.getData().add(slice);

            slice.getNode().setStyle("-fx-pie-color: " + colors.get(colorIndex.getAndIncrement() % colors.size()) + ";");
        });

        return chart;
    }

    private LineChart<String, Number> createRevenueChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Mois");
        xAxis.setTickLabelRotation(45);
        xAxis.setTickLabelFont(Font.font(12));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Revenus (TND)");
        yAxis.setTickLabelFont(Font.font(12));

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Revenus mensuels");

        // Style amélioré
        chart.setStyle("-fx-background-color: transparent; -fx-padding: 15;");
        chart.getXAxis().setStyle("-fx-tick-label-fill: black; -fx-font-size: 12px;");
        chart.getYAxis().setStyle("-fx-tick-label-fill: black; -fx-font-size: 12px;");
        // Calcul des revenus par mois - version simplifiée sans nbPlaces
        Map<String, Long> eventsData = eventService.getAll().stream()
                .collect(Collectors.groupingBy(
                        e -> {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(e.getStartDate());
                            return String.format("%02d/%d", cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR));
                        },
                        TreeMap::new, // Garde l'ordre chronologique
                        Collectors.counting()
                ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenus");
        Map<String, Double> revenueData = reservationService.getAll().stream()
                .collect(Collectors.groupingBy(
                        r -> {
                            Optional<ProposerEvent> event = eventService.getAll().stream()
                                    .filter(e -> e.getTitle().equals(r.getEventTitle()))
                                    .findFirst();
                            if (event.isPresent()) {
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(event.get().getStartDate());
                                return String.format("%02d/%d", cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR));
                            }
                            return "Unknown";
                        },
                        TreeMap::new,
                        Collectors.summingDouble(r -> {
                            Optional<ProposerEvent> event = eventService.getAll().stream()
                                    .filter(e -> e.getTitle().equals(r.getEventTitle()))
                                    .findFirst();
                            return event.map(ProposerEvent::getPrice).orElse(0.0);
                        })
                ));
        revenueData.forEach((month, revenue) -> {
            XYChart.Data<String, Number> data = new XYChart.Data<>(month, revenue);
            series.getData().add(data);
        });

        chart.getData().add(series);
        return chart;
    }

    @FXML
    public void handleNavigation(ActionEvent event) {
        try {
            if (!(event.getSource() instanceof Node)) {
                showErrorAlert("Erreur", new Exception("Source de l'événement non valide"));
                return;
            }

            String buttonId = ((Node) event.getSource()).getId();
            if (buttonId == null || buttonId.isEmpty()) {
                showErrorAlert("Erreur", new Exception("Bouton sans ID"));
                return;
            }

            String viewPath = switch (buttonId) {
                case "btnUtilisateur", "btnOffres", "btnReservationsCovoit", "btnStatsCovoit" -> "/AllOffre.fxml";
                case "btnVeh", "btnVehReservation" -> "/BackVehiculeView.fxml";
                case "btnEvents", "btnStats" -> ""; // Already handled by other methods
                default -> {
                    showErrorAlert("Navigation", new Exception("Bouton non implémenté: " + buttonId));
                    yield "";
                }
            };

            if (viewPath.isEmpty()) {
                return;
            }

            // Charger la vue
            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
            Parent root = loader.load();

            // Obtenir la scène actuelle et la remplacer
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1500, 765));
            stage.show();

            // Initialisations spécifiques pour AllOffreView
            if (viewPath.equals("/AllOffreView.fxml") && loader.getController() instanceof AllOffreController) {
                AllOffreController controller = loader.getController();
                switch (buttonId) {
                    case "btnOffres" -> controller.showOffres();
                    case "btnReservationsCovoit" -> controller.showReservations();
                    case "btnStatsCovoit" -> controller.showStatistics();
                }
            }

        } catch (IOException e) {
            showErrorAlert("Erreur de navigation", e);
            e.printStackTrace();
        } catch (Exception e) {
            showErrorAlert("Erreur inattendue", e);
            e.printStackTrace();
        }
    }
    private VBox createStatisticsContainer() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #f8f9fa;");

        // Titre principal avec animation
        Label statsTitle = new Label("📊 Tableau de bord des statistiques");
        statsTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        statsTitle.setPadding(new Insets(0, 0, 20, 0));

        // Animation du titre
        FadeTransition titleFade = new FadeTransition(Duration.millis(800), statsTitle);
        titleFade.setFromValue(0);
        titleFade.setToValue(1);
        titleFade.play();

        // Création d'un panneau d'onglets pour les différentes vues
        TabPane chartTabs = new TabPane();
        chartTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        chartTabs.setStyle("-fx-background-color: transparent;");

        // Onglet Aperçu
        Tab overviewTab = new Tab("Aperçu");
        overviewTab.setContent(createOverviewTab());
        overviewTab.setStyle("-fx-font-weight: bold;");

        // Onglet Analyse Saisonnière
        Tab seasonalTab = new Tab("Analyse Saisonnière");
        seasonalTab.setContent(createSeasonalTab());
        seasonalTab.setStyle("-fx-font-weight: bold;");

        // Onglet Analyse Financière
        Tab financialTab = new Tab("Analyse Financière");
        financialTab.setContent(createFinancialTab());
        financialTab.setStyle("-fx-font-weight: bold;");

        chartTabs.getTabs().addAll(overviewTab, seasonalTab, financialTab);

        // Ajout des cartes de métriques en haut
        HBox metricsBox = createMetricsCards();

        container.getChildren().addAll(statsTitle, metricsBox, chartTabs);

        return container;
    }
    private HBox createMetricsCards() {
        // Calcul des métriques
        long totalEvents = eventService.getAll().size();
        long totalReservations = reservationService.getAll().size();
        double totalRevenue = reservationService.getAll().stream()
                .mapToDouble(r -> {
                    Optional<ProposerEvent> event = eventService.getAll().stream()
                            .filter(e -> e.getTitle().equals(r.getEventTitle()))
                            .findFirst();
                    return event.map(ProposerEvent::getPrice).orElse(0.0);
                })
                .sum();
        double avgRevenuePerEvent = totalEvents > 0 ? totalRevenue / totalEvents : 0;

        // Création des cartes
        HBox metricsBox = new HBox(15);
        metricsBox.setAlignment(Pos.CENTER);

        metricsBox.getChildren().addAll(
                createMetricCard("Événements totaux", String.valueOf(totalEvents), "#3498db", "📅"),
                createMetricCard("Réservations totales", String.valueOf(totalReservations), "#2ecc71", "🎟️"),
                createMetricCard("Revenu total", String.format("%.2f TND", totalRevenue), "#e74c3c", "💰"),
                createMetricCard("Revenu moyen/événement", String.format("%.2f TND", avgRevenuePerEvent), "#f39c12", "📊")
        );

        return metricsBox;
    }
    private VBox createSeasonalTab() {
        VBox seasonalTab = new VBox(20);
        seasonalTab.setPadding(new Insets(15));
        seasonalTab.setStyle("-fx-background-color: transparent;");

        // Graphique saisonnier avec animation
        BarChart<String, Number> seasonalChart = createEnhancedSeasonalChart();

        // Détails saisonniers avec disposition en cartes
        TilePane seasonalDetails = new TilePane();
        seasonalDetails.setPadding(new Insets(15));
        seasonalDetails.setHgap(15);
        seasonalDetails.setVgap(15);
        seasonalDetails.setPrefColumns(2);

        Map<String, Long> seasonalData = reservationService.getAll().stream()
                .collect(Collectors.groupingBy(
                        r -> {
                            Optional<ProposerEvent> event = eventService.getAll().stream()
                                    .filter(e -> e.getTitle().equals(r.getEventTitle()))
                                    .findFirst();
                            return event.isPresent() ? getSeasonFrench(event.get().getStartDate()) : "Inconnu";
                        },
                        Collectors.counting()
                ));

        // Ajout des cartes pour chaque saison
        String[] seasons = {"Hiver", "Printemps", "Été", "Automne"};
        String[] seasonColors = {"#3498db", "#2ecc71", "#e74c3c", "#f39c12"};
        String[] seasonEmojis = {"⛄", "🌸", "☀️", "🍂"};

        for (int i = 0; i < seasons.length; i++) {
            String season = seasons[i];
            long count = seasonalData.getOrDefault(season, 0L);

            VBox seasonCard = createSeasonCard(
                    season,
                    String.valueOf(count),
                    seasonColors[i],
                    seasonEmojis[i],
                    getSeasonalDescriptionFrench(season)
            );

            seasonalDetails.getChildren().add(seasonCard);
        }

        seasonalTab.getChildren().addAll(seasonalChart, seasonalDetails);
        return seasonalTab;
    }
    private void showErrorAlert(String title, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(e.getMessage());
        e.printStackTrace();
        alert.showAndWait();
    }

    // Méthode générique pour charger les vues
    private void loadView(String fxmlPath, Node sourceNode) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Stage stage = (Stage) sourceNode.getScene().getWindow();
        stage.setScene(new Scene(root, 1500, 765));
        stage.show();
    }
    private String getSeasonFrench(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH) + 1; // Janvier = 1

        if (month >= 3 && month <= 5) return "Printemps";
        else if (month >= 6 && month <= 8) return "Été";
        else if (month >= 9 && month <= 11) return "Automne";
        else return "Hiver";
    }
    private String getSeasonalDescriptionFrench(String season) {
        switch (season) {
            case "Hiver":
                return "Saison froide avec des événements principalement en intérieur";
            case "Printemps":
                return "Météo parfaite pour les activités en extérieur";
            case "Été":
                return "Haute saison pour les festivals et concerts";
            case "Automne":
                return "Période de transition avec des événements diversifiés";
            default:
                return "Événements tout au long de la saison";
        }
    }
    private BarChart<String, Number> createEnhancedSeasonalChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Saisons");
        xAxis.setTickLabelFont(Font.font("Arial", FontWeight.BOLD, 12));
        // Style explicite
        xAxis.setStyle("-fx-tick-label-fill: black; -fx-font-size: 12px; -fx-axis-label-fill: black; -fx-tick-mark-visible: true;");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nombre de réservations");
        yAxis.setTickLabelFont(Font.font("Arial", FontWeight.BOLD, 12));
        // Style explicite
        yAxis.setStyle("-fx-tick-label-fill: black; -fx-font-size: 12px; -fx-axis-label-fill: black; -fx-tick-mark-visible: true;");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Réservations par saison");

        // Style global
        chart.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 15;");
        chart.setCategoryGap(20);
        chart.setAnimated(false); // Désactive l'animation pour plus de stabilité

        // Style du graphique
        chart.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15;");
        chart.getXAxis().setStyle("-fx-tick-label-fill: black; -fx-font-size: 12px;");
        chart.getYAxis().setStyle("-fx-tick-label-fill: black; -fx-font-size: 12px;");

        // Récupération des données
        Map<String, Long> seasonalData = reservationService.getAll().stream()
                .collect(Collectors.groupingBy(
                        r -> {
                            Optional<ProposerEvent> event = eventService.getAll().stream()
                                    .filter(e -> e.getTitle().equals(r.getEventTitle()))
                                    .findFirst();
                            return event.isPresent() ? getSeasonFrench(event.get().getStartDate()) : "Inconnu";
                        },
                        Collectors.counting()
                ));

        // S'assurer que toutes les saisons sont représentées
        Map<String, Long> completeData = new LinkedHashMap<>();
        completeData.put("Hiver", seasonalData.getOrDefault("Hiver", 0L));
        completeData.put("Printemps", seasonalData.getOrDefault("Printemps", 0L));
        completeData.put("Été", seasonalData.getOrDefault("Été", 0L));
        completeData.put("Automne", seasonalData.getOrDefault("Automne", 0L));

        // Création de la série de données
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Réservations");

        // Ajout des données avec couleurs spécifiques
        completeData.forEach((season, count) -> {
            XYChart.Data<String, Number> data = new XYChart.Data<>(season, count);
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    // Appliquer des couleurs différentes pour chaque saison
                    switch (season) {
                        case "Hiver":
                            newNode.setStyle("-fx-bar-fill: #5DADE2;");
                            break;
                        case "Printemps":
                            newNode.setStyle("-fx-bar-fill: #58D68D;");
                            break;
                        case "Été":
                            newNode.setStyle("-fx-bar-fill: #F39C12;");
                            break;
                        case "Automne":
                            newNode.setStyle("-fx-bar-fill: #A569BD;");
                            break;
                    }

                    // Ajout d'un tooltip
                    Tooltip tooltip = new Tooltip(season + ": " + count + " réservations");
                    tooltip.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                    Tooltip.install(newNode, tooltip);
                }
            });
            series.getData().add(data);
        });

        chart.getData().add(series);
        return chart;
    }
    private int getMaxSeasonalReservations() {
        return reservationService.getAll().stream()
                .collect(Collectors.groupingBy(
                        r -> {
                            Optional<ProposerEvent> event = eventService.getAll().stream()
                                    .filter(e -> e.getTitle().equals(r.getEventTitle()))
                                    .findFirst();
                            return event.isPresent() ? getSeason(event.get().getStartDate()) : "Unknown";
                        },
                        Collectors.counting()
                ))
                .values().stream()
                .mapToInt(Long::intValue)
                .max()
                .orElse(0);
    }
    private void applyChartStyle(Chart chart) {
        Label someLabel = new Label("text");
        someLabel.setFont(Font.font("Arial", 12));
        chart.setStyle("-fx-font-family: 'Arial';");
        chart.setTitleSide(Side.TOP);
        if (chart.isLegendVisible()) {
            // Use lookup to find the legend node
            Node legend = chart.lookup(".chart-legend");
            if (legend != null) {
                // First check if the legend is a Region (which has setPadding)
                if (legend instanceof Region) {
                    ((Region) legend).setPadding(new Insets(10));
                }
                // Apply font size through CSS
                legend.setStyle("-fx-font-size: 12;");
            }
        }
    }
    private String getSeasonalDescription(String season) {
        switch (season) {
            case "Winter":
                return "Cold season with indoor events dominating";
            case "Spring":
                return "Perfect weather for outdoor activities";
            case "Summer":
                return "Peak season for festivals and concerts";
            case "Autumn":
                return "Transition period with diverse events";
            default:
                return "Events throughout the season";
        }
    }
    private VBox createTopEventsBox() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label title = new Label("🏆 Événements générant le plus de revenus");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Map<String, Double> revenueByEvent = new HashMap<>();
        reservationService.getAll().forEach(r -> {
            Optional<ProposerEvent> event = eventService.getAll().stream()
                    .filter(e -> e.getTitle().equals(r.getEventTitle()))
                    .findFirst();
            event.ifPresent(e -> {
                revenueByEvent.merge(e.getTitle(), e.getPrice() * r.getQuantity(), Double::sum);
            });
        });

        List<Map.Entry<String, Double>> topEvents = revenueByEvent.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        VBox eventsList = new VBox(10);
        eventsList.setAlignment(Pos.CENTER_LEFT);

        String[] medals = {"🥇", "🥈", "🥉", "4️⃣", "5️⃣"};

        for (int i = 0; i < topEvents.size(); i++) {
            Map.Entry<String, Double> entry = topEvents.get(i);
            HBox hbox = new HBox(10);
            hbox.setAlignment(Pos.CENTER_LEFT);

            Label medalLabel = new Label(medals[i]);
            medalLabel.setStyle("-fx-font-size: 16px;");

            Label eventLabel = new Label(entry.getKey());
            eventLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            Label revenueLabel = new Label(String.format("%.2f TND", entry.getValue()));
            revenueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60;");

            hbox.getChildren().addAll(medalLabel, eventLabel, revenueLabel);
            eventsList.getChildren().add(hbox);
        }

        box.getChildren().addAll(title, eventsList);
        return box;
    }

    private HBox createFinancialSummaryBox() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        // Calculate financial metrics
        double totalRevenue = reservationService.getAll().stream()
                .mapToDouble(r -> {
                    Optional<ProposerEvent> event = eventService.getAll().stream()
                            .filter(e -> e.getTitle().equals(r.getEventTitle()))
                            .findFirst();
                    return event.map(ProposerEvent::getPrice).orElse(0.0);
                })
                .sum();

        long totalEvents = eventService.getAll().size();
        double avgRevenuePerEvent = totalEvents > 0 ? totalRevenue / totalEvents : 0;

        long totalReservations = reservationService.getAll().size();
        double avgRevenuePerReservation = totalReservations > 0 ? totalRevenue / totalReservations : 0;

        // Create summary cards
        VBox totalCard = createFinancialMetricCard("Total Revenue",
                String.format("%.2f TND", totalRevenue),
                "#27ae60", "💰");

        VBox avgEventCard = createFinancialMetricCard("Avg/Event",
                String.format("%.2f TND", avgRevenuePerEvent),
                "#3498db", "📊");

        VBox avgResCard = createFinancialMetricCard("Avg/Reservation",
                String.format("%.2f TND", avgRevenuePerReservation),
                "#e74c3c", "🎟️");

        box.getChildren().addAll(totalCard, avgEventCard, avgResCard);
        return box;
    }

    private VBox createFinancialMetricCard(String title, String value, String color, String emoji) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        HBox header = new HBox(5);
        header.setAlignment(Pos.CENTER);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 16px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        header.getChildren().addAll(emojiLabel, titleLabel);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(header, valueLabel);

        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                    "-fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                    "-fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        });

        return card;
    }
    private VBox createFinancialTab() {
        VBox financialTab = new VBox(20);
        financialTab.setPadding(new Insets(15));
        financialTab.setStyle("-fx-background-color: transparent;");

        // Graphique des revenus
        LineChart<String, Number> revenueChart = createEnhancedRevenueChart();

        // Événements générant le plus de revenus
        VBox topEventsBox = createTopEventsBox();

        // Résumé financier
        HBox summaryBox = createFinancialSummaryBox();

        financialTab.getChildren().addAll(revenueChart, summaryBox, topEventsBox);
        return financialTab;
    }
    private LineChart<String, Number> createEnhancedRevenueChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Mois");
        xAxis.setTickLabelRotation(45);
        xAxis.setTickLabelFont(Font.font("Arial", FontWeight.BOLD, 12));
        // Style explicite pour l'axe X
        xAxis.setStyle("-fx-tick-label-fill: black; -fx-font-size: 12px; -fx-axis-label-fill: black; -fx-tick-mark-visible: true;");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Revenus (TND)");
        yAxis.setTickLabelFont(Font.font("Arial", FontWeight.BOLD, 12));
        yAxis.setTickUnit(50);
        // Style explicite pour l'axe Y
        yAxis.setStyle("-fx-tick-label-fill: black; -fx-font-size: 12px; -fx-axis-label-fill: black; -fx-tick-mark-visible: true;");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Revenus mensuels");

        // Style global
        chart.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 15; " +
                "-fx-text-fill: black;");

        // Assurer la visibilité des axes et des lignes
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(true);
        chart.setAlternativeRowFillVisible(false);
        chart.setAlternativeColumnFillVisible(false); // Affiche les points sur la ligne

        // Style du graphique
        chart.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15;");
        chart.getXAxis().setStyle("-fx-tick-label-fill: black; -fx-font-size: 12px;");
        chart.getYAxis().setStyle("-fx-tick-label-fill: black; -fx-font-size: 12px;");

        // Calcul des données
        Map<String, Double> revenueData = new TreeMap<>();
        reservationService.getAll().forEach(r -> {
            Optional<ProposerEvent> event = eventService.getAll().stream()
                    .filter(e -> e.getTitle().equals(r.getEventTitle()))
                    .findFirst();

            if (event.isPresent()) {
                String month = new SimpleDateFormat("MMM yyyy", Locale.FRENCH).format(event.get().getStartDate());
                double amount = event.get().getPrice();
                revenueData.merge(month, amount, Double::sum);
            }
        });

        // Création de la série
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenus");

        // Ajout des données
        revenueData.forEach((month, revenue) -> {
            XYChart.Data<String, Number> data = new XYChart.Data<>(month, revenue);
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-stroke: #E74C3C; -fx-stroke-width: 2px;");
                    Tooltip tooltip = new Tooltip(month + ": " + String.format("%.2f TND", revenue));
                    tooltip.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                    Tooltip.install(newNode, tooltip);
                }
            });
            series.getData().add(data);
        });

        chart.getData().add(series);
        return chart;
    }
    private double getMaxRevenue() {
        return reservationService.getAll().stream()
                .mapToDouble(r -> {
                    Optional<ProposerEvent> event = eventService.getAll().stream()
                            .filter(e -> e.getTitle().equals(r.getEventTitle()))
                            .findFirst();
                    return event.map(ProposerEvent::getPrice).orElse(0.0);
                })
                .max()
                .orElse(0.0);
    }
    private VBox createSeasonCard(String season, String count, String color, String emoji, String description) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 24px;");

        Label seasonLabel = new Label(season);
        seasonLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        header.getChildren().addAll(emojiLabel, seasonLabel);

        Label countLabel = new Label(count + " reservations");
        countLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-wrap-text: true;");
        descLabel.setMaxWidth(200);
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setTextAlignment(TextAlignment.CENTER);

        card.getChildren().addAll(header, countLabel, descLabel);

        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                    "-fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                    "-fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        });

        return card;
    }
    private VBox createMetricCard(String title, String value, String color, String emoji) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 24px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(emojiLabel, titleLabel, valueLabel);

        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                    "-fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                    "-fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        });

        return card;
    }
    private VBox createOverviewTab() {
        VBox overviewTab = new VBox(20);
        overviewTab.setPadding(new Insets(15));
        overviewTab.setStyle("-fx-background-color: transparent;");

        // Création d'un panneau grid pour une meilleure disposition
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(10));

        // Première ligne - graphiques principaux
        BarChart<String, Number> eventsChart = createEnhancedEventsChart();
        PieChart reservationsChart = createEnhancedReservationsChart();

        // Deuxième ligne - graphique en ligne
        LineChart<String, Number> revenueChart = createEnhancedRevenueChart();

        grid.add(eventsChart, 0, 0);
        grid.add(reservationsChart, 1, 0);
        grid.add(revenueChart, 0, 1, 2, 1);

        // Définition des contraintes de colonne
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        overviewTab.getChildren().add(grid);
        return overviewTab;
    }
    private PieChart createEnhancedReservationsChart() {
        PieChart chart = new PieChart();
        chart.setTitle("Répartition des réservations par événement");

        // Style amélioré pour la visibilité
        chart.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-border-width: 1; " +
                "-fx-text-fill: black;"); // Ajout pour la couleur du texte

        // Style de la légende
        chart.setLegendVisible(true);
        chart.setLegendSide(Side.RIGHT);
        chart.lookup(".chart-legend").setStyle("-fx-text-fill: black; -fx-font-size: 12px;");
        chart.setLabelLineLength(10);

        Map<String, Long> reservationsData = reservationService.getAll().stream()
                .collect(Collectors.groupingBy(
                        ReserverEvent::getEventTitle,
                        Collectors.counting()
                ));

        List<Map.Entry<String, Long>> sortedEntries = reservationsData.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());

        List<String> colors = List.of(
                "#3498db", "#2ecc71", "#e74c3c",
                "#f39c12", "#9b59b6", "#1abc9c",
                "#d35400"
        );

        double total = getTotalReservations();

        for (Map.Entry<String, Long> entry : sortedEntries) {
            PieChart.Data slice = new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue());
            chart.getData().add(slice);

            int colorIndex = chart.getData().indexOf(slice) % colors.size();
            slice.getNode().setStyle("-fx-pie-color: " + colors.get(colorIndex) + ";");

            Tooltip tooltip = new Tooltip(entry.getKey() + ": " + entry.getValue() + " réservations (" +
                    String.format("%.1f", (entry.getValue() / total) * 100) + "%)");
            tooltip.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            Tooltip.install(slice.getNode(), tooltip);
        }

        return chart;
    }
    private long getTotalReservations() {
        return reservationService.getAll().stream()
                .collect(Collectors.groupingBy(
                        ReserverEvent::getEventTitle,
                        Collectors.counting()
                ))
                .values().stream()
                .mapToLong(Long::longValue)
                .sum();
    }
    private BarChart<String, Number> createEnhancedEventsChart() {
        // Création des axes
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Mois");
        xAxis.setTickLabelRotation(45);
        xAxis.setTickLabelFont(Font.font("Arial", FontWeight.BOLD, 12));
        // Style explicite pour l'axe X
        xAxis.setStyle("-fx-tick-label-fill: black; -fx-font-size: 12px; -fx-axis-label-fill: black; -fx-tick-mark-visible: true;");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nombre d'événements");
        yAxis.setTickLabelFont(Font.font("Arial", FontWeight.BOLD, 12));
        yAxis.setTickUnit(1);
        yAxis.setMinorTickVisible(false);
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(10);
        // Style explicite pour l'axe Y
        yAxis.setStyle("-fx-tick-label-fill: black; -fx-font-size: 12px; -fx-axis-label-fill: black; -fx-tick-mark-visible: true;");

        // Création du graphique
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Événements par mois");
        chart.setLegendVisible(false);
        chart.setAnimated(false);

        // Style global du graphique
        chart.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15;");

        // Assurer la visibilité des axes
        chart.setAlternativeRowFillVisible(false);
        chart.setAlternativeColumnFillVisible(false);
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(true);


        // Style du graphique
        chart.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15;");
        chart.getXAxis().setStyle("-fx-tick-label-fill: black; -fx-font-size: 12px;");
        chart.getYAxis().setStyle("-fx-tick-label-fill: black; -fx-font-size: 12px;");

        // Récupération des données
        Map<String, Long> eventsData = eventService.getAll().stream()
                .collect(Collectors.groupingBy(
                        e -> new SimpleDateFormat("MMM yyyy", Locale.FRENCH).format(e.getStartDate()),
                        TreeMap::new,
                        Collectors.counting()
                ));

        // Création de la série
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Événements");

        // Ajout des données
        eventsData.forEach((month, count) -> {
            XYChart.Data<String, Number> data = new XYChart.Data<>(month, count);
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: #3498DB;");
                    Tooltip tooltip = new Tooltip(month + ": " + count + " événements");
                    tooltip.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                    Tooltip.install(newNode, tooltip);
                }
            });
            series.getData().add(data);
        });

        chart.getData().add(series);
        return chart;
    }
    private int getMaxEventCount() {
        return eventService.getAll().stream()
                .collect(Collectors.groupingBy(
                        e -> new SimpleDateFormat("MMM yyyy").format(e.getStartDate()),
                        Collectors.counting()
                ))
                .values().stream()
                .mapToInt(Long::intValue)
                .max()
                .orElse(0);
    }
    private BarChart<String, Number> createSeasonalReservationsChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Saison");
        xAxis.setTickLabelFont(Font.font(12));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nombre de réservations");
        yAxis.setTickLabelFont(Font.font(12));

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Réservations par saison");
        chart.setLegendVisible(false);

        // Style amélioré
        chart.setStyle("-fx-background-color: transparent; -fx-padding: 15;");
        chart.getXAxis().setStyle("-fx-tick-label-fill: black; -fx-font-size: 12px;");
        chart.getYAxis().setStyle("-fx-tick-label-fill: black; -fx-font-size: 12px;");

        Map<String, Long> seasonalData = reservationService.getAll().stream()
                .collect(Collectors.groupingBy(
                        r -> {
                            Optional<ProposerEvent> event = eventService.getAll().stream()
                                    .filter(e -> e.getTitle().equals(r.getEventTitle()))
                                    .findFirst();
                            return event.isPresent() ? getSeason(event.get().getStartDate()) : "Inconnu";
                        },
                        Collectors.counting()
                ));

        Map<String, Long> completeData = new LinkedHashMap<>();
        completeData.put("Hiver", seasonalData.getOrDefault("Hiver", 0L));
        completeData.put("Printemps", seasonalData.getOrDefault("Printemps", 0L));
        completeData.put("Été", seasonalData.getOrDefault("Été", 0L));
        completeData.put("Automne", seasonalData.getOrDefault("Automne", 0L));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        completeData.forEach((season, count) -> {
            XYChart.Data<String, Number> data = new XYChart.Data<>(season, count);
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    String color = switch (season) {
                        case "Hiver" -> "#3498db";
                        case "Printemps" -> "#2ecc71";
                        case "Été" -> "#e74c3c";
                        case "Automne" -> "#f39c12";
                        default -> "#9b59b6";
                    };
                    newNode.setStyle("-fx-bar-fill: " + color + ";");
                    Tooltip.install(newNode, new Tooltip(season + ": " + count + " réservations"));
                }
            });
            series.getData().add(data);
        });

        chart.getData().add(series);
        return chart;
    }


    private TableView<Map.Entry<String, Long>> createSeasonalDetailsTable() {
        TableView<Map.Entry<String, Long>> table = new TableView<>();

        Map<String, Long> seasonalData = reservationService.getAll().stream()
                .collect(Collectors.groupingBy(
                        r -> {
                            Optional<ProposerEvent> event = eventService.getAll().stream()
                                    .filter(e -> e.getTitle().equals(r.getEventTitle()))
                                    .findFirst();
                            return event.isPresent() ? getSeason(event.get().getStartDate()) : "Inconnu";
                        },
                        Collectors.counting()
                ));

        TableColumn<Map.Entry<String, Long>, String> seasonCol = new TableColumn<>("Saison");
        seasonCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getKey()));

        TableColumn<Map.Entry<String, Long>, Long> countCol = new TableColumn<>("Réservations");
        countCol.setCellValueFactory(param -> new SimpleLongProperty(param.getValue().getValue()).asObject());

        seasonCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
        countCol.setStyle("-fx-alignment: CENTER;");

        table.getColumns().addAll(seasonCol, countCol);

        ObservableList<Map.Entry<String, Long>> items = FXCollections.observableArrayList(seasonalData.entrySet());
        items.sort(Map.Entry.<String, Long>comparingByValue().reversed());
        table.setItems(items);

        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        table.setPrefHeight(150);

        return table;
    }

    private VBox createFinancialStatsBox() {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        Label title = new Label("💵 Statistiques Financières");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Calcul du revenu total - version simplifiée sans nbPlaces
        double totalRevenue = reservationService.getAll().stream()
                .mapToDouble(r -> {
                    Optional<ProposerEvent> event = eventService.getAll().stream()
                            .filter(e -> e.getTitle().equals(r.getEventTitle()))
                            .findFirst();
                    return event.map(ProposerEvent::getPrice).orElse(0.0); // Suppression de * r.getNbPlaces()
                })
                .sum();

        Label totalLabel = new Label(String.format("Revenu total: %.2f TND", totalRevenue));
        totalLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #27ae60;");

        // Top événements - version simplifiée sans nbPlaces
        Map<String, Double> revenueByEvent = new HashMap<>();
        reservationService.getAll().forEach(r -> {
            Optional<ProposerEvent> event = eventService.getAll().stream()
                    .filter(e -> e.getTitle().equals(r.getEventTitle()))
                    .findFirst();
            event.ifPresent(e -> {
                revenueByEvent.merge(
                        e.getTitle(),
                        e.getPrice(), // Suppression de * r.getNbPlaces()
                        Double::sum
                );
            });
        });

        List<Map.Entry<String, Double>> topEvents = revenueByEvent.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        VBox topEventsBox = new VBox(8);
        topEventsBox.setAlignment(Pos.CENTER_LEFT);
        Label topLabel = new Label("Top 3 événements:");
        topLabel.setStyle("-fx-font-weight: bold;");
        topEventsBox.getChildren().add(topLabel);

        String[] medals = {"🥇", "🥈", "🥉"};
        for (int i = 0; i < topEvents.size(); i++) {
            Map.Entry<String, Double> entry = topEvents.get(i);
            HBox hbox = new HBox(5);
            hbox.setAlignment(Pos.CENTER_LEFT);

            Label medalLabel = new Label(medals[i]);
            Label eventLabel = new Label(entry.getKey() + ": " + String.format("%.2f TND", entry.getValue()));

            hbox.getChildren().addAll(medalLabel, eventLabel);
            topEventsBox.getChildren().add(hbox);
        }

        box.getChildren().addAll(title, totalLabel, topEventsBox);
        return box;
    }

    @FXML
    public void initialize() {
        // Initialize table columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        reservationsColumn.setCellValueFactory(new PropertyValueFactory<>("reservationsCount"));
        // Initialisation des notifications
        if (notificationButton == null) {
            System.err.println("Erreur: notificationButton est null!");
        } else {
            notificationButton.setOnAction(e -> toggleNotifications());
        }

        if (notificationsContainer == null) {
            System.err.println("Erreur: notificationsContainer est null!");
        } else {
            notificationsContainer.setVisible(false);
            notificationsContainer.setManaged(false);
            notificationsContainer.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1; " +
                    "-fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
        }

        if (notificationBadge == null) {
            System.err.println("Erreur: notificationBadge est null!");
        } else {
            notificationBadge.setVisible(false);
            notificationBadge.setStyle("-fx-background-color: red; -fx-text-fill: white; " +
                    "-fx-background-radius: 10; -fx-min-width: 20; -fx-min-height: 20; " +
                    "-fx-alignment: center;");
        }
        // Format date column
        dateColumn.setCellFactory(column -> new TableCell<ProposerEvent, Date>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(format.format(item));
                }
            }
        });
        locationColumn.setCellFactory(column -> new TableCell<ProposerEvent, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-padding: 8; -fx-border-color: #e0e6f0; -fx-border-width: 0 1 1 0; -fx-alignment: CENTER;");
                }
            }
        });

        // Format price column
        priceColumn.setCellFactory(column -> new TableCell<ProposerEvent, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f TND", item));
                }
            }
        });
        initializeNotifications();
        startNotificationChecker();
        configureTableColumns();
        // Setup actions column
        setupActionsColumn();

        // Load events
        loadEvents();

        // Set initial view
        showEvents();
    }

    private void initializeNotifications() {
        // Assurez-vous que le conteneur est correctement initialisé
        if (notificationsContainer == null) {
            System.err.println("Erreur: notificationsContainer n'est pas initialisé!");
            return;
        }

        notificationsContainer.setVisible(false);
        notificationsContainer.setManaged(false);
        notificationBadge.setVisible(false);

        // Style du conteneur de notifications
        notificationsContainer.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
        notificationsContainer.setSpacing(5);
        notificationsContainer.setPadding(new Insets(10));

        // Gestionnaire d'événements pour le bouton de notification
        if (notificationButton != null) {
            notificationButton.setOnAction(e -> toggleNotifications());
        } else {
            System.err.println("Erreur: notificationButton n'est pas initialisé!");
        }
    }
    private void startNotificationChecker() {
        notificationTimer = new Timer(true);
        notificationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkNewReservations();
            }
        }, 0, 5000); // Vérifie toutes les 5 secondes
    }

    private void checkNewReservations() {
        try {
            List<ReserverEvent> currentReservations = reservationService.getAll();

            if (lastKnownReservations.isEmpty()) {
                lastKnownReservations = new ArrayList<>(currentReservations);
                return;
            }

            if (currentReservations.size() > lastKnownReservations.size()) {
                List<ReserverEvent> newReservations = new ArrayList<>(currentReservations);
                newReservations.removeAll(lastKnownReservations);

                Platform.runLater(() -> {
                    showBackendNotification(newReservations);
                });
            }

            lastKnownReservations = new ArrayList<>(currentReservations);
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification des nouvelles réservations: " + e.getMessage());
        }
    }
    public void stop() {
        if (notificationTimer != null) {
            notificationTimer.cancel();
            notificationTimer.purge();
        }
    }
    private void showBackendNotification(List<ReserverEvent> newReservations) {
        for (ReserverEvent reservation : newReservations) {

            HBox notificationBox = new HBox(10);
            notificationBox.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 5; -fx-padding: 10; -fx-border-color: #bbdefb; -fx-border-width: 1; -fx-border-radius: 5;");
            notificationBox.setAlignment(Pos.CENTER_LEFT);

            // Icône de notification
            Label icon = new Label("🔔");
            icon.setStyle("-fx-font-size: 16px;");

            VBox content = new VBox(3);

            Label titleLabel = new Label("Nouvelle réservation");
            titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0d47a1;");

            Label eventLabel = new Label("Événement: " + reservation.getEventTitle());
            Label dateLabel = new Label("Date: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(reservation.getBookingDate()));

            content.getChildren().addAll(titleLabel, eventLabel, dateLabel);
            notificationBox.getChildren().addAll(icon, content);

            // Ajouter en haut du conteneur
            notificationsContainer.getChildren().add(0, notificationBox);

            // Animation d'entrée
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), notificationBox);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            // Auto-suppression après 8 secondes
            PauseTransition delay = new PauseTransition(Duration.seconds(8));
            delay.setOnFinished(e -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(500), notificationBox);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(ev -> notificationsContainer.getChildren().remove(notificationBox));
                fadeOut.play();
            });
            delay.play();
        }
    }

    // Ajoutez cette méthode pour arrêter le timer lorsque le contrôleur n'est plus utilisé
    public void stopNotificationChecker() {
        if (notificationTimer != null) {
            notificationTimer.cancel();
        }
    }
    private void configureTableColumns() {
        // Initialize table columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        reservationsColumn.setCellValueFactory(new PropertyValueFactory<>("reservationsCount"));
        notificationsContainer.setVisible(false);
        notificationsContainer.setManaged(false); // Pour ne pas occuper d'espace quand caché
        notificationBadge.setVisible(false); // Caché par défaut
        // Format date column
        dateColumn.setCellFactory(column -> new TableCell<ProposerEvent, Date>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : format.format(item));
            }
        });

        // Format location column
        locationColumn.setCellFactory(column -> new TableCell<ProposerEvent, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-padding: 8; -fx-border-color: #e0e6f0; -fx-border-width: 0 1 1 0; -fx-alignment: CENTER;");
                }
            }
        });

        // Format price column
        priceColumn.setCellFactory(column -> new TableCell<ProposerEvent, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f TND", item));
            }
        });

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<ProposerEvent, Void>() {
            private final Button viewBtn = createActionButton("Voir", "#3498db");
            private final Button editBtn = createActionButton("Modifier", "#f39c12");
            private final Button deleteBtn = createActionButton("Supprimer", "#e74c3c");
            private final HBox pane = new HBox(viewBtn, editBtn, deleteBtn);

            {
                pane.setSpacing(5);
                pane.setAlignment(Pos.CENTER);
                setupButtonActions();
            }

            private Button createActionButton(String text, String color) {
                Button button = new Button(text);
                button.setStyle(
                        "-fx-background-color: " + color + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 12px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 5 10; " +
                                "-fx-background-radius: 5; " +
                                "-fx-border-radius: 5; " +
                                "-fx-cursor: hand; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"
                );

                // Effet de survol
                button.setOnMouseEntered(e -> button.setStyle(
                        "-fx-background-color: " + darkenColor(color) + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 12px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 5 10; " +
                                "-fx-background-radius: 5; " +
                                "-fx-border-radius: 5; " +
                                "-fx-cursor: hand; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 1);"
                ));

                button.setOnMouseExited(e -> button.setStyle(
                        "-fx-background-color: " + color + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 12px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 5 10; " +
                                "-fx-background-radius: 5; " +
                                "-fx-border-radius: 5; " +
                                "-fx-cursor: hand; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"
                ));

                return button;
            }

            private String darkenColor(String hexColor) {
                // Logique simplifiée pour assombrir la couleur
                return hexColor.replaceFirst("#", "#80");
            }

            private void setupButtonActions() {
                viewBtn.setOnAction(event -> viewDetails(getCurrentEvent()));
                editBtn.setOnAction(event -> editEvent(getCurrentEvent()));
                deleteBtn.setOnAction(event -> deleteEvent(getCurrentEvent()));
            }

            private ProposerEvent getCurrentEvent() {
                return getTableView().getItems().get(getIndex());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                    setStyle("-fx-padding: 8; -fx-border-color: #e0e6f0; -fx-border-width: 0 1 1 0; -fx-alignment: CENTER;");
                }
            }
        });
    }

    private void loadEvents() {
        ObservableList<ProposerEvent> events = FXCollections.observableArrayList(
                eventService.getAll().stream()
                        .peek(event -> {
                            long count = reservationService.getAll().stream()
                                    .filter(r -> r.getEventTitle().equals(event.getTitle()))
                                    .count();
                            event.setReservationsCount((int) count);
                        })
                        .toList()
        );
        eventsTable.setItems(events);
    }


    private String getSeason(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH) + 1; // Janvier = 1

        if (month >= 3 && month <= 5) return "Printemps";
        else if (month >= 6 && month <= 8) return "Été";
        else if (month >= 9 && month <= 11) return "Automne";
        else return "Hiver";
    }

    @FXML
    public void showEvents() {
        setActiveTab(btnEvents);
        centerContent.getChildren().clear();
        centerContent.getChildren().addAll(eventsTable, addButton);
    }


    private String getTabButtonStyle(boolean isSelected) {
        return "-fx-background-color: " + (isSelected ? "#3498db" : "#f8f9fa") + "; " +
                "-fx-text-fill: " + (isSelected ? "white" : "#2c3e50") + "; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10 20; " +
                "-fx-background-radius: 5; " +
                "-fx-border-radius: 5; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1); " +
                "-fx-cursor: hand;";
    }

    private void viewDetails(ProposerEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetails.fxml"));
            Parent root = loader.load();

            EventDetailsController controller = loader.getController();
            controller.setEventData(event);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails de l'événement");
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les détails", Alert.AlertType.ERROR);
        }
    }

    private void editEvent(ProposerEvent event) {
        Dialog<ProposerEvent> dialog = new Dialog<>();
        dialog.setTitle("Modifier l'événement");
        dialog.setHeaderText("Modifier les détails de l'événement");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField(event.getTitle());
        TextField locationField = new TextField(event.getLocation());
        TextField priceField = new TextField(String.valueOf(event.getPrice()));
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(event.getStartDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());

        grid.add(new Label("Titre:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Lieu:"), 0, 1);
        grid.add(locationField, 1, 1);
        grid.add(new Label("Prix:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Date:"), 0, 3);
        grid.add(datePicker, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                event.setTitle(titleField.getText());
                event.setLocation(locationField.getText());
                event.setPrice(Double.parseDouble(priceField.getText()));
                event.setStartDate(java.sql.Date.valueOf(datePicker.getValue()));
                return event;
            }
            return null;
        });

        Optional<ProposerEvent> result = dialog.showAndWait();

        result.ifPresent(updatedEvent -> {
            eventService.update(updatedEvent);
            loadEvents();
            showNotification("Événement mis à jour avec succès!");
        });
    }

    private void deleteEvent(ProposerEvent event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer l'événement");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cet événement ?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            eventService.delete(event);
            loadEvents();
            showNotification("Événement supprimé avec succès!");
        }
    }

    @FXML
    private void addEvent() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AjouterEvent.fxml"));
            Stage stage = (Stage) addButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un Événement");
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showNotification(String message) {
        Label notificationLabel = new Label(message);
        notificationLabel.setStyle(
                "-fx-background-color: linear-gradient(to right, #16A085, #1ABC9C);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 24 12 24;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: #1ABC9C;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0.4, 2, 2);"
        );
        notificationLabel.setAlignment(Pos.CENTER);
        notificationLabel.setMaxWidth(Double.MAX_VALUE);
        notificationLabel.setTextAlignment(TextAlignment.CENTER);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), notificationLabel);
        slideIn.setFromY(-50);
        slideIn.setToY(0);
        slideIn.play();

        centerContent.getChildren().add(0, notificationLabel);

        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(event -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(1000), notificationLabel);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> centerContent.getChildren().remove(notificationLabel));
            fadeOut.play();
        });
        pause.play();
    }
}