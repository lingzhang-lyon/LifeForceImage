package project1pbversion.cmpe275.sjsu.othercluster;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import project1pbversion.cmpe275.sjsu.model.Socket;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.RequestType;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;
import project1pbversion.cmpe275.sjsu.protobuf.MessageManager;

public class OtherClusterManager {
	
	
	public static Request createChannelAndSendRequest(Socket otherClusterSocket, Request req) throws Exception{
		//TODO
		 EventLoopGroup group = new NioEventLoopGroup();
		 String host=otherClusterSocket.getIp();
		 int port=otherClusterSocket.getPort();
	    try {
	        Bootstrap b = new Bootstrap();
	        
	        b.group(group)
	         .channel(NioSocketChannel.class)
	         .handler(new OtherClusterInitializer());
	
	        // Make a new connection.
	        Channel ch = b.connect(host, port).sync().channel();
	        
	        

            
	        
	        System.out.println("image uuid in passing request: " +req.getBody().getPhotoPayload().getUuid());
	        
	        ch.writeAndFlush(req);
		     // Wait for the server to close the connection.
	        ch.closeFuture().sync();
	        
		     // Get the handler instance to initiate the request.
            OtherClusterHandler handler = ch.pipeline().get(OtherClusterHandler.class);
	        Request resRequest=handler.getResRequest();
	        System.out.println("In Manger,1, image uuid in feedback request from other cluster: " 
					+resRequest.getBody().getPhotoPayload().getUuid());

	        
	        ch.close();
	
	       
	        return resRequest;
	
	    } finally {
	        group.shutdownGracefully();
	    }
		
		
	}
	
	public static void main(String[] args) throws Exception{
		Socket otherClusterSocket=new Socket("127.0.0.1", 8080);
		Request sampleRequest=MessageManager.createSampleRequest("testForOtherCluster", RequestType.read);
		
		Request resRequest=createChannelAndSendRequest(otherClusterSocket, sampleRequest);
		System.out.println("In Manger,2, image uuid in feedback request from other cluster: " 
							+resRequest.getBody().getPhotoPayload().getUuid());
		
		
	}
	
	
}
