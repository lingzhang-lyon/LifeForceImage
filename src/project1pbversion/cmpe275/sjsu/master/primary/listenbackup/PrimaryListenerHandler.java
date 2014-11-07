package project1pbversion.cmpe275.sjsu.master.primary.listenbackup;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import project1pbversion.cmpe275.sjsu.master.primary.PrimaryMasterServerHandler;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Heartbeat;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;

public class PrimaryListenerHandler extends SimpleChannelInboundHandler<Heartbeat> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Heartbeat hb)
			throws Exception {
		
		//if got heatbeat request from backup master 
		
		//TODO  notify primary master keep sending data to backup master
		PrimaryListener.setBackupMasterConnected(true);
		
		//then sent heatbeat back to backup master
		// Write the response.
    	 ChannelFuture future=ctx.channel().writeAndFlush(hb);
    	// Close the connection after the write operation is done.
    	 future.addListener(ChannelFutureListener.CLOSE);
		
		//if not get heatbeat from primary master

		
		
	}
	
	
	
}
