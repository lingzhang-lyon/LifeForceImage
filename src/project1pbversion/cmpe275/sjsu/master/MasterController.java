package project1pbversion.cmpe275.sjsu.master;

import java.util.Scanner;

import project1pbversion.cmpe275.sjsu.master.backup.BackupMaster;
import project1pbversion.cmpe275.sjsu.master.backup.BackupMasterTest;
import project1pbversion.cmpe275.sjsu.master.primary.PrimaryMasterServer;
import project1pbversion.cmpe275.sjsu.master.primary.PrimaryMasterServerTest;
import project1pbversion.cmpe275.sjsu.master.primary.listenbackup.PrimaryListener;

import org.junit.Test;


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
		int portForBackup=7070;
		int portForClient=8080;
		startPrimaryMasterAndPrimaryListener(portForBackup, portForClient);
		
	}
	
	public void startPrimaryMasterAndPrimaryListener(int portForBackup, int portForClient) throws Exception{
		
		Thread t1= new Thread( new PrimaryListenerThread(portForBackup)) ;
		Thread t2= new Thread( new PrimaryServerThread()) ;
		
		t1.start();
		t2.start();
		
		t1.join();
		t1.join();
		
	}
	
	public class PrimaryListenerThread implements Runnable{

		private int portForBackup;
		
		public PrimaryListenerThread (int portForBackup){
			super();
			this.portForBackup=portForBackup;
		}
		@Override
		public void run() {
			PrimaryListener.startPrimaryMasterListener(portForBackup);
		}
		
	}
	
	public class PrimaryServerThread implements Runnable{

		@Override
		public void run() {
			try {
				PrimaryMasterServer.main(null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	

}
