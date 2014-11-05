package project1pbversion.cmpe275.sjsu.othercluster;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;

public class OtherClusterHandler extends SimpleChannelInboundHandler<Request>{
	
    // Stateful properties
    private volatile Channel channel;
    
	private Request resRequest;
	
	 public OtherClusterHandler() {
	        super(false);
	 }
	
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        channel = ctx.channel();
    }	 
 
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Request msg)
			throws Exception {
		resRequest=msg;
		System.out.println("In Handler,1, image uuid in feedback request from other cluster: " 
				+msg.getBody().getPhotoPayload().getUuid());
		System.out.println("In Handler,2, image uuid in feedback request from other cluster: " 
				+this.resRequest.getBody().getPhotoPayload().getUuid());
		
	}

	public Request getResRequest() {
		return resRequest;
	}

	public void setResRequest(Request resRequest) {
		this.resRequest = resRequest;
	}
	
	

}
