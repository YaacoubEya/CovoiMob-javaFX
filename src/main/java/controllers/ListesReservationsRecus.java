package controllers;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import tn.esprit.models.OffreCovoiturage;
import tn.esprit.models.Reservation;
import tn.esprit.services.ServiceOffreCovoiturage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ListesReservationsRecus {

    @FXML
    private ListView<Offer> offersListView;
    @FXML
    private VBox welcomeCard;

    @FXML
    private Label titleLabel;
    @FXML
    void handleMesOffres(ActionEvent event) {
        loadScene("/MesOffres.fxml",  event);
    }
    @FXML
    void handleAutresOffres(ActionEvent event) {
        loadScene("/Acceuil.fxml",  event);
    }
    @FXML
    void handleListeAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListesReservationRecus.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1500, 765);
            stage.setScene(scene);
            stage.setTitle("Ajouter une demande/offre");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @FXML
    public void handleAjouterAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterOffre.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
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
            // Load the new FXML
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));

            // Get the current stage from the event source
            Stage stage = (Stage) ((javafx.scene.control.MenuItem) event.getSource()).getParentPopup().getOwnerWindow();

            // Create a new scene with the loaded root and set it to the stage
            Scene scene = new Scene(root, 1500, 765);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML

    private void showHelpPopup() {
        // Création de l'icône d'information
        ImageView infoIcon = new ImageView(new Image(getClass().getResourceAsStream("/img/conducteur.jpg")));
        infoIcon.setFitWidth(150);
        infoIcon.setFitHeight(150);

        // Si vous n'avez pas d'icône, vous pouvez utiliser un Label stylisé
        if (infoIcon.getImage() == null) {
            infoIcon = null;
        }

        // Création du header
        Label header = new Label("Bienvenue conducteur !");
        header.setGraphic(infoIcon);
        header.setContentDisplay(ContentDisplay.LEFT);
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        header.setGraphicTextGap(10);

        // Création du contenu avec formatage riche
        TextFlow content = new TextFlow();
        content.setStyle("-fx-padding: 10 0 0 0;");

        Text intro = new Text("Ici, vous trouverez toutes les informations concernant vos offres et les réservations des passagers.\n\n");
        intro.setStyle("-fx-font-size: 14px; -fx-fill: #34495e;");

        Text step1 = new Text("1. Statut initial de l'offre : ");
        step1.setStyle("-fx-font-size: 14px; -fx-fill: #2c3e50; -fx-font-weight: bold;");
        Text step1Desc = new Text("en attente de réservation. Si au moins une réservation est reçue, le statut passe à 'en attente de votre sélection'.\n\n");
        step1Desc.setStyle("-fx-font-size: 14px; -fx-fill: #34495e;");

        Text step2 = new Text("2. Sélection du passager : ");
        step2.setStyle("-fx-font-size: 14px; -fx-fill: #2c3e50; -fx-font-weight: bold;");
        Text step2Desc = new Text("Vous devez choisir parmi les passagers ayant réservé, en respectant le nombre de places disponibles.\n\n");
        step2Desc.setStyle("-fx-font-size: 14px; -fx-fill: #34495e;");

        Text step3 = new Text("3. Confirmation : ");
        step3.setStyle("-fx-font-size: 14px; -fx-fill: #2c3e50; -fx-font-weight: bold;");
        Text step3Desc = new Text("Après sélection, le statut devient 'en attente de confirmation'. Si tous confirment, c'est confirmé. Sinon, vous devez re-sélectionner.\n\n");
        step3Desc.setStyle("-fx-font-size: 14px; -fx-fill: #34495e;");

        Text goodLuck = new Text("Bonne chance et bon choix !");
        goodLuck.setStyle("-fx-font-size: 14px; -fx-fill: #27ae60; -fx-font-weight: bold; -fx-font-style: italic;");

        content.getChildren().addAll(intro, step1, step1Desc, step2, step2Desc, step3, step3Desc, goodLuck);

        // Création du conteneur principal
        VBox popupContent = new VBox(15);
        popupContent.setStyle("-fx-padding: 20; -fx-background-color: #ffffff;");
        popupContent.setPrefSize(650, 400);
        popupContent.getChildren().addAll(header, new Separator(), content);

        // Création de la boîte de dialogue
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Guide du Conducteur");

        // Style de la boîte de dialogue
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #ffffff; "
                + "-fx-border-color: #bdc3c7; "
                + "-fx-border-width: 2; "
                + "-fx-border-radius: 5; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0.5, 0, 2);");

        dialogPane.getButtonTypes().add(ButtonType.OK);
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #3498db, "
                + "linear-gradient(#3498db 0%, #2980b9 20%, #1a5276 100%); "
                + "-fx-text-fill: white; "
                + "-fx-font-weight: bold; "
                + "-fx-background-radius: 5; "
                + "-fx-padding: 5 15 5 15;");

        dialogPane.setContent(popupContent);

        // Animation d'apparition
        dialogPane.setOpacity(0);
        dialogPane.setScaleX(0.9);
        dialogPane.setScaleY(0.9);

        dialog.show();

        ParallelTransition animations = new ParallelTransition();

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.3), dialogPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scaleIn = new ScaleTransition(Duration.seconds(0.3), dialogPane);
        scaleIn.setFromX(0.9);
        scaleIn.setFromY(0.9);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);

        animations.getChildren().addAll(fadeIn, scaleIn);
        animations.play();

        // Animation de survol pour le bouton OK
        okButton.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), okButton);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        okButton.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), okButton);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    @FXML

    private void playEntranceAnimation() {
        // Slide-in from the top
        TranslateTransition slideIn = new TranslateTransition(Duration.seconds(1), welcomeCard);
        slideIn.setFromY(-100);
        slideIn.setToY(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);

        // Fade-in
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), welcomeCard);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Run both animations in parallel
        ParallelTransition parallelTransition = new ParallelTransition(slideIn, fadeIn);
        parallelTransition.play();
    }

    private void setupHoverEffects() {
        // Hover glow effect on title
        titleLabel.setOnMouseEntered(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), titleLabel);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);
            scaleUp.play();
        });

        titleLabel.setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), titleLabel);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();
        });
    }
    @FXML
    public void initialize() {
        // Fetch data from the service
        ServiceOffreCovoiturage service = new ServiceOffreCovoiturage();
        List<OffreCovoiturage> covoiturageOffers;
        playEntranceAnimation();
        setupHoverEffects();
        try {
            covoiturageOffers = service.getOffresByConducteurId(SessionManager.getInstance().getUserId());
        } catch (Exception e) {
            System.err.println("Error fetching offers: " + e.getMessage());
            return; // Exit if fetching fails
        }

        // Map OffreCovoiturage to Offer
        List<Offer> offers = covoiturageOffers.stream()
                .map(oc -> new Offer(oc.getDepart(), oc.getDestination(), oc.getPrix(), oc.getStatut(), oc.getReservations()))
                .collect(Collectors.toList());

        // Populate the ListView
        if (offers != null && !offers.isEmpty()) {
            offersListView.getItems().addAll(offers);
        }

        // Customize the ListView cell rendering
        offersListView.setCellFactory(listView -> new ListCell<Offer>() {
            private VBox reservationBox = null;
            private boolean isExpanded = false;

            @Override
            protected void updateItem(Offer offer, boolean empty) {
                super.updateItem(offer, empty);
                if (empty || offer == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create a custom VBox for each offer
                    VBox mainBox = new VBox(10);
                    mainBox.setStyle("-fx-background-color: rgba(255,255,255,0.9); " +
                            "-fx-background-radius: 10; " +
                            "-fx-padding: 15; " +
                            "-fx-border-color: #d3d3d3; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 10; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);");

                    // Offer details
                    HBox offerBox = new HBox(20);
                    offerBox.setStyle("-fx-alignment: CENTER_LEFT;");

                    // Status indicator
                    Circle statusIndicator = new Circle(8);
                    statusIndicator.setFill(getStatusColor(offer.getStatus()));

                    VBox routeBox = new VBox(5);
                    Label routeLabel = new Label("📍 " + offer.getName() + " → " + offer.getDestination());
                    routeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #003087; -fx-font-weight: bold;");

                    Label priceLabel = new Label("💵 Prix: " + offer.getPrix() + " DT");
                    priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2e8b57;");
                    routeBox.getChildren().addAll(routeLabel, priceLabel);

                    Label statusLabel = new Label("Statut: " + offer.getStatus());
                    statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-font-weight: bold;");

                    // Spacer to push status to the right
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    offerBox.getChildren().addAll(statusIndicator, routeBox, spacer, statusLabel);
                    mainBox.getChildren().add(offerBox);

                    // Add click event to toggle reservations
                    offerBox.setOnMouseClicked(event -> {
                        if (reservationBox == null) {
                            // Initialize reservation list
                            reservationBox = new VBox(10);
                            reservationBox.setStyle("-fx-background-color: #f8f9fa; " +
                                    "-fx-padding: 15; " +
                                    "-fx-background-radius: 5; " +
                                    "-fx-border-color: #e0e0e0; " +
                                    "-fx-border-width: 1; " +
                                    "-fx-border-radius: 5;");

                            if (offer.getReservations() != null && !offer.getReservations().isEmpty()) {
                                Label resTitle = new Label("Réservations (" + offer.getReservations().size() + ")");
                                resTitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #003087; -fx-font-weight: bold;");
                                reservationBox.getChildren().add(resTitle);

                                for (Reservation reservation : offer.getReservations()) {
                                    HBox resRow = new HBox(15);
                                    resRow.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5;");

                                    Circle passengerIcon = new Circle(5, Color.web("#ff8c00"));

                                    VBox passengerDetails = new VBox(2);
                                    Label passengerLabel = new Label("Passager: Yaacoub Eya");
                                    passengerLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

                                    Label resStatusLabel = new Label("Statut: " + reservation.getStatut());
                                    resStatusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
                                    passengerDetails.getChildren().addAll(passengerLabel, resStatusLabel);

                                    resRow.getChildren().addAll(passengerIcon, passengerDetails);
                                    reservationBox.getChildren().add(resRow);

                                    // Add separator between reservations
                                    if (offer.getReservations().indexOf(reservation) < offer.getReservations().size() - 1) {
                                        Rectangle separator = new Rectangle(0, 1);
                                        separator.widthProperty().bind(reservationBox.widthProperty().subtract(30));
                                        separator.setFill(Color.web("#e0e0e0"));
                                        reservationBox.getChildren().add(separator);
                                    }
                                }
                            } else {
                                Label noResLabel = new Label("Aucune réservation pour le moment.");
                                noResLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-font-style: italic;");
                                reservationBox.getChildren().add(noResLabel);
                            }
                        }

                        if (isExpanded) {
                            mainBox.getChildren().remove(reservationBox);
                        } else {
                            mainBox.getChildren().add(reservationBox);
                        }
                        isExpanded = !isExpanded;
                    });

                    setGraphic(mainBox);
                }
            }

            private Color getStatusColor(String status) {
                switch (status.toLowerCase()) {
                    case "confirmé":
                        return Color.web("#2e8b57"); // Sea green
                    case "en attente":
                        return Color.web("#ff8c00"); // Dark orange
                    case "annulé":
                        return Color.web("#dc143c"); // Crimson
                    default:
                        return Color.web("#666666"); // Gray
                }
            }
        });
    }


    // Offer class to hold data
    public static class Offer {
        private final String name;
        private final String destination;
        private final float prix;
        private final String status;
        private final List<Reservation> reservations;

        public Offer(String name, String destination, float prix, String status, List<Reservation> reservations) {
            this.name = name;
            this.destination = destination;
            this.prix = prix;
            this.status = status;
            this.reservations = reservations;
        }

        public String getName() {
            return name;
        }

        public String getDestination() {
            return destination;
        }

        public String getStatus() {
            return status;
        }

        public List<Reservation> getReservations() {
            return reservations;
        }

        public float getPrix() {
            return prix;
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



}