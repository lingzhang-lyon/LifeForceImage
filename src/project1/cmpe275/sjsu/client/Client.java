package project1.cmpe275.sjsu.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;

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
	
	/**
	 * Configure the client channel,  connect to master server
	 * @throws Exception
	 */
	public void initialClientChannel() throws Exception{
		// Configure the client channel,  connect to master server
		
        EventLoopGroup group = new NioEventLoopGroup();

        // setup the factory: here using a mixed memory/disk based on size threshold
        HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk if MINSIZE exceed

        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory
		
		Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class).handler(new ClientInitializer());
        
	}
	
	/**
	 * collect request information from user
	 * create message
	 */
	public void collectRequest(){
         //if post request
		 createPostMessage();
		 //if get request
		 createGetMessage();
		
	}

	/**
	 * send message out through Channel
	 */
	public void writeToChannel(){
		
		
	}
	
	private void createPostMessage(){
		
	}
	
	private void createGetMessage(){
		
	}
	
	
	
}
