package tn.esprit.services;

import controllers.ConfigLoader;
import tn.esprit.interfaces.IService;
import tn.esprit.models.OffreCovoiturage;
import tn.esprit.models.Reservation;
import tn.esprit.utils.MyDataBase;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceOffreCovoiturage implements IService<OffreCovoiturage> {
    private Connection cnx;

    public ServiceOffreCovoiturage() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(OffreCovoiturage offre) {
        String qry = "INSERT INTO `offre_covoiturage` (`depart`, `conducteur_id`, `destination`, `mat_vehicule`, `places_dispo`, `date`, `statut`, `prix`, `img`) VALUES (?,?,?,?,?,?,?,?,?)";
        try {
            // Étape 1 : Enregistrer l'offre dans la base de données
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setString(1, offre.getDepart());
            pstm.setInt(2, offre.getConducteurId());
            pstm.setString(3, offre.getDestination());
            pstm.setInt(4, offre.getMatVehicule());
            pstm.setInt(5, offre.getPlacesDispo());
            pstm.setTimestamp(6, Timestamp.valueOf(offre.getDate()));
            pstm.setString(7, offre.getStatut());
            pstm.setFloat(8, offre.getPrix());
            pstm.setString(9, offre.getImg());
            pstm.executeUpdate();

            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                offre.setId(rs.getInt(1));
            }

            // Étape 2 : Partager l'offre sur Facebook
            String facebookPageId = ConfigLoader.getFacebookPageId();
            String accessToken = ConfigLoader.getFacebookAccessToken();

            // Créer le message à publier
            String postMessage = String.format(
                    "🚗 Nouvelle offre de covoiturage disponible !\n\n" +
                            "📍 Départ : %s\n" +
                            "🏁 Destination : %s\n" +
                            "📅 Date : %s\n" +
                            "💵 Prix : %.2f DT\n" +
                            " Matricule : %d\n",
                    offre.getDepart(),
                    offre.getDestination(),
                    offre.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    offre.getPrix(),

                    offre.getMatVehicule()
            );

            // Construire la requête HTTP
            String url = String.format("https://graph.facebook.com/v22.0/%s/feed", facebookPageId);
            String queryParams = String.format("message=%s&access_token=%s",
                    URLEncoder.encode(postMessage, StandardCharsets.UTF_8),
                    URLEncoder.encode(accessToken, StandardCharsets.UTF_8));

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url + "?" + queryParams))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            // Envoyer la requête
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            // Vérifier la réponse
            if (statusCode == 200) {
                System.out.println("Offre partagée sur Facebook avec succès !");
            } else {
                System.out.println("Échec du partage sur Facebook. Code : " + statusCode + ", Réponse : " + response.body());
            }

        } catch (SQLException e) {
            System.out.println("Erreur SQL : " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Erreur lors du partage sur Facebook : " + e.getMessage());
        }
    }
    @Override
    public List<OffreCovoiturage> getAll() {
        List<OffreCovoiturage> offres = new ArrayList<>();
        String qry = "SELECT * FROM `offre_covoiturage`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                OffreCovoiturage o = new OffreCovoiturage();
                o.setId(rs.getInt("id"));
                o.setDepart(rs.getString("depart"));
                o.setConducteurId(rs.getInt("conducteur_id"));
                o.setDestination(rs.getString("destination"));
                o.setMatVehicule(rs.getInt("mat_vehicule"));
                o.setPlacesDispo(rs.getInt("places_dispo"));
                o.setDate(rs.getTimestamp("date").toLocalDateTime());
                o.setStatut(rs.getString("statut"));
                o.setPrix(rs.getFloat("prix"));
                o.setImg(rs.getString("img"));
                offres.add(o);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return offres;
    }

    public List<OffreCovoiturage> getFilteredOffres(String searchText, LocalDate date, String sortOption) {
        List<OffreCovoiturage> offres = new ArrayList<>();
        StringBuilder qry = new StringBuilder("SELECT * FROM `offre_covoiturage` WHERE 1=1");

        // Add search filter
        if (searchText != null && !searchText.trim().isEmpty()) {
            String searchPattern = "%" + searchText.trim().toLowerCase() + "%";
            qry.append(" AND (LOWER(depart) LIKE '").append(searchPattern.replace("'", "''")).append("'")
                    .append(" OR LOWER(destination) LIKE '").append(searchPattern.replace("'", "''")).append("')");
        }

        // Add date filter
        if (date != null) {
            qry.append(" AND DATE(date) = '").append(date.toString()).append("'");
        }

        // Add sorting
        if (sortOption != null) {
            switch (sortOption) {
                case "Date":
                    qry.append(" ORDER BY date ASC");
                    break;
                case "Prix croissant":
                    qry.append(" ORDER BY prix ASC");
                    break;
                case "Prix décroissant":
                    qry.append(" ORDER BY prix DESC");
                    break;
            }
        }

        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry.toString());
            while (rs.next()) {
                OffreCovoiturage o = new OffreCovoiturage();
                o.setId(rs.getInt("id"));
                o.setConducteurId(rs.getInt("conducteur_id"));
                o.setDepart(rs.getString("depart"));
                o.setDestination(rs.getString("destination"));
                o.setMatVehicule(rs.getInt("mat_vehicule"));
                o.setPlacesDispo(rs.getInt("places_dispo"));
                o.setDate(rs.getTimestamp("date").toLocalDateTime());
                o.setStatut(rs.getString("statut"));
                o.setPrix(rs.getFloat("prix"));
                o.setImg(rs.getString("img"));
                offres.add(o);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return offres;
    }

    @Override
    public void update(OffreCovoiturage offre) {
        String qry = "UPDATE `offre_covoiturage` SET `depart`=?, `conducteur_id`=?, `destination`=?, `mat_vehicule`=?, `places_dispo`=?, `date`=?, `statut`=?, `prix`=?, `img`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, offre.getDepart());
            pstm.setInt(2, offre.getConducteurId());
            pstm.setString(3, offre.getDestination());
            pstm.setInt(4, offre.getMatVehicule());
            pstm.setInt(5, offre.getPlacesDispo());
            pstm.setTimestamp(6, Timestamp.valueOf(offre.getDate()));
            pstm.setString(7, offre.getStatut());
            pstm.setFloat(8, offre.getPrix());
            pstm.setString(9, offre.getImg());
            pstm.setInt(10, offre.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(OffreCovoiturage offre) {
        String qry = "DELETE FROM `offre_covoiturage` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, offre.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<OffreCovoiturage> getAutresOffresByConducteurId(int conducteurId) {
        List<OffreCovoiturage> offres = new ArrayList<>();
        String qry = "SELECT * FROM `offre_covoiturage` WHERE `conducteur_id` != ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, conducteurId);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                OffreCovoiturage offre = new OffreCovoiturage();
                offre.setId(rs.getInt("id"));
                offre.setDepart(rs.getString("depart"));
                offre.setConducteurId(rs.getInt("conducteur_id"));
                offre.setDestination(rs.getString("destination"));
                offre.setMatVehicule(rs.getInt("mat_vehicule"));
                offre.setPlacesDispo(rs.getInt("places_dispo"));
                offre.setDate(rs.getTimestamp("date").toLocalDateTime());
                offre.setStatut(rs.getString("statut"));
                offre.setPrix(rs.getFloat("prix"));
                offre.setImg(rs.getString("img"));
                offres.add(offre);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return offres;
    }

    public List<OffreCovoiturage> getOffresByConducteurId(int conducteurId) {
        List<OffreCovoiturage> offres = new ArrayList<>();
        String qry = "SELECT * FROM `offre_covoiturage` WHERE `conducteur_id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, conducteurId);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                OffreCovoiturage offre = new OffreCovoiturage();
                offre.setId(rs.getInt("id"));
                offre.setDepart(rs.getString("depart"));
                offre.setConducteurId(rs.getInt("conducteur_id"));
                offre.setDestination(rs.getString("destination"));
                offre.setMatVehicule(rs.getInt("mat_vehicule"));
                offre.setPlacesDispo(rs.getInt("places_dispo"));
                offre.setDate(rs.getTimestamp("date").toLocalDateTime());
                offre.setStatut(rs.getString("statut"));
                offre.setPrix(rs.getFloat("prix"));
                offre.setImg(rs.getString("img"));
                // Fetch reservations for this offer
                offre.setReservations(getReservationsByOffreId(offre.getId()));
                offres.add(offre);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return offres;
    }

    // Helper method to fetch reservations for an offer
    private List<Reservation> getReservationsByOffreId(int offreId) {
        List<Reservation> reservations = new ArrayList<>();
        String qry = "SELECT * FROM `reservation` WHERE `offre_id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, offreId);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setId(rs.getInt("id"));
                reservation.setPassagerId(rs.getInt("passager_id"));
                reservation.setStatut(rs.getString("statut"));
                reservation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                // Not setting offre to avoid circular reference
                reservations.add(reservation);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return reservations;
    }

    // Note: This method seems redundant with getOffresByConducteurId; consider merging or removing
    public List<OffreCovoiturage> getByConducteurId(int conducteurId) {
        List<OffreCovoiturage> offres = new ArrayList<>();
        String query = "SELECT * FROM offre_covoiturage WHERE conducteur_id = ?";

        try {
            PreparedStatement statement = cnx.prepareStatement(query);
            statement.setInt(1, conducteurId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                OffreCovoiturage offre = new OffreCovoiturage();
                offre.setId(resultSet.getInt("id"));
                offre.setConducteurId(resultSet.getInt("conducteur_id"));
                offre.setDepart(resultSet.getString("depart"));
                offre.setDestination(resultSet.getString("destination"));
                offre.setMatVehicule(resultSet.getInt("mat_vehicule"));
                offre.setPlacesDispo(resultSet.getInt("places_dispo"));
                offre.setDate(resultSet.getTimestamp("date").toLocalDateTime());
                offre.setStatut(resultSet.getString("statut"));
                offre.setPrix(resultSet.getFloat("prix"));
                offre.setImg(resultSet.getString("img"));
                // Fetch reservations for this offer
                offre.setReservations(getReservationsByOffreId(offre.getId()));
                offres.add(offre);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return offres;
    }

    public OffreCovoiturage getById(int id) {
        OffreCovoiturage offre = null;
        String qry = "SELECT * FROM `offre_covoiturage` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                offre = new OffreCovoiturage();
                offre.setId(rs.getInt("id"));
                offre.setDepart(rs.getString("depart"));
                offre.setConducteurId(rs.getInt("conducteur_id"));
                offre.setDestination(rs.getString("destination"));
                offre.setMatVehicule(rs.getInt("mat_vehicule"));
                offre.setPlacesDispo(rs.getInt("places_dispo"));
                offre.setDate(rs.getTimestamp("date").toLocalDateTime());
                offre.setStatut(rs.getString("statut"));
                offre.setPrix(rs.getFloat("prix"));
                offre.setImg(rs.getString("img"));
                // Fetch reservations for this offer
                offre.setReservations(getReservationsByOffreId(offre.getId()));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return offre;
    }
}