package tn.esprit.models;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import java.io.FileOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.net.URL;

public class PDFGenerator1 {
    public static void generateReservationPDF(ReserverEvent reservation) throws Exception {
        Document document = new Document(PageSize.A4, 30, 30, 50, 50);
        String fileName = "Reservation_" + reservation.getId() + ".pdf";
        PdfWriter.getInstance(document, new FileOutputStream(fileName));

        document.open();

        // Couleurs personnalisées
        BaseColor primaryColor = new BaseColor(0, 92, 75); // Vert foncé
        BaseColor secondaryColor = new BaseColor(0, 172, 193); // Turquoise
        BaseColor accentColor = new BaseColor(255, 193, 7); // Or

        // Police personnalisée
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, primaryColor);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, secondaryColor);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font thankYouFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 14, BaseColor.GRAY);

        // Ajouter un logo ou une image d'en-tête
        try {
            Image headerImage = Image.getInstance(PDFGenerator.class.getResource("/images/logo1.png"));
            headerImage.scaleToFit(150, 80);
            headerImage.setAlignment(Element.ALIGN_CENTER);
            document.add(headerImage);
        } catch (Exception e) {
            System.err.println("Logo non trouvé: " + e.getMessage());
            e.printStackTrace();
        }

        // Titre principal
        Paragraph title = new Paragraph("Confirmation de Réservation", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Image de l'événement (centrée et plus grande)
        if (reservation.getEventImageUrl() != null && !reservation.getEventImageUrl().isEmpty()) {
            try {
                System.out.println("Chargement de l'image de l'événement: " + reservation.getEventImageUrl());
                Image eventImage;

                if (reservation.getEventImageUrl().startsWith("http")) {
                    eventImage = Image.getInstance(new URL(reservation.getEventImageUrl()));
                } else {
                    File imageFile = new File(reservation.getEventImageUrl());
                    System.out.println("Chemin absolu de l'image: " + imageFile.getAbsolutePath());
                    if (!imageFile.exists()) {
                        throw new Exception("Fichier image introuvable");
                    }
                    eventImage = Image.getInstance(imageFile.getAbsolutePath());
                }

                // Ajustement de la taille de l'image
                eventImage.scaleToFit(400, 250); // Taille augmentée
                eventImage.setAlignment(Element.ALIGN_CENTER);
                eventImage.setSpacingBefore(10);
                eventImage.setSpacingAfter(20);
                document.add(eventImage);

            } catch (Exception e) {
                System.err.println("Erreur chargement image événement: " + e.getMessage());
                e.printStackTrace();

                try {
                    // Image par défaut si l'image spécifique n'est pas trouvée
                    Image defaultImage = Image.getInstance(PDFGenerator.class.getResource("/images/default_event.png"));
                    defaultImage.scaleToFit(400, 250);
                    defaultImage.setAlignment(Element.ALIGN_CENTER);
                    document.add(defaultImage);
                } catch (Exception ex) {
                    System.err.println("Image par défaut non trouvée: " + ex.getMessage());

                    // Ajout d'un message textuel si aucune image n'est disponible
                    Paragraph imageError = new Paragraph("Image de l'événement non disponible",
                            new Font(FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.RED)));
                    imageError.setAlignment(Element.ALIGN_CENTER);
                    document.add(imageError);
                }
            }
        } else {
            // Message si aucune URL d'image n'est fournie
            Paragraph noImage = new Paragraph("Aucune image disponible pour cet événement",
                    new Font(FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.GRAY)));
            noImage.setAlignment(Element.ALIGN_CENTER);
            document.add(noImage);
        }

        // Tableau des détails
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80);
        table.setSpacingBefore(20);
        table.setSpacingAfter(20);

        // Style des cellules
        PdfPCell cell;

        // En-tête du tableau
        cell = new PdfPCell(new Phrase("Détails de la Réservation", subtitleFont));
        cell.setColspan(2);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new BaseColor(240, 240, 240));
        cell.setPadding(10);
        table.addCell(cell);

        // Contenu du tableau
        addTableRow(table, "Événement:", reservation.getEventTitle(), normalFont);
        addTableRow(table, "Date:", new SimpleDateFormat("dd/MM/yyyy").format(reservation.getBookingDate()), normalFont);
        addTableRow(table, "Heure:", new SimpleDateFormat("HH:mm").format(reservation.getBookingDate()), normalFont);
        addTableRow(table, "Quantité:", String.valueOf(reservation.getQuantity()), normalFont);
        addTableRow(table, "Prix unitaire:", String.format("%.2f TND", reservation.getTotalAmount()/reservation.getQuantity()), normalFont);
        addTableRow(table, "Montant total:", String.format("%.2f TND", reservation.getTotalAmount()), normalFont);

        // Style pour le montant total
        cell = new PdfPCell(new Phrase("Montant total:", normalFont));
        cell.setBackgroundColor(new BaseColor(220, 220, 220));
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(String.format("%.2f TND", reservation.getTotalAmount()),
                new Font(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
        cell.setBackgroundColor(new BaseColor(220, 220, 220));
        cell.setPadding(5);
        table.addCell(cell);

        document.add(table);

        // Code de réservation simple (remplace le QR code)
        Paragraph reservationCode = new Paragraph("\nCode de réservation: RES-" + reservation.getId(),
                new Font(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, primaryColor)));
        reservationCode.setAlignment(Element.ALIGN_CENTER);
        reservationCode.setSpacingBefore(15);
        document.add(reservationCode);

        // Instructions pour l'entrée
        Paragraph instructions = new Paragraph("Présentez ce code avec une pièce d'identité à l'entrée de l'événement",
                new Font(FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY)));
        instructions.setAlignment(Element.ALIGN_CENTER);
        instructions.setSpacingAfter(20);
        document.add(instructions);

        // Message de remerciement
        Paragraph thankYou = new Paragraph("\n\nMerci pour votre réservation ! Nous avons hâte de vous voir à l'événement.", thankYouFont);
        thankYou.setAlignment(Element.ALIGN_CENTER);
        thankYou.setSpacingBefore(20);
        document.add(thankYou);

        // Pied de page
        Paragraph footer = new Paragraph("\n\nPour toute question, contactez-nous à contact@votreevenement.com",
                new Font(FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY)));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
    }

    private static void addTableRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(label, new Font(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(value, font));
        cell.setPadding(5);
        table.addCell(cell);
    }
}