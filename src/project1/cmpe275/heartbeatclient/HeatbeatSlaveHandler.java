package project1.cmpe275.heartbeatclient;

import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Heartbeat;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

public class HeatbeatSlaveHandler extends ChannelDuplexHandler {
	    
		Heartbeat hb = Heartbeat.newBuilder()
						.setIp("192.168.1.1")
						.setLoadfactor(0.1)
						.build();
	    @Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	        String retMsg=(String)msg;
	        if("Ping".equals(retMsg)){
	            ctx.writeAndFlush(hb);
	        }
	    }
	 
}
