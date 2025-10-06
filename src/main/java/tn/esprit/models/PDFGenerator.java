package tn.esprit.models;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PDFGenerator {
    public static void generateVehiculePDF(Vehicule vehicule) throws Exception {
        // Création du document avec marges
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        String fileName = "Vehicule_" + vehicule.getId() + "_" + vehicule.getModele() + ".pdf";
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));

        // Ajout des métadonnées
        document.addTitle("Fiche Technique Véhicule - " + vehicule.getModele());
        document.addSubject("Détails du véhicule");
        document.addKeywords("véhicule, location, fiche technique");
        document.addCreator("Système de Location de Véhicules");

        document.open();

        // Définition des polices
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.DARK_GRAY);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new BaseColor(0, 105, 92));
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
        Font priceFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(220, 0, 0));
        Font signatureFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY);

        // Titre principal avec logo
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingAfter(10);

        // Logo de l'entreprise
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        try {
            Image logo = Image.getInstance("src/main/resources/images/logo.png");
            logo.scaleToFit(100, 60);
            logoCell.addElement(new Chunk(logo, 0, 0));
        } catch (Exception e) {
            logoCell.addElement(new Paragraph("COVOIMOB", subtitleFont));
        }
        headerTable.addCell(logoCell);

        // Titre
        PdfPCell titleCell = new PdfPCell(new Paragraph("Fiche Technique du Véhicule", titleFont));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(titleCell);

        document.add(headerTable);

        // Ligne de séparation
        Paragraph separator = new Paragraph();
        LineSeparator line = new LineSeparator();
        line.setLineWidth(1f);
        line.setLineColor(new BaseColor(0, 172, 193));
        separator.add(new Chunk(line));
        document.add(separator);

        // Tableau principal (image + infos)
        PdfPTable mainTable = new PdfPTable(2);
        mainTable.setWidthPercentage(100);
        mainTable.setSpacingBefore(10);
        mainTable.setSpacingAfter(20);

        // Cellule pour l'image du véhicule
        PdfPCell imageCell = new PdfPCell();
        imageCell.setBorder(Rectangle.NO_BORDER);
        imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        try {
            String imagePath = vehicule.getImageUrl();
            if (imagePath != null && !imagePath.isEmpty()) {
                Image vehiculeImage;
                if (imagePath.startsWith("/")) {
                    vehiculeImage = Image.getInstance("src/main/resources" + imagePath);
                } else {
                    vehiculeImage = Image.getInstance(imagePath);
                }
                vehiculeImage.scaleToFit(250, 180);
                imageCell.addElement(vehiculeImage);
            } else {
                imageCell.addElement(new Paragraph("Aucune image disponible", normalFont));
            }
        } catch (Exception e) {
            imageCell.addElement(new Paragraph("Erreur de chargement de l'image", normalFont));
        }
        mainTable.addCell(imageCell);

        // Cellule pour les informations du véhicule
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setPaddingLeft(15);

        // Titre du véhicule
        Paragraph vehiculeTitle = new Paragraph(
                vehicule.getType_vehicule() + " " + vehicule.getModele(), subtitleFont);
        vehiculeTitle.setSpacingAfter(15);
        infoCell.addElement(vehiculeTitle);

        // Tableau des caractéristiques
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(90);

        // Ajout des informations
        addInfoRow(infoTable, "Type:", vehicule.getType_vehicule(), normalFont);
        addInfoRow(infoTable, "Modèle:", vehicule.getModele(), normalFont);
        addInfoRow(infoTable, "Prix par heure:", vehicule.getPrix_par_heure() + " DT", priceFont);
        addInfoRow(infoTable, "Prix par jour:", vehicule.getPrix_par_jour() + " DT", priceFont);
        addInfoRow(infoTable, "Disponibilité:",
                vehicule.getDisponibilite() != null ? vehicule.getDisponibilite() : "Disponible", normalFont);
        addInfoRow(infoTable, "Lieu de retrait:", vehicule.getLieu_retrait(), normalFont);

        infoCell.addElement(infoTable);
        mainTable.addCell(infoCell);

        document.add(mainTable);

        // Section des détails supplémentaires
        Paragraph detailsTitle = new Paragraph("Informations Complémentaires", subtitleFont);
        detailsTitle.setSpacingBefore(20);
        document.add(detailsTitle);

        // Liste à puces
        List detailsList = new List(List.UNORDERED);
        detailsList.setListSymbol("\u2022");
        detailsList.add(new ListItem("Véhicule contrôlé et entretenu régulièrement", normalFont));
        detailsList.add(new ListItem("Assurance tous risques incluse", normalFont));
        detailsList.add(new ListItem("Kilométrage illimité pour les locations longue durée", normalFont));
        detailsList.add(new ListItem("Service client disponible 24/7", normalFont));
        document.add(detailsList);





        // Zone de signature
        PdfPTable signatureTable = new PdfPTable(2);
        signatureTable.setWidthPercentage(100);
        signatureTable.setSpacingBefore(30);

        // Cellule vide pour alignement
        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(Rectangle.NO_BORDER);
        signatureTable.addCell(emptyCell);

        // Cellule de signature
        PdfPCell signatureCell = new PdfPCell();
        signatureCell.setBorder(Rectangle.TOP);
        signatureCell.setBorderColor(BaseColor.LIGHT_GRAY);
        signatureCell.setPaddingTop(10);
        signatureCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph signatureParagraph = new Paragraph();
        signatureParagraph.add(new Chunk("Pour l'équipe de Location Véhicule\n", normalFont));
        signatureParagraph.add(new Chunk("Signature", signatureFont));

        signatureCell.addElement(signatureParagraph);
        signatureTable.addCell(signatureCell);

        document.add(signatureTable);

        // Pied de page
        Paragraph footer = new Paragraph(
                "Document généré le " + new SimpleDateFormat("dd/MM/yyyy à HH:mm").format(new Date()) +
                        " - © " + new SimpleDateFormat("yyyy").format(new Date()) + " Location Véhicule Tous droits réservés",
                FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);

        // Numéro de page
        ColumnText.showTextAligned(
                writer.getDirectContent(),
                Element.ALIGN_CENTER,
                new Phrase(String.format("Page %d", writer.getPageNumber())),
                (document.right() - document.left()) / 2 + document.leftMargin(),
                document.bottom() - 10,
                0);

        document.close();
    }

    // Méthode utilitaire pour ajouter une ligne d'information au tableau
    private static void addInfoRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(8);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(8);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}