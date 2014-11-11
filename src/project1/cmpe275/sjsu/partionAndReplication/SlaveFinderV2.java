package project1.cmpe275.sjsu.partionAndReplication;


import java.net.UnknownHostException;
import java.util.ArrayList;

import project1.cmpe275.sjsu.model.Socket;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class SlaveFinderV2 {
	
	public ArrayList<Socket> slaveList=new ArrayList<Socket>();
	
	public SlaveFinderV2(){
		//predefined slaveList
		ArrayList<Socket> slaveList =new ArrayList<Socket>();
		
		slaveList.add(new Socket("127.0.0.1",27017) );
		slaveList.add(new Socket("127.0.0.1",27017) );
		slaveList.add(new Socket("127.0.0.1",27017) );
		slaveList.add(new Socket("127.0.0.1",27017) );
		
		this.slaveList=slaveList;
	}
    
	
	public Socket findSlave2(){
		//TODO need to make sure will return different slave each time
		//get a random number between 0 and slaveList size
		int listSize=slaveList.size();
		int randomNumber = (int) Math.random() * listSize ;
		
		return this.slaveList.get(randomNumber) ;
	
	}
	
	
	

	public Socket searchImageStoreSocket(String uuid) throws UnknownHostException {
	   //search in Meta Collection
		Mongo mongo = new Mongo("localhost", 27017);
		DB db = mongo.getDB("275db");  // Connect DB
        DBCollection collection = db.getCollection("Meta");  // Connect collectionClass
        
        DBObject obj = collection.findOne(new BasicDBObject("uuid", uuid));
        String storeip=(String) obj.get("storeip"); 
        int storeport=(Integer) obj.get("storeport");
        return new Socket(storeip, storeport);
	}
        
	
	

	
}