import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Scanner;
 
public class Client {
	
 
	public static void main(String[] args) {
 
       // Địa chỉ máy chủ.
       final String serverHost = "localhost";
 
       Socket socketOfClient = null;
       BufferedWriter os = null;
       BufferedReader is = null;
 
       try {
           // Gửi yêu cầu kết nối tới Server đang lắng nghe
           // trên máy 'localhost' cổng 7777.
           socketOfClient = new Socket(serverHost, 8787);
 
           // Tạo luồng đầu ra tại client (Gửi dữ liệu tới server)
           os = new BufferedWriter(new OutputStreamWriter(socketOfClient.getOutputStream()));
 
           // Luồng đầu vào tại Client (Nhận dữ liệu từ server).
           is = new BufferedReader(new InputStreamReader(socketOfClient.getInputStream()));
 
       } catch (UnknownHostException e) {
           System.err.println("Don't know about host " + serverHost);
           return;
       } catch (IOException e) {
           System.err.println("Couldn't get I/O for the connection to " + serverHost);
           return;
       }
 
       try {
    	   
    	   Scanner scn = new Scanner(System.in);
    	   String mess;
    	   int i = 0;
    	   
    	   while(i != 1) {
    		   mess = scn.nextLine();
    		   os.write(mess);
        	   os.newLine();
        	   os.flush();
        	   
               // Đọc dữ liệu trả lời từ phía server
               // Bằng cách đọc luồng đầu vào của Socket tại Client.
               String responseLine;
               while ((responseLine = is.readLine()) != null) {
                   System.out.println("Server: " + responseLine);
                   if (responseLine.indexOf("OK") != -1) {
                       break;
                   }
                   mess = scn.nextLine();
        		   os.write(mess);
            	   os.newLine();
            	   os.flush();
               }
               
               os.close();
               is.close();
               socketOfClient.close();
               i = 1;
    	   }
    	  
       } catch (UnknownHostException e) {
           System.err.println("Trying to connect to unknown host: " + e);
       } catch (IOException e) {
           System.err.println("IOException:  " + e);
       }
   }
 
}