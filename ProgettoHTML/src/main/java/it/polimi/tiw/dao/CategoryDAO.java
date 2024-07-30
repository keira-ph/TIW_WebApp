package it.polimi.tiw.dao;

import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.beans.Category;

public class CategoryDAO {
	private Connection connection;
	
	
	
	

	public CategoryDAO() {
		// TODO Auto-generated constructor stub
	}

	public CategoryDAO(Connection con) {
		this.connection = con;
	}

	//trova tutte le categorie presenti nel db
	public List<Category> findAllCategories() throws SQLException {
		List<Category> Categorys = new ArrayList<Category>();
		try (PreparedStatement pstatement = connection.prepareStatement("SELECT * FROM category");) {
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Category BP = new Category();
					BP.setId(result.getInt("id"));
					BP.setName(result.getString("name"));
					BP.setDescription(result.getString("description"));
					BP.setChildren(result.getInt("children"));
					
					addCategory(Categorys, BP);
				}
			
		}
	  }
		
		return Categorys;
	}
	//trova tutti gli id presenti nel db
	public ArrayList<Integer> findAllIds() throws SQLException {
		ArrayList<Integer> Ids = new ArrayList<Integer>();
		try (PreparedStatement pstatement = connection.prepareStatement("SELECT id FROM category");) {
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					
					Ids.add(result.getInt("id"));
				}
			
		}
	  }
		
		return Ids;
	}
// aggiunge in ordine crescente di id una categoria c ad una lista l
	public List<Category>  addCategory( List<Category> l, Category c) {
		for(Category cat : l) {
			if(cat.getId() > c.getId()) {
				l.add(l.indexOf(cat) , c);
				return l;
			}
		}
		l.add(c);
		return l;
	}
	//trova le caegorie radice e le loro sottocategorie
	public List<Category> findTopCategoriesAndSubtrees() throws SQLException {
		List<Category> Categorys = new ArrayList<Category>();
		try (PreparedStatement pstatement = connection.prepareStatement("SELECT * FROM category WHERE id NOT IN (SELECT child FROM dbhtmlh.subcats)");) {
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Category BP = new Category();
					BP.setId(result.getInt("id"));
					BP.setName(result.getString("name"));
					BP.setDescription(result.getString("description"));
					BP.setChildren(result.getInt("children"));

					BP.setIsTop(true);
					addCategory(Categorys,BP);
				}
				for (Category p : Categorys) {
					findSubcats(p);
				}
			}
		}
		return Categorys;
	}
	//ritorna true se la categoria passata per id non ha ancora 9 figli
	public boolean isLegit(int id) throws SQLException {
		int children =0;
		try (PreparedStatement pstatement = connection.prepareStatement(
				"SELECT P.children FROM  dbhtmlh.category P WHERE P.id = ?");) {
			pstatement.setInt(1, id);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next( )) {
					children=result.getInt("children");
				}
			}
			
			
		}
		return children<9;
	}
//data una categoria padre p trova le sue sottocategorie
	// la funzione è ricorsiva
	public void findSubcats(Category p) throws SQLException {
		Category BP = null;
		try (PreparedStatement pstatement = connection.prepareStatement(
				"SELECT P.id, P.name, P.description, P.children FROM subcats S JOIN category P on P.id = S.child WHERE S.father = ?");) {
			pstatement.setInt(1, p.getId());
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					BP = new Category();
					BP.setId(result.getInt("id"));
					BP.setName(result.getString("name"));
					BP.setDescription(result.getString("description"));
					BP.setChildren(result.getInt("children"));
					findSubcats(BP);
					p.addSubcat(BP);
				}
			}
		}

	} 
	// dati i dati di una categoria, restituisce la categoria con il sottoalbero che da lei deriva
	// NB assicurarsi i dati passati in input siano validi
	public Category findSubtreeOf(int id, String name,String description,int children) throws SQLException {
		
		 
		Category newp = new Category();
		newp.setId(id);
		newp.setName(name);
		newp.setDescription(description);
		newp.setChildren(children);
		newp.setIsTop(false);
		
		findSubcats(newp);
			
		return newp;
		
	}
	// dati i dati di due categorie, copia il sottoalbero di ROOT come nuovo figlio di NUOVOPADRE
		// NB assicurarsi i dati passati in input siano validi
	public void copyHereTheTree(int  idNuovoPadre, int childrenNuovoPadre, int rootid, String rootname, int rootchildren, String rootdescription) throws SQLException  {
		
		if(idNuovoPadre != 0) {
			
			Category radiceDelSottoalbero = findSubtreeOf( rootid,  rootname, rootdescription, rootchildren);
		
	        int nuovoId = idNuovoPadre*10+childrenNuovoPadre+1;
	        
	        createCategory(radiceDelSottoalbero.getName(), radiceDelSottoalbero.getDescription(), nuovoId, 0);
			
			updateChildrenCategory(idNuovoPadre);
			createLink( idNuovoPadre,  nuovoId);
			int numCifre = String.valueOf(radiceDelSottoalbero.getId()).length();
			
			IdChange(nuovoId, radiceDelSottoalbero, numCifre );
			treeMaking(radiceDelSottoalbero, idNuovoPadre);
		}else{
			try (PreparedStatement pstatement = connection.prepareStatement("SELECT id FROM category WHERE id NOT IN (SELECT child FROM dbhtmlh.subcats)");) {
				try (ResultSet result = pstatement.executeQuery();) {
					int count=0;
					while (result.next()) {count ++;}
					
					Category radiceDelSottoalbero = findSubtreeOf( rootid,  rootname, rootdescription, rootchildren);
					createCategory(radiceDelSottoalbero.getName(), radiceDelSottoalbero.getDescription(), count+1, 0);
					int numCifre = String.valueOf(radiceDelSottoalbero.getId()).length();
					IdChange(count+1, radiceDelSottoalbero, numCifre );
					treeMaking(radiceDelSottoalbero, idNuovoPadre);
				}
			}
			
			
		}
		
			
						
		
        
	}
	//costruzione nuovo albero
	public void  treeMaking (Category cat, int idNuovoPadre) throws SQLException {
		if (!(cat.getSubcats().isEmpty())) {
			for (Category c: cat.getSubcats()) {
		    	createCategory(c.getName(), c.getDescription(), c.getId(),0);
		    	updateChildrenCategory(c.getId()/10);
				createLink( c.getId()/10,  c.getId());
				treeMaking(c,idNuovoPadre);
		    }
		}
		
	}
	//aggiornamento id
	public void IdChange (int  nuovoId, Category cat, int numCifre) throws SQLException {
		
		
		
		for (Category c: cat.getSubcats()) {
			String str;
			str = String.valueOf(c.getId());
			str = str.substring(numCifre);
			str= String.valueOf(nuovoId) + str;
			c.setId(Integer.parseInt(str));
			IdChange(nuovoId, c, numCifre);
			
			
		}
	}
	
	
	
			
		

	
	
	
	
	
//crea nuova categoria
	public void createCategory(String name, String descr, int id, int children) throws SQLException {
		String query = "INSERT into dbhtmlh.category (name, id, description, children) VALUES(?, ?, ?,?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, name);
			pstatement.setInt(2, id);
			pstatement.setString(3, descr);
			pstatement.setInt(4, children);
			pstatement.executeUpdate();
		}
	}
	//aggiunge nuova categoria
	public void addCategory(String name, String descr, int fid) throws SQLException {
		
		if(fid != 0) {
			try (PreparedStatement pstatement = connection.prepareStatement(
				"SELECT P.children FROM  dbhtmlh.category P WHERE P.id = ?");) {
				pstatement.setInt(1, fid);
				try (ResultSet result = pstatement.executeQuery();) {
					while (result.next()) {
						int numC=result.getInt("children");
						
						int id=fid*10+numC+1;
						createCategory(name,  descr, id, 0);
						updateChildrenCategory(fid);
						createLink(fid , id);
						
					}
				}
					
			}
			
		
		}else{
			try (PreparedStatement pstatement = connection.prepareStatement("SELECT id FROM category WHERE id NOT IN (SELECT child FROM dbhtmlh.subcats)");) {
				try (ResultSet result = pstatement.executeQuery();) {
					int count=0;
					while (result.next()) {count ++;}
					createCategory(name, descr,count+1 , 0);
					
				}
			}
		}
		
	}

	
	//incrementa il numero dei figli
	public void updateChildrenCategory(int pid) throws SQLException {
		int numC=0;
		try (PreparedStatement pstatement = connection.prepareStatement(
				"SELECT P.children FROM  dbhtmlh.category P WHERE P.id = ?");) {
			pstatement.setInt(1, pid);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					numC=result.getInt("children");
				}
			}
			
			
		}
		String query = "UPDATE dbhtmlh.category SET children = ? WHERE id = ? ";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, numC+1);
			pstatement.setInt(2, pid);
			pstatement.executeUpdate();
		}
	}

	//crea i link
	public void createLink(int fatherId, int childId) throws SQLException {
		
		
		String query = "INSERT into dbhtmlh.subcats(father, child) VALUES(?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
				pstatement.setInt(1, fatherId);
				pstatement.setInt(2, childId);
				pstatement.executeUpdate();
		}
	}
//dato un id ritorna la categoria
	public Category getCategory(int pid) throws SQLException{
		Category category = new Category();
		try (PreparedStatement pstatement = connection.prepareStatement("SELECT * FROM category WHERE id = ?");) {
			pstatement.setInt(1, pid);
		
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					while (result.next()) {
					
						category.setId(result.getInt("id"));
						category.setName(result.getString("name"));
						category.setDescription(result.getString("description"));
						category.setChildren(result.getInt("children"));
					
				}
					
				}
			}
				
			
		}
		return category;
	}
	
	
	
	
	}
