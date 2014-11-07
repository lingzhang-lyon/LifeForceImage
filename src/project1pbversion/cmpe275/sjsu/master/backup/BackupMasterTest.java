package project1pbversion.cmpe275.sjsu.master.backup;

import static org.junit.Assert.*;

import org.junit.Test;

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
		System.out.println("Start  sent heartbeat from backup master");
		Heartbeat hb=BackupMaster.createHeartbeat(backupMasterHost, backupMasterPort, 0);
		BackupMaster.createChannelAndSendHeartbeat(primaryMasterHost, primaryMasterPort, hb);
	}

}
