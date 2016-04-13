import java.nio.ByteBuffer;

class Message {
    public static final byte START = 1;
    public static final byte FILENAME = 2;
    public static final byte FILEDATA = 3;
    public static final byte FILE_NOT_FOUND = 4;
    public static final byte ACK = 5;
    public static final byte QUIT = 6;
    public static final byte END_OF_FILE = 7;

    public byte message_type;
    public byte seq_num[];
    public byte[] data;
    public int data_length;
    
    public int seq_num_int;
    
    public Message() {}
    
    public void parseMessage(byte[] input, int length) {
        message_type = input[0];
        
        seq_num = new byte[4];
        
        seq_num[0] = input[1];
        seq_num[1] = input[2];
        seq_num[2] = input[3];
        seq_num[3] = input[4];
        
        seq_num_int = ByteBuffer.wrap(seq_num).getInt();
        data = new byte[length - 5];
        data_length = length -5;
        
        
        for(int i=0; i<length-5; ++i) {
            data[i] = input[i+5];
        }
    }
    
    public static byte[] codeMessage(int snum, byte type, byte[] input, int length) {
        byte[] output = new byte[length + 5];
        output[0] = type;
        
        output[1] = (byte) (snum >> 24);
        output[2] = (byte) (snum >> 16);
        output[3] = (byte) (snum >> 8);
        output[4] = (byte) (snum /*>> 0*/);
        
        for(int i=0; i<length; ++i) {
            output[i+5] = input[i];
        }
        
        return output;
    }
    
}