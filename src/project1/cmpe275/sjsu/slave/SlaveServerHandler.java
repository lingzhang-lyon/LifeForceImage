package project1.cmpe275.sjsu.slave;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import project1.cmpe275.sjsu.master.MasterServerHandler;

public class SlaveServerHandler extends SimpleChannelInboundHandler<HttpObject> {
	
	private static final Logger logger = Logger.getLogger(SlaveServerHandler.class.getName());
	private HttpRequest request;
	private final StringBuilder responseContent = new StringBuilder();
	private HttpPostRequestDecoder decoder;
	
 	@Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        if (decoder != null) {
            decoder.cleanFiles();
        }
    }
 	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	     logger.log(Level.WARNING, responseContent.toString(), cause);
	     ctx.channel().close();
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		if (msg instanceof HttpRequest) {
	        HttpRequest request = this.request = (HttpRequest) msg;
	        URI uri = new URI(request.getUri());
	        
	        //if not a form request just write the menu back to the channel
		    if (!uri.getPath().startsWith("/form")) {
		        // Write Menu
		            writeMenu(ctx);
		            return;
		        }
		    else{		    	
		    	//write an info message back to channel????
		    		writeFormResponse(ctx);
		        //or pass to other handler????
		    }
		}
		
	}
    
	private void writeMenu(ChannelHandlerContext ctx) {
	        // print several HTML forms
	        // Convert the response content to a ChannelBuffer.
	        responseContent.setLength(0);
	
	        // create Pseudo Menu
	        responseContent.append("<html>");
	        responseContent.append("<head>");
	        responseContent.append("<title>Netty Test Form</title>\r\n");
	        responseContent.append("</head>\r\n");
	        responseContent.append("<body bgcolor=white><style>td{font-size: 12pt;}</style>");
	
	        responseContent.append("<table border=\"0\">");
	        responseContent.append("<tr>");
	        responseContent.append("<td>");
	        responseContent.append("<h1>Netty Test Form</h1>");
	
	        responseContent.append("</td>");
	        responseContent.append("</tr>");
	        responseContent.append("</table>\r\n");
	
	
	        // POST with enctype="multipart/form-data"
	        responseContent.append("<CENTER>POST MULTIPART FORM<HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");
	        responseContent.append("<FORM ACTION=\"/formpostmultipart\" ENCTYPE=\"multipart/form-data\" METHOD=\"POST\">");
	        responseContent.append("<input type=hidden name=getform value=\"POST\">");
	        responseContent.append("<table border=\"0\">");
	        responseContent.append("<tr><td>Picture name: <br> <input type=text name=\"pictureName\" size=20></td></tr>");
	        responseContent.append("<tr><td>User name: <br> <input type=text name=\"userName\" size=20></td></tr>");
	        responseContent.append("<tr><td>Category: <br> <input type=text name=\"category\" size=20></td></tr>");
	        responseContent.append("<tr><td>Select file to upload: <br> <input type=file name=\"myfile\">");
	//	    	        responseContent.append("<tr><td>Select file to upload: <br> <input type=file name=\"myfile2\">");
	        responseContent.append("</td></tr>");
	        responseContent.append("<tr><td><INPUT TYPE=\"submit\" NAME=\"Send\" VALUE=\"Send\"></INPUT></td>");
	        responseContent.append("<td><INPUT TYPE=\"reset\" NAME=\"Clear\" VALUE=\"Clear\" ></INPUT></td></tr>");
	        responseContent.append("</table></FORM>\r\n");
	        responseContent.append("<CENTER><HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");
	
	        responseContent.append("</body>");
	        responseContent.append("</html>");
	
	        ByteBuf buf = copiedBuffer(responseContent.toString(), CharsetUtil.UTF_8);
	        // Build the response object.
	        FullHttpResponse response = new DefaultFullHttpResponse(
	                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
	
	        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
	        response.headers().set(CONTENT_LENGTH, buf.readableBytes());
	
	        // Write the response.
	        ctx.channel().writeAndFlush(response);
	  }
	
	private void writeFormResponse(ChannelHandlerContext ctx){
   	 responseContent.setLength(0);
   	 responseContent.append("it's a form request, need other handler");
   	 ByteBuf buf = copiedBuffer(responseContent.toString(), CharsetUtil.UTF_8);
	        // Build the response object.
	        FullHttpResponse response = new DefaultFullHttpResponse(
	                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
	
	        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
	        response.headers().set(CONTENT_LENGTH, buf.readableBytes());
	
	        // Write the response.
	        ctx.channel().writeAndFlush(response);
	}
		
}
