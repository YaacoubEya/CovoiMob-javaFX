package tn.esprit.models;
import java.util.Date;
import java.util.List;

public class ProposerEvent {
    private int id;
    private String title;
    private String description;
    private String location;
    private Date startDate;
    private Date endDate;
    private String eventType;
    private double price;
    private String status;
    private int userId;
    private String imageUrl;
    private List<ReserverEvent> reservations;

    public ProposerEvent() {
    }

    public ProposerEvent(int id, String title, String description, String location, Date startDate, Date endDate, String eventType, double price, String status, int userId, String imageUrl, List<ReserverEvent> reservations) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.eventType = eventType;
        this.price = price;
        this.status = status;
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.reservations = reservations;
    }

    public ProposerEvent(String title, String description, String location, Date startDate, Date endDate, String eventType, double price, int userId, String imageUrl) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.eventType = eventType;
        this.price = price;
        this.status = "ACTIVE";
        this.userId = userId;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<ReserverEvent> getReservations() {
        return reservations;
    }

    public void setReservations(List<ReserverEvent> reservations) {
        this.reservations = reservations;
    }
    private int reservationsCount;

    public int getReservationsCount() {
        return reservationsCount;
    }

    public void setReservationsCount(int reservationsCount) {
        this.reservationsCount = reservationsCount;
    }

    public String getDummy() {
        return "";
    }

    @Override
    public String toString() {
        return "ProposerEvent{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' + // Ajouté
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", eventType='" + eventType + '\'' +
                ", price=" + price +
                ", status='" + status + '\'' +
                ", userId=" + userId +
                ", imageUrl='" + imageUrl + '\'' +
                ", reservations=" + reservations +
                "}\n";
    }
}