package project1.cmpe275.sjsu.master;

import java.util.ArrayList;

import project1.cmpe275.sjsu.model.Image;

public class MessageSender {
	
   public void setStorgeLoal(ArrayList<String>nodes,Image image){
	   	   //assume index 0 is storage location, index 1 is replica location.
		   image.setNodeAdress(nodes.get(0));
		   image.setBackupNodeAdress(nodes.get(1));
	    }
   
   
}
