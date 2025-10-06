package tn.esprit.models;

public class User {
    private int id;
    private String first_name, last_name, email, telephone, password_hash;
    private boolean verified;
    public String role;
    public String vehicule;

    public User() {}

    public User(int id, String email, String password_hash, String role, boolean verified,
                String first_name, String last_name, String telephone, String vehicule) {
        this.id = id;
        this.email = email;
        this.password_hash = password_hash;
        this.role = role;
        this.verified = verified;
        this.first_name = first_name;
        this.last_name = last_name;
        this.telephone = telephone;
        this.vehicule = vehicule;
    }

    public User(String email, String password_hash, String role, boolean verified,
                String first_name, String last_name, String telephone, String vehicule) {
        this.email = email;
        this.password_hash = password_hash;
        this.role = role;
        this.verified = verified;
        this.first_name = first_name;
        this.last_name = last_name;
        this.telephone = telephone;
        this.vehicule = vehicule;
    }
    public User(String first_name, String last_name, String email, String telephone, String password_hash, String vehicule) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.telephone = telephone;
        this.password_hash = password_hash;
        this.vehicule = vehicule;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getPassword_hash() {
        return password_hash;
    }

    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getVehicule() {
        return vehicule;
    }

    public void setVehicule(String vehicule) {
        this.vehicule = vehicule;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", password_hash='" + password_hash + '\'' +
                ", verified=" + verified +
                ", role='" + role + '\'' +
                ", vehicule='" + vehicule + '\'' +
                '}';
    }
}
