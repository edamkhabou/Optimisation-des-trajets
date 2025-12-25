package com.covoiturage.services;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.covoiturage.dao.TrajetDAO;
import com.covoiturage.models.Trajet;

/**
 * Service de calcul et génération de statistiques sur les trajets.
 * 
 * Fournit :
 * - Distance totale parcourue
 * - Temps moyen de trajet
 * - Taux de remplissage des véhicules
 * - Nombre de conflits détectés
 * - Métriques d'optimisation
 */
public class StatistiqueService {
    
    private static final Logger logger = LoggerFactory.getLogger(StatistiqueService.class);
    private final TrajetDAO trajetDAO;
    private final ConflitService conflitService;
    
    public StatistiqueService() {
        this.trajetDAO = new TrajetDAO();
        this.conflitService = new ConflitService();
    }
    
    /**
     * Calcule toutes les statistiques globales du système.
     * 
     * @return Map contenant toutes les statistiques
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    public Map<String, Object> calculerStatistiquesGlobales() throws SQLException {
        logger.info("Calcul des statistiques globales");
        
        Map<String, Object> stats = new HashMap<>();
        
        List<Trajet> tousTrajets = trajetDAO.findAll();
        
        if (tousTrajets.isEmpty()) {
            logger.warn("Aucun trajet trouvé pour les statistiques");
            stats.put("message", "Aucun trajet disponible");
            return stats;
        }
        
        // 1. Distance totale parcourue
        double distanceTotale = calculerDistanceTotale(tousTrajets);
        stats.put("distanceTotale", distanceTotale);
        stats.put("distanceTotaleFormatted", String.format("%.2f km", distanceTotale));
        
        // 2. Distance moyenne par trajet
        double distanceMoyenne = distanceTotale / tousTrajets.size();
        stats.put("distanceMoyenne", distanceMoyenne);
        stats.put("distanceMoyenneFormatted", String.format("%.2f km", distanceMoyenne));
        
        // 3. Temps moyen de trajet
        double tempsMoyen = calculerTempsMoyen(tousTrajets);
        stats.put("tempsMoyen", tempsMoyen);
        stats.put("tempsMoyenFormatted", String.format("%.0f min", tempsMoyen));
        
        // 4. Taux de remplissage moyen des véhicules
        double tauxRemplissage = calculerTauxRemplissageMoyen(tousTrajets);
        stats.put("tauxRemplissageMoyen", tauxRemplissage);
        stats.put("tauxRemplissageMoyenFormatted", String.format("%.1f%%", tauxRemplissage));
        
        // 5. Nombre de trajets optimisés vs non optimisés
        long trajetsOptimises = tousTrajets.stream().filter(Trajet::isOptimise).count();
        long trajetsNonOptimises = tousTrajets.size() - trajetsOptimises;
        stats.put("trajetsOptimises", trajetsOptimises);
        stats.put("trajetsNonOptimises", trajetsNonOptimises);
        stats.put("totalTrajets", tousTrajets.size());
        
        // 6. Nombre total d'utilisateurs transportés
        int totalUtilisateurs = calculerTotalUtilisateurs(tousTrajets);
        stats.put("totalUtilisateurs", totalUtilisateurs);
        
        // 7. Nombre moyen de passagers par trajet
        double passagersMoyens = (double) totalUtilisateurs / tousTrajets.size();
        stats.put("passagersMoyens", passagersMoyens);
        stats.put("passagersMoyensFormatted", String.format("%.1f", passagersMoyens));
        
        // 8. Détection de conflits
        int totalConflits = compterConflits(tousTrajets);
        stats.put("totalConflits", totalConflits);
        
        // 9. Efficacité (km économisés grâce au covoiturage)
        // Hypothèse: sans covoiturage, chaque utilisateur fait le trajet seul
        double kmEconomises = calculerKmEconomises(tousTrajets);
        stats.put("kmEconomises", kmEconomises);
        stats.put("kmEconomisesFormatted", String.format("%.2f km", kmEconomises));
        
        // 10. Émissions CO2 économisées (estimation)
        // Hypothèse: 120g CO2/km en moyenne pour une voiture
        double co2Economise = kmEconomises * 0.12; // kg
        stats.put("co2EconomiseKg", co2Economise);
        stats.put("co2EconomiseFormatted", String.format("%.2f kg", co2Economise));
        
        logger.info("Statistiques calculées: {} trajets, {:.2f} km total, {:.1f}% remplissage", 
                    tousTrajets.size(), distanceTotale, tauxRemplissage);
        
        return stats;
    }
    
    /**
     * Calcule la distance totale de tous les trajets.
     */
    private double calculerDistanceTotale(List<Trajet> trajets) {
        return trajets.stream()
            .mapToDouble(Trajet::getDistanceTotale)
            .sum();
    }
    
    /**
     * Calcule le temps moyen de tous les trajets.
     */
    private double calculerTempsMoyen(List<Trajet> trajets) {
        return trajets.stream()
            .mapToDouble(Trajet::getTempsTotalMinutes)
            .average()
            .orElse(0.0);
    }
    
    /**
     * Calcule le taux de remplissage moyen des véhicules.
     */
    private double calculerTauxRemplissageMoyen(List<Trajet> trajets) {
        return trajets.stream()
            .filter(t -> t.getVehicule() != null)
            .mapToDouble(Trajet::tauxRemplissage)
            .average()
            .orElse(0.0);
    }
    
    /**
     * Calcule le nombre total d'utilisateurs transportés.
     */
    private int calculerTotalUtilisateurs(List<Trajet> trajets) {
        return trajets.stream()
            .mapToInt(t -> t.getUtilisateurs().size())
            .sum();
    }
    
    /**
     * Compte le nombre total de conflits détectés.
     */
    private int compterConflits(List<Trajet> trajets) {
        return trajets.stream()
            .mapToInt(t -> conflitService.detecterConflits(t).size())
            .sum();
    }
    
    /**
     * Calcule les kilomètres économisés grâce au covoiturage.
     * 
     * Hypothèse: sans covoiturage, chaque utilisateur ferait le trajet individuellement.
     * Économie = (nombre d'utilisateurs - 1) × distance du trajet
     */
    private double calculerKmEconomises(List<Trajet> trajets) {
        double kmEconomises = 0.0;
        
        for (Trajet trajet : trajets) {
            int nbUtilisateurs = trajet.getUtilisateurs().size();
            if (nbUtilisateurs > 1) {
                // Économie = passagers supplémentaires × distance
                kmEconomises += (nbUtilisateurs - 1) * trajet.getDistanceTotale();
            }
        }
        
        return kmEconomises;
    }
    
    /**
     * Génère un rapport détaillé des statistiques.
     */
    public String genererRapport() throws SQLException {
        Map<String, Object> stats = calculerStatistiquesGlobales();
        
        StringBuilder rapport = new StringBuilder();
        rapport.append("═══════════════════════════════════════════════\n");
        rapport.append("   RAPPORT STATISTIQUES - COVOITURAGE\n");
        rapport.append("═══════════════════════════════════════════════\n\n");
        
        rapport.append("📊 TRAJETS\n");
        rapport.append("─────────────────────────────────────────────\n");
        rapport.append(String.format("  Total de trajets: %d\n", stats.get("totalTrajets")));
        rapport.append(String.format("  Trajets optimisés: %d\n", stats.get("trajetsOptimises")));
        rapport.append(String.format("  Trajets non optimisés: %d\n\n", stats.get("trajetsNonOptimises")));
        
        rapport.append("📏 DISTANCES\n");
        rapport.append("─────────────────────────────────────────────\n");
        rapport.append(String.format("  Distance totale: %s\n", stats.get("distanceTotaleFormatted")));
        rapport.append(String.format("  Distance moyenne: %s\n", stats.get("distanceMoyenneFormatted")));
        rapport.append(String.format("  Kilomètres économisés: %s\n\n", stats.get("kmEconomisesFormatted")));
        
        rapport.append("⏱️  TEMPS\n");
        rapport.append("─────────────────────────────────────────────\n");
        rapport.append(String.format("  Temps moyen par trajet: %s\n\n", stats.get("tempsMoyenFormatted")));
        
        rapport.append("👥 UTILISATEURS\n");
        rapport.append("─────────────────────────────────────────────\n");
        rapport.append(String.format("  Total d'utilisateurs: %d\n", stats.get("totalUtilisateurs")));
        rapport.append(String.format("  Passagers moyens/trajet: %s\n", stats.get("passagersMoyensFormatted")));
        rapport.append(String.format("  Taux de remplissage: %s\n\n", stats.get("tauxRemplissageMoyenFormatted")));
        
        rapport.append("🌱 IMPACT ENVIRONNEMENTAL\n");
        rapport.append("─────────────────────────────────────────────\n");
        rapport.append(String.format("  CO₂ économisé: %s\n\n", stats.get("co2EconomiseFormatted")));
        
        rapport.append("⚠️  CONFLITS\n");
        rapport.append("─────────────────────────────────────────────\n");
        rapport.append(String.format("  Nombre de conflits: %d\n", stats.get("totalConflits")));
        
        rapport.append("\n═══════════════════════════════════════════════\n");
        
        return rapport.toString();
    }
}
