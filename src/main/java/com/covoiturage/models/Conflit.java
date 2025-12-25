package com.covoiturage.models;

/**
 * Modèle représentant un conflit détecté dans le système.
 * 
 * Types de conflits :
 * - CAPACITE : Dépassement de capacité du véhicule
 * - HORAIRE : Chevauchement d'horaires incompatible
 * - PREFERENCE : Violation des préférences utilisateur
 */
public class Conflit {
    
    public enum TypeConflit {
        CAPACITE("Dépassement de capacité"),
        HORAIRE("Conflit d'horaires"),
        PREFERENCE("Préférence non respectée"),
        DISPONIBILITE("Véhicule non disponible");
        
        private final String description;
        
        TypeConflit(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private TypeConflit type;
    private String message;
    private Long trajetId;
    private Long utilisateurId;
    private Long vehiculeId;
    private String details;
    
    // Constructeurs
    public Conflit() {}
    
    public Conflit(TypeConflit type, String message) {
        this.type = type;
        this.message = message;
    }
    
    public Conflit(TypeConflit type, String message, Long trajetId) {
        this.type = type;
        this.message = message;
        this.trajetId = trajetId;
    }
    
    // Getters et Setters
    public TypeConflit getType() {
        return type;
    }
    
    public void setType(TypeConflit type) {
        this.type = type;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Long getTrajetId() {
        return trajetId;
    }
    
    public void setTrajetId(Long trajetId) {
        this.trajetId = trajetId;
    }
    
    public Long getUtilisateurId() {
        return utilisateurId;
    }
    
    public void setUtilisateurId(Long utilisateurId) {
        this.utilisateurId = utilisateurId;
    }
    
    public Long getVehiculeId() {
        return vehiculeId;
    }
    
    public void setVehiculeId(Long vehiculeId) {
        this.vehiculeId = vehiculeId;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    @Override
    public String toString() {
        return "Conflit{" +
                "type=" + type +
                ", message='" + message + '\'' +
                ", trajetId=" + trajetId +
                ", utilisateurId=" + utilisateurId +
                ", vehiculeId=" + vehiculeId +
                '}';
    }
}
