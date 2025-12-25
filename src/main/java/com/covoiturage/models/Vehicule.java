package com.covoiturage.models;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Modèle représentant un véhicule du système de covoiturage.
 * 
 * Contient les informations du véhicule incluant :
 * - Identification (id, immatriculation)
 * - Conducteur (référence vers Utilisateur)
 * - Capacité maximale
 * - Disponibilité temporelle
 */
public class Vehicule {
    
    private Long id;
    private Long conducteurId; // ID de l'utilisateur conducteur
    private String immatriculation;
    private int capacite; // Nombre de places disponibles (conducteur non compris)
    private LocalTime heureDebutDisponibilite;
    private LocalTime heureFinDisponibilite;
    private boolean disponible;
    
    // Pour jointure avec la table Utilisateur
    private Utilisateur conducteur;
    
    // Constructeurs
    public Vehicule() {
        this.disponible = true;
    }
    
    public Vehicule(Long conducteurId, String immatriculation, int capacite) {
        this.conducteurId = conducteurId;
        this.immatriculation = immatriculation;
        this.capacite = capacite;
        this.disponible = true;
    }
    
    public Vehicule(Long id, Long conducteurId, String immatriculation, int capacite,
                   LocalTime heureDebutDisponibilite, LocalTime heureFinDisponibilite, boolean disponible) {
        this.id = id;
        this.conducteurId = conducteurId;
        this.immatriculation = immatriculation;
        this.capacite = capacite;
        this.heureDebutDisponibilite = heureDebutDisponibilite;
        this.heureFinDisponibilite = heureFinDisponibilite;
        this.disponible = disponible;
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getConducteurId() {
        return conducteurId;
    }
    
    public void setConducteurId(Long conducteurId) {
        this.conducteurId = conducteurId;
    }
    
    public String getImmatriculation() {
        return immatriculation;
    }
    
    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }
    
    public int getCapacite() {
        return capacite;
    }
    
    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }
    
    public LocalTime getHeureDebutDisponibilite() {
        return heureDebutDisponibilite;
    }
    
    public void setHeureDebutDisponibilite(LocalTime heureDebutDisponibilite) {
        this.heureDebutDisponibilite = heureDebutDisponibilite;
    }
    
    public LocalTime getHeureFinDisponibilite() {
        return heureFinDisponibilite;
    }
    
    public void setHeureFinDisponibilite(LocalTime heureFinDisponibilite) {
        this.heureFinDisponibilite = heureFinDisponibilite;
    }
    
    public boolean isDisponible() {
        return disponible;
    }
    
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
    
    public Utilisateur getConducteur() {
        return conducteur;
    }
    
    public void setConducteur(Utilisateur conducteur) {
        this.conducteur = conducteur;
    }
    
    /**
     * Vérifie si le véhicule est disponible à une heure donnée.
     * 
     * @param heure L'heure à vérifier
     * @return true si disponible, false sinon
     */
    public boolean disponibleA(LocalTime heure) {
        if (!disponible) {
            return false;
        }
        
        if (heureDebutDisponibilite == null || heureFinDisponibilite == null) {
            return true; // Si horaires non définis, toujours disponible
        }
        
        return !heure.isBefore(heureDebutDisponibilite) && 
               !heure.isAfter(heureFinDisponibilite);
    }
    
    /**
     * Vérifie si le véhicule peut accueillir un nombre de passagers donné.
     * 
     * @param nombrePassagers Nombre de passagers à transporter
     * @return true si la capacité est suffisante, false sinon
     */
    public boolean peutAccueillir(int nombrePassagers) {
        return nombrePassagers <= capacite;
    }
    
    /**
     * Calcule le nombre de places restantes.
     * 
     * @param passagersActuels Nombre de passagers actuellement dans le véhicule
     * @return Nombre de places disponibles
     */
    public int placesRestantes(int passagersActuels) {
        return Math.max(0, capacite - passagersActuels);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicule vehicule = (Vehicule) o;
        return Objects.equals(id, vehicule.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Vehicule{" +
                "id=" + id +
                ", conducteurId=" + conducteurId +
                ", immatriculation='" + immatriculation + '\'' +
                ", capacite=" + capacite +
                ", disponible=" + disponible +
                '}';
    }
}
