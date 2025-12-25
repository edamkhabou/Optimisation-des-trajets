package com.covoiturage.models;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Modèle représentant un utilisateur du système de covoiturage.
 * 
 * Contient les informations de l'utilisateur incluant :
 * - Identification (id, nom)
 * - Adresses (départ, arrivée)
 * - Contraintes temporelles (horaires)
 * - Préférences et groupe
 */
public class Utilisateur {
    
    private Long id;
    private String nom;
    private String adresseDepart;
    private String adresseArrivee;
    private LocalTime heureDepart;
    private LocalTime heureArrivee;
    private String preferences; // JSON: {"eviter": ["arrêt1", "arrêt2"], "priorite": true}
    private String groupe; // Groupe de l'utilisateur (même entreprise, même école, etc.)
    private Double latitude;  // Latitude du point de départ
    private Double longitude; // Longitude du point de départ
    
    // Constructeurs
    public Utilisateur() {}
    
    public Utilisateur(String nom, String adresseDepart, String adresseArrivee, 
                      LocalTime heureDepart, LocalTime heureArrivee) {
        this.nom = nom;
        this.adresseDepart = adresseDepart;
        this.adresseArrivee = adresseArrivee;
        this.heureDepart = heureDepart;
        this.heureArrivee = heureArrivee;
    }
    
    public Utilisateur(Long id, String nom, String adresseDepart, String adresseArrivee,
                      LocalTime heureDepart, LocalTime heureArrivee, String preferences, String groupe) {
        this.id = id;
        this.nom = nom;
        this.adresseDepart = adresseDepart;
        this.adresseArrivee = adresseArrivee;
        this.heureDepart = heureDepart;
        this.heureArrivee = heureArrivee;
        this.preferences = preferences;
        this.groupe = groupe;
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getAdresseDepart() {
        return adresseDepart;
    }
    
    public void setAdresseDepart(String adresseDepart) {
        this.adresseDepart = adresseDepart;
    }
    
    public String getAdresseArrivee() {
        return adresseArrivee;
    }
    
    public void setAdresseArrivee(String adresseArrivee) {
        this.adresseArrivee = adresseArrivee;
    }
    
    public LocalTime getHeureDepart() {
        return heureDepart;
    }
    
    public void setHeureDepart(LocalTime heureDepart) {
        this.heureDepart = heureDepart;
    }
    
    public LocalTime getHeureArrivee() {
        return heureArrivee;
    }
    
    public void setHeureArrivee(LocalTime heureArrivee) {
        this.heureArrivee = heureArrivee;
    }
    
    public String getPreferences() {
        return preferences;
    }
    
    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }
    
    public String getGroupe() {
        return groupe;
    }
    
    public void setGroupe(String groupe) {
        this.groupe = groupe;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    /**
     * Vérifie si les horaires de cet utilisateur sont compatibles avec un autre utilisateur.
     * 
     * @param autre L'autre utilisateur à comparer
     * @return true si les horaires sont compatibles (chevauchement), false sinon
     */
    public boolean horairesCompatibles(Utilisateur autre) {
        if (this.heureDepart == null || this.heureArrivee == null ||
            autre.heureDepart == null || autre.heureArrivee == null) {
            return true; // Si horaires non définis, considérer comme compatibles
        }
        
        // Vérifier si les plages horaires se chevauchent
        return !this.heureArrivee.isBefore(autre.heureDepart) && 
               !autre.heureArrivee.isBefore(this.heureDepart);
    }
    
    /**
     * Vérifie si cet utilisateur appartient au même groupe qu'un autre utilisateur.
     * 
     * @param autre L'autre utilisateur à comparer
     * @return true si même groupe, false sinon
     */
    public boolean memeGroupe(Utilisateur autre) {
        return this.groupe != null && this.groupe.equals(autre.getGroupe());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Utilisateur that = (Utilisateur) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", adresseDepart='" + adresseDepart + '\'' +
                ", adresseArrivee='" + adresseArrivee + '\'' +
                ", heureDepart=" + heureDepart +
                ", heureArrivee=" + heureArrivee +
                ", groupe='" + groupe + '\'' +
                '}';
    }
}
