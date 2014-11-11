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
	
	public ArrayList<String> FindSlave2(){
		//ramdom number
		ArrayList<String> slaves = new ArrayList<String>();
		
		slaves.add("127.0.0.1:27017");
		slaves.add("127.0.0.1:27017");
		return slaves;
		
		
		
	}

	
}