package controllers;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import tn.esprit.models.PDFGenerator;
import tn.esprit.models.Vehicule;
import tn.esprit.services.ServiceVehicule;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class VehiculeController implements Initializable {

    @FXML
    private FlowPane vehiculesContainer;
    @FXML
    private HBox paginationContainer;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private Label pageInfo;
    @FXML
    private ComboBox<String> triComboBox;
    @FXML
    private TextField searchField;
    @FXML
    private ToggleButton darkModeToggle;
    @FXML
    private BorderPane mainContainer;


    private final ServiceVehicule vehiculeService = new ServiceVehicule();
    private final Image defaultImage = new Image(getClass().getResourceAsStream("/images/pic5.jpg"));
    private List<Vehicule> allVehicules;
    private static final int ITEMS_PER_PAGE = 3;
    private int currentPage = 0;
    private int totalPages = 0;
    private boolean darkMode = false;

    // Options de tri
    private static final String TRI_PAR_DEFAUT = "Trier par";
    private static final String TRI_PRIX_HEURE_CROISSANT = "Prix/heure (↑)";
    private static final String TRI_PRIX_HEURE_DECROISSANT = "Prix/heure (↓)";
    private static final String TRI_PRIX_JOUR_CROISSANT = "Prix/jour (↑)";
    private static final String TRI_PRIX_JOUR_DECROISSANT = "Prix/jour (↓)";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        allVehicules = vehiculeService.getAll();

        // Configuration du ComboBox de tri
        triComboBox.getItems().addAll(
                TRI_PAR_DEFAUT,
                TRI_PRIX_HEURE_CROISSANT,
                TRI_PRIX_HEURE_DECROISSANT,
                TRI_PRIX_JOUR_CROISSANT,
                TRI_PRIX_JOUR_DECROISSANT
        );
        triComboBox.setValue(TRI_PAR_DEFAUT);

        triComboBox.setOnAction(event -> {
            trierVehicules();
            currentPage = 0;
            setupPagination();
        });

        setupPagination();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentPage = 0;
            setupPagination();
        });
        darkModeToggle.setOnAction(event -> {
            darkMode = darkModeToggle.isSelected(); // met à jour la variable selon l’état du bouton
            applyDarkMode(); // applique le style
        });


    }
    private void applyDarkMode() {
        if (darkMode) {
            // Style dark mode
            mainContainer.setStyle("-fx-background-color: #2b2b2b;");
            pageInfo.setStyle("-fx-text-fill: white;");
            triComboBox.setStyle("-fx-background-color: #3c3f41; -fx-text-fill: white;");
            searchField.setStyle("-fx-background-color: #3c3f41; -fx-text-fill: white;");
            darkModeToggle.setText("☀️ Light Mode");
        } else {
            // Style light mode
            mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #e0f7fa, #ffffff);");
            pageInfo.setStyle("-fx-text-fill: #0b2e4a;");
            triComboBox.setStyle("-fx-background-color: white; -fx-text-fill: black;");
            searchField.setStyle("-fx-background-color: white; -fx-text-fill: black;");
            darkModeToggle.setText("🌙 Dark Mode");
        }

        // Mettre à jour les cartes existantes
        currentPage = 0;
        setupPagination();
    }

    private void trierVehicules() {
        String choixTri = triComboBox.getValue();

        if (choixTri == null || choixTri.equals(TRI_PAR_DEFAUT)) {
            allVehicules = vehiculeService.getAll();
            return;
        }

        switch (choixTri) {
            case TRI_PRIX_HEURE_CROISSANT:
                allVehicules.sort(Comparator.comparing(Vehicule::getPrix_par_heure));
                break;

            case TRI_PRIX_HEURE_DECROISSANT:
                allVehicules.sort(Comparator.comparing(Vehicule::getPrix_par_heure).reversed());
                break;

            case TRI_PRIX_JOUR_CROISSANT:
                allVehicules.sort(Comparator.comparing(Vehicule::getPrix_par_jour));
                break;

            case TRI_PRIX_JOUR_DECROISSANT:
                allVehicules.sort(Comparator.comparing(Vehicule::getPrix_par_jour).reversed());
                break;
        }
    }

    private void setupPagination() {
        if (allVehicules == null || allVehicules.isEmpty()) {
            Label noData = new Label("Aucun véhicule disponible pour le moment.");
            noData.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
            vehiculesContainer.getChildren().add(noData);
            paginationContainer.setVisible(false);
            return;
        }

        totalPages = (int) Math.ceil((double) allVehicules.size() / ITEMS_PER_PAGE);
        updatePage();
    }

    private void updatePage() {
        vehiculesContainer.getChildren().clear();

        String searchText = searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        List<Vehicule> filteredList = allVehicules.stream()
                .filter(v -> v.getModele().toLowerCase().contains(searchText) ||
                        v.getType_vehicule().toLowerCase().contains(searchText))
                .toList();

        totalPages = (int) Math.ceil((double) filteredList.size() / ITEMS_PER_PAGE);

        int fromIndex = currentPage * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredList.size());

        if (filteredList.isEmpty()) {
            Label noResults = new Label("Aucun véhicule trouvé.");
            noResults.setStyle(darkMode ?
                    "-fx-font-size: 16px; -fx-text-fill: #cccccc;" :
                    "-fx-font-size: 16px; -fx-text-fill: gray;");
            vehiculesContainer.getChildren().add(noResults);
        } else {
            for (int i = fromIndex; i < toIndex; i++) {
                VBox card = createVehiculeCard(filteredList.get(i));
                applyEntranceAnimation(card, (i % ITEMS_PER_PAGE) * 100);
                vehiculesContainer.getChildren().add(card);
            }
        }

        pageInfo.setText(String.format("Page %d/%d", totalPages == 0 ? 0 : currentPage + 1, totalPages));
        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(currentPage >= totalPages - 1 || totalPages == 0);
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

    private VBox createVehiculeCard(Vehicule vehicule) {
        VBox card = new VBox(8);
        card.setPrefSize(250, 300);
        card.setMinSize(280, 350);
        card.setMaxSize(280, 350);

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
                -fx-background-color: linear-gradient(to bottom right, #ffffff, #e0f7fa);
                -fx-border-color: #00acc1;
                -fx-border-width: 1.5;
                -fx-border-radius: 15;
                -fx-background-radius: 15;
                -fx-padding: 15;
            """);
        }

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.2));
        shadow.setRadius(10);
        shadow.setOffsetX(4);
        shadow.setOffsetY(4);
        card.setEffect(shadow);

        ImageView imageView = new ImageView(loadImage(vehicule.getImageUrl()));
        imageView.setFitWidth(220);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");

        Label title = new Label(vehicule.getType_vehicule() + " " + vehicule.getModele());
        title.setStyle(darkMode ?
                "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;" :
                """
                -fx-font-size: 18px;
                -fx-font-weight: bold;
                -fx-text-fill: #005b4f;
                -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.7), 2, 0.5, 0, 1);
                """);

        Label prixLabel = new Label("💸 Prix: " + vehicule.getPrix_par_heure() + " DT/heure - " +
                vehicule.getPrix_par_jour() + " DT/jour");
        prixLabel.setStyle(darkMode ?
                "-fx-font-size: 14px; -fx-text-fill: #a8dadc; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 1, 0.3, 0, 1);" :
                """
                -fx-font-size: 14px;
                -fx-text-fill: #00695c;
                -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.7), 1, 0.3, 0, 1);
                """);

        Label dispoLabel = new Label("⏱️ " + (vehicule.getDisponibilite() != null ?
                vehicule.getDisponibilite() : "Disponible"));
        dispoLabel.setStyle(darkMode ?
                "-fx-font-size: 14px; -fx-text-fill: #a8dadc; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 1, 0.3, 0, 1);" :
                """
                -fx-font-size: 14px;
                -fx-text-fill: #00695c;
                -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.7), 1, 0.3, 0, 1);
                """);

        Label lieuLabel = new Label("📍 " + vehicule.getLieu_retrait());
        lieuLabel.setStyle(darkMode ?
                "-fx-font-size: 14px; -fx-text-fill: #a8dadc; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 1, 0.3, 0, 1);" :
                """
                -fx-font-size: 14px;
                -fx-text-fill: #00695c;
                -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.7), 1, 0.3, 0, 1);
                """);

        // Boutons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button reserverButton = new Button("Réserver");
        reserverButton.setStyle("""
            -fx-background-color: #005288;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-font-weight: bold;
            -fx-padding: 6 12;
        """);
        reserverButton.setOnAction(event -> handleReservation(vehicule));

        Button pdfButton = new Button("PDF");
        pdfButton.setStyle("""
            -fx-background-color: #2196F3;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-font-weight: bold;
            -fx-padding: 6 12;
        """);
        pdfButton.setOnAction(event -> generateVehiculePDF(vehicule));

        buttonsBox.getChildren().addAll(reserverButton, pdfButton);
        card.getChildren().addAll(imageView, title, prixLabel, dispoLabel, lieuLabel, buttonsBox);

        // Animations
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

        return card;
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

    private void handleReservation(Vehicule vehicule) {
        try {
            // Charger le FXML directement depuis le classpath
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReserverVehicule.fxml"));
            Parent root = loader.load();

            ReserverVehiculeController controller = loader.getController();
            controller.setVehicule(vehicule);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Réservation - " + vehicule.getModele());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Rafraîchir la liste après réservation
            allVehicules = vehiculeService.getAll();
            setupPagination();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'interface de réservation: " + e.getMessage());

            // Afficher le chemin recherché pour le débogage
            URL resourceUrl = getClass().getResource("/ReserverVehicule.fxml");
            System.err.println("Chemin recherché pour le FXML: " +
                    (resourceUrl != null ? resourceUrl.toString() : "null"));
        }
    }
    @FXML
    private void handleAjouterAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterVehicule.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un véhicule");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            allVehicules = vehiculeService.getAll();
            setupPagination();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'interface d'ajout");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Erreur") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }    private void generateVehiculePDF(Vehicule vehicule) {
        try {
            PDFGenerator.generateVehiculePDF(vehicule);
            showAlert("Succès", "PDF généré avec succès pour le véhicule");
        } catch (Exception e) {
            showAlert("Erreur", "Échec de la génération du PDF: " + e.getMessage());
        }
    }
    @FXML
    private void handleEventsAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Event.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1500, 765);
            stage.setScene(scene);
            stage.setTitle("Événements");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Vous pouvez ajouter une alerte en cas d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de charger la page des événements.");
            alert.showAndWait();
        }
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
    void handleMesOffres(ActionEvent event) {
        loadScene("/MesOffres.fxml", event);
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
    private void loadScene(String fxmlPath, ActionEvent event) {
        try {

            // Load the new FXML
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            // Get the current stage from the event source
            Stage stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
            // Create a new scene with the loaded root and set it to the stage
            Scene scene = new Scene(root, 1500, 765);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}