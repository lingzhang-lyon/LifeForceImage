package project1.cmpe275.sjsu.master;

public class MenuManager {
//	private final StringBuilder responseContent = new StringBuilder();
	
	public static StringBuilder createMenu(StringBuilder sb){
		// print several HTML forms
        // Convert the response content to a ChannelBuffer.
		
		sb.setLength(0);

        // create Pseudo Menu
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<title>Netty Test Form</title>\r\n");
        sb.append("</head>\r\n");
        sb.append("<body bgcolor=white><style>td{font-size: 12pt;}</style>");

        sb.append("<table border=\"0\">");
        sb.append("<tr>");
        sb.append("<td>");
        sb.append("<h1>Netty Test Form</h1>");

        sb.append("</td>");
        sb.append("</tr>");
        sb.append("</table>\r\n");


        // POST with enctype="multipart/form-data"
        sb.append("<CENTER>POST MULTIPART FORM<HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");
        sb.append("<FORM ACTION=\"/formpostmultipart\" ENCTYPE=\"multipart/form-data\" METHOD=\"POST\">");
        sb.append("<input type=hidden name=getform value=\"POST\">");
        sb.append("<table border=\"0\">");
        sb.append("<tr><td>Picture name: <br> <input type=text name=\"pictureName\" size=20></td></tr>");
        sb.append("<tr><td>User name: <br> <input type=text name=\"userName\" size=20></td></tr>");
        sb.append("<tr><td>Category: <br> <input type=text name=\"category\" size=20></td></tr>");
        sb.append("<tr><td>Select file to upload: <br> <input type=file name=\"myfile\">");
//	    	        responseContent.append("<tr><td>Select file to upload: <br> <input type=file name=\"myfile2\">");
        sb.append("</td></tr>");
        sb.append("<tr><td><INPUT TYPE=\"submit\" NAME=\"Send\" VALUE=\"Send\"></INPUT></td>");
        sb.append("<td><INPUT TYPE=\"reset\" NAME=\"Clear\" VALUE=\"Clear\" ></INPUT></td></tr>");
        sb.append("</table></FORM>\r\n");
        sb.append("<CENTER><HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");

        sb.append("</body>");
        sb.append("</html>");
        
        return sb;
	}
	
}
