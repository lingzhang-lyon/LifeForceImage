package project1pbversion.cmpe275.sjsu.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import org.junit.Test;

public class ClientTest {

	@Test
	public void testForRead() throws InterruptedException{
		
		//for read
    	//uuid for multiread same pic
    	String uuid= Client.createUuid("testPic", "ling");
		long threadid=Thread.currentThread().getId();
		uuid=uuid+threadid;
		uuid="20141105_015423_ling_testPic11";
		ClientHandler.setEnableSaveOption(false);
		multiThreadTest(2,uuid) ; 
		
	}
	
	@Test
	public void testForBoth() throws InterruptedException{
		//for both
		multiThreadTest(1,null) ; 
	}	
	
	@Test
	public void testForWrite() throws InterruptedException{
		//for write
		multiThreadTest(3, null) ; 
	}
	
	
	@SuppressWarnings("resource")
	public void multiThreadTest(int type, String uuid) throws InterruptedException {
		System.out.println("***********MultiThreadTest********\nMain ThreadId: " + Thread.currentThread().getId());
		long start = System.currentTimeMillis();
		System.out.println("Start at:"+new Date(start));
		Scanner reader0 = new Scanner(System.in);
		System.out.println("How many thread you want to test for multiple users");
    	int numberOfSubThread=reader0.nextInt();
    	

    	
    	
	    ArrayList<Thread> threadpool= new ArrayList<Thread>();
	    for (int i =0; i<numberOfSubThread; i++){
	    	Thread thread=null;
	    	if(type==1){
	    		thread=new Thread(new OneThreadClientTest()); //for both
	    	}
	    	else if(type==2){
	    		
	    		thread=new Thread(new OneThreadClientReadTest(uuid)); //for multi read for same pic
	    	}
	    	else if(type==3){
	    		thread=new Thread(new OneThreadClientWriteTest()); //for write
	    	}
	    	threadpool.add(thread);	    	
	    }
	    
	    for(Thread thread: threadpool){
	    	thread.start();
	    }
	    
	    for(Thread thread: threadpool){
	    	thread.join();
	    }
	    
	    long end = System.currentTimeMillis();
		System.out.println("End at:"+new Date(end));
		System.out.println("many small query finished, time taken:" + (end-start)+" milliseconds");
		System.out.println("Number of sub threads : "+ numberOfSubThread);

	}
	

	
	public class OneThreadClientTest implements Runnable{

		@Override
		public void run() {
			
			String uuid= Client.createUuid("testPic", "ling");
			long subthreadid=Thread.currentThread().getId();
			uuid=uuid+subthreadid;
			
			try {
				Client.testPost(Client.HOST,Client.PORT, uuid, Client.filePath);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				Client.testGet(Client.HOST,Client.PORT, uuid);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	public class OneThreadClientReadTest  implements Runnable  { //for read request for specific uuid
		
		private String uuid=null;
		public OneThreadClientReadTest (String uuid){
			super();
			this.uuid=uuid;
		}
		
		@Override
		public void run()  {
			
			
			try {
				Client.testGet(Client.HOST,Client.PORT, uuid);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	public class OneThreadClientWriteTest  implements Runnable  {
		
		@Override
		public void run()  {
			String uuid= Client.createUuid("testPic", "ling");
			long subthreadid=Thread.currentThread().getId();
			uuid=uuid+subthreadid;
			
			try {
				Client.testPost(Client.HOST,Client.PORT, uuid, Client.filePath);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

}
