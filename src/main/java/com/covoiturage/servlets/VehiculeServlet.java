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

import com.covoiturage.dao.VehiculeDAO;
import com.covoiturage.models.Vehicule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Servlet pour gérer les opérations CRUD sur les véhicules.
 */
@WebServlet("/api/vehicules")
public class VehiculeServlet extends HttpServlet {
    
    private VehiculeDAO vehiculeDAO;
    private Gson gson;


    
    @Override
    public void init() throws ServletException {
        vehiculeDAO = new VehiculeDAO();
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
        String disponibleParam = request.getParameter("disponible");
        
        try {
            if (idParam != null) {
                Long id = Long.parseLong(idParam);
                Vehicule vehicule = vehiculeDAO.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Véhicule introuvable"));
                
                response.getWriter().write(gson.toJson(vehicule));
                
            } else if ("true".equals(disponibleParam)) {
                List<Vehicule> vehicules = vehiculeDAO.findDisponibles();
                response.getWriter().write(gson.toJson(vehicules));
                
            } else {
                List<Vehicule> vehicules = vehiculeDAO.findAll();
                response.getWriter().write(gson.toJson(vehicules));
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
            Vehicule vehicule = gson.fromJson(request.getReader(), Vehicule.class);
            Vehicule created = vehiculeDAO.create(vehicule);
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(created));
            
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
            Vehicule vehicule = gson.fromJson(request.getReader(), Vehicule.class);
            
            if (vehicule.getId() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"ID requis\"}");
                return;
            }
            
            boolean updated = vehiculeDAO.update(vehicule);
            
            if (updated) {
                response.getWriter().write(gson.toJson(vehicule));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Véhicule introuvable\"}");
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
            boolean deleted = vehiculeDAO.delete(id);
            
            if (deleted) {
                response.getWriter().write("{\"success\": true, \"message\": \"Véhicule supprimé\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Véhicule introuvable\"}");
            }
            
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"ID invalide\"}");
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
