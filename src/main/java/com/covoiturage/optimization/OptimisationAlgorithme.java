package com.covoiturage.optimization;

import java.util.List;

import com.covoiturage.models.Utilisateur;
import com.covoiturage.models.Vehicule;

/**
 * Interface pour les algorithmes d'optimisation de trajets.
 * 
 * Tous les algorithmes d'optimisation doivent implémenter cette interface.
 */
public interface OptimisationAlgorithme {
    
    /**
     * Optimise l'ordre de prise en charge des utilisateurs.
     * 
     * @param utilisateurs Liste des utilisateurs à transporter
     * @param vehicule Le véhicule utilisé
     * @return La solution optimisée
     */
    Solution optimiser(List<Utilisateur> utilisateurs, Vehicule vehicule);
    
    /**
     * Obtient le nom de l'algorithme.
     * 
     * @return Nom de l'algorithme
     */
    String getNom();
}
