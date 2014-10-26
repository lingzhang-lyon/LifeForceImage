package project1.cmpe275.sjsu.master;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;

import project1.cmpe275.sjsu.client.Client2;
import project1.cmpe275.sjsu.client.ClientInitializer;
import project1.cmpe275.sjsu.model.Image;
import project1.cmpe275.sjsu.model.Socket;

public class MessageSender2 {
	
	static final boolean SSL = System.getProperty("ssl") != null;
    static final int SlavePort= 8080;
    static final String SlaveHost="127.0.0.1";
    
   public void setStorgeLoal(ArrayList<String>nodes,Image image){
	   	   //assume index 0 is storage location, index 1 is replica location.
		   image.setNodeAdress(nodes.get(0));
		   image.setBackupNodeAdress(nodes.get(1));
	    }
   
   
   public static void masterPostSlave(Socket socket, Image image) {
	   String host=socket.getIp();
	   int port=socket.getPort();
	   String baseURL="http://"+host+":"+port;
	  	    
       try {
			
			URI uriSimple=new URI(baseURL + "formpost");
			
			Client2.createAndSendPostMessage(host, port,uriSimple, image);
			//later this method should encapsulated to a separate package!!!!
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	  
	}	
   
   
   
}
