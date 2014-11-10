package project1pbversion.cmpe275.sjsu.master.backup;

import java.util.Scanner;

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
	         .handler(new BackupMasterInitializer());
	
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
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		Scanner reader0 = new Scanner(System.in);
    	System.out.println("Input IP of primary master: like 127.0.0.1");
		String primaryMasterHost=reader0.nextLine();
		
		System.out.println("Input port of primary master listening heartbeat: like 7070");
		int primaryMasterPort =reader0.nextInt();
		
		Scanner reader1 = new Scanner(System.in);
    	System.out.println("Input mili seconds for heartbeatInterval: like 5000");
    	int heartbeatInterval=reader1.nextInt();
		
		String backupMasterHost="127.0.0.1";
		int backupMasterPort =7070;
		
		BackupMasterHandler.setHeartbeatInterval(heartbeatInterval);
		
		System.out.println("Start sent heartbeat from backup to primary master ");
		Heartbeat hb=BackupMaster.createHeartbeat(backupMasterHost, backupMasterPort, 0);
		BackupMaster.createChannelAndSendHeartbeat(primaryMasterHost, primaryMasterPort, hb);
		
	}
			

}
