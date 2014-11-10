package project1pbversion.cmpe275.sjsu.master;

import java.util.Scanner;

import org.junit.Test;

import project1.cmpe275.sjsu.conf.Configure;
import project1.cmpe275.sjsu.heartbeatserver.HeartbeatServer;
import project1.cmpe275.sjsu.model.Socket;
import project1pbversion.cmpe275.sjsu.master.backup.BackupMaster;
import project1pbversion.cmpe275.sjsu.master.primary.PrimaryMasterServer;
import project1pbversion.cmpe275.sjsu.master.primary.PrimaryMasterServerHandler;
import project1pbversion.cmpe275.sjsu.master.primary.listenbackup.PrimaryListener;


public class MasterControllerV2 {
	
	private static boolean primaryMaster=true;
	private static boolean backupMaster=false;
	
	
	public static boolean isPrimaryMaster() {
		return primaryMaster;
	}

	public static void setPrimaryMaster(boolean primaryMaster) {
		MasterControllerV2.primaryMaster = primaryMaster;
	}
	
	public static boolean isBackupMaster() {
		return backupMaster;
	}

	public static void setBackupMaster(boolean backupMaster) {
		MasterControllerV2.backupMaster = backupMaster;
	}
	
	@Test
	@SuppressWarnings("resource")
	public void test() throws Exception{
		
		
		Scanner reader0 = new Scanner(System.in);
    	System.out.println("Please choose this is a. Primary Master | b. Backup Master");
    	String backupOrPrimary=reader0.nextLine();
    	if(backupOrPrimary.equals("b")||backupOrPrimary.equals("B")){
    		backupMaster=true;
    	}
		boolean serverStarted=false;
		while (backupMaster){
			if(!serverStarted){
				serverStarted=true;
				try{
					BackupMaster.main(null);
					//when backupMasterHandler could not receive heartbeat, will set MasterHandlerV2.backupMaster=false;
				}catch(Exception e){
					System.out.println("\nThere is something wrong with send/ receive heartbeat to primary master");
					break;
				}
			}
			
		}
		System.out.println("\nIt is not a backup master now, Set to primary already, PrimaryMaster= " + primaryMaster);		

		startPrimaryMaster();
		
	}
	
	@SuppressWarnings("resource")
	private void startPrimaryMaster() throws InterruptedException {
		System.out.println("Start Master Server***************\n");
    	
		Scanner reader0 = new Scanner(System.in);
    	System.out.println("a.Just simple test(use static configure)  | b. Input your own configure:");
    	String input=reader0.nextLine();
    	int masterPortForClient=Configure.MasterPortForClient;
    	int heartbeatPortForSlave=Configure.MasterPortForSlave;
    	int masterPortForBackup=Configure.MasterPortForBackup;
    	String backupmongohost=Configure.MongoHost;
    	int backupmongoport=Configure.MongoPort;
    	boolean saveToLocal=true;
    	boolean usePartition=false;
    	boolean useBackupMaster=false;
		boolean passFailedRequestToOtherCluster=false;
		boolean dummyTestForMasterHandler=false;
		
    	if (input.equals("b")){ //input own configure
    	
	    	System.out.println("enter the port open for client, like: 8080");
	    	masterPortForClient =reader0.nextInt();
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
		    		Scanner reader1 = new Scanner(System.in);
		    		System.out.println("Input port for listen heatbeat from slave: like 9090");
		    		heartbeatPortForSlave=reader1.nextInt();
		    		
		    	}else{
		    		usePartition=false;
		    	}
		    	
		    	System.out.println("Do you want to use Backup Master? (Y/N) ");
		    	String res3=reader.nextLine();
		    	if(res3.equals("Y")||res3.equals("y")){
		    		useBackupMaster=true;
		    		Scanner reader5 = new Scanner(System.in);
		    		System.out.println("Input port for listen heatbeat from backup: like 7070");
		    		masterPortForBackup=reader5.nextInt();
		    		Scanner reader1 = new Scanner(System.in);
		    		System.out.println("Input mongohost of backup master: like 127.0.0.1");
		    		backupmongohost=reader1.nextLine();
		    		System.out.println("Input mongoport of backup master: like 27017");
		    		backupmongoport=reader1.nextInt();
		    		Socket backupMongoSocket=new Socket(backupmongohost, backupmongoport);
		    		PrimaryMasterServerHandler.setBackupMongoSocket(backupMongoSocket);
		    		PrimaryMasterServerHandler.setUseBackupMaster(true);
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
    	
    	
    	StringBuilder sb=new StringBuilder();
    	sb.append("Your configure: ");
    	sb.append("\nportForClient= "+masterPortForClient);
    	sb.append("\nsaveToLocal= "+ saveToLocal);
    	sb.append("\nusePartition= "+ usePartition);
    	if(usePartition){
    		sb.append("\n  heartbeatPortForSlave= "+ heartbeatPortForSlave);
    	}
    	sb.append("\nusebackupMaster =" + useBackupMaster);
    	if(useBackupMaster){
    		sb.append("\n  masterPortForBackup= "+masterPortForBackup);
    		sb.append("\n  backupmongohost "+ backupmongohost);
    		sb.append("\n  backupmongoport= "+backupmongoport);
    	}
    	sb.append("\npassFailedRequestToOtherCluster= "+ passFailedRequestToOtherCluster);
    	sb.append("\ndummyTestForMasterHandler= "+dummyTestForMasterHandler );
    	
    	System.out.println(sb.toString());
    	
		Thread t1= new Thread( new PrimaryListenerThread(masterPortForBackup)) ; //for backup heartbeat
		Thread t2 =new Thread( new PrimaryHeatbeatForSlaveThread(heartbeatPortForSlave)); //for slave heartbeat
    	
		if(usePartition){
    		t2.start();
    		Thread.sleep(1000);
    	}
    	if(useBackupMaster){
    		t1.start();
    		Thread.sleep(1000);
    	}
    	
    	System.out.println("Start the Primary Master Server for Client********" );
    	PrimaryMasterServer.startMasterServer(masterPortForClient,saveToLocal, usePartition, useBackupMaster,
    			passFailedRequestToOtherCluster,dummyTestForMasterHandler );
		
	}


	

	
	
	
	public class PrimaryListenerThread implements Runnable{

		private int portForBackup=7070;
		
		public PrimaryListenerThread (int portForBackup){
			super();
			this.portForBackup=portForBackup;
		}
		
		public PrimaryListenerThread(){
			
		}
		
		@Override
		public void run() {
			System.out.println("Start the Primary Listener to listen heartbeat from backup******** ");
			//PrimaryListener.main(null); //just for test, 
			
			PrimaryListener.startPrimaryMasterListener(portForBackup); 
			// will be used in real, because once promote to primary, 
			//it should set the port automatically
		}
		
	}
	
	public class PrimaryHeatbeatForSlaveThread implements Runnable{
		
		private int portForSlave=9090;
		
		
		public PrimaryHeatbeatForSlaveThread(int portForSlave) {
			super();
			this.portForSlave = portForSlave;
		}
		

		public PrimaryHeatbeatForSlaveThread() {
			
		}


		@Override
		public void run() {
			try {
				
				System.out.println("Start the Primary Heartbeat server for slave******** ");
				//HeartbeatServer.main(null); //just for test 
				
				HeartbeatServer.startHeartbeatServer(portForSlave);  //with default port 9090
				// will be used in real, because once promote to primary, 
				//it should set the parameters automatically according to the previous primary master
				//TODO 
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	public class PrimaryMasterServerThread implements Runnable{

		@Override
		public void run() {
			try {
				
				PrimaryMasterServer.main(null); //just for test
				
				//PrimaryMasterServer.startMasterServer(portForClient, saveToLocal, usePartition, useBackupMaster, passFailedRequestToOtherCluster, dummyTestForMasterHandler);
				// will be used in real, because once promote to primary, 
				//it should set the parameters automatically according to the previous primary master
				//TODO so the heatbeat between primary and backup should store the parameters of primary master
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
	

}
