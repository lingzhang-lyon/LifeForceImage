package project1pbversion.cmpe275.sjsu.master;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import project1.cmpe275.sjsu.conf.Configure;
import project1.cmpe275.sjsu.model.Image;
import project1.cmpe275.sjsu.model.Socket;
import project1pbversion.cmpe275.sjsu.database.DatabaseManager;
import project1pbversion.cmpe275.sjsu.othercluster.OtherClusterManager;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.RequestType;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.ResponseFlag;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Header;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;
import project1pbversion.cmpe275.sjsu.protobuf.MessageManager;

import com.google.protobuf.ByteString;


public class MasterServerHandler extends SimpleChannelInboundHandler<Request>{
	
	private static final boolean isTest =Configure.isTest;
	private static final String desPath=Configure.desPath;
	private static final Logger logger = Logger.getLogger(MasterServerHandler.class.getName());
	private static boolean saveToLocal=false;
	
	
	public MasterServerHandler(boolean savetolocal){
		this.saveToLocal=savetolocal;
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
		
		 //TODO use partition manager	 
		 // PartitionManager pm=new PartitionManager();		 		 
		 // responseRequest = pm.download(img);
        //TODO 
		 //pm.download(img) should be able to find where the image is, 
		 //and download from slave DB
		 //return a responseRequest, which contain all the image infomation
         
		//TODO test for DatabaseManager , need to be removed later
		 Socket socket = null;
		 DatabaseManager dm = new DatabaseManager();
		 responseRequest = dm.downloadFromDB(socket, img);
		 System.out.println("UUID in response to read request: "
		 + responseRequest.getBody().getPhotoPayload().getUuid());
		 
		 
		 //TODO if could not find in our cluster, pass the request to other cluster!!!!!!!!!!!!
		 //need to set timeout, if after long time still don't get success, will return failure to client!!!!!
//		 if(responseRequest.getHeader().getPhotoHeader().equals(ResponseFlag.failure)){
//			 responseRequest=passRequestToOtherCluster(req);
//			 
//		 }
		 
          //TODO for test, need to be removed later
//         if(isTest){ 
//        	img.setImageName("testReadResponse.jpeg"); 
//         	responseRequest=MessageManager.createResponseRequest(img,ResponseFlag.success,RequestType.read);
//   	 
// 	    	 System.out.println("UUID in response to read request: "
// 	    			 			+ responseRequest.getBody().getPhotoPayload().getUuid());
//     	 
//         }
		 
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
		//TODO after refactor DBManager, to handle the byteString in image object, no need file in image object
		//we can choose to not save to local, now we need this for temperary transfer
		
		
		
		Image img=new Image();
//		img.setFile(file); //can be delete later after refactor DBManager
		img.setUuid(uuid);
		img.setImageName(picname);
		img.setData(data);
		       
		Request responseRequest=null;
		
		 //TODO use partition manager	 
		 // PartitionManager pm=new PartitionManager();		 		 
		 // responseRequest= pm.upload(img);
		//TODO pm.upload(img) should be able to find proper socket, 
		 //and upload to slave DB
		 //return a responseRequest, which contain success or failure information
		

		//TODO test for DatabaseManager , need to be removed later
		Socket socket = null;
		DatabaseManager dm = new DatabaseManager();
		responseRequest = dm.uploadToDB(socket, img);
		System.out.println("UUID in response to write request: "
	 			+ responseRequest.getBody().getPhotoPayload().getUuid()); 
		
         //TODO for test, need to be removed later
//        if(isTest){              
//	         //create a test response message after uploaded to DB
//        	responseRequest=MessageManager.createResponseRequest(img,ResponseFlag.success,RequestType.write);
//        	
//	    	 System.out.println("UUID in response to write request: "
//	    			 			+ responseRequest.getBody().getPhotoPayload().getUuid());
//    	 
//        }
    	// Write the response.
    	 ChannelFuture future=ctx.channel().writeAndFlush(responseRequest);
    	// Close the connection after the write operation is done.
    	 future.addListener(ChannelFutureListener.CLOSE);
		
		
		
	}

	private void handleDeleteRequest(ChannelHandlerContext ctx, Request req) throws Exception {
		// TODO Auto-generated method stub
		String uuid=req.getBody().getPhotoPayload().getUuid();		
		System.out.println("received delete request for picture with UUID:"+uuid);
		Request responseRequest=null;
		Image img=new Image(); 
		img.setUuid(uuid);
		
		 //TODO use partition manager	 
		 // PartitionManager pm=new PartitionManager();		 		 
		 // responseRequest= pm.delete(img);
		//TODO pm.delete(img) should be able to find proper socket, 
		 //and upload to slave DB
		 //return a responseRequest, which contain success or failure information
		
		//TODO for test, need to be removed later
      if(isTest){              
	         //create a test response message after uploaded to DB
      	responseRequest=MessageManager.createResponseRequest(img,ResponseFlag.success,RequestType.delete);
      	
	    	 System.out.println("UUID in response to delete request: "
	    			 			+ responseRequest.getBody().getPhotoPayload().getUuid());
  	 
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

	
    

}
