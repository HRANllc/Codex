
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.awt.Color;

class Constant														
{
	String serverIp = "127.0.0.1";
	final int serverPort = 55555;
	final int DeskNum = 100;										
	final int ChairNum = DeskNum * 2;								
	final int perLineDesks;											
	final double initMutiple;										
	final double multiple;											
	final int wsizex;												
	final int wsizey;												
	final int formerX;												
	final int formerY;												
	final int halflength;											
	final int WLen;													
	final int redlength;											
	final int maxlength;											
	final int dev_x;												
	final int dev_y;												
	final int nowDeskPanelLength;									
	final String STATEND = "stateEnd";								
	final int BUFFER_SIZE = 1024;									
	final int ImageBufferSize = 1024;								
	final int MaxImageLength = (int)(1024000 * 10);					
	final int MaxByteLength = (int)(1024000 * 10);

	
	final String rootPrefix = "";									

	final String imagepath = rootPrefix + "image/";
	final String SysImgpath = rootPrefix + "sys_image/";
	final String portrait_path = rootPrefix + "sys_portrait/";
	final String TableWaitImgPath = rootPrefix + "sys_image/a.jpg";
	final String TableStartImgPath = rootPrefix + "sys_image/c.jpg";
	final String ChairWaitImgPath = rootPrefix + "sys_image/b.jpg";
	final String myPortraitPath = rootPrefix + "config/my_portrait.txt";
	final String dllPath = rootPrefix + "config/dll.txt";
	final String chessManualPath = rootPrefix + "chessmanual/";
	final Color chatColor = new Color(232,232,232);					
	final Color chatMouseOnColor = new Color(48,117,174);			
	final Color ViewFriend_Online = Color.blue;						
	final Color ViewFriend_Offline = Color.gray;					
	final int maxChatLabelsum = 50;
	final boolean debug = true;
	final boolean isFitPosition;									

	Constant(double IM,String ip){
		serverIp=ip;
		initMutiple = IM;
		multiple = initMutiple;
		wsizex = m(512);
		wsizey = m(364);
		formerX = wsizex;
		formerY = wsizey;
		halflength = m(10);
		WLen = m(20);
		redlength = m(4);
		maxlength = m(300);
		dev_x = m(15);
		dev_y = m(80);
		nowDeskPanelLength = m(340);															
		perLineDesks = nowDeskPanelLength / m(100);
		isFitPosition = true;
	}

	Constant(double IM,double m,int x,int y,int formerx,int formery,boolean fit,int FDL){		
		initMutiple = IM;
		multiple = m;
		wsizex = x;
		wsizey = y;
		formerX = formerx;
		formerY = formery;
		halflength = m(10);
		WLen = m(20);
		redlength = m(4);
		maxlength = m(300);
		dev_x = m(15);
		dev_y = m(80);
		nowDeskPanelLength = wsizex - (int)(formerX*multiple/initMutiple) + (int)(FDL*multiple/initMutiple);
		perLineDesks = nowDeskPanelLength / m(100);
		isFitPosition = fit;
	}

	public int m(int i){
		return (int)(multiple * i);
	}

	public int extendL(int i) {
		int windowChangeLength = wsizex - (int)(formerX*multiple/initMutiple);		
		return windowChangeLength + m(i);											
	}

	private ByteBuffer sbuffer;
	public void sendMessage(SocketChannel socket,String message)	
	{
		try{
			message += STATEND;
			byte[] sendBytes = message.getBytes();					
			sbuffer = ByteBuffer.allocate(sendBytes.length);		
			sbuffer.put(sendBytes);									
			sbuffer.flip();											
			socket.write(sbuffer);									
			System.out.println("send£º" + message);
		}
		catch(Exception e){
			System.out.println("send data error");
			if(debug){	e.printStackTrace();}
		}
	}
}

