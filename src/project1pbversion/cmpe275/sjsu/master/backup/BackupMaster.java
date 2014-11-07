package project1pbversion.cmpe275.sjsu.master.backup;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import project1pbversion.cmpe275.sjsu.client.ClientInitializer;
import project1pbversion.cmpe275.sjsu.master.MasterController;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Heartbeat;

public class BackupMaster {
	
	
	private static String primaryMasterHost=null;
	private static int primaryMasterPort =0;
	private static String backupMasterHost=null;
	private static int backupMasterPort =0;
	
	public static String getPrimaryMasterHost() {
		return primaryMasterHost;
	}

	public static void setPrimaryMasterHost(String primaryMasterHost) {
		BackupMaster.primaryMasterHost = primaryMasterHost;
	}

	public static int getPrimaryMasterPort() {
		return primaryMasterPort;
	}

	public static void setPrimaryMasterPort(int primaryMasterPort) {
		BackupMaster.primaryMasterPort = primaryMasterPort;
	}

	


	
	
	public static void startBackupMaster() throws Exception{
		
		while(MasterController.isBackupMaster()){
			Heartbeat hb=createHeartbeat(backupMasterHost, backupMasterPort, 0);
			createChannelAndSendHeartbeat(primaryMasterHost, primaryMasterPort, hb);
		}
	}
	
	
	
	public static void createChannelAndSendHeartbeat(String host, int port, Heartbeat hb) throws Exception{
		
	    EventLoopGroup group = new NioEventLoopGroup();
	    try {
	        Bootstrap b = new Bootstrap();
	        b.group(group)
	         .channel(NioSocketChannel.class)
	         .handler(new ClientInitializer());
	
	        // Make a new connection.
	        Channel ch = b.connect(host, port).sync().channel();
	        
	        

	        
	        System.out.println("send a heartbeat from backup master " );
	        
	        ch.writeAndFlush(hb);
	
	        
	     // Wait for the server to close the connection.
	        ch.closeFuture().sync();
	        
	        ch.close();
	
	
	    } finally {
	        group.shutdownGracefully();
	    }
		
		
	}
	
	public static Heartbeat createHeartbeat(String ip, int port, int timeref){
        //create Heartbeat msg
        Heartbeat hb= Heartbeat.newBuilder()
        		.setIp(ip)
        		.setPort(port)
        		.setTimeRef(timeref)
        		.build();
        return hb;
        		
	}
			

}
