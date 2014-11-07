package project1pbversion.cmpe275.sjsu.master.backup;

import static org.junit.Assert.*;

import org.junit.Test;

import project1pbversion.cmpe275.sjsu.master.MasterController;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Heartbeat;

public class BackupMasterTest {

	@Test
	public void test() throws Exception {
		String primaryMasterHost="127.0.0.1";
		int primaryMasterPort =7070;
		String backupMasterHost="127.0.0.1";
		int backupMasterPort =7070;
		Heartbeat hb=BackupMaster.createHeartbeat(backupMasterHost, backupMasterPort, 0);
		BackupMaster.createChannelAndSendHeartbeat(primaryMasterHost, primaryMasterPort, hb);
	}
	
	public static void testBackupMaster() throws Exception {
		String primaryMasterHost="127.0.0.1";
		int primaryMasterPort =7070;
		String backupMasterHost="127.0.0.1";
		int backupMasterPort =7070;
		System.out.println("Start  sent heartbeat from backup to primary master ");
		Heartbeat hb=BackupMaster.createHeartbeat(backupMasterHost, backupMasterPort, 0);
		BackupMaster.createChannelAndSendHeartbeat(primaryMasterHost, primaryMasterPort, hb);
	}
	
	public static void dummyWorkingTest(){
		System.out.println("Start sent heartbeat from backup master to primary master ");
		System.out.println("Got heartbeat from primary master to backup master");
	}
	
	public static void dummyFailTest(){
		System.out.println("Start sent heartbeat from backup master to primary master ");
		System.out.println("Not Got heartbeat from primary master to backup master");
		 MasterController.setBackupMaster(false);
	     MasterController.setPrimaryMaster(true);
	}

}
