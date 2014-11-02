/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package project1pbversion.cmpe275.sjsu.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.net.URI;

import project1.cmpe275.sjsu.conf.Configure;
import project1.cmpe275.sjsu.model.Image;
import project1pbversion.cmpe275.sjsu.example.worldclock.WorldClockServer;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Header;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Header.RequestType;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Payload;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;

/**
 * Sends a list of continent/city pairs to a {@link WorldClockServer} to
 * get the local times of the specified cities.
 */
public final class Client {

    static final boolean SSL = System.getProperty("ssl") != null;
    private static final boolean isTest =Configure.isTest;
    static final String BASE_URL = System.getProperty("baseUrl", Configure.BASE_URL);

    public static void main(String[] args) throws Exception {
    	
    	System.out.println("Start Client***************\n");
    	
    	//TODO for get test, need to be removed later
        if(isTest){
        	 System.out.println("test with get (read) request");	
	         Image image = new Image();
	         image.setUri(new URI(BASE_URL+"formget"));
	         image.setUuid("testuuid");
	         System.out.println("creat a test image with uuid: " +image.getUuid());
	         createChannelAndSendRequest(image);
         
        }
        //end of test code
        
        
        //TODO create a user interface to input the request and picture information
        
    }
    
    public static void createChannelAndSendRequest(Image img) throws Exception{

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ClientInitializer());

            // Make a new connection.
           String host=img.getUri().getHost();
           int port=img.getUri().getPort();         
            Channel ch = b.connect(host, port).sync().channel();
            
            
            //convert img to a Request
          //  Request req= createRequest(img);
           // Request req= createTestRequest();
            Request req= createRequest(RequestType.read, img);
            
            System.out.println("image uuid in the request: " +req.getBody().getUuid());
            
            ch.writeAndFlush(req);
 
            
         // Wait for the server to close the connection.
            ch.closeFuture().sync();
            
            ch.close();


        } finally {
            group.shutdownGracefully();
        }
    	
    	
    }
    
    
    public static Request createRequest(Image img)throws Exception{ //restful 
    	
    	Request.Builder builder = Request.newBuilder();        
        Payload.Builder payloadbuilder = Payload.newBuilder();
        Header.Builder headerbuilder = Header.newBuilder();
//        System.out.println("image uuid: " +img.getUuid()); 
//        System.out.println(img.getUri().getPath());
        
        if(img.getUri().getPath().startsWith("/formget")){
        	headerbuilder.setRequestType(RequestType.read);  
        	payloadbuilder.setUuid(img.getUuid());
        }
        else if( img.getUri().getPath().startsWith("/formpost") ){
        	headerbuilder.setRequestType(RequestType.write);  
        	payloadbuilder.setUuid(img.getUuid()).setName(img.getImageName());
        	//TODO we also need to set file into payload
        	
        }
        
        builder.setHeader(headerbuilder.build());
   	 	builder.setBody(payloadbuilder.build());
        return builder.build();
    
    	
    }
    
    public static Request createRequest(RequestType reqtype, Image img)throws Exception{
    	
    	Request.Builder builder = Request.newBuilder();        
        Payload.Builder payloadbuilder = Payload.newBuilder();
        Header.Builder headerbuilder = Header.newBuilder();
//        System.out.println("image uuid: " +img.getUuid()); 
//        System.out.println(img.getUri().getPath());
        
        if(reqtype.equals(RequestType.read)){
        	headerbuilder.setRequestType(RequestType.read);  
        	payloadbuilder.setUuid(img.getUuid());
        }
        else if( reqtype.equals(RequestType.write)){
        	headerbuilder.setRequestType(RequestType.write);  
        	payloadbuilder.setUuid(img.getUuid()).setName(img.getImageName());
        	//TODO we also need to set file into payload
        	
        }
        
        builder.setHeader(headerbuilder.build());
   	 	builder.setBody(payloadbuilder.build());
        return builder.build();
    
    	
    }
    
public static Request createTestRequest()throws Exception{
    	
    	Request.Builder builder = Request.newBuilder();        
        Payload.Builder payloadbuilder = Payload.newBuilder();
        Header.Builder headerbuilder = Header.newBuilder();
        
        
    	headerbuilder.setRequestType(RequestType.read);  
    	payloadbuilder.setUuid("testuuid");
        
        
        builder.setHeader(headerbuilder.build());
   	 	builder.setBody(payloadbuilder.build());
        return builder.build();
    
    	
    }
    
    
    
}
