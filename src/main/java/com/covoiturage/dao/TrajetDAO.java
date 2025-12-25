package com.covoiturage.dao;

import com.covoiturage.models.Trajet;
import com.covoiturage.models.Utilisateur;
import com.covoiturage.utils.DatabaseManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO (Data Access Object) pour l'entité Trajet.
 * 
 * Gère toutes les opérations CRUD sur la table trajets
 * et la table de liaison trajet_utilisateurs.
 */
public class TrajetDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(TrajetDAO.class);
    private final DatabaseManager dbManager;
    private final UtilisateurDAO utilisateurDAO;
    
    public TrajetDAO() {
        this.dbManager = DatabaseManager.getInstance();
        this.utilisateurDAO = new UtilisateurDAO();
    }
    
    /**
     * Crée un nouveau trajet dans la base de données.
     * 
     * @param trajet Le trajet à créer
     * @return Le trajet créé avec son ID généré
     * @throws SQLException En cas d'erreur SQL
     */
    public Trajet create(Trajet trajet) throws SQLException {
        String sql = "INSERT INTO trajets (vehicule_id, distance_totale, temps_total_minutes, " +
                     "route_polyline, optimise) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false); // Début de transaction
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setLong(1, trajet.getVehiculeId());
                pstmt.setDouble(2, trajet.getDistanceTotale());
                pstmt.setDouble(3, trajet.getTempsTotalMinutes());
                pstmt.setString(4, trajet.getRoutePolyline());
                pstmt.setBoolean(5, trajet.isOptimise());
                
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("Échec de la création du trajet");
                }
                
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        trajet.setId(generatedKeys.getLong(1));
                        logger.info("Trajet créé avec ID: {}", trajet.getId());
                    }
                }
            }
            
            // Associer les utilisateurs au trajet
            if (trajet.getUtilisateurs() != null && !trajet.getUtilisateurs().isEmpty()) {
                associerUtilisateurs(conn, trajet);
            }
            
            conn.commit();
            return trajet;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Erreur lors du rollback", ex);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Erreur lors de la fermeture de la connexion", e);
                }
            }
        }
    }
    
    /**
     * Récupère un trajet par son ID avec ses utilisateurs.
     * 
     * @param id L'ID du trajet
     * @return Optional contenant le trajet s'il existe
     * @throws SQLException En cas d'erreur SQL
     */
    public Optional<Trajet> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM trajets WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Trajet trajet = mapResultSetToTrajet(rs);
                    chargerUtilisateurs(trajet);
                    return Optional.of(trajet);
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Récupère tous les trajets avec leurs utilisateurs.
     * 
     * @return Liste de tous les trajets
     * @throws SQLException En cas d'erreur SQL
     */
    public List<Trajet> findAll() throws SQLException {
        List<Trajet> trajets = new ArrayList<>();
        String sql = "SELECT * FROM trajets ORDER BY id DESC";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Trajet trajet = mapResultSetToTrajet(rs);
                chargerUtilisateurs(trajet);
                trajets.add(trajet);
            }
        }
        
        logger.info("Récupération de {} trajets", trajets.size());
        return trajets;
    }
    
    /**
     * Récupère les trajets par véhicule.
     * 
     * @param vehiculeId L'ID du véhicule
     * @return Liste des trajets du véhicule
     * @throws SQLException En cas d'erreur SQL
     */
    public List<Trajet> findByVehicule(Long vehiculeId) throws SQLException {
        List<Trajet> trajets = new ArrayList<>();
        String sql = "SELECT * FROM trajets WHERE vehicule_id = ? ORDER BY id DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, vehiculeId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Trajet trajet = mapResultSetToTrajet(rs);
                    chargerUtilisateurs(trajet);
                    trajets.add(trajet);
                }
            }
        }
        
        return trajets;
    }
    
    /**
     * Met à jour un trajet existant.
     * 
     * @param trajet Le trajet à mettre à jour
     * @return true si la mise à jour a réussi, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean update(Trajet trajet) throws SQLException {
        String sql = "UPDATE trajets SET vehicule_id = ?, distance_totale = ?, " +
                     "temps_total_minutes = ?, route_polyline = ?, optimise = ? WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, trajet.getVehiculeId());
                pstmt.setDouble(2, trajet.getDistanceTotale());
                pstmt.setDouble(3, trajet.getTempsTotalMinutes());
                pstmt.setString(4, trajet.getRoutePolyline());
                pstmt.setBoolean(5, trajet.isOptimise());
                pstmt.setLong(6, trajet.getId());
                
                int affectedRows = pstmt.executeUpdate();
                
                // Supprimer les anciennes associations
                supprimerAssociations(conn, trajet.getId());
                
                // Réassocier les utilisateurs
                if (trajet.getUtilisateurs() != null && !trajet.getUtilisateurs().isEmpty()) {
                    associerUtilisateurs(conn, trajet);
                }
                
                conn.commit();
                logger.info("Trajet {} mis à jour", trajet.getId());
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Erreur lors du rollback", ex);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Erreur lors de la fermeture de la connexion", e);
                }
            }
        }
    }
    
    /**
     * Supprime un trajet par son ID.
     * 
     * @param id L'ID du trajet à supprimer
     * @return true si la suppression a réussi, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM trajets WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Trajet {} supprimé", id);
                return true;
            }
            
            return false;
        }
    }
    
    /**
     * Associe des utilisateurs à un trajet dans l'ordre spécifié.
     * 
     * @param conn La connexion SQL
     * @param trajet Le trajet contenant les utilisateurs
     * @throws SQLException En cas d'erreur SQL
     */
    private void associerUtilisateurs(Connection conn, Trajet trajet) throws SQLException {
        String sql = "INSERT INTO trajet_utilisateurs (trajet_id, utilisateur_id, ordre_prise_en_charge) " +
                     "VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int ordre = 1;
            for (Utilisateur utilisateur : trajet.getUtilisateurs()) {
                pstmt.setLong(1, trajet.getId());
                pstmt.setLong(2, utilisateur.getId());
                pstmt.setInt(3, ordre++);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
    
    /**
     * Supprime les associations utilisateurs d'un trajet.
     * 
     * @param conn La connexion SQL
     * @param trajetId L'ID du trajet
     * @throws SQLException En cas d'erreur SQL
     */
    private void supprimerAssociations(Connection conn, Long trajetId) throws SQLException {
        String sql = "DELETE FROM trajet_utilisateurs WHERE trajet_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, trajetId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Charge les utilisateurs associés à un trajet.
     * 
     * @param trajet Le trajet
     * @throws SQLException En cas d'erreur SQL
     */
    private void chargerUtilisateurs(Trajet trajet) throws SQLException {
        String sql = "SELECT u.* FROM utilisateurs u " +
                     "JOIN trajet_utilisateurs tu ON u.id = tu.utilisateur_id " +
                     "WHERE tu.trajet_id = ? ORDER BY tu.ordre_prise_en_charge";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, trajet.getId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Utilisateur> utilisateurs = new ArrayList<>();
                while (rs.next()) {
                    utilisateurs.add(mapResultSetToUtilisateur(rs));
                }
                trajet.setUtilisateurs(utilisateurs);
            }
        }
    }
    
    /**
     * Mappe un ResultSet vers un objet Trajet.
     * 
     * @param rs Le ResultSet
     * @return Le trajet créé depuis le ResultSet
     * @throws SQLException En cas d'erreur SQL
     */
    private Trajet mapResultSetToTrajet(ResultSet rs) throws SQLException {
        Trajet trajet = new Trajet();
        trajet.setId(rs.getLong("id"));
        trajet.setVehiculeId(rs.getLong("vehicule_id"));
        trajet.setDistanceTotale(rs.getDouble("distance_totale"));
        trajet.setTempsTotalMinutes(rs.getDouble("temps_total_minutes"));
        trajet.setRoutePolyline(rs.getString("route_polyline"));
        trajet.setOptimise(rs.getBoolean("optimise"));
        
        return trajet;
    }
    
    /**
     * Mappe un ResultSet vers un objet Utilisateur (méthode auxiliaire).
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
        
        return utilisateur;
    }
}
