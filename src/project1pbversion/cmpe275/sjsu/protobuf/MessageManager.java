package project1pbversion.cmpe275.sjsu.protobuf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

import project1.cmpe275.sjsu.model.Image;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Header;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Payload;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoPayload;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.Request;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.RequestType;
import project1pbversion.cmpe275.sjsu.protobuf.ImagePB.PhotoHeader.ResponseFlag;

import com.google.protobuf.ByteString;

public class MessageManager {
	
	public static Request createResponseRequest(Image img, ResponseFlag res, RequestType reqtype) throws Exception{
		
		PhotoPayload.Builder ppb = PhotoPayload.newBuilder()
	 			.setUuid(img.getUuid())
	 			.setName(img.getImageName());
		
		if(reqtype.equals(RequestType.write)){			
			//add image file data to photoPayload
	        ByteString data=convertFileToByteString(img.getFile());
			ppb.setData(data);
			
		}
		PhotoPayload pp=ppb.build();
		Payload p=Payload.newBuilder().setPhotoPayload(pp).build();
		
		
		PhotoHeader ph= PhotoHeader.newBuilder()
				 		.setRequestType(reqtype)
				 		.build();	         	      	       	    	 
		Header h=Header.newBuilder().setPhotoHeader(ph).build();
		
		
		Request request=Request.newBuilder()
				 				.setHeader(h)
				 				.setBody(p)
				 				.build();
	    
		return request;
	}
	
	public static Request createSampleRequest(String uuid, RequestType reqtype) throws Exception{
		
		PhotoPayload.Builder ppb = PhotoPayload.newBuilder()
	 			.setUuid(uuid);
		PhotoPayload pp=ppb.build();
		Payload p=Payload.newBuilder().setPhotoPayload(pp).build();
		
		
		PhotoHeader ph= PhotoHeader.newBuilder()
				 		.setRequestType(reqtype)
				 		.build();	         	      	       	    	 
		Header h=Header.newBuilder().setPhotoHeader(ph).build();
		
		
		Request request=Request.newBuilder()
				 				.setHeader(h)
				 				.setBody(p)
				 				.build();
	    
		return request;
	}
	

	public static ByteString convertFileToByteString(File file) throws Exception{
		 @SuppressWarnings("resource")
		FileInputStream f = new FileInputStream(file);           
        byte b[] = new byte[f.available()];
        f.read(b);
        ByteString data=ByteString.copyFrom( b);
        return data;
	}
	
	@SuppressWarnings("resource")
	public static void writeByteStringToFile(ByteString data, File file) throws Exception {
		byte c[]=data.toByteArray();
		FileOutputStream fout = new FileOutputStream(file);
        fout.write(c);
        fout.flush();
		
	}
	
	public static File createFile(String picname, String path) {
		 String uploadTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());  // Time stamp
		 String storedName = uploadTime  +"_"+ picname;  
		 File dest = new File(path + storedName); 
		 return dest;
	}
	
	
	
	public static void handleResponse(Request req) throws Exception{
		RequestType type = req.getHeader().getPhotoHeader().getRequestType();
		ResponseFlag reFlag=req.getHeader().getPhotoHeader().getResponseFlag();
		String uuid = req.getBody().getPhotoPayload().getUuid();
		String picname=  req.getBody().getPhotoPayload().getName();
		
    	
    	StringBuilder sb= new StringBuilder();
    	sb.append("Original Request Type: " + type+"\n");
    	sb.append("Respond Flag: " + reFlag+"\n");
    	sb.append("UUID: " + uuid +"\n");
    	sb.append("Image Name: " +picname+"\n");
    	System.out.println(sb.toString());
    	
    	if(type.equals(RequestType.read) && reFlag.equals(ResponseFlag.success)){
	    	ByteString data=req.getBody().getPhotoPayload().getData();   	
			System.out.println("Received data:"+data.toString());
    	
	    	@SuppressWarnings("resource")
			Scanner reader = new Scanner(System.in);
	    	
	    	boolean tosave;
	    	do{
		        System.out.println("Do you want to save file to local file system? (Y/N)");
		        String saveToLocal=reader.nextLine();
		        if(saveToLocal.equals("Y")||saveToLocal.equals("y")){
		        	tosave=true;
		        	System.out.println("Please input the path you want to save the picture");
		        	System.out.println("Like: /Users/lingzhang/Desktop/");
		        	String savePath=reader.nextLine();
		        	try{
						File file=MessageManager.createFile("feedback_"+req.getBody().getPhotoPayload().getName(),savePath);
						MessageManager.writeByteStringToFile(data,file);
						System.out.println("The file has been saved to your local file system");
						tosave=false;
		        	}catch(Exception e){
		        		System.out.println("somthing wrong, maybe your path is not correct?");
		        		continue;
		        	}
		        }else{
		        	tosave=false;
		        	System.out.println("Goodbye!");
		        }
	    	}while(tosave);
    	}
    	
	}
}
