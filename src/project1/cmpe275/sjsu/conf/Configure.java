package project1.cmpe275.sjsu.conf;

public class Configure {
	
	public static final String desPath="/Users/lingzhang/Desktop/";
	//public static final String desPath="~/Desktop/";
	static final boolean SSL = System.getProperty("ssl") != null;
	public static final int MasterPortForClient = Integer.parseInt(System.getProperty("port", SSL? "8443" : "8080"));
	public static final int MasterPortForSlave= 9090;
	
	public static final String clientFilePath="/Users/lingzhang/Desktop/test1.jpeg";
	public static final String HOST = "127.0.0.1";
	public static final int PORT = 8080;
	public static final String BASE_URL = System.getProperty("baseUrl", "http://127.0.0.1:8080/");
	public static final String USERNAME ="tester";
	public static final String PICNAME ="myclientpic.jpeg";
	public static final String CAT ="clientfood";
	
	// configuration for slave
	public static final int SLAVE_PORT = 10010;

}
