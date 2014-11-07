package project1pbversion.cmpe275.sjsu.master.primary;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Heartbeat;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;

public class PrimaryMasterHandler extends SimpleChannelInboundHandler<Heartbeat> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Heartbeat hb)
			throws Exception {
		// TODO Auto-generated method stub
		
		//if got heatbeat request from backup master 
		
		//TODO  notify primary master keep sending 
		
		//then sent heatbeat back to backup master
		// Write the response.
    	 ChannelFuture future=ctx.channel().writeAndFlush(hb);
    	// Close the connection after the write operation is done.
    	 future.addListener(ChannelFutureListener.CLOSE);
		
		//if not get heatbeat from primary master

		
		
	}
	
}
