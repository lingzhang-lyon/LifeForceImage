package project1.cmpe275.sjsu.slave;

/**
 * This class is to receive msg from MasterServer 
 * 
 * if msg is just a connect request, send back connection information
 * 
 * if msg is a Get request (search request), it should find the image according provided information (name, userid), 
 * and send back all the image object back to MasterServer;
 * 
 * if msg is a Post request (save image), it should save the file to the database in SlaveServer and return the metadata information to MasterServer
 * 
 *
 */
public class SlaveServer {

}
