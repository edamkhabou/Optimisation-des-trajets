package com.covoiturage.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.covoiturage.models.Conflit;
import com.covoiturage.models.Trajet;
import com.covoiturage.models.Utilisateur;
import com.covoiturage.models.Vehicule;

/**
 * Service de détection des conflits dans les trajets de covoiturage.
 * 
 * Détecte :
 * - Dépassement de capacité des véhicules
 * - Conflits d'horaires entre utilisateurs
 * - Incompatibilités de préférences
 * - Disponibilité des véhicules
 */
public class ConflitService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConflitService.class);
    
    /**
     * Détecte tous les conflits possibles pour un trajet donné.
     * 
     * @param trajet Le trajet à vérifier
     * @return Liste des conflits détectés
     */
    public List<Conflit> detecterConflits(Trajet trajet) {
        List<Conflit> conflits = new ArrayList<>();
        
        if (trajet == null || trajet.getVehicule() == null) {
            logger.error("Trajet ou véhicule null");
            return conflits;
        }
        
        logger.info("Détection de conflits pour le trajet ID: {}", trajet.getId());
        
        // 1. Vérifier la capacité du véhicule
        Conflit conflitCapacite = verifierCapacite(trajet);
        if (conflitCapacite != null) {
            conflits.add(conflitCapacite);
        }
        
        // 2. Vérifier la disponibilité du véhicule
        Conflit conflitDisponibilite = verifierDisponibilite(trajet);
        if (conflitDisponibilite != null) {
            conflits.add(conflitDisponibilite);
        }
        
        // 3. Vérifier les conflits d'horaires entre utilisateurs
        List<Conflit> conflitsHoraires = verifierHoraires(trajet);
        conflits.addAll(conflitsHoraires);
        
        // 4. Vérifier les préférences utilisateurs
        List<Conflit> conflitsPreferences = verifierPreferences(trajet);
        conflits.addAll(conflitsPreferences);
        
        logger.info("{} conflit(s) détecté(s)", conflits.size());
        return conflits;
    }
    
    /**
     * Vérifie si la capacité du véhicule est dépassée.
     * 
     * @param trajet Le trajet à vérifier
     * @return Un conflit si la capacité est dépassée, null sinon
     */
    private Conflit verifierCapacite(Trajet trajet) {
        Vehicule vehicule = trajet.getVehicule();
        int nombrePassagers = trajet.getUtilisateurs().size();
        int capacite = vehicule.getCapacite();
        
        if (nombrePassagers > capacite) {
            Conflit conflit = new Conflit();
            conflit.setType(Conflit.TypeConflit.CAPACITE);
            conflit.setMessage(String.format(
                "Capacité du véhicule dépassée: %d passagers pour %d places disponibles",
                nombrePassagers, capacite
            ));
            conflit.setTrajetId(trajet.getId());
            conflit.setVehiculeId(vehicule.getId());
            conflit.setDetails(String.format(
                "Véhicule %s (capacité: %d), Passagers: %d, Dépassement: %d",
                vehicule.getImmatriculation(), capacite, nombrePassagers, 
                nombrePassagers - capacite
            ));
            
            logger.warn("Conflit de capacité détecté: {}", conflit.getMessage());
            return conflit;
        }
        
        return null;
    }
    
    /**
     * Vérifie la disponibilité du véhicule aux horaires requis.
     * 
     * @param trajet Le trajet à vérifier
     * @return Un conflit si le véhicule n'est pas disponible, null sinon
     */
    private Conflit verifierDisponibilite(Trajet trajet) {
        Vehicule vehicule = trajet.getVehicule();
        
        if (!vehicule.isDisponible()) {
            Conflit conflit = new Conflit();
            conflit.setType(Conflit.TypeConflit.DISPONIBILITE);
            conflit.setMessage(String.format(
                "Le véhicule %s n'est pas disponible",
                vehicule.getImmatriculation()
            ));
            conflit.setTrajetId(trajet.getId());
            conflit.setVehiculeId(vehicule.getId());
            
            logger.warn("Conflit de disponibilité: {}", conflit.getMessage());
            return conflit;
        }
        
        // Vérifier les horaires de disponibilité
        if (vehicule.getHeureDebutDisponibilite() != null && 
            vehicule.getHeureFinDisponibilite() != null &&
            !trajet.getUtilisateurs().isEmpty()) {
            
            // Vérifier si tous les utilisateurs peuvent être pris en charge dans la plage horaire
            for (Utilisateur utilisateur : trajet.getUtilisateurs()) {
                if (utilisateur.getHeureDepart() != null) {
                    if (!vehicule.disponibleA(utilisateur.getHeureDepart())) {
                        Conflit conflit = new Conflit();
                        conflit.setType(Conflit.TypeConflit.DISPONIBILITE);
                        conflit.setMessage(String.format(
                            "Le véhicule %s n'est pas disponible à %s pour l'utilisateur %s",
                            vehicule.getImmatriculation(), 
                            utilisateur.getHeureDepart(),
                            utilisateur.getNom()
                        ));
                        conflit.setTrajetId(trajet.getId());
                        conflit.setVehiculeId(vehicule.getId());
                        conflit.setUtilisateurId(utilisateur.getId());
                        
                        logger.warn("Conflit de disponibilité horaire: {}", conflit.getMessage());
                        return conflit;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Vérifie les conflits d'horaires entre utilisateurs.
     * 
     * @param trajet Le trajet à vérifier
     * @return Liste des conflits d'horaires
     */
    private List<Conflit> verifierHoraires(Trajet trajet) {
        List<Conflit> conflits = new ArrayList<>();
        List<Utilisateur> utilisateurs = trajet.getUtilisateurs();
        
        // Comparer chaque paire d'utilisateurs
        for (int i = 0; i < utilisateurs.size(); i++) {
            for (int j = i + 1; j < utilisateurs.size(); j++) {
                Utilisateur u1 = utilisateurs.get(i);
                Utilisateur u2 = utilisateurs.get(j);
                
                // Vérifier si les horaires sont compatibles
                if (!u1.horairesCompatibles(u2)) {
                    Conflit conflit = new Conflit();
                    conflit.setType(Conflit.TypeConflit.HORAIRE);
                    conflit.setMessage(String.format(
                        "Conflit d'horaires entre %s et %s",
                        u1.getNom(), u2.getNom()
                    ));
                    conflit.setTrajetId(trajet.getId());
                    conflit.setDetails(String.format(
                        "%s: %s-%s | %s: %s-%s",
                        u1.getNom(), u1.getHeureDepart(), u1.getHeureArrivee(),
                        u2.getNom(), u2.getHeureDepart(), u2.getHeureArrivee()
                    ));
                    
                    logger.warn("Conflit d'horaires: {}", conflit.getMessage());
                    conflits.add(conflit);
                }
            }
        }
        
        return conflits;
    }
    
    /**
     * Vérifie les conflits liés aux préférences utilisateurs.
     * 
     * Par exemple :
     * - Utilisateurs qui préfèrent voyager avec leur groupe
     * - Utilisateurs qui ont des restrictions spécifiques
     * 
     * @param trajet Le trajet à vérifier
     * @return Liste des conflits de préférences
     */
    private List<Conflit> verifierPreferences(Trajet trajet) {
        List<Conflit> conflits = new ArrayList<>();
        List<Utilisateur> utilisateurs = trajet.getUtilisateurs();
        
        // Vérifier si des utilisateurs ont une préférence pour le même groupe
        // mais sont mélangés avec d'autres groupes
        for (Utilisateur utilisateur : utilisateurs) {
            if (utilisateur.getGroupe() != null && !utilisateur.getGroupe().isEmpty()) {
                // Compter combien d'utilisateurs du même groupe
                long memeGroupe = utilisateurs.stream()
                    .filter(u -> u.getGroupe() != null && u.getGroupe().equals(utilisateur.getGroupe()))
                    .count();
                
                // Si l'utilisateur préfère son groupe mais est mélangé
                if (memeGroupe > 0 && memeGroupe < utilisateurs.size()) {
                    boolean preferenceGroupe = verifierPreferenceGroupe(utilisateur);
                    
                    if (preferenceGroupe) {
                        Conflit conflit = new Conflit();
                        conflit.setType(Conflit.TypeConflit.PREFERENCE);
                        conflit.setMessage(String.format(
                            "L'utilisateur %s préfère voyager uniquement avec son groupe %s",
                            utilisateur.getNom(), utilisateur.getGroupe()
                        ));
                        conflit.setTrajetId(trajet.getId());
                        conflit.setUtilisateurId(utilisateur.getId());
                        
                        logger.info("Conflit de préférence (groupe): {}", conflit.getMessage());
                        conflits.add(conflit);
                        break; // Un seul conflit suffit pour ce type
                    }
                }
            }
        }
        
        return conflits;
    }
    
    /**
     * Vérifie si un utilisateur a une préférence stricte pour son groupe.
     * 
     * @param utilisateur L'utilisateur
     * @return true si préférence stricte, false sinon
     */
    private boolean verifierPreferenceGroupe(Utilisateur utilisateur) {
        // Analyser les préférences JSON (simplifié ici)
        // Dans une vraie implémentation, parser le JSON des préférences
        String preferences = utilisateur.getPreferences();
        
        if (preferences != null && preferences.contains("\"priorite\":true")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Vérifie si un trajet est valide (sans conflit bloquant).
     * 
     * @param trajet Le trajet à vérifier
     * @return true si valide, false si conflits bloquants
     */
    public boolean estValide(Trajet trajet) {
        List<Conflit> conflits = detecterConflits(trajet);
        
        // Les conflits de capacité et de disponibilité sont bloquants
        for (Conflit conflit : conflits) {
            if (conflit.getType() == Conflit.TypeConflit.CAPACITE ||
                conflit.getType() == Conflit.TypeConflit.DISPONIBILITE) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Obtient un résumé des conflits.
     * 
     * @param conflits Liste des conflits
     * @return Message résumé
     */
    public String genererResume(List<Conflit> conflits) {
        if (conflits.isEmpty()) {
            return "Aucun conflit détecté. Le trajet est valide.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("⚠️ %d conflit(s) détecté(s):\n\n", conflits.size()));
        
        for (int i = 0; i < conflits.size(); i++) {
            Conflit c = conflits.get(i);
            sb.append(String.format("%d. [%s] %s\n", 
                i + 1, c.getType().getDescription(), c.getMessage()));
        }
        
        return sb.toString();
    }
}
