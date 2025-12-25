package com.covoiturage.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.covoiturage.models.Vehicule;
import com.covoiturage.utils.DatabaseManager;

/**
 * DAO (Data Access Object) pour l'entité Véhicule.
 * 
 * Gère toutes les opérations CRUD sur la table vehicules.
 */
public class VehiculeDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(VehiculeDAO.class);
    private final DatabaseManager dbManager;
    
    public VehiculeDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * Crée un nouveau véhicule dans la base de données.
     * 
     * @param vehicule Le véhicule à créer
     * @return Le véhicule créé avec son ID généré
     * @throws SQLException En cas d'erreur SQL
     */
    public Vehicule create(Vehicule vehicule) throws SQLException {
        String sql = "INSERT INTO vehicules (conducteur_id, immatriculation, capacite, " +
                     "heure_debut_disponibilite, heure_fin_disponibilite, disponible) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setLong(1, vehicule.getConducteurId());
            pstmt.setString(2, vehicule.getImmatriculation());
            pstmt.setInt(3, vehicule.getCapacite());
            pstmt.setObject(4, vehicule.getHeureDebutDisponibilite());
            pstmt.setObject(5, vehicule.getHeureFinDisponibilite());
            pstmt.setBoolean(6, vehicule.isDisponible());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Échec de la création du véhicule");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    vehicule.setId(generatedKeys.getLong(1));
                    logger.info("Véhicule créé avec ID: {}", vehicule.getId());
                }
            }
            
            return vehicule;
        }
    }
    
    /**
     * Récupère un véhicule par son ID.
     * 
     * @param id L'ID du véhicule
     * @return Optional contenant le véhicule s'il existe
     * @throws SQLException En cas d'erreur SQL
     */
    public Optional<Vehicule> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM vehicules WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVehicule(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Récupère tous les véhicules.
     * 
     * @return Liste de tous les véhicules
     * @throws SQLException En cas d'erreur SQL
     */
    public List<Vehicule> findAll() throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicules ORDER BY id";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                vehicules.add(mapResultSetToVehicule(rs));
            }
        }
        
        logger.info("Récupération de {} véhicules", vehicules.size());
        return vehicules;
    }
    
    /**
     * Récupère les véhicules disponibles.
     * 
     * @return Liste des véhicules disponibles
     * @throws SQLException En cas d'erreur SQL
     */
    public List<Vehicule> findDisponibles() throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicules WHERE disponible = TRUE ORDER BY id";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                vehicules.add(mapResultSetToVehicule(rs));
            }
        }
        
        return vehicules;
    }
    
    /**
     * Récupère les véhicules d'un conducteur.
     * 
     * @param conducteurId L'ID du conducteur
     * @return Liste des véhicules du conducteur
     * @throws SQLException En cas d'erreur SQL
     */
    public List<Vehicule> findByConducteur(Long conducteurId) throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicules WHERE conducteur_id = ? ORDER BY id";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, conducteurId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    vehicules.add(mapResultSetToVehicule(rs));
                }
            }
        }
        
        return vehicules;
    }
    
    /**
     * Met à jour un véhicule existant.
     * 
     * @param vehicule Le véhicule à mettre à jour
     * @return true si la mise à jour a réussi, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean update(Vehicule vehicule) throws SQLException {
        String sql = "UPDATE vehicules SET conducteur_id = ?, immatriculation = ?, capacite = ?, " +
                     "heure_debut_disponibilite = ?, heure_fin_disponibilite = ?, disponible = ? " +
                     "WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, vehicule.getConducteurId());
            pstmt.setString(2, vehicule.getImmatriculation());
            pstmt.setInt(3, vehicule.getCapacite());
            pstmt.setObject(4, vehicule.getHeureDebutDisponibilite());
            pstmt.setObject(5, vehicule.getHeureFinDisponibilite());
            pstmt.setBoolean(6, vehicule.isDisponible());
            pstmt.setLong(7, vehicule.getId());
            
            int affectedRows = pstmt.executeUpdate();
            logger.info("Véhicule {} mis à jour", vehicule.getId());
            return affectedRows > 0;
        }
    }
    
    /**
     * Supprime un véhicule par son ID.
     * 
     * @param id L'ID du véhicule à supprimer
     * @return true si la suppression a réussi, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM vehicules WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Véhicule {} supprimé", id);
                return true;
            }
            
            return false;
        }
    }
    
    /**
     * Compte le nombre total de véhicules.
     * 
     * @return Nombre total de véhicules
     * @throws SQLException En cas d'erreur SQL
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM vehicules";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Mappe un ResultSet vers un objet Vehicule.
     * 
     * @param rs Le ResultSet
     * @return Le véhicule créé depuis le ResultSet
     * @throws SQLException En cas d'erreur SQL
     */
    private Vehicule mapResultSetToVehicule(ResultSet rs) throws SQLException {
        Vehicule vehicule = new Vehicule();
        vehicule.setId(rs.getLong("id"));
        vehicule.setConducteurId(rs.getLong("conducteur_id"));
        vehicule.setImmatriculation(rs.getString("immatriculation"));
        vehicule.setCapacite(rs.getInt("capacite"));
        
        Time heureDebut = rs.getTime("heure_debut_disponibilite");
        if (heureDebut != null) {
            vehicule.setHeureDebutDisponibilite(heureDebut.toLocalTime());
        }
        
        Time heureFin = rs.getTime("heure_fin_disponibilite");
        if (heureFin != null) {
            vehicule.setHeureFinDisponibilite(heureFin.toLocalTime());
        }
        
        vehicule.setDisponible(rs.getBoolean("disponible"));
        
        return vehicule;
    }
}
