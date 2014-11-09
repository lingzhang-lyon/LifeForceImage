package project1pbversion.cmpe275.sjsu.master.primary;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Scanner;

import project1.cmpe275.sjsu.conf.Configure;
import project1.cmpe275.sjsu.model.Socket;


public class PrimaryMasterServer {
    static final int PortForClient = Configure.MasterPortForClient;
      
    @SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
        
    	System.out.println("Start Master Server***************\n");
    	
		Scanner reader0 = new Scanner(System.in);
    	System.out.println("a.Just simple test(use static configure)  | b. Input your own configure:");
    	String input=reader0.nextLine();
    	int port=PortForClient;
    	boolean saveToLocal=true;
    	boolean usePartition=false;
    	boolean useBackupMaster=false;
		boolean passFailedRequestToOtherCluster=false;
		boolean dummyTestForMasterHandler=false;
		
    	if (input.equals("b")){ //input own configure
    	
	    	System.out.println("enter the port open for client, like: 8080");
	    	port =reader0.nextInt();
	    	Scanner reader = new Scanner(System.in);
	    	
	    	
	    	System.out.println("Do you want to just dummy Test For MasterHandler? (Y/N) ");
	    	String res0=reader.nextLine();
	    	if(res0.equals("Y")||res0.equals("y")){
	    		dummyTestForMasterHandler=true;
	    	}else{
	    		dummyTestForMasterHandler=false;
	    		
	    		System.out.println("Do you want to save received file to master server local file system? (Y/N) ");
		    	String res1=reader.nextLine();
		    	if(res1.equals("Y")||res1.equals("y")){
		    		saveToLocal=true;
		    	}else{
		    		saveToLocal=false;
		    	}
	    	
		    	System.out.println("Do you want to use partion? (Y/N) ");
		    	String res2=reader.nextLine();
		    	if(res2.equals("Y")||res2.equals("y")){
		    		usePartition=true;
		    	}else{
		    		usePartition=false;
		    	}
		    	
		    	System.out.println("Do you want to use Backup Master? (Y/N) ");
		    	String res3=reader.nextLine();
		    	if(res3.equals("Y")||res3.equals("y")){
		    		useBackupMaster=true;
		    		Scanner reader1 = new Scanner(System.in);
		    		System.out.println("Input mongohost of backup master: like 127.0.0.1");
		    		String backupmongohost=reader1.nextLine();
		    		System.out.println("Input mongoport of backup master: like 27017");
		    		int backupmongoport=reader1.nextInt();
		    		Socket backupMongoSocket=new Socket(backupmongohost, backupmongoport);
		    		PrimaryMasterServerHandler.setBackupMongoSocket(backupMongoSocket);
		    	}else{
		    		useBackupMaster=false;
		    	}
		    	
		    	
		    	System.out.println("Do you want to pass Failed Request To Other Cluster? (Y/N) ");
		    	String res4=reader.nextLine();
		    	if(res4.equals("Y")||res4.equals("y")){
		    		passFailedRequestToOtherCluster=true;
		    	}else{
		    		passFailedRequestToOtherCluster=false;
		    	}
	    	}	
	    	    	
    	}
    	
    	System.out.println("Your configure: "
				+ "\nport= "+port
				+ "\nsaveToLocal= "+ saveToLocal
				+ "\nusePartition= "+ usePartition
				+ "\nusebackupMaster =" + useBackupMaster
				+ "\npassFailedRequestToOtherCluster= "+ passFailedRequestToOtherCluster
				+ "\ndummyTestForMasterHandler= "+dummyTestForMasterHandler 
				);
    	
    	startMasterServer(port,saveToLocal, usePartition, useBackupMaster,
    			passFailedRequestToOtherCluster,dummyTestForMasterHandler );
				
    }
    
    public static void startMasterServer(int portForClient, boolean saveToLocal, boolean usePartition, boolean useBackupMaster, 
    						boolean passFailedRequestToOtherCluster, boolean dummyTestForMasterHandler){
    	 
    	
    	 EventLoopGroup bossGroup = new NioEventLoopGroup(1);
         EventLoopGroup workerGroup = new NioEventLoopGroup();
         try {

        	 PrimaryMasterServerHandler.setSaveToLocal(saveToLocal);
        	 PrimaryMasterServerHandler.setUsePartition(usePartition);
        	 PrimaryMasterServerHandler.setPassFailedRequestToOtherCluster(passFailedRequestToOtherCluster);
        	 PrimaryMasterServerHandler.setDummyTestForMasterHandle(dummyTestForMasterHandler);
        	 
        	 
             ServerBootstrap b = new ServerBootstrap();
             b.group(bossGroup, workerGroup);
             b.channel(NioServerSocketChannel.class);
             b.handler(new LoggingHandler(LogLevel.INFO));
             boolean compress=false;            
             b.childHandler(new PrimaryMasterServerInitializer(compress));  //false means no compression

             Channel ch = b.bind(portForClient).sync().channel();


             ch.closeFuture().sync();
             
         } catch (Exception ex) {
         	System.err.println("Failed to setup channel for client");
         }finally {
             bossGroup.shutdownGracefully();
             workerGroup.shutdownGracefully();
         }
    	
    	
    }
    

    
}
