
package it.polimi.tiw.beans;

import java.util.ArrayList;
import java.util.*;
import java.io.Serializable;

public class Category implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private String name;
	private String description;
	private int children;
	private boolean isTop =false;
	
	
	
	private List<Category> subcats = new ArrayList<Category>();

	public Category() {}
	

	public int getId() {
		return this.id;
	}

	public void setId(int idproduct) {
		this.id = idproduct;
	}

	public Boolean getIsTop() {
		return this.isTop;
	}

	public void setIsTop(Boolean ist) {
		this.isTop = ist;
	}
	
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String image) {
		this.description = image;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getChildren() {
		return children;
	}
	public void setChildren(int i) {
		children = i;
	}
	
	public void increaseNumChildren() {
		children++;
	}

	
	public List<Category> getSubcats() {
		return subcats;
	}

	
	public void  addSubcat( Category c) {
		for(Category cat : subcats) {
			if(cat.getId()>c.getId()) {
				subcats.add(subcats.indexOf(cat),c);
				return;
			}
		}
		subcats.add(c);
	}

	


	public String toString() {
		StringBuffer aBuffer = new StringBuffer("Category");
		
		
		aBuffer.append(id);
		aBuffer.append(" : ");
		aBuffer.append(name);
		
		return aBuffer.toString();
	}

	public void setIsTop(boolean b) {
		isTop=b;
		// TODO Auto-generated method stub
		
	}

}