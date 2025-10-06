package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import tn.esprit.models.OffreCovoiturage;
import tn.esprit.services.ServiceOffreCovoiturage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class MesOffresController {
    @FXML
    private FlowPane mesOffresContainer;

    @FXML
    private Pagination pagination;

    private List<OffreCovoiturage> allOffres;
    private static final int ITEMS_PER_PAGE = 3;

    @FXML
    public void initialize() {
        loadMesOffres();
        setupPagination();
    }

    private void loadMesOffres() {
        ServiceOffreCovoiturage service = new ServiceOffreCovoiturage();
        allOffres = service.getOffresByConducteurId(SessionManager.getInstance().getUserId()); // Assuming conducteurId is 1 for now; replace with actual logic);
    }

    private void setupPagination() {
        if (allOffres == null || allOffres.isEmpty()) {
            mesOffresContainer.getChildren().clear();
            Label noData = new Label("Vous n'avez publié aucune offre.");
            noData.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
            mesOffresContainer.getChildren().add(noData);
            pagination.setVisible(false);
            return;
        }

        int pageCount = (int) Math.ceil((double) allOffres.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        pagination.setVisible(true);

        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            displayPage(newIndex.intValue());
        });

        displayPage(0);
    }

    private void displayPage(int pageIndex) {
        mesOffresContainer.getChildren().clear();

        int startIndex = pageIndex * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allOffres.size());

        for (int i = startIndex; i < endIndex; i++) {
            VBox card = createCard(allOffres.get(i));
            applyEntranceAnimation(card, (i - startIndex) * 100);
            mesOffresContainer.getChildren().add(card);
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

    private VBox createCard(OffreCovoiturage offre) {
        VBox card = new VBox(10);
        card.setPrefSize(240, 180);
        card.setStyle("""
            -fx-background-color: linear-gradient(to bottom right, #ffffff, #e0f7fa);
            -fx-border-color: #6eabff;
            -fx-border-width: 2;
            -fx-border-radius: 20;
            -fx-background-radius: 20;
            -fx-padding: 20;
        """);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.2));
        shadow.setRadius(10);
        shadow.setOffsetX(4);
        shadow.setOffsetY(4);
        card.setEffect(shadow);

        Label title = new Label("🚗 " + offre.getDepart() + " ➝ " + offre.getDestination());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #005288;");

        Label date = new Label("📅 Date : " + offre.getDate());
        date.setStyle("-fx-font-size: 12px;");

        Label prix = new Label("💸 Prix : " + offre.getPrix() + " DT");
        prix.setStyle("-fx-font-size: 12px;");

        Button deleteButton = new Button("❌ ");
        deleteButton.setStyle("""
            -fx-background-color: #8ebbf2;
            -fx-text-fill: white;
            -fx-background-radius: 10;
            -fx-font-weight: bold;
            -fx-padding: 5 10;
        """);
        deleteButton.setOnAction(event -> handleDeleteOffre(offre));

        Button editButton = new Button("✍");
        editButton.setStyle("""
            -fx-background-color: #002557;
            -fx-text-fill: white;
            -fx-background-radius: 10;
            -fx-font-weight: bold;
            -fx-padding: 5 10;
        """);
        editButton.setOnAction(event -> handleEditOffre(offre));

        Button pdfButton = new Button("📄");
        pdfButton.setStyle("""
            -fx-background-color: #4CAF50;
            -fx-text-fill: white;
            -fx-background-radius: 10;
            -fx-font-weight: bold;
            -fx-padding: 5 10;
        """);
        pdfButton.setOnAction(event -> generatePdfForOffre(offre));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        buttonBox.getChildren().addAll(deleteButton, editButton, pdfButton);

        card.getChildren().addAll(title, date, prix, buttonBox);

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

    private void generatePdfForOffre(OffreCovoiturage offre) {
        try {
            Document document = new Document();
            String desktopPath = System.getProperty("user.home") + "/Desktop/";
            String fileName = "Offre_Covoiturage_" + offre.getId() + ".pdf";
            String filePath = desktopPath + fileName;

            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            document.add(new Paragraph("Détails de l'offre de covoiturage"));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("ID: " + offre.getId()));
            document.add(new Paragraph("Départ: " + offre.getDepart()));
            document.add(new Paragraph("Destination: " + offre.getDestination()));
            document.add(new Paragraph("Date: " + offre.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            document.add(new Paragraph("Prix: " + offre.getPrix() + " DT"));
            document.add(new Paragraph("Places disponibles: " + offre.getPlacesDispo()));
            document.add(new Paragraph("Matricule véhicule: " + offre.getMatVehicule()));
            document.add(new Paragraph("Statut: " + offre.getStatut()));

            document.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("PDF généré");
            alert.setHeaderText(null);
            alert.setContentText("Le PDF a été sauvegardé sur votre bureau sous le nom: " + fileName);
            alert.showAndWait();

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de la génération du PDF");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void handleEditOffre(OffreCovoiturage offre) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Éditer Offre");
        dialog.setHeaderText("Modifier les détails de l'offre");

        TextField departField = new TextField(offre.getDepart());
        TextField destinationField = new TextField(offre.getDestination());
        TextField matriculeField = new TextField(String.valueOf(offre.getMatVehicule()));
        TextField placesField = new TextField(String.valueOf(offre.getPlacesDispo()));
        TextField prixField = new TextField(String.valueOf(offre.getPrix()));
        DatePicker datePicker = new DatePicker(offre.getDate() != null ? offre.getDate().toLocalDate() : LocalDate.now());
        TextField timeField = new TextField(offre.getDate() != null ? offre.getDate().toLocalTime().toString() : "00:00");
        TextField imgField = new TextField(offre.getImg() != null ? offre.getImg() : "");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Départ:"), 0, 0);
        grid.add(departField, 1, 0);
        grid.add(new Label("Destination:"), 0, 1);
        grid.add(destinationField, 1, 1);
        grid.add(new Label("Matricule véhicule:"), 0, 2);
        grid.add(matriculeField, 1, 2);
        grid.add(new Label("Places disponibles:"), 0, 3);
        grid.add(placesField, 1, 3);
        grid.add(new Label("Prix:"), 0, 4);
        grid.add(prixField, 1, 4);
        grid.add(new Label("Date:"), 0, 5);
        grid.add(datePicker, 1, 5);
        grid.add(new Label("Heure (HH:mm):"), 0, 6);
        grid.add(timeField, 1, 6);
        grid.add(new Label("Image:"), 0, 7);
        grid.add(imgField, 1, 7);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    String depart = departField.getText();
                    String destination = destinationField.getText();
                    int matricule = Integer.parseInt(matriculeField.getText());
                    int places = Integer.parseInt(placesField.getText());
                    float prix = Float.parseFloat(prixField.getText());

                    LocalDate date = datePicker.getValue();
                    LocalTime time = LocalTime.parse(timeField.getText());
                    LocalDateTime dateTime = LocalDateTime.of(date, time);

                    String img = imgField.getText();

                    offre.setDepart(depart);
                    offre.setDestination(destination);
                    offre.setMatVehicule(matricule);
                    offre.setPlacesDispo(places);
                    offre.setDate(dateTime);
                    offre.setPrix(prix);
                    offre.setImg(img.isEmpty() ? null : img);
                    offre.setStatut("EN_ATTENTE");
                    offre.setConducteurId(SessionManager.getInstance().getUserId()); // Assuming conducteurId is 1 for now; replace with actual logic);

                    ServiceOffreCovoiturage service = new ServiceOffreCovoiturage();
                    service.update(offre);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Succès");
                    alert.setHeaderText("Offre modifiée avec succès !");
                    alert.show();
                    TimeUnit.SECONDS.sleep(1);
                    loadMesOffres();
                    setupPagination();

                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText("Échec de la modification de l'offre");
                    alert.setContentText(e.getMessage());
                    alert.show();
                }
            }
        });
    }

    private void handleDeleteOffre(OffreCovoiturage offre) {
        ServiceOffreCovoiturage service = new ServiceOffreCovoiturage();
        service.delete(offre);
        System.out.println("Offer deleted successfully.");
        loadMesOffres();
        setupPagination();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Offer deleted successfully.");
        alert.showAndWait();
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
    void handleAutresOffres(ActionEvent event) {
        loadScene("/Acceuil.fxml", event);
    }
    @FXML
    private void handleProfile(ActionEvent event) throws IOException {

        loadScene("/Profile.fxml", event);

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
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((javafx.scene.control.MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
            Scene scene = new Scene(root, 1500, 765);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleRetourAccueil(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Acceuil.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 1500, 765));
            stage.show();
            ((Stage) mesOffresContainer.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}