package it.polimi.tiw.beans;

public class MyDataStructure {
    private String categoryID;
    private String newName;

    public String getId() {
        return categoryID;
    }

    public void setId(String id) {
        this.categoryID = id;
    }

    public String getNewname() {
        return newName;
    }

    public void setNewname(String newname) {
        this.newName = newname;
    }
}