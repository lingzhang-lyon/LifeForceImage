package project1.cmpe275.sjsu.slave;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
 
import org.bson.types.Binary;
 
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import project1.cmpe275.sjsu.model.Image;

public class DatabaseManager {
	
	public static Mongo mongo;
	public static DB db;
	public static DBCollection collection;
//	public static Image image;
	
	// Connect Database
	public void connectDatabase()
	{
		try {
			// DB connection
        	mongo = new Mongo("localhost", 27017);  // Connect DB Server
            db = mongo.getDB("275db");  // Connect DB
            collection = db.getCollection("Images_Collection_New");  // Connect collection
		} catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        } 
	}

	// upload to database
    public void uploadToDB(Image image) 
    {    	
    	// File properties
//    	String fileName = "image_1.jpg";  // Original name in local file system
//    	String client = "sjsu";  // Upload client
//    	String uploadTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());  // Time stamp
//    	String storedName = client + uploadTime + fileName;  // Stored name
//    	String category = "default";  //Category of image
    	
    	String uuid = image.getUuid();
    	String fileName = image.getImageName();
    	String client = image.getUserName();
    	String uploadTime = image.getCreated();
    	String storedName = client + uploadTime + fileName;
    	String category = image.getCategory();

    	// Insert an image
    	insert(uuid, fileName, client, uploadTime, storedName, category, collection);
    }
    
    public void downloadFromDB() 
    {
    	// Retrieve an image where name = storedName, save to local as destFileName
//    	String destFileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".jpg";
//    	retrieve(storedName, destFileName, collection);
//    
    	
//    	retrieve(String name, String filename, DBCollection collection)
    }
    
    // insert data to database
    public void insert(String uuid, String originalName, String client, String uploadTime, String storedName, String category, DBCollection collection)
    {
        try
        {
            File imageFile = new File(originalName);
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
    
    // download from database
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
