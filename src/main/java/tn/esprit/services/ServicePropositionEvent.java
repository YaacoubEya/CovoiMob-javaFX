package tn.esprit.services;

import tn.esprit.models.ProposerEvent;
import tn.esprit.interfaces.IService;
import tn.esprit.utils.MyDataBase;
import java.util.Date;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ServicePropositionEvent implements IService<ProposerEvent> {

    private Connection cnx;

    public ServicePropositionEvent() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(ProposerEvent event) {
        String qry = "INSERT INTO proposer_event (title, description, location, start_date, end_date, event_type, price, status, user_id, image_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(qry);
            pst.setString(1, event.getTitle());
            pst.setString(2, event.getDescription());
            pst.setString(3, event.getLocation());
            pst.setDate(4, new java.sql.Date(event.getStartDate().getTime()));
            pst.setDate(5, new java.sql.Date(event.getEndDate().getTime()));
            pst.setString(6, event.getEventType());
            pst.setDouble(7, event.getPrice());
            pst.setString(8, event.getStatus());
            pst.setInt(9, event.getUserId());
            pst.setString(10, event.getImageUrl());

            pst.executeUpdate();
            System.out.println("Event inserted successfully!");
        } catch (SQLException ex) {
            System.out.println("Error inserting event: " + ex.getMessage());
        }
    }

    @Override
    public List<ProposerEvent> getAll() {
        List<ProposerEvent> events = new ArrayList<>();
        String qry = "SELECT * FROM `proposer_event`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                ProposerEvent e = new ProposerEvent();
                e.setId(rs.getInt("id"));
                e.setTitle(rs.getString("title"));
                e.setDescription(rs.getString("description"));
                e.setLocation(rs.getString("location"));
                e.setStartDate(rs.getTimestamp("start_date"));
                e.setEndDate(rs.getTimestamp("end_date"));
                e.setEventType(rs.getString("event_type"));
                e.setPrice(rs.getDouble("price"));
                e.setStatus(rs.getString("status"));
                e.setUserId(rs.getInt("user_id"));
                e.setImageUrl(rs.getString("image_url"));

                events.add(e);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return events;
    }

    @Override
    public void update(ProposerEvent event) {
        String qry = "UPDATE `proposer_event` SET `title`=?, `description`=?, `start_date`=?, `end_date`=?, `event_type`=?, `price`=?, `status`=?, `user_id`=?, `image_url`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, event.getTitle());
            pstm.setString(2, event.getDescription());
            pstm.setTimestamp(3, new Timestamp(event.getStartDate().getTime()));
            pstm.setTimestamp(4, new Timestamp(event.getEndDate().getTime()));
            pstm.setString(5, event.getEventType());
            pstm.setDouble(6, event.getPrice());
            pstm.setString(7, event.getStatus());
            pstm.setInt(8, event.getUserId());
            pstm.setString(9, event.getImageUrl());
            pstm.setInt(10, event.getId());

            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(tn.esprit.models.ProposerEvent event) {
        String qry = "DELETE FROM `proposer_event` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, event.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public ProposerEvent getById(int id) {
        // Implémentez la logique pour récupérer un événement par son ID
        // Par exemple :
        return getAll().stream()
                .filter(e -> e.getId() == id)
                .findFirst()
                .orElse(null);
    }
    public List<ProposerEvent> getUpcomingEvents() {
        List<tn.esprit.models.ProposerEvent> allEvents = getAll();

        if (allEvents == null || allEvents.isEmpty()) {
            return Collections.emptyList();
        }

        Date now = new Date();
        return allEvents.stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getStartDate() != null)
                .filter(e -> e.getStartDate().after(now))
                .collect(Collectors.toList());
    }
}