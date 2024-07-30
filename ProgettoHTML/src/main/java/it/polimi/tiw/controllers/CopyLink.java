package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import it.polimi.tiw.dao.CategoryDAO;



@WebServlet("/CopyLink")
public class CopyLink extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public CopyLink() {
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession s = request.getSession(false);
		if (s == null) {
			String path = getServletContext().getContextPath() + "/index.html";
			response.sendRedirect(path);
		}else {
			String idParam = request.getParameter("id");
		
		
		
		Integer id = -1;
        
		
		boolean badRequestCopy = false;
		
		
		
		
		if (idParam == null || idParam.isEmpty() ) {
			id=0;
				 badRequestCopy = true;		
		}
		try {
			
			id = Integer.parseInt(idParam);
		} catch (NumberFormatException e) {
			id=0;
			badRequestCopy = true;
		}
		
		
		CategoryDAO control = new CategoryDAO(connection);
		ArrayList<Integer> ids;
		try {
			ids = control.findAllIds();
			if (!ids.contains(id) ) {
				id=0;
			badRequestCopy = true;
			}
		
		} catch (SQLException e) {
			id=0;
			badRequestCopy = true;
		}
		
		
		
			
		
		
		String path = getServletContext().getContextPath() + "/GoToHomePage?rootid=" + id+ "&idimage=" + "0" + 
				"&badRequestCopy=" + badRequestCopy + "&badRequestQui=" +  "false" + "&badRequestadd=" + "false";
			response.sendRedirect(path);
		}
		
		
		
		
		
		
		
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
