/**
 * Gestion de la carte OpenStreetMap avec Leaflet
 * Affichage des trajets optimisés sur la carte
 */

let map;
let markers = [];
let routeLayer = null;
let routingControl = null;

// ============================================
// INITIALISATION DE LA CARTE
// ============================================
function initMap() {
    try {
        // Centre de Paris par défaut
        map = L.map('map').setView([48.8566, 2.3522], 12);
        
        // Ajouter les tuiles OpenStreetMap (gratuites)
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
            maxZoom: 19,
            minZoom: 3
        }).addTo(map);
        
        console.log('✅ Carte OpenStreetMap (Leaflet) initialisée');
    } catch (error) {
        console.error('❌ Erreur initialisation carte:', error);
    }
}

// Initialiser la carte au chargement du DOM
document.addEventListener('DOMContentLoaded', () => {
    console.log('📍 Initialisation de la carte...');
    initMap();
});

// ============================================
// AFFICHAGE DU TRAJET
// ============================================
function afficherTrajetSurCarte(trajet) {
    console.log('🗺️ Affichage du trajet sur la carte:', trajet);
    
    if (!map) {
        console.error('❌ Carte non initialisée');
        alert('⚠️ La carte n\'est pas encore chargée. Veuillez réessayer dans quelques secondes.');
        return;
    }
    
    // Effacer les marqueurs précédents
    effacerMarqueurs();
    
    // Récupérer les utilisateurs du trajet
    const utilisateurs = trajet.utilisateurs;
    
    if (!utilisateurs || utilisateurs.length === 0) {
        console.warn('⚠️ Aucun utilisateur dans le trajet');
        alert('⚠️ Aucun utilisateur à afficher sur la carte');
        return;
    }
    
    console.log(`📌 Affichage de ${utilisateurs.length} utilisateurs`);
    
    // Afficher les marqueurs et la route
    afficherMarqueurs(utilisateurs);
}

// ============================================
// AFFICHAGE DES MARQUEURS
// ============================================
function afficherMarqueurs(utilisateurs) {
    const bounds = [];
    const routePoints = [];
    
    console.log('🔍 Traitement des utilisateurs pour affichage...');
    
    utilisateurs.forEach((user, index) => {
        console.log(`   Utilisateur ${index + 1}:`, user.nom, 
                    `Lat: ${user.latitude}, Lng: ${user.longitude}`);
        
        // Si on a les coordonnées GPS
        if (user.latitude && user.longitude) {
            const lat = parseFloat(user.latitude);
            const lng = parseFloat(user.longitude);
            
            if (isNaN(lat) || isNaN(lng)) {
                console.warn(`⚠️ Coordonnées invalides pour ${user.nom}`);
                return;
            }
            
            const position = [lat, lng];
            
            // Créer une icône numérotée
            const icon = L.divIcon({
                className: 'custom-marker',
                html: `<div style="
                    background-color: #2563eb;
                    color: white;
                    border-radius: 50%;
                    width: 32px;
                    height: 32px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-weight: bold;
                    font-size: 14px;
                    border: 2px solid white;
                    box-shadow: 0 2px 5px rgba(0,0,0,0.3);
                ">${index + 1}</div>`,
                iconSize: [32, 32],
                iconAnchor: [16, 16]
            });
            
            // Créer le marqueur
            const marker = L.marker(position, { icon: icon }).addTo(map);
            
            // Popup au clic
            const popupContent = `
                <div style="min-width: 200px;">
                    <h3 style="margin: 0 0 10px 0; color: #2563eb;">${user.nom}</h3>
                    <p style="margin: 5px 0;"><strong>📍 Départ:</strong><br>${user.adresseDepart}</p>
                    <p style="margin: 5px 0;"><strong>🎯 Arrivée:</strong><br>${user.adresseArrivee}</p>
                    <p style="margin: 5px 0;"><strong>🔢 Ordre:</strong> ${index + 1}</p>
                    ${user.heureDepart ? `<p style="margin: 5px 0;"><strong>🕐 Heure:</strong> ${user.heureDepart}</p>` : ''}
                </div>
            `;
            marker.bindPopup(popupContent);
            
            markers.push(marker);
            bounds.push(position);
            routePoints.push(position);
            
            console.log(`   ✅ Marqueur ${index + 1} créé`);
            
        } else {
            console.warn(`⚠️ Pas de coordonnées pour ${user.nom}, tentative de géocodage...`);
            // Géocoder l'adresse si pas de coordonnées
            geocoderAdresse(user.adresseDepart, index, user);
        }
    });
    
    // Tracer une route qui suit les rues
    if (routePoints.length > 1) {
        console.log(`🛣️ Création de la route avec ${routePoints.length} points`);
        
        // Supprimer l'ancienne route si elle existe
        if (routeLayer) {
            map.removeLayer(routeLayer);
            routeLayer = null;
        }
        if (routingControl) {
            map.removeControl(routingControl);
            routingControl = null;
        }
        
        // Créer le routage qui suit les rues (OSRM)
        const waypoints = routePoints.map(point => L.latLng(point[0], point[1]));
        
        routingControl = L.Routing.control({
            waypoints: waypoints,
            router: L.Routing.osrmv1({
                serviceUrl: 'https://router.project-osrm.org/route/v1'
            }),
            lineOptions: {
                styles: [{ color: '#2563eb', opacity: 0.8, weight: 5 }]
            },
            show: false, // Cacher les instructions
            addWaypoints: false, // Empêcher l'ajout de waypoints
            routeWhileDragging: false,
            draggableWaypoints: false,
            fitSelectedRoutes: false,
            showAlternatives: false,
            createMarker: function() { return null; } // Pas de marqueurs du routing
        }).addTo(map);
        
        console.log('✅ Route suivant les rues tracée');
    }
    
    // Ajuster la vue pour voir tous les marqueurs
    if (bounds.length > 0) {
        console.log(`🎯 Ajustement de la vue sur ${bounds.length} points`);
        map.fitBounds(bounds, { padding: [50, 50] });
        console.log('✅ Trajet affiché avec succès !');
    } else {
        console.warn('⚠️ Aucun point à afficher sur la carte');
    }
}

// ============================================
// GÉOCODAGE D'ADRESSES avec Nominatim (OpenStreetMap)
// ============================================
async function geocoderAdresse(adresse, index, user) {
    try {
        // Utiliser l'API Nominatim (gratuite, OpenStreetMap)
        const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(adresse)}&limit=1`;
        
        const response = await fetch(url, {
            headers: {
                'User-Agent': 'Covoiturage-Optimisation-App/1.0'
            }
        });
        
        const data = await response.json();
        
        if (data && data.length > 0) {
            const lat = parseFloat(data[0].lat);
            const lng = parseFloat(data[0].lon);
            const position = [lat, lng];
            
            // Mettre à jour les coordonnées de l'utilisateur
            user.latitude = lat;
            user.longitude = lng;
            
            // Créer une icône numérotée
            const icon = L.divIcon({
                className: 'custom-marker',
                html: `<div style="
                    background-color: #2563eb;
                    color: white;
                    border-radius: 50%;
                    width: 32px;
                    height: 32px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-weight: bold;
                    font-size: 14px;
                    border: 2px solid white;
                    box-shadow: 0 2px 5px rgba(0,0,0,0.3);
                ">${index + 1}</div>`,
                iconSize: [32, 32],
                iconAnchor: [16, 16]
            });
            
            const marker = L.marker(position, { icon: icon }).addTo(map);
            
            const popupContent = `
                <div style="min-width: 200px;">
                    <h3 style="margin: 0 0 10px 0; color: #2563eb;">${user.nom}</h3>
                    <p style="margin: 5px 0;"><strong>📍 Départ:</strong><br>${user.adresseDepart}</p>
                    <p style="margin: 5px 0;"><strong>🎯 Arrivée:</strong><br>${user.adresseArrivee}</p>
                </div>
            `;
            marker.bindPopup(popupContent);
            
            markers.push(marker);
            
            console.log(`✅ Géocodé: ${adresse} → [${lat}, ${lng}]`);
        } else {
            console.warn(`⚠️ Adresse non trouvée: ${adresse}`);
        }
    } catch (error) {
        console.error('❌ Erreur géocodage pour:', adresse, error);
    }
}

// ============================================
// EFFACER LES MARQUEURS
// ============================================
function effacerMarqueurs() {
    markers.forEach(marker => {
        if (marker instanceof L.Routing.Control) {
            map.removeControl(marker);
        } else {
            map.removeLayer(marker);
        }
    });
    markers = [];
    
    if (routeLayer) {
        map.removeLayer(routeLayer);
        routeLayer = null;
    }
    
    if (routingControl) {
        map.removeControl(routingControl);
        routingControl = null;
    }
    
    console.log('🧹 Marqueurs et routes effacés');
}

// ============================================
// CALCUL DE DISTANCE HAVERSINE
// ============================================
function calculerDistance(lat1, lon1, lat2, lon2) {
    const R = 6371; // Rayon de la Terre en km
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = 
        Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
        Math.sin(dLon/2) * Math.sin(dLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    const distance = R * c;
    return distance;
}

// ============================================
// CENTRER SUR UNE ADRESSE
// ============================================
async function centrerSurAdresse(adresse) {
    try {
        const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(adresse)}&limit=1`;
        
        const response = await fetch(url, {
            headers: {
                'User-Agent': 'Covoiturage-Optimisation-App/1.0'
            }
        });
        
        const data = await response.json();
        
        if (data && data.length > 0) {
            const lat = parseFloat(data[0].lat);
            const lng = parseFloat(data[0].lon);
            map.setView([lat, lng], 14);
            console.log(`📍 Centré sur: ${adresse}`);
        } else {
            console.warn(`⚠️ Impossible de centrer sur: ${adresse}`);
        }
    } catch (error) {
        console.error('❌ Erreur centrage:', error);
    }
}

// ============================================
// UTILITAIRES - EXPORT DES FONCTIONS GLOBALES
// ============================================

// Rendre les fonctions disponibles globalement pour app.js
window.initMap = initMap;
window.afficherTrajetSurCarte = afficherTrajetSurCarte;
window.afficherMarqueurs = afficherMarqueurs;
window.effacerMarqueurs = effacerMarqueurs;
window.geocoderAdresse = geocoderAdresse;
window.calculerDistance = calculerDistance;
window.centrerSurAdresse = centrerSurAdresse;
window.afficherDeuxTrajets = afficherDeuxTrajets;

// ============================================
// COMPARAISON VISUELLE DES DEUX ALGORITHMES
// ============================================
function afficherDeuxTrajets(comparison) {
    console.log('🔥 Affichage comparaison des deux algorithmes');
    
    if (!map) {
        console.error('❌ Carte non initialisée');
        return;
    }
    
    effacerMarqueurs();
    
    const nn = comparison.nearestNeighborTrajet;
    const sa = comparison.simulatedAnnealingTrajet;
    
    if (!nn || !sa) {
        console.error('❌ Trajets manquants dans la comparaison');
        return;
    }
    
    // Afficher les deux trajets avec des couleurs différentes
    const bounds = [];
    
    // Trajet Nearest Neighbor (BLEU)
    if (nn.utilisateurs && nn.utilisateurs.length > 0) {
        const pointsNN = [];
        nn.utilisateurs.forEach((user, index) => {
            if (user.latitude && user.longitude) {
                const lat = parseFloat(user.latitude);
                const lng = parseFloat(user.longitude);
                const position = [lat, lng];
                
                // Marqueur bleu
                const icon = L.divIcon({
                    className: 'custom-marker',
                    html: `<div style="
                        background-color: #3b82f6;
                        color: white;
                        border-radius: 50%;
                        width: 28px;
                        height: 28px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-weight: bold;
                        font-size: 12px;
                        border: 2px solid white;
                        box-shadow: 0 2px 5px rgba(0,0,0,0.3);
                    ">${index + 1}</div>`,
                    iconSize: [28, 28],
                    iconAnchor: [14, 14]
                });
                
                const marker = L.marker(position, { icon: icon }).addTo(map);
                marker.bindPopup(`<b>⚡ NN ${index + 1}:</b> ${user.nom}`);
                markers.push(marker);
                pointsNN.push(position);
                bounds.push(position);
            }
        });
        
        // Route bleue pour Nearest Neighbor (suit les rues)
        if (pointsNN.length > 1) {
            const waypointsNN = pointsNN.map(point => L.latLng(point[0], point[1]));
            
            const routeNN = L.Routing.control({
                waypoints: waypointsNN,
                router: L.Routing.osrmv1({
                    serviceUrl: 'https://router.project-osrm.org/route/v1'
                }),
                lineOptions: {
                    styles: [{ color: '#3b82f6', opacity: 0.7, weight: 5, dashArray: '10, 10' }]
                },
                show: false,
                addWaypoints: false,
                routeWhileDragging: false,
                draggableWaypoints: false,
                fitSelectedRoutes: false,
                showAlternatives: false,
                createMarker: function() { return null; }
            }).addTo(map);
            
            markers.push(routeNN);
        }
    }
    
    // Trajet Simulated Annealing (VERT)
    if (sa.utilisateurs && sa.utilisateurs.length > 0) {
        const pointsSA = [];
        sa.utilisateurs.forEach((user, index) => {
            if (user.latitude && user.longitude) {
                const lat = parseFloat(user.latitude);
                const lng = parseFloat(user.longitude);
                const position = [lat, lng];
                
                // Marqueur vert
                const icon = L.divIcon({
                    className: 'custom-marker',
                    html: `<div style="
                        background-color: #10b981;
                        color: white;
                        border-radius: 50%;
                        width: 28px;
                        height: 28px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-weight: bold;
                        font-size: 12px;
                        border: 2px solid white;
                        box-shadow: 0 2px 5px rgba(0,0,0,0.3);
                    ">${index + 1}</div>`,
                    iconSize: [28, 28],
                    iconAnchor: [14, 14]
                });
                
                const marker = L.marker(position, { icon: icon }).addTo(map);
                marker.bindPopup(`<b>🎯 SA ${index + 1}:</b> ${user.nom}`);
                markers.push(marker);
                pointsSA.push(position);
                bounds.push(position);
            }
        });
        
        // Route verte pour Simulated Annealing (suit les rues)
        if (pointsSA.length > 1) {
            const waypointsSA = pointsSA.map(point => L.latLng(point[0], point[1]));
            
            const routeSA = L.Routing.control({
                waypoints: waypointsSA,
                router: L.Routing.osrmv1({
                    serviceUrl: 'https://router.project-osrm.org/route/v1'
                }),
                lineOptions: {
                    styles: [{ color: '#10b981', opacity: 0.9, weight: 5 }]
                },
                show: false,
                addWaypoints: false,
                routeWhileDragging: false,
                draggableWaypoints: false,
                fitSelectedRoutes: false,
                showAlternatives: false,
                createMarker: function() { return null; }
            }).addTo(map);
            
            markers.push(routeSA);
        }
    }
    
    // Ajuster la vue
    if (bounds.length > 0) {
        map.fitBounds(bounds, { padding: [50, 50] });
    }
    
    console.log('✅ Deux trajets affichés : Bleu (NN) vs Vert (SA)');
}
