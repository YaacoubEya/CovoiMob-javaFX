package controllers;

import tn.esprit.models.Vehicule;
import tn.esprit.services.ServiceVehicule;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class BackVehiculeController implements Initializable {

    @FXML private TableView<Vehicule> vehiculeTable;
    @FXML private TableColumn<Vehicule, String> typeCol;
    @FXML private TableColumn<Vehicule, String> modeleCol;
    @FXML private TableColumn<Vehicule, String> prixHeureCol;
    @FXML private TableColumn<Vehicule, String> prixJourCol;
    @FXML private TableColumn<Vehicule, String> dispoCol;
    @FXML private TableColumn<Vehicule, String> lieuCol;
    @FXML private TableColumn<Vehicule, Void> actionsCol;

    private final ServiceVehicule serviceVehicule = new ServiceVehicule();
    private final ObservableList<Vehicule> vehicules = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadVehicules();
    }

    private void setupTable() {
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type_vehicule"));
        modeleCol.setCellValueFactory(new PropertyValueFactory<>("modele"));
        prixHeureCol.setCellValueFactory(new PropertyValueFactory<>("prix_par_heure"));
        prixJourCol.setCellValueFactory(new PropertyValueFactory<>("prix_par_jour"));
        dispoCol.setCellValueFactory(new PropertyValueFactory<>("disponibilite"));
        lieuCol.setCellValueFactory(new PropertyValueFactory<>("lieu_retrait"));

        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");

            {
                editBtn.setOnAction(event -> {
                    Vehicule vehicule = getTableView().getItems().get(getIndex());
                    editVehicule(vehicule, event);
                });

                deleteBtn.setOnAction(event -> {
                    Vehicule vehicule = getTableView().getItems().get(getIndex());
                    deleteVehicule(vehicule);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(5, editBtn, deleteBtn));
                }
            }
        });

        vehiculeTable.setItems(vehicules);
    }

    private void loadVehicules() {
        vehicules.setAll(serviceVehicule.getAll());
    }

    private void editVehicule(Vehicule vehicule, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterVehicule.fxml"));
            Parent root = loader.load();

            AjouterVehiculeController controller = loader.getController();
            controller.setVehiculeToEdit(vehicule);

            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue de modification");
        }
    }

    private void deleteVehicule(Vehicule vehicule) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation de suppression");
        confirmationAlert.setHeaderText("Supprimer le véhicule");
        confirmationAlert.setContentText("Êtes-vous sûr de vouloir supprimer ce véhicule ? Toutes les réservations associées seront également supprimées.");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceVehicule.delete(vehicule);
                vehicules.remove(vehicule);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Véhicule supprimé avec succès");
            } catch (RuntimeException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAddVehicule(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterVehicule.fxml"));
            Parent root = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue d'ajout");
        }
    }

    @FXML
    private void handleReservationsView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackReservationView.fxml"));
            Parent root = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue des réservations");
        }
    }

    @FXML
    private void handleAllOffresView(ActionEvent event) {
        System.out.println("handleAllOffresView called in BackVehiculeController");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AllOffre.fxml"));
            Parent root = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue des offres : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}