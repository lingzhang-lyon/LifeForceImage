package project1.cmpe275.heartbeatclient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
import java.net.InetSocketAddress;
 

public class HeartbeatClient {
    public static final Logger logger= LoggerFactory.getLogger(HeartbeatClient.class);
 
    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group).channel(NioSocketChannel.class).remoteAddress(new InetSocketAddress("localhost", 9090));
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
 
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                //  ch.pipeline().addLast(new OutboundHandlerT());
                ch.pipeline().addLast(new LineBasedFrameDecoder(8192));
                ch.pipeline().addLast(new StringEncoder());
                ch.pipeline().addLast(new StringDecoder());
                ch.pipeline().addLast(new ClientHeartbeatHandler());
            }
        });
        try {
            ChannelFuture future = bootstrap.connect().sync();
            future.channel().closeFuture().sync();
            logger.info("===========Close==========");
        } catch (InterruptedException e) {
            logger.error(e.toString());
        } finally {
            group.shutdownGracefully();
        }
    }
}
 
class ClientHeartbeatHandler extends ChannelDuplexHandler {
 
 
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String retMsg=(String)msg;
        if("Ping".equals(retMsg)){
            ctx.writeAndFlush("ipaddress,loadfactor");
        }
    }
 
 
 
 
}
