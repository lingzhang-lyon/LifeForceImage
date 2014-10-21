package project1.cmpe275.sjsu;

import java.io.File;

/**
 * This class is the model for storing image attributes and the file 
 * as well as some other component needed for MasterServer and SlaveServer
 *
 */
public class Image {
	public String imageName="";
	public String category="";
	public long userID=0;
	public File file=null;
	
	//TODO determine more attributes that needed
	public String NodeAdress="";
	
	
	
	public long getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getNodeAdress() {
		return NodeAdress;
	}

	public void setNodeAdress(String nodeAdress) {
		NodeAdress = nodeAdress;
	}
	
	

}
