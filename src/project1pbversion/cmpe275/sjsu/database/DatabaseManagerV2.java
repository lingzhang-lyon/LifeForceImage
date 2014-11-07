package project1pbversion.cmpe275.sjsu.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;

import org.bson.types.Binary;

import project1.cmpe275.sjsu.conf.Configure;
import project1.cmpe275.sjsu.model.Image;
import project1.cmpe275.sjsu.model.Socket;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Header;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Payload;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoPayload;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.RequestType;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.ResponseFlag;

import com.google.protobuf.ByteString;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

/**
 *  Database Operations Manager
 */
public class DatabaseManagerV2 {
	
	public static Mongo mongo;
	public static DB db;
	public static DBCollection collection;
	
	static final String filePath = System.getProperty("file", Configure.clientFilePath);
	static final String MongoHost = Configure.MongoHost;
	static final int 	MongoPort = Configure.MongoPort;
	static final String DBName = Configure.DBName;
	static final String CollectionName = Configure.CollectionName;
	
	
	/**
	 *  Database Connecting Method
	 */
	public void connectDatabase(String mongoHost, int mongoPort)
	{
		try {
			// DB connection
        	mongo = new Mongo(mongoHost, mongoPort);  // Connect DB Server
            db = mongo.getDB(DBName);  // Connect DB
            collection = db.getCollection(CollectionName);  // Connect collection
		} catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        } 
	}
	
	/**
	 *  Database Connecting Method
	 */
	public void connectDatabase(String mongoHost, int mongoPort, String collectionName)
	{
		try {
			// DB connection
        	mongo = new Mongo(mongoHost, mongoPort);  // Connect DB Server
            db = mongo.getDB(DBName);  // Connect DB
            collection = db.getCollection(collectionName);  // Connect collection
		} catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        } 
	}
    
	
    /**
	 *  Image downloading Method
	 */
    public Request downloadFromDB(Socket socket, Image img) 
    {
    	// return variable
    	Request downloadResponseRequest = null;
    	boolean responseFlag = false;
    	String fileName = null;
    	ByteString imageData = null;
    	PhotoPayload pp = null;
    			
    	// Connect to DB
    	String host=socket.getIp();
    	int port=socket.getPort();
    	DatabaseManagerV2 dm = new DatabaseManagerV2();
    	dm.connectDatabase(host, port);
    			
    	// Parse the info of image
    	String uuid = img.getUuid();
    	
    	// Download an image from DB by UUID
    	Image retrievedImage = retrieveProperties(uuid, collection);

    	if(retrievedImage.imageName != "") {  // Retrieve successfully
    		responseFlag = true;
    		fileName = retrievedImage.getImageName();
    		imageData = retrieveData(uuid, collection);
    		
    		pp = PhotoPayload.newBuilder()
					  		 .setUuid(uuid)
					  		 .setName(fileName)
					  		 .setData(imageData)
					  		 .build();
    	} else {  // Retrieve failed
    		pp = PhotoPayload.newBuilder()
					  		 .setUuid(uuid)
					  		 .build();
    	}
    	
    	// Organize return info
    	Payload p = Payload.newBuilder().setPhotoPayload(pp).build();

    	PhotoHeader ph = PhotoHeader.newBuilder()
    								.setResponseFlag(responseFlag? ResponseFlag.success : ResponseFlag.failure)
   									.setRequestType(RequestType.read)
   									.build();	         	      	       	    	 
    	Header h = Header.newBuilder().setPhotoHeader(ph).build();

    	downloadResponseRequest = Request.newBuilder()
    								   .setHeader(h)
    								   .setBody(p)
    								   .build();

    	System.out.println("UUID in response to download: "
	 			+ downloadResponseRequest.getBody().getPhotoPayload().getUuid());
    	
    	return downloadResponseRequest;
    	
    	// Close DB
    }

    
    /**
	 *  Image uploading Method
	 */
	public Request uploadToDB(Socket socket, Image img) {
		// return variable
		Request uploadResponseRequest = null;
		
		// Connect to DB
		String host=socket.getIp();
    	int port=socket.getPort();
		DatabaseManagerV2 dm = new DatabaseManagerV2();
		dm.connectDatabase(host,port);
		
		// Parse the info of image
		String 		uuid		= img.getUuid();
    	String 		fileName	= img.getImageName();
    	String 		client		= img.getUserName();
    	String 		uploadTime	= img.getCreated();
    	String 		storedName	= client + uploadTime + fileName;
    	String 		category	= img.getCategory();
    	ByteString	fileData	= img.getData();

    	// Insert an image to DB
    	insert(uuid, fileName, client, uploadTime, storedName, category, fileData, collection);

		// Organize return info
    	PhotoPayload pp = PhotoPayload.newBuilder()
    								  .setUuid(uuid).setName(fileName)
    								  .build();
    	Payload p = Payload.newBuilder().setPhotoPayload(pp).build();

    	PhotoHeader ph = PhotoHeader.newBuilder()
    								.setResponseFlag(ResponseFlag.success)
   									.setRequestType(RequestType.write)
   									.build();	         	      	       	    	 
    	Header h = Header.newBuilder().setPhotoHeader(ph).build();

    	uploadResponseRequest = Request.newBuilder()
    								   .setHeader(h)
    								   .setBody(p)
    								   .build();

    	System.out.println("UUID in response to upload: "
	 			+ uploadResponseRequest.getBody().getPhotoPayload().getUuid());
    	
    	// Close DB
		
		return uploadResponseRequest;
	}
	
	/**
	 *  Image uploading Method
	 *  only changed to insert2()
	 */
	public Request uploadToDB2(Socket socket, Image img) {
		// return variable
		Request uploadResponseRequest = null;
		
		// Connect to DB
		String host=socket.getIp();
    	int port=socket.getPort();
		DatabaseManagerV2 dm = new DatabaseManagerV2();
		dm.connectDatabase(host,port);
		
		// Parse the info of image
		String 		uuid		= img.getUuid();
    	String 		fileName	= img.getImageName();
    	String 		client		= img.getUserName();
    	String 		uploadTime	= img.getCreated();
    	String 		storedName	= client + uploadTime + fileName;
    	String 		category	= img.getCategory();
    	ByteString	fileData	= img.getData();

    	// Insert an image to DB
    	//insert(uuid, fileName, client, uploadTime, storedName, category, fileData, collection);
    	insert2(uuid, fileName, client, uploadTime, storedName, category, fileData, collection);

		// Organize return info
    	PhotoPayload pp = PhotoPayload.newBuilder()
    								  .setUuid(uuid).setName(fileName)
    								  .build();
    	Payload p = Payload.newBuilder().setPhotoPayload(pp).build();

    	PhotoHeader ph = PhotoHeader.newBuilder()
    								.setResponseFlag(ResponseFlag.success)
   									.setRequestType(RequestType.write)
   									.build();	         	      	       	    	 
    	Header h = Header.newBuilder().setPhotoHeader(ph).build();

    	uploadResponseRequest = Request.newBuilder()
    								   .setHeader(h)
    								   .setBody(p)
    								   .build();

    	System.out.println("UUID in response to upload: "
	 			+ uploadResponseRequest.getBody().getPhotoPayload().getUuid());
    	
    	// Close DB
		
		return uploadResponseRequest;
	}
	
	
    /**
	 *  Image deleting Method
	 */
	public Request deleteInDB(Socket socket, Image img) {
		// return variable
		Request deleteResponseRequest = null;
		String host=socket.getIp();
    	int port=socket.getPort();
		DatabaseManagerV2 dm = new DatabaseManagerV2();
		dm.connectDatabase(host,port);
		
		String uuid 	= img.getUuid();
		String fileName = img.getImageName();
		
		// Delete an image in DB
		delete(uuid, collection);
		
		// Organize return info
    	PhotoPayload pp = PhotoPayload.newBuilder()
    								  .setUuid(uuid).setName(fileName)
    								  .build();
    	Payload p = Payload.newBuilder().setPhotoPayload(pp).build();

    	PhotoHeader ph = PhotoHeader.newBuilder()
    								.setResponseFlag(ResponseFlag.success)
   									.setRequestType(RequestType.delete)
   									.build();	         	      	       	    	 
    	Header h = Header.newBuilder().setPhotoHeader(ph).build();

    	deleteResponseRequest = Request.newBuilder()
    								   .setHeader(h)
    								   .setBody(p)
    								   .build();

    	System.out.println("UUID in response to delete: "
	 			+ deleteResponseRequest.getBody().getPhotoPayload().getUuid());
    	
    	// Close DB
		
		return deleteResponseRequest;
	}
	
	
    /**
	 *  Info Inserting Method
	 */
    public void insert(String uuid, String originalName, String client, 
    				   String uploadTime, String storedName, String category, 
    				   ByteString imageData, 
    				   DBCollection collection)
    {
    	BasicDBObject o = new BasicDBObject();
        
        // prepare inserting statement
        o.append("Uuid", uuid)
         .append("Image Name", storedName)
         .append("Upload User", client)
         .append("Upload Time", uploadTime)
         .append("Category", category)
         .append("Image Data",imageData);
        
        collection.insert(o);
        
        System.out.println("Inserted record of " + uuid + ", file size = " + imageData.size());
    }
    
    /**
	 *  Info Inserting Method
	 */
    //store byte[] to mongoDB
    public void insert2(String uuid, String originalName, String client, 
    				   String uploadTime, String storedName, String category, 
    				   ByteString imageData, 
    				   DBCollection collection)
    {
    	 //File imageFile = fileData;
    	 //FileInputStream f = new FileInputStream(imageFile);
    	 byte b[] = imageData.toByteArray();
    	 //f.read(b);
    	 // data of image
    	 Binary binaryData = new Binary(b);
    	
    	BasicDBObject o = new BasicDBObject();
        
        // prepare inserting statement
        o.append("Uuid", uuid)
         .append("Image Name", storedName)
         .append("Upload User", client)
         .append("Upload Time", uploadTime)
         .append("Category", category)
         .append("Image Data",binaryData);
        
        collection.insert(o);
        
        System.out.println("Inserted record of " + uuid + ", file size = " + binaryData.length());
    }
    
    
    
    
    /**
   	 *  Info updateSlaveStatus Method
   	 */
       public void updateSlaveStatus(String ip,double d, DBCollection collection)
       {
       	BasicDBObject o = new BasicDBObject();
      
           // prepare inserting statement
           o.append("ipaddress", ip)
            .append("loadfactor", d);
            
           
           collection.insert(o);
           
           System.out.println("Inserted record of " + ip + ", loadfactor = " + d);
       }
      
    
    /**
  	 *  Image Byte Data Retrieving Method
  	 */
    public ByteString retrieveData(String uuid, DBCollection collection)
    {
    	ByteString retrievedImageData = null;
    	DBObject obj = collection.findOne(new BasicDBObject("Uuid", uuid));
    	
    	retrievedImageData = ByteString.copyFrom((obj.get("Image Data").toString().getBytes()));
		
		System.out.println("Retrieved Byte String data of " + uuid +", data size = " + retrievedImageData.size());
		
		return retrievedImageData;
    }
    
    /**
  	 *  Image Byte Data Retrieving Method
  	 */
    public ByteString retrieveData2(String uuid, DBCollection collection)
    {
    	ByteString retrievedImageData = null;
    	DBObject obj = collection.findOne(new BasicDBObject("Uuid", uuid));
    	
    	retrievedImageData = ByteString.copyFrom((obj.get("Image Data").toString().getBytes()));
		
		System.out.println("Retrieved Byte String data of " + uuid +", data size = " + retrievedImageData.size());
		
		return retrievedImageData;
    }
    
    
    /**
  	 *  Image Properties Retrieving Method
  	 */
	public Image retrieveProperties(String uuid, DBCollection collection)
	{
		String fileName;
		Image retrievedImage = new Image();
		
		DBObject obj = collection.findOne(new BasicDBObject("Uuid", uuid));

		// Nothing found 
		if (obj == null)
			return retrievedImage;
		
		// Set retrieved file name
		fileName = (String)obj.get("Image Name");              
		retrievedImage.setImageName(fileName);
		
		// Set retrieved file name
		retrievedImage.setUuid(uuid);

		System.out.println("Retrieved Properties of " + uuid +" , file name = " + fileName);
		
		return retrievedImage;
	}
	
	
	/**
  	 *  Info deleting Method
  	 */
	public void delete(String uuid, DBCollection collection)
	{	
		WriteResult result = collection.remove(new BasicDBObject("Uuid", uuid));
		
	    System.out.println("Image of " + uuid +" deleted with " + result.getN() + " records");
	}
}


	
