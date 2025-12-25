package com.covoiturage.optimization;

import java.util.ArrayList;
import java.util.List;

import com.covoiturage.models.Utilisateur;

/**
 * Classe représentant une solution pour le problème d'optimisation de trajets.
 * 
 * Une solution contient :
 * - L'ordre de visite des utilisateurs
 * - La distance totale calculée
 * - Le temps total calculé
 */
public class Solution {
    
    private List<Utilisateur> ordreUtilisateurs;
    private double distanceTotale;
    private double tempsTotalMinutes;
    private double cout; // Fonction objectif combinée
    
    /**
     * Constructeur.
     */
    public Solution() {
        this.ordreUtilisateurs = new ArrayList<>();
        this.distanceTotale = 0.0;
        this.tempsTotalMinutes = 0.0;
        this.cout = 0.0;
    }
    
    /**
     * Constructeur avec liste d'utilisateurs.
     * 
     * @param utilisateurs Liste des utilisateurs
     */
    public Solution(List<Utilisateur> utilisateurs) {
        this.ordreUtilisateurs = new ArrayList<>(utilisateurs);
        this.distanceTotale = 0.0;
        this.tempsTotalMinutes = 0.0;
        this.cout = 0.0;
    }
    
    /**
     * Crée une copie profonde de la solution.
     * 
     * @return Une nouvelle instance de Solution
     */
    public Solution copier() {
        Solution copie = new Solution();
        copie.ordreUtilisateurs = new ArrayList<>(this.ordreUtilisateurs);
        copie.distanceTotale = this.distanceTotale;
        copie.tempsTotalMinutes = this.tempsTotalMinutes;
        copie.cout = this.cout;
        return copie;
    }
    
    /**
     * Échange deux utilisateurs dans l'ordre de visite.
     * 
     * @param i Index du premier utilisateur
     * @param j Index du second utilisateur
     */
    public void echangerUtilisateurs(int i, int j) {
        if (i >= 0 && i < ordreUtilisateurs.size() && j >= 0 && j < ordreUtilisateurs.size()) {
            Utilisateur temp = ordreUtilisateurs.get(i);
            ordreUtilisateurs.set(i, ordreUtilisateurs.get(j));
            ordreUtilisateurs.set(j, temp);
        }
    }
    
    /**
     * Inverse l'ordre d'un segment du trajet (opération 2-opt).
     * 
     * @param debut Index de début du segment
     * @param fin Index de fin du segment
     */
    public void inverserSegment(int debut, int fin) {
        while (debut < fin) {
            echangerUtilisateurs(debut, fin);
            debut++;
            fin--;
        }
    }
    
    /**
     * Calcule le coût de la solution (fonction objectif).
     * Combine distance et temps avec des poids.
     * 
     * @param poidsDistance Poids de la distance (ex: 0.7)
     * @param poidsTemps Poids du temps (ex: 0.3)
     */
    public void calculerCout(double poidsDistance, double poidsTemps) {
        // Normaliser les valeurs (optionnel, dépend de l'échelle)
        this.cout = (poidsDistance * distanceTotale) + (poidsTemps * tempsTotalMinutes);
    }
    
    /**
     * Calcule le coût avec des poids par défaut.
     */
    public void calculerCout() {
        calculerCout(0.7, 0.3); // 70% distance, 30% temps
    }
    
    // Getters et Setters
    public List<Utilisateur> getOrdreUtilisateurs() {
        return ordreUtilisateurs;
    }
    
    public void setOrdreUtilisateurs(List<Utilisateur> ordreUtilisateurs) {
        this.ordreUtilisateurs = ordreUtilisateurs;
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
    
    public double getCout() {
        return cout;
    }
    
    public void setCout(double cout) {
        this.cout = cout;
    }
    
    @Override
    public String toString() {
        return "Solution{" +
                "nombreUtilisateurs=" + ordreUtilisateurs.size() +
                ", distanceTotale=" + String.format("%.2f", distanceTotale) + " km" +
                ", tempsTotalMinutes=" + String.format("%.2f", tempsTotalMinutes) + " min" +
                ", cout=" + String.format("%.2f", cout) +
                '}';
    }
}
