package project1.cmpe275.sjsu.partionAndReplication;


import java.net.UnknownHostException;
import java.util.ArrayList;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import project1.cmpe275.sjsu.model.Image;
import project1.cmpe275.sjsu.model.Socket;
import project1pbversion.cmpe275.sjsu.database.DatabaseManager;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.ResponseFlag;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;


public class PartitionManager {

	public Request upload(Image image){
		boolean r1=false;
		boolean r2= false;
		ArrayList<String> soc = new ArrayList<String>();
		SlaveFinder sla = new SlaveFinder();
		soc= sla.FindSlave();
		
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
		if(r1&&r2) {
			this.storeToMasterDB(socket.get(0),image);
			this.storeToMasterDB(socket.get(1), image);
			return req1;
		}
		if(r1)
		return req2;
		else if(r2)
			return req1;
		return req1;			
		
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
		else return ds
		
	        DatabaseManager DB = new DatabaseManager();
	        Request req1=DB.deleteFromDB(soc.get(0), image);
	        Request req2=DB.deleteFromDB(soc.get(1), image);
		
		
		return null;
		
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
