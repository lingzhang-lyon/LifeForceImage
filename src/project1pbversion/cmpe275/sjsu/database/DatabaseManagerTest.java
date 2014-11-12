package project1pbversion.cmpe275.sjsu.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.bson.types.Binary;

import project1pbversion.cmpe275.sjsu.client.Client;
import project1pbversion.cmpe275.sjsu.conf.Configure;
import project1pbversion.cmpe275.sjsu.model.Image;
import project1pbversion.cmpe275.sjsu.model.Socket;
import project1pbversion.cmpe275.sjsu.protobuf.MessageManager;
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
 *  Test for Database Operations Manager
 */
public class DatabaseManagerTest {
	
	public static Mongo mongo;
	public static DB db;
	public static DBCollection collection;
	
	static final String filePath = System.getProperty("file", Configure.clientFilePath);
	static final String MongoHost = Configure.MongoHost;
	static final int 	MongoPort = Configure.MongoPort;
	static final String DBName = Configure.DBName;
	static final String CollectionName = Configure.CollectionName;
	
	//for test
	public static void main(String[] args) throws Exception{
		
		String uuid=Client.createUuid("testForDBManger", "ling");
		String imageName="testForDBManger.jpeg";
		
		//testForWrite(uuid, imageName, "127.0.0.1");
		testForWrite(uuid, imageName, "192.168.1.3");
		
		//testForRead(uuid, "127.0.0.1") ;
		
		//testForWriteRemoteMongoDB(uuid, imageName, "192.168.1.3", 27017, "db275", "Images_Collection_BSD");
		//testForWriteRemoteMongoDB(uuid, imageName, "127.0.0.1", 27017, "db275", "Images_Collection_BSD");
    	//not work!!!!!!!!!!	
	}
	
	public static void testForRead(String uuid, String mongohost) throws Exception{
    	
    	Image img= new Image();
    	
    	img.setUuid(uuid);
    	Socket socket=new Socket(mongohost, 27017);    	
    	Request request=DatabaseManagerV2.downloadFromDB(socket,img);
    	MessageManager.handleResponse(request,true);
	}
	
	public static void testForWrite(String uuid, String imageName,String mongohost) throws Exception{
    	
    	Image img= new Image();
    	img.setUuid(uuid);
    	img.setImageName(imageName);
    	File file= new File(System.getProperty("filePath", filePath));
        ByteString filedata=MessageManager.convertFileToByteString(file);
        img.setData(filedata);
    	
        Socket socket=new Socket(mongohost, 27017);    	
    	Request request=DatabaseManagerV2.uploadToDB(socket,img);
    	MessageManager.handleResponse(request,true);
	}
	
	

	public static void testForWriteRemoteMongoDB(String uuid, String fileName, String mongohost, int mongoport, 
											String DBName, String collectionName) throws Exception{
		
		
		System.out.println("\nTest for write to remote MongoDB");
    	String 		client		= "dbtester";
    	String 		uploadTime	= "dbtest_time";
    	String 		storedName	= "dbtest_storeName";
    	String 		category	= "dbtest_category";
    	File file= new File(System.getProperty("filePath", filePath));
        ByteString fileData=MessageManager.convertFileToByteString(file);
    	
		DatabaseManagerV2 dm= new DatabaseManagerV2(mongohost, mongoport, DBName, collectionName);
		dm.insert(uuid, fileName, client, uploadTime, storedName, category, fileData, dm.getCollection());
		
		
	}
	
}
