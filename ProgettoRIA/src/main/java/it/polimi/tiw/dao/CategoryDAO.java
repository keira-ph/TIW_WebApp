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


	public CategoryDAO(Connection con) {
		this.connection = con;
	}

	//OK
	public List<Category> findAllCategories() throws SQLException {
		List<Category> Categorys = new ArrayList<Category>();
		try (PreparedStatement pstatement = connection.prepareStatement("SELECT * FROM category");) {
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Category BP = new Category();
					BP.setId(result.getInt("id"));
					BP.setName(result.getString("name"));
					BP.setChildren(result.getInt("children"));
					
					
					addCategory(Categorys, BP);
				}
			
		}
	  }
		
	
		
		return Categorys;
	}
	
	//OK
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
	
	
	//OK
	public List<Category>  addCategory( List<Category> l, Category c) {
		for(Category cat : l) {
			if(cat.getId() == c.getId() && cat.getId()>c.getId()) {
				l.add(l.indexOf(cat) , c);
				return l;
			}else if(cat.getId() > c.getId() && c.getId()<=Integer.parseInt(Integer.toString(cat.getId()).substring(0, Integer.toString(c.getId()).length())) ) {
				l.add(l.indexOf(cat) , c);
				return l;
			}else if(cat.getId() < c.getId() && cat.getId()>Integer.parseInt(Integer.toString(c.getId()).substring(0, Integer.toString(cat.getId()).length())) ) {
				l.add(l.indexOf(cat) , c);
				return l;
			}
		}
		l.add(c);
		return l;
	}
	
	
	public List<Category> findTopCategoriesAndSubtrees() throws SQLException {
		List<Category> Categorys = new ArrayList<Category>();
		try (PreparedStatement pstatement = connection.prepareStatement("SELECT * FROM category WHERE id NOT IN (SELECT child FROM dbhtml.subcats)");) {
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Category BP = new Category();
					BP.setId(result.getInt("id"));
					BP.setName(result.getString("name"));
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
	
		
	public void findSubcats(Category p) throws SQLException {
		Category BP = null;
		try (PreparedStatement pstatement = connection.prepareStatement(
				"SELECT P.id, P.name, P.children FROM subcats S JOIN category P on P.id = S.child WHERE S.father = ?");) {
			pstatement.setInt(1, p.getId());
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					BP = new Category();
					BP.setId(result.getInt("id"));
					BP.setName(result.getString("name"));
					BP.setChildren(result.getInt("children"));
					findSubcats(BP);
					p.addSubcat(BP);
				}
			}
		}
	} 
	
	public Category findSubtreeOf(int id, String name,int children) throws SQLException {
		Category newp = new Category();
		newp.setId(id);
		newp.setName(name);
		newp.setChildren(children);
		newp.setIsTop(false);
		findSubcats(newp);
			
		return newp;
	}
	
	
	public void handleCopy(int[][] arr) throws Exception {
		boolean ok = true;
		ArrayList<Integer> allIds= findAllIds();
		for(int i=0;i<arr.length;i++) {

            if(!allIds.contains(arr[i][0]) || !allIds.contains(arr[i][1])) {
                ok=false;
            }
            
            

        }
		if(ok) {
			
        connection.setAutoCommit(false);

        try {
            for(int i=0;i<arr.length;i++) {

                if(arr[i][0]<0 || arr[i][1]<=0) {
                    throw new Exception();
                }
                
                int fid = arr[i][0];
                int cid = arr[i][1];

                copyHereTheTree(cid,fid);

            }

            connection.commit();

        } catch(Exception e) {
            connection.rollback();
            throw e;

        } finally {
            connection.setAutoCommit(true);
        }
		}
        


    }
	

	public void copyHereTheTree(int cid, int destid) throws Exception {
    	
        Category chosen = findById(cid);  //id e sottoalbero che voglio copiare
        Category destination = findById(destid);  //NUOVO FATHER ID

        if(chosen == null || destination == null) {
            throw new SQLException();
        }

        int destNumChild = findNumChild(destination);

        if(destNumChild >= 9) {
            throw new Exception("Maximum number of children reached");
        }
        if(cyclicLinkExists(destid,cid)) {
            throw new Exception("Impossible to complete the copy");
        }
        
        if(destid != 0) {
			
			Category radiceDelSottoalbero = findSubclasses (chosen);
		
			int newPosition = (destination.getId()*10)+ (destNumChild+1);
			
	        createCategory(chosen.getName(), newPosition, 0);
			
			updateChildrenCategory(destid);  
			createLink (destid ,  newPosition);
			int numCifre = String.valueOf(radiceDelSottoalbero.getId()).length();
			
			IdChange(newPosition, radiceDelSottoalbero, numCifre);
			treeMaking(radiceDelSottoalbero, destid);
		}
        
        else{
			
			try (PreparedStatement pstatement = connection.prepareStatement("SELECT id FROM category WHERE id NOT IN (SELECT child FROM dbhtml.subcats)");) {
				try (ResultSet result = pstatement.executeQuery();) {
					int count=0;
					while (result.next()) {count ++;}
					
					Category radiceDelSottoalbero = findSubclasses (destination);
					createCategory(radiceDelSottoalbero.getName(), count+1, 0);
					int numCifre = String.valueOf(radiceDelSottoalbero.getId()).length();
					IdChange(count+1, radiceDelSottoalbero, numCifre);
					treeMaking(radiceDelSottoalbero, destid);
				}
			}
		}
	}
	
	
	private Category registerCategory(ResultSet result) throws SQLException {

        Category cat = new Category();
        cat.setId(result.getInt("id"));
        cat.setName(result.getString("name"));
        
        return cat;
    }
	
	
	private Category findById(int id) throws SQLException {

        String query = "SELECT * FROM category WHERE id = ?";
        Category category = null;
        try (PreparedStatement pStatement = connection.prepareStatement(query)) {
            pStatement.setInt(1, id);
            try (ResultSet result = pStatement.executeQuery()) {
                while (result.next()) {
                    category = registerCategory(result);
                }
            }
        }

        return category;
    }

	
	public void  treeMaking (Category cat, int idNuovoPadre) throws SQLException {
		if (!(cat.getSubcats().isEmpty())) {
			for (Category c: cat.getSubcats()) {
		    	createCategory(c.getName(), c.getId(),0);
		    	updateChildrenCategory(c.getId()/10);
				createLink( c.getId()/10,  c.getId());
				treeMaking(c,idNuovoPadre);
		    }
		}
		
	}
	
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
	

	public void createCategory(String name, int id, int children) throws SQLException {
		String query = "INSERT into dbhtml.category (name, id, children) VALUES(?, ?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, name);
			pstatement.setInt(2, id);
			pstatement.setInt(3, children);
			pstatement.executeUpdate();
		}
	}
	
	public void addCategory(String name, int fid) throws SQLException {
		
		if(fid != 0) {
			
			try (PreparedStatement pstatement = connection.prepareStatement(
				"SELECT P.children FROM  dbhtml.category P WHERE P.id = ?");) {
				pstatement.setInt(1, fid);
				try (ResultSet result = pstatement.executeQuery();) {
					while (result.next()) {
						int numC=result.getInt("children");
						
						int id=(fid*10)+(numC+1);
						createCategory(name, id, 0);
						updateChildrenCategory(fid);
						createLink(fid , id);
					}
				}
			}
			
		}else{
			try (PreparedStatement pstatement = connection.prepareStatement("SELECT id FROM category WHERE id NOT IN (SELECT child FROM dbhtml.subcats)");) {
				try (ResultSet result = pstatement.executeQuery();) {
					int count=0;
					while (result.next()) {count ++;}
					createCategory(name,count+1 , 0);
					
				}
			}
		}
		
	}
	
	
	public void updateChildrenCategory(int pid) throws SQLException {
		int numC=0;
		try (PreparedStatement pstatement = connection.prepareStatement(
				"SELECT P.children FROM  dbhtml.category P WHERE P.id = ?");) {
			pstatement.setInt(1, pid);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					numC=result.getInt("children");
				}
			}
			
			
		}
		String query = "UPDATE dbhtml.category SET children = ? WHERE id = ? ";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, numC+1);
			pstatement.setInt(2, pid);
			pstatement.executeUpdate();
		}
	}

	
	public void createLink(int fatherId, int childId) throws SQLException {

		String query = "INSERT into dbhtml.subcats(father, child) VALUES(?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
				pstatement.setInt(1, fatherId);
				pstatement.setInt(2, childId);
				pstatement.executeUpdate();
		}
	}

		
	public boolean cyclicLinkExists(int p1, int p2) throws SQLException {
        //check if p2 is an ancestor of p1 by using RECURSIVE
        boolean exists = false;

        String query = "with recursive cte (father, child) as (select father, child from dbhtml.subcats where child = ? union all select p.father, p.child from dbhtml.subcats p inner join cte on p.child = cte.father) select  * from cte where father = ?;";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, p1);
            pstatement.setInt(2, p2);
            // System.out.println(pstatement.toString());
            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next()) {
                    exists = true;
                }
            }
        }
        return exists;
    }
	
	private int findNumChild(Category father) throws SQLException {

        int fid = father.getId();
        int nChild = 0;

        String query = "SELECT COUNT(*) AS num FROM dbhtml.subcats WHERE father = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, fid);
            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next()) {
                    nChild = result.getInt("num");
                }
            }
        }

        return nChild;
    }
	
	public Category findSubclasses(Category c) throws SQLException {

        String query = "SELECT C.id, C.name FROM dbhtml.subcats R JOIN category C on C.id = R.child WHERE R.father = ?";

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {

            pstatement.setInt(1, c.getId());

            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next()) {
                    Category newCat = registerCategory(result);
                    findSubclasses(newCat);
                    c.addSubcat(newCat);
                }
            }
        }
        return c;
    }
	
	
	public void editName(int categoryID, String newName) throws SQLException {
	    String query = "UPDATE category SET name = ? WHERE id=?";
	    try (PreparedStatement pstatement = connection.prepareStatement(query);) {
	        pstatement.setString(1, newName);
	        pstatement.setInt(2, categoryID);

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
							category.setChildren(result.getInt("children"));
						
					}
						
					}
				}
					
				
			}
			return category;
		}
		//ritorna true se la categoria passata per id non ha ancora 9 figli
		public boolean isLegit(int id) throws SQLException {
			int children =0;
			try (PreparedStatement pstatement = connection.prepareStatement(
					"SELECT P.children FROM  dbhtml.category P WHERE P.id = ?");) {
				pstatement.setInt(1, id);
				try (ResultSet result = pstatement.executeQuery();) {
					while (result.next( )) {
						children=result.getInt("children");
					}
				}
				
				
			}
			return children<9;
		}
		// NB assicurarsi i dati passati in input siano validi
		public void copyHereTheTree(int  idNuovoPadre, int childrenNuovoPadre, int rootid, String rootname, int rootchildren) throws SQLException  {
			
			
				
				Category radiceDelSottoalbero = findSubtreeOf( rootid,  rootname, rootchildren);
			
		        int nuovoId = idNuovoPadre*10+childrenNuovoPadre+1;
		        
		        createCategory(radiceDelSottoalbero.getName(), nuovoId, 0);
				
				updateChildrenCategory(idNuovoPadre);
				createLink( idNuovoPadre,  nuovoId);
				int numCifre = String.valueOf(radiceDelSottoalbero.getId()).length();
				
				IdChange(nuovoId, radiceDelSottoalbero, numCifre );
				treeMaking(radiceDelSottoalbero, idNuovoPadre);
			
					
	        
		}
		
		
		

}
