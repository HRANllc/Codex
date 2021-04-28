
import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

class Constant														//The Constant class, used to save constants used 
{
	String serverIp = "127.0.0.1";
	final int serverPort = 55555;

	final int maxTables  = 100;										//max tables
	final int maxUsers  = 2 * maxTables;							//max users
	final String STATEND = "stateEnd";								//mark the end
	final int BUFFER_SIZE = 1024;									
	final int MaxImageLength = (int)(1024000 * 10);
	final double multiple = 1.0;									//window size ratio
	final int wsizex = m(512);										
	final int wsizey = m(364);										
	final boolean isShowUser = true;								//show Server User interface



	final String rootPrefix = "./";									//the root directory

	final String imagepath = rootPrefix + "image/";
	final String SysImgpath = rootPrefix + "sys_image/";
	final String usersPath = rootPrefix + "users.txt";
	final String friendsPath = rootPrefix + "friends.txt";
	final String recordsPath = rootPrefix + "records.txt";
	final Color chatColor = new Color(232,232,232);
	final int ADD = 2;												//win add marks
	final int MINUS = -2;											//lose decrease marks
	final int ESCAPEMINUS = -4;										//escape decrease marks
	final boolean debug = true;                                     //show debug message

	public Constant(String ip)
	{
		serverIp=ip;		
	}
	
	public int m(int i){
		return (int)(multiple * i);
	}

	ByteBuffer sendbuffer;
	public void sendInforBack(SocketChannel client,String message)	//send message to user
	{
		try{
			message += STATEND;										//add end mark
			byte[] sendBytes = message.getBytes();					//get length 
			sendbuffer = ByteBuffer.allocate(sendBytes.length);		//allocate memory
			sendbuffer.put(sendBytes);								//put message into buffer
			sendbuffer.flip();										//reset flags
			client.write(sendbuffer);								//send message
			System.out.println("send£º" + message);
		}
		catch (Exception e) {
			System.out.println("send error.");
			if(debug){	e.printStackTrace();}
		}
	}
}