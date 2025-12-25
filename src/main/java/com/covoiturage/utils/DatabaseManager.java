package com.covoiturage.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gestionnaire de connexions à la base de données.
 * 
 * Utilise le pattern Singleton pour gérer une seule instance.
 * Charge la configuration depuis db.properties.
 */
public class DatabaseManager {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static DatabaseManager instance;
    
    private String url;
    private String username;
    private String password;
    private String driver;
    
    /**
     * Constructeur privé (pattern Singleton).
     * Charge la configuration depuis db.properties.
     */
    private DatabaseManager() {
        loadConfiguration();
    }
    
    /**
     * Obtient l'instance unique du gestionnaire.
     * 
     * @return Instance du DatabaseManager
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Charge la configuration depuis le fichier db.properties.
     */
    private void loadConfiguration() {
        Properties props = new Properties();
        
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                logger.error("Fichier db.properties introuvable dans le classpath");
                setDefaultConfiguration();
                return;
            }
            
            props.load(input);
            
            this.url = props.getProperty("db.url");
            this.username = props.getProperty("db.username");
            this.password = props.getProperty("db.password");
            this.driver = props.getProperty("db.driver");
            
            // Charger le driver JDBC
            Class.forName(driver);
            
            logger.info("Configuration de base de données chargée avec succès");
            
        } catch (IOException e) {
            logger.error("Erreur lors du chargement de db.properties", e);
            setDefaultConfiguration();
        } catch (ClassNotFoundException e) {
            logger.error("Driver JDBC introuvable: " + driver, e);
        }
    }
    
    /**
     * Définit une configuration par défaut si le fichier properties n'est pas trouvé.
     */
    private void setDefaultConfiguration() {
        this.url = "jdbc:mysql://localhost:3306/covoiturage_db?useSSL=false&serverTimezone=UTC";
        this.username = "root";
        this.password = "";
        this.driver = "com.mysql.cj.jdbc.Driver";
        
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            logger.error("Driver JDBC par défaut introuvable", e);
        }
    }
    
    /**
     * Obtient une connexion à la base de données.
     * 
     * @return Connexion active
     * @throws SQLException Si la connexion échoue
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
    
    /**
     * Ferme une connexion de manière sécurisée.
     * 
     * @param connection La connexion à fermer
     */
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                logger.debug("Connexion fermée");
            } catch (SQLException e) {
                logger.error("Erreur lors de la fermeture de la connexion", e);
            }
        }
    }
    
    /**
     * Teste la connexion à la base de données.
     * 
     * @return true si la connexion réussit, false sinon
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean valid = conn.isValid(5);
            if (valid) {
                logger.info("Test de connexion réussi");
            }
            return valid;
        } catch (SQLException e) {
            logger.error("Test de connexion échoué", e);
            return false;
        }
    }
    
    // Getters
    public String getUrl() {
        return url;
    }
    
    public String getUsername() {
        return username;
    }
}
