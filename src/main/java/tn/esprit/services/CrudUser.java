package tn.esprit.services;

import interfaces.IServiceCrud;
import tn.esprit.models.User;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CrudUser implements IServiceCrud<User> {
    private static final Logger LOGGER = Logger.getLogger(CrudUser.class.getName());
    private final Connection conn;

    public CrudUser() {
        conn = MyDataBase.getInstance().getCnx();
        if (conn == null) {
            LOGGER.severe("Échec de l'initialisation de la connexion à la base de données");
            throw new IllegalStateException("Connexion à la base de données non initialisée");
        }
    }

    @Override
    public User getById(int id) {
        String query = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Erreur lors de la récupération de l'utilisateur ID " + id + ": " + e.getMessage());
            throw new RuntimeException("Échec de la récupération de l'utilisateur", e);
        }
        return null;
    }

    @Override
    public void add(User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("L'email est requis");
        }
        if (user.getPassword_hash() == null || user.getPassword_hash().isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe haché est requis");
        }

        // Optional: Validate email format
    /* if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
        throw new IllegalArgumentException("Format d'email invalide");
    } */

        String qry = "INSERT INTO `user` (`email`, `password_hash`, `first_name`, `last_name`, `telephone`, `vehicule`) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getPassword_hash());
            statement.setString(3, user.getFirst_name());
            statement.setString(4, user.getLast_name());
            statement.setString(5, user.getTelephone());
            statement.setString(6, user.getVehicule());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                        System.out.println("✅ Utilisateur '" + user.getEmail() + "' ajouté avec ID " + user.getId());
                    }
                }
            } else {
                LOGGER.warning("Aucun utilisateur n'a été ajouté pour l'email : " + user.getEmail());
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000") && e.getErrorCode() == 1062) {
                throw new RuntimeException("L'email " + user.getEmail() + " est déjà utilisé", e);
            }
            LOGGER.severe("Erreur lors de l'ajout de l'utilisateur : " + e.getMessage());
            throw new RuntimeException("Échec de l'ajout de l'utilisateur", e);
        }
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM `user`";
        try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.severe("Erreur lors de la récupération des utilisateurs : " + e.getMessage());
            throw new RuntimeException("Échec de la récupération des utilisateurs", e);
        }
        return users;
    }

    @Override
    public void update(User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("L'email est requis");
        }
        if (user.getRole() == null || user.getRole().isEmpty()) {
            throw new IllegalArgumentException("Le rôle est requis");
        }
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Format d'email invalide");
        }

        String qry = "UPDATE `user` SET `email`=?, `password_hash`=?, `role`=?, `verified`=?, `first_name`=?, `last_name`=?, `telephone`=?, `vehicule`=? WHERE `id`=?";
        try (PreparedStatement statement = conn.prepareStatement(qry)) {
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getPassword_hash());
            statement.setString(3, user.getRole());
            statement.setBoolean(4, user.isVerified());
            statement.setString(5, user.getFirst_name());
            statement.setString(6, user.getLast_name());
            statement.setString(7, user.getTelephone());
            statement.setString(8, user.getVehicule());
            statement.setInt(9, user.getId());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Utilisateur ID " + user.getId() + " mis à jour avec succès.");
            } else {
                LOGGER.warning("Aucun utilisateur trouvé avec l'ID : " + user.getId());
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000") && e.getErrorCode() == 1062) {
                throw new RuntimeException("L'email " + user.getEmail() + " est déjà utilisé", e);
            }
            LOGGER.severe("Erreur lors de la mise à jour de l'utilisateur : " + e.getMessage());
            throw new RuntimeException("Échec de la mise à jour de l'utilisateur", e);
        }
    }

    @Override
    public void delete(int id) {
        String qry = "DELETE FROM `user` WHERE `id`=?";
        try (PreparedStatement statement = conn.prepareStatement(qry)) {
            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("🗑️ Utilisateur ID " + id + " supprimé avec succès.");
            } else {
                LOGGER.warning("Aucun utilisateur trouvé avec l'ID : " + id);
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000") && e.getErrorCode() == 1451) {
                throw new RuntimeException("Impossible de supprimer l'utilisateur ID " + id + " car il est référencé ailleurs", e);
            }
            LOGGER.severe("Erreur lors de la suppression de l'utilisateur : " + e.getMessage());
            throw new RuntimeException("Échec de la suppression de l'utilisateur", e);
        }
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String email = rs.getString("email");
        String password_hash = rs.getString("password_hash");
        String role = rs.getString("role");
        boolean verified = rs.getBoolean("verified");
        String first_name = rs.getString("first_name");
        String last_name = rs.getString("last_name");
        String telephone = rs.getString("telephone");
        String vehicule = rs.getString("vehicule");

        // Use setters instead of constructor to avoid dependency on specific constructor
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPassword_hash(password_hash);
        user.setRole(role);
        user.setVerified(verified);
        user.setFirst_name(first_name);
        user.setLast_name(last_name);
        user.setTelephone(telephone);
        user.setVehicule(vehicule);
        return user;
    }

    public User findByEmailOrPhone(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("L'entrée pour la recherche est requise");
        }
        String sql = "SELECT * FROM user WHERE email = ? OR telephone = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, input);
            stmt.setString(2, input);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Erreur lors de la recherche de l'utilisateur pour input " + input + ": " + e.getMessage());
            throw new RuntimeException("Échec de la recherche de l'utilisateur", e);
        }
        return null;
    }
}