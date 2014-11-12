package project1pbversion.cmpe275.sjsu.partionAndReplication;



import java.net.UnknownHostException;
import java.util.ArrayList;

import project1pbversion.cmpe275.sjsu.database.DatabaseManagerV2;
import project1pbversion.cmpe275.sjsu.master.primary.PrimaryMasterServerHandler;
import project1pbversion.cmpe275.sjsu.master.primary.listenbackup.PrimaryListener;
import project1pbversion.cmpe275.sjsu.model.Image;
import project1pbversion.cmpe275.sjsu.model.Socket;
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

		SlaveFinderV2 sla = new SlaveFinderV2();
		Socket storeImageSocket= sla.findSlave2();
					
		System.out.println("used partition, will store to: " +storeImageSocket.getIp());
		
		Request req1= DatabaseManagerV2.uploadToDB(storeImageSocket,image);
		
		//store image meta data
		if(req1.getHeader().getPhotoHeader().getResponseFlag() == ResponseFlag.success){
			
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
	
	public Request download(Image image) throws UnknownHostException{

		SlaveFinderV2 sla = new SlaveFinderV2();
		Socket storesocket=sla.searchImageStoreSocket(image.getUuid());
		
		System.out.println("used partition, found the storesocket: " +storesocket.getIp());

		Request req1=DatabaseManagerV2.downloadFromDB(storesocket, image);
				
        
        return req1;
	        
		
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

	
	
	

}
