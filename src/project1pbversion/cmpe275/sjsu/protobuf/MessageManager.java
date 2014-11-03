package project1pbversion.cmpe275.sjsu.protobuf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
}
