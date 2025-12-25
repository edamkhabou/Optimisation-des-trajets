package com.covoiturage.optimization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.covoiturage.models.Utilisateur;
import com.covoiturage.models.Vehicule;

/**
 * Algorithme d'optimisation Nearest Neighbor (Plus Proche Voisin).
 * 
 * PRINCIPE:
 * 1. Partir d'un utilisateur de départ
 * 2. À chaque étape, choisir l'utilisateur non visité le plus proche
 * 3. Répéter jusqu'à ce que tous les utilisateurs soient visités
 * 
 * COMPLEXITÉ: O(n²) où n = nombre d'utilisateurs
 * 
 * AVANTAGES:
 * - Rapide et simple
 * - Garantit une solution acceptable
 * - Bon pour des ensembles de taille petite à moyenne
 * 
 * INCONVÉNIENTS:
 * - Peut rester bloqué dans un minimum local
 * - Pas toujours la solution optimale globale
 */
public class NearestNeighborAlgorithme implements OptimisationAlgorithme {
    
    private static final Logger logger = LoggerFactory.getLogger(NearestNeighborAlgorithme.class);
    
    /**
     * Optimise l'ordre de prise en charge en utilisant l'algorithme du plus proche voisin.
     * 
     * @param utilisateurs Liste des utilisateurs à transporter
     * @param vehicule Le véhicule utilisé (pour vérifier la capacité)
     * @return La solution optimisée
     */
    @Override
    public Solution optimiser(List<Utilisateur> utilisateurs, Vehicule vehicule) {
        if (utilisateurs == null || utilisateurs.isEmpty()) {
            logger.warn("Liste d'utilisateurs vide");
            return new Solution();
        }
        
        if (utilisateurs.size() > vehicule.getCapacite()) {
            logger.error("Nombre d'utilisateurs ({}) dépasse la capacité du véhicule ({})", 
                        utilisateurs.size(), vehicule.getCapacite());
            throw new IllegalArgumentException("Capacité du véhicule dépassée");
        }
        
        logger.info("Démarrage de l'optimisation Nearest Neighbor pour {} utilisateurs", 
                    utilisateurs.size());
        
        long startTime = System.currentTimeMillis();
        
        List<Utilisateur> ordreOptimise = new ArrayList<>();
        Set<Utilisateur> visites = new HashSet<>();
        
        // Commencer par le premier utilisateur (ou choisir le meilleur point de départ)
        Utilisateur courant = utilisateurs.get(0);
        ordreOptimise.add(courant);
        visites.add(courant);
        
        // Parcourir tous les utilisateurs restants
        while (visites.size() < utilisateurs.size()) {
            Utilisateur plusProche = trouverPlusProche(courant, utilisateurs, visites);
            
            if (plusProche != null) {
                ordreOptimise.add(plusProche);
                visites.add(plusProche);
                courant = plusProche;
            } else {
                logger.error("Impossible de trouver le prochain utilisateur");
                break;
            }
        }
        
        // Créer la solution
        Solution solution = new Solution(ordreOptimise);
        
        // Calculer la distance et le temps total
        calculerMetriques(solution);
        
        long endTime = System.currentTimeMillis();
        logger.info("Optimisation terminée en {} ms. Distance: {:.2f} km, Temps: {:.2f} min", 
                    endTime - startTime, solution.getDistanceTotale(), solution.getTempsTotalMinutes());
        
        return solution;
    }
    
    /**
     * Trouve l'utilisateur non visité le plus proche de l'utilisateur courant.
     * 
     * @param courant L'utilisateur de référence
     * @param tousUtilisateurs Liste de tous les utilisateurs
     * @param visites Ensemble des utilisateurs déjà visités
     * @return L'utilisateur le plus proche non visité
     */
    private Utilisateur trouverPlusProche(Utilisateur courant, List<Utilisateur> tousUtilisateurs, 
                                          Set<Utilisateur> visites) {
        Utilisateur plusProche = null;
        double distanceMin = Double.MAX_VALUE;
        
        for (Utilisateur utilisateur : tousUtilisateurs) {
            if (!visites.contains(utilisateur)) {
                double distance = calculerDistance(courant, utilisateur);
                
                if (distance < distanceMin) {
                    distanceMin = distance;
                    plusProche = utilisateur;
                }
            }
        }
        
        return plusProche;
    }
    
    /**
     * Calcule la distance euclidienne entre deux utilisateurs.
     * 
     * Utilise la formule: distance = √((x2-x1)² + (y2-y1)²)
     * 
     * Note: Pour une version production, utiliser l'API Google Maps Distance Matrix
     * pour obtenir les distances réelles.
     * 
     * @param u1 Premier utilisateur
     * @param u2 Second utilisateur
     * @return Distance en kilomètres (approximation)
     */
    private double calculerDistance(Utilisateur u1, Utilisateur u2) {
        // Si les coordonnées sont disponibles, utiliser la distance euclidienne
        if (u1.getLatitude() != null && u1.getLongitude() != null &&
            u2.getLatitude() != null && u2.getLongitude() != null) {
            
            double lat1 = u1.getLatitude();
            double lon1 = u1.getLongitude();
            double lat2 = u2.getLatitude();
            double lon2 = u2.getLongitude();
            
            // Formule de Haversine pour distance entre deux points GPS
            return calculerDistanceHaversine(lat1, lon1, lat2, lon2);
        }
        
        // Sinon, utiliser une heuristique basée sur les adresses (simplifiée)
        // Dans une vraie app, appeler Google Maps Distance Matrix API
        return Math.random() * 10 + 1; // Distance aléatoire entre 1 et 11 km (pour simulation)
    }
    
    /**
     * Calcule la distance entre deux points GPS avec la formule de Haversine.
     * 
     * @param lat1 Latitude du point 1
     * @param lon1 Longitude du point 1
     * @param lat2 Latitude du point 2
     * @param lon2 Longitude du point 2
     * @return Distance en kilomètres
     */
    private double calculerDistanceHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int RAYON_TERRE_KM = 6371;
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return RAYON_TERRE_KM * c;
    }
    
    /**
     * Calcule les métriques (distance et temps) pour une solution.
     * 
     * @param solution La solution à évaluer
     */
    private void calculerMetriques(Solution solution) {
        List<Utilisateur> ordre = solution.getOrdreUtilisateurs();
        
        if (ordre.isEmpty()) {
            return;
        }
        
        double distanceTotale = 0.0;
        
        // Calculer la distance totale en parcourant les utilisateurs dans l'ordre
        for (int i = 0; i < ordre.size() - 1; i++) {
            distanceTotale += calculerDistance(ordre.get(i), ordre.get(i + 1));
        }
        
        // Estimation du temps : vitesse moyenne de 30 km/h en ville
        double vitesseMoyenne = 30.0; // km/h
        double tempsTotalMinutes = (distanceTotale / vitesseMoyenne) * 60;
        
        solution.setDistanceTotale(distanceTotale);
        solution.setTempsTotalMinutes(tempsTotalMinutes);
        solution.calculerCout();
    }
    
    @Override
    public String getNom() {
        return "Nearest Neighbor (Plus Proche Voisin)";
    }
}
