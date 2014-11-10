package project1.cmpe275.sjsu.heartbeatserver;

import java.net.UnknownHostException;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import project1.cmpe275.sjsu.model.Image;
import project1.cmpe275.sjsu.model.Socket;
import project1pbversion.cmpe275.sjsu.database.DatabaseManagerV2;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Heartbeat;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

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
    	
    	String mongoHost = "127.0.0.1";
    	int mongoPort =  27017;
    	String dbname = "heartbeat";
    	String collectionName = "slavestats";
    	Heartbeat hb = (Heartbeat) msg;
        String ip = hb.getIp();
        System.out.println("ip "+ ip);
        int port = hb.getPort();
        System.out.println("port "+ port);
        String slaveSocketString =ip +":"+port;
        
        Double loadfactor = hb.getLoadfactor();
        System.out.println("loadfactor " + loadfactor);       
        
        System.out.println("write to database!!!");
        
        storeSlaveHeartbeatMetaData( mongoHost, mongoPort,dbname,collectionName,
				slaveSocketString,  loadfactor);
        
        	
     }
 
   
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    	Heartbeat hb = Heartbeat.newBuilder()
		.setPort(9090)
		.build();
    	if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
               if (event.state() == IdleState.ALL_IDLE) {
                    System.out.println("no active event for 10 seconds");
                    ctx.writeAndFlush(hb);
                    } 
            //} 
           // else ctx.close();
        }
        super.userEventTriggered(ctx, evt);
    }
 
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    	 System.out.println("errors!!!");
    	 cause.printStackTrace();
        //ctx.close();
    }
    
    /**
	 * @param metaSocket is the socket that will store the meta data
	 * @param img
	 * @throws UnknownHostException
	 */
	private void storeSlaveHeartbeatMetaData(String mongohost, int mongoport,String dbname,String collectionName,
								String slaveSocketString, double loadfactor) throws UnknownHostException{
		
		
		System.out.println("Stored MetaData to MongoDB at "+ mongohost +":" + mongoport);
		
		
		
		Mongo mongo=new Mongo(mongohost, mongoport);
		DB db=mongo.getDB(dbname);
		DBCollection collection=db.getCollection(collectionName);
		
		BasicDBObject o=new BasicDBObject("socketstring", slaveSocketString)
						.append("loadfactor", loadfactor)
						.append("status", 1);//slave status 1 is active. 0 is inactive
		collection.insert(o);
		
		
		
	}
}
