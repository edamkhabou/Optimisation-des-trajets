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

import com.covoiturage.dao.TrajetDAO;
import com.covoiturage.models.Trajet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Servlet pour gérer les trajets.
 */
@WebServlet("/api/trajets")
public class TrajetServlet extends HttpServlet {
    
    private TrajetDAO trajetDAO;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        trajetDAO = new TrajetDAO();
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
        String vehiculeParam = request.getParameter("vehiculeId");
        
        try {
            if (idParam != null) {
                Long id = Long.parseLong(idParam);
                Trajet trajet = trajetDAO.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Trajet introuvable"));
                
                response.getWriter().write(gson.toJson(trajet));
                
            } else if (vehiculeParam != null) {
                Long vehiculeId = Long.parseLong(vehiculeParam);
                List<Trajet> trajets = trajetDAO.findByVehicule(vehiculeId);
                response.getWriter().write(gson.toJson(trajets));
                
            } else {
                List<Trajet> trajets = trajetDAO.findAll();
                response.getWriter().write(gson.toJson(trajets));
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
            boolean deleted = trajetDAO.delete(id);
            
            if (deleted) {
                response.getWriter().write("{\"success\": true, \"message\": \"Trajet supprimé\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Trajet introuvable\"}");
            }
            
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
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
