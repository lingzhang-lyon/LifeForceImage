package project1pbversion.cmpe275.sjsu.master.primary;

import java.util.Scanner;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import project1pbversion.cmpe275.sjsu.master.MasterServerHandler;
import project1pbversion.cmpe275.sjsu.master.MasterServerInitializer;

public class PrimaryMaster implements Runnable{
	
	
	private int portForBackup;
	
	public PrimaryMaster(int portForBackup){
		this.portForBackup=portForBackup;
	}
	
	 public static void startMasterServer(int portForBackup){


		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.handler(new LoggingHandler(LogLevel.INFO));
			boolean compress=false;            
			b.childHandler(new PrimaryMasterInitializer(compress));  //false means no compression
			
			Channel ch = b.bind(portForBackup).sync().channel();
			
			
			ch.closeFuture().sync();
		
		} catch (Exception ex) {
			System.err.println("Failed to setup channel to listen primary master");
		}finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
		
		
	}

	@Override
	public void run() {
		startMasterServer(this.portForBackup);
	}
	
	public static void main(String[] ags){
		@SuppressWarnings("resource")
		Scanner reader0 = new Scanner(System.in);
    	System.out.println("input port for heartbeat:");
    	int port=reader0.nextInt();
		startMasterServer(port);
	}
}
