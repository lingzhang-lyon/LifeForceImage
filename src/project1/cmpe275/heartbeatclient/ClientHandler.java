package project1.cmpe275.heartbeatclient;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

public class ClientHandler extends ChannelDuplexHandler {
	    
		 
	    @Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	        String retMsg=(String)msg;
	        if("Ping".equals(retMsg)){
	            ctx.writeAndFlush("ipaddress,loadfactor");
	        }
	    }
	 
}
