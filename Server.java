package dfsfsdd;




import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;


public class Server {

   static final int portNumber = 8003;
   static int range = -1;
   static String hashedPassword;

   public static void main(String args[]) throws IOException {

       
       hashedPassword = genratehashedpassword();
       System.out.println("Hash password: " + hashedPassword);

       ServerSocket ss = new ServerSocket(portNumber);
       System.out.println("\nWaiting for client response....");
       int id = 0;
       
       while (true) {
           Socket clientSocket = ss.accept();
           ClientServiceThread clientService = new ClientServiceThread(clientSocket, ++id);
           clientService.start();
           
           System.out.println("\nClient_" + id + " connection established\n");
       }
   }

   

   //genrating a random password then hashing it
   public static String genratehashedpassword() {

       StringBuilder stmp = new StringBuilder();
       char[] symbols, buffer = new char[5];
       String date = getSystemDate();
       Random random = new Random();
       StringBuffer sb = new StringBuffer();

       for (char ch = '0'; ch <= '9'; ++ch) {
           stmp.append(ch);
       }
       for (char ch = 'A'; ch <= 'Z'; ++ch) {
           stmp.append(ch);
       }
       symbols = stmp.toString().toCharArray();

       for (int index = 0; index < buffer.length; ++index) {
           buffer[index] = symbols[random.nextInt(symbols.length)];
       }
       
       try {
           String actualPassword = new String(buffer);
           System.out.println("\nRandomly generated password: " + actualPassword);
           
           MessageDigest md = MessageDigest.getInstance("MD5");
           md.update(( actualPassword + date).getBytes());
           byte[] byteData = md.digest();

           for (int i = 0; i < byteData.length; i++) {
               sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
           }
       } catch (NoSuchAlgorithmException ex) {

       }

       return new String(sb);
   }

  
   
   
  // method to get system date (from the internet)
   public static String getSystemDate() {

       DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
       Calendar cal = Calendar.getInstance();
       String date = (dateFormat.format(cal.getTime())).toString();
       return date;
   }

   
   
   
   
   
  //thread to extend multi users as needed in project
   public static class ClientServiceThread extends Thread{
       
       int dataSent = 1, clientId = -1;
       String sendMsg = "" ;
       String receiveMsg = "";
       Socket clientSocket;
       
       ClientServiceThread(Socket socket, int id){
           clientSocket = socket;
           clientId = id;
       }
       
   
       
       public void run() {
           try {
               BufferedReader brIN = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
               PrintStream psOUT = new PrintStream(clientSocket.getOutputStream());   //for writing

               while (dataSent <= 4) {
                   sendMsg = (++range) + "\n" + hashedPassword;
                   psOUT.println(sendMsg);
                   receiveMsg = brIN.readLine();
                   if (receiveMsg.equals("success")) {
                       System.out.println("Client_" + clientId + " password cracked ");
                       break;
                   }
                   dataSent++;
               }

               clientSocket.close();
               brIN.close();
               psOUT.close();
               System.out.println("Connection lost from client_" + clientId);

           } catch (IOException ex) {

           }

       }
   }
}
