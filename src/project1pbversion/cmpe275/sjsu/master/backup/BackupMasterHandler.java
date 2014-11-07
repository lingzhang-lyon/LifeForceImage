
package project1pbversion.cmpe275.sjsu.master.backup;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import project1pbversion.cmpe275.sjsu.master.MasterController;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Heartbeat;

public class BackupMasterHandler extends SimpleChannelInboundHandler<Heartbeat> {


	@Override
    public void channelRead0(ChannelHandlerContext ctx, Heartbeat hb) throws Exception {
		System.out.println("\nGot the heartbeat back from primary master server-------");
		
    	

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //cause.printStackTrace();
        MasterController.setBackupMaster(false);
        MasterController.setPrimaryMaster(true);
        ctx.close();
    }
    
   
    
    
    
}
