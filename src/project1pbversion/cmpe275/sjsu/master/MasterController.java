package project1pbversion.cmpe275.sjsu.master;

import java.util.Scanner;

import org.junit.Test;

import project1.cmpe275.sjsu.heartbeatserver.HeartbeatServer;
import project1pbversion.cmpe275.sjsu.master.backup.BackupMaster;
import project1pbversion.cmpe275.sjsu.master.primary.PrimaryMasterServer;
import project1pbversion.cmpe275.sjsu.master.primary.listenbackup.PrimaryListener;


public class MasterController {
	
	private static boolean primaryMaster=true;
	private static boolean backupMaster=false;
	
	
	public static boolean isPrimaryMaster() {
		return primaryMaster;
	}

	public static void setPrimaryMaster(boolean primaryMaster) {
		MasterController.primaryMaster = primaryMaster;
	}
	
	public static boolean isBackupMaster() {
		return backupMaster;
	}

	public static void setBackupMaster(boolean backupMaster) {
		MasterController.backupMaster = backupMaster;
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
			//dummy test do nothing
			if(!serverStarted){
				serverStarted=true;
				try{
					BackupMaster.main(null);
					//BackupMasterTest.testBackupMaster();
					//BackupMasterTest.dummyWorkingTest();
					//BackupMasterTest.dummyFailTest();
				}catch(Exception e){
					System.out.println("\nThere is something wrong with send/ receive heartbeat to primary master");
					break;
				}
			}
			
		}
		System.out.println("\nIt is not a backup master now, Set to primary already, PrimaryMaster= " + primaryMaster);		
		//int portForBackup=7070;
		//int portForClient=8080;
		//startPrimaryMasterAndPrimaryListener(portForBackup, portForClient);
		startPrimaryAllWithDefaultSetMultiThread();
		
	}
	
	public void startPrimaryMasterAndPrimaryListener(int portForBackup, int portForClient) throws Exception{
		
		Thread t1= new Thread( new PrimaryListenerThread(portForBackup)) ;
		Thread t2= new Thread( new PrimaryMasterServerThread()) ;
		
		t1.start();
		t2.start();
		
		t1.join();
		t1.join();
		
	}
	
	public void startPrimaryAllWithDefaultSetMultiThread() throws Exception{
		
		Thread t1= new Thread( new PrimaryListenerThread()) ; //for backup heartbeat
		Thread t2 =new Thread( new PrimaryHeatbeatForSlaveThread()); //for slave heartbeat
		 
		Thread t3= new Thread( new PrimaryMasterServerThread()) ; //for primary master
		
		
		t1.start();
		Thread.sleep(1000);
		t2.start();
		Thread.sleep(1000);
		t3.start();
		
		t1.join();
		t2.join();
		t3.join();
		
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
			System.out.println("Start the Primary Listener******** \nwith default prot: "+ portForBackup
					+" to listen heartbeat from backup");
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
