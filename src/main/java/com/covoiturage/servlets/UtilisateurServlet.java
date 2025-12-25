package com.covoiturage.servlets;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.covoiturage.dao.UtilisateurDAO;
import com.covoiturage.models.Utilisateur;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Servlet pour gérer les opérations CRUD sur les utilisateurs.
 * 
 * Endpoints:
 * - GET /api/utilisateurs : Liste tous les utilisateurs
 * - GET /api/utilisateurs?id=X : Récupère un utilisateur par ID
 * - POST /api/utilisateurs : Crée un utilisateur
 * - PUT /api/utilisateurs : Met à jour un utilisateur
 * - DELETE /api/utilisateurs?id=X : Supprime un utilisateur
 */
@WebServlet("/api/utilisateurs")
public class UtilisateurServlet extends HttpServlet {
    
    private UtilisateurDAO utilisateurDAO;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        utilisateurDAO = new UtilisateurDAO();
        gson = new GsonBuilder()
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
            .create();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String idParam = request.getParameter("id");
        String groupeParam = request.getParameter("groupe");
        
        try {
            if (idParam != null) {
                // Récupérer un utilisateur par ID
                Long id = Long.parseLong(idParam);
                Utilisateur utilisateur = utilisateurDAO.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
                
                String json = gson.toJson(utilisateur);
                response.getWriter().write(json);
                
            } else if (groupeParam != null) {
                // Récupérer les utilisateurs d'un groupe
                List<Utilisateur> utilisateurs = utilisateurDAO.findByGroupe(groupeParam);
                String json = gson.toJson(utilisateurs);
                response.getWriter().write(json);
                
            } else {
                // Récupérer tous les utilisateurs
                List<Utilisateur> utilisateurs = utilisateurDAO.findAll();
                String json = gson.toJson(utilisateurs);
                response.getWriter().write(json);
            }
            
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"ID invalide\"}");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // Lire le JSON du corps de la requête
            Utilisateur utilisateur = gson.fromJson(request.getReader(), Utilisateur.class);
            
            // Créer l'utilisateur
            Utilisateur created = utilisateurDAO.create(utilisateur);
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            String json = gson.toJson(created);
            response.getWriter().write(json);
            
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            Utilisateur utilisateur = gson.fromJson(request.getReader(), Utilisateur.class);
            
            if (utilisateur.getId() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"ID requis pour la mise à jour\"}");
                return;
            }
            
            boolean updated = utilisateurDAO.update(utilisateur);
            
            if (updated) {
                String json = gson.toJson(utilisateur);
                response.getWriter().write(json);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Utilisateur introuvable\"}");
            }
            
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String idParam = request.getParameter("id");
        
        if (idParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"ID requis\"}");
            return;
        }
        
        try {
            Long id = Long.parseLong(idParam);
            boolean deleted = utilisateurDAO.delete(id);
            
            if (deleted) {
                response.getWriter().write("{\"success\": true, \"message\": \"Utilisateur supprimé\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Utilisateur introuvable\"}");
            }
            
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"ID invalide\"}");
        }
    }
    
    /**
     * Adaptateur Gson pour LocalTime.
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
