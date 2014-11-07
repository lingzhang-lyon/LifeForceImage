package project1.cmpe275.sjsu.heartbeatserver;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeverHandler  extends ChannelDuplexHandler {
	public static final Logger logger = LoggerFactory.getLogger(SeverHandler.class);
   
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String retMsg = (String) msg;
        if (!"Pong".equals(retMsg)) {
        	logger.info("update slave status to dead not network!!!");
            ctx.close();
            
        }
       
        	logger.info("write to database!!!");
        	//insert("192.168.1.1",0.1,cl);
        	
     }
 
    /**
	 *  Info Inserting Method
	 
    public void insert(String ip,int loadfactor, DBCollection collection)
    {
    	BasicDBObject o = new BasicDBObject();
        
        // prepare inserting statement
        o.append("ipaddress", ip)
         .append("loadfactor", loadfactor);
         
        
        collection.insert(o);
        
        logger.info("Inserted record of " + ip + ", loadfactor = " + loadfactor);
    }
    */
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
