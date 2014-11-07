package project1.cmpe275.sjsu.heartbeatserver;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import project1pbversion.cmpe275.sjsu.database.DatabaseManagerV2;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Heartbeat;

import com.mongodb.DBCollection;

public class HeartbeatSeverHandler  extends ChannelDuplexHandler {
	public static final Logger logger = LoggerFactory.getLogger(HeartbeatSeverHandler.class);
	public static DBCollection cl;
	public static DatabaseManagerV2 dbv2;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
       /* String retMsg = (String) msg;
        if (!"Pong".equals(retMsg)) {
        	logger.info("update slave status to dead not network!!!");
            ctx.close();
            
        }*/
    	Heartbeat hb = (Heartbeat) msg;
        String ip = hb.getIp();
        logger.info(ip);
        Double loadfactor = hb.getLoadfactor();
        logger.info("loadfactor " + loadfactor);
        logger.info("write to database!!!");
        dbv2.updateSlaveStatus(ip,loadfactor,cl);
        	
     }
 
   
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            //if (event.isFirst()) { //如果是第一次出现Idle，则发送心跳包进行检测，否则直接关闭连接
               if (event.state() == IdleState.ALL_IDLE) {
                    System.out.println("no active event for 10 seconds");
                    ctx.writeAndFlush("Ping\n");
                    } 
            //} 
           // else ctx.close();
        }
        super.userEventTriggered(ctx, evt);
    }
 
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    
        ctx.close();
    }
}
