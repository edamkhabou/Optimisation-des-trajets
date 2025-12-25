package com.covoiturage.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.covoiturage.models.Utilisateur;
import com.covoiturage.utils.DatabaseManager;

/**
 * DAO (Data Access Object) pour l'entité Utilisateur.
 * 
 * Gère toutes les opérations CRUD sur la table utilisateurs.
 */
public class UtilisateurDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(UtilisateurDAO.class);
    private final DatabaseManager dbManager;
    
    public UtilisateurDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * Crée un nouvel utilisateur dans la base de données.
     * 
     * @param utilisateur L'utilisateur à créer
     * @return L'utilisateur créé avec son ID généré
     * @throws SQLException En cas d'erreur SQL
     */
    public Utilisateur create(Utilisateur utilisateur) throws SQLException {
        String sql = "INSERT INTO utilisateurs (nom, adresse_depart, adresse_arrivee, " +
                     "heure_depart, heure_arrivee, preferences, groupe, latitude, longitude) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, utilisateur.getNom());
            pstmt.setString(2, utilisateur.getAdresseDepart());
            pstmt.setString(3, utilisateur.getAdresseArrivee());
            pstmt.setObject(4, utilisateur.getHeureDepart());
            pstmt.setObject(5, utilisateur.getHeureArrivee());
            pstmt.setString(6, utilisateur.getPreferences());
            pstmt.setString(7, utilisateur.getGroupe());
            
            if (utilisateur.getLatitude() != null) {
                pstmt.setDouble(8, utilisateur.getLatitude());
            } else {
                pstmt.setNull(8, Types.DECIMAL);
            }
            
            if (utilisateur.getLongitude() != null) {
                pstmt.setDouble(9, utilisateur.getLongitude());
            } else {
                pstmt.setNull(9, Types.DECIMAL);
            }
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Échec de la création de l'utilisateur");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    utilisateur.setId(generatedKeys.getLong(1));
                    logger.info("Utilisateur créé avec ID: {}", utilisateur.getId());
                }
            }
            
            return utilisateur;
        }
    }
    
    /**
     * Récupère un utilisateur par son ID.
     * 
     * @param id L'ID de l'utilisateur
     * @return Optional contenant l'utilisateur s'il existe
     * @throws SQLException En cas d'erreur SQL
     */
    public Optional<Utilisateur> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM utilisateurs WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUtilisateur(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Récupère tous les utilisateurs.
     * 
     * @return Liste de tous les utilisateurs
     * @throws SQLException En cas d'erreur SQL
     */
    public List<Utilisateur> findAll() throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs ORDER BY nom";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                utilisateurs.add(mapResultSetToUtilisateur(rs));
            }
        }
        
        logger.info("Récupération de {} utilisateurs", utilisateurs.size());
        return utilisateurs;
    }
    
    /**
     * Récupère les utilisateurs d'un même groupe.
     * 
     * @param groupe Le groupe recherché
     * @return Liste des utilisateurs du groupe
     * @throws SQLException En cas d'erreur SQL
     */
    public List<Utilisateur> findByGroupe(String groupe) throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs WHERE groupe = ? ORDER BY nom";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, groupe);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    utilisateurs.add(mapResultSetToUtilisateur(rs));
                }
            }
        }
        
        return utilisateurs;
    }
    
    /**
     * Met à jour un utilisateur existant.
     * 
     * @param utilisateur L'utilisateur à mettre à jour
     * @return true si la mise à jour a réussi, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean update(Utilisateur utilisateur) throws SQLException {
        String sql = "UPDATE utilisateurs SET nom = ?, adresse_depart = ?, adresse_arrivee = ?, " +
                     "heure_depart = ?, heure_arrivee = ?, preferences = ?, groupe = ?, " +
                     "latitude = ?, longitude = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, utilisateur.getNom());
            pstmt.setString(2, utilisateur.getAdresseDepart());
            pstmt.setString(3, utilisateur.getAdresseArrivee());
            pstmt.setObject(4, utilisateur.getHeureDepart());
            pstmt.setObject(5, utilisateur.getHeureArrivee());
            pstmt.setString(6, utilisateur.getPreferences());
            pstmt.setString(7, utilisateur.getGroupe());
            
            if (utilisateur.getLatitude() != null) {
                pstmt.setDouble(8, utilisateur.getLatitude());
            } else {
                pstmt.setNull(8, Types.DECIMAL);
            }
            
            if (utilisateur.getLongitude() != null) {
                pstmt.setDouble(9, utilisateur.getLongitude());
            } else {
                pstmt.setNull(9, Types.DECIMAL);
            }
            
            pstmt.setLong(10, utilisateur.getId());
            
            int affectedRows = pstmt.executeUpdate();
            logger.info("Utilisateur {} mis à jour", utilisateur.getId());
            return affectedRows > 0;
        }
    }
    
    /**
     * Supprime un utilisateur par son ID.
     * 
     * @param id L'ID de l'utilisateur à supprimer
     * @return true si la suppression a réussi, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM utilisateurs WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Utilisateur {} supprimé", id);
                return true;
            }
            
            return false;
        }
    }
    
    /**
     * Compte le nombre total d'utilisateurs.
     * 
     * @return Nombre total d'utilisateurs
     * @throws SQLException En cas d'erreur SQL
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateurs";
        
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
     * Mappe un ResultSet vers un objet Utilisateur.
     * 
     * @param rs Le ResultSet
     * @return L'utilisateur créé depuis le ResultSet
     * @throws SQLException En cas d'erreur SQL
     */
    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(rs.getLong("id"));
        utilisateur.setNom(rs.getString("nom"));
        utilisateur.setAdresseDepart(rs.getString("adresse_depart"));
        utilisateur.setAdresseArrivee(rs.getString("adresse_arrivee"));
        
        Time heureDepart = rs.getTime("heure_depart");
        if (heureDepart != null) {
            utilisateur.setHeureDepart(heureDepart.toLocalTime());
        }
        
        Time heureArrivee = rs.getTime("heure_arrivee");
        if (heureArrivee != null) {
            utilisateur.setHeureArrivee(heureArrivee.toLocalTime());
        }
        
        utilisateur.setPreferences(rs.getString("preferences"));
        utilisateur.setGroupe(rs.getString("groupe"));
        
        Double latitude = rs.getDouble("latitude");
        if (!rs.wasNull()) {
            utilisateur.setLatitude(latitude);
        }
        
        Double longitude = rs.getDouble("longitude");
        if (!rs.wasNull()) {
            utilisateur.setLongitude(longitude);
        }
        
        return utilisateur;
    }
}
