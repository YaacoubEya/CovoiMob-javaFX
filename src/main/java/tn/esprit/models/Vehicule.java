package tn.esprit.models;

public class Vehicule {
    private int id;
    private String type_vehicule;
    private String modele;
    private String role;
    private String prix_par_heure;
    private String prix_par_jour;
    private String disponibilite;
    private String lieu_retrait;
    private String imageUrl;

    public Vehicule() {
    }

    public Vehicule(int id, String type_vehicule, String modele, String role, String prix_par_heure, String prix_par_jour, String disponibilite, String lieu_retrait) {
        this.id = id;
        this.type_vehicule = type_vehicule;
        this.modele = modele;
        this.role = role;
        this.prix_par_heure = prix_par_heure;
        this.prix_par_jour = prix_par_jour;
        this.disponibilite = disponibilite;
        this.lieu_retrait = lieu_retrait;
    }
    public Vehicule(String type_vehicule, String modele, String role, String prix_par_heure,
                    String prix_par_jour, String disponibilite, String lieu_retrait, String imageUrl) {
        this.type_vehicule = type_vehicule;
        this.modele = modele;
        this.role = role;
        this.prix_par_heure = prix_par_heure;
        this.prix_par_jour = prix_par_jour;
        this.disponibilite = disponibilite;
        this.lieu_retrait = lieu_retrait;
        this.imageUrl = imageUrl;
    }
    public Vehicule( String type_vehicule, String modele, String role, String prix_par_heure, String prix_par_jour, String disponibilite, String lieu_retrait) {
        this.type_vehicule = type_vehicule;
        this.modele = modele;
        this.role = role;
        this.prix_par_heure = prix_par_heure;
        this.prix_par_jour = prix_par_jour;
        this.disponibilite = disponibilite;
        this.lieu_retrait = lieu_retrait;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType_vehicule() {
        return type_vehicule;
    }

    public void setType_vehicule(String type_vehicule) {
        this.type_vehicule = type_vehicule;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPrix_par_heure() {
        return prix_par_heure;
    }

    public void setPrix_par_heure(String prix_par_heure) {
        this.prix_par_heure = prix_par_heure;
    }

    public String getPrix_par_jour() {
        return prix_par_jour;
    }

    public void setPrix_par_jour(String prix_par_jour) {
        this.prix_par_jour = prix_par_jour;
    }

    public String getDisponibilite() {
        return disponibilite;
    }

    public void setDisponibilite(String disponibilite) {
        this.disponibilite = disponibilite;
    }

    public String getLieu_retrait() {
        return lieu_retrait;
    }

    public void setLieu_retrait(String lieu_retrait) {
        this.lieu_retrait = lieu_retrait;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    @Override
    public String toString() {
        return "Vehicule{" +
                "id=" + id +
                ", type_vehicule='" + type_vehicule + '\'' +
                ", modele='" + modele + '\'' +
                ", role='" + role + '\'' +
                ", prix_par_heure='" + prix_par_heure + '\'' +
                ", prix_par_jour='" + prix_par_jour + '\'' +
                ", disponibilite='" + disponibilite + '\'' +
                ", lieu_retrait='" + lieu_retrait + '\'' +
                "}\n";
    }
}