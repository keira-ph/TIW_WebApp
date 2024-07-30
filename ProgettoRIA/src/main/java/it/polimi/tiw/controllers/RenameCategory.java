package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.beans.MyDataStructure;
import it.polimi.tiw.dao.CategoryDAO;
import it.polimi.tiw.utils.ConnectionHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.google.gson.Gson;
/**
 * Servlet implementation class SaveCategory
 */
@WebServlet("/RenameCategory")
public class RenameCategory extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Connection connection = null;


    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }
    
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		 
		boolean badRequest = false;
		BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
	        StringBuilder requestBody = new StringBuilder();
	        String line;
	        while ((line = reader.readLine()) != null) {
	            requestBody.append(line);
	        }
	        reader.close();
	        
	        // Analisi della stringa JSON
	        Gson gson = new Gson();
	        MyDataStructure data = gson.fromJson(requestBody.toString(), MyDataStructure.class);
	        
	        
	        // Accesso ai parametri
	        String ID = data.getId();
	        String NAME = data.getNewname();
	        System.out.println(ID + NAME);
	        
	        
		CategoryDAO categoryDAO = new CategoryDAO(connection);
		int categoryID = 0;
		try {
			categoryID = Integer.parseInt(ID);
			
			try {
				ArrayList<Integer> allIds= categoryDAO.findAllIds();
				if(!allIds.contains(categoryID)) {
		            badRequest = true;

				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}catch(NumberFormatException e ) {
			badRequest=true;
		}
		
        if ( NAME == null || badRequest==true) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Impossible");
			return;
		}
        System.out.print("PRIMA DI creo category DAO");
        
        System.out.print("creo category DAO");
		try {
			System.out.print("sono nel try");
            categoryDAO.editName(categoryID,NAME);
			System.out.print("sono dopo il try");


        } catch(Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error in edit the category's name");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println("Operation completed successfully");
	}
	
	@Override
    public void destroy() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e){

            }
        }
    }

}
