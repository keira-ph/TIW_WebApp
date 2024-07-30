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

import it.polimi.tiw.beans.Category;
import it.polimi.tiw.dao.*;

@WebServlet("/CopyHere")
public class CopyHere extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public CopyHere() {
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
		}
		boolean badRequestQui = false;
		
		
		
		
		String id_param = request.getParameter("id");
		
		if(request.getParameter("rootid")!=null) {
			String  vecchioid_param = request.getParameter("rootid").toString();
			 
			Integer  pid = -1;
			Integer  vpid = -1;
			
			
			
			
			
			
			
		
				if (id_param == null || vecchioid_param== null) {badRequestQui = true;}
			
				try {
					pid = Integer.parseInt(id_param);
					vpid = Integer.parseInt(vecchioid_param);
				} catch (NumberFormatException e) {badRequestQui = true;}

				if(!badRequestQui) {
					CategoryDAO control = new CategoryDAO(connection);
					ArrayList<Integer> ids;
					try {
						ids = control.findAllIds();
						List<Category> topcats = control.findTopCategoriesAndSubtrees();
						if (!(pid==0 || ids.contains(pid))  || (pid==0 && !topcats.isEmpty() && topcats.size()==9 )|| !ids.contains(vpid) || pid.toString().startsWith(vpid.toString())|| !control.isLegit(pid ) ) {
							badRequestQui = true;
							}
					} catch (SQLException e) {
						System.out.println("qui1");
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				if(!badRequestQui) {
					CategoryDAO bService = new CategoryDAO(connection);
					Category pCAT, vpCAT;
					try {
						
						
						pCAT = bService.getCategory((int)pid);
						vpCAT = bService.getCategory((int)vpid);
						if( !badRequestQui && pCAT!=null ) {
							try {
								connection.setAutoCommit(false);
								bService.copyHereTheTree(pid, pCAT.getChildren(), vpid, vpCAT.getName(),vpCAT.getChildren(), vpCAT.getDescription() );
								connection.commit();
							} catch (SQLException e) {
								System.out.println("qui5");
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
											
						}else if(!badRequestQui && pCAT==null ){
							try {
								connection.setAutoCommit(false);
								bService.copyHereTheTree(0, 0, vpid, vpCAT.getName(),vpCAT.getChildren(), vpCAT.getDescription() );
								connection.commit();
							} catch (SQLException e) {
								System.out.println("qui2");
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							
						}else {
							badRequestQui=true;
						}
					
					} catch (SQLException e) {
						System.out.println("qui3");
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				try {
					connection.setAutoCommit(true);
				} catch (SQLException e) {
					System.out.println("qui4");
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			
				}	
		}
		
				
				
			
			
			
		
		
			
			
				
		        
				
				
				
			
		
		
		
		
			String path = getServletContext().getContextPath() + "/GoToHomePage?rootid=" + 0+ "&idimage=" + "0" + 
					"&badRequestCopy=" + "false" + "&badRequestQui=" + badRequestQui  + "&badRequestadd=" + "false";
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
