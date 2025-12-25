package com.covoiturage.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.covoiturage.models.Utilisateur;
import com.covoiturage.models.Vehicule;

/**
 * Algorithme d'optimisation par Recuit Simulé (Simulated Annealing).
 * 
 * PRINCIPE:
 * Inspiré du processus de recuit en métallurgie, cet algorithme explore l'espace
 * des solutions en acceptant parfois des solutions moins bonnes pour éviter les minima locaux.
 * 
 * ALGORITHME:
 * 1. Partir d'une solution initiale (aléatoire ou heuristique)
 * 2. Température initiale élevée
 * 3. À chaque itération:
 *    - Générer une solution voisine (petite modification)
 *    - Si meilleure: accepter
 *    - Si moins bonne: accepter avec probabilité P = exp(-ΔE/T)
 *    - Réduire la température: T = T * α (0 < α < 1)
 * 4. Répéter jusqu'à température minimale ou convergence
 * 
 * PARAMÈTRES:
 * - Température initiale (T0): 1000.0
 * - Taux de refroidissement (α): 0.95
 * - Nombre d'itérations: 1000
 * - Température minimale: 1.0
 * 
 * COMPLEXITÉ: O(n × iterations) où n = nombre d'utilisateurs
 * 
 * AVANTAGES:
 * - Évite les minima locaux
 * - Peut trouver des solutions proches de l'optimum global
 * - Flexible et adaptable
 * 
 * INCONVÉNIENTS:
 * - Plus lent que les heuristiques simples
 * - Nécessite un réglage des paramètres
 */
public class SimulatedAnnealingAlgorithme implements OptimisationAlgorithme {
    
    private static final Logger logger = LoggerFactory.getLogger(SimulatedAnnealingAlgorithme.class);
    
    // Paramètres de l'algorithme
    private double temperatureInitiale = 1000.0;
    private double tauxRefroidissement = 0.95;
    private int nombreIterations = 1000;
    private double temperatureMin = 1.0;
    private Random random = new Random();
    
    /**
     * Constructeur par défaut.
     */
    public SimulatedAnnealingAlgorithme() {}
    
    /**
     * Constructeur avec paramètres personnalisés.
     * 
     * @param temperatureInitiale Température de départ
     * @param tauxRefroidissement Facteur de refroidissement (0 < α < 1)
     * @param nombreIterations Nombre d'itérations
     */
    public SimulatedAnnealingAlgorithme(double temperatureInitiale, double tauxRefroidissement, 
                                       int nombreIterations) {
        this.temperatureInitiale = temperatureInitiale;
        this.tauxRefroidissement = tauxRefroidissement;
        this.nombreIterations = nombreIterations;
    }
    
    /**
     * Optimise l'ordre de prise en charge en utilisant le recuit simulé.
     * 
     * @param utilisateurs Liste des utilisateurs à transporter
     * @param vehicule Le véhicule utilisé
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
        
        logger.info("Démarrage du Recuit Simulé pour {} utilisateurs", utilisateurs.size());
        logger.info("Paramètres: T0={}, α={}, iterations={}", 
                    temperatureInitiale, tauxRefroidissement, nombreIterations);
        
        long startTime = System.currentTimeMillis();
        
        // 1. Générer une solution initiale (aléatoire)
        Solution solutionCourante = genererSolutionInitiale(utilisateurs);
        calculerMetriques(solutionCourante);
        
        Solution meilleureSolution = solutionCourante.copier();
        
        double temperature = temperatureInitiale;
        int iterationsSansAmelioration = 0;
        
        // 2. Boucle principale du recuit simulé
        for (int iteration = 0; iteration < nombreIterations && temperature > temperatureMin; iteration++) {
            
            // Générer une solution voisine
            Solution solutionVoisine = genererSolutionVoisine(solutionCourante);
            calculerMetriques(solutionVoisine);
            
            // Calculer la différence de coût (ΔE)
            double deltaE = solutionVoisine.getCout() - solutionCourante.getCout();
            
            // Décider d'accepter ou non la nouvelle solution
            if (deltaE < 0) {
                // Meilleure solution -> accepter
                solutionCourante = solutionVoisine;
                iterationsSansAmelioration = 0;
                
                // Mettre à jour la meilleure solution globale
                if (solutionCourante.getCout() < meilleureSolution.getCout()) {
                    meilleureSolution = solutionCourante.copier();
                    logger.debug("Nouvelle meilleure solution trouvée: {}", meilleureSolution);
                }
            } else {
                // Solution moins bonne -> accepter avec probabilité P = exp(-ΔE/T)
                double probabiliteAcceptation = Math.exp(-deltaE / temperature);
                
                if (random.nextDouble() < probabiliteAcceptation) {
                    solutionCourante = solutionVoisine;
                    logger.trace("Solution moins bonne acceptée (P={:.4f})", probabiliteAcceptation);
                }
                
                iterationsSansAmelioration++;
            }
            
            // Refroidir la température
            temperature *= tauxRefroidissement;
            
            // Log périodique
            if (iteration % 100 == 0) {
                logger.debug("Iteration {}/{}: T={:.2f}, Coût actuel={:.2f}, Meilleur coût={:.2f}", 
                            iteration, nombreIterations, temperature, 
                            solutionCourante.getCout(), meilleureSolution.getCout());
            }
            
            // Critère d'arrêt anticipé si pas d'amélioration
            if (iterationsSansAmelioration > 200) {
                logger.info("Arrêt anticipé: pas d'amélioration depuis 200 itérations");
                break;
            }
        }
        
        long endTime = System.currentTimeMillis();
        logger.info("Recuit simulé terminé en {} ms", endTime - startTime);
        logger.info("Solution finale: Distance={:.2f} km, Temps={:.2f} min, Coût={:.2f}", 
                    meilleureSolution.getDistanceTotale(), 
                    meilleureSolution.getTempsTotalMinutes(),
                    meilleureSolution.getCout());
        
        return meilleureSolution;
    }
    
    /**
     * Génère une solution initiale aléatoire.
     * 
     * @param utilisateurs Liste des utilisateurs
     * @return Une solution initiale
     */
    private Solution genererSolutionInitiale(List<Utilisateur> utilisateurs) {
        List<Utilisateur> ordre = new ArrayList<>(utilisateurs);
        // Mélanger aléatoirement
        for (int i = ordre.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Utilisateur temp = ordre.get(i);
            ordre.set(i, ordre.get(j));
            ordre.set(j, temp);
        }
        
        return new Solution(ordre);
    }
    
    /**
     * Génère une solution voisine en appliquant une petite modification.
     * 
     * Opérateurs de voisinage :
     * - Swap: Échanger deux utilisateurs
     * - 2-opt: Inverser un segment du trajet
     * 
     * @param solution La solution de départ
     * @return Une solution voisine
     */
    private Solution genererSolutionVoisine(Solution solution) {
        Solution voisine = solution.copier();
        int n = voisine.getOrdreUtilisateurs().size();
        
        if (n < 2) {
            return voisine;
        }
        
        // Choisir aléatoirement un opérateur
        if (random.nextBoolean()) {
            // Opérateur Swap: échanger deux utilisateurs
            int i = random.nextInt(n);
            int j = random.nextInt(n);
            voisine.echangerUtilisateurs(i, j);
        } else {
            // Opérateur 2-opt: inverser un segment
            int i = random.nextInt(n);
            int j = random.nextInt(n);
            
            if (i > j) {
                int temp = i;
                i = j;
                j = temp;
            }
            
            voisine.inverserSegment(i, j);
        }
        
        return voisine;
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
        
        // Calculer la distance totale
        for (int i = 0; i < ordre.size() - 1; i++) {
            distanceTotale += calculerDistance(ordre.get(i), ordre.get(i + 1));
        }
        
        // Estimation du temps
        double vitesseMoyenne = 30.0; // km/h
        double tempsTotalMinutes = (distanceTotale / vitesseMoyenne) * 60;
        
        solution.setDistanceTotale(distanceTotale);
        solution.setTempsTotalMinutes(tempsTotalMinutes);
        solution.calculerCout(); // Calculer le coût combiné
    }
    
    /**
     * Calcule la distance entre deux utilisateurs.
     * 
     * @param u1 Premier utilisateur
     * @param u2 Second utilisateur
     * @return Distance en kilomètres
     */
    private double calculerDistance(Utilisateur u1, Utilisateur u2) {
        if (u1.getLatitude() != null && u1.getLongitude() != null &&
            u2.getLatitude() != null && u2.getLongitude() != null) {
            
            return calculerDistanceHaversine(
                u1.getLatitude(), u1.getLongitude(),
                u2.getLatitude(), u2.getLongitude()
            );
        }
        
        // Fallback: distance aléatoire pour simulation
        return Math.random() * 10 + 1;
    }
    
    /**
     * Calcule la distance GPS avec la formule de Haversine.
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
    
    @Override
    public String getNom() {
        return "Simulated Annealing (Recuit Simulé)";
    }
    
    // Setters pour configurer l'algorithme
    public void setTemperatureInitiale(double temperatureInitiale) {
        this.temperatureInitiale = temperatureInitiale;
    }
    
    public void setTauxRefroidissement(double tauxRefroidissement) {
        this.tauxRefroidissement = tauxRefroidissement;
    }
    
    public void setNombreIterations(int nombreIterations) {
        this.nombreIterations = nombreIterations;
    }
}
