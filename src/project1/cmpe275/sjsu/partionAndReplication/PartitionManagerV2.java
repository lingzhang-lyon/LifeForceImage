package project1.cmpe275.sjsu.partionAndReplication;



import java.net.UnknownHostException;
import java.util.ArrayList;

import project1.cmpe275.sjsu.model.Image;
import project1.cmpe275.sjsu.model.Socket;
import project1pbversion.cmpe275.sjsu.database.DatabaseManagerV2;
import project1pbversion.cmpe275.sjsu.master.primary.PrimaryMasterServerHandler;
import project1pbversion.cmpe275.sjsu.master.primary.listenbackup.PrimaryListener;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.RequestType;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.ResponseFlag;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;
import project1pbversion.cmpe275.sjsu.protobuf.MessageManager;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;


public class PartitionManagerV2 {

	public Request upload(Image image) throws UnknownHostException{

		ArrayList<String> soc = new ArrayList<String>();
		SlaveFinderV2 sla = new SlaveFinderV2();
		soc= sla.FindSlave();
					
		ArrayList<Socket> socket=this.trans(soc);
		System.out.println(socket.get(0).getIp());
		
		Request req1= DatabaseManagerV2.uploadToDB(socket.get(0),image);
		
		//store image meta data
		if(req1.getHeader().getPhotoHeader().getResponseFlag() == ResponseFlag.success){
			
			//Socket storeImageSocket =new Socket("127.0.0.1",27017);
			Socket storeImageSocket =socket.get(0);
			image.setStoreSocket(storeImageSocket);
			
			Socket localMetaSocket =new Socket("127.0.0.1",27017);
			DatabaseManagerV2.storeImageMetaData(localMetaSocket, image);
			
			//send image meta data to backup master
			if(PrimaryMasterServerHandler.isUseBackupMaster() 
					&& PrimaryListener.isBackupMasterConnected()){
				System.out.println("also store image meata data to backup");
				  Socket backupMetaSocket= PrimaryMasterServerHandler.getBackupMongoSocket();
				  DatabaseManagerV2.storeImageMetaData(backupMetaSocket, image);
			}
			else{
				System.out.println("No backup meta data");
			}
		}
		
		return req1;
		
	}
	
	public Request download(Image image){
//		try {
//			Mongo mongo = new Mongo("localhost", 27017);
//			DB db = mongo.getDB("275db");  // Connect DB
//	        DBCollection collection = db.getCollection("Meta");  // Connect collectionClass
//	        
//	        DBCursor obj = collection.find(new BasicDBObject("uuid", image.getUuid()));
//	        
//	        
//	        while(obj.hasNext()){
//			    soc_str.add((obj.next().get("socket").toString()));					   
//		   }
//		     obj.close();
//	        
//		
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}  // Connect DB Server
		

			Socket storesocket=new Socket("127.0.0.1", 27017);
			Socket repicatestoresocket= new Socket("127.0.0.1", 27017);
			Request req1=DatabaseManagerV2.downloadFromDB(storesocket, image);
					
	        
	        if(req1.getHeader().getPhotoHeader().getResponseFlag()== ResponseFlag.success){
	        	return req1;
	        }
	        else {
	        	Request req2=DatabaseManagerV2.downloadFromDB(repicatestoresocket, image);
	        	return	req2;
	        }
	        
		
	}
	
	public Request delete(Image image) throws Exception{
		
		Socket localMetaSocket =new Socket("127.0.0.1",27017);
		DatabaseManagerV2.deleteImageMetaData(localMetaSocket, image);
		
		//delete image meta data to backup master
		if(PrimaryListener.isBackupMasterConnected()){
			  Socket backupMetaSocket= PrimaryListener.getBackupMasterSocket();
			  DatabaseManagerV2.deleteImageMetaData(backupMetaSocket, image);
		} 
		return MessageManager.createResponseRequest(image, ResponseFlag.success, RequestType.delete);
	}

	private ArrayList<Socket> trans(ArrayList<String> soc){
		Socket s1= new Socket();
		Socket s2= new Socket();
		ArrayList<Socket> socket = new ArrayList<Socket>();
		s1.setIp(soc.get(0).split(":")[0]);
		s1.setPort(Integer.parseInt(soc.get(0).split(":")[1]));
		socket.add(s1);
		s2.setIp(soc.get(1).split(":")[0]);
		s2.setPort(Integer.parseInt(soc.get(1).split(":")[1]));
		socket.add(s2);
		
		return socket;
	}
	/*
	public static void main(String[] args){
		PartitionManager pm1= new PartitionManager();
	
		pm1.upload(im);
		
	}*/

	
	
	

}
