package project1.cmpe275.sjsu.partionAndReplication;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Stack;





//import com.mongodb.MongoException;
import project1.cmpe275.sjsu.conf.Configure;
import project1.cmpe275.sjsu.model.Socket;
//import com.mongodb.DBObject;





import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class SlaveFinderV2 {
    
    private int numberOfRep = Configure.NumberOfReplication;
    //store the cached result. Avoid sorting every time
    private Stack<String> cache= new Stack<String>();
    
	public ArrayList<String> FindSlave(){
		
		ArrayList<String> slaves = new ArrayList<String>();
		
		slaves.add("192.168.1.3:27017");
		slaves.add("192.168.1.5:27017");
		return slaves;
		
		
		
	}
	
	public Socket FindSlave2(){
		//TODO need to make sure everytime will return different slave
		return (new Socket("127.0.0.1",27017) );
		
		
		
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