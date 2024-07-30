package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.beans.Category;
import it.polimi.tiw.dao.*;

@WebServlet("/AddCategory")
public class AddCategory extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public AddCategory() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init() throws ServletException {
		try {
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new UnavailableException("Couldn't get db connection");
		}

	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String name = null;
		String description = null;
		String fidCat=null;
		int fid = -1;
		boolean badRequestadd = false;
		
		HttpSession s = request.getSession(false);
		if (s == null) {
			String path = getServletContext().getContextPath() + "/index.html";
			response.sendRedirect(path);
		}
		
		
		try {
			name = request.getParameter("name");
			description = request.getParameter("description");
			fidCat = request.getParameter("fcatId");
			
			if (fidCat == null ) {badRequestadd=true;
			}else {
				try {
					fid = Integer.parseInt(fidCat);
				} catch (NumberFormatException e) {
				badRequestadd=true;}
				CategoryDAO control = new CategoryDAO(connection);
				ArrayList<Integer> ids = control.findAllIds();
				List<Category> topcats = control.findTopCategoriesAndSubtrees();
				
				if((fid!=0 && !ids.contains(fid)) || (fid==0 && !topcats.isEmpty() && topcats.size()==9 ) ) {
					
					badRequestadd=true;
				}
				
				
				if (name.isEmpty() || description.isEmpty() ) {
					
					badRequestadd=true;
				}
			}

			
			
			
			
			
		} catch (NullPointerException e) {
			badRequestadd=true;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		
			try {
				CategoryDAO bService = new CategoryDAO(connection);
				
					if(!bService.isLegit(fid )) { badRequestadd=true;}
				if(!badRequestadd){bService.addCategory(name, description,fid);
				}
			} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	
			

			String path = getServletContext().getContextPath() + "/GoToHomePage?rootid=" + "0"+ "&idimage=" + "0" + 
			"&badRequestCopy=" + "false" + "&badRequestQui=" +  "false" + "&badRequestadd=" + badRequestadd;
			response.sendRedirect(path);
		
		
		
		

		
		
		
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
