package project1.cmpe275.sjsu.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;

/**
 *  this handler is for client after received msg from MasterServer
 *  it should be able to print out the received image information
 *  or show the image
 *  or save the image
 *
 */
public class ClientHandler extends SimpleChannelInboundHandler<HttpObject> {
	
	@Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {		
	
	}

}
