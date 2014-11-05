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
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;
import project1pbversion.cmpe275.sjsu.protobuf.MessageManager;

public class ClientHandler extends SimpleChannelInboundHandler<Request> {

	private static boolean enableSaveOption=true;

	@Override
    public void channelRead0(ChannelHandlerContext ctx, Request req) throws Exception {
		System.out.println("\nGot the feedback from Server-------");
		MessageManager.handleResponse(req,enableSaveOption);
    	

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

	public boolean isEnableSaveOption() {
		return enableSaveOption;
	}

	public static void setEnableSaveOption(boolean enableSaveOption) {
		ClientHandler.enableSaveOption = enableSaveOption;
	}
    
    
    
}
