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


public class SlaveFinderTest {
    
    private int numberOfRep=2;
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
			Mongo mongo = new Mongo("127.0.0.1", 27017);  // Connect DB Server
	        DB db = mongo.getDB("275db");  // Connect DB
	        DBCollection collection = db.getCollection("Slave");  // Connect collectionClass
	        ArrayList<String> temp = new ArrayList<String>();
	        

	        DBCursor cursor = collection.find(new BasicDBObject(),new BasicDBObject("socketstring", 1)).sort(new BasicDBObject("loadfactor", -1)).limit(10);
				 
	       
	        
	        while(cursor.hasNext()){
					    //temp.add((cursor.next().get("socketstring").toString()));
	        			System.out.println(cursor.next().toString());
	        			//temp.add("127.0.0.1:27017"); 
					    try{
					    	System.out.println(cursor.next().toString());
					    	temp.add((cursor.next().get("socketstring").toString()));
					    }catch (NullPointerException e){
					    	System.out.println("could not find socketstring for this record");
					    	System.out.println(cursor.next().toString());
					    }
					    //System.out.println(cursor.next().toString());


				   }
				cursor.close();
				for(int i=0; i<10; i++){
					for(int j=0; j<temp.size(); j++)
						cache.push(temp.get(j));
				}
		}catch(UnknownHostException e){
	            e.printStackTrace();
	        }
	}
	
	public static void main(String[] args){
		SlaveFinderTest sf = new SlaveFinderTest();
		ArrayList<String> sl = sf.FindSlave();
		ArrayList<String> sl1 = sf.FindSlave();
		ArrayList<String> sl2 = sf.FindSlave();
		ArrayList<String> sl3= sf.FindSlave();
		System.out.println(sl.get(0));
		System.out.println(sl.get(1));
		System.out.println(sl1.get(0));
		System.out.println(sl1.get(1));
		System.out.println(sl2.get(0));
		System.out.println(sl2.get(1));
		System.out.println(sl3.get(0));
		System.out.println(sl3.get(1));
		
	}

}
