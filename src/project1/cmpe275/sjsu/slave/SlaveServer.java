package project1.cmpe275.sjsu.slave;

import project1.cmpe275.sjsu.conf.Configure;
import project1.cmpe275.sjsu.slave.SlaveServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
//import project1.cmpe275.sjsu.HttpUploadServerInitializer;
/**
 * This class is to receive msg from MasterServer 
 * 
 * if msg is just a connect request, send back connection information
 * 
 * if msg is a Get request (search request), it should find the image according provided information (name, userid), 
 * and send back all the image object back to MasterServer;
 * 
 * if msg is a Post request (save image), it should save the file to the database in SlaveServer and return the metadata information to MasterServer
 * 
 *
 */
public class SlaveServer {
	
    static final int SlavePort = Configure.SLAVE_PORT;

	static final boolean SSL = System.getProperty("ssl") != null;

    public static void main(String[] args) throws Exception {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
        } else {
            sslCtx = null;
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.handler(new LoggingHandler(LogLevel.INFO));
            b.childHandler(new SlaveServerInitializer(sslCtx));

            Channel ch = b.bind(SlavePort).sync().channel();

            System.err.println("Open your web browser and navigate to " +
                    (SSL? "https" : "http") + "://127.0.0.1:" + SlavePort + '/');

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
