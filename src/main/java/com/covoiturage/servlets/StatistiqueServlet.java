package com.covoiturage.servlets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.covoiturage.services.StatistiqueService;
import com.google.gson.Gson;

/**
 * Servlet pour les statistiques.
 * 
 * Endpoints:
 * - GET /api/stats : Récupère toutes les statistiques
 */
@WebServlet("/api/stats")
public class StatistiqueServlet extends HttpServlet {
    
    private StatistiqueService statistiqueService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        statistiqueService = new StatistiqueService();
        gson = new Gson();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            Map<String, Object> stats = statistiqueService.calculerStatistiquesGlobales();
            String json = gson.toJson(stats);
            response.getWriter().write(json);
            
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
