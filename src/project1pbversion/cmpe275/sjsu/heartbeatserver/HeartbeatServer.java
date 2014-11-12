package project1pbversion.cmpe275.sjsu.heartbeatserver;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import project1pbversion.cmpe275.sjsu.protobuf.ImagePB;

import com.google.protobuf.ByteString;

import java.net.InetSocketAddress;
import java.util.logging.Level;
 

public class HeartbeatServer {
    public static final Logger logger = LoggerFactory.getLogger(HeartbeatServer.class);
 
    public static void main(String[] args) {
    	int portForSlave=9090;
    	startHeartbeatServer(portForSlave);

    }
    
    public static void startHeartbeatServer(int portForSlave){
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workerGroup).localAddress(new InetSocketAddress(portForSlave)).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
 
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline cp = ch.pipeline();
                    cp.addLast(new LoggingHandler(LogLevel.INFO));
                    cp.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(67108864, 0, 4, 0, 4));
                    cp.addLast(new ProtobufDecoder(ImagePB.Heartbeat.getDefaultInstance()));

                    cp.addLast("frameEncoder", new LengthFieldPrepender(4));
                    cp.addLast(new ProtobufEncoder());
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
 
