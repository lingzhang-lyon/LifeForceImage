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
import project1.cmpe275.sjsu.master.MessageSender;
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
public class Client {
	

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
		 // setup the factory: here using a mixed memory/disk based on size threshold
        HttpDataFactory fact = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk if MINSIZE exceed
        
        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory		
		
        EventLoopGroup g = new NioEventLoopGroup();
 
        //Image image=new Image();
        
        try {
			Bootstrap b = new Bootstrap();
			b.group(g)
			 .channel(NioSocketChannel.class)
			 .handler(new ClientInitializer());
      
						
			URI uriSimple=new URI(baseURL + "formpost");
			File file = new File(filePath);
			if (!file.canRead()) {
			    throw new FileNotFoundException(filePath);
			}
			
			Image image=new Image(username, picname, category, file );
			
			MessageSender.createAndSendPostMessage(b, host, port,uriSimple, fact, image);
			
			//createAndSendPostMessage(bootstrap, HOST,PORT,uriSimple, file, factory, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			// Shut down executor threads to exit.
            g.shutdownGracefully();
            // Really clean all temporary files if they still exist
            fact.cleanAllHttpDatas();
		}
	}
	

	
	
	private void createGetMessage(){
		
	}
	
	
	
	
	
}
