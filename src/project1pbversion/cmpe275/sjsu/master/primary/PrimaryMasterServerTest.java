package project1pbversion.cmpe275.sjsu.master.primary;

import static org.junit.Assert.*;

import org.junit.Test;

import project1pbversion.cmpe275.sjsu.conf.Configure;

public class PrimaryMasterServerTest {

	@Test
	public void test() { //use default configure to test
		int port=Configure.MasterPortForClient;
    	boolean saveToLocal=true;
    	boolean usePartition=false;
    	boolean useBackupMaster=false;
		boolean passFailedRequestToOtherCluster=false;
		boolean dummyTestForMasterHandler=false;
		
		System.out.println("Your configure: "
				+ "\nport= "+port
				+ "\nsaveToLocal= "+ saveToLocal
				+ "\nusePartition= "+ usePartition
				+ "\nusebackupMaster =" + useBackupMaster
				+ "\npassFailedRequestToOtherCluster= "+ passFailedRequestToOtherCluster
				+ "\ndummyTestForMasterHandler= "+dummyTestForMasterHandler 
				);
    	
    	PrimaryMasterServer.startMasterServer(port,saveToLocal, usePartition, useBackupMaster,
    			passFailedRequestToOtherCluster,dummyTestForMasterHandler );
    	
	}

}
