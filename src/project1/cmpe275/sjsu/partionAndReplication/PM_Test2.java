package project1.cmpe275.sjsu.partionAndReplication;



import java.net.UnknownHostException;
import java.util.ArrayList;

import project1.cmpe275.sjsu.model.Image;
import project1.cmpe275.sjsu.model.Socket;
import project1pbversion.cmpe275.sjsu.database.DatabaseManagerV2;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.RequestType;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.ResponseFlag;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;
import project1pbversion.cmpe275.sjsu.protobuf.MessageManager;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;


public class PM_Test2 {

	public Request upload(Image image){
		boolean r1=false;
		boolean r2= false;
		ArrayList<String> soc = new ArrayList<String>();
		//soc.add("127.0.0.1:27017");
		//soc.add("127.0.0.1:27017");
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
		System.out.println(socket.get(0).getIp());
		System.out.println(socket.get(1).getIp());
		Request req1= DatabaseManagerV2.uploadToDB(socket.get(0),image);
		Request req2= DatabaseManagerV2.uploadToDB(socket.get(1),image);
		
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
			DatabaseManagerV2.deleteInDB(socket.get(0), image);
			return this.upload(image);
		}
		else if(r2&&(!r1)){
			DatabaseManagerV2.deleteInDB(socket.get(1), image);
			return this.upload(image);
		}
		//both fails. recall in some time, then timeout return false request;
			//*/
		return req;
		
	}
	
	private void storeToMasterDB(Socket s, Image image) {
	
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("275db");  // Connect DB
	        DBCollection collection = db.getCollection("Meta");  // Connect collectionClass
	        
	        String sock= s.getIp()+":"+s.getPort();
	        BasicDBObject o = new BasicDBObject("socket", sock)
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
	        Request req1=DatabaseManagerV2.downloadFromDB(soc.get(0), image);
	        
	        if(req1.getHeader().getPhotoHeader().getResponseFlag()== ResponseFlag.success){
	        	return req1;
	        }
	        else {
	        	Request req2=DatabaseManagerV2.downloadFromDB(soc.get(0), image);
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
		
	        Request req1=DatabaseManagerV2.deleteInDB(soc.get(0), image);
	        Request req2=DatabaseManagerV2.deleteInDB(soc.get(1), image);
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
				DatabaseManagerV2.uploadToDB(soc.get(0), image);
				return this.delete(image);
			}
			else if(r2&&(!r1)){
				DatabaseManagerV2.uploadToDB(soc.get(1), image);
				return this.delete(image);
			}
			//both fails. recall in some time, then timeout return false request;
			return req;	//
	        
		
	}

	private ArrayList<Socket> trans(ArrayList<String> soc){
		Socket s1= new Socket();
		Socket s2= new Socket();
		ArrayList<Socket> socket = new ArrayList<Socket>();
		s1.setIp(soc.get(0).split(":")[0]);
		s1.setPort(Integer.parseInt(soc.get(0).split(":")[1]));
		socket.add(s1);
		s2.setIp(soc.get(1).split(":")[0]);
		s2.setPort(Integer.parseInt(soc.get(1).split(":")[1]));
		socket.add(s2);
		
		return socket;
	}

	public static void main(String[] args){
		PM_Test2 pm1= new PM_Test2();
	    ArrayList<Socket> soc = new ArrayList<Socket>();
	    ArrayList<String>  result = new ArrayList<String>();
	    result.add("192.168.1.1:80");
	    result.add("192.168.1.2:80");
	    soc= pm1.trans(result);
	    System.out.println(soc.get(0).getIp());
	    System.out.println(soc.get(0).getPort());
	    System.out.println(soc.get(1).getIp());
	    System.out.println(soc.get(1).getPort());
		
		
	}

}
