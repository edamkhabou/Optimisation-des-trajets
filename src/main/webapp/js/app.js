/**
 * Application JavaScript principale
 * Gère les interactions avec l'API et l'interface utilisateur
 */

const API_BASE_URL = '/covoiturage/api';

// ============================================
// INITIALISATION
// ============================================
document.addEventListener('DOMContentLoaded', function() {
    console.log('Application initialisée');
    
    // Charger les données initiales
    chargerUtilisateurs();
    chargerVehicules();
    
    // Initialiser les event listeners
    initEventListeners();
});

// ============================================
// EVENT LISTENERS
// ============================================
function initEventListeners() {
    // Form utilisateur
    document.getElementById('form-utilisateur').addEventListener('submit', ajouterUtilisateur);
    
    // Form véhicule
    document.getElementById('form-vehicule').addEventListener('submit', ajouterVehicule);
    
    // Form optimisation
    document.getElementById('form-optimisation').addEventListener('submit', optimiserTrajet);
}

// ============================================
// GESTION DES TABS
// ============================================
function showTab(tabName) {
    // Masquer tous les contenus
    const contents = document.querySelectorAll('.tab-content');
    contents.forEach(content => content.classList.remove('active'));
    
    // Désactiver tous les boutons
    const buttons = document.querySelectorAll('.tab-button');
    buttons.forEach(button => button.classList.remove('active'));
    
    // Activer le tab sélectionné
    document.getElementById(`tab-${tabName}`).classList.add('active');
    event.target.classList.add('active');
    
    // Charger les données spécifiques au tab
    if (tabName === 'statistiques') {
        chargerStatistiques();
    } else if (tabName === 'optimisation') {
        chargerPassagers();
    }
}

// ============================================
// UTILISATEURS
// ============================================
async function chargerUtilisateurs() {
    try {
        const response = await fetch(`${API_BASE_URL}/utilisateurs`);
        const utilisateurs = await response.json();
        
        afficherUtilisateurs(utilisateurs);
        remplirSelectConducteurs(utilisateurs);
        
    } catch (error) {
        console.error('Erreur lors du chargement des utilisateurs:', error);
        alert('Erreur lors du chargement des utilisateurs');
    }
}

function afficherUtilisateurs(utilisateurs) {
    const tbody = document.querySelector('#table-utilisateurs tbody');
    tbody.innerHTML = '';
    
    utilisateurs.forEach(user => {
        const tr = document.createElement('tr');
        
        const horaires = user.heureDepart && user.heureArrivee 
            ? `${user.heureDepart} - ${user.heureArrivee}`
            : 'Non défini';
        
        tr.innerHTML = `
            <td>${user.id}</td>
            <td><strong>${user.nom}</strong></td>
            <td>${user.adresseDepart}</td>
            <td>${user.adresseArrivee}</td>
            <td>${horaires}</td>
            <td>${user.groupe || '-'}</td>
            <td>
                <button class="btn btn-danger" onclick="supprimerUtilisateur(${user.id})">
                    🗑️ Supprimer
                </button>
            </td>
        `;
        
        tbody.appendChild(tr);
    });
}

async function ajouterUtilisateur(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const utilisateur = {
        nom: formData.get('nom'),
        adresseDepart: formData.get('adresseDepart'),
        adresseArrivee: formData.get('adresseArrivee'),
        heureDepart: formData.get('heureDepart') || null,
        heureArrivee: formData.get('heureArrivee') || null,
        groupe: formData.get('groupe') || null
    };
    
    try {
        const response = await fetch(`${API_BASE_URL}/utilisateurs`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(utilisateur)
        });
        
        if (response.ok) {
            alert('✅ Utilisateur ajouté avec succès!');
            event.target.reset();
            chargerUtilisateurs();
        } else {
            const error = await response.json();
            alert('❌ Erreur: ' + error.error);
        }
    } catch (error) {
        console.error('Erreur:', error);
        alert('❌ Erreur lors de l\'ajout de l\'utilisateur');
    }
}

async function supprimerUtilisateur(id) {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/utilisateurs?id=${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            alert('✅ Utilisateur supprimé');
            chargerUtilisateurs();
        } else {
            alert('❌ Erreur lors de la suppression');
        }
    } catch (error) {
        console.error('Erreur:', error);
        alert('❌ Erreur lors de la suppression');
    }
}

// ============================================
// VÉHICULES
// ============================================
async function chargerVehicules() {
    try {
        const response = await fetch(`${API_BASE_URL}/vehicules`);
        const vehicules = await response.json();
        
        afficherVehicules(vehicules);
        remplirSelectVehicules(vehicules);
        
    } catch (error) {
        console.error('Erreur lors du chargement des véhicules:', error);
    }
}

function afficherVehicules(vehicules) {
    const tbody = document.querySelector('#table-vehicules tbody');
    tbody.innerHTML = '';
    
    vehicules.forEach(vehicule => {
        const tr = document.createElement('tr');
        
        const disponibilite = vehicule.disponible 
            ? '<span style="color: green;">✓ Disponible</span>'
            : '<span style="color: red;">✗ Indisponible</span>';
        
        tr.innerHTML = `
            <td>${vehicule.id}</td>
            <td><strong>${vehicule.immatriculation}</strong></td>
            <td>ID ${vehicule.conducteurId}</td>
            <td>${vehicule.capacite} places</td>
            <td>${disponibilite}</td>
            <td>
                <button class="btn btn-danger" onclick="supprimerVehicule(${vehicule.id})">
                    🗑️ Supprimer
                </button>
            </td>
        `;
        
        tbody.appendChild(tr);
    });
}

async function ajouterVehicule(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const vehicule = {
        conducteurId: parseInt(formData.get('conducteurId')),
        immatriculation: formData.get('immatriculation'),
        capacite: parseInt(formData.get('capacite')),
        disponible: formData.get('disponible') === 'on'
    };
    
    try {
        const response = await fetch(`${API_BASE_URL}/vehicules`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(vehicule)
        });
        
        if (response.ok) {
            alert('✅ Véhicule ajouté avec succès!');
            event.target.reset();
            chargerVehicules();
        } else {
            const error = await response.json();
            alert('❌ Erreur: ' + error.error);
        }
    } catch (error) {
        console.error('Erreur:', error);
        alert('❌ Erreur lors de l\'ajout du véhicule');
    }
}

async function supprimerVehicule(id) {
    if (!confirm('Êtes-vous sûr de vouloir supprimer ce véhicule ?')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/vehicules?id=${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            alert('✅ Véhicule supprimé');
            chargerVehicules();
        }
    } catch (error) {
        console.error('Erreur:', error);
    }
}

// ============================================
// HELPERS - SELECTS
// ============================================
function remplirSelectConducteurs(utilisateurs) {
    const select = document.getElementById('conducteurId');
    select.innerHTML = '<option value="">Sélectionner un utilisateur</option>';
    
    utilisateurs.forEach(user => {
        const option = document.createElement('option');
        option.value = user.id;
        option.textContent = user.nom;
        select.appendChild(option);
    });
}

function remplirSelectVehicules(vehicules) {
    const select = document.getElementById('vehiculeId');
    select.innerHTML = '<option value="">Sélectionner un véhicule</option>';
    
    vehicules.filter(v => v.disponible).forEach(vehicule => {
        const option = document.createElement('option');
        option.value = vehicule.id;
        option.textContent = `${vehicule.immatriculation} (${vehicule.capacite} places)`;
        select.appendChild(option);
    });
}

async function chargerPassagers() {
    try {
        const response = await fetch(`${API_BASE_URL}/utilisateurs`);
        const utilisateurs = await response.json();
        
        const container = document.getElementById('passagers-list');
        container.innerHTML = '';
        
        utilisateurs.forEach(user => {
            const label = document.createElement('label');
            label.innerHTML = `
                <input type="checkbox" name="passagers" value="${user.id}">
                <strong>${user.nom}</strong> - ${user.adresseDepart}
            `;
            container.appendChild(label);
        });
        
    } catch (error) {
        console.error('Erreur:', error);
    }
}

// ============================================
// OPTIMISATION
// ============================================
async function optimiserTrajet(event) {
    event.preventDefault();
    
    const vehiculeId = parseInt(document.getElementById('vehiculeId').value);
    const algorithme = document.getElementById('algorithme').value;
    
    // Récupérer les passagers sélectionnés
    const checkboxes = document.querySelectorAll('input[name="passagers"]:checked');
    const utilisateurIds = Array.from(checkboxes).map(cb => parseInt(cb.value));
    
    if (utilisateurIds.length === 0) {
        alert('⚠️ Veuillez sélectionner au moins un passager');
        return;
    }
    
    // Si mode comparaison, appeler une fonction spéciale
    if (algorithme === 'compare') {
        comparerAlgorithmes(vehiculeId, utilisateurIds);
        return;
    }
    
    const data = {
        vehiculeId: vehiculeId,
        utilisateurIds: utilisateurIds,
        algorithme: algorithme
    };
    
    try {
        // Afficher un loader (sans effacer le contenu)
        const resultsDiv = document.getElementById('resultats-optimisation');
        resultsDiv.style.display = 'block';
        
        // Ajouter un indicateur de chargement temporaire
        const loadingMsg = document.createElement('div');
        loadingMsg.id = 'loading-msg';
        loadingMsg.innerHTML = '<p style="text-align: center; padding: 20px; font-size: 18px;">⏳ Optimisation en cours...</p>';
        resultsDiv.insertBefore(loadingMsg, resultsDiv.firstChild);
        
        const response = await fetch(`${API_BASE_URL}/optimiser`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        
        if (response.ok) {
            const trajet = await response.json();
            afficherResultatsOptimisation(trajet);
            afficherTrajetSurCarte(trajet);
        } else {
            const error = await response.json();
            alert('❌ Erreur: ' + error.error);
            resultsDiv.style.display = 'none';
        }
        
    } catch (error) {
        console.error('Erreur:', error);
        alert('❌ Erreur lors de l\'optimisation');
    }
}

function afficherResultatsOptimisation(trajet) {
    const resultsDiv = document.getElementById('resultats-optimisation');
    resultsDiv.style.display = 'block';
    
    // Supprimer le message de chargement s'il existe
    const loadingMsg = document.getElementById('loading-msg');
    if (loadingMsg) {
        loadingMsg.remove();
    }
    
    // Mettre à jour les métriques
    document.getElementById('result-distance').textContent = 
        trajet.distanceTotale.toFixed(2) + ' km';
    document.getElementById('result-temps').textContent = 
        trajet.tempsTotalMinutes.toFixed(0) + ' min';
    document.getElementById('result-passagers').textContent = 
        trajet.utilisateurs.length;
    document.getElementById('result-taux').textContent = 
        ((trajet.utilisateurs.length / trajet.vehicule.capacite) * 100).toFixed(0) + '%';
    
    // Afficher l'ordre de passage
    const ordreList = document.getElementById('ordre-utilisateurs');
    ordreList.innerHTML = '';
    
    trajet.utilisateurs.forEach((user, index) => {
        const li = document.createElement('li');
        li.innerHTML = `<strong>${user.nom}</strong> - ${user.adresseDepart}`;
        ordreList.appendChild(li);
    });
}

async function comparerAlgorithmes() {
    const vehiculeId = parseInt(document.getElementById('vehiculeId').value);
    const checkboxes = document.querySelectorAll('input[name="passagers"]:checked');
    const utilisateurIds = Array.from(checkboxes).map(cb => parseInt(cb.value));
    
    if (!vehiculeId || utilisateurIds.length === 0) {
        alert('⚠️ Veuillez sélectionner un véhicule et des passagers');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/optimiser?action=comparer`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                vehiculeId: vehiculeId,
                utilisateurIds: utilisateurIds
            })
        });
        
        if (response.ok) {
            const result = await response.json();
            afficherComparaison(result);
        }
    } catch (error) {
        console.error('Erreur:', error);
        alert('❌ Erreur lors de la comparaison');
    }
}

function afficherComparaison(result) {
    const message = `
        📊 COMPARAISON DES ALGORITHMES
        
        1️⃣ NEAREST NEIGHBOR (Plus proche voisin)
           Distance: ${result.nearestNeighbor.distance.toFixed(2)} km
           Temps: ${result.nearestNeighbor.temps.toFixed(0)} min
           Exécution: ${result.nearestNeighbor.executionMs} ms
        
        2️⃣ SIMULATED ANNEALING (Recuit simulé)
           Distance: ${result.simulatedAnnealing.distance.toFixed(2)} km
           Temps: ${result.simulatedAnnealing.temps.toFixed(0)} min
           Exécution: ${result.simulatedAnnealing.executionMs} ms
        
        🏆 MEILLEUR: ${result.meilleur}
        📈 Amélioration: ${result.amelioration.toFixed(2)}%
    `;
    
    alert(message);
}

// ============================================
// STATISTIQUES
// ============================================
async function chargerStatistiques() {
    try {
        const response = await fetch(`${API_BASE_URL}/stats`);
        const stats = await response.json();
        
        afficherStatistiques(stats);
        
    } catch (error) {
        console.error('Erreur:', error);
        alert('❌ Erreur lors du chargement des statistiques');
    }
}

function afficherStatistiques(stats) {
    const grid = document.getElementById('stats-grid');
    grid.innerHTML = '';
    
    // Créer les cartes de statistiques
    const statsToDisplay = [
        { label: 'Total trajets', value: stats.totalTrajets || 0, icon: '🚗' },
        { label: 'Trajets optimisés', value: stats.trajetsOptimises || 0, icon: '🎯' },
        { label: 'Total utilisateurs', value: stats.totalUtilisateurs || 0, icon: '👥' },
        { label: 'Distance totale', value: stats.distanceTotaleFormatted || '0 km', icon: '📏' },
        { label: 'Distance moyenne', value: stats.distanceMoyenneFormatted || '0 km', icon: '📊' },
        { label: 'Temps moyen', value: stats.tempsMoyenFormatted || '0 min', icon: '⏱️' },
        { label: 'Taux de remplissage', value: stats.tauxRemplissageMoyenFormatted || '0%', icon: '📈' },
        { label: 'Conflits détectés', value: stats.totalConflits || 0, icon: '⚠️' }
    ];
    
    statsToDisplay.forEach(stat => {
        const card = document.createElement('div');
        card.className = 'stat-card';
        card.innerHTML = `
            <div style="font-size: 2rem; margin-bottom: 10px;">${stat.icon}</div>
            <div class="stat-value">${stat.value}</div>
            <div class="stat-label">${stat.label}</div>
        `;
        grid.appendChild(card);
    });
    
    // Afficher impact environnemental
    document.getElementById('env-km').textContent = stats.kmEconomisesFormatted || '0 km';
    document.getElementById('env-co2').textContent = stats.co2EconomiseFormatted || '0 kg';
}

// ============================================
// COMPARAISON DES ALGORITHMES
// ============================================
async function comparerAlgorithmes(vehiculeId, utilisateurIds) {
    const data = {
        vehiculeId: vehiculeId,
        utilisateurIds: utilisateurIds
    };
    
    try {
        const resultsDiv = document.getElementById('resultats-optimisation');
        resultsDiv.style.display = 'block';
        resultsDiv.innerHTML = '<p style="text-align: center; padding: 20px; font-size: 18px;">⏳ Comparaison des algorithmes en cours...</p>';
        
        const response = await fetch(`${API_BASE_URL}/optimiser?action=comparer`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        
        if (response.ok) {
            const comparison = await response.json();
            afficherComparaison(comparison);
            afficherDeuxTrajets(comparison);
        } else {
            const error = await response.json();
            alert('❌ Erreur: ' + error.error);
            resultsDiv.style.display = 'none';
        }
        
    } catch (error) {
        console.error('Erreur:', error);
        alert('❌ Erreur lors de la comparaison');
    }
}

function afficherComparaison(comparison) {
    const resultsDiv = document.getElementById('resultats-optimisation');
    
    const nn = comparison.nearestNeighbor;
    const sa = comparison.simulatedAnnealing;
    
    const html = `
        <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 20px; border-radius: 10px; color: white; margin-bottom: 20px;">
            <h2 style="margin-top: 0; text-align: center;">🔥 Comparaison des Algorithmes 🔥</h2>
        </div>
        
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px;">
            <div style="background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); padding: 20px; border-radius: 10px; color: white;">
                <h3 style="margin-top: 0;">⚡ Plus Proche Voisin</h3>
                <div style="font-size: 14px; opacity: 0.9; margin-bottom: 15px;">Algorithme glouton (rapide)</div>
                <div style="background: rgba(255,255,255,0.2); padding: 15px; border-radius: 8px; margin-bottom: 10px;">
                    <div style="font-size: 12px; opacity: 0.8;">Distance</div>
                    <div style="font-size: 28px; font-weight: bold;">${nn.distanceTotale.toFixed(2)} km</div>
                </div>
                <div style="background: rgba(255,255,255,0.2); padding: 15px; border-radius: 8px; margin-bottom: 10px;">
                    <div style="font-size: 12px; opacity: 0.8;">Temps</div>
                    <div style="font-size: 28px; font-weight: bold;">${nn.tempsTotalMinutes.toFixed(0)} min</div>
                </div>
                <div style="background: rgba(255,255,255,0.2); padding: 15px; border-radius: 8px;">
                    <div style="font-size: 12px; opacity: 0.8;">Temps de calcul</div>
                    <div style="font-size: 20px; font-weight: bold;">${nn.tempsCalculMillis} ms</div>
                </div>
            </div>
            
            <div style="background: linear-gradient(135deg, #10b981 0%, #059669 100%); padding: 20px; border-radius: 10px; color: white;">
                <h3 style="margin-top: 0;">🎯 Recuit Simulé</h3>
                <div style="font-size: 14px; opacity: 0.9; margin-bottom: 15px;">Métaheuristique (optimal)</div>
                <div style="background: rgba(255,255,255,0.2); padding: 15px; border-radius: 8px; margin-bottom: 10px;">
                    <div style="font-size: 12px; opacity: 0.8;">Distance</div>
                    <div style="font-size: 28px; font-weight: bold;">${sa.distanceTotale.toFixed(2)} km</div>
                </div>
                <div style="background: rgba(255,255,255,0.2); padding: 15px; border-radius: 8px; margin-bottom: 10px;">
                    <div style="font-size: 12px; opacity: 0.8;">Temps</div>
                    <div style="font-size: 28px; font-weight: bold;">${sa.tempsTotalMinutes.toFixed(0)} min</div>
                </div>
                <div style="background: rgba(255,255,255,0.2); padding: 15px; border-radius: 8px;">
                    <div style="font-size: 12px; opacity: 0.8;">Temps de calcul</div>
                    <div style="font-size: 20px; font-weight: bold;">${sa.tempsCalculMillis} ms</div>
                </div>
            </div>
        </div>
        
        <div style="background: ${comparison.meilleur === 'Simulated Annealing' ? '#10b981' : '#3b82f6'}; padding: 20px; border-radius: 10px; color: white; text-align: center;">
            <h3 style="margin: 0 0 10px 0;">🏆 Meilleur algorithme: ${comparison.meilleur}</h3>
            ${comparison.amelioration > 0 ? `<div style="font-size: 18px;">Gain: ${comparison.amelioration.toFixed(1)}% plus court</div>` : ''}
        </div>
    `;
    
    resultsDiv.innerHTML = html;
}
