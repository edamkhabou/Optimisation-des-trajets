package com.covoiturage.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Modèle représentant un trajet optimisé de covoiturage.
 * 
 * Un trajet contient :
 * - Un véhicule
 * - Une liste ordonnée d'utilisateurs (ordre de prise en charge)
 * - Métriques : distance totale, temps total
 * - Coordonnées de route pour affichage sur carte
 */
public class Trajet {
    
    private Long id;
    private Long vehiculeId;
    private double distanceTotale; // En kilomètres
    private double tempsTotalMinutes; // En minutes
    private String routePolyline; // Encoded polyline pour Google Maps
    private boolean optimise;
    
    // Relations
    private Vehicule vehicule;
    private List<Utilisateur> utilisateurs; // Ordre = ordre de prise en charge
    
    // Constructeurs
    public Trajet() {
        this.utilisateurs = new ArrayList<>();
        this.optimise = false;
    }
    
    public Trajet(Long vehiculeId) {
        this.vehiculeId = vehiculeId;
        this.utilisateurs = new ArrayList<>();
        this.optimise = false;
    }
    
    public Trajet(Long id, Long vehiculeId, double distanceTotale, double tempsTotalMinutes, boolean optimise) {
        this.id = id;
        this.vehiculeId = vehiculeId;
        this.distanceTotale = distanceTotale;
        this.tempsTotalMinutes = tempsTotalMinutes;
        this.optimise = optimise;
        this.utilisateurs = new ArrayList<>();
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getVehiculeId() {
        return vehiculeId;
    }
    
    public void setVehiculeId(Long vehiculeId) {
        this.vehiculeId = vehiculeId;
    }
    
    public double getDistanceTotale() {
        return distanceTotale;
    }
    
    public void setDistanceTotale(double distanceTotale) {
        this.distanceTotale = distanceTotale;
    }
    
    public double getTempsTotalMinutes() {
        return tempsTotalMinutes;
    }
    
    public void setTempsTotalMinutes(double tempsTotalMinutes) {
        this.tempsTotalMinutes = tempsTotalMinutes;
    }
    
    public String getRoutePolyline() {
        return routePolyline;
    }
    
    public void setRoutePolyline(String routePolyline) {
        this.routePolyline = routePolyline;
    }
    
    public boolean isOptimise() {
        return optimise;
    }
    
    public void setOptimise(boolean optimise) {
        this.optimise = optimise;
    }
    
    public Vehicule getVehicule() {
        return vehicule;
    }
    
    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
        if (vehicule != null) {
            this.vehiculeId = vehicule.getId();
        }
    }
    
    public List<Utilisateur> getUtilisateurs() {
        return utilisateurs;
    }
    
    public void setUtilisateurs(List<Utilisateur> utilisateurs) {
        this.utilisateurs = utilisateurs;
    }
    
    /**
     * Ajoute un utilisateur au trajet.
     * 
     * @param utilisateur L'utilisateur à ajouter
     */
    public void ajouterUtilisateur(Utilisateur utilisateur) {
        if (!this.utilisateurs.contains(utilisateur)) {
            this.utilisateurs.add(utilisateur);
        }
    }
    
    /**
     * Retire un utilisateur du trajet.
     * 
     * @param utilisateur L'utilisateur à retirer
     */
    public void retirerUtilisateur(Utilisateur utilisateur) {
        this.utilisateurs.remove(utilisateur);
    }
    
    /**
     * Vérifie si le trajet peut accueillir un utilisateur supplémentaire.
     * 
     * @return true si la capacité n'est pas dépassée, false sinon
     */
    public boolean peutAjouterUtilisateur() {
        if (vehicule == null) {
            return true; // On ne peut pas vérifier sans véhicule
        }
        return utilisateurs.size() < vehicule.getCapacite();
    }
    
    /**
     * Calcule le taux de remplissage du véhicule.
     * 
     * @return Taux de remplissage en pourcentage (0-100)
     */
    public double tauxRemplissage() {
        if (vehicule == null || vehicule.getCapacite() == 0) {
            return 0.0;
        }
        return (utilisateurs.size() * 100.0) / vehicule.getCapacite();
    }
    
    /**
     * Obtient le nombre de places disponibles.
     * 
     * @return Nombre de places restantes
     */
    public int placesDisponibles() {
        if (vehicule == null) {
            return 0;
        }
        return vehicule.getCapacite() - utilisateurs.size();
    }
    
    /**
     * Calcule la distance moyenne par utilisateur.
     * 
     * @return Distance moyenne en km
     */
    public double distanceMoyenneParUtilisateur() {
        if (utilisateurs.isEmpty()) {
            return 0.0;
        }
        return distanceTotale / utilisateurs.size();
    }
    
    /**
     * Calcule le temps moyen par utilisateur.
     * 
     * @return Temps moyen en minutes
     */
    public double tempsMoyenParUtilisateur() {
        if (utilisateurs.isEmpty()) {
            return 0.0;
        }
        return tempsTotalMinutes / utilisateurs.size();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trajet trajet = (Trajet) o;
        return Objects.equals(id, trajet.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Trajet{" +
                "id=" + id +
                ", vehiculeId=" + vehiculeId +
                ", distanceTotale=" + distanceTotale + " km" +
                ", tempsTotalMinutes=" + tempsTotalMinutes + " min" +
                ", nombreUtilisateurs=" + utilisateurs.size() +
                ", optimise=" + optimise +
                '}';
    }
}
