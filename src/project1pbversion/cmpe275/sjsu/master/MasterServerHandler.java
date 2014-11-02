package project1pbversion.cmpe275.sjsu.master;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import project1.cmpe275.sjsu.conf.Configure;
import project1.cmpe275.sjsu.database.DatabaseManager;
import project1.cmpe275.sjsu.model.Image;
import project1.cmpe275.sjsu.model.Socket;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Header;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Header.RequestType;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Header.ResponseFlag;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Payload;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;


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
		RequestType type =req.getHeader().getRequestType();
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


	private void handleReadRequest(ChannelHandlerContext ctx, Request req) {
		

		String uuid=req.getBody().getUuid();		
		System.out.println("received read request for picture with UUID:"+uuid);

		Image img=new Image(); 
		img.setUuid(uuid);
		Socket socket = findProperSlaveForDownload(img);
         
        DatabaseManager dm = new DatabaseManager();        
        Image resultImg=null; 
        try{
         //TODO need to change the parameter for downloadFromDB();	
         resultImg = dm.downloadFromDB(socket, img);
         
        } catch (Exception e){
        	responseContent.append("Sorry, the search process met some problem");
        }
        
         //TODO for test, need to be removed later
        if(isTest){
         resultImg = new Image("tester","testpic", "testcat", null);
         resultImg.setUuid(uuid);//set received uuid to result image
         System.out.println("creat a test image named: " +resultImg.getImageName());
         System.out.println("with a UUID: " +resultImg.getImageName());
         
        }
         //end of test code
       
         //create response message and write back to channel
         Request.Builder builder = Request.newBuilder();        
         Payload.Builder payloadbuilder = Payload.newBuilder();
         Header.Builder headerbuilder = Header.newBuilder();
         
         if(resultImg!=null){
        	 payloadbuilder.setUuid(resultImg.getUuid()).setName(resultImg.getImageName());
        	 headerbuilder.setResponseFlag(ResponseFlag.success);	         	      
         }else{
        	 headerbuilder.setResponseFlag(ResponseFlag.failure);
         }
    	 builder.setHeader(headerbuilder.build());
    	 builder.setBody(payloadbuilder.build());
    	 Request responseRequest=builder.build();
    	 System.out.println("UUID in response request: "+ responseRequest.getBody().getUuid());
    	 
 //   	 ctx.write(responseRequest);
    	 
    	// Write the response.
    	 ChannelFuture future=ctx.channel().writeAndFlush(responseRequest);
    	// Close the connection after the write operation is done.
    	 future.addListener(ChannelFutureListener.CLOSE);
    	 
	}
	

	private void handleWriteRequest(ChannelHandlerContext ctx, Request req) {
		// TODO Auto-generated method stub
		
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

    

}
