

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;


class Server{
    public static final String FOLDER = "./files/";
    public static File folder = new File(FOLDER);
    public static ArrayList<String> files  = new ArrayList<>();
    
    public static void printFiles() {
        File[] listOfFiles = folder.listFiles();

        if(listOfFiles.length == 0){
            System.out.println("No files were found\n");                }
        else{
            System.out.println("The following files were found on server:\n");
            for (int i = 0; i < listOfFiles.length; i++) {
                System.out.println("File " + listOfFiles[i].getName());
                files.add(listOfFiles[i].getName());
            }
        }
    }
    
    public void printDatagramInfo(DatagramPacket receivePacket) {
        //get Client IP
        InetAddress IPAddress = receivePacket.getAddress();
        System.out.println("IP Address: " + IPAddress);

        //get Client port
        int port = receivePacket.getPort();
        System.out.println("Port #: " + port);

        System.out.println("Client requesting possible files to download");
        System.out.println("Fetching...\n");
    }
    
    
    public static void main(String args[]) throws IOException, InterruptedException{
        //ArrayList<Integer> ackToSend = new ArrayList<>();
        int seq_num = 1;
        String filename = null;
        Message message = new Message();
        String eof = "End Of File";
        int rc;
        InetAddress IPAddress = InetAddress.getByName("localhost");

        //server runs on port 2014
        DatagramSocket serverSocket = new DatagramSocket(2014);

        //creates array of bytes with size 1024
        byte[] recieveData = new byte[1024];
        byte[] sendData;
        
        //Implementing UDP using DatagramPacket
        DatagramPacket receivePacket = new DatagramPacket(recieveData, recieveData.length);

        System.out.println("\nFTP Server up and running, waiting for client request\n");

        while(true){    

            //Receives a datagram packet 
            serverSocket.receive(receivePacket);
            recieveData = receivePacket.getData();
            message.parseMessage(recieveData, receivePacket.getLength());
            System.out.println("Got message on server");
            

            if(message.message_type == Message.START) {
                // send ack for start.
                printFiles();
            } else if(message.message_type == Message.FILENAME) {
                // send ack and open and start sending file.
                
                
                filename = new String(message.data,0, message.data_length);
                System.out.println("Got request for: "+ filename);
                if(files.contains(filename) == false) {
                    System.out.println("File not found.");
                    // send a message of type file not found
                    String error = "File Not Found";
                    sendData = Message.codeMessage(seq_num, Message.FILE_NOT_FOUND, error.getBytes(), error.length());
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 2015);
                    System.out.println("Sending data of length: " + sendPacket.getLength());
                    serverSocket.send(sendPacket);
                    continue;
                }
                FileInputStream in = new FileInputStream("./files/" + filename);
                byte[] fileBuff = new byte[512];
                rc = in.read(fileBuff);
                sendData = Message.codeMessage(seq_num, Message.FILEDATA, fileBuff, rc);
                while(rc != -1)
                {
                    seq_num += 1;
                    DatagramPacket sendPacket = new DatagramPacket(sendData, rc+5, IPAddress, 2015);
                    System.out.println("Sending data of length: " + sendPacket.getLength());
                    serverSocket.send(sendPacket);
                    fileBuff = new byte[512];
                    rc = in.read(fileBuff);
                    if(rc > 0)
                        sendData = Message.codeMessage(seq_num, Message.FILEDATA, fileBuff, rc);
                }
                sendData = Message.codeMessage(seq_num, Message.END_OF_FILE, eof.getBytes(), eof.length());
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 2015);
                System.out.println("Sending empty packet");
                serverSocket.send(sendPacket);
                System.out.println("File Sending Complete.");
                in.close();
            } else if(message.message_type == Message.FILEDATA) {
                // wont happen here.
            } else if(message.message_type == Message.ACK) {
                // receive and show seqnum
                System.out.println("Got ack for seq number: " + message.seq_num_int);
            } else if(message.message_type == Message.QUIT) {
                // send ack for quit.
                System.out.println("Received quit.");
                break;
            } 

        }
        serverSocket.close();
    }
}