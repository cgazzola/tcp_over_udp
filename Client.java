
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class Client {
    @SuppressWarnings("null")
    public static void main(String args[]) throws IOException{
        System.out.println("\nWelcome to Crisa's FTP program.\n");
        System.out.println("Enter <start> to get started,");
        System.out.println("Enter <help> to see the message code table,");
        System.out.println("or <quit> to quit\n");
        
        Message message = new Message();
        InetAddress IPAddress = InetAddress.getByName("localhost");
        byte[] receiveData = new byte[1024];
        int seq_num = 1;

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        String userInput;
        DatagramSocket clientSocket = new DatagramSocket();
        DatagramSocket clientSocket2 = new DatagramSocket(2015);
        String fileData;
        PrintWriter writer = null;
        
        while(true){
            System.out.print(">> ");
            userInput = inFromUser.readLine();
            if(userInput.equals("start")){
                // Message for start.

                byte[] sendData = Message.codeMessage(seq_num, Message.START, userInput.getBytes(), userInput.length());
                
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 2014);
                clientSocket.send(sendPacket);
                seq_num++;
                
                System.out.println("\nEnter name of file you want to download: ");
                System.out.print(">> ");
                // getting file name from user
                userInput = inFromUser.readLine();
                sendData = Message.codeMessage(seq_num, Message.FILENAME, userInput.getBytes(), userInput.length());

                sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 2014);

                //sending requested filename to server
                clientSocket.send(sendPacket);
                
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                while(true) {
                    System.out.println("before receive in client");
                    clientSocket2.receive(receivePacket);
                    if(receivePacket.getLength() <= 0) {
                        System.out.println("Got a " + receivePacket.getLength() + " length packet from server");
                        // time to close file.
                        writer.close();
                        writer = null;
                        
                        break;
                    } else {
                        message.parseMessage(receivePacket.getData(), receivePacket.getLength());
                        
                        System.out.println("Got packet of kind: " + String.valueOf(message.message_type));
                        System.out.println("Got packet with seq number: " + String.valueOf(message.seq_num_int));
                        
                        fileData = new String(message.data,0, message.data_length);
                        System.out.println(fileData);
                        // No file on server
                        if(message.message_type == Message.FILE_NOT_FOUND) {
                            System.out.println("Got: " + fileData);
                            break;
                        } else if(message.message_type == Message.FILEDATA) {
                            // open a file once and start saving onto it.
                            if(writer == null)
                                writer = new PrintWriter("./rcv/file", "UTF-8");
                            // writing onto file.
                            System.out.println("Writing to file.");
                            writer.print(fileData);
                        } else if(message.message_type == Message.END_OF_FILE) {
                            writer.close();
                            writer = null;
                        }
                    }
                }
                // construct
            }
            else if(userInput.equals("help")){
                System.out.println("\n           ********** MESSAGE CODE TABLE **********");
                System.out.println(" _____________________________________________________________");
                System.out.println("| Message Code  |                  Meaning                    |");
                System.out.println("|===============|=============================================|");
                System.out.println("|        1      |   Request a file to be read from server     |");
                System.out.println("|===============|=============================================|");
                System.out.println("|        2      | The message contains data being transferred |");
                System.out.println("|===============|=============================================|");
                System.out.println("|        3      |   The message acknowledges a data message   |");
                System.out.println("|===============|=============================================|");
                System.out.println("|        4      | Reports an error [Error msg: File not found]|");
                System.out.println("|===============|=============================================|");
                System.out.println("|_______________|_____________________________________________|\n");

                System.out.print(">> ");
                userInput = inFromUser.readLine();
            }
            else if(userInput.equals("quit")){
                System.out.println("\nFTP program is shutting down, goodbye!\n");
                break;
            }           
        }
        clientSocket.close();
        clientSocket2.close();

    }
}