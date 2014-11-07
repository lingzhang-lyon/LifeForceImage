package project1pbversion.cmpe275.sjsu.master;

import project1pbversion.cmpe275.sjsu.master.backup.BackupMasterTest;


public class MasterController {
	
	private static boolean PrimaryMaster=true;
	private static boolean backupMaster=false;
	
	
	public static boolean isPrimaryMaster() {
		return PrimaryMaster;
	}

	public static void setPrimaryMaster(boolean primaryMaster) {
		PrimaryMaster = primaryMaster;
	}
	
	public static boolean isBackupMaster() {
		return backupMaster;
	}

	public static void setBackupMaster(boolean backupMaster) {
		MasterController.backupMaster = backupMaster;
	}
	
	
	public static void main(String[] args) throws Exception{
		backupMaster=true;
		boolean serverStarted=false;
		while (backupMaster){
			//dummy test do nothing
			if(!serverStarted){
				BackupMasterTest.testBackupMaster();
			}
		}
		System.out.println("It is not a backup master now, promoted to primary already");
		
	}

}
