
package project1pbversion.cmpe275.sjsu.master.backup;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import project1pbversion.cmpe275.sjsu.master.MasterController;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Heartbeat;

public class BackupMasterHandler extends SimpleChannelInboundHandler<Heartbeat> {

	private static int heartbeatInterval=5000;
	
	public static int getHeartbeatInterval() {
		return heartbeatInterval;
	}

	public static void setHeartbeatInterval(int heartbeatInterval) {
		BackupMasterHandler.heartbeatInterval = heartbeatInterval;
	}
	

	@Override
    public void channelRead0(ChannelHandlerContext ctx, Heartbeat hb) throws Exception {
		System.out.println("\nGot the heartbeat back from primary master server-------");
		
		Thread.sleep(heartbeatInterval);
		
		System.out.println("After " +heartbeatInterval/1000+" seconds, Send another heartbeat to primary master");
		ctx.channel().writeAndFlush(hb);
    	

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //cause.printStackTrace();
    	System.out.println("!!! exception got by backupMasterHandler");
        MasterController.setBackupMaster(false);
        MasterController.setPrimaryMaster(true);
        ctx.close();
    }
    
   
    
    
    
}
