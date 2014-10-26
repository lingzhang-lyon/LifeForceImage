package project1.cmpe275.sjsu.master;

import project1.cmpe275.sjsu.conf.Configure;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * This class is for receiving the msg from Client and finish task accordingly
 * if msg is just a connect request, send back connection information
 * 
 * if msg is a POST request (save image, the msg contain image object), find proper SlaveServers, and sent new msg (contain image object) to them.
 * then receive information sent back from SlaveServer, and store the metadata in the local database on MasterServer.
 *
 */
public class MasterServer {
	static final boolean SSL = System.getProperty("ssl") != null;
    static final int PortForClient = Configure.MasterPortForClient;
      
    static final int PortForSlave= Configure.MasterPortForSlave;


    public static void main(String[] args) throws Exception {
        
    	//for listen request from client
    	StartChannelForClient start1= new StartChannelForClient();
    	Thread thread1=new Thread(start1);
    	
    	//for listen slave heart beat
    	StartChannelForSlave start2= new StartChannelForSlave();
    	Thread thread2=new Thread(start2);
    	
    	//we can add even more channel for different usage
    	
    	thread1.start();
    	thread2.start();

    }
    
    private static class StartChannelForClient implements Runnable{
    		   	
			@Override
			public void run()  {				
		        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		        EventLoopGroup workerGroup = new NioEventLoopGroup();
		        try {
		            // Configure SSL.
		            final SslContext sslCtx;
		            if (SSL) {
		                SelfSignedCertificate ssc = new SelfSignedCertificate();
		                sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
		            } else {
		                sslCtx = null;
		            }
		            
		            ServerBootstrap b = new ServerBootstrap();
		            b.group(bossGroup, workerGroup);
		            b.channel(NioServerSocketChannel.class);
		            b.handler(new LoggingHandler(LogLevel.INFO));
		            b.childHandler(new MasterServerInitializer(sslCtx));
		
		            Channel ch = b.bind(PortForClient).sync().channel();
		
		            System.err.println("Open your web browser and navigate to " +
		                    (SSL? "https" : "http") + "://127.0.0.1:" + PortForClient + '/');
		
		            ch.closeFuture().sync();
		        } catch (Exception ex) {
		        	System.err.println("Failed to setup channel for client");
		        }finally {
		            bossGroup.shutdownGracefully();
		            workerGroup.shutdownGracefully();
		        }
			}	
    }
    
    /**
     * for listen slave heart beat
     *
     */
    private static class StartChannelForSlave implements Runnable{
    		   	
		@Override
		public void run()  {
			
		        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		        EventLoopGroup workerGroup = new NioEventLoopGroup();
		        try {
		            // Configure SSL.
		            final SslContext sslCtx;
		            if (SSL) {
		                SelfSignedCertificate ssc = new SelfSignedCertificate();
		                sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
		            } else {
		                sslCtx = null;
		            }
		            
		            ServerBootstrap b = new ServerBootstrap();
		            b.group(bossGroup, workerGroup);
		            b.channel(NioServerSocketChannel.class);
		            b.handler(new LoggingHandler(LogLevel.INFO));
		            b.childHandler(new MasterServerInitializer2(sslCtx));
		            //TODO need to change to heat beat Initializer, which connect to heart beat handler!!!!!
		
		            Channel ch = b.bind(PortForSlave).sync().channel();
		
		            System.err.println("Open your web browser and navigate to " +
		                    (SSL? "https" : "http") + "://127.0.0.1:" + PortForSlave + '/');
		
		            ch.closeFuture().sync();
		        } catch (Exception ex) {
		        	System.err.println("Failed to setup channel for client");
		        }finally {
		            bossGroup.shutdownGracefully();
		            workerGroup.shutdownGracefully();
		        }	
		}	
	
    }
    
}
