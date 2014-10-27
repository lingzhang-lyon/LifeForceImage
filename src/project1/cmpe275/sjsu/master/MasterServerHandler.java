package project1.cmpe275.sjsu.master;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.IncompatibleDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import project1.cmpe275.sjsu.conf.Configure;
import project1.cmpe275.sjsu.model.Image;
import project1.cmpe275.sjsu.model.Socket;

/**
 * 
 *
 */
/**
 * @author lingzhang
 *
 */
public class MasterServerHandler extends SimpleChannelInboundHandler<HttpObject>{
	
	
	private static final String desPath=Configure.desPath;
	private static final Logger logger = Logger.getLogger(MasterServerHandler.class.getName());
	private HttpRequest request;
	private final StringBuilder responseContent = new StringBuilder();
	private HttpPostRequestDecoder decoder;
	private static final HttpDataFactory factory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk if size exceed
	
	//private static Image image=new Image("fooUser","fooPic.jpeg","fooCat",null);
	private static Image image=new Image();
	
 	@Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        if (decoder != null) {
            decoder.cleanFiles();
        }
    }
 	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	     logger.log(Level.WARNING, responseContent.toString(), cause);
	     ctx.channel().close();
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		if (msg instanceof HttpRequest) {
	        HttpRequest request = this.request = (HttpRequest) msg;
	        URI uri = new URI(request.getUri());
	        
	        //if not a form request just write the menu back to the channel
		    if (!uri.getPath().startsWith("/form")) {
		        // Write Menu
		            writeMenu(ctx);
		            return;
		        }
		    //if it's a form request
		    else {
		    	responseContent.setLength(0);
		    	responseContent.append("It's a Form reuest, REQUEST_URI: " + request.getUri() + "\r\n\r\n");

//		    	if (uri.getPath().equalsIgnoreCase("/formget") ) {		    		
//		    		handleFormGetRequest(ctx);			        
//		    	}
		    	if (request.getMethod().equals(HttpMethod.GET) ) {		    		
		    		handleFormGetRequest(ctx);			        
		    	}
		    	
		    	else if (uri.getPath().equalsIgnoreCase("/formpost") ) {		    		
		    		//TODO
		    		//handleFormPostRequest(ctx); 
		    		//will construct the decoder,
		    		//which prepare for reading more HttpContent msg 		    		
		    	}
		    	
		    	else if (uri.getPath().equalsIgnoreCase("/formpostmultipart") ) {		    		
		    		handleFormPostMultipartRequest(ctx); //will construct the decoder,
		    		//which prepare for reading more HttpContent msg 		    		
		    	}
		    				  
		    }		    		    
		}
		
		else if (msg instanceof HttpContent){
			if (decoder!=null){		
				responseContent.append("get more http content chunk\n");								
				HttpContent chunk = (HttpContent) msg;
				
				//put data chunk into decoder
				//once there is decode error, will close the channel				
				try{
					decoder.offer(chunk);
				}catch (ErrorDataDecoderException e1) {
                    e1.printStackTrace();
                    responseContent.append(e1.getMessage());
                    writeResponseBackToChannel(ctx);
                    ctx.channel().close();
                    return;
                }
				
		
				responseContent.append(" o ");	
				
				//read all the data from decoder and do what ever you want to treat the data!!!!!!!!!!!
				// add information to responseContent
				// store the attributes and file to image object!!!!!				
				readHttpDataChunkByChunk(); 
				
				//if the data is the last part, write response to the channel
				// or we can send the image to slave server!!!!!!!!!!!!!!!!!!
				if (chunk instanceof LastHttpContent) {
					
					
				    writeResponseBackToChannel(ctx);
				    
				    //save image meta data information to master server database
				    saveImageInfoToLocalDatabase(image); 
				    //may be we can use another channel to receive feedback from slave
				    //then save the meta data to master database???????
				    
				    //find proper slave socket according to our replication and partition algorithm
				 	ArrayList<Socket> sockets = findProperSlave(image);
				 	
				 	//send proper format of pacakage to slave
				 	sendMessageToSlave(sockets, image);	
				 	
				     reset();
				     
				}
			}else { 
				responseContent.append("decoder not constructed");
				writeResponseBackToChannel(ctx);
			}
			
			
			
		}
		
	}
	
	
	
	//TODO 
	private void handleFormGetRequest(ChannelHandlerContext ctx) throws Exception{
		
		responseContent.append("It's a Form GET request. ");
		
		//part of this code is from HttpSnoopServerHandler
		 QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
         Map<String, List<String>> params = queryStringDecoder.parameters();
         Image img=new Image(); 
         img.setUri(new URI(request.getUri()) );  
         
         if (!params.isEmpty()) {
             for (Entry<String, List<String>> p: params.entrySet()) {
                 String key = p.getKey();
                 List<String> vals = p.getValue();
                 for (String val : vals) {
                	 responseContent.append("PARAM: ").append(key).append(" = ").append(val).append("\r\n");
                 }                 
             }
             responseContent.append("\r\n");
             img.setUuid(params.get("uuid").get(0));                       
             img.setUserName(params.get("userName").get(0));
             img.setImageName(params.get("pictureName").get(0));
             img.setCategory(params.get("category").get(0));
                         
         }

         ArrayList<Socket> sockets = findProperSlave(img);
         Image resultImg = sendMessageToSlave(sockets, img);
         
       
         if(resultImg!=null){
	         responseContent.append("The requested image with uuid "+img.getUuid() +" is found.");
	         responseContent.append("The UUID of the image is: "+resultImg.getUuid());
	         responseContent.append("The Image Name of the image is: "+resultImg.getImageName());
	         responseContent.append("The User of the image is: "+resultImg.getUserName());
	         responseContent.append("The Created Time of the image is: "+resultImg.getCreated());
	         
	         writeImageBackToChannel(ctx, resultImg);
         }else{
        	 responseContent.append("The requested image with uuid "+img.getUuid() +" is not found.");
         }
       
        
		responseContent.append(" \r\n\r\nEND OF GET CONTENT~~~~~~~~~~\r\n");
		writeResponseBackToChannel(ctx);
	
	}
	
	private void writeImageBackToChannel(ChannelHandlerContext ctx, Image img) {
		// TODO Auto-generated method stub
		
	}

	
	
	private void handleFormPostMultipartRequest(ChannelHandlerContext ctx){
		responseContent.append("It's a Form Multipart request. \r\n");
		
		//construct a decoder
		try {
            decoder = new HttpPostRequestDecoder(factory, request);
        } catch (ErrorDataDecoderException e1) {
            e1.printStackTrace();
            responseContent.append(e1.getMessage());
            writeResponseBackToChannel(ctx);
            ctx.channel().close();
            return;
        } catch (IncompatibleDataDecoderException e1) {
            // GET Method: should not try to create a HttpPostRequestDecoder
            // So OK but stop here
            responseContent.append(e1.getMessage());
            responseContent.append("\r\n\r\nEND OF GET CONTENT\r\n");
            writeResponseBackToChannel(ctx);
            return;
        }
		
				
	}

	private void saveImageInfoToLocalDatabase(Image img ){
		
		System.out.println("image file " + img.getImageName() +" was saved to local database");
		
		//TODO add more code to store metadata to local database
	}

	/**
	 * send proper format of pacakage to slave
	 * @param socket
	 * @param img
	 * 
	 */
	private Image sendMessageToSlave(ArrayList<Socket> sockets, Image img) {
		System.out.println("image file " + img.getImageName() +" was send to slave");
		// TODO Auto-generated method stub
		// will call MessageSender
		return null;
		
	}

	
	/**
	 * find proper slave socket according to our replication and partition algorithm
	 * @param img
	 * @return Socket
	 * 
	 */
	private ArrayList<Socket> findProperSlave(Image img) {
		// TODO Auto-generated method stub
		//will call SlaveFinder
		return null;
	}

	private void readHttpDataChunkByChunk() throws Exception {    
        try {
			while (decoder.hasNext()) {
			    InterfaceHttpData data = decoder.next();
			    if(data!=null)
					try {
						writeHttpData(data);
					} finally{
						data.release();
					}
				else {
			    	System.out.println("this data is empty");
			    }
			        
			 }
		} catch (EndOfDataDecoderException e1) {
			responseContent.append("\r\n\r\nEND OF CONTENT CHUNK BY CHUNK\r\n\r\n");
		}
	}
	
	        
	
	private void writeHttpData(InterfaceHttpData data)throws Exception{
		if (data.getHttpDataType() == HttpDataType.Attribute) {
			writeAttribute(data);
			writeAttributeToImage(data,image);
            
		}
		else if(data.getHttpDataType() == HttpDataType.FileUpload) {
						
			writeFile(data);
			writeFileToImage(data,image);

	
		            
		}		
		
	}
	
	
	private void writeAttributeToImage(InterfaceHttpData data, Image img) throws IOException {
		Attribute attribute = (Attribute) data;
		String value=attribute.getString();
		String name=attribute.getName();	
		if (name.equals("pictureName") ) img.setImageName(value);							
		if (name.equals("userName") ) img.setUserName(value);
		if (name.equals("category") ) img.setCategory(value);
		
	}

	private void writeAttribute(InterfaceHttpData data) throws IOException{
        Attribute attribute = (Attribute) data;
        String value=attribute.getString();
        responseContent.append("\r\nBODY Attribute: " + attribute.getHttpDataType().name() + ": "
                + attribute + "\r\n");        
       //print out the attribute
       System.out.println("Attribute:" + attribute.getName() +": " + value);
	}
	

	
	private void writeFileToImage(InterfaceHttpData data,Image img) {
		FileUpload fileUpload = (FileUpload) data;
	    if (fileUpload.isCompleted()) {
	    	try {
				File receivedfile =fileUpload.getFile();
				img.setFile(receivedfile);
	    	}catch (Exception e) {
				// TODO Auto-generated catch block
				//responseContent.append("file is empty");
			}
	    }
		
	}

	private void writeFile(InterfaceHttpData data){
		responseContent.append("\r\nBODY FileUpload: " + data.getHttpDataType().name() + ": " + data + "\r\n");
		FileUpload fileUpload = (FileUpload) data;
        if (fileUpload.isCompleted()) {
        	 fileUpload.isInMemory();            	 

        	 
        	 String fileName =image.getImageName(); // --works
             
			 String client = "sjsu";  // Upload client
			 String uploadTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());  // Time stamp
			 String storedName = client + uploadTime + fileName;  // Stored name
			 String destPath =desPath;
			 
			 File dest = new File(destPath + storedName);  
			 
			 //store file to destination		  
		    	try {
					File receivedfile =fileUpload.getFile();
					if(receivedfile!=null){
						dest.createNewFile();
						fileUpload.renameTo(dest);
						responseContent.append("file was saved to local");
					}

				    
				} catch (Exception e) {
					// TODO Auto-generated catch block
					responseContent.append("file is empty");
				}				    	    
        	 
        }
	}
	
	private void reset() {
        request = null;

        // destroy the decoder to release all resources
        decoder.destroy();
        decoder = null;
    }	
	
	
	private void writeResponseBackToChannel(ChannelHandlerContext ctx){
		ByteBuf buf = copiedBuffer(responseContent.toString(), CharsetUtil.UTF_8);
        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);					
        // Write the response.
        ctx.channel().writeAndFlush(response);
	}
	
    
	private void writeMenu(ChannelHandlerContext ctx) {
	        // print several HTML forms
	        // Convert the response content to a ChannelBuffer.
           MenuManager.createMenu(responseContent);
	
	        ByteBuf buf = copiedBuffer(responseContent.toString(), CharsetUtil.UTF_8);
	        // Build the response object.
	        FullHttpResponse response = new DefaultFullHttpResponse(
	                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
	
	        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
	        response.headers().set(CONTENT_LENGTH, buf.readableBytes());
	
	        // Write the response.
	        ctx.channel().writeAndFlush(response);
	  }
}
