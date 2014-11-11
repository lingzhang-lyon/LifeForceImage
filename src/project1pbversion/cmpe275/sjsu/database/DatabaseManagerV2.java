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
	
	private Mongo mongo;
	private DB db;
	private DBCollection collection;
	
	static final String filePath = System.getProperty("file", Configure.clientFilePath);
	static final String MongoHost = Configure.MongoHost;
	static final int 	MongoPort = Configure.MongoPort;
	static final String DBName = Configure.DBName;
	static final String CollectionName = Configure.CollectionName;
	
	

	
	public Mongo getMongo() {
		return mongo;
	}


	public DB getDb() {
		return db;
	}


	public DBCollection getCollection() {
		return collection;
	}


	public void setMongo(Mongo mongo) {
		this.mongo = mongo;
	}


	public void setDb(DB db) {
		this.db = db;
	}


	public void setCollection(DBCollection collection) {
		this.collection = collection;
	}


	/**
	 * constructor, will set private collection
	 * @param mongoHost
	 * @param mongoPort
	 * @param DBName
	 * @param collectionName
	 */
	public DatabaseManagerV2(String mongoHost, int mongoPort, String DBName, String collectionName)
	{
		try {
			// DB connection
        	this.mongo = new Mongo(mongoHost, mongoPort);  // Connect DB Server
        	this.db = mongo.getDB(DBName);  // Connect DB
            this.collection = db.getCollection(collectionName);  // Connect collection
		} catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        } 
	}
	
	
	/**
	 *  Database Connecting Method --static method
	 *  will find collection with collectionName 
	 *  then set  dbCollection to found collection 
	 */
	public static void connectDatabase(String mongoHost, int mongoPort, String DBName, String collectionName, DBCollection dbCollection)
	{
		try {
			// DB connection
        	Mongo mongo = new Mongo(mongoHost, mongoPort);  // Connect DB Server
            DB db = mongo.getDB(DBName);  // Connect DB
            dbCollection = db.getCollection(collectionName);  // Connect collection
		} catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        } 
	}
	
	/**
	 *  Database Connecting Method
	 *  will assign the attributes of DBManager
	 *  
	 */
	public void connectDatabase(String mongoHost, int mongoPort, String DBName, String collectionName)
	{
		try {
			// DB connection
        	this.mongo = new Mongo(mongoHost, mongoPort);  // Connect DB Server
        	this.db = mongo.getDB(DBName);  // Connect DB
            this.collection = db.getCollection(collectionName);  // Connect collection
		} catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        } 
	}
	
	
    
	
    /**
	 *  Image downloading Method
	 */
    public static Request downloadFromDB(Socket socket, Image img) 
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
    	
    	DatabaseManagerV2 dm= new DatabaseManagerV2(host, port,DBName, CollectionName); 
    			
    	// Parse the info of image
    	String uuid = img.getUuid();
    	
    	// Download an image from DB by UUID
    	Image retrievedImage = dm.retrieveProperties(uuid, dm.collection);

    	if(retrievedImage.imageName != "") {  // Retrieve successfully
    		responseFlag = true;
    		fileName = retrievedImage.getImageName();
    		imageData = dm.retrieveData(uuid, dm.collection);
    		
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
	 *  
	 */
	public static Request uploadToDB(Socket socket, Image img) {
		// return variable
		Request uploadResponseRequest = null;
		
		
		try {
			// Connect to DB
			String host=socket.getIp();
	    	int port=socket.getPort();
	    	DatabaseManagerV2 dm= new DatabaseManagerV2(host,port,DBName, CollectionName);
			
			// Parse the info of image
			String 		uuid		= img.getUuid();
	    	String 		fileName	= img.getImageName();
	    	String 		client		= img.getUserName();
	    	String 		uploadTime	= img.getCreated();
	    	String 		storedName	= client + uploadTime + fileName;
	    	String 		category	= img.getCategory();
	    	ByteString	fileData	= img.getData();

	    	// Insert an image to DB
	    	dm.insert(uuid, fileName, client, uploadTime, storedName, category, fileData, dm.collection);

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
		} catch (Exception e) {
			// Organize return info
			String 		uuid		= img.getUuid();
	    	String 		fileName	= img.getImageName();
			
			PhotoPayload pp = PhotoPayload.newBuilder()
	    								  .setUuid(uuid).setName(fileName)
	    								  .build();
	    	Payload p = Payload.newBuilder().setPhotoPayload(pp).build();

	    	PhotoHeader ph = PhotoHeader.newBuilder()
	    								.setResponseFlag(ResponseFlag.failure)
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
		
	}
	
	
    /**
	 *  Image deleting Method
	 */
	public static Request deleteInDB(Socket socket, Image img) {
		// return variable
		Request deleteResponseRequest = null;
		String host=socket.getIp();
    	int port=socket.getPort();
    	
    	try {
    		DatabaseManagerV2 dm= new DatabaseManagerV2(host,port,DBName, CollectionName);
    		
    		String uuid 	= img.getUuid();
    		String fileName = img.getImageName();
    		
    		// Delete an image in DB
    		dm.delete(uuid, dm.collection);
    		
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

    	} catch (Exception e) {
    		String uuid 	= img.getUuid();
    		String fileName = img.getImageName();
    		
    		// Organize return info
        	PhotoPayload pp = PhotoPayload.newBuilder()
        								  .setUuid(uuid).setName(fileName)
        								  .build();
        	Payload p = Payload.newBuilder().setPhotoPayload(pp).build();

        	PhotoHeader ph = PhotoHeader.newBuilder()
        								.setResponseFlag(ResponseFlag.failure)
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
    	
    	
    }
	
	

    
    /**
	 *  Info Inserting Method
	 */
    //store Binary data to mongoDB
    public void insert(String uuid, String originalName, String client, 
    				   String uploadTime, String storedName, String category, 
    				   ByteString imageData, 
    				   DBCollection collection)
    {
    	 byte b[] = imageData.toByteArray();
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
    	byte[] b= (byte[])obj.get("Image Data");
    	retrievedImageData = ByteString.copyFrom(b);
		
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
	
	
	
	
	/**
	 * @param metaSocket is the socket that will store the meta data
	 * @param img
	 * @throws UnknownHostException
	 */
	public static void storeImageMetaData(Socket metaSocket,Image img) throws UnknownHostException{
		
		
		System.out.println("Stored MetaData to MongoDB at "+ metaSocket.getIp() +":" +metaSocket.getPort());
		
		
		String mongohost=metaSocket.getIp();
		int mongoport=metaSocket.getPort();
		Mongo mongo=new Mongo(mongohost, mongoport);
		DB db=mongo.getDB("275db");
		DBCollection collection=db.getCollection("Meta");
		
		String storeip=img.getStoreSocket().getIp();
		int storeport= img.getStoreSocket().getPort();
		BasicDBObject o=new BasicDBObject("storeip", storeip)
						.append("storeport", storeport)
						.append("uuid", img.getUuid())
						.append("name",img.getImageName());
		collection.insert(o);
		
		
		
	}
	
	/**
	 * @param metaSocket is the socket that  stored the meta data
	 * @param img
	 * @throws UnknownHostException 
	 */
	public static void deleteImageMetaData(Socket metaSocket,Image img) throws UnknownHostException{

		String mongohost=metaSocket.getIp();
		int mongoport=metaSocket.getPort();
		Mongo mongo=new Mongo(mongohost, mongoport);
		DB db=mongo.getDB("275db");
		DBCollection collection=db.getCollection("Meta");
		
		WriteResult result = collection.remove(new BasicDBObject("Uuid", img.getUuid() ));
		
	    System.out.println("Image of " + img.getUuid()+" deleted with " + result.getN() + " records");
	}
	
}


	
