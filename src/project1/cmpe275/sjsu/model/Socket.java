package project1.cmpe275.sjsu.model;

public class Socket {
	private String ip=null;
	private int port=0;
	
	public Socket(){
		
	}
	
	
	public Socket(String ip, int port) {
		super();
		this.ip = ip;
		this.port = port;
	}


	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
}
