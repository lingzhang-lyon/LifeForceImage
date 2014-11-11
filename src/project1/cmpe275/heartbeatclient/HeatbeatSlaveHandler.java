package project1.cmpe275.heartbeatclient;

import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Heartbeat;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class HeatbeatSlaveHandler extends ChannelDuplexHandler {	
	    
		Heartbeat hb = Heartbeat.newBuilder()
						.setIp("192.168.1.2")  //ip of slave to store image


						.setPort(27017)  //port for slave mongoDB to store image
						.setLoadfactor(0.1)
						.build();
	    @Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	        Heartbeat hbc = (Heartbeat)msg;
	    	int retMsg=hbc.getPort();
	    	 System.out.println("\nreceived heartbeat call from master with port:" + retMsg);
	        //if(retMsg==9090){
	            ctx.writeAndFlush(hb);
	        //}
	    }
	    
	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	        System.out.println("something wrong with slave heartbeat");
	    	cause.printStackTrace();
	        
	       // ctx.close();
	    }
	
	 
}
