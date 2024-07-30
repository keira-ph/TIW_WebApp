package it.polimi.tiw.controllers;

import it.polimi.tiw.dao.CategoryDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import org.apache.commons.lang.StringEscapeUtils;


import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;


@WebServlet("/CreateCategory")
@MultipartConfig
public class CreateCategory extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;


    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        boolean badRequest = false;
        
        String name = null;
        String fidParam = null;
        name = StringEscapeUtils.escapeJava(request.getParameter("name"));
        fidParam = StringEscapeUtils.escapeJava(request.getParameter("fcatId"));

        int fid = -1;

        if(name == null || fidParam == null) {
            badRequest = true;
        }

        try {
        	fid = Integer.parseInt(fidParam);

            if(fid < 0) {
                badRequest = true;
            }
        } catch (NumberFormatException e) {
            badRequest = true;
        }
         CategoryDAO categoryDAO = new CategoryDAO(connection);
         try {
			ArrayList<Integer> allIds= categoryDAO.findAllIds();
			if(!allIds.contains(fid)) {
	            badRequest = true;

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
        if(badRequest) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Wrong parameter values");
            return;
        }

        
        try {
            categoryDAO.addCategory(name,fid);

        } catch(Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error in creating the category");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println("Creation completed");

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


