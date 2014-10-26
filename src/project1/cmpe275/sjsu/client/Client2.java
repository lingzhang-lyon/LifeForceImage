package project1.cmpe275.sjsu.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.ClientCookieEncoder;
import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder.ErrorDataEncoderException;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.net.URI;

import project1.cmpe275.sjsu.conf.Configure;
import project1.cmpe275.sjsu.model.Image;

/**
 * 
 * the class for collect user's request and then form the message, and pass to MasterServer
 * Need initialized the Channel to connect to MasterServer
 * Message is http package
 * the Client should also be able to receive message sent from MasterServer
 * should print the information and show or save the image
 * 
 *
 */
public class Client2 {
	

	static final String HOST = Configure.HOST;
	static final int PORT = Configure.PORT;
	static final String BASE_URL = System.getProperty("baseUrl", Configure.BASE_URL);
    static final String FILE = System.getProperty("file", Configure.clientFilePath);
	static final String USERNAME =Configure.USERNAME;
	static final String PICNAME =Configure.PICNAME;
	static final String CAT =Configure.CAT;
	
	
	public static void main(String[] agrs) throws Exception{
		clientPost(HOST, PORT, BASE_URL, FILE, USERNAME,PICNAME,CAT);	
		
	}
	
	
	public static void clientPost(String host, int port, String baseURL, String filePath, String username, String picname, String category ){        
        try {
			//URI uriSimple=new URI(baseURL + "formpost");
			URI uriSimple=new URI(baseURL + "formpostmultipart");
			File file = new File(filePath);
			if (!file.canRead()) {
			    throw new FileNotFoundException(filePath);
			}
			
			Image image=new Image(username, picname, category, file );
			
			createAndSendPostMessage(host, port,uriSimple, image);
			
			//createAndSendPostMessage(bootstrap, HOST,PORT,uriSimple, file, factory, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	

	
	public static void createAndSendPostMessage( String host, int port, 
			URI uriSimple, Image image) throws Exception{
		
		 HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk if MINSIZE exceed
	        
	        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
	        DiskFileUpload.baseDirectory = null; // system temp directory
	        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
	        DiskAttribute.baseDirectory = null; // system temp directory		
			
	     EventLoopGroup group = new NioEventLoopGroup();
	     Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group)
			 .channel(NioSocketChannel.class)
			 .handler(new ClientInitializer());
		
		try{
			// Start the connection attempt.
	        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
	        // Wait until the connection attempt succeeds or fails.
	        Channel channel = future.sync().channel();
	        
	
	        // Prepare the HTTP request.
	        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uriSimple.toASCIIString());
	
	        //set headers
	        // it is legal to add directly header or cookie into the request until finalize
	        HttpHeaders headers = request.headers();
	        setHeaders(headers, host, uriSimple);
	        
	              
	        
	        // Use the PostBody encoder
	        HttpPostRequestEncoder bodyRequestEncoder =
	                new HttpPostRequestEncoder(factory, request, true);  // false => not multipart
	        		//new HttpPostRequestEncoder(factory, request, false);  // false => not multipart
	       
	        // add Form attribute
	        setFormAttributes(bodyRequestEncoder, image);
	        
	
	        bodyRequestEncoder.finalizeRequest();
	        
	        // send request
	        channel.write(request);
	
	        // test if request was chunked and if so, finish the write
	        if (bodyRequestEncoder.isChunked()) {
	            channel.write(bodyRequestEncoder);
	        }
	        channel.flush();
	
	        // On standard program, it is clearly recommended to clean all files after each request
	        //but here if we cleanfiles, it will delete the attribute values, so we don't do it here!!!!!!!!!!!!!
	        //bodyRequestEncoder.cleanFiles();
	
	        // Wait for the server to close the connection.
	        channel.closeFuture().sync();
        
		}finally{
			// Shut down executor threads to exit.
            group.shutdownGracefully();
            // Really clean all temporary files if they still exist
            factory.cleanAllHttpDatas();
		}
        		
	}
	
	private static void setHeaders(HttpHeaders headers, String host, URI uriSimple){
		headers.set(HttpHeaders.Names.HOST, host);
        headers.set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
        headers.set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP + ',' + HttpHeaders.Values.DEFLATE);

        headers.set(HttpHeaders.Names.ACCEPT_CHARSET, "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        headers.set(HttpHeaders.Names.ACCEPT_LANGUAGE, "fr");
        headers.set(HttpHeaders.Names.REFERER, uriSimple.toString());
        headers.set(HttpHeaders.Names.USER_AGENT, "Netty Simple Http Client side");
        headers.set(HttpHeaders.Names.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");       
 
        headers.set(
        			HttpHeaders.Names.COOKIE, 
        			ClientCookieEncoder.encode(new DefaultCookie("my-cookie", "foo"))
                    );
        
	}
	
	private static void setFormAttributes(HttpPostRequestEncoder bodyRequestEncoder, Image image ) throws ErrorDataEncoderException{
		//get information from image object and add to requestEncoder
        bodyRequestEncoder.addBodyAttribute("userName", image.getUserName());
        bodyRequestEncoder.addBodyAttribute("pictureName", image.getImageName());
        bodyRequestEncoder.addBodyAttribute("category", image.getCategory());
        bodyRequestEncoder.addBodyFileUpload("myfile", image.getFile(), "application/x-zip-compressed", false); // isText=false
	}
	
	private void createGetMessage(){
		
	}
	
	
	
	
	
}
