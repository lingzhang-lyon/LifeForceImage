package project1.cmpe275.sjsu.message;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.ClientCookieEncoder;
import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder.ErrorDataEncoderException;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;

import project1.cmpe275.sjsu.model.Image;

public class MessageSender {
	
   public void setStorgeLoal(ArrayList<String>nodes,Image image){
	   	   //assume index 0 is storage location, index 1 is replica location.
		   image.setNodeAdress(nodes.get(0));
		   image.setBackupNodeAdress(nodes.get(1));
	    }
   
   public static void createAndSendPostMessage( Bootstrap bootstrap, String host, int port, 
			URI uriSimple, HttpDataFactory factory, Image image) throws Exception{
		 
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
       bodyRequestEncoder.addBodyAttribute("catgory", image.getCategory());
       bodyRequestEncoder.addBodyFileUpload("myfile", image.getFile(), "application/x-zip-compressed", false); // isText=false
	}
	
   
}
