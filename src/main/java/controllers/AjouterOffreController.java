package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import tn.esprit.models.OffreCovoiturage;
import tn.esprit.models.User;
import tn.esprit.services.CrudUser;
import tn.esprit.services.ServiceOffreCovoiturage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Properties;

public class AjouterOffreController {

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField departField;

    @FXML
    private TextField destinationField;

    @FXML
    private Label fileLabel;

    @FXML
    private TextField matriculeField;

    @FXML
    private TextField placesField;

    @FXML
    private TextField prixField;

    @FXML
    private Button submitButton;

    @FXML
    private TextField timeField;

    @FXML
    private Button homeButton;

    @FXML
    private Button uploadButton;

    private String selectedImagePath;

    private static final String GMAIL_USERNAME = "eyayaacoub555@gmail.com";
    private static final String GMAIL_PASSWORD = "zdzoxlhdmgvfcdtr";

    @FXML
    void initialize() {
        uploadButton.setOnAction(this::handleUploadAction);
    }

    @FXML
    private void handleUploadAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        // Restrict to image files
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        // Show the file chooser dialog
        File selectedFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
        if (selectedFile != null) {
            // Update the label with the file name
            fileLabel.setText(selectedFile.getName());
            // Store the file path
            selectedImagePath = selectedFile.getAbsolutePath();
        } else {
            fileLabel.setText("Aucune sélection");
            selectedImagePath = null;
        }
    }

    @FXML
    void ajouterOffreAction(ActionEvent event) {
        try {
            String depart = departField.getText().trim();
            String destination = destinationField.getText().trim();
            String matriculeStr = matriculeField.getText().trim();
            String placesStr = placesField.getText().trim();
            String prixStr = prixField.getText().trim();
            String timeStr = timeField.getText().trim();
            LocalDate date = datePicker.getValue();

            if (depart.isEmpty() || destination.isEmpty() || matriculeStr.isEmpty() ||
                    placesStr.isEmpty() || prixStr.isEmpty() || timeStr.isEmpty() || date == null) {
                showAlert("Erreur de saisie", "Tous les champs doivent être remplis.");
                return;
            }

            if (!depart.matches("[a-zA-Z ]+") || !destination.matches("[a-zA-Z ]+")) {
                showAlert("Erreur de saisie", "Le champ 'Départ' et 'Destination' doivent contenir uniquement des lettres.");
                return;
            }

            if (matriculeStr.length() != 8) {
                showAlert("Erreur de saisie", "Le matricule doit contenir exactement 8 caractères.");
                return;
            }

            float prix;
            try {
                prix = Float.parseFloat(prixStr);
                if (prix <= 0) {
                    showAlert("Erreur de saisie", "Le prix doit être un nombre décimal positif.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur de saisie", "Le prix doit être un nombre décimal.");
                return;
            }

            int places;
            try {
                places = Integer.parseInt(placesStr);
            } catch (NumberFormatException e) {
                showAlert("Erreur de saisie", "Le nombre de places doit être un entier.");
                return;
            }

            int matricule;
            try {
                matricule = Integer.parseInt(matriculeStr);
            } catch (NumberFormatException e) {
                showAlert("Erreur de saisie", "Le matricule doit contenir uniquement des chiffres.");
                return;
            }

            LocalTime time;
            try {
                time = LocalTime.parse(timeStr);
            } catch (Exception e) {
                showAlert("Erreur de saisie", "L'heure doit être au format HH:mm.");
                return;
            }

            LocalDateTime dateTime = LocalDateTime.of(date, time);

            if (dateTime.isBefore(LocalDateTime.now())) {
                showAlert("Erreur de saisie", "La date et l'heure doivent être ultérieures à l'instant actuel.");
                return;
            }

            // Use the selectedImagePath for the image path
            String img = selectedImagePath != null ? selectedImagePath : "";

            OffreCovoiturage offre = new OffreCovoiturage(
                    depart,
                    SessionManager.getInstance().getUserId(), // Assuming conducteurId from SessionManager
                    destination,
                    matricule,
                    places,
                    dateTime,
                    "active",
                    prix,
                    img.isEmpty() ? null : img
            );

            ServiceOffreCovoiturage sf = new ServiceOffreCovoiturage();
            sf.add(offre);

            Integer iduser = SessionManager.getInstance().getUserId();
            CrudUser s_user = new CrudUser();
            User user = s_user.getById(iduser);
            String userEmail = user.getEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                sendConfirmationEmail(userEmail, depart, destination);
            } else {
                System.err.println("User email not found in SessionManager.");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Offre ajoutée avec succès !");
            alert.show();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Acceuil.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1500, 765));
            stage.show();

        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors du chargement de la page : " + e.getMessage());
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur inattendue est survenue : " + e.getMessage());
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
    private void handleProfile(ActionEvent event) throws IOException {

        loadScene("/Profile.fxml", event);

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
    void handleAjouterAction(ActionEvent event) {
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
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
            Scene scene = new Scene(root, 1500, 765);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToHome(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Acceuil.fxml"));
            Stage stage = (Stage) homeButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1500, 765));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void sendConfirmationEmail(String to, String depart, String destination) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(GMAIL_USERNAME, GMAIL_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(GMAIL_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Confirmation de votre offre de covoiturage");

            String htmlContent = String.format(
                    "<html><body>" +
                            "<h2>Confirmation de votre offre</h2>" +
                            "<p>Salut, utilisateur !</p>" +
                            "<p>Vous avez partagé une offre de covoiturage avec les détails suivants :</p>" +
                            "<ul>" +
                            "<li><strong>Départ :</strong> %s</li>" +
                            "<li><strong>Destination :</strong> %s</li>" +
                            "</ul>" +
                            "<p>Merci de votre contribution !</p>" +
                            "</body></html>",
                    depart, destination
            );

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
            showAlert("Erreur d'email", "Impossible d'envoyer l'email de confirmation : " + e.getMessage());
        }
    }
}