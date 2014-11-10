package project1pbversion.cmpe275.sjsu.master.primary.listenbackup;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import project1.cmpe275.sjsu.heartbeatserver.HeartbeatServer;
import project1.cmpe275.sjsu.model.Socket;

public class PrimaryListener implements Runnable{
	
	public static final Logger logger = LoggerFactory.getLogger(HeartbeatServer.class);
	
	private int portForBackup;
	private static boolean backupMasterConnected=false;
	private static Socket backupMasterSocket=null;
	
	public static Socket getBackupMasterSocket() {
		return backupMasterSocket;
	}


	public static void setBackupMasterSocket(Socket backupMasterSocket) {
		PrimaryListener.backupMasterSocket = backupMasterSocket;
	}


	public static boolean isBackupMasterConnected() {
		return backupMasterConnected;
	}


	public static void setBackupMasterConnected(boolean backupMasterConnected) {
		PrimaryListener.backupMasterConnected = backupMasterConnected;
	}
	
	
	
	public PrimaryListener(int portForBackup){
		this.portForBackup=portForBackup;
	}
	
	 public static void startPrimaryMasterListener(int portForBackup){


		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.handler(new LoggingHandler(LogLevel.INFO));
			boolean compress=false;            
			b.childHandler(new PrimaryListenerInitializer(compress));  //false means no compression
			
			Channel ch = b.bind(portForBackup).sync().channel();
			logger.info("===========Primary Listener for listening Backup heatbeat call started===========");
			
			ch.closeFuture().sync();
			logger.info("===========Primary Listener for listening Backup heatbeat call closed===========");
		
		} catch (Exception ex) {
			System.err.println("Failed to setup channel to listen primary master");
		}finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
		
		
	}

	@Override
	public void run() {
		startPrimaryMasterListener(this.portForBackup);
	}
	
	public static void main(String[] ags){
		@SuppressWarnings("resource")
		Scanner reader0 = new Scanner(System.in);
    	System.out.println("Input port for listen heartbeat from backup master:");
    	int port=reader0.nextInt();
    	
		startPrimaryMasterListener(port);
	}
}
