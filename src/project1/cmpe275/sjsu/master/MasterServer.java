package project1.cmpe275.sjsu.master;

/**
 * This class is for receiving the msg from Client and finish task accordingly
 * if msg is just a connect request, send back connection information
 * 
 * if msg is a POST request (save image, the msg contain image object), find proper SlaveServers, and sent new msg (contain image object) to them.
 * then receive information sent back from SlaveServer, and store the metadata in the local database on MasterServer.
 *
 */
public class MasterServer {

}
