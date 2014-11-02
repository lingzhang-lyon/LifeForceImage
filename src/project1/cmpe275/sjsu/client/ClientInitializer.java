package project1.cmpe275.sjsu.client;

import project1.cmpe275.sjsu.example.HttpUploadClientHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class ClientInitializer extends ChannelInitializer<SocketChannel>{
	
	@Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        
        pipeline.addLast("codec", new HttpClientCodec());


        // to be used since huge file transfer
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());

        pipeline.addLast("handler", new HttpUploadClientHandler());
    }
	

}
