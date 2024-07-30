package it.polimi.tiw.controllers;

import com.google.gson.Gson;

import it.polimi.tiw.beans.Category;
import it.polimi.tiw.dao.CategoryDAO;
import it.polimi.tiw.utils.ConnectionHandler;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
//import java.util.HashMap;
//import java.util.Map;
import java.sql.SQLException;
import java.util.ArrayList;


@WebServlet("/UpdateCategory")
public class UpdateCategory extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    //private Map<Integer, Integer> modifiedData = new HashMap<>();
    //private Boolean done = false;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());

    }


    /*
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

     */



    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    	HttpSession s = request.getSession(false);
		
		if (s == null) {
			String path = getServletContext().getContextPath() + "/index.html";
			response.sendRedirect(path);
		}
    	
        StringBuffer buffer = new StringBuffer();
        String line = null;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                buffer.append(line);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Error in parsing input");
            return;
        }

        String result = buffer.toString();

        

        Gson gson = new Gson();
        int[][] arr;

        try {
            arr = gson.fromJson(result,int[][].class);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Wrong format");
            return;
        }
        int rootid;
        int fatherid;
        try {
        rootid = Integer.parseInt((Integer.toString(arr[0][1]))) ;
        fatherid=Integer.parseInt((Integer.toString(arr[0][0]))) ;
        }catch(NullPointerException e ){
        	System.out.println("3");
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        response.getWriter().println("Error in copy the category");
	            return; 
        }
        

        CategoryDAO categoryDAO = new CategoryDAO(connection);
        
        try {
        	System.out.println("qui");
			ArrayList<Integer> allIds= categoryDAO.findAllIds();
			if(!allIds.contains(rootid)||!allIds.contains(fatherid) ||!categoryDAO.isLegit(fatherid) || categoryDAO.cyclicLinkExists(fatherid,rootid)) {
				response.setStatus(HttpServletResponse.SC_OK);
				
		        response.getWriter().println("Error in copy the category");
		        return;
		           
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        
        
        
		Category pCAT, vpCAT;
		try {
			
			
			pCAT = categoryDAO.getCategory(fatherid);
			vpCAT = categoryDAO.getCategory(rootid);
			
				try {
					connection.setAutoCommit(false);
					System.out.println(connection.getAutoCommit());
					categoryDAO.copyHereTheTree(fatherid, pCAT.getChildren(), rootid, vpCAT.getName(),vpCAT.getChildren() );
					System.out.println(connection.getAutoCommit());
					connection.commit();
				} catch (SQLException e) {
					 e.printStackTrace();
					  System.out.println("1");
			            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			            
			            return;
				}
				System.out.println(connection.getAutoCommit());			
				connection.setAutoCommit(true);
		
		} catch (SQLException e) {
			 e.printStackTrace();
			 System.out.println("2");
	            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	            
	            return;
		}
		
        
        
        
        
        
      
		
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println("Operation completed successfully");

    }

}
