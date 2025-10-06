package tn.esprit.services;

import tn.esprit.models.ReserverEvent;
import tn.esprit.interfaces.IService;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReservationEvent implements IService<ReserverEvent> {

    private Connection cnx;

    public ServiceReservationEvent() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(ReserverEvent reservation) {
        String qry = "INSERT INTO `reserve_event`(`booking_date`, `quantity`, `total_amount`, `status`, `user_id`, `event_title`) VALUES (?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setTimestamp(1, new Timestamp(reservation.getBookingDate().getTime()));
            pstm.setInt(2, reservation.getQuantity());
            pstm.setDouble(3, reservation.getTotalAmount());
            pstm.setString(4, reservation.getStatus());
            pstm.setInt(5, reservation.getUserId());
            pstm.setString(6, reservation.getEventTitle());

            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<ReserverEvent> getAll() {
        List<ReserverEvent> reservations = new ArrayList<>();
        String qry = "SELECT re.*, pe.image_url, pe.start_date FROM `reserve_event` re " +
                "LEFT JOIN `proposer_event` pe ON re.event_title = pe.title";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                ReserverEvent r = new ReserverEvent();
                r.setId(rs.getInt("id"));
                r.setBookingDate(rs.getTimestamp("booking_date"));
                r.setQuantity(rs.getInt("quantity"));
                r.setTotalAmount(rs.getDouble("total_amount"));
                r.setStatus(rs.getString("status"));
                r.setUserId(rs.getInt("user_id"));
                r.setEventTitle(rs.getString("event_title"));
                r.setEventImageUrl(rs.getString("image_url"));
                r.setEventDate(rs.getTimestamp("start_date")); // Nouveau champ
                reservations.add(r);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return reservations;
    }
    @Override
    public void update(ReserverEvent reservation) {
        String qry = "UPDATE `reserve_event` SET `booking_date`=?, `quantity`=?, `total_amount`=?, `status`=?, `user_id`=?, `event_title`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setTimestamp(1, new Timestamp(reservation.getBookingDate().getTime()));
            pstm.setInt(2, reservation.getQuantity());
            pstm.setDouble(3, reservation.getTotalAmount());
            pstm.setString(4, reservation.getStatus());
            pstm.setInt(5, reservation.getUserId());
            pstm.setString(6, reservation.getEventTitle());
            pstm.setInt(7, reservation.getId());

            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(ReserverEvent reservation) {
        String qry = "DELETE FROM `reserve_event` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, reservation.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public  List<ReserverEvent> getByUserId(int userId) {
        List<ReserverEvent> reservations = new ArrayList<>();
        String qry = "SELECT * FROM `reserve_event` WHERE user_id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, userId);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                ReserverEvent r = new ReserverEvent();
                r.setId(rs.getInt("id"));
                r.setBookingDate(rs.getTimestamp("booking_date"));
                r.setQuantity(rs.getInt("quantity"));
                r.setTotalAmount(rs.getDouble("total_amount"));
                r.setStatus(rs.getString("status"));
                r.setUserId(rs.getInt("user_id"));
                r.setEventTitle(rs.getString("event_title"));
                reservations.add(r);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return reservations;
    }

    public void addWithNotification(ReserverEvent reservation) {
        this.add(reservation);
        triggerBackendNotification(reservation);
    }

    private void triggerBackendNotification(ReserverEvent reservation) {
        // Vous pourriez implémenter ici une logique pour notifier le backend
        // via WebSocket, SSE ou autre mécanisme de communication en temps réel
        System.out.println("Nouvelle réservation créée : " + reservation.getEventTitle());
    }

}