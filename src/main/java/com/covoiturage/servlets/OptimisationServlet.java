package com.covoiturage.servlets;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.covoiturage.models.Trajet;
import com.covoiturage.services.OptimisationService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Servlet pour l'optimisation des trajets.
 * 
 * Endpoints:
 * - POST /api/optimiser : Optimise un nouveau trajet
 * - POST /api/optimiser/comparer : Compare les algorithmes
 */
@WebServlet("/api/optimiser")
public class OptimisationServlet extends HttpServlet {
    
    private OptimisationService optimisationService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        optimisationService = new OptimisationService();
        gson = new GsonBuilder()
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
            .create();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        
        try {
            if ("comparer".equals(action)) {
                comparerAlgorithmes(request, response);
            } else {
                optimiserTrajet(request, response);
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Optimise un trajet.
     */
    private void optimiserTrajet(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, SQLException {
        
        // Lire le JSON de la requête
        Map<String, Object> params = gson.fromJson(
            request.getReader(), 
            new TypeToken<Map<String, Object>>(){}.getType()
        );
        
        Long vehiculeId = ((Double) params.get("vehiculeId")).longValue();
        List<Double> utilisateurIdsDouble = (List<Double>) params.get("utilisateurIds");
        String algorithme = (String) params.getOrDefault("algorithme", "nearest_neighbor");
        
        // Convertir les IDs
        List<Long> utilisateurIds = new java.util.ArrayList<>();
        for (Double id : utilisateurIdsDouble) {
            utilisateurIds.add(id.longValue());
        }
        
        // Optimiser
        Trajet trajet = optimisationService.optimiserTrajet(
            vehiculeId, 
            utilisateurIds, 
            algorithme
        );
        
        response.setStatus(HttpServletResponse.SC_OK);
        String json = gson.toJson(trajet);
        response.getWriter().write(json);
    }
    
    /**
     * Compare les algorithmes d'optimisation.
     */
    private void comparerAlgorithmes(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, SQLException {
        
        Map<String, Object> params = gson.fromJson(
            request.getReader(), 
            new TypeToken<Map<String, Object>>(){}.getType()
        );
        
        Long vehiculeId = ((Double) params.get("vehiculeId")).longValue();
        List<Double> utilisateurIdsDouble = (List<Double>) params.get("utilisateurIds");
        
        List<Long> utilisateurIds = new java.util.ArrayList<>();
        for (Double id : utilisateurIdsDouble) {
            utilisateurIds.add(id.longValue());
        }
        
        OptimisationService.ComparisonResult result = 
            optimisationService.comparerAlgorithmes(vehiculeId, utilisateurIds);
        
        String json = gson.toJson(result);
        response.getWriter().write(json);
    }
    
    /**
     * Adaptateur pour LocalTime.
     */
    private static class LocalTimeAdapter extends com.google.gson.TypeAdapter<LocalTime> {
        @Override
        public void write(com.google.gson.stream.JsonWriter out, LocalTime value) throws IOException {
            out.value(value != null ? value.toString() : null);
        }
        
        @Override
        public LocalTime read(com.google.gson.stream.JsonReader in) throws IOException {
            String timeStr = in.nextString();
            return timeStr != null && !timeStr.isEmpty() ? LocalTime.parse(timeStr) : null;
        }
    }
}
