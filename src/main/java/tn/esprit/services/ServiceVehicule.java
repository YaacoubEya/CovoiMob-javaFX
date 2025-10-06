package tn.esprit.services;


import tn.esprit.models.Vehicule;
import tn.esprit.interfaces.IService;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceVehicule implements IService<Vehicule> {
    private Connection cnx;

    public ServiceVehicule() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Vehicule vehicule) {
        String qry = "INSERT INTO `vehicule`(`type_vehicule`, `modele`, `role`, `prix_par_heure`, `prix_par_jour`, `disponibilite`, `lieu_retrait`, `image`, `notifications`, `likes`, `dislikes`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try {
            cnx.setAutoCommit(false);
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, vehicule.getType_vehicule());
            pstm.setString(2, vehicule.getModele());
            pstm.setString(3, vehicule.getRole() != null ? vehicule.getRole() : "");
            pstm.setString(4, vehicule.getPrix_par_heure());
            pstm.setString(5, vehicule.getPrix_par_jour());
            pstm.setString(6, vehicule.getDisponibilite());
            pstm.setString(7, vehicule.getLieu_retrait());
            pstm.setString(8, vehicule.getImageUrl() != null ? vehicule.getImageUrl() : "");
            pstm.setString(9, "");
            pstm.setInt(10, 0);
            pstm.setInt(11, 0);

            int rowsAffected = pstm.executeUpdate();
            if (rowsAffected > 0) {
                cnx.commit();
            } else {
                cnx.rollback();
            }
        } catch (SQLException e) {
            try {
                cnx.rollback();
            } catch (SQLException ex) {
                System.out.println("Erreur lors du rollback: " + ex.getMessage());
            }
            System.out.println("Erreur SQL: " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'ajout du véhicule", e);
        } finally {
            try {
                cnx.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Erreur lors de la réactivation de l'auto-commit: " + e.getMessage());
            }
        }
    }

    @Override
    public List<Vehicule> getAll() {
        List<Vehicule> vehicules = new ArrayList<>();
        String qry = "SELECT * FROM `vehicule`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                Vehicule v = new Vehicule();
                v.setId(rs.getInt("id"));
                v.setType_vehicule(rs.getString("type_vehicule"));
                v.setModele(rs.getString("modele"));
                v.setRole(rs.getString("role"));
                v.setPrix_par_heure(rs.getString("prix_par_heure"));
                v.setPrix_par_jour(rs.getString("prix_par_jour"));
                v.setDisponibilite(rs.getString("disponibilite"));
                v.setLieu_retrait(rs.getString("lieu_retrait"));
                v.setImageUrl(rs.getString("image"));
                vehicules.add(v);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return vehicules;
    }

    @Override
    public void update(Vehicule vehicule) {
        String qry = "UPDATE `vehicule` SET `type_vehicule`=?, `modele`=?, `role`=?, `prix_par_heure`=?, `prix_par_jour`=?, `disponibilite`=?, `lieu_retrait`=?, `image`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, vehicule.getType_vehicule());
            pstm.setString(2, vehicule.getModele());
            pstm.setString(3, vehicule.getRole());
            pstm.setString(4, vehicule.getPrix_par_heure());
            pstm.setString(5, vehicule.getPrix_par_jour());
            pstm.setString(6, vehicule.getDisponibilite());
            pstm.setString(7, vehicule.getLieu_retrait());
            pstm.setString(8, vehicule.getImageUrl() != null ? vehicule.getImageUrl() : "");
            pstm.setInt(9, vehicule.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Erreur lors de la mise à jour du véhicule", e);
        }
    }

    @Override
    public void delete(Vehicule vehicule) {
        try {
            // D'abord supprimer les réservations associées
            String deleteReservationsQuery = "DELETE FROM `res_veh` WHERE `id_vehicule` = ?";
            PreparedStatement deleteReservationsStmt = cnx.prepareStatement(deleteReservationsQuery);
            deleteReservationsStmt.setInt(1, vehicule.getId());
            deleteReservationsStmt.executeUpdate();

            // Ensuite supprimer le véhicule
            String deleteVehicleQuery = "DELETE FROM `vehicule` WHERE `id` = ?";
            PreparedStatement deleteVehicleStmt = cnx.prepareStatement(deleteVehicleQuery);
            deleteVehicleStmt.setInt(1, vehicule.getId());
            deleteVehicleStmt.executeUpdate();

        } catch(SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Erreur lors de la suppression du véhicule", e);
        }
    }
}