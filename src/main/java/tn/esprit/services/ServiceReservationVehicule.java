package tn.esprit.services;

import tn.esprit.models.ReservationVehicule;
import tn.esprit.models.Vehicule;
import tn.esprit.interfaces.IService;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceReservationVehicule implements IService<ReservationVehicule> {
    private Connection cnx;

    public ServiceReservationVehicule() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public boolean isVehiculeDisponible(int vehiculeId, LocalDate dateDebut, LocalDate dateFin) {
        String qry = "SELECT id, date_debut, date_fin FROM res_veh " +
                "WHERE id_vehicule = ? AND status = 'CONFIRMEE' " +
                "AND ((date_debut BETWEEN ? AND ?) OR " +
                "     (date_fin BETWEEN ? AND ?) OR " +
                "     (date_debut <= ? AND date_fin >= ?))";

        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, vehiculeId);
            pstm.setDate(2, Date.valueOf(dateDebut));
            pstm.setDate(3, Date.valueOf(dateFin));
            pstm.setDate(4, Date.valueOf(dateDebut));
            pstm.setDate(5, Date.valueOf(dateFin));
            pstm.setDate(6, Date.valueOf(dateDebut));
            pstm.setDate(7, Date.valueOf(dateFin));

            ResultSet rs = pstm.executeQuery();
            return !rs.next();
        } catch (SQLException e) {
            System.err.println("Erreur de vérification: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void add(ReservationVehicule reservation) {
        String qry = "INSERT INTO res_veh (id_client, id_vehicule, date_debut, date_fin, prix_total, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            LocalDate debut = reservation.getDate_debut(); // Déjà LocalDate
            LocalDate fin = reservation.getDate_fin(); // Déjà LocalDate

            if (!isVehiculeDisponible(reservation.getId_vehicule(), debut, fin)) {
                throw new RuntimeException("Véhicule déjà réservé pour cette période");
            }

            try (PreparedStatement pst = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS)) {
                pst.setInt(1, reservation.getId_client());
                pst.setInt(2, reservation.getId_vehicule());
                pst.setDate(3, Date.valueOf(reservation.getDate_debut()));
                pst.setDate(4, Date.valueOf(reservation.getDate_fin()));
                pst.setDouble(5, reservation.getPrix_total());
                pst.setString(6, reservation.getStatus());

                int affected = pst.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Échec de création");
                }

                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        reservation.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur technique: " + e.getMessage());
        }
    }    public List<ReservationVehicule> getReservationsForVehicule(int vehiculeId) {
        List<ReservationVehicule> reservations = new ArrayList<>();
        String qry = "SELECT * FROM res_veh WHERE id_vehicule = ? AND status = 'CONFIRMEE'";

        try (PreparedStatement pst = cnx.prepareStatement(qry)) {
            pst.setInt(1, vehiculeId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                LocalDate date_debut = rs.getDate("date_debut").toLocalDate();
                LocalDate date_fin = rs.getDate("date_fin").toLocalDate();

                ReservationVehicule r = new ReservationVehicule(
                        rs.getInt("id_client"),
                        rs.getInt("id_vehicule"),
                        date_debut, // Utilisation de LocalDate
                        date_fin,   // Utilisation de LocalDate
                        rs.getDouble("prix_total"),
                        rs.getString("status")
                );
                r.setId(rs.getInt("id"));
                reservations.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Erreur de récupération: " + e.getMessage());
        }
        return reservations;
    }
    @Override
    public List<ReservationVehicule> getAll() {
        List<ReservationVehicule> reservations = new ArrayList<>();
        String qry = "SELECT * FROM res_veh";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(qry)) {

            while (rs.next()) {
                LocalDate date_debut = rs.getDate("date_debut").toLocalDate();
                LocalDate date_fin = rs.getDate("date_fin").toLocalDate();
                ReservationVehicule r = new ReservationVehicule(
                        rs.getInt("id_client"),
                        rs.getInt("id_vehicule"),
                        date_debut,  // LocalDate
                        date_fin,    // LocalDate
                        rs.getDouble("prix_total"),
                        rs.getString("status")
                );
                r.setId(rs.getInt("id"));
                reservations.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Erreur de récupération: " + e.getMessage());
        }
        return reservations;
    }
    @Override
    public void update(ReservationVehicule reservation) {
        String qry = "UPDATE res_veh SET id_client=?, id_vehicule=?, date_debut=?, " +
                "date_fin=?, prix_total=?, status=? WHERE id=?";

        try (PreparedStatement pst = cnx.prepareStatement(qry)) {
            pst.setInt(1, reservation.getId_client());
            pst.setInt(2, reservation.getId_vehicule());
            pst.setDate(3, Date.valueOf(reservation.getDate_debut()));  // Conversion LocalDate -> sql.Date
            pst.setDate(4, Date.valueOf(reservation.getDate_fin()));    // Conversion LocalDate -> sql.Date
            pst.setDouble(5, reservation.getPrix_total());
            pst.setString(6, reservation.getStatus());
            pst.setInt(7, reservation.getId());

            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur de mise à jour: " + e.getMessage());
        }
    }    @Override
    public void delete(ReservationVehicule reservation) {
        String qry = "DELETE FROM res_veh WHERE id=?";

        try (PreparedStatement pst = cnx.prepareStatement(qry)) {
            pst.setInt(1, reservation.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur de suppression: " + e.getMessage());
        }
    }
    // Dans ServiceReservationVehicule.java
    public Map<String, Long> getReservationsBySeason() {
        Map<String, Long> seasonCounts = new LinkedHashMap<>();
        seasonCounts.put("Hiver", 0L);
        seasonCounts.put("Printemps", 0L);
        seasonCounts.put("Été", 0L);
        seasonCounts.put("Automne", 0L);

        String qry = "SELECT date_debut FROM res_veh";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(qry)) {

            while (rs.next()) {
                LocalDate date = rs.getDate("date_debut").toLocalDate();
                String season = getSeasonForDate(date);
                seasonCounts.put(season, seasonCounts.get(season) + 1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur de récupération par saison: " + e.getMessage());
        }

        return seasonCounts;
    }

    private String getSeasonForDate(LocalDate date) {
        int month = date.getMonthValue();

        if (month == 12 || month <= 2) {
            return "Hiver";
        } else if (month >= 3 && month <= 5) {
            return "Printemps";
        } else if (month >= 6 && month <= 8) {
            return "Été";
        } else {
            return "Automne";
        }
    }
    public void addFeedback(int reservationId, String feedback, int rating) {
        String qry = "UPDATE res_veh SET feedback = ?, rating = ? WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(qry)) {
            pst.setString(1, feedback);
            pst.setInt(2, rating);
            pst.setInt(3, reservationId);

            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du feedback: " + e.getMessage());
        }
    }
    public void addNotification(int reservationId, String message) {
        String qry = "UPDATE res_veh SET notifications = CONCAT(IFNULL(notifications, '[]'), ?) WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(qry)) {
            // Format JSON simple pour les notifications
            String notificationJson = ",{\"message\":\"" + message + "\",\"date\":\"" + LocalDate.now() + "\"}";
            notificationJson = notificationJson.replaceFirst(",", ""); // Pour la première notification

            pst.setString(1, notificationJson);
            pst.setInt(2, reservationId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la notification: " + e.getMessage());
        }
    }

    public List<String> getNotifications(int reservationId) {
        List<String> notifications = new ArrayList<>();
        String qry = "SELECT notifications FROM res_veh WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(qry)) {
            pst.setInt(1, reservationId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String json = rs.getString("notifications");
                // Parsez le JSON ici (vous pouvez utiliser une librairie comme Gson)
                // Pour cet exemple, nous retournons simplement la chaîne brute
                if (json != null && !json.isEmpty()) {
                    notifications.add(json);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur de récupération des notifications: " + e.getMessage());
        }
        return notifications;
    }
    public String getMostReservedVehicle() {
        String qry = "SELECT v.modele, COUNT(r.id) as reservation_count " +
                "FROM res_veh r JOIN vehicule v ON r.id_vehicule = v.id " +
                "GROUP BY r.id_vehicule ORDER BY reservation_count DESC LIMIT 1";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(qry)) {
            if (rs.next()) {
                return rs.getString("modele");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du véhicule le plus réservé: " + e.getMessage());
        }
        return null;
    }
}