package project1.cmpe275.sjsu.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map.Entry;

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
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

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
	

	private static String HOST = "127.0.0.1";
	private static int PORT = 8080;
	static final String BASE_URL = System.getProperty("baseUrl", "http://127.0.0.1:8080/");
    static final String FILE = System.getProperty("file", "/Users/lingzhang/Desktop/test1.jpeg");
	
	
	public static void main(String[] agrs) throws Exception{
			
		 // setup the factory: here using a mixed memory/disk based on size threshold
        HttpDataFactory fact = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk if MINSIZE exceed
        
        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory		
		
        EventLoopGroup g = new NioEventLoopGroup();
 
        
        try {
			Bootstrap b = new Bootstrap();
			b.group(g)
			 .channel(NioSocketChannel.class)
			 .handler(new ClientInitializer());
      
						
			URI uriSimple=new URI(BASE_URL + "formpost");
			File file = new File(FILE);
			if (!file.canRead()) {
			    throw new FileNotFoundException(FILE);
			}
			
			createAndSendPostMessage(b, HOST,PORT,uriSimple, file, fact, null);
			
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
	
	
	

	
	private static void createAndSendPostMessage( Bootstrap bootstrap, String host, int port, URI uriSimple, File file, HttpDataFactory factory,
            List<Entry<String, String>> headers) throws Exception{
		 
		// Start the connection attempt.
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
        // Wait until the connection attempt succeeds or fails.
        Channel channel = future.sync().channel();
        

        // Prepare the HTTP request.
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uriSimple.toASCIIString());

        //set headers
        // it is legal to add directly header or cookie into the request until finalize
        if(headers==null){ //default header
        	request.headers().set(
        			HttpHeaders.Names.COOKIE, 
        			ClientCookieEncoder.encode(new DefaultCookie("my-cookie", "foo"))
                    );
        }else{
	        for (Entry<String, String> entry : headers) {
	            request.headers().set(entry.getKey(), entry.getValue());
	        }
        }
        
        
        // Use the PostBody encoder
        HttpPostRequestEncoder bodyRequestEncoder =
                new HttpPostRequestEncoder(factory, request, false);  // false => not multipart

        // add Form attribute
        bodyRequestEncoder.addBodyAttribute("getform", "POST");
        bodyRequestEncoder.addBodyAttribute("user", "lingzhang");
        bodyRequestEncoder.addBodyAttribute("picname", "mypic");
        bodyRequestEncoder.addBodyAttribute("catgory", "food");
        bodyRequestEncoder.addBodyFileUpload("myfile", file, "application/x-zip-compressed", false); //what each parameter mean??????

       request= bodyRequestEncoder.finalizeRequest();
        
        // send request
        channel.write(request);

        // test if request was chunked and if so, finish the write
        if (bodyRequestEncoder.isChunked()) {
            channel.write(bodyRequestEncoder);
        }
        channel.flush();

        // Now no more use of file representation (and list of HttpData)
        bodyRequestEncoder.cleanFiles();

        // Wait for the server to close the connection.
        channel.closeFuture().sync();
        		
	}
	
	private void createGetMessage(){
		
	}
	
	
	
	
	
}
