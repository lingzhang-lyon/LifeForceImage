package project1pbversion.cmpe275.sjsu.master.primary;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.TimeoutException;

import java.io.File;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import project1.cmpe275.sjsu.conf.Configure;
import project1.cmpe275.sjsu.model.Image;
import project1.cmpe275.sjsu.model.Socket;
import project1.cmpe275.sjsu.partionAndReplication.PartitionManager;
import project1pbversion.cmpe275.sjsu.database.DatabaseManager;
import project1pbversion.cmpe275.sjsu.database.DatabaseManagerV2;
import project1pbversion.cmpe275.sjsu.master.primary.listenbackup.PrimaryListener;
import project1pbversion.cmpe275.sjsu.othercluster.OtherClusterManager;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Header;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.RequestType;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.ResponseFlag;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;
import project1pbversion.cmpe275.sjsu.protobuf.MessageManager;

import com.google.protobuf.ByteString;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.WriteResult;


public class PrimaryMasterServerHandler extends SimpleChannelInboundHandler<Request>{
	
	private static final String desPath=Configure.desPath;
	private static final Logger logger = Logger.getLogger(PrimaryMasterServerHandler.class.getName());
	private static boolean saveToLocal=false;
	private static boolean usePartition=false;
	private static boolean passFailedRequestToOtherCluster=false;
	private static boolean dummyTestForMasterHandler=false;
	private static Socket backupMongoSocket=new Socket("127.0.0.1", 27017);
	
	public static boolean isSaveToLocal() {
		return saveToLocal;
	}


	public static void setSaveToLocal(boolean saveToLocal) {
		PrimaryMasterServerHandler.saveToLocal = saveToLocal;
	}



	public static boolean isDummyTestForMasterHandle() {
		return dummyTestForMasterHandler;
	}



	public static void setDummyTestForMasterHandle(boolean dummyTestForMasterHandle) {
		PrimaryMasterServerHandler.dummyTestForMasterHandler = dummyTestForMasterHandle;
	}



	public static boolean isPassFailedRequestToOtherCluster() {
		return passFailedRequestToOtherCluster;
	}



	public static void setPassFailedRequestToOtherCluster(
			boolean passFailedRequestToOtherCluster) {
		PrimaryMasterServerHandler.passFailedRequestToOtherCluster = passFailedRequestToOtherCluster;
	}

	
	
 	public static boolean isUsePartition() {
		return usePartition;
	}



	public static void setUsePartition(boolean usePartition) {
		PrimaryMasterServerHandler.usePartition = usePartition;
	}



	public static Socket getBackupMongoSocket() {
		return backupMongoSocket;
	}


	public static void setBackupMongoSocket(Socket backupMongoSocket) {
		PrimaryMasterServerHandler.backupMongoSocket = backupMongoSocket;
	}


	@Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    }
 	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	     logger.log(Level.WARNING,"Something wrong with server:" , cause);
	     ctx.channel().close();
	}
	
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
    
	@Override
	public void channelRead0(ChannelHandlerContext ctx, Request req) throws Exception {
		RequestType type =req.getHeader().getPhotoHeader().getRequestType();
		if(type.equals(RequestType.read)){
		    handleReadRequest(ctx, req);
		}
		else if (type.equals(RequestType.write)){
			handleWriteRequest(ctx,req);
		}
		else if (type.equals(RequestType.delete)){
			handleDeleteRequest(ctx,req);
		}
		
		
	}


	private void handleReadRequest(ChannelHandlerContext ctx, Request req) throws Exception{
		

		String uuid=req.getBody().getPhotoPayload().getUuid();		
		System.out.println("received read request for picture with UUID:"+uuid);

		Image img=new Image(); 
		img.setUuid(uuid);
		 Request responseRequest=null;
		 
         // for test, didn't interact with DB, just return failure response
        if(dummyTestForMasterHandler){ 
        	responseRequest=MessageManager.createResponseRequest(img,ResponseFlag.failure,RequestType.read);
  	 
	    	 System.out.println("Just a dummy test for master handler: ResponseFlag: "
	    			 			+ responseRequest.getHeader().getPhotoHeader().getResponseFlag());
    	 
        }
        else{
		 
			 if(usePartition){//use partition manager
				   PartitionManager pm=new PartitionManager();		 		 
				  responseRequest = pm.download(img);
		    
				 //pm.download(img) should be able to find where the image is, 
				 //and download from slave DB
				 //return a responseRequest, which contain all the image infomation 
			 }
			 else{ //don't use partition manager         
				 Socket socket = new Socket("127.0.0.1", 27017);
				 responseRequest = DatabaseManagerV2.downloadFromDB(socket, img);
				 System.out.println("UUID in response to read request: "
				 + responseRequest.getBody().getPhotoPayload().getUuid());
				 
			
				 
			 }
			 
			 
			 if(passFailedRequestToOtherCluster){
			 //if could not find in our cluster, pass the request to other cluster!!!!!!!!!!!!
			 //need to set timeout, if after long time still don't get success, will return failure to client!!!!!
				 if(responseRequest.getHeader().getPhotoHeader().equals(ResponseFlag.failure)){
					 try{
						 responseRequest=passRequestToOtherCluster(req);
					 }catch(TimeoutException e){
						 //if timeout from the channel that send and receive msg from other cluster
						 System.out.println("timeout: didn't received feedback from other cluster ");
					 }
				 }
			 }
		 		 
        } 

		 
     	// Write the response.
     	 ChannelFuture future=ctx.channel().writeAndFlush(responseRequest);
     	// Close the connection after the write operation is done.
     	 future.addListener(ChannelFutureListener.CLOSE);
    	 
	}
	


	private void handleWriteRequest(ChannelHandlerContext ctx, Request req) throws Exception {

		String uuid=req.getBody().getPhotoPayload().getUuid();		
		System.out.println("received write request for picture with UUID:"+uuid);
		String picname=req.getBody().getPhotoPayload().getName();		
		System.out.println("received write request for picture with new name:"+picname);

		ByteString data=req.getBody().getPhotoPayload().getData();
		System.out.println("received write request for picture with new data:"+data.toString());
		
		//if choose to save to master local file system
		if(saveToLocal){
			File file=MessageManager.createFileWithThread(picname,desPath);
			MessageManager.writeByteStringToFile(data,file);
			System.out.println("received file data with uuid:<"+uuid+ ">was saved in master local file system at \n" + desPath);
		}


		Image img=new Image();
		img.setUuid(uuid);
		img.setImageName(picname);
		img.setData(data);
		       
		Request responseRequest=null;
		
       if(dummyTestForMasterHandler){              
	         //create a test response message after uploaded to DB
       	responseRequest=MessageManager.createResponseRequest(img,ResponseFlag.success,RequestType.write);
       	
	    	 System.out.println("UUID in response to write request: "
	    			 			+ responseRequest.getBody().getPhotoPayload().getUuid());
   	 
       }
       else{ 
		
    	   if(usePartition){//use partition manager 
			  PartitionManager pm=new PartitionManager();		 		 
			  responseRequest= pm.upload(img);
			//TODO pm.upload(img) should be able to find proper socket, 
			 //and upload to slave DB
			 //return a responseRequest, which contain success or failure information
    	   }
    	   else{ //don't use partition manager 
    		   	Socket socket = new Socket("127.0.0.1", 27017);
				responseRequest = DatabaseManagerV2.uploadToDB(socket, img);
				System.out.println("UUID in response to write request: "
			 			+ responseRequest.getBody().getPhotoPayload().getUuid()); 
				
				if (responseRequest.getHeader().getPhotoHeader().getResponseFlag().equals(ResponseFlag.success) ){
					
					//TODO just for test, need to put into PartitionManager
					 //write image metadata to local DB
					Socket storeImageSocket =new Socket("127.0.0.1",27017);
					img.setStoreSocket(storeImageSocket);
					
					Socket localMetaSocket =new Socket("127.0.0.1",27017);
					storeImageMetaData(localMetaSocket, img);
					
					//send image meta data to backup master
					if(PrimaryListener.isBackupMasterConnected()){
						  Socket backupMetaSocket= backupMongoSocket;
						  storeImageMetaData(backupMetaSocket, img);
					}
				}
				
    	   }
    	   
		  if(passFailedRequestToOtherCluster){
			 //if could not find in our cluster, pass the request to other cluster!!!!!!!!!!!!
			 //need to set timeout, if after long time still don't get success, will return failure to client!!!!!
				 if(responseRequest.getHeader().getPhotoHeader().equals(ResponseFlag.failure)){
					 try{
						 responseRequest=passRequestToOtherCluster(req);
					 }catch(TimeoutException e){
						 //if timeout from the channel that send and receive msg from other cluster
						 System.out.println("timeout: didn't received feedback from other cluster ");
					 }
				 }
		   }
		  

    	   
    	   
       }

    	// Write the response.
    	 ChannelFuture future=ctx.channel().writeAndFlush(responseRequest);
    	// Close the connection after the write operation is done.
    	 future.addListener(ChannelFutureListener.CLOSE);
		
		
		
	}

	private void handleDeleteRequest(ChannelHandlerContext ctx, Request req) throws Exception {
		String uuid=req.getBody().getPhotoPayload().getUuid();		
		System.out.println("received delete request for picture with UUID:"+uuid);
		
		Request responseRequest=null;
		Image img=new Image(); 
		img.setUuid(uuid);
		
		
		if(dummyTestForMasterHandler){              
	         //create a test response message after uploaded to DB
			responseRequest=MessageManager.createResponseRequest(img,ResponseFlag.success,RequestType.delete);
      	
	    	 System.out.println("UUID in response to delete request: "
	    			 			+ responseRequest.getBody().getPhotoPayload().getUuid());
  	 
		}
		else{
			 if(usePartition){//use partition manager 
				  PartitionManager pm=new PartitionManager();		 		 
				  responseRequest= pm.delete(img);
				//TODO pm.upload(img) should be able to find proper socket, 
				 //and upload to slave DB
				 //return a responseRequest, which contain success or failure information
		   	   }
		   	   else{ //don't use partition manager 
						Socket socket = new Socket("127.0.0.1", 27017);
						responseRequest = DatabaseManagerV2.deleteInDB(socket, img);
						System.out.println("UUID in response to delete request: "
					 			+ responseRequest.getBody().getPhotoPayload().getUuid()); 
						
						if (responseRequest.getHeader().getPhotoHeader().getResponseFlag().equals(ResponseFlag.success) ){
							//TODO just for test, need to put into PartitionManager
							// delete image metadata from local DB
							Socket localMetaSocket =new Socket("127.0.0.1",27017);
							deleteImageMetaData(localMetaSocket, img);
							
							//delete image meta data to backup master
							if(PrimaryListener.isBackupMasterConnected()){
								  Socket backupMetaSocket= PrimaryListener.getBackupMasterSocket();
								  deleteImageMetaData(backupMetaSocket, img);
							}
						}
		   	   }
			 
			  if(passFailedRequestToOtherCluster){
				 //if could not find in our cluster, pass the request to other cluster!!!!!!!!!!!!
				 //need to set timeout, if after long time still don't get success, will return failure to client!!!!!
					 if(responseRequest.getHeader().getPhotoHeader().equals(ResponseFlag.failure)){
						 try{
							 responseRequest=passRequestToOtherCluster(req);
						 }catch(TimeoutException e){
							 //if timeout from the channel that send and receive msg from other cluster
							 System.out.println("timeout: didn't received feedback from other cluster ");
						 }
					 }
			  }
			 
			 
		}
		
		
		// Write the response.
	   	 ChannelFuture future=ctx.channel().writeAndFlush(responseRequest);
	   	// Close the connection after the write operation is done.
	   	 future.addListener(ChannelFutureListener.CLOSE);
		
		
	}


	private Request passRequestToOtherCluster( Request req) throws Exception{
		Socket otherClusterSocket=new Socket("127.0.0.1", 8080); //need to find other socket!!!!
		 
		 //create a new Request for passing, add originator information in the request
		 Request.Builder builder=Request.newBuilder(req);
		 builder.setHeader(Header.newBuilder(req.getHeader()).setOriginator(1));
		 Request passRequest=builder.build();
		 
		 Request resRequest=OtherClusterManager.createChannelAndSendRequest(otherClusterSocket, passRequest);
		 return resRequest;
		 
	}
	
	/**
	 * @param metaSocket is the socket that will store the meta data
	 * @param img
	 * @throws UnknownHostException
	 */
	private void storeImageMetaData(Socket metaSocket,Image img) throws UnknownHostException{
		
		
		System.out.println("Stored MetaData to MongoDB at "+ metaSocket.getIp() +":" +metaSocket.getPort());
		
		
		String mongohost=metaSocket.getIp();
		int mongoport=metaSocket.getPort();
		Mongo mongo=new Mongo(mongohost, mongoport);
		DB db=mongo.getDB("275db");
		DBCollection collection=db.getCollection("Meta");
		
		String store=img.getStoreSocket().getIp() +":"+ img.getStoreSocket().getPort();
		BasicDBObject o=new BasicDBObject("socket", store)
						.append("uuid", img.getUuid())
						.append("name",img.getImageName());
		collection.insert(o);
		
		
		
	}
	
	/**
	 * @param metaSocket is the socket that  stored the meta data
	 * @param img
	 * @throws UnknownHostException 
	 */
	private void deleteImageMetaData(Socket metaSocket,Image img) throws UnknownHostException{

		String mongohost=metaSocket.getIp();
		int mongoport=metaSocket.getPort();
		Mongo mongo=new Mongo(mongohost, mongoport);
		DB db=mongo.getDB("275db");
		DBCollection collection=db.getCollection("Meta");
		
		WriteResult result = collection.remove(new BasicDBObject("Uuid", img.getUuid() ));
		
	    System.out.println("Image of " + img.getUuid()+" deleted with " + result.getN() + " records");
	}
	

	
    

}
