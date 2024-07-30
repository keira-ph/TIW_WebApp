package it.polimi.tiw.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import it.polimi.tiw.dao.ImageDAO;

@WebServlet("/CreateImage")
@MultipartConfig
public class CreateImage extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection;

	
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
		HttpSession session = request.getSession(false);
		if (session == null) {
			String path = getServletContext().getContextPath();
			response.sendRedirect(path);
		}
		
		String id = request.getParameter("id");
		Part imagePart = request.getPart("image");
		
		InputStream imageStream = null;
		String mimeType = null;
		if (imagePart != null) {
			imageStream = imagePart.getInputStream();
			String filename = imagePart.getSubmittedFileName();
			mimeType = getServletContext().getMimeType(filename);			
		}
		if (imageStream == null || (imageStream.available()==0) || !mimeType.startsWith("image/")) {
			
			return;
		}
		if (id == null ) {
			}
		int idimage = 0;
		try {
			idimage = Integer.parseInt(id);
		} catch (NumberFormatException e) {
			idimage = 0;
		}
		
		ImageDAO mDAO = new ImageDAO(connection);
		

		String error = null;
		try {
			mDAO.createImage( imageStream, idimage);
		} catch (SQLException e3) {
			error = "Bad database insertion input";
		}
		if (error != null) {
			return;
		}
			
			String path = getServletContext().getContextPath() + "/GoToHomePage?rootid=" + "0" + "&idimage=" + "0" + 
					"&badRequestCopy=" + "false" + "&badRequestQui=" + "false"  + "&badRequestadd=" + "false";
				response.sendRedirect(path);
		
	}
	
	public void destroy() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e){
				System.out.println("qui");
			}
		}
	}
}