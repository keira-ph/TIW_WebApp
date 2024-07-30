package it.polimi.tiw.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import it.polimi.tiw.beans.Image;


public class ImageDAO {
	private Connection con;

	public ImageDAO(Connection connection) {
		this.con = connection;
	}

	public List<Image> findImagesById(int id) throws SQLException {
		List<Image> images = new ArrayList<Image>();
		String query = "SELECT image FROM image WHERE id = ? ";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, id);
			result = pstatement.executeQuery();
			if (!result.isBeforeFirst()) // no results, credential check failed
				return null;
			else {
				
				while (result.next()) {
					Image image = new Image();
					
					
					byte[] imgData = result.getBytes("image");
					String encodedImg=Base64.getEncoder().encodeToString(imgData);
					image.setImage(encodedImg);
								
					images.add(image);
				}
			}	
		} catch (SQLException e) {
		    e.printStackTrace();
			throw new SQLException(e);

		} 
		return images;
	}

	public void createImage( InputStream imageStream, int userId) throws SQLException {
		String query = "INSERT into image ( image, id  ) VALUES(?, ?)";
		PreparedStatement pstatement = null;
				
		try {
			pstatement = con.prepareStatement(query);
			
			pstatement.setBlob(1, imageStream);
			pstatement.setInt(2, userId);
			pstatement.executeUpdate();
		} catch (SQLException e) {
		    e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				pstatement.close();
			} catch (Exception e1) {}
		}
		return;
	}
}
	