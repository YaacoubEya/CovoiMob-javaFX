package controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.effect.Glow;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.control.DatePicker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import tn.esprit.models.OffreCovoiturage;
import tn.esprit.models.Reservation;
import tn.esprit.services.ServiceOffreCovoiturage;
import tn.esprit.services.ServiceReservation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class AllOffreController {

    @FXML
    private BorderPane borderPane;

    @FXML
    private VBox centerContent;

    @FXML
    private Button btnUsers;

    @FXML
    private Button btnProducts;

    @FXML
    private Button btnStats;
    @FXML
    private Button btnEvents;

    @FXML
    private void handleLogoutAction(ActionEvent event) {
        Integer userId = SessionManager.getInstance().getUserId();
        if (userId != null) {
            System.out.println("✅ Logging out user ID: " + userId);
            SessionManager.getInstance().clearSession();
        } else {
            System.out.println("⚠️ No active session to logout");
        }
        loadScene("/login.fxml", (Node) event.getSource());
    }

    @FXML
    void handleEvents(ActionEvent event) {
        System.out.println("Bouton Événements cliqué"); // Debug
        try {
            System.out.println("Tentative de chargement de EventsTableView.fxml");
            URL url = getClass().getResource("/EventsTableView.fxml");
            if (url == null) {
                System.err.println("Fichier EventsTableView.fxml introuvable!");
                throw new IOException("Fichier FXML introuvable");
            }
            System.out.println("URL du fichier FXML: " + url);

            Parent root = FXMLLoader.load(url);
            Scene scene = new Scene(root, 1500, 765);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de EventsTableView.fxml:");
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la vue des événements: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    public void showStatistics() {
        btnUsers.setStyle(getTabButtonStyle(false));
        btnProducts.setStyle(getTabButtonStyle(false));
        btnStats.setStyle(getTabButtonStyle(true));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox chartsContainer = new VBox(30);
        chartsContainer.setAlignment(Pos.CENTER);
        chartsContainer.setPadding(new Insets(20));
        chartsContainer.setFillWidth(true);

        // First two charts remain as LineCharts
        LineChart<String, Number> offersChart = createChart(
                "Nombre d'offres crées (5 derniers jours)",
                "Les derniers 5 jours",
                "Nombre d'offres crées",
                getOfferData()
        );
        offersChart.setMaxWidth(Double.MAX_VALUE);
        offersChart.setPrefHeight(350);
        offersChart.setOpacity(0);

        LineChart<String, Number> offerPriceChart = createChart(
                "Prix moyen des offres (5 derniers jours)",
                "Les derniers 5 jours",
                "Prix moyen en Dinars",
                getOfferAveragePriceData()
        );
        offerPriceChart.setMaxWidth(Double.MAX_VALUE);
        offerPriceChart.setPrefHeight(350);
        offerPriceChart.setOpacity(0);

        // Next two charts as BarCharts (histograms)
        BarChart<String, Number> offerPlacesChart = createBarChart(
                "Places disponibles (5 derniers jours)",
                "Les derniers 5 jours",
                "Nombre de places disponibles",
                getOfferAvailablePlacesData()
        );
        offerPlacesChart.setMaxWidth(Double.MAX_VALUE);
        offerPlacesChart.setPrefHeight(350);
        offerPlacesChart.setOpacity(0);

        BarChart<String, Number> reservationsChart = createBarChart(
                "Nombre de réservations créées (5 derniers jours)",
                "Les derniers 5 jours",
                "Nombre de réservations",
                getReservationData()
        );
        reservationsChart.setMaxWidth(Double.MAX_VALUE);
        reservationsChart.setPrefHeight(350);
        reservationsChart.setOpacity(0);

        // Last two charts as AreaCharts
        AreaChart<String, Number> reservationStatusChart = createAreaChart(
                "Réservations confirmées (5 derniers jours)",
                "Les derniers 5 jours",
                "Nombre de réservations confirmées",
                getReservationStatusData()
        );
        reservationStatusChart.setMaxWidth(Double.MAX_VALUE);
        reservationStatusChart.setPrefHeight(350);
        reservationStatusChart.setOpacity(0);

        AreaChart<String, Number> reservationCostChart = createAreaChart(
                "Coût total des réservations (5 derniers jours)",
                "Les derniers 5 jours",
                "Coût total en Dinars",
                getReservationCostData()
        );
        reservationCostChart.setMaxWidth(Double.MAX_VALUE);
        reservationCostChart.setPrefHeight(350);
        reservationCostChart.setOpacity(0);

        // Add charts to container
        chartsContainer.getChildren().addAll(
                offersChart,
                offerPriceChart,
                offerPlacesChart,
                reservationsChart,
                reservationStatusChart,
                reservationCostChart
        );

        // Apply animations to each chart
        int delay = 0;
        for (Node chart : chartsContainer.getChildren()) {
            // Fade-in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(800), chart);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            // Scale animation for a "pop-in" effect
            ScaleTransition scale = new ScaleTransition(Duration.millis(800), chart);
            scale.setFromX(0.8);
            scale.setFromY(0.8);
            scale.setToX(1.0);
            scale.setToY(1.0);

            // Slight rotation for a dynamic entrance
            RotateTransition rotate = new RotateTransition(Duration.millis(800), chart);
            rotate.setFromAngle(-5);
            rotate.setToAngle(0);

            // Add a glow effect that fades in and out
            Glow glow = new Glow(0.0);
            chart.setEffect(glow);
            FadeTransition glowFade = new FadeTransition(Duration.millis(1000), chart);
            glowFade.setFromValue(0.0);
            glowFade.setToValue(0.6);
            glowFade.setOnFinished(e -> glow.setLevel(0.3)); // Settle at a subtle glow

            // Combine animations into a sequence
            SequentialTransition sequence = new SequentialTransition(
                    new PauseTransition(Duration.millis(delay)),
                    new ParallelTransition(fadeIn, scale, rotate, glowFade)
            );
            sequence.play();

            delay += 200; // Stagger each chart's animation by 200ms
        }

        scrollPane.setContent(chartsContainer);
        centerContent.getChildren().setAll(scrollPane);
    }

    private LineChart<String, Number> createChart(String title, String xLabel, String yLabel, Map<Integer, ? extends Number> data) {
        // Create axes
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();

        // Style axes with warmer colors
        xAxis.setLabel(xLabel);
        xAxis.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-weight: bold;" +
                        "-fx-tick-label-fill: #2D3748;" +
                        "-fx-tick-label-font-size: 12px;"
        );

        yAxis.setLabel(yLabel);
        yAxis.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-weight: bold;" +
                        "-fx-tick-label-fill: #2D3748;" +
                        "-fx-tick-label-font-size: 12px;"
        );
        yAxis.setTickUnit(1);
        yAxis.setMinorTickVisible(false);
        yAxis.setAutoRanging(true);

        // Create the chart
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);

        // Set title with warmer styling
        chart.setTitle(title);
        chart.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #002557;" +
                        "-fx-border-color: #E2E8F0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        // Style chart title with a warm gradient
        Node chartTitle = chart.lookup(".chart-title");
        if (chartTitle != null) {
            chartTitle.setStyle(
                    "-fx-font-size: 20px;" +
                            "-fx-font-family: 'Segoe UI';" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #002557;" +
                            "-fx-padding: 10 0 20 0;"
            );
        }

        chart.setCreateSymbols(true);
        chart.setLegendVisible(false);
        chart.setAnimated(true); // Enable smooth animations
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(true);

        // Add warmer CSS styling for curves and data points
        chart.getStylesheets().add(
                "data:text/css," +
                        ".chart-series-line {" +
                        "    -fx-stroke: linear-gradient(to right, #B91C1C, #EA580C);" + // Deep red to deep orange gradient
                        "    -fx-stroke-width: 3px;" +
                        "    -fx-effect: dropshadow(gaussian, rgba(185,28,28,0.3), 8, 0, 0, 2);" + // Shadow matches line color
                        "}" +
                        ".chart-line-symbol {" +
                        "    -fx-background-color: #B91C1C, #FEE2E2;" + // Deep red with light red fill
                        "    -fx-background-insets: 0, 2;" +
                        "    -fx-background-radius: 8px;" +
                        "    -fx-padding: 6px;" +
                        "    -fx-shape: \"M5,0 C5,2.76 2.76,5 0,5 C-2.76,5 -5,2.76 -5,0 C-5,-2.76 -2.76,-5 0,-5 C2.76,-5 5,-2.76 5,0 Z\";" +
                        "}" +
                        ".chart-line-symbol:hover {" +
                        "    -fx-background-color: #EA580C, #FEE2E2;" + // Deep orange on hover
                        "    -fx-scale-x: 1.2;" +
                        "    -fx-scale-y: 1.2;" +
                        "}" +
                        ".chart-plot-background {" +
                        "    -fx-background-color: #F7FAFC;" +
                        "}" +
                        ".chart-vertical-grid-lines {" +
                        "    -fx-stroke: #EDF2F7;" +
                        "}" +
                        ".chart-horizontal-grid-lines {" +
                        "    -fx-stroke: #EDF2F7;" +
                        "}" +
                        ".axis {" +
                        "    -fx-font-family: 'Segoe UI';" +
                        "    -fx-tick-label-fill: #4A2C0B;" + // Darker brown for tick labels
                        "    -fx-font-size: 12px;" +
                        "}" +
                        ".axis-label {" +
                        "    -fx-font-family: 'Segoe UI';" +
                        "    -fx-font-size: 16px;" +
                        "    -fx-font-weight: bold;" +
                        "    -fx-text-fill: #9F1239;" + // Deep rose for axis labels
                        "}"
        );

        // Create the series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] dayLabels = {"J-4", "J-3", "J-2", "J-1", "Aujourd'hui"};

        for (int i = 0; i < 5; i++) {
            series.getData().add(new XYChart.Data<>(dayLabels[i], data.get(i)));
        }

        chart.getData().add(series);

        // Add enhanced value labels and tooltips after chart is rendered
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> item : series.getData()) {
                Node node = item.getNode();
                if (node != null) {
                    // Create value label with premium styling
                    StackPane labelContainer = new StackPane();
                    Label label = new Label(String.format("%.2f", item.getYValue().doubleValue()));
                    label.setStyle(
                            "-fx-font-size: 12px;" +
                                    "-fx-font-family: 'Segoe UI';" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-text-fill: #FFFFFF;" +
                                    "-fx-background-color: rgba(45,55,72,0.9);" +
                                    "-fx-padding: 4px 8px;" +
                                    "-fx-background-radius: 5px;" +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 1);"
                    );
                    labelContainer.getChildren().add(label);

                    // Add label to chart
                    Pane chartPane = (Pane) chart.lookup(".chart-plot-background");
                    if (chartPane != null) {
                        chartPane.getChildren().add(labelContainer);

                        // Position label above data point
                        double x = node.getBoundsInParent().getMinX() - label.prefWidth(-1) / 2;
                        double y = node.getBoundsInParent().getMinY() - 30;
                        labelContainer.setLayoutX(x);
                        labelContainer.setLayoutY(y);

                        // Update position on resize
                        node.boundsInParentProperty().addListener((obs, old, bounds) -> {
                            labelContainer.setLayoutX(bounds.getMinX() - label.prefWidth(-1) / 2);
                            labelContainer.setLayoutY(bounds.getMinY() - 30);
                        });

                        // Add fade-in animation for label
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), labelContainer);
                        fadeIn.setFromValue(0.0);
                        fadeIn.setToValue(1.0);
                        fadeIn.play();
                    }

                    // Add tooltip with premium styling
                    Tooltip tooltip = new Tooltip(dayLabels[series.getData().indexOf(item)] + ": " + item.getYValue());
                    tooltip.setStyle(
                            "-fx-font-family: 'Segoe UI';" +
                                    "-fx-font-size: 12px;" +
                                    "-fx-background-color: rgba(45,55,72,0.95);" +
                                    "-fx-text-fill: #FFFFFF;" +
                                    "-fx-background-radius: 5px;" +
                                    "-fx-padding: 8px;"
                    );
                    Tooltip.install(node, tooltip);

                    // Add hover animation for data point
                    node.setOnMouseEntered(e -> {
                        node.setScaleX(1.3);
                        node.setScaleY(1.3);
                        labelContainer.setVisible(true);
                    });
                    node.setOnMouseExited(e -> {
                        node.setScaleX(1.0);
                        node.setScaleY(1.0);
                        labelContainer.setVisible(false);
                    });
                }
            }

            // Smooth curve animation
            for (Node node : chart.lookupAll(".chart-series-line")) {
                FadeTransition fade = new FadeTransition(Duration.millis(1000), node);
                fade.setFromValue(0.0);
                fade.setToValue(1.0);
                fade.play();
            }
        });

        return chart;
    }

    private BarChart<String, Number> createBarChart(String title, String xLabel, String yLabel, Map<Integer, ? extends Number> data) {
        // Create axes
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();

        // Style axes with warmer colors
        xAxis.setLabel(xLabel);
        xAxis.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-weight: bold;" +
                        "-fx-tick-label-fill: #2D3748;" +
                        "-fx-tick-label-font-size: 12px;"
        );

        yAxis.setLabel(yLabel);
        yAxis.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-weight: bold;" +
                        "-fx-tick-label-fill: #2D3748;" +
                        "-fx-tick-label-font-size: 12px;"
        );
        yAxis.setTickUnit(1);
        yAxis.setMinorTickVisible(false);
        yAxis.setAutoRanging(true);

        // Create the chart
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);

        // Set title with warmer styling
        chart.setTitle(title);
        chart.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #002557;" +
                        "-fx-border-color: #E2E8F0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        // Style chart title with a warm gradient
        Node chartTitle = chart.lookup(".chart-title");
        if (chartTitle != null) {
            chartTitle.setStyle(
                    "-fx-font-size: 20px;" +
                            "-fx-font-family: 'Segoe UI';" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #002557;" +
                            "-fx-padding: 10 0 20 0;"
            );
        }

        chart.setLegendVisible(false);
        chart.setAnimated(true); // Enable smooth animations
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(true);

        // Add CSS styling for bars
        chart.getStylesheets().add(
                "data:text/css," +
                        ".default-color0.chart-bar {" +
                        "    -fx-bar-fill: #3B82F6;" + // Blue color for bars
                        "    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);" +
                        "}" +
                        ".default-color0.chart-bar:hover {" +
                        "    -fx-bar-fill: #2563EB;" + // Darker blue on hover
                        "}" +
                        ".chart-plot-background {" +
                        "    -fx-background-color: #F7FAFC;" +
                        "}" +
                        ".chart-vertical-grid-lines {" +
                        "    -fx-stroke: #EDF2F7;" +
                        "}" +
                        ".chart-horizontal-grid-lines {" +
                        "    -fx-stroke: #EDF2F7;" +
                        "}" +
                        ".axis {" +
                        "    -fx-font-family: 'Segoe UI';" +
                        "    -fx-tick-label-fill: #4A2C0B;" +
                        "    -fx-font-size: 12px;" +
                        "}" +
                        ".axis-label {" +
                        "    -fx-font-family: 'Segoe UI';" +
                        "    -fx-font-size: 16px;" +
                        "    -fx-font-weight: bold;" +
                        "    -fx-text-fill: #9F1239;" +
                        "}"
        );

        // Create the series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] dayLabels = {"J-4", "J-3", "J-2", "J-1", "Aujourd'hui"};

        for (int i = 0; i < 5; i++) {
            series.getData().add(new XYChart.Data<>(dayLabels[i], data.get(i)));
        }

        chart.getData().add(series);

        // Add enhanced value labels and tooltips after chart is rendered
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> item : series.getData()) {
                Node node = item.getNode();
                if (node != null) {
                    // Create value label with premium styling
                    StackPane labelContainer = new StackPane();
                    Label label = new Label(String.format("%.2f", item.getYValue().doubleValue()));
                    label.setStyle(
                            "-fx-font-size: 12px;" +
                                    "-fx-font-family: 'Segoe UI';" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-text-fill: #FFFFFF;" +
                                    "-fx-background-color: rgba(45,55,72,0.9);" +
                                    "-fx-padding: 4px 8px;" +
                                    "-fx-background-radius: 5px;" +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 1);"
                    );
                    labelContainer.getChildren().add(label);

                    // Add label to chart
                    Pane chartPane = (Pane) chart.lookup(".chart-plot-background");
                    if (chartPane != null) {
                        chartPane.getChildren().add(labelContainer);

                        // Position label above bar
                        double x = node.getBoundsInParent().getMinX() + node.getBoundsInParent().getWidth() / 2 - label.prefWidth(-1) / 2;
                        double y = node.getBoundsInParent().getMinY() - 30;
                        labelContainer.setLayoutX(x);
                        labelContainer.setLayoutY(y);

                        // Update position on resize
                        node.boundsInParentProperty().addListener((obs, old, bounds) -> {
                            labelContainer.setLayoutX(bounds.getMinX() + bounds.getWidth() / 2 - label.prefWidth(-1) / 2);
                            labelContainer.setLayoutY(bounds.getMinY() - 30);
                        });

                        // Add fade-in animation for label
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), labelContainer);
                        fadeIn.setFromValue(0.0);
                        fadeIn.setToValue(1.0);
                        fadeIn.play();
                    }

                    // Add tooltip with premium styling
                    Tooltip tooltip = new Tooltip(dayLabels[series.getData().indexOf(item)] + ": " + item.getYValue());
                    tooltip.setStyle(
                            "-fx-font-family: 'Segoe UI';" +
                                    "-fx-font-size: 12px;" +
                                    "-fx-background-color: rgba(45,55,72,0.95);" +
                                    "-fx-text-fill: #FFFFFF;" +
                                    "-fx-background-radius: 5px;" +
                                    "-fx-padding: 8px;"
                    );
                    Tooltip.install(node, tooltip);

                    // Add hover animation for bar
                    node.setOnMouseEntered(e -> {
                        node.setScaleX(1.05);
                        node.setScaleY(1.05);
                        labelContainer.setVisible(true);
                    });
                    node.setOnMouseExited(e -> {
                        node.setScaleX(1.0);
                        node.setScaleY(1.0);
                        labelContainer.setVisible(false);
                    });
                }
            }
        });

        return chart;
    }

    private AreaChart<String, Number> createAreaChart(String title, String xLabel, String yLabel, Map<Integer, ? extends Number> data) {
        // Create axes
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();

        // Style axes with warmer colors
        xAxis.setLabel(xLabel);
        xAxis.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-weight: bold;" +
                        "-fx-tick-label-fill: #2D3748;" +
                        "-fx-tick-label-font-size: 12px;"
        );

        yAxis.setLabel(yLabel);
        yAxis.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-weight: bold;" +
                        "-fx-tick-label-fill: #2D3748;" +
                        "-fx-tick-label-font-size: 12px;"
        );
        yAxis.setTickUnit(1);
        yAxis.setMinorTickVisible(false);
        yAxis.setAutoRanging(true);

        // Create the chart
        AreaChart<String, Number> chart = new AreaChart<>(xAxis, yAxis);

        // Set title with warmer styling
        chart.setTitle(title);
        chart.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #002557;" +
                        "-fx-border-color: #E2E8F0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        // Style chart title with a warm gradient
        Node chartTitle = chart.lookup(".chart-title");
        if (chartTitle != null) {
            chartTitle.setStyle(
                    "-fx-font-size: 20px;" +
                            "-fx-font-family: 'Segoe UI';" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #002557;" +
                            "-fx-padding: 10 0 20 0;"
            );
        }

        chart.setLegendVisible(false);
        chart.setAnimated(true); // Enable smooth animations
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(true);

        // Add CSS styling for area
        chart.getStylesheets().add(
                "data:text/css," +
                        ".default-color0.chart-series-area-fill {" +
                        "    -fx-fill: rgba(75, 192, 192, 0.5);" + // Teal color with transparency
                        "}" +
                        ".default-color0.chart-series-area-line {" +
                        "    -fx-stroke: #4BC0C0;" + // Teal color for line
                        "    -fx-stroke-width: 2px;" +
                        "}" +
                        ".default-color0.chart-area-symbol {" +
                        "    -fx-background-color: #4BC0C0, white;" +
                        "    -fx-background-insets: 0, 2;" +
                        "    -fx-background-radius: 5px;" +
                        "    -fx-padding: 5px;" +
                        "}" +
                        ".chart-plot-background {" +
                        "    -fx-background-color: #F7FAFC;" +
                        "}" +
                        ".chart-vertical-grid-lines {" +
                        "    -fx-stroke: #EDF2F7;" +
                        "}" +
                        ".chart-horizontal-grid-lines {" +
                        "    -fx-stroke: #EDF2F7;" +
                        "}" +
                        ".axis {" +
                        "    -fx-font-family: 'Segoe UI';" +
                        "    -fx-tick-label-fill: #4A2C0B;" +
                        "    -fx-font-size: 12px;" +
                        "}" +
                        ".axis-label {" +
                        "    -fx-font-family: 'Segoe UI';" +
                        "    -fx-font-size: 16px;" +
                        "    -fx-font-weight: bold;" +
                        "    -fx-text-fill: #9F1239;" +
                        "}"
        );

        // Create the series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] dayLabels = {"J-4", "J-3", "J-2", "J-1", "Aujourd'hui"};

        for (int i = 0; i < 5; i++) {
            series.getData().add(new XYChart.Data<>(dayLabels[i], data.get(i)));
        }

        chart.getData().add(series);

        // Add enhanced value labels and tooltips after chart is rendered
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> item : series.getData()) {
                Node node = item.getNode();
                if (node != null) {
                    // Create value label with premium styling
                    StackPane labelContainer = new StackPane();
                    Label label = new Label(String.format("%.2f", item.getYValue().doubleValue()));
                    label.setStyle(
                            "-fx-font-size: 12px;" +
                                    "-fx-font-family: 'Segoe UI';" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-text-fill: #FFFFFF;" +
                                    "-fx-background-color: rgba(45,55,72,0.9);" +
                                    "-fx-padding: 4px 8px;" +
                                    "-fx-background-radius: 5px;" +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 1);"
                    );
                    labelContainer.getChildren().add(label);

                    // Add label to chart
                    Pane chartPane = (Pane) chart.lookup(".chart-plot-background");
                    if (chartPane != null) {
                        chartPane.getChildren().add(labelContainer);

                        // Position label above data point
                        double x = node.getBoundsInParent().getMinX() - label.prefWidth(-1) / 2;
                        double y = node.getBoundsInParent().getMinY() - 30;
                        labelContainer.setLayoutX(x);
                        labelContainer.setLayoutY(y);

                        // Update position on resize
                        node.boundsInParentProperty().addListener((obs, old, bounds) -> {
                            labelContainer.setLayoutX(bounds.getMinX() - label.prefWidth(-1) / 2);
                            labelContainer.setLayoutY(bounds.getMinY() - 30);
                        });

                        // Add fade-in animation for label
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), labelContainer);
                        fadeIn.setFromValue(0.0);
                        fadeIn.setToValue(1.0);
                        fadeIn.play();
                    }

                    // Add tooltip with premium styling
                    Tooltip tooltip = new Tooltip(dayLabels[series.getData().indexOf(item)] + ": " + item.getYValue());
                    tooltip.setStyle(
                            "-fx-font-family: 'Segoe UI';" +
                                    "-fx-font-size: 12px;" +
                                    "-fx-background-color: rgba(45,55,72,0.95);" +
                                    "-fx-text-fill: #FFFFFF;" +
                                    "-fx-background-radius: 5px;" +
                                    "-fx-padding: 8px;"
                    );
                    Tooltip.install(node, tooltip);

                    // Add hover animation for data point
                    node.setOnMouseEntered(e -> {
                        node.setScaleX(1.3);
                        node.setScaleY(1.3);
                        labelContainer.setVisible(true);
                    });
                    node.setOnMouseExited(e -> {
                        node.setScaleX(1.0);
                        node.setScaleY(1.0);
                        labelContainer.setVisible(false);
                    });
                }
            }
        });

        return chart;
    }


    private Map<Integer, Integer> getOfferData() {
        Map<Integer, Integer> dayCounts = new HashMap<>();
        LocalDate today = LocalDate.now();

        // Initialize last 5 days with count 0
        for (int i = 4; i >= 0; i--) {
            dayCounts.put(i, 0);
        }

        // Count offers for each day
        for (OffreCovoiturage offre : offreService.getAll()) {
            LocalDate offerDate = offre.getDate().toLocalDate();
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(offerDate, today);

            if (daysBetween >= 0 && daysBetween <= 4) {
                int dayKey = (int) (4 - daysBetween); // Reverse order (0 = 5 days ago, 4 = today)
                dayCounts.put(dayKey, dayCounts.get(dayKey) + 1);
            }
        }

        return dayCounts;
    }

    private Map<Integer, Integer> getReservationData() {
        Map<Integer, Integer> dayCounts = new HashMap<>();
        LocalDate today = LocalDate.now();

        // Initialize last 5 days with count 0
        for (int i = 4; i >= 0; i--) {
            dayCounts.put(i, 0);
        }

        // Count reservations for each day
        for (Reservation reservation : reservationService.getAll()) {
            LocalDate reservationDate = reservation.getCreatedAt().toLocalDate();
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(reservationDate, today);

            if (daysBetween >= 0 && daysBetween <= 4) {
                int dayKey = (int) (4 - daysBetween); // Reverse order (0 = 5 days ago, 4 = today)
                dayCounts.put(dayKey, dayCounts.get(dayKey) + 1);
            }
        }

        return dayCounts;
    }

    private Map<Integer, Double> getOfferAveragePriceData() {
        Map<Integer, List<Double>> priceSums = new HashMap<>();
        Map<Integer, Integer> offerCounts = new HashMap<>();
        LocalDate today = LocalDate.now();

        // Initialize last 5 days
        for (int i = 4; i >= 0; i--) {
            priceSums.put(i, new ArrayList<>());
            offerCounts.put(i, 0);
        }

        // Aggregate prices for each day
        for (OffreCovoiturage offre : offreService.getAll()) {
            LocalDate offerDate = offre.getDate().toLocalDate();
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(offerDate, today);

            if (daysBetween >= 0 && daysBetween <= 4) {
                int dayKey = (int) (4 - daysBetween);
                priceSums.get(dayKey).add((double) offre.getPrix());
                offerCounts.put(dayKey, offerCounts.get(dayKey) + 1);
            }
        }

        // Calculate averages
        Map<Integer, Double> dayAverages = new HashMap<>();
        for (int i = 4; i >= 0; i--) {
            List<Double> prices = priceSums.get(i);
            double average = prices.isEmpty() ? 0.0 : prices.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            dayAverages.put(i, average);
        }

        return dayAverages;
    }

    private Map<Integer, Integer> getOfferAvailablePlacesData() {
        Map<Integer, Integer> dayPlaces = new HashMap<>();
        LocalDate today = LocalDate.now();

        // Initialize last 5 days with count 0
        for (int i = 4; i >= 0; i--) {
            dayPlaces.put(i, 0);
        }

        // Sum available places for each day
        for (OffreCovoiturage offre : offreService.getAll()) {
            LocalDate offerDate = offre.getDate().toLocalDate();
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(offerDate, today);

            if (daysBetween >= 0 && daysBetween <= 4) {
                int dayKey = (int) (4 - daysBetween);
                dayPlaces.put(dayKey, dayPlaces.get(dayKey) + offre.getPlacesDispo());
            }
        }

        return dayPlaces;
    }

    private Map<Integer, Integer> getReservationStatusData() {
        Map<Integer, Integer> dayCounts = new HashMap<>();
        LocalDate today = LocalDate.now();

        // Initialize last 5 days with count 0
        for (int i = 4; i >= 0; i--) {
            dayCounts.put(i, 0);
        }

        // Count reservations with status "Confirmed"
        for (Reservation reservation : reservationService.getAll()) {
            if ("Confirmed".equalsIgnoreCase(reservation.getStatut())) {
                LocalDate reservationDate = reservation.getCreatedAt().toLocalDate();
                long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(reservationDate, today);

                if (daysBetween >= 0 && daysBetween <= 4) {
                    int dayKey = (int) (4 - daysBetween);
                    dayCounts.put(dayKey, dayCounts.get(dayKey) + 1);
                }
            }
        }

        return dayCounts;
    }

    private Map<Integer, Double> getReservationCostData() {
        Map<Integer, Double> dayCosts = new HashMap<>();
        LocalDate today = LocalDate.now();

        // Initialize last 5 days with cost 0
        for (int i = 4; i >= 0; i--) {
            dayCosts.put(i, 0.0);
        }

        // Sum the price of offers associated with reservations for each day
        for (Reservation reservation : reservationService.getAll()) {
            LocalDate reservationDate = reservation.getCreatedAt().toLocalDate();
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(reservationDate, today);

            if (daysBetween >= 0 && daysBetween <= 4) {
                int dayKey = (int) (4 - daysBetween);
                double currentCost = dayCosts.get(dayKey);
                double reservationCost = reservation.getOffre().getPrix();
                dayCosts.put(dayKey, currentCost + reservationCost);
            }
        }

        return dayCosts;
    }

    private TableView<OffreCovoiturage> offreTable;
    private TableView<Reservation> reservationTable;

    private final ServiceOffreCovoiturage offreService = new ServiceOffreCovoiturage();
    private final ServiceReservation reservationService = new ServiceReservation();

    @FXML
    public void initialize() {
        Label welcomeLabel = new Label("Hello Admouna");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        centerContent.getChildren().setAll(welcomeLabel);

        // Style des boutons pour les onglets
        btnUsers.setStyle(getTabButtonStyle(false));
        btnProducts.setStyle(getTabButtonStyle(false));
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

    @FXML
    public void showOffres() {
        btnUsers.setStyle(getTabButtonStyle(true));
        btnProducts.setStyle(getTabButtonStyle(false));

        if (offreTable == null) {
            offreTable = createOffreTable();
            loadOffres();
        }
        centerContent.getChildren().setAll(offreTable);
    }

    @FXML
    public void showReservations() {
        btnUsers.setStyle(getTabButtonStyle(false));
        btnProducts.setStyle(getTabButtonStyle(true));

        if (reservationTable == null) {
            reservationTable = createReservationTable();
            loadReservations();
        }
        centerContent.getChildren().setAll(reservationTable);
    }

    private TableView<OffreCovoiturage> createOffreTable() {
        TableView<OffreCovoiturage> table = new TableView<>();
        table.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-radius: 10; " +
                        "-fx-border-color: #e0e6f0; " +
                        "-fx-border-width: 1; " +
                        "-fx-padding: 10; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-family: 'Segoe UI'; " +
                        "-fx-text-fill: #1a2b4c; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2); " +
                        "-fx-selection-bar: #e6f3ff; " +
                        "-fx-selection-bar-non-focused: #f0f6ff;"
        );
        table.setMaxWidth(900);
        table.setMaxHeight(500);

        // Style commun pour les colonnes
        String columnStyle = "-fx-alignment: CENTER; " +
                "-fx-background-color: #f8fafc; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #1a2b4c; " +
                "-fx-padding: 8; " +
                "-fx-border-color: #e0e6f0; " +
                "-fx-border-width: 0 1 1 0;";

        // Style pour les cellules
        String cellStyle = "-fx-padding: 8; " +
                "-fx-border-color: #e0e6f0; " +
                "-fx-border-width: 0 1 1 0; " +
                "-fx-alignment: CENTER;";

        TableColumn<OffreCovoiturage, String> departColumn = new TableColumn<>("Départ");
        departColumn.setPrefWidth(100);
        departColumn.setStyle(columnStyle);
        departColumn.setCellValueFactory(new PropertyValueFactory<>("depart"));
        departColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle(cellStyle + "-fx-alignment: CENTER-LEFT;");
            }
        });

        TableColumn<OffreCovoiturage, String> destinationNadColumn = new TableColumn<>("Destination");
        destinationNadColumn.setPrefWidth(100);
        destinationNadColumn.setStyle(columnStyle);
        destinationNadColumn.setCellValueFactory(new PropertyValueFactory<>("destination"));
        destinationNadColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle(cellStyle + "-fx-alignment: CENTER-LEFT;");
            }
        });

        TableColumn<OffreCovoiturage, Integer> matVehiculeColumn = new TableColumn<>("Mat. Véh.");
        matVehiculeColumn.setPrefWidth(80);
        matVehiculeColumn.setStyle(columnStyle);
        matVehiculeColumn.setCellValueFactory(new PropertyValueFactory<>("matVehicule"));
        matVehiculeColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.toString());
                setStyle(cellStyle);
            }
        });

        TableColumn<OffreCovoiturage, Integer> placesDispoColumn = new TableColumn<>("Places");
        placesDispoColumn.setPrefWidth(60);
        placesDispoColumn.setStyle(columnStyle);
        placesDispoColumn.setCellValueFactory(new PropertyValueFactory<>("placesDispo"));
        placesDispoColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.toString());
                setStyle(cellStyle);
            }
        });

        TableColumn<OffreCovoiturage, LocalDateTime> dateColumn = new TableColumn<>("Date");
        dateColumn.setPrefWidth(120);
        dateColumn.setStyle(columnStyle);
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
                setStyle(cellStyle + "-fx-alignment: CENTER-LEFT;");
            }
        });

        TableColumn<OffreCovoiturage, String> statutColumn = new TableColumn<>("Statut");
        statutColumn.setPrefWidth(80);
        statutColumn.setStyle(columnStyle);
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        statutColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle(cellStyle);
            }
        });

        TableColumn<OffreCovoiturage, Float> prixColumn = new TableColumn<>("Prix");
        prixColumn.setPrefWidth(60);
        prixColumn.setStyle(columnStyle);
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        prixColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Float item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%.2f", item));
                setStyle(cellStyle);
            }
        });

        TableColumn<OffreCovoiturage, Void> actionColumn = new TableColumn<>("Action");
        actionColumn.setPrefWidth(150);
        actionColumn.setStyle(columnStyle);
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Éditer");
            private final Button deleteButton = new Button("Supprimer");

            private final HBox buttonsContainer = new HBox(5, editButton, deleteButton);

            {
                buttonsContainer.setStyle("-fx-alignment: CENTER;");
                editButton.setStyle(
                        "-fx-background-color: #3498db; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 12px; " +
                                "-fx-padding: 5 10; " +
                                "-fx-background-radius: 5;"
                );
                editButton.setOnAction(event -> {
                    OffreCovoiturage offre = getTableView().getItems().get(getIndex());
                    editOffre(offre);
                });

                deleteButton.setStyle(
                        "-fx-background-color: #e74c3c; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 12px; " +
                                "-fx-padding: 5 10; " +
                                "-fx-background-radius: 5;"
                );
                deleteButton.setOnAction(event -> {
                    OffreCovoiturage offre = getTableView().getItems().get(getIndex());
                    offreService.delete(offre);
                    loadOffres();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsContainer);
                }
                setStyle(cellStyle);
            }
        });

        table.getColumns().addAll(
                departColumn, destinationNadColumn, matVehiculeColumn, placesDispoColumn,
                dateColumn, statutColumn, prixColumn, actionColumn
        );

        // Style pour les lignes
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(OffreCovoiturage item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    setStyle("-fx-border-color: #e0e6f0; -fx-border-width: 0 0 1 0;");
                }
            }
        });

        return table;
    }

    private void editOffre(OffreCovoiturage offre) {
        Dialog<OffreCovoiturage> dialog = new Dialog<>();
        dialog.setTitle("Modifier l'offre");
        dialog.setHeaderText("Modifier les détails de l'offre");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField departField = new TextField(offre.getDepart());
        TextField destinationField = new TextField(offre.getDestination());
        TextField placesField = new TextField(String.valueOf(offre.getPlacesDispo()));
        TextField prixField = new TextField(String.valueOf(offre.getPrix()));
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(offre.getDate().toLocalDate());

        grid.add(new Label("Départ:"), 0, 0);
        grid.add(departField, 1, 0);
        grid.add(new Label("Destination:"), 0, 1);
        grid.add(destinationField, 1, 1);
        grid.add(new Label("Places disponibles:"), 0, 2);
        grid.add(placesField, 1, 2);
        grid.add(new Label("Prix:"), 0, 3);
        grid.add(prixField, 1, 3);
        grid.add(new Label("Date:"), 0, 4);
        grid.add(datePicker, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Store original values before any modifications
        String originalDepart = offre.getDepart();
        String originalDestination = offre.getDestination();
        int originalPlaces = offre.getPlacesDispo();
        float originalPrix = offre.getPrix();
        LocalDateTime originalDate = offre.getDate();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                offre.setDepart(departField.getText());
                offre.setDestination(destinationField.getText());
                offre.setPlacesDispo(Integer.parseInt(placesField.getText()));
                offre.setPrix(Float.parseFloat(prixField.getText()));
                offre.setDate(datePicker.getValue().atStartOfDay());
                return offre;
            }
            return null;
        });

        Optional<OffreCovoiturage> result = dialog.showAndWait();

        result.ifPresent(updatedOffre -> {
            // Update the offer
            offreService.update(updatedOffre);
            loadOffres();

            // Check which fields have changed
            List<String> changedFields = new ArrayList<>();
            if (!originalDepart.equals(updatedOffre.getDepart())) {
                changedFields.add("Départ");
            }
            if (!originalDestination.equals(updatedOffre.getDestination())) {
                changedFields.add("Destination");
            }
            if (originalPlaces != updatedOffre.getPlacesDispo()) {
                changedFields.add("Places disponibles");
            }
            if (originalPrix != updatedOffre.getPrix()) {
                changedFields.add("Prix");
            }
            if (!originalDate.equals(updatedOffre.getDate())) {
                changedFields.add("Date");
            }

            // Construct notification message
            String notificationMessage;
            if (changedFields.isEmpty()) {
                notificationMessage = "Aucune modification n'a été effectuée pour l'offre";
            } else {
                String fieldsList = String.join(", ", changedFields);
                notificationMessage= "Changement appliqué à l'offre : " + fieldsList;
            }

            // Create a professional notification
            Label notificationLabel = new Label(notificationMessage);
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

            // Slide-in animation
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), notificationLabel);
            slideIn.setFromY(-50);
            slideIn.setToY(0);
            slideIn.play();

            // Add to top of centerContent
            centerContent.getChildren().add(0, notificationLabel);

            // Fade-out after 5 seconds
            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(event -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(1000), notificationLabel);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> centerContent.getChildren().remove(notificationLabel));
                fadeOut.play();
            });
            pause.play();
        });
    }

    private TableView<Reservation> createReservationTable() {
        TableView<Reservation> table = new TableView<>();
        table.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-radius: 10; " +
                        "-fx-border-color: #e0e6f0; " +
                        "-fx-border-width: 1; " +
                        "-fx-padding: 10; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-family: 'Segoe UI'; " +
                        "-fx-text-fill: #1a2b4c; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2); " +
                        "-fx-selection-bar: #e6f3ff; " +
                        "-fx-selection-bar-non-focused: #f0f6ff;"
        );
        table.setMaxWidth(900);
        table.setFixedCellSize(40);

        // Style commun pour les colonnes
        String columnStyle = "-fx-alignment: CENTER; " +
                "-fx-background-color: #f8fafc; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #1a2b4c; " +
                "-fx-padding: 8; " +
                "-fx-border-color: #e0e6f0; " +
                "-fx-border-width: 0 1 1 0;";

        // Style pour les cellules
        String cellStyle = "-fx-padding: 8; " +
                "-fx-border-color: #e0e6f0; " +
                "-fx-border-width: 0 1 1 0; " +
                "-fx-alignment: CENTER;";

        TableColumn<Reservation, String> departColumn = new TableColumn<>("Départ");
        departColumn.setPrefWidth(100);
        departColumn.setStyle(columnStyle);
        departColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOffre().getDepart()));
        departColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle(cellStyle + "-fx-alignment: CENTER-LEFT;");
            }
        });

        TableColumn<Reservation, String> destinationColumn = new TableColumn<>("Destination");
        destinationColumn.setPrefWidth(100);
        destinationColumn.setStyle(columnStyle);
        destinationColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOffre().getDestination()));
        destinationColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle(cellStyle + "-fx-alignment: CENTER-LEFT;");
            }
        });

        TableColumn<Reservation, LocalDateTime> offreDateColumn = new TableColumn<>("Date Offre");
        offreDateColumn.setPrefWidth(120);
        offreDateColumn.setStyle(columnStyle);
        offreDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getOffre().getDate()));
        offreDateColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
                setStyle(cellStyle + "-fx-alignment: CENTER-LEFT;");
            }
        });

        TableColumn<Reservation, LocalDateTime> createdAtColumn = new TableColumn<>("Date Rés.");
        createdAtColumn.setPrefWidth(120);
        createdAtColumn.setStyle(columnStyle);
        createdAtColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getCreatedAt()));
        createdAtColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
                setStyle(cellStyle + "-fx-alignment: CENTER-LEFT;");
            }
        });

        TableColumn<Reservation, String> statutColumn = new TableColumn<>("Statut");
        statutColumn.setPrefWidth(80);
        statutColumn.setStyle(columnStyle);
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        statutColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle(cellStyle);
            }
        });

        TableColumn<Reservation, Void> actionColumn = new TableColumn<>("Action");
        actionColumn.setPrefWidth(150);
        actionColumn.setStyle(columnStyle);
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Éditer");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox buttonsContainer = new HBox(5, editButton, deleteButton);

            {
                buttonsContainer.setStyle("-fx-alignment: CENTER;");
                editButton.setStyle(
                        "-fx-background-color: #3498db; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 12px; " +
                                "-fx-padding: 5 10; " +
                                "-fx-background-radius: 5;"
                );
                editButton.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    editReservation(reservation);
                });

                deleteButton.setStyle(
                        "-fx-background-color: #e74c3c; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 12px; " +
                                "-fx-padding: 5 10; " +
                                "-fx-background-radius: 5;"
                );
                deleteButton.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    reservationService.delete(reservation);
                    loadReservations();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsContainer);
                }
                setStyle(cellStyle);
            }
        });

        table.getColumns().addAll(
                departColumn, destinationColumn, offreDateColumn,
                createdAtColumn, statutColumn, actionColumn
        );

        // Style pour les lignes et empêcher l'affichage des lignes vides
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Reservation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                    setPrefHeight(0);
                    setVisible(false);
                } else {
                    setStyle("-fx-border-color: #e0e6f0; -fx-border-width: 0 0 1 0;");
                    setPrefHeight(table.getFixedCellSize());
                    setVisible(true);
                }
            }
        });

        // Dynamically adjust table height based on number of items
        table.itemsProperty().addListener((obs, oldItems, newItems) -> {
            double rowHeight = table.getFixedCellSize();
            double headerHeight = 40;
            double totalHeight = newItems.size() * rowHeight + headerHeight;
            table.setPrefHeight(Math.min(totalHeight, 500));
        });

        return table;
    }

    private void editReservation(Reservation reservation) {
        Dialog<Reservation> dialog = new Dialog<>();
        dialog.setTitle("Modifier la réservation");
        dialog.setHeaderText("Modifier les détails de la réservation");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField statutField = new TextField(reservation.getStatut());
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(reservation.getCreatedAt().toLocalDate());

        grid.add(new Label("Statut:"), 0, 0);
        grid.add(statutField, 1, 0);
        grid.add(new Label("Date Réservation:"), 0, 1);
        grid.add(datePicker, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Store original values before any modifications
        String originalStatut = reservation.getStatut();
        LocalDateTime originalCreatedAt = reservation.getCreatedAt();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                reservation.setStatut(statutField.getText());
                reservation.setCreatedAt(datePicker.getValue().atStartOfDay());
                return reservation;
            }
            return null;
        });

        Optional<Reservation> result = dialog.showAndWait();

        result.ifPresent(updatedReservation -> {
            // Update the reservation
            reservationService.update(updatedReservation);
            loadReservations();

            // Check which fields have changed
            List<String> changedFields = new ArrayList<>();
            if (!originalStatut.equals(updatedReservation.getStatut())) {
                changedFields.add("Statut");
            }
            if (!originalCreatedAt.equals(updatedReservation.getCreatedAt())) {
                changedFields.add("Date Réservation");
            }

            // Construct notification message
            String notificationMessage;
            if (changedFields.isEmpty()) {
                notificationMessage = "Aucune modification n'a été effectuée pour la réservation";
            } else {
                String fieldsList = String.join(", ", changedFields);
                notificationMessage = "Changement appliqué à la réservation : " + fieldsList;
            }

            // Create a professional notification
            Label notificationLabel = new Label(notificationMessage);
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

            // Slide-in animation
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), notificationLabel);
            slideIn.setFromY(-50);
            slideIn.setToY(0);
            slideIn.play();

            // Add to top of centerContent
            centerContent.getChildren().add(0, notificationLabel);

            // Fade-out after 5 seconds
            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(event -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(1000), notificationLabel);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> centerContent.getChildren().remove(notificationLabel));
                fadeOut.play();
            });
            pause.play();
        });
    }

    private void loadOffres() {
        ObservableList<OffreCovoiturage> offres = FXCollections.observableArrayList(offreService.getAll());
        offreTable.setItems(offres);
    }

    private void loadScene(String fxmlPath, Node node) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root, 1500, 765);
            Stage stage = (Stage) node.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(fxmlPath.contains("login") ? "Covoiturage Login" : fxmlPath.contains("allOffres") ? "All Offres" : "Covoiturage");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error redirecting to " + fxmlPath + ": " + e.getMessage());
        }
    }

    private void loadReservations() {
        ObservableList<Reservation> reservations = FXCollections.observableArrayList(reservationService.getAll());
        reservationTable.setItems(reservations);
    }
    @FXML
    private void handleMesVehicules(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackVehiculeView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1500, 765);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de BackVehiculeView.fxml:");
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la vue des véhicules: " + e.getMessage());
        }
    }
}