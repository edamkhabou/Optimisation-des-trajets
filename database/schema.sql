-- ============================================
-- Schéma de base de données pour l'application
-- de covoiturage avec optimisation de trajets
-- ============================================

-- Création de la base de données
DROP DATABASE IF EXISTS covoiturage_db;
CREATE DATABASE covoiturage_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE covoiturage_db;

-- ============================================
-- TABLE: utilisateurs
-- Stocke les informations des utilisateurs
-- ============================================
CREATE TABLE utilisateurs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    adresse_depart VARCHAR(255) NOT NULL,
    adresse_arrivee VARCHAR(255) NOT NULL,
    heure_depart TIME,
    heure_arrivee TIME,
    preferences TEXT COMMENT 'JSON: préférences utilisateur',
    groupe VARCHAR(50) COMMENT 'Groupe d''appartenance (entreprise, école, etc.)',
    latitude DECIMAL(10, 8) COMMENT 'Latitude du point de départ',
    longitude DECIMAL(11, 8) COMMENT 'Longitude du point de départ',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_groupe (groupe),
    INDEX idx_horaires (heure_depart, heure_arrivee)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: vehicules
-- Stocke les informations des véhicules
-- ============================================
CREATE TABLE vehicules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conducteur_id BIGINT NOT NULL,
    immatriculation VARCHAR(20) NOT NULL UNIQUE,
    capacite INT NOT NULL CHECK (capacite > 0),
    heure_debut_disponibilite TIME,
    heure_fin_disponibilite TIME,
    disponible BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (conducteur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    INDEX idx_conducteur (conducteur_id),
    INDEX idx_disponibilite (disponible)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: trajets
-- Stocke les trajets optimisés
-- ============================================
CREATE TABLE trajets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vehicule_id BIGINT NOT NULL,
    distance_totale DECIMAL(10, 2) DEFAULT 0 COMMENT 'Distance en kilomètres',
    temps_total_minutes DECIMAL(10, 2) DEFAULT 0 COMMENT 'Temps en minutes',
    route_polyline TEXT COMMENT 'Polyline encodée pour Google Maps',
    optimise BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (vehicule_id) REFERENCES vehicules(id) ON DELETE CASCADE,
    INDEX idx_vehicule (vehicule_id),
    INDEX idx_optimise (optimise)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: trajet_utilisateurs
-- Table de liaison entre trajets et utilisateurs
-- Ordre = ordre de prise en charge
-- ============================================
CREATE TABLE trajet_utilisateurs (
    trajet_id BIGINT NOT NULL,
    utilisateur_id BIGINT NOT NULL,
    ordre_prise_en_charge INT NOT NULL COMMENT 'Ordre dans lequel l''utilisateur est récupéré',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (trajet_id, utilisateur_id),
    FOREIGN KEY (trajet_id) REFERENCES trajets(id) ON DELETE CASCADE,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    INDEX idx_ordre (trajet_id, ordre_prise_en_charge)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: conflits
-- Stocke l'historique des conflits détectés
-- ============================================
CREATE TABLE conflits (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type_conflit ENUM('CAPACITE', 'HORAIRE', 'PREFERENCE', 'DISPONIBILITE') NOT NULL,
    message VARCHAR(255) NOT NULL,
    trajet_id BIGINT,
    utilisateur_id BIGINT,
    vehicule_id BIGINT,
    details TEXT,
    resolu BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (trajet_id) REFERENCES trajets(id) ON DELETE SET NULL,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE SET NULL,
    FOREIGN KEY (vehicule_id) REFERENCES vehicules(id) ON DELETE SET NULL,
    INDEX idx_type (type_conflit),
    INDEX idx_resolu (resolu),
    INDEX idx_date (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- VUES UTILES
-- ============================================

-- Vue: Trajets avec informations complètes
CREATE VIEW v_trajets_complets AS
SELECT 
    t.id,
    t.vehicule_id,
    v.immatriculation,
    v.capacite,
    u.nom AS conducteur_nom,
    t.distance_totale,
    t.temps_total_minutes,
    t.optimise,
    COUNT(tu.utilisateur_id) AS nombre_passagers,
    ROUND((COUNT(tu.utilisateur_id) * 100.0 / v.capacite), 2) AS taux_remplissage,
    t.created_at
FROM trajets t
JOIN vehicules v ON t.vehicule_id = v.id
JOIN utilisateurs u ON v.conducteur_id = u.id
LEFT JOIN trajet_utilisateurs tu ON t.id = tu.trajet_id
GROUP BY t.id, t.vehicule_id, v.immatriculation, v.capacite, u.nom, 
         t.distance_totale, t.temps_total_minutes, t.optimise, t.created_at;

-- Vue: Statistiques globales
CREATE VIEW v_statistiques AS
SELECT 
    (SELECT COUNT(*) FROM trajets) AS total_trajets,
    (SELECT COUNT(*) FROM vehicules) AS total_vehicules,
    (SELECT COUNT(*) FROM utilisateurs) AS total_utilisateurs,
    ROUND((SELECT AVG(distance_totale) FROM trajets), 2) AS distance_moyenne,
    ROUND((SELECT AVG(temps_total_minutes) FROM trajets), 2) AS temps_moyen,
    ROUND(AVG(taux_remplissage), 2) AS taux_remplissage_moyen,
    (SELECT COUNT(*) FROM trajets WHERE optimise = TRUE) AS trajets_optimises
FROM (
    SELECT 
        t.id,
        ROUND((COUNT(tu.utilisateur_id) * 100.0 / v.capacite), 2) AS taux_remplissage
    FROM trajets t
    JOIN vehicules v ON t.vehicule_id = v.id
    LEFT JOIN trajet_utilisateurs tu ON t.id = tu.trajet_id
    GROUP BY t.id, v.capacite
) AS trajets_stats;

-- ============================================
-- DONNÉES DE TEST
-- ============================================

-- Insertion d'utilisateurs de test
INSERT INTO utilisateurs (nom, adresse_depart, adresse_arrivee, heure_depart, heure_arrivee, groupe, latitude, longitude) VALUES
('Alice Martin', '15 Rue de la Paix, Paris', '50 Avenue des Champs-Élysées, Paris', '08:00:00', '09:00:00', 'Entreprise A', 48.8698, 2.3322),
('Bob Dupont', '25 Boulevard Saint-Michel, Paris', '100 Rue de Rivoli, Paris', '08:15:00', '09:15:00', 'Entreprise A', 48.8534, 2.3438),
('Charlie Rousseau', '30 Rue du Faubourg Saint-Antoine, Paris', '75 Rue de la Pompe, Paris', '08:30:00', '09:30:00', 'Entreprise B', 48.8510, 2.3736),
('Diana Laurent', '45 Rue de Belleville, Paris', '20 Rue de Vaugirard, Paris', '07:45:00', '08:45:00', 'Entreprise A', 48.8720, 2.3825),
('Etienne Bernard', '10 Rue Montmartre, Paris', '35 Boulevard Haussmann, Paris', '08:00:00', '09:00:00', 'Entreprise C', 48.8656, 2.3422),
('Fanny Petit', '60 Rue Saint-Denis, Paris', '90 Rue du Temple, Paris', '08:20:00', '09:20:00', 'Entreprise B', 48.8634, 2.3516);

-- Insertion de véhicules de test
INSERT INTO vehicules (conducteur_id, immatriculation, capacite, heure_debut_disponibilite, heure_fin_disponibilite, disponible) VALUES
(1, 'AB-123-CD', 4, '07:00:00', '10:00:00', TRUE),
(3, 'EF-456-GH', 3, '07:30:00', '10:30:00', TRUE),
(5, 'IJ-789-KL', 5, '07:00:00', '11:00:00', TRUE);

-- Insertion de trajets de test
INSERT INTO trajets (vehicule_id, distance_totale, temps_total_minutes, optimise) VALUES
(1, 12.5, 35, FALSE),
(2, 15.3, 42, FALSE);

-- Association utilisateurs - trajets
INSERT INTO trajet_utilisateurs (trajet_id, utilisateur_id, ordre_prise_en_charge) VALUES
(1, 2, 1),
(1, 4, 2),
(2, 6, 1);

-- Insertion de conflits de test
INSERT INTO conflits (type_conflit, message, trajet_id, vehicule_id, resolu) VALUES
('CAPACITE', 'Capacité du véhicule dépassée: 5 passagers pour 4 places', 1, 1, FALSE),
('HORAIRE', 'Conflit d''horaires entre utilisateurs', NULL, NULL, TRUE);

-- ============================================
-- PROCÉDURES STOCKÉES UTILES
-- ============================================

-- Procédure: Obtenir les utilisateurs compatibles pour un trajet
DELIMITER $$

CREATE PROCEDURE sp_utilisateurs_compatibles(
    IN p_vehicule_id BIGINT
)
BEGIN
    SELECT u.*
    FROM utilisateurs u
    WHERE NOT EXISTS (
        SELECT 1 
        FROM trajet_utilisateurs tu
        JOIN trajets t ON tu.trajet_id = t.id
        WHERE t.vehicule_id = p_vehicule_id
        AND tu.utilisateur_id = u.id
    );
END$$

-- Procédure: Calculer les statistiques d'un trajet
CREATE PROCEDURE sp_statistiques_trajet(
    IN p_trajet_id BIGINT
)
BEGIN
    SELECT 
        t.id,
        t.distance_totale,
        t.temps_total_minutes,
        COUNT(tu.utilisateur_id) AS nombre_passagers,
        v.capacite,
        ROUND((COUNT(tu.utilisateur_id) * 100.0 / v.capacite), 2) AS taux_remplissage,
        ROUND(t.distance_totale / NULLIF(COUNT(tu.utilisateur_id), 0), 2) AS distance_par_passager,
        ROUND(t.temps_total_minutes / NULLIF(COUNT(tu.utilisateur_id), 0), 2) AS temps_par_passager
    FROM trajets t
    JOIN vehicules v ON t.vehicule_id = v.id
    LEFT JOIN trajet_utilisateurs tu ON t.id = tu.trajet_id
    WHERE t.id = p_trajet_id
    GROUP BY t.id, t.distance_totale, t.temps_total_minutes, v.capacite;
END$$

DELIMITER ;

-- ============================================
-- TRIGGERS
-- ============================================

-- Trigger: Vérifier la capacité avant insertion
DELIMITER $$

CREATE TRIGGER tr_check_capacite_before_insert
BEFORE INSERT ON trajet_utilisateurs
FOR EACH ROW
BEGIN
    DECLARE v_capacite INT;
    DECLARE v_nb_passagers INT;
    
    -- Récupérer la capacité du véhicule
    SELECT v.capacite INTO v_capacite
    FROM trajets t
    JOIN vehicules v ON t.vehicule_id = v.id
    WHERE t.id = NEW.trajet_id;
    
    -- Compter les passagers actuels
    SELECT COUNT(*) INTO v_nb_passagers
    FROM trajet_utilisateurs
    WHERE trajet_id = NEW.trajet_id;
    
    -- Vérifier si on peut ajouter
    IF v_nb_passagers >= v_capacite THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Capacité du véhicule dépassée';
    END IF;
END$$

DELIMITER ;

-- ============================================
-- INDEX SUPPLÉMENTAIRES POUR PERFORMANCE
-- ============================================

CREATE INDEX idx_utilisateurs_adresses ON utilisateurs(adresse_depart(50), adresse_arrivee(50));
CREATE INDEX idx_trajets_metrics ON trajets(distance_totale, temps_total_minutes);

-- ============================================
-- FIN DU SCRIPT
-- ============================================

-- Afficher un résumé
SELECT 'Base de données créée avec succès!' AS status;
SELECT COUNT(*) AS nb_utilisateurs FROM utilisateurs;
SELECT COUNT(*) AS nb_vehicules FROM vehicules;
SELECT COUNT(*) AS nb_trajets FROM trajets;
