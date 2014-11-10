package project1.cmpe275.sjsu.heartbeatserver;

import java.net.SocketAddress;
import java.net.UnknownHostException;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import project1.cmpe275.sjsu.model.Image;
import project1.cmpe275.sjsu.model.Socket;
import project1pbversion.cmpe275.sjsu.database.DatabaseManagerV2;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Heartbeat;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;

public class HeartbeatSeverHandler extends ChannelDuplexHandler {
	public static final Logger logger = LoggerFactory.getLogger(HeartbeatSeverHandler.class);
	public static DBCollection cl;
	public static DatabaseManagerV2 dbv2;
	private String mongoHost= "127.0.0.1";
	private int mongoPort =  27017;
	private String dbname = "275db";
	private String collectionName = "Slave";
	private String slaveSocketString="";
	private Double loadfactor=0.0;
	private int status =1;

	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
       /* String retMsg = (String) msg;
        if (!"Pong".equals(retMsg)) {
        	logger.info("update slave status to dead not network!!!");
            ctx.close();
            
        }*/
    	Heartbeat hb = (Heartbeat) msg;
        String ip = hb.getIp();
        logger.info("ip "+ ip);
        int port = hb.getPort();
        System.out.println("port "+ port);
        slaveSocketString =ip +":"+port;
        loadfactor = hb.getLoadfactor();
        logger.info("loadfactor " + loadfactor);       
        //logger.info("update database!!!");   
        storeSlaveHeartbeatMetaData( mongoHost, mongoPort,dbname,collectionName,slaveSocketString,loadfactor,status);
     }
    
    /*@Override
    public void channelActive(ChannelHandlerContext ctx)throws Exception{
    	logger.info("monitor channel active");
    	// TODO to db
    	System.out.println("write to database!!!");
        storeSlaveHeartbeatMetaData( mongoHost, mongoPort,dbname,collectionName,
				slaveSocketString,  loadfactor, status);
    	
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.error("monitor channel inactive");
        status = 0;
        storeSlaveHeartbeatMetaData( mongoHost, mongoPort,dbname,collectionName,
				slaveSocketString,  loadfactor, status);
		// TODO try to reconnect
	}*/
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
								String slaveSocketString, double loadfactor,int status) throws UnknownHostException{
		
		 Mongo mongo=new Mongo(mongoHost, mongoPort);//connect to MongoDB server
	    	DB db=mongo.getDB(dbname);// connect to DB
	    	DBCollection collection=db.getCollection(collectionName);//connect to colloction
	        DBCursor cursor = collection.find(new BasicDBObject(),new BasicDBObject("socketstring", slaveSocketString));
	        BasicDBObject o=new BasicDBObject("socketstring", slaveSocketString)
		    		.append("loadfactor", loadfactor)
		    		.append("status", status);//slave status 1 is active. 0 is inactive
	        
	        if(!cursor.hasNext()){
	        	System.out.println("Stored MetaData to MongoDB at "+ mongohost +":" + mongoport);
	            collection.insert(o);
	        }
	        else{
	        	System.out.println("update slave status");
	        	BasicDBObject query = new BasicDBObject("socketstring", slaveSocketString);
	        	o.append("loadfactor", 0.5);
	        	collection.update(query, o, true, false);
	        	
	        }
		
		
		
		
	}
}
