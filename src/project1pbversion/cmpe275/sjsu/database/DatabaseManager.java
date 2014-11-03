package project1pbversion.cmpe275.sjsu.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;

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

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 *  Database Operations Manager
 */
public class DatabaseManager {
	
	public static Mongo mongo;
	public static DB db;
	public static DBCollection collection;
//	public static Image image;
	
	static final String filePath = System.getProperty("file", Configure.clientFilePath);
	static final String MongoHost = Configure.MongoHost;
	static final int 	MongoPort = Configure.MongoPort;
	static final String DBName = Configure.DBName;
	static final String CollectionName = Configure.CollectionName;
	
	/**
	 *  Database Connecting Method
	 */
	public void connectDatabase()
	{
		try {
			// DB connection
        	mongo = new Mongo(MongoHost, MongoPort);  // Connect DB Server
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
    public Image downloadFromDB(Socket socket, Image img) 
    {
    	return null;
    	// Retrieve an image where name = storedName, save to local as destFileName
//    	String destFileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".jpg";
//    	retrieve(storedName, destFileName, collection);
//    
    	
//    	retrieve(String name, String filename, DBCollection collection)
    }

    /**
	 *  Image uploading Method
	 */
	public Request uploadToDB(ArrayList<Socket> sockets, Image img) {
		// return variable
		Request uploadResponseRequest = null;
		
		// Connect to DB
		DatabaseManager dm = new DatabaseManager();
		dm.connectDatabase();
		
		// Parse the info of image
		String 	uuid			= img.getUuid();
    	String 	fileName		= img.getImageName();
    	String 	client			= img.getUserName();
    	String 	uploadTime		= img.getCreated();
    	String 	storedName		= client + uploadTime + fileName;
    	String 	category		= img.getCategory();
    	File	fileData		= img.getFile();

    	// for test
    	System.out.println("uuid:" + uuid);
    	System.out.println("fileName:" + fileName);
    	System.out.println("client:" + client);
    	System.out.println("uploadTime:" + uploadTime);
    	System.out.println("storedName:" + storedName);
    	System.out.println("category:" + category);
    	
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

    	System.out.println("UUID in response to write request: "
	 			+ uploadResponseRequest.getBody().getPhotoPayload().getUuid());
    	
    	// Close DB
		
		return uploadResponseRequest;
	}

    /**
	 *  Info Inserting Method
	 */
    public void insert(String uuid, String originalName, String client, 
    				   String uploadTime, String storedName, String category, 
    				   File fileData, 
    				   DBCollection collection)
    {
        try
        {
            File imageFile = fileData;
            FileInputStream f = new FileInputStream(imageFile);
 
            byte b[] = new byte[f.available()];
            f.read(b);
 
            // data of image
            Binary data = new Binary(b);
            BasicDBObject o = new BasicDBObject();
            
            // prepare inserting statement
            o.append("Uuid", uuid)
             .append("Image Name", storedName)
             .append("Upload User", client)
             .append("Upload Time", uploadTime)
             .append("Category", category)
             .append("Image",data);
            
            collection.insert(o);
            
            System.out.println("Inserted record.");
 
            f.close();
 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
  	 *  Info Retrieving Method
  	 */
      public void retrieve(String name, String filename, DBCollection collection)
      {
          byte c[];
          try
          {
              DBObject obj = collection.findOne(new BasicDBObject("name", name));
              //String n = (String)obj.get("name");
              c = (byte[])obj.get("Image");
              FileOutputStream fout = new FileOutputStream(filename);
              fout.write(c);
              fout.flush();
              System.out.println("Photo of "+name+" retrieved and stored at "+filename);
              fout.close();
          }
          catch (IOException e) {
              e.printStackTrace();
          }
      }
}