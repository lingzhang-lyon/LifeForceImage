package project1pbversion.cmpe275.sjsu.master;

import java.util.Scanner;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import project1.cmpe275.sjsu.conf.Configure;


public class MasterServer {
    static final int PortForClient = Configure.MasterPortForClient;
      
    @SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
        
    	System.out.println("Start Master Server***************\n");
    	
		Scanner reader0 = new Scanner(System.in);
    	System.out.println("a.Just simple test(use static configure)  | b. Input your own configure:");
    	String input=reader0.nextLine();
    	int port;
    	boolean saveToLocal=false;
    	
    	if (input.equals("a")){
    		port=PortForClient;
    		saveToLocal=true;
    	}
    	else{
	    	System.out.println("enter the port open for client, like: 8080");
	    	port =reader0.nextInt();
	    	Scanner reader = new Scanner(System.in);
	    	System.out.println("Do you want to save received file to master server local file system? (Y/N) ");
	    	String res=reader.nextLine();
	    	if(res.equals("Y")||res.equals("y")){
	    		saveToLocal=true;
	    	}
	    	
    	}	
    	
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {

            
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.handler(new LoggingHandler(LogLevel.INFO));
            boolean compress=false;            
            b.childHandler(new MasterServerInitializer(compress,saveToLocal));  //false means no compression

            Channel ch = b.bind(port).sync().channel();


            ch.closeFuture().sync();
            
        } catch (Exception ex) {
        	System.err.println("Failed to setup channel for client");
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
				
    }
    

    
}
