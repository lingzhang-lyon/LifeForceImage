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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.regex.Pattern;

import project1.cmpe275.sjsu.model.Image;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;

public class ClientHandler extends SimpleChannelInboundHandler<Request> {


    @Override
    public void channelRead0(ChannelHandlerContext ctx, Request req) throws Exception {
    	
    	System.out.println("\nGot the feedback from Server-------");
    	StringBuilder sb= new StringBuilder();
    	sb.append("Original Request Type: " + req.getHeader().getPhotoHeader().getRequestType()+"\n");
    	sb.append("Respond Flag: " + req.getHeader().getPhotoHeader().getResponseFlag()+"\n");
    	sb.append("UUID: " + req.getBody().getPhotoPayload().getUuid()+"\n");
    	sb.append("Image Name: " + req.getBody().getPhotoPayload().getName()+"\n");
    	
    	System.out.println(sb.toString());
    	
		
    	
    	

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
