package project1.cmpe275.sjsu.partionAndReplication;



import java.net.UnknownHostException;
import java.util.ArrayList;

import project1.cmpe275.sjsu.model.Image;
import project1.cmpe275.sjsu.model.Socket;
import project1pbversion.cmpe275.sjsu.database.DatabaseManager;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.RequestType;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.ResponseFlag;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;
import project1pbversion.cmpe275.sjsu.protobuf.MessageManager;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;


public class PartitionManager {

	public Request upload(Image image){
		boolean r1=false;
		boolean r2= false;
		ArrayList<String> soc = new ArrayList<String>();
		SlaveFinder sla = new SlaveFinder();
		soc= sla.FindSlave();
		
		Image img= new Image();
		ResponseFlag res =ResponseFlag.failure;
		Request req = null;
		try {
			req = MessageManager.createResponseRequest(img, res, RequestType.write);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		ArrayList<Socket> socket=this.trans(soc);
		DatabaseManager DB = new DatabaseManager();
		
		Request req1= DB.uploadToDB(socket.get(0),image);
		Request req2= DB.uploadToDB(socket.get(1),image);
		
		if(req1.getHeader().getPhotoHeader().getResponseFlag() == ResponseFlag.success){
			r1=true;
		}
		if(req2.getHeader().getPhotoHeader().getResponseFlag() == ResponseFlag.success){
			r2=true;	
		}
		// if Success, write to the meta-data table and  return any request
		if(r1&&r2) {
			this.storeToMasterDB(socket.get(0),image);
			this.storeToMasterDB(socket.get(1), image);
			return req1;
		}
		// if anyone fails, recall the upload process
		else if(r1&&(!r2)){
		    DB.deleteInDB(socket.get(0), image);
			return this.upload(image);
		}
		else if(r2&&(!r1)){
			DB.deleteInDB(socket.get(1), image);
			return this.upload(image);
		}
		//both fails. recall in some time, then timeout return false request;
			//
		return req;
		
	}
	
	private void storeToMasterDB(Socket s, Image image) {
	
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("275db");  // Connect DB
	        DBCollection collection = db.getCollection("Meta");  // Connect collectionClass
	        
	        BasicDBObject o = new BasicDBObject("socket", s)
	        		              .append("uuid",image.getUuid())
	        		              .append("name", image.getImageName());
			collection.insert(o);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  // Connect DB Server
		
		// TODO Auto-generated method stub
		
	}
	
	 private void removeFromMasterDB(Socket s, Image image){
		 try {
				Mongo mongo = new Mongo("localhost", 27017);
				DB db = mongo.getDB("275db");  // Connect DB
		        DBCollection collection = db.getCollection("Meta");  // Connect collectionClass
		        
		        BasicDBObject o = new BasicDBObject("socket", s)
		        		              .append("uuid",image.getUuid())
		        		              .append("name", image.getImageName());
				collection.remove(o);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  // Connect DB Server
			
		 
		 
	 }
	
	public Request download(Image image){
		ArrayList<Socket> soc = new ArrayList<Socket>();
		ArrayList<String> soc_str = new ArrayList<String>();
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("275db");  // Connect DB
	        DBCollection collection = db.getCollection("Meta");  // Connect collectionClass
	        
	        DBCursor obj = collection.find(new BasicDBObject("uuid", image.getUuid()));
	        while(obj.hasNext()){
			    soc_str.add((obj.next().get("socket").toString()));					   
		   }
		     obj.close();
	        
		
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  // Connect DB Server
		soc = this.trans(soc_str);
	        DatabaseManager DB = new DatabaseManager();
	        Request req1=DB.downloadFromDB(soc.get(0), image);
	        
	        if(req1.getHeader().getPhotoHeader().getResponseFlag()== ResponseFlag.success){
	        	return req1;
	        }
	        else {
	        	Request req2=DB.downloadFromDB(soc.get(0), image);
	        	return	req2;
	        }
		// TODO Auto-generated method stub
		
	}
	
	public Request delete(Image image){
		ArrayList<Socket> soc = new ArrayList<Socket>();
		ArrayList<String> soc_str = new ArrayList<String>();
		boolean r1=false, r2= false;	
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("275db");  // Connect DB
	        DBCollection collection = db.getCollection("Meta");  // Connect collectionClass
	        
	        DBCursor obj = collection.find(new BasicDBObject("uuid", image.getUuid()));
	        while(obj.hasNext()){
			    soc_str.add((obj.next().get("socket").toString()));					   
		   }
		     obj.close();
	        
		
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  // Connect DB Server
		if(soc_str!= null)
		soc = this.trans(soc_str);
		
		Image img= new Image();
		ResponseFlag res =ResponseFlag.failure;
		Request req = null;
		try {
			req = MessageManager.createResponseRequest(img, res, RequestType.delete);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	        DatabaseManager DB = new DatabaseManager();
	        Request req1=DB.deleteInDB(soc.get(0), image);
	        Request req2=DB.deleteInDB(soc.get(1), image);
	        if(req1.getHeader().getPhotoHeader().getResponseFlag() == ResponseFlag.success){
				r1=true;
			}
			if(req2.getHeader().getPhotoHeader().getResponseFlag() == ResponseFlag.success){
				r2=true;	
			}
			if(r1&&r2) {
				this.removeFromMasterDB(soc.get(0), image);
				this.removeFromMasterDB(soc.get(1), image);
				return req1;
			}
			// if anyone fails, recall the upload process
			else if(r1&&(!r2)){
			    DB.uploadToDB(soc.get(0), image);
				return this.delete(image);
			}
			else if(r2&&(!r1)){
				DB.uploadToDB(soc.get(1), image);
				return this.delete(image);
			}
			//both fails. recall in some time, then timeout return false request;
			return req;	//
	        
		
	}

	private ArrayList<Socket> trans(ArrayList<String> soc){
		Socket s= new Socket();
		ArrayList<Socket> socket = new ArrayList<Socket>();
		s.setIp(soc.get(0).split(":")[0]);
		s.setPort(Integer.parseInt(soc.get(0).split(":")[1]));
		socket.add(s);
		s.setIp(soc.get(1).split(":")[0]);
		s.setPort(Integer.parseInt(soc.get(1).split(":")[1]));
		socket.add(s);
		
		return socket;
	}

}
