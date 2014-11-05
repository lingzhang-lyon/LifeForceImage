package project1pbversion.cmpe275.sjsu.master;

import static org.junit.Assert.*;

import org.junit.Test;

import project1.cmpe275.sjsu.conf.Configure;

public class MasterServerTest {

	@Test
	public void test() { //use default configure to test
		int port=Configure.MasterPortForClient;
    	boolean saveToLocal=true;
    	boolean usePartition=false;
		boolean passFailedRequestToOtherCluster=false;
		boolean dummyTestForMasterHandler=false;
		System.out.println("Your configure: "
				+ "\nport= "+port
				+ "\nsaveToLocal= "+ saveToLocal
				+ "\nusePartition= "+ usePartition
				+ "\npassFailedRequestToOtherCluster= "+ passFailedRequestToOtherCluster
				+ "\ndummyTestForMasterHandler= "+dummyTestForMasterHandler 
				);
    	
    	MasterServer.startMasterServer(port,saveToLocal, usePartition, 
    			passFailedRequestToOtherCluster,dummyTestForMasterHandler );
    	
	}

}
