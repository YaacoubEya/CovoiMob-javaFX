package tn.esprit.test;

import tn.esprit.models.Reservation;
import tn.esprit.models.DemandeCovoiturage;
import tn.esprit.models.OffreCovoiturage;
import tn.esprit.models.PropositionCovoiturage;
import tn.esprit.services.ServiceReservation;
import tn.esprit.services.ServiceDemandeCovoiturage;
import tn.esprit.services.ServiceOffreCovoiturage;
import tn.esprit.services.ServicePropositionCovoiturage;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) throws SQLException {
        ServiceReservation sr = new ServiceReservation();
        ServiceDemandeCovoiturage sd = new ServiceDemandeCovoiturage();
        ServiceOffreCovoiturage so = new ServiceOffreCovoiturage();
        ServicePropositionCovoiturage sp = new ServicePropositionCovoiturage();
        ServicePropositionCovoiturage spp = new ServicePropositionCovoiturage();


        //  OffreCovoiturage
        // Ajouter une offre
        OffreCovoiturage offre = new OffreCovoiturage("Tunis", 1, "Lyon", 123456, 4, LocalDateTime.now().plusDays(1), "En attente", 50.0f, "car.jpg");
        so.add(offre);

        // Read
        System.out.println("Liste des offres : " + so.getAll());
        // Update
        so.update(so.getById(42));
        // Delete
        //so.delete(so.getById(41));
        // System.out.println("Offre supprimée. Liste des offres : " + so.getAll());

        //  Reservation
        // Ajouter une réservation
        Reservation res = new Reservation(2, "Confirmée", offre);
        sr.add(res);
        // Read
        System.out.println("Liste des réservations : " + sr.getAll());

        // Update
        //sr.update(sr.getById(62));

        // Delete
        // sr.delete(sr.getById(41));

        //  DemandeCovoiturage
        // Ajouter une demande
        DemandeCovoiturage demande = new DemandeCovoiturage(3, "Tunis", "Lyon", LocalDateTime.now().plusDays(2), "En attente", 40.0f);
        sd.add(demande);

        // Read
        System.out.println("Liste des demandes : " + sd.getAll());

        // Update

        sd.update(sd.getById(55));

        // Delete
        // sd.delete(sd.getById(55));
        // System.out.println("Demande supprimée. Liste des demandes : " + sd.getAll());

        //  PropositionCovoiturage

        // Ajouter une proposition
       PropositionCovoiturage prop = new PropositionCovoiturage(4, "Proposée", demande, 123);
        sp.add(prop);


        // Read
        System.out.println("Liste des propositions : " + sp.getAll());

        // Update

        //sp.update(sp.getById(79));

        // Delete
        // sp.delete(sd.getById(79));
        // System.out.println("Proposition supprimée. Liste des propositions : " + sp.getAll());

        // Afficher toutes les données finales
        System.out.println("\n=== Données finales ===");
        System.out.println("Réservations : " + sr.getAll());
        System.out.println("Demandes : " + sd.getAll());
        System.out.println("Offres : " + so.getAll());
        System.out.println("Propositions : " + sp.getAll());
    }
}