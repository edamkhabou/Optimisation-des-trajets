package com.covoiturage.services;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.covoiturage.dao.TrajetDAO;
import com.covoiturage.dao.UtilisateurDAO;
import com.covoiturage.dao.VehiculeDAO;
import com.covoiturage.models.Trajet;
import com.covoiturage.models.Utilisateur;
import com.covoiturage.models.Vehicule;
import com.covoiturage.optimization.NearestNeighborAlgorithme;
import com.covoiturage.optimization.OptimisationAlgorithme;
import com.covoiturage.optimization.SimulatedAnnealingAlgorithme;
import com.covoiturage.optimization.Solution;

/**
 * Service principal d'optimisation des trajets de covoiturage.
 * 
 * Coordonne les algorithmes d'optimisation avec les données de la base.
 */
public class OptimisationService {
    
    private static final Logger logger = LoggerFactory.getLogger(OptimisationService.class);
    
    private final TrajetDAO trajetDAO;
    private final UtilisateurDAO utilisateurDAO;
    private final VehiculeDAO vehiculeDAO;
    private final ConflitService conflitService;
    
    public OptimisationService() {
        this.trajetDAO = new TrajetDAO();
        this.utilisateurDAO = new UtilisateurDAO();
        this.vehiculeDAO = new VehiculeDAO();
        this.conflitService = new ConflitService();
    }
    
    /**
     * Optimise un trajet avec un algorithme spécifié.
     * 
     * @param vehiculeId ID du véhicule
     * @param utilisateurIds Liste des IDs des utilisateurs à transporter
     * @param typeAlgorithme Type d'algorithme ("nearest_neighbor" ou "simulated_annealing")
     * @return Le trajet optimisé
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    public Trajet optimiserTrajet(Long vehiculeId, List<Long> utilisateurIds, 
                                  String typeAlgorithme) throws SQLException {
        
        logger.info("Optimisation d'un trajet: véhicule {}, {} utilisateurs, algorithme: {}", 
                    vehiculeId, utilisateurIds.size(), typeAlgorithme);
        
        // 1. Charger le véhicule
        Vehicule vehicule = vehiculeDAO.findById(vehiculeId)
            .orElseThrow(() -> new IllegalArgumentException("Véhicule introuvable: " + vehiculeId));
        
        // 2. Charger les utilisateurs
        List<Utilisateur> utilisateurs = new java.util.ArrayList<>();
        for (Long userId : utilisateurIds) {
            Utilisateur utilisateur = utilisateurDAO.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + userId));
            utilisateurs.add(utilisateur);
        }
        
        // 3. Vérifier la capacité avant optimisation
        if (utilisateurs.size() > vehicule.getCapacite()) {
            throw new IllegalArgumentException(
                String.format("Nombre d'utilisateurs (%d) dépasse la capacité du véhicule (%d)",
                            utilisateurs.size(), vehicule.getCapacite())
            );
        }
        
        // 4. Sélectionner l'algorithme d'optimisation
        OptimisationAlgorithme algorithme = selectionnerAlgorithme(typeAlgorithme);
        
        // 5. Exécuter l'optimisation
        Solution solution = algorithme.optimiser(utilisateurs, vehicule);
        
        // 6. Créer le trajet à partir de la solution
        Trajet trajet = new Trajet();
        trajet.setVehiculeId(vehiculeId);
        trajet.setVehicule(vehicule);
        trajet.setUtilisateurs(solution.getOrdreUtilisateurs());
        trajet.setDistanceTotale(solution.getDistanceTotale());
        trajet.setTempsTotalMinutes(solution.getTempsTotalMinutes());
        trajet.setOptimise(true);
        
        // 7. Vérifier les conflits
        if (!conflitService.estValide(trajet)) {
            logger.warn("Le trajet optimisé contient des conflits bloquants");
            throw new IllegalStateException("Le trajet contient des conflits bloquants");
        }
        
        // 8. Sauvegarder le trajet
        Trajet trajetSauvegarde = trajetDAO.create(trajet);
        
        logger.info("Trajet optimisé créé avec ID: {}, Distance: {:.2f} km, Temps: {:.2f} min",
                    trajetSauvegarde.getId(), 
                    trajetSauvegarde.getDistanceTotale(),
                    trajetSauvegarde.getTempsTotalMinutes());
        
        return trajetSauvegarde;
    }
    
    /**
     * Re-optimise un trajet existant.
     * 
     * @param trajetId ID du trajet à re-optimiser
     * @param typeAlgorithme Type d'algorithme
     * @return Le trajet re-optimisé
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    public Trajet reOptimiserTrajet(Long trajetId, String typeAlgorithme) throws SQLException {
        logger.info("Re-optimisation du trajet {}", trajetId);
        
        // Charger le trajet existant
        Trajet trajetExistant = trajetDAO.findById(trajetId)
            .orElseThrow(() -> new IllegalArgumentException("Trajet introuvable: " + trajetId));
        
        // Charger le véhicule
        Vehicule vehicule = vehiculeDAO.findById(trajetExistant.getVehiculeId())
            .orElseThrow(() -> new IllegalArgumentException("Véhicule introuvable"));
        
        List<Utilisateur> utilisateurs = trajetExistant.getUtilisateurs();
        
        // Optimiser
        OptimisationAlgorithme algorithme = selectionnerAlgorithme(typeAlgorithme);
        Solution solution = algorithme.optimiser(utilisateurs, vehicule);
        
        // Mettre à jour le trajet
        trajetExistant.setUtilisateurs(solution.getOrdreUtilisateurs());
        trajetExistant.setDistanceTotale(solution.getDistanceTotale());
        trajetExistant.setTempsTotalMinutes(solution.getTempsTotalMinutes());
        trajetExistant.setOptimise(true);
        
        // Sauvegarder
        trajetDAO.update(trajetExistant);
        
        logger.info("Trajet {} re-optimisé", trajetId);
        return trajetExistant;
    }
    
    /**
     * Compare les résultats de différents algorithmes sur les mêmes données.
     * 
     * @param vehiculeId ID du véhicule
     * @param utilisateurIds Liste des IDs utilisateurs
     * @return Résultats comparatifs
     * @throws SQLException En cas d'erreur
     */
    public ComparisonResult comparerAlgorithmes(Long vehiculeId, List<Long> utilisateurIds) 
            throws SQLException {
        
        logger.info("Comparaison des algorithmes pour {} utilisateurs", utilisateurIds.size());
        
        // Charger les données
        Vehicule vehicule = vehiculeDAO.findById(vehiculeId)
            .orElseThrow(() -> new IllegalArgumentException("Véhicule introuvable"));
        
        List<Utilisateur> utilisateurs = new java.util.ArrayList<>();
        for (Long userId : utilisateurIds) {
            utilisateurs.add(utilisateurDAO.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable")));
        }
        
        ComparisonResult result = new ComparisonResult();
        
        // Tester Nearest Neighbor
        long startNN = System.currentTimeMillis();
        NearestNeighborAlgorithme nn = new NearestNeighborAlgorithme();
        Solution solutionNN = nn.optimiser(utilisateurs, vehicule);
        long timeNN = System.currentTimeMillis() - startNN;
        
        result.nearestNeighbor = new AlgorithmResult(
            "Nearest Neighbor",
            solutionNN.getDistanceTotale(),
            solutionNN.getTempsTotalMinutes(),
            timeNN
        );
        
        // Tester Simulated Annealing
        long startSA = System.currentTimeMillis();
        SimulatedAnnealingAlgorithme sa = new SimulatedAnnealingAlgorithme();
        Solution solutionSA = sa.optimiser(utilisateurs, vehicule);
        long timeSA = System.currentTimeMillis() - startSA;
        
        result.simulatedAnnealing = new AlgorithmResult(
            "Simulated Annealing",
            solutionSA.getDistanceTotale(),
            solutionSA.getTempsTotalMinutes(),
            timeSA
        );
        
        // Déterminer le meilleur
        if (solutionNN.getCout() < solutionSA.getCout()) {
            result.meilleur = "Nearest Neighbor";
            result.amelioration = 0.0;
        } else {
            result.meilleur = "Simulated Annealing";
            double gain = ((solutionNN.getCout() - solutionSA.getCout()) / solutionNN.getCout()) * 100;
            result.amelioration = gain;
        }
        
        // Créer les trajets pour affichage sur la carte
        Trajet trajetNN = new Trajet();
        trajetNN.setVehicule(vehicule);
        trajetNN.setUtilisateurs(solutionNN.getOrdreUtilisateurs());
        trajetNN.setDistanceTotale(solutionNN.getDistanceTotale());
        trajetNN.setTempsTotalMinutes(solutionNN.getTempsTotalMinutes());
        
        Trajet trajetSA = new Trajet();
        trajetSA.setVehicule(vehicule);
        trajetSA.setUtilisateurs(solutionSA.getOrdreUtilisateurs());
        trajetSA.setDistanceTotale(solutionSA.getDistanceTotale());
        trajetSA.setTempsTotalMinutes(solutionSA.getTempsTotalMinutes());
        
        result.nearestNeighborTrajet = trajetNN;
        result.simulatedAnnealingTrajet = trajetSA;
        
        logger.info("Comparaison terminée. Meilleur: {}, Amélioration: {:.2f}%", 
                    result.meilleur, result.amelioration);
        
        return result;
    }
    
    /**
     * Sélectionne l'algorithme d'optimisation approprié.
     */
    private OptimisationAlgorithme selectionnerAlgorithme(String type) {
        if (type == null || type.equalsIgnoreCase("nearest_neighbor")) {
            return new NearestNeighborAlgorithme();
        } else if (type.equalsIgnoreCase("simulated_annealing")) {
            return new SimulatedAnnealingAlgorithme();
        } else {
            logger.warn("Type d'algorithme inconnu: {}. Utilisation de Nearest Neighbor.", type);
            return new NearestNeighborAlgorithme();
        }
    }
    
    /**
     * Classe pour les résultats de comparaison.
     */
    public static class ComparisonResult {
        public AlgorithmResult nearestNeighbor;
        public AlgorithmResult simulatedAnnealing;
        public Trajet nearestNeighborTrajet;
        public Trajet simulatedAnnealingTrajet;
        public String meilleur;
        public double amelioration; // En pourcentage
    }
    
    /**
     * Classe pour les résultats d'un algorithme.
     */
    public static class AlgorithmResult {
        public String nom;
        public double distanceTotale;
        public double tempsTotalMinutes;
        public long tempsCalculMillis;
        
        public AlgorithmResult(String nom, double distance, double temps, long executionMs) {
            this.nom = nom;
            this.distanceTotale = distance;
            this.tempsTotalMinutes = temps;
            this.tempsCalculMillis = executionMs;
        }
    }
}
