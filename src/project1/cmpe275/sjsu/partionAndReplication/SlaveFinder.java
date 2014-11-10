package project1.cmpe275.sjsu.partionAndReplication;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Stack;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
//import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
//import com.mongodb.MongoException;
import project1.cmpe275.sjsu.conf.Configure;;

public class SlaveFinder {
    
    private int numberOfRep = Configure.NumberOfReplication;
    //store the cached result. Avoid sorting every time
    private Stack<String> cache= new Stack<String>();
    
	public ArrayList<String> FindSlave(){
		
		ArrayList<String> slaves = new ArrayList<String>();
		
		//We have element in cache
		if(cache.size()>=numberOfRep){
		for(int i=0;i<numberOfRep; i++){
			slaves.add(cache.pop());
		}
		return slaves;
		}
		//else update cache and re-find
		else {
			this.updateCache();
		    return this.FindSlave();
		}
		
	}

	public void updateCache(){
		try{
			Mongo mongo = new Mongo("localhost", 27017);  // Connect DB Server
	        DB db = mongo.getDB("275db");  // Connect DB
	        DBCollection collection = db.getCollection("Slave");  // Connect collectionClass
	        ArrayList<String> temp = new ArrayList<String>();
	        
	        DBCursor cursor = collection.find(new BasicDBObject(),new BasicDBObject("socketstring", 1)).sort(new BasicDBObject("loadfactor", -1)).limit(10);
				 while(cursor.hasNext()){
					    temp.add((cursor.next().get("socketstring").toString()));					   
				   }
				cursor.close();
				for(int i=0; i<2; i++){
					for(int j=0; j<temp.size(); j++)
						cache.push(temp.get(j));
				}
		}catch(UnknownHostException e){
	            e.printStackTrace();
	        }
	}
}