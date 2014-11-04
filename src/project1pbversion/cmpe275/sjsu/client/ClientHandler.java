/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package project1pbversion.cmpe275.sjsu.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;

import project1.cmpe275.sjsu.conf.Configure;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.ResponseFlag;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.RequestType;
import project1pbversion.cmpe275.sjsu.protobuf.MessageManager;

import com.google.protobuf.ByteString;

public class ClientHandler extends SimpleChannelInboundHandler<Request> {


    private static final String savePath = Configure.ClientSavePath;

	@Override
    public void channelRead0(ChannelHandlerContext ctx, Request req) throws Exception {
    	
		RequestType type = req.getHeader().getPhotoHeader().getRequestType();
		ResponseFlag reFlag=req.getHeader().getPhotoHeader().getResponseFlag();
		String uuid = req.getBody().getPhotoPayload().getUuid();
		String picname=  req.getBody().getPhotoPayload().getName();
		
    	System.out.println("\nGot the feedback from Server-------");
    	StringBuilder sb= new StringBuilder();
    	sb.append("Original Request Type: " + type+"\n");
    	sb.append("Respond Flag: " + reFlag+"\n");
    	sb.append("UUID: " + uuid +"\n");
    	sb.append("Image Name: " +picname+"\n");
 
    	if(type.equals(RequestType.read)){
	    	ByteString data=req.getBody().getPhotoPayload().getData();   	
			sb.append("Received data:"+data.toString()+"\n");		
			File file=MessageManager.createFile("feedback_"+req.getBody().getPhotoPayload().getName(),savePath);
			MessageManager.writeByteStringToFile(data,file);
    	}
    	System.out.println(sb.toString());
    	
		
    	
    	

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
