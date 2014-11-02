package project1pbversion.cmpe275.sjsu.master;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import project1.cmpe275.sjsu.conf.Configure;
import project1.cmpe275.sjsu.database.DatabaseManagerTest;
import project1.cmpe275.sjsu.model.Image;
import project1.cmpe275.sjsu.model.Socket;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Header;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Payload;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.RequestType;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.ResponseFlag;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoPayload;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;

import com.google.protobuf.ByteString;


public class MasterServerHandler extends SimpleChannelInboundHandler<Request>{
	
	private static final boolean isTest =Configure.isTest;
	private static final String desPath=Configure.desPath;
	private static final Logger logger = Logger.getLogger(MasterServerHandler.class.getName());
	private final StringBuilder responseContent = new StringBuilder();
	
	
 	@Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    }
 	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	     logger.log(Level.WARNING, responseContent.toString(), cause);
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
		Socket socket = findProperSlaveForDownload(img);
         
		//TODO  uncomment after finish implementation of DatabaseManager
		//DatabaseManager dm = new DatabaseManager(); 
		
		//TODO just for test, need to be removed
		DatabaseManagerTest dm = new DatabaseManagerTest();       

         Request responseRequest=null;
         
         //TODO need to change the parameter for uploadToDB();	
         responseRequest = dm.downloadFromDB(socket, img);
          
          
          //TODO for test, need to be removed later
         if(isTest){              
 	         //create a test response message after uploaded to DB
 	         
 	         PhotoPayload pp=PhotoPayload.newBuilder()
 	        		 			.setUuid(uuid).setName("testReadPic")
 	        		 			.build();
 	         Payload p=Payload.newBuilder().setPhotoPayload(pp).build();
 	         
 	    	 PhotoHeader ph= PhotoHeader.newBuilder()
 	    			 		.setResponseFlag(ResponseFlag.success)
 	    			 		.setRequestType(RequestType.read)
 	    			 		.build();	         	      	       	    	 
 	    	 Header h=Header.newBuilder().setPhotoHeader(ph).build();
 	    
 	    	 
 	    	 responseRequest=Request.newBuilder()
 	    			 				.setHeader(h)
 	    			 				.setBody(p)
 	    			 				.build();
 	    	 
 	    	 System.out.println("UUID in response to read request: "
 	    			 			+ responseRequest.getBody().getPhotoPayload().getUuid());
     	 
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
		
		//TODO need to convert ByteString data back to image file 
		//the file is stored in local, is there any other way for zero copy?
		File file=createFileInLocal(picname,desPath);
		WriteByteStringToFile(data,file);

		Image img=new Image(); 
		img.setUuid(uuid);
		img.setFile(file);
		ArrayList<Socket> sockets = findProperSlaveForUpload(img);
         
		//TODO  uncomment after finish implementation of DatabaseManager
		//DatabaseManager dm = new DatabaseManager(); 
		
		//TODO just for test, need to be removed
		DatabaseManagerTest dm = new DatabaseManagerTest(); 
        
		Request responseRequest=null;
       
        //TODO need to change the parameter for uploadToDB();	
        responseRequest = dm.uploadToDB(sockets, img);
         
         
         //TODO for test, need to be removed later
        if(isTest){              
	         //create a test response message after uploaded to DB
	         
	         PhotoPayload pp=PhotoPayload.newBuilder()
	        		 			.setUuid(uuid).setName(picname)
	        		 			.build();
	         Payload p=Payload.newBuilder().setPhotoPayload(pp).build();
	         
	    	 PhotoHeader ph= PhotoHeader.newBuilder()
	    			 		.setResponseFlag(ResponseFlag.success)
	    			 		.setRequestType(RequestType.write)
	    			 		.build();	         	      	       	    	 
	    	 Header h=Header.newBuilder().setPhotoHeader(ph).build();
	    
	    	 
	    	 responseRequest=Request.newBuilder()
	    			 				.setHeader(h)
	    			 				.setBody(p)
	    			 				.build();
	    	 
	    	 System.out.println("UUID in response to write request: "
	    			 			+ responseRequest.getBody().getPhotoPayload().getUuid());
    	 
        }
    	// Write the response.
    	 ChannelFuture future=ctx.channel().writeAndFlush(responseRequest);
    	// Close the connection after the write operation is done.
    	 future.addListener(ChannelFutureListener.CLOSE);
		
		
		
	}

	private void handleDeleteRequest(ChannelHandlerContext ctx, Request req) {
		// TODO Auto-generated method stub
		
	}

	private Socket findProperSlaveForDownload(Image img) {
		// TODO Auto-generated method stub
		// return SlaveFinder.findProperSlaveForDownload(img);
		return null;
	}

	
	private ArrayList<Socket> findProperSlaveForUpload(Image img) {
		// TODO Auto-generated method stub
		//will call SlaveFinder
		return null;
	}

	private void saveImageInfoToLocalDatabase(Image img ){
		
		//System.out.println("image information " + img.getImageName() +" was saved to local database");
		
		//TODO add more code to store metadata to local database
	}

	private File createFileInLocal(String picname, String path) {
		 String uploadTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());  // Time stamp
		 String storedName = uploadTime  +"_"+ picname;  
		 File dest = new File(path + storedName); 
		 return dest;
	}

	@SuppressWarnings("resource")
	private void WriteByteStringToFile(ByteString data, File file) throws Exception {
		// TODO Auto-generated method stub
		byte c[]=data.toByteArray();
		FileOutputStream fout = new FileOutputStream(file);
        fout.write(c);
        fout.flush();
		
	}

    

}
