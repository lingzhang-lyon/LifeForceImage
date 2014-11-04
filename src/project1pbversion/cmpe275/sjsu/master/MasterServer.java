package project1pbversion.cmpe275.sjsu.master;

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
      
    public static void main(String[] args) throws Exception {
        
		
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {

            
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.handler(new LoggingHandler(LogLevel.INFO));
            b.childHandler(new MasterServerInitializer(false));  //false means no compression

            Channel ch = b.bind(PortForClient).sync().channel();


            ch.closeFuture().sync();
            
        } catch (Exception ex) {
        	System.err.println("Failed to setup channel for client");
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
				
    }
    

    
}
