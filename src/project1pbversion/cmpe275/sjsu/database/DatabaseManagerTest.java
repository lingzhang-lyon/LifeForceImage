package project1pbversion.cmpe275.sjsu.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.bson.types.Binary;

import project1.cmpe275.sjsu.conf.Configure;
import project1.cmpe275.sjsu.model.Image;
import project1.cmpe275.sjsu.model.Socket;
import project1pbversion.cmpe275.sjsu.client.Client;
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
		
		String uuid=Client.createUuid("testForFile", "ling");
		testForWrite(uuid, "127.0.0.1");
		
		testForRead(uuid, "127.0.0.1") ;
    	
    		
	}
	
	public static void testForRead(String uuid, String mongohost) throws Exception{
		//DatabaseManager dm = new DatabaseManager();
		DatabaseManagerV2 dm = new DatabaseManagerV2();
    	//dm.connectDatabase();
    	
    	Image img= new Image();
    	
    	img.setUuid(uuid);
    	Socket socket=new Socket(mongohost, 27017);    	
    	Request request=dm.downloadFromDB(socket,img);
    	MessageManager.handleResponse(request,true);
	}
	
	public static void testForWrite(String uuid, String mongohost) throws Exception{
		DatabaseManagerV2 dm = new DatabaseManagerV2();
    	//dm.connectDatabase();
    	
    	Image img= new Image();
    	img.setUuid(uuid);
    	File file= new File(System.getProperty("filePath", filePath));
        ByteString filedata=MessageManager.convertFileToByteString(file);
        img.setData(filedata);
    	
        Socket socket=new Socket(mongohost, 27017);    	
    	Request request=dm.uploadToDB2(socket,img);
    	MessageManager.handleResponse(request,true);
	}

	
	
}
