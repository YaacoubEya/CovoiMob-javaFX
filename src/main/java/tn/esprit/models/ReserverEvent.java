package tn.esprit.models;

import java.util.Date;

public class ReserverEvent {
    private int id;
    private Date bookingDate;
    private int quantity;
    private double totalAmount;
    private String status;
    private int userId;
    private String eventTitle;
    private String eventImageUrl;
    private Date eventDate;

    public ReserverEvent() {
    }

    public ReserverEvent(int quantity, double totalAmount, int userId,
                         String eventTitle, String eventImageUrl, Date eventDate) {
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.userId = userId;
        this.eventTitle = eventTitle;
        this.eventImageUrl = eventImageUrl;
        this.eventDate = eventDate;
        this.bookingDate = new Date(); // Date actuelle pour la réservation
        this.status = "Confirmée"; // Valeur par défaut
    }

    public ReserverEvent(int quantity, double totalAmount, int userId, String eventTitle, String eventImageUrl) {
        this.bookingDate = new Date();
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.userId = userId;
        this.eventTitle = eventTitle;
        this.eventImageUrl = eventImageUrl;
        this.eventDate = eventDate;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }
    public String getEventImageUrl() {
        return eventImageUrl;
    }

    public void setEventImageUrl(String eventImageUrl) {
        this.eventImageUrl = eventImageUrl;
    }
    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    @Override
    public String toString() {
        return "ReserveEvent{" +
                "id=" + id +
                ", bookingDate=" + bookingDate +
                ", quantity=" + quantity +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", userId=" + userId +
                ", eventTitle='" + eventTitle + '\'' +
                ", eventImageUrl='" + eventImageUrl + '\'' +
                "}\n";
    }
}