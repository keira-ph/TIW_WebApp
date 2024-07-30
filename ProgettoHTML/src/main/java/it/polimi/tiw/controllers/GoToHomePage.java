package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.dao.*;


import it.polimi.tiw.beans.*;



@WebServlet("/GoToHomePage")
public class GoToHomePage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;


	public GoToHomePage() {
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
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get db connection");
		}
		
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		HttpSession s = request.getSession(false);
		
		int idimage,rootid;
		boolean badRequestCopy, badRequestQui, badRequestadd ;
		boolean badRequestHome = false;
		
		if (s == null || s.getAttribute("user")==null ) {
			
			String path = getServletContext().getContextPath() + "/index.html";
			response.sendRedirect(path);
		
		
		
		}else {
			String path = "/WEB-INF/Home.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			
			if(request.getParameter("idimage")==null) {
				idimage=0;
			}else {
				try {
					idimage=Integer.parseInt(request.getParameter("idimage"));
				}catch (Exception e){
					idimage=0;
					badRequestHome=true;
				}
				
			}
			
			if(request.getParameter("badRequestCopy")==null) {
				badRequestCopy=false;
			}else {
				try {
					badRequestCopy =Boolean.parseBoolean(request.getParameter("badRequestCopy"));
				}catch (Exception e){
					badRequestCopy=false;
					badRequestHome=true;
				}

			}
			if(request.getParameter("rootid")==null || badRequestCopy==true) {
				rootid=0;
			}else {
				try {
					rootid=Integer.parseInt(request.getParameter("rootid"));
				}catch (Exception e){
					rootid=0;
					badRequestHome=true;
				}
			}
			if(request.getParameter("badRequestQui")==null) {
				badRequestQui=false;
			}else {
				try {
					badRequestQui =Boolean.parseBoolean(request.getParameter("badRequestQui"));
				}catch (Exception e){
					badRequestQui=false;
					badRequestHome=true;
				}

			}
			if(request.getParameter("badRequestadd")==null) {
				badRequestadd=false;
			}else {
				try {
					badRequestadd =Boolean.parseBoolean(request.getParameter("badRequestadd"));
				}catch (Exception e){
					badRequestadd=false;
					badRequestHome=true;
				}

			}
			
			
				
				
				
			
				
			List<Category> allcats = null;
			List<Category> topcats = null;
			
			List<Image> images = null;
			ImageDAO collector = new ImageDAO(connection);
			try {
				images = collector.findImagesById(idimage);
				
			} catch (Exception e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Error in retrieving products from the database");
				return;
			}
			
			
			CategoryDAO bService = new CategoryDAO(connection);
			
			try {
				allcats = bService.findAllCategories();
				topcats = bService.findTopCategoriesAndSubtrees();
			} catch (Exception e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Error in retrieving products from the database");
				return;
			}
			List<Category> almostallcats = new ArrayList<Category>();
			for(Category c: allcats) {
				if(c.getChildren() <9) {
					almostallcats.add(c);
				}
			}
			
			
			
			
				
			ctx.setVariable("allcats", allcats);
			ctx.setVariable("topcats", topcats);
			ctx.setVariable("almostallcats", almostallcats);
			ctx.setVariable("images", images);
			ctx.setVariable("rootid", rootid);

			ctx.setVariable("idimage", idimage);
			ctx.setVariable("badRequestHome",badRequestHome);
			ctx.setVariable("badRequestCopy", badRequestCopy);
			ctx.setVariable("badRequestadd", badRequestadd);
			ctx.setVariable("badRequestQui", badRequestQui);
			
			
			
			
			templateEngine.process(path, ctx, response.getWriter());
		}
		
		
		
		
		
		
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
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
