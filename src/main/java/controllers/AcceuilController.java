package controllers;
import javafx.scene.layout.*;

import com.sun.javafx.stage.EmbeddedWindow;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Separator;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.models.OffreCovoiturage;
import tn.esprit.models.Reservation;
import tn.esprit.services.ServiceOffreCovoiturage;
import tn.esprit.services.ServiceReservation;
import javafx.scene.shape.Circle;
import javafx.scene.Group;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AcceuilController {


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
    private FlowPane offresContainer;

    @FXML
    private Button ajouterButton;

    @FXML
    private TextField searchBar;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private Button loadMoreButton;

    private List<OffreCovoiturage> allOffres; // Store all offers
    private int displayedItems = 0; // Track total number of displayed items
    private final int initialItems = 8; // 3 rows (~8 cards)
    private final int itemsPerLoad = 6; // 2 rows (~6 cards)


    // Méthode utilitaire pour afficher des alertes
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleViewProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Profile.fxml"));
            Parent root = loader.load();

            Stage profileStage = new Stage();
            profileStage.setTitle("Mon Profil");
            profileStage.setScene(new Scene(root));

            // Correction ici - utiliser la fenêtre de l'avatarMenu comme parent
            profileStage.initModality(Modality.WINDOW_MODAL);
            profileStage.initOwner(avatarMenu.getScene().getWindow()); // Utilisez avatarMenu au lieu de event.getSource()

            profileStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le profil", Alert.AlertType.ERROR);
        }
    }
    @FXML
    private MenuButton avatarMenu; // Ajout de la déclaration manquante

    @FXML
    public void initialize() {
        Integer userId = SessionManager.getInstance().getUserId();
        if (userId == null) {
            System.out.println("❌ No user ID in session, redirecting to login");

            return;
        }
        else{
            System.out.println(userId);
        }
        searchBar.textProperty().addListener((obs, oldValue, newValue) -> renderFilteredOffres());
        datePicker.valueProperty().addListener((obs, oldValue, newValue) -> renderFilteredOffres());
        sortComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> renderFilteredOffres());
        loadOffres();
    }

    @FXML
    void handleAjouterEvent(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Event.fxml"));
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
    void handleMesOffres(ActionEvent event) {
        loadScene("/MesOffres.fxml", event);
    }
    @FXML
    private void openChatLink(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://chatbot.getmindpal.com/coivoimob"));
        } catch (Exception e) {
            e.printStackTrace();
            // Show error message if the link couldn't be opened
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not open the link. Please check your internet connection.");
            alert.showAndWait();
        }
    }
    @FXML
    void handleAutresOffres(ActionEvent event) {
        loadScene("/Acceuil.fxml", event);
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

    private void renderFilteredOffres() {
        String searchText = searchBar.getText() != null ? searchBar.getText().trim() : null;
        LocalDate selectedDate = datePicker.getValue();
        String sortOption = sortComboBox.getValue();
        ServiceOffreCovoiturage service = new ServiceOffreCovoiturage();
        allOffres = service.getFilteredOffres(searchText, selectedDate, sortOption);
        displayedItems = 0; // Reset pagination on filter change
        renderOffresPage();
    }

    public void loadOffres() {
        ServiceOffreCovoiturage service = new ServiceOffreCovoiturage();
        allOffres = service.getAutresOffresByConducteurId( SessionManager.getInstance().getUserId());
        displayedItems = 0;
        renderOffresPage();
    }

    @FXML
    private void loadMore() {
        displayedItems += itemsPerLoad; // Increment by 6 for next load
        renderOffresPage();
    }

    private void renderOffresPage() {
        offresContainer.getChildren().clear();
        if (allOffres == null) {
            allOffres = new ArrayList<>();
        }
        if (allOffres.isEmpty()) {
            Label noDataLabel = new Label("Aucune offre disponible.");
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
            offresContainer.getChildren().add(noDataLabel);
            loadMoreButton.setVisible(false);
            return;
        }

        // Determine how many items to display
        int itemsToShow = displayedItems == 0 ? initialItems : displayedItems + itemsPerLoad;
        int endIndex = Math.min(itemsToShow, allOffres.size());

        // Render the offers up to endIndex
        for (int i = 0; i < endIndex; i++) {
            VBox card = createOffreCard(allOffres.get(i));
            offresContainer.getChildren().add(card);
        }

        // Show or hide the Load More button
        loadMoreButton.setVisible(endIndex < allOffres.size());
    }

    private VBox createOffreCard(OffreCovoiturage offre) {
        // Main card container
        VBox card = new VBox(20);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(340); // Slightly wider for premium feel
        card.setPrefHeight(360); // Taller to accommodate enhanced content
        card.setMaxWidth(340); // Ensure consistent sizing

        // Premium gradient background with subtle glow
        Stop[] stops = new Stop[]{
                new Stop(0, Color.web("#f8fafc")),
                new Stop(1, Color.web("#e0e7ff"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        card.setBackground(new Background(
                new BackgroundFill(gradient, new CornerRadii(30), null)
        ));
        card.setStyle(
                "-fx-border-color: linear-gradient(to bottom, #93c5fd, #3b82f6);" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 30;" +
                        "-fx-background-radius: 30;" +
                        "-fx-padding: 25;" +
                        "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.3), 20, 0.2, 5, 5);" +
                        "-fx-cursor: hand;"
        );

        // Smooth hover animation
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), card);
        card.setOnMouseEntered(e -> {
            scaleTransition.setToX(1.03);
            scaleTransition.setToY(1.03);
            scaleTransition.playFromStart();
            card.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #ffffff, #dbeafe);" +
                            "-fx-border-color: linear-gradient(to bottom, #60a5fa, #1d4ed8);" +
                            "-fx-border-width: 1.5;" +
                            "-fx-border-radius: 30;" +
                            "-fx-background-radius: 30;" +
                            "-fx-padding: 25;" +
                            "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.5), 25, 0.3, 6, 6);" +
                            "-fx-cursor: hand;"
            );
        });

        card.setOnMouseExited(e -> {
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.playFromStart();
            card.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #f8fafc, #e0e7ff);" +
                            "-fx-border-color: linear-gradient(to bottom, #93c5fd, #3b82f6);" +
                            "-fx-border-width: 1.5;" +
                            "-fx-border-radius: 30;" +
                            "-fx-background-radius: 30;" +
                            "-fx-padding: 25;" +
                            "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.3), 20, 0.2, 5, 5);" +
                            "-fx-cursor: hand;"
            );
        });


        // Profile section wit
        //
        // h circular avatar and elegant typography
        HBox profileBox = new HBox(15);
        profileBox.setAlignment(Pos.CENTER_LEFT);
        profileBox.setStyle("-fx-padding: 0 0 15 0;");

        // Profile image with premium border
        double imageSize = 70;
        double radius = imageSize / 2;
        StackPane profileImageContainer = new StackPane();
        profileImageContainer.setMinSize(imageSize, imageSize);
        profileImageContainer.setPrefSize(imageSize, imageSize);

        ImageView profileImage;
        try {
            Image image = new Image("file:src/main/resources/img/imgprofile.jpg", imageSize, imageSize, true, true);
            if (image.isError()) {
                image = new Image("/img/imgprofile.jpg", imageSize, imageSize, true, true);
            }
            profileImage = new ImageView(image);
        } catch (Exception e) {
            profileImage = new ImageView(new Image("/img/imgprofile.png", imageSize, imageSize, true, true));
        }

        profileImage.setFitWidth(imageSize);
        profileImage.setFitHeight(imageSize);
        profileImage.setPreserveRatio(true);
        profileImage.setSmooth(true);

        Circle clip = new Circle(radius, radius, radius);
        profileImage.setClip(clip);

        Circle border = new Circle(radius);
        border.setFill(null);
        border.setStroke(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#60a5fa")), new Stop(1, Color.web("#1d4ed8"))));
        border.setStrokeWidth(3);

        profileImageContainer.getChildren().addAll(profileImage, border);
        profileImageContainer.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0, 2, 2);");

        // Profile name and role
        VBox nameContainer = new VBox(4);
        Label nameLabel = new Label("YAACOUB EYA");
        nameLabel.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-font-size: 24px;" +
                        "-fx-text-fill: #1e3a8a;" +
                        "-fx-font-family: 'Roboto', 'Segoe UI', Arial, sans-serif;"
        );

        Label roleLabel = new Label("Conducteur");
        roleLabel.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-text-fill: #64748b;" +
                        "-fx-font-family: 'Roboto', 'Segoe UI', Arial, sans-serif;"
        );

        nameContainer.getChildren().addAll(nameLabel, roleLabel);
        profileBox.getChildren().addAll(profileImageContainer, nameContainer);

        // Separator with gradient
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: linear-gradient(to right, #93c5fd, #3b82f6); -fx-pref-height: 2;");

        // Trip details with premium icons and layout
        VBox tripDetails = new VBox(15);
        tripDetails.setStyle("-fx-padding: 15 0;");

        // Route information
        HBox trajetBox = new HBox(12);
        trajetBox.setAlignment(Pos.CENTER_LEFT);

        Label routeIcon = new Label("🛣️");
        routeIcon.setStyle("-fx-font-size: 22px; -fx-text-fill: #3b82f6;");

        Label trajetLabel = new Label(offre.getDepart() + " → " + offre.getDestination());
        trajetLabel.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #1e293b;" +
                        "-fx-font-family: 'Roboto', 'Segoe UI', Arial, sans-serif;"
        );

        trajetBox.getChildren().addAll(routeIcon, trajetLabel);

        // Date and price in a premium grid-like layout
        HBox infoBox = new HBox(25);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setStyle("-fx-padding: 10 0 0 0;");

        // Date section
        VBox dateBox = new VBox(6);
        dateBox.setAlignment(Pos.CENTER_LEFT);

        Label dateIcon = new Label("📅");
        dateIcon.setStyle("-fx-font-size: 20px; -fx-text-fill: #4b5563;");

        Label dateLabel = new Label(offre.getDate().toString());
        dateLabel.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #4b5563;" +
                        "-fx-font-family: 'Roboto', 'Segoe UI', Arial, sans-serif;"
        );

        Label dateSubtitle = new Label("Date de départ");
        dateSubtitle.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #6b7280;" +
                        "-fx-font-family: 'Roboto', 'Segoe UI', Arial, sans-serif;"
        );

        dateBox.getChildren().addAll(dateIcon, dateLabel, dateSubtitle);

        // Price section with premium styling
        VBox priceBox = new VBox(6);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        Label priceIcon = new Label("💎");
        priceIcon.setStyle("-fx-font-size: 20px; -fx-text-fill: #059669;");

        Label prixLabel = new Label(offre.getPrix() + " DT");
        prixLabel.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #059669;" +
                        "-fx-font-family: 'Roboto', 'Segoe UI', Arial, sans-serif;"
        );

        Label priceSubtitle = new Label("Prix par personne");
        priceSubtitle.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #6b7280;" +
                        "-fx-font-family: 'Roboto', 'Segoe UI', Arial, sans-serif;"
        );

        priceBox.getChildren().addAll(priceIcon, prixLabel, priceSubtitle);

        infoBox.getChildren().addAll(dateBox, priceBox);
        tripDetails.getChildren().addAll(trajetBox, infoBox);

        // Premium buttons with micro-interactions
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setStyle("-fx-padding: 15 0 0 0;");

        Button reserverButton = new Button("Réserver");
        reserverButton.setGraphic(new Label("✨ "));
        reserverButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #3b82f6, #1d4ed8);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 12 30;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 10, 0, 3, 3);"
        );

        Button voirVoitureButton = new Button("Véhicule");
        voirVoitureButton.setGraphic(new Label("🚗 "));
        voirVoitureButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #f59e0b, rgba(253,171,79,0.93));" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 12 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(253,171,79,0.93), 10, 0, 3, 3);"
        );

        // Button hover animations
        ScaleTransition buttonScale = new ScaleTransition(Duration.millis(150));
        FadeTransition buttonFade = new FadeTransition(Duration.millis(150));

        reserverButton.setOnMouseEntered(e -> {
            buttonScale.setNode(reserverButton);
            buttonScale.setToX(1.05);
            buttonScale.setToY(1.05);
            buttonScale.playFromStart();
            reserverButton.setStyle(
                    "-fx-background-color: linear-gradient(to right, #2563eb, #1e40af);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 15px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 15;" +
                            "-fx-padding: 12 10;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.6), 12, 0, 4, 4);"
            );
        });

        reserverButton.setOnMouseExited(e -> {
            buttonScale.setNode(reserverButton);
            buttonScale.setToX(1.0);
            buttonScale.setToY(1.0);
            buttonScale.playFromStart();
            reserverButton.setStyle(
                    "-fx-background-color: linear-gradient(to right, #3b82f6, #1d4ed8);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 15px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 15;" +
                            "-fx-padding: 12 30;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 10, 0, 3, 3);"
            );
        });

        voirVoitureButton.setOnMouseEntered(e -> {
            buttonScale.setNode(voirVoitureButton);
            buttonScale.setToX(1.05);
            buttonScale.setToY(1.05);
            buttonScale.playFromStart();
            voirVoitureButton.setStyle(
                    "-fx-background-color: linear-gradient(to right, #d97706, #b45309);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 15px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 15;" +
                            "-fx-padding: 12 30;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(245, 158, 11, 0.6), 12, 0, 4, 4);"
            );
        });

        voirVoitureButton.setOnMouseExited(e -> {
            buttonScale.setNode(voirVoitureButton);
            buttonScale.setToX(1.0);
            buttonScale.setToY(1.0);
            buttonScale.playFromStart();
            voirVoitureButton.setStyle(
                    "-fx-background-color: linear-gradient(to right, #f59e0b, #d97706);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 15px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 15;" +
                            "-fx-padding: 12 30;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(245, 158, 11, 0.4), 10, 0, 3, 3);"
            );
        });

        reserverButton.setOnAction(event -> showReservationConfirmation(offre));
        voirVoitureButton.setOnAction(event -> showCarImagePopup(offre));

        buttonBox.getChildren().addAll(reserverButton, voirVoitureButton);

        // Add all components to the card
        card.getChildren().addAll( profileBox, separator, tripDetails, buttonBox);
        return card;
    }
    private void showCarImagePopup(OffreCovoiturage offre) {
        // Debug: Log the start of the method
        System.out.println("Starting showCarImagePopup for offre: " + offre.getDepart() + " → " + offre.getDestination());

        Stage popupStage = new Stage();
        popupStage.setTitle("Image du Véhicule");

        // Main container
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 20; -fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-background-radius: 10;");
        layout.setPrefWidth(600); // Match the ImageView's fitWidth to prevent clipping

        // Title
        Label title = new Label("Véhicule pour le trajet " + offre.getDepart() + " → " + offre.getDestination());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");

        // Image view
        ImageView carImageView = new ImageView();
        carImageView.setFitWidth(600);
        carImageView.setFitHeight(300);
        carImageView.setPreserveRatio(true);
        carImageView.setSmooth(true);
        carImageView.setCache(true);

        // Load the image
        Image image = null;
        try {
            // Debug: Log the image path from offre
            System.out.println("offre.getImg() = " + offre.getImg());

            if (offre.getImg() != null && !offre.getImg().isEmpty()) {
                if (offre.getImg().startsWith("http")) {
                    // Handle URL-based image
                    System.out.println("Attempting to load image from URL: " + offre.getImg());
                    image = new Image(offre.getImg(), true); // true for background loading
                    if (image.isError()) {
                        System.out.println("Error loading URL image: " + image.getException().getMessage());
                        throw image.getException();
                    } else {
                        System.out.println("URL image loaded successfully");
                    }
                } else if (offre.getImg().contains("/") || offre.getImg().contains("\\")) {
                    // Handle filesystem path (absolute or relative)
                    System.out.println("Attempting to load image from filesystem: " + offre.getImg());
                    // Ensure the path uses the correct protocol and format
                    String filePath = offre.getImg();
                    // Convert backslashes to forward slashes for consistency
                    filePath = filePath.replace("\\", "/");
                    // If the path doesn't start with "file:", prepend it
                    if (!filePath.startsWith("file:")) {
                        filePath = "file:" + filePath;
                    }
                    System.out.println("Constructed file path: " + filePath);
                    image = new Image(filePath);
                    if (image.isError()) {
                        System.out.println("Error loading filesystem image: " + image.getException().getMessage());
                        throw image.getException();
                    } else {
                        System.out.println("Filesystem image loaded successfully");
                    }
                } else {
                    // Handle classpath resource
                    String resourcePath = "/images/" + offre.getImg(); // Adjust based on your project structure
                    System.out.println("Attempting to load image from classpath: " + resourcePath);
                    java.net.URL imgUrl = getClass().getResource(resourcePath);
                    if (imgUrl == null) {
                        System.out.println("Image resource not found in classpath: " + resourcePath);
                        throw new IllegalArgumentException("Image not found in classpath: " + resourcePath);
                    }
                    image = new Image(imgUrl.toString());
                    if (image.isError()) {
                        System.out.println("Error loading classpath image: " + image.getException().getMessage());
                        throw image.getException();
                    } else {
                        System.out.println("Classpath image loaded successfully");
                    }
                }
            } else {
                // No image specified, fall back to default
                System.out.println("No image specified in offre, loading default image");
                java.net.URL defaultImgUrl = getClass().getResource("/img/pic3.jpg");
                if (defaultImgUrl == null) {
                    System.out.println("Default image not found: /img/pic3.jpg");
                    throw new IllegalArgumentException("Default image not found: /img/pic3.jpg");
                }
                image = new Image(defaultImgUrl.toString());
                if (image.isError()) {
                    System.out.println("Error loading default image: " + image.getException().getMessage());
                    throw image.getException();
                } else {
                    System.out.println("Default image loaded successfully");
                }
            }
            carImageView.setImage(image);
        } catch (Exception e) {
            // Debug: Log any errors during image loading
            System.out.println("Error loading image: " + e.getMessage());
            e.printStackTrace();

            // Attempt to load the default image as a fallback
            try {
                java.net.URL defaultImgUrl = getClass().getResource("/img/pic3.jpg");
                if (defaultImgUrl == null) {
                    System.out.println("Default image not found: /img/pic3.jpg");
                } else {
                    image = new Image(defaultImgUrl.toString());
                    if (image.isError()) {
                        System.out.println("Error loading default image in catch block: " + image.getException().getMessage());
                    } else {
                        carImageView.setImage(image);
                        System.out.println("Default image loaded successfully in catch block");
                    }
                }
            } catch (Exception ex) {
                System.out.println("Failed to load default image in catch block: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        // Debug: Verify if an image was set
        if (carImageView.getImage() == null) {
            System.out.println("No image was set in carImageView");
        } else {
            System.out.println("Image set in carImageView: " + carImageView.getImage().getUrl());
        }

        // Close button
        Button closeButton = new Button("Fermer");
        closeButton.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 8;");
        closeButton.setOnAction(e -> {
            System.out.println("Closing popup");
            popupStage.close();
        });

        layout.getChildren().addAll(title, carImageView, closeButton);

        Scene scene = new Scene(layout);
        popupStage.setScene(scene);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initOwner(ajouterButton.getScene().getWindow());

        // Debug: Log before showing the popup
        System.out.println("Showing popup");
        popupStage.showAndWait();
        System.out.println("Popup closed");
    }
    private void showReservationConfirmation(OffreCovoiturage offre) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Confirmation de réservation");

        Label title = new Label("💬 Confirmer votre réservation");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label message = new Label("Souhaitez-vous réserver ce trajet de " + offre.getDepart() +
                " à " + offre.getDestination() + " le " + offre.getDate().toString() + " ?");
        message.setWrapText(true);
        message.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

        Button btnConfirmer = new Button("✅ Confirmer");
        btnConfirmer.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-padding: 8 16 8 16; -fx-background-radius: 8;");

        Button btnAnnuler = new Button("❌ Annuler");
        btnAnnuler.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 8 16 8 16; -fx-background-radius: 8;");

        HBox buttonBox = new HBox(10, btnConfirmer, btnAnnuler);
        buttonBox.setStyle("-fx-alignment: center; -fx-padding: 10 0 0 0;");

        VBox layout = new VBox(15, title, message, buttonBox);
        layout.setStyle("-fx-padding: 20; -fx-background-color: #fefce8; -fx-border-color: #facc15; -fx-border-radius: 10; -fx-background-radius: 10;");
        layout.setPrefWidth(400);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initOwner(ajouterButton.getScene().getWindow());
        popupStage.show();

        btnConfirmer.setOnAction(e -> {
            try {
                Reservation reservation = new Reservation();
                reservation.setPassagerId(SessionManager.getInstance().getUserId());
                reservation.setStatut("EN_ATTENTE");
                reservation.setOffre(offre);
                reservation.setCreatedAt(LocalDateTime.now());

                ServiceReservation serviceReservation = new ServiceReservation();
                serviceReservation.add(reservation);

                popupStage.close();

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Réservation réussie");
                success.setHeaderText(null);
                success.setContentText("🎉 Réservation enregistrée avec succès !");
                success.showAndWait();

            } catch (Exception ex) {
                popupStage.close();
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Erreur");
                error.setHeaderText(null);
                error.setContentText("Une erreur s'est produite lors de la réservation.");
                error.showAndWait();
            }
        });

        btnAnnuler.setOnAction(e -> popupStage.close());
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
    private void handleOpenAssistant(ActionEvent event) {
        try {
            System.out.println("Attempting to load Gemini.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Gemini.fxml"));
            Parent root = loader.load();
            System.out.println("FXML loaded successfully");

            Stage stage = new Stage();
            stage.setTitle("Assistant Virtuel");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(avatarMenu.getScene().getWindow());
            System.out.println("Showing stage");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading Gemini.fxml: " + e.getMessage());
            showAlert("Erreur", "Impossible d'ouvrir l'assistant: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}