package project1pbversion.cmpe275.sjsu.model;
import java.net.*;
import java.util.*;

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
//get mac address
public String getMacAddr() {  
       String MacAddr = "";
       String str = "";
       try {
           NetworkInterface NIC = NetworkInterface.getByName("eth0");
           byte[] buf = NIC.getHardwareAddress();
           for (int i = 0; i < buf.length; i++) {
               str = str + byteHEX(buf[i]);
           }
           MacAddr = str.toUpperCase();
       } catch (SocketException e) {
           e.printStackTrace();
           System.exit(-1);
       }
       return MacAddr;
   }
   
//get local machine ip address
public String getLocalIP() {
       String ip = "";
       try {
           Enumeration<?> e1 = (Enumeration<?>) NetworkInterface.getNetworkInterfaces();
           while (e1.hasMoreElements()) {
               NetworkInterface ni = (NetworkInterface) e1.nextElement();
               if (!ni.getName().equals("eth0")) {
                   continue;
               } else {
                   Enumeration<?> e2 = ni.getInetAddresses();
                   while (e2.hasMoreElements()) {
                       InetAddress ia = (InetAddress) e2.nextElement();
                       if (ia instanceof Inet6Address)
                           continue;
                       ip = ia.getHostAddress();
                   }
                   break;
               }
           }
       } catch (SocketException e) {
           e.printStackTrace();
           System.exit(-1);
       }
       return ip;
   }

private static String byteHEX(byte ib) {
       char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a','b', 'c', 'd', 'e', 'f' };
       char[] ob = new char[2];
       ob[0] = Digit[(ib >>> 4) & 0X0F];
       ob[1] = Digit[ib & 0X0F];
       String s = new String(ob);
       return s;
   }
	
}
