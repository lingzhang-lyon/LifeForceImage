package project1.cmpe275.sjsu.heartbeatserver;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.google.protobuf.ByteString;

import java.net.InetSocketAddress;
import java.util.logging.Level;
 

public class HeartbeatServer {
    public static final Logger logger = LoggerFactory.getLogger(HeartbeatServer.class);
 
    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workerGroup).localAddress(new InetSocketAddress(9090)).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
 
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline cp = ch.pipeline();
                    cp.addLast(new LoggingHandler(LogLevel.INFO));
                    cp.addLast(new LineBasedFrameDecoder(8192));
                    cp.addLast(new StringDecoder());
                    cp.addLast(new StringEncoder());
                    //     cp.addLast(new ChatMessageDispatchHandler());
                    //reader writer all idle time
                    cp.addLast("heartbeat", new IdleStateHandler(0, 0, 10));
                    cp.addLast("chatHandler", new HeartbeatSeverHandler());
                }
            });
 
            ChannelFuture future = server.bind().sync();
            logger.info("===========Server started===========");
            future.channel().closeFuture().sync();
            logger.info("===========Server closed===========");
        } catch (InterruptedException e) {
            logger.error(e.toString());
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
 
