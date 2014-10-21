package project1.cmpe275.sjsu;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map.Entry;

public class HttpUploadClientTest {
	
    static final String BASE_URL = System.getProperty("baseUrl", "http://127.0.0.1:8080/");
    //static final String FILE = System.getProperty("file", "/Users/lingzhang/Desktop/datadir/testDataParsed.json");
    static final String FILE = System.getProperty("file", "/Users/lingzhang/Desktop/test1.jpeg");
    
    public static void main(String[] args) throws Exception{
    	String postSimple, postFile, get;
    	
    	if (BASE_URL.endsWith("/")) {
            postSimple = BASE_URL + "formpost";
            postFile = BASE_URL + "formpostmultipart";
            get = BASE_URL + "formget";
        } else {
            postSimple = BASE_URL + "/formpost";
            postFile = BASE_URL + "/formpostmultipart";
            get = BASE_URL + "/formget";
        }
    	
        URI uriSimple = new URI(postSimple);
        String scheme = uriSimple.getScheme() == null? "http" : uriSimple.getScheme();
        String host = uriSimple.getHost() == null? "127.0.0.1" : uriSimple.getHost();
        int port = uriSimple.getPort();
        
        
        URI uriFile = new URI(postFile);
        File file = new File(FILE);
        if (!file.canRead()) {
            throw new FileNotFoundException(FILE);
        }
        
        final boolean ssl = "https".equalsIgnoreCase(scheme);
        final SslContext sslCtx;
        if (ssl) {
            sslCtx = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
        } else {
            sslCtx = null;
        }

        
        // Configure the client.
        EventLoopGroup group = new NioEventLoopGroup();

        // setup the factory: here using a mixed memory/disk based on size threshold
        HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk if MINSIZE exceed

        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory
        
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(new HttpUploadClientIntializer(sslCtx));

            // Simple Get form: no factory used (not usable)
            List<Entry<String, String>> headers = project1.cmpe275.sjsu.HttpUploadClient.formget(b, host, port, get, uriSimple);
            if (headers == null) {
                factory.cleanAllHttpDatas();
                return;
            }

            // Simple Post form: factory used for big attributes
            List<InterfaceHttpData> bodylist = project1.cmpe275.sjsu.HttpUploadClient.formpost(b, host, port, uriSimple, file, factory, headers);
            if (bodylist == null) {
                factory.cleanAllHttpDatas();
                return;
            }

            // Multipart Post form: factory used
            project1.cmpe275.sjsu.HttpUploadClient.formpostmultipart(b, host, port, uriFile, factory, headers, bodylist);
        } finally {
            // Shut down executor threads to exit.
            group.shutdownGracefully();

            // Really clean all temporary files if they still exist
            factory.cleanAllHttpDatas();
        }
    
    
    }
    	
    

    
}
