package controllers;

import tn.esprit.models.ProposerEvent;
import tn.esprit.services.ServicePropositionEvent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.ZoneId;
import java.util.Date;
import java.io.File;
import java.time.LocalDate;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.scene.control.DateCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

public class AjouterEventController {

    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private ComboBox<String> typeCombo;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TextField priceField;
    @FXML
    private TextField imageUrlField;
    @FXML
    private TextField locationField;
    @FXML
    private VBox weatherContainer;
    @FXML
    private HBox weatherBox;
    @FXML
    private ImageView weatherIcon;
    @FXML
    private Label weatherDescription;
    @FXML
    private Label weatherTemp;
    @FXML
    private Label weatherLoading;
    private boolean darkMode = false;

    @FXML
    private VBox mainBox;
    @FXML
    private ToggleButton darkModeToggle;

    private static final String API_KEY = "4e7d3a52f736efaa4cfcc0dac8b1d495";
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric&lang=fr";

    @FXML
    void handleAddEvent(ActionEvent event) {
        try {
            // Validation des champs
            if (titleField.getText().isEmpty() || descriptionField.getText().isEmpty()
                    || startDatePicker.getValue() == null || endDatePicker.getValue() == null
                    || typeCombo.getValue() == null || priceField.getText().isEmpty()
                    || locationField.getText().isEmpty()) {
                throw new IllegalArgumentException("Tous les champs doivent être remplis.");
            }

            if (startDatePicker.getValue().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("La date de début ne peut pas être dans le passé.");
            }
            if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
                throw new IllegalArgumentException("La date de fin doit être après la date de début.");
            }

            // Validation du prix
            double price;
            try {
                price = Double.parseDouble(priceField.getText());
                if (price < 0) {
                    throw new IllegalArgumentException("Le prix doit être positif.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Le prix doit être un nombre valide.");
            }

            ProposerEvent proposerEvent = new ProposerEvent(
                    titleField.getText(),
                    descriptionField.getText(),
                    locationField.getText(),
                    Date.from(startDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    Date.from(endDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    typeCombo.getValue(),
                    price,
                    1,
                    imageUrlField.getText()
            );

            // Insertion dans la base
            ServicePropositionEvent service = new ServicePropositionEvent();
            service.add(proposerEvent);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement ajouté avec succès !");
            clearFields();

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBrowseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");

        // Filtre pour les fichiers images
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            imageUrlField.setText(selectedFile.getAbsolutePath());
        }
    }
    private void clearFields() {
        titleField.clear();
        descriptionField.clear();
        locationField.clear();
        typeCombo.getSelectionModel().clearSelection();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        priceField.clear();
        imageUrlField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void initialize() {
        // Initialisation du ComboBox
        typeCombo.getItems().addAll();

        startDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        endDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate startDate = startDatePicker.getValue() != null ?
                        startDatePicker.getValue() : LocalDate.now();
                setDisable(empty || date.isBefore(startDate));
            }
        });

        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                endDatePicker.setValue(newVal.plusDays(1));
                fetchWeatherData(locationField.getText(), newVal);
            }
        });
        locationField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (startDatePicker.getValue() != null && !newVal.isEmpty()) {
                fetchWeatherData(newVal, startDatePicker.getValue());
            }
        });
        darkModeToggle.setOnAction(event -> {
            darkMode = darkModeToggle.isSelected();
            applyDarkMode();
        });
    }
    private void applyDarkMode() {
        // Obtenir le conteneur racine (AnchorPane)
        AnchorPane root = (AnchorPane) mainBox.getScene().getRoot();

        if (darkMode) {
            // Style dark mode pour l'arrière-plan principal
            root.setStyle("-fx-background-color: #2b2b2b;");

            // Style dark mode pour le conteneur principal
            mainBox.setStyle("""
            -fx-background-color: #3c3f41;
            -fx-border-color: #555555;
            -fx-background-radius: 25;
            -fx-border-radius: 25;
            -fx-border-width: 1;
            -fx-padding: 30;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0.3, 0, 5);
            -fx-font-family: 'Segoe UI', sans-serif;
        """);

            // Style pour la barre de navigation
            HBox topBox = (HBox) root.lookup("#navBar");
            if (topBox != null) {
                topBox.setStyle("""
                -fx-background-color: linear-gradient(to right, #3c3f41, #555555);
                -fx-padding: 15;
                -fx-border-width: 0 0 2 0;
                -fx-border-color: #555555;
                -fx-background-radius: 12;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4);
            """);

                // Style pour les éléments dans la barre de navigation
                for (Node node : topBox.getChildren()) {
                    if (node instanceof Label) {
                        node.setStyle("-fx-text-fill: white;");
                    }
                    if (node instanceof MenuButton) {
                        node.setStyle("""
                        -fx-background-color: #555555;
                        -fx-text-fill: white;
                        -fx-font-size: 17px;
                        -fx-font-weight: bold;
                        -fx-background-radius: 10;
                        -fx-padding: 10 18;
                    """);
                    }
                }
            }

            // Style pour le bouton de soumission
            Button submitButton = (Button) mainBox.lookup("#submitButton");
            if (submitButton != null) {
                submitButton.setStyle("""
                -fx-background-radius: 25;
                -fx-text-fill: white;
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                -fx-padding: 10 25;
                -fx-background-color: #555555;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0.3, 0, 4);
            """);
            }

            // Style pour les champs de formulaire
            String fieldStyle = """
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-border-color: #555555;
            -fx-border-width: 1.5;
            -fx-padding: 8 12;
            -fx-background-color: #3c3f41;
            -fx-text-fill: white;
        """;

            // Appliquer le style à tous les éléments de formulaire
            for (Node node : mainBox.lookupAll(".text-field, .text-area, .combo-box, .date-picker")) {
                node.setStyle(fieldStyle);
            }

            // Appliquer le style blanc à tous les labels du formulaire
            for (Node node : mainBox.lookupAll(".label")) {
                if (node instanceof Label) {
                    ((Label) node).setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                }
            }

            // Style spécifique pour les prévisions météo
            weatherDescription.setStyle("-fx-font-size: 14px; -fx-text-fill: #a8dadc;");
            weatherTemp.setStyle("-fx-font-size: 12px; -fx-text-fill: #a8dadc;");

            darkModeToggle.setText("☀️ Light Mode");
        } else {
            // Style light mode
            root.setStyle("-fx-background-color: white;");

            mainBox.setStyle("""
            -fx-background-color: linear-gradient(to bottom, #c2e4fb, #eff6ff);
            -fx-background-radius: 25;
            -fx-border-radius: 25;
            -fx-border-color: linear-gradient(to right, #001f4d, #003366);
            -fx-border-width: 1;
            -fx-padding: 30;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0.3, 0, 5);
            -fx-font-family: 'Segoe UI', sans-serif;
        """);

            // Rétablir le style original de la barre de navigation
            HBox topBox = (HBox) root.lookup("#navBar");
            if (topBox != null) {
                topBox.setStyle("""
                -fx-background-color: linear-gradient(to right, #c9eaff, #eaf6ff);
                -fx-padding: 15;
                -fx-border-width: 0 0 2 0;
                -fx-border-color: #b3d1f5;
                -fx-background-radius: 12;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);
            """);

                for (Node node : topBox.getChildren()) {
                    if (node instanceof Label) {
                        node.setStyle("-fx-text-fill: #002244;");
                    }
                    if (node instanceof MenuButton) {
                        node.setStyle("""
                        -fx-background-color: rgba(255,255,255,0.6);
                        -fx-text-fill: #002557;
                        -fx-font-size: 17px;
                        -fx-font-weight: bold;
                        -fx-background-radius: 10;
                        -fx-padding: 10 18;
                    """);
                    }
                }
            }

            // Rétablir le style original du bouton
            Button submitButton = (Button) mainBox.lookup("#submitButton");
            if (submitButton != null) {
                submitButton.setStyle("""
                -fx-background-radius: 25;
                -fx-text-fill: white;
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                -fx-padding: 10 25;
                -fx-background-color: linear-gradient(to right, #001f4d, #003366);
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0.3, 0, 4);
            """);
            }

            // Rétablir les styles par défaut des champs
            String fieldStyle = """
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-border-color: #003366;
            -fx-border-width: 1.5;
            -fx-padding: 8 12;
            -fx-background-color: white;
            -fx-text-fill: black;
        """;

            for (Node node : mainBox.lookupAll(".text-field, .text-area, .combo-box, .date-picker")) {
                node.setStyle(fieldStyle);
            }

            // Rétablir le style par défaut des labels
            for (Node node : mainBox.lookupAll(".label")) {
                if (node instanceof Label) {
                    ((Label) node).setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
                }
            }

            weatherDescription.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
            weatherTemp.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

            darkModeToggle.setText("🌙 Dark Mode");
        }
    }
    private void fetchWeatherData(String location, LocalDate date) {
        Platform.runLater(() -> {
            weatherLoading.setText("Chargement...");
            weatherLoading.setVisible(true);
            weatherIcon.setVisible(false);
        });
        if (location == null || location.isEmpty()) {
            weatherContainer.setVisible(false);
            return;
        }

        new Thread(() -> {
            try {
                // Vérifier si la date est dans le futur
                if (date.isAfter(LocalDate.now())) {
                    Platform.runLater(() -> {
                        weatherContainer.setVisible(true);
                        weatherDescription.setText("Prévisions non disponibles (API gratuite)");
                        weatherTemp.setText("--°C");
                        weatherIcon.setImage(new Image(getClass().getResourceAsStream("/images/weather_unknown.png")));
                        return;
                    });
                }

                String urlString = String.format(API_URL, location, API_KEY);
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    Platform.runLater(() -> {
                        weatherContainer.setVisible(false);
                        showAlert(Alert.AlertType.WARNING, "Météo",
                                "Impossible d'obtenir les données météo pour " + location);
                    });
                    return;
                }

                Scanner scanner = new Scanner(url.openStream());
                String response = scanner.useDelimiter("\\Z").next();
                scanner.close();

                JSONObject json = new JSONObject(response);
                JSONObject main = json.getJSONObject("main");
                JSONObject weather = json.getJSONArray("weather").getJSONObject(0);

                double temp = main.getDouble("temp");
                String description = weather.getString("description");
                String iconCode = weather.getString("icon");

                Platform.runLater(() -> {
                    updateWeatherUI(temp, description, iconCode);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    weatherContainer.setVisible(true);
                    weatherDescription.setText("Données météo indisponibles");
                    weatherTemp.setText("--°C");
                    weatherIcon.setImage(new Image(getClass().getResourceAsStream("/images/weather_unknown.png")));
                    System.err.println("Erreur API météo: " + e.getMessage());
                });
            }
        }).start();
        Platform.runLater(() -> {
            weatherLoading.setVisible(false);
            weatherIcon.setVisible(true);
        });
    }

    private void updateWeatherUI(double temp, String description, String iconCode) {
        weatherContainer.setVisible(true);

        if (description == null || iconCode == null) {
            weatherDescription.setText("Données météo indisponibles");
            weatherTemp.setText("--°C");
            weatherIcon.setImage(new Image(getClass().getResourceAsStream("/images/weather_unknown.png")));
            return;
        }

        weatherDescription.setText(description.substring(0, 1).toUpperCase() + description.substring(1));
        weatherTemp.setText(String.format("%.1f°C", temp));

        String iconUrl = "http://openweathermap.org/img/wn/" + iconCode + "@2x.png";
        try {
            Image image = new Image(iconUrl, true);
            weatherIcon.setImage(image);
        } catch (Exception e) {
            weatherIcon.setImage(new Image(getClass().getResourceAsStream("/images/weather_unknown.png")));
            System.err.println("Erreur de chargement de l'icône météo: " + e.getMessage());
        }
    }
}