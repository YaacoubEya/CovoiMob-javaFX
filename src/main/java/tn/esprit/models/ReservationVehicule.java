package tn.esprit.models;
import java.time.LocalDate;
import java.util.Date;

public class ReservationVehicule {
    private int id;
    private int id_client;
    private int id_vehicule;
    private LocalDate date_debut;
    private LocalDate date_fin;
    private double prix_total; // Changé de String à double
    private String status;
    private String notifications;
    public ReservationVehicule() {}

    public ReservationVehicule(int id_client, int id_vehicule, LocalDate  date_debut,
                               LocalDate  date_fin, double prix_total, String status) {
        this.id_client = id_client;
        this.id_vehicule = id_vehicule;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.prix_total = prix_total;
        this.status = status;
    }


    // Getters et Setters
    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }
    public int getId_client()
    {
        return id_client;
    }
    public void setId_client(int id_client)
    {
        this.id_client = id_client;
    }
    public int getId_vehicule()
    {
        return id_vehicule;
    }
    public void setId_vehicule(int id_vehicule)
    {
        this.id_vehicule = id_vehicule;
    }
    public LocalDate getDate_debut() {
        return date_debut;
    }
    public void setDate_debut(LocalDate date_debut) {
        this.date_debut = date_debut;
    }
    public LocalDate getDate_fin() {
        return date_fin;
    }
    public void setDate_fin(LocalDate date_fin) {
        this.date_fin = date_fin;
    }
    public double getPrix_total()
    {
        return prix_total;
    }
    public void setPrix_total(double prix_total)
    {
        this.prix_total = prix_total;
    }
    public String getStatus()
    {
        return status;
    }
    public void setStatus(String status)
    {
        this.status = status;
    }
    public String getNotifications() {
        return notifications;
    }

    public void setNotifications(String notifications) {
        this.notifications = notifications;
    }
}