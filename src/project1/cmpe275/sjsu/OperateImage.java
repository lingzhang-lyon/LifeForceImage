package project1.cmpe275.sjsu;

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
  
/*
 * Java MongoDB : Save image example
 */
  
public class OperateImage {
 
    public static void main(String[] args) 
    {
        OperateImage o = new OperateImage();
                 
        /* Stores image in a collection */
        o.withoutUsingGridFS();
    }
 
    void withoutUsingGridFS() 
    {
        try {
  
            // DB connection
        	Mongo mongo = new Mongo("localhost", 27017);  // Connect DB Server
            DB db = mongo.getDB("275db");  // Connect DB
            DBCollection collection = db.getCollection("ImagesCollection");  // Connect collection
            
            // File properties
            String fileName = "image_1.jpg";  // Original name in local file system
            String client = "sjsu";  // Upload client
            String uploadTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());  // Time stamp
            String storedName = client + uploadTime + fileName;  // Stored name
             
            // Insert an image
            insert(fileName, client, uploadTime, storedName, collection);
             
            // Retrieve an image where name = storedName, save to local as destFileName
            String destFileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".jpg";
            retrieve(storedName, destFileName, collection);
             
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        } 
    }
     
    void insert(String originalName, String client, String uploadTime, String storedName, DBCollection collection)
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
            o.append("name", storedName)
             .append("user", client)
             .append("time", uploadTime)
             .append("category", "default")
             .append("photo",data);
            
            collection.insert(o);
            
            System.out.println("Inserted record.");
 
            f.close();
 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
     
    void retrieve(String name, String filename, DBCollection collection)
    {
        byte c[];
        try
        {
            DBObject obj = collection.findOne(new BasicDBObject("name", name));
            //String n = (String)obj.get("name");
            c = (byte[])obj.get("photo");
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