

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;  
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import java.awt.Color;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.JLabel;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.imageio.ImageIO;

class Server extends JFrame	implements ActionListener, MouseListener, MouseMotionListener 	
{
	private Constant c;
	private ImgTemp imgTemps[];										
	private TXTDataBase txtDataBase;								//txt database
	private GameTable Tables[];										
	private UAS Users[];											//players					
	private int userSum = 0;										
	private Selector selector;										//socket selector
	private ServerSocketChannel ssc;								//asynchronous socket
	private ServerSocket ss;										
	private InetSocketAddress address;								
	private JTextArea showUsers = new JTextArea("");
	private JScrollPane	showUsersScroll = new JScrollPane(showUsers);
	private Image playerImage;
	private JLabel viewersInfors[];
	private String viewersInforsID[];


	//constructor
	Server(Constant cc)												
	{
		super("player");
		addWindowListener(new WindowAdapter(){
			   @Override
			   public void windowClosing(WindowEvent e) {
			         System.exit(0); 
			   }
			  });														
		c = cc;														
		imgTemps = new ImgTemp[c.maxUsers];
		viewersInfors = new JLabel[c.maxUsers];
		viewersInforsID = new String[c.maxUsers];
		txtDataBase = new TXTDataBase();							



		Tables = new GameTable[c.maxTables];
		for(int i = 0;i < Tables.length;i++){						
			Tables[i] = new GameTable(c);
			Tables[i].setUsers(Users);
			Tables[i].setServer(this);
			
		}

		Users = new UAS[c.maxUsers];								
		for(int i = 0;i < Users.length;i++){
			Users[i] = new UAS();	
		}		

		for(int i = 0;i < imgTemps.length;i++){
			imgTemps[i] = new ImgTemp();
		}

		if(c.isShowUser){
			showUser();
		}

		try {
            selector = Selector.open();
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);							
            ss = ssc.socket();
            address = new InetSocketAddress(c.serverIp,c.serverPort);
            ss.bind(address);										
            ssc.register(selector, SelectionKey.OP_ACCEPT);			//register ACCEPT event
			System.out.println("********* Gomoku Server  ********");
            System.out.println("Server port registered OK");
		}
		catch (Exception e){	
			System.out.println("can't start the server.");
			if(c.debug){	e.printStackTrace();}
			System.exit(0);
		}
		try {
			int userNumber = -1;									
            while(true)												
			{
                int event = selector.select();					
                if(event == 0)	{	continue;	}
                ByteBuffer echoBuffer = ByteBuffer.allocate(c.BUFFER_SIZE);			
                for (SelectionKey key : selector.selectedKeys())					
				{
                    if(key.isAcceptable())							
					{
                        ServerSocketChannel server = (ServerSocketChannel)key.channel();	
                        SocketChannel client = server.accept();						
                        client.configureBlocking(false);							
                        client.register(selector, SelectionKey.OP_READ);			
                        System.out.println("new connection:" + client.getRemoteAddress());
                    }
                    else if(key.isReadable())						
					{
                        SocketChannel client = (SocketChannel)key.channel();		
                        echoBuffer.clear();											
						int readInt = 0;											
                        try {
                            while ((readInt = client.read(echoBuffer)) > 0){
								
								byte[] readByte = new byte[readInt];				
								for(int i = 0;i < readInt;i++){						
									readByte[i]=echoBuffer.get(i);
								}
								echoBuffer.clear();									
								client.register(selector, SelectionKey.OP_READ);
								userNumber = getUserNum(client);
								prepareParse(readByte,client,userNumber);
                            }
							if(readInt < 0)	{						
								
								System.out.println("client offline: "+client.getRemoteAddress());
								userExit(client);
								client.close();
							}
                        }
						catch(Exception e){							
							
							System.out.println("client log out");	
							
							userExit(client);
							key.cancel();
                            break;
                        }
                    }
                }
				selector.selectedKeys().removeAll(selector.selectedKeys());	//removed all processed keys
            }
        }
        catch (Exception e){	
			System.out.println(e);
			if(c.debug){	e.printStackTrace();}
		}
	}

	public void prepareParse(byte[] readByte, SocketChannel client,int userNumber)		
	{	
		String dirtyResult = new String(readByte);
		if(userNumber >= 0){
			if(!Users[userNumber].getIsFileSended()){
				imageSaveControl(readByte,client,userNumber);
			}
		}

		String results[];
		try{
			results = dirtyResult.split(c.STATEND);
		}
		catch(Exception e){	
			if(c.debug){	e.printStackTrace();}
			return; 
		}

		if(userNumber < 0 || (userNumber >= 0 && !Users[userNumber].getIsFile())) {	
			for(int i = 0;i < results.length;i++){
				System.out.println("parse: " + results[i]);
				Parse(results[i],client,userNumber);	
			}
		}
	}

	public void imageSaveControl(byte[] readByte, SocketChannel client,int userNumber){	//save player's profile photo
		try{
			String dirtyResult = new String(readByte,"ISO-8859-1");
			if(dirtyResult.contains("ULMImg")){						
				System.out.println("userNumber = " + userNumber);
				Users[userNumber].setIsFile(true);
				int l = ("ULMImg" + c.STATEND).length();
				int position = dirtyResult.indexOf("ULMImg");
				String remain = dirtyResult.substring(position + l);
				if(remain.length() > 0){
					System.out.println("^^^^^");
					byte [] remains = remain.getBytes("ISO-8859-1");
					saveImage(userNumber,remains);
					byte another[] = dirtyResult.substring(0,position).getBytes("ISO-8859-1");
					if(another.length > 0){
						prepareParse(another,client,userNumber);
					}
				}
			}
			else if(dirtyResult.contains("SMImgD")){				
				Users[userNumber].setIsFile(false);
				Users[userNumber].setIsFileSended(true);
				int l = ("SMImgD" + c.STATEND).length();
				int position = dirtyResult.indexOf("SMImgD");
				String remain = dirtyResult.substring(0,position);
				if(remain.length() > 0){
					System.out.println("vvvvv");
					byte [] remains = remain.getBytes("ISO-8859-1");
					saveImage(userNumber,remains);
					byte another[] = dirtyResult.substring(position + l).getBytes("ISO-8859-1");
					if(another.length > 0){
						prepareParse(another,client,userNumber);
					}
				}
				if(Users[userNumber].imgTempNum != -1){						//image can't be null
					System.out.println("imagesize:" + imgTemps[Users[userNumber].imgTempNum].getTempLength());
					try{													//save to disk
						File fWrite = new File(c.imagepath + Users[userNumber].getId() + ".PNG");
						FileOutputStream out = new FileOutputStream(fWrite);
						out.write(imgTemps[Users[userNumber].imgTempNum].getNewFiles(),0,imgTemps[Users[userNumber].imgTempNum].getTempLength());
						out.close();
					}
					catch(Exception e){
						System.out.println(e);
						if(c.debug){	e.printStackTrace();}
					}
					imgTemps[Users[userNumber].imgTempNum].userNumber = -1;
					imgTemps[Users[userNumber].imgTempNum].newFiles = null;	
					imgTemps[Users[userNumber].imgTempNum].setTempLength(0);
				}
				Users[userNumber].setIsFile(false);
				Users[userNumber].setIsFileSended(true);
				String result = "setIsFileSended;true";
				c.sendInforBack(client,result);
			}

			if(Users[userNumber].getIsFile()){								
				if((!dirtyResult.contains("ULMImg")) && (!dirtyResult.contains("SMImgD"))) {
					saveImage(userNumber,readByte);
				}
			}
		}
		catch(Exception e){
			System.out.println(e);
			if(c.debug){	e.printStackTrace();}
		}
	}

	public void saveImage(int userNumber, byte[] readByte){			//save user's image to ImgTemps
		if(readByte.length <= 0)return;
		for(int j = 0; j < c.maxUsers; j++){
			if(Users[userNumber].imgTempNum == -1){					
				for(int k = 0; k < c.maxUsers; k++){
					if(imgTemps[k].userNumber == -1){
						imgTemps[k].userNumber = userNumber;
						Users[userNumber].imgTempNum = k;
						imgTemps[k].newFiles = new byte[c.MaxImageLength];
						System.out.println("k = "+k);
						break;
					}
				}
			}
		}
		if(imgTemps[Users[userNumber].imgTempNum].newFiles == null){//init it 
			imgTemps[Users[userNumber].imgTempNum].newFiles = new byte[c.MaxImageLength];
		}
		imgTemps[Users[userNumber].imgTempNum].setNewFiles(readByte,readByte.length);
	}
	
	public void Parse(String str, SocketChannel client ,int userNumber)		//parse info from user
	{
		if(str.equals("") || str == null) return;
		boolean isok = false;
		String [] message = null;
		try{
			message = str.split(";");
		}
		catch(Exception e){	
			if(c.debug){	e.printStackTrace();}
			return; 
		}
		if(message.length > 0){
			
			if(message[0].equals("login")&&message.length == 3)		//user log in
			{
				//format£º"login;userName;password"
				doUserLogin(message,client);
			}
			else if(message[0].equals("regist"))					//user register
			{
				//format£º"regist;id;name;password"
				regist(client,message[1],message[2],message[3]);
			}
			else if(message[0].equals("sitdown2"))					//user sit down
			{
				//format£º"sitdown;tableid;seatid;image"
				int deskNumber = Integer.parseInt(message[1]);
				int chairNumber = Integer.parseInt(message[2]);

				GameTable t = Tables[deskNumber];

				if(!t.seat[chairNumber].userId.equals("")&&!t.getIsStart()){
					String result = "NoSit;";
					c.sendInforBack(client,result);
					return;											//can't seat here
				}
				
				int i = getUserNum(client);

				if(deskNumber < 0)									//mark the table when the first user sit down
				{	t.setTableNumber(deskNumber);}
				UAS	Viewers[] = t.getViewers();
				Seat[] seat = t.getSeat();

				if(!t.getIsStart()){								
					for(int j = 0;j < Viewers.length; j++)			
					{
						if(Viewers[j].getId().equals("")){			
							t.seat[chairNumber].set(Users[i].getId(),Users[i].getName(),message[3]); 	
							broadcast(seatState());					
							Viewers[j] = Users[i];
							String result = "ackSit;" + deskNumber + ";" + "NotStart";
							StepRecord stepRecords[] = t.getStepRecords();
							c.sendInforBack(client,result);			
							System.out.println("player: "+ Viewers[j].getName() + "enter room " + deskNumber);
							break;
						}
					}
				}
				else if(t.getIsStart()){							
					for(int j = 0;j < Viewers.length; j++)			
					{
						if(Viewers[j].getId().equals("")){			
							Viewers[j] = Users[i];
							String result = "ackSit;" + deskNumber + ";";
							StepRecord stepRecords[] = t.getStepRecords();	
							int step = t.getStep();
							result += "AreadyStart;" + step + ";";
							for(int k = 0;k < step;k++){
								result += stepRecords[k].getX() + "," + stepRecords[k].getY() + "," + stepRecords[k].getColor() + ";";
							}
							c.sendInforBack(client,result);			

							System.out.println("player: "+ Viewers[j].getName() + "enter room " + deskNumber);
							break;
						}
					}
				}

				t.setViewerSum(t.getViewerSum() + 1);				
				t.refreshViewersInfor();							
				Users[i].setDeskNumber(deskNumber,chairNumber);
			}
			else if(message[0].equals("initGameHallOk"))			
			{
				refreshGameHallPlayers();
				c.sendInforBack(client,seatState());	
			}
			else if(message[0].equals("viewPlayersInfor"))			
			{
				String imageName =c.imagepath + message[1]+".PNG";
				String from = "";
				if(message[2].equals("GameHall")){
					from = "0";
				}
				else{
					from = "1";
				}
				int n = 0;
				try{
					String item = "";

					
						String texts[] = {message[1]};
						int positions[] = {1};
						item = txtDataBase.isExsitAndGetItem(c.recordsPath,texts,positions);
					
					c.sendInforBack(client,"userRecord;" + from + ";" + item);
					c.sendInforBack(client,"DLPImg" + from);

					File file =new File(imageName);
					System.out.println(imageName + " filelength:" + file.length());
					if(file.length() > c.MaxImageLength){
						System.out.println("file too large");//
						c.sendInforBack(client,"ImgIllegal" + from);
						return;
					}
					FileInputStream  fr = new FileInputStream (file);
					byte[] b = new byte[1024];
					ByteBuffer sendbuffer; 
					while ((n = fr.read(b)) > 0) {	
						sendbuffer = ByteBuffer.wrap(b,0,n);  
						client.write(sendbuffer);
						sendbuffer.flip();
						Thread.sleep(3);	
					}
					fr.close();
					c.sendInforBack(client,"LPImgD");
				}
				catch(Exception e){
					System.out.println(e);
					if(c.debug){	e.printStackTrace();}
					System.out.println("send data error");
					c.sendInforBack(client,"ImgIllegal" + from);
				}
			}
			else if(message[0].equals("setMyPortrait"))				//set image
			{
				int i = getUserNum(client);
				Users[i].setPortrait(message[1]);
			}
			else if(message[0].equals("saveDone"))					//save chess manual
			{
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].refreshGamersInforPartly(client);	
				Tables[deskNumber].refreshViewersInforPartly(client);
				c.sendInforBack(client,seatState());
			}
			else if(message[0].equals("initOppoPictOk"))			//init opponent image
			{
				refreshGameHallPlayersPartly(client);
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].setIOPS(Tables[deskNumber].getIOPS() + 1);	
			}
			else if(message[0].equals("getSeatState"))				//update seat info in Hall
			{
				c.sendInforBack(client,seatState());	
			}
			else if(message[0].equals("started"))					//game start
			{
				//format£º"started;tableid;x;y;color"
				int deskNumber = Integer.parseInt(message[1]);
				int i = getUserNum(client);
				Users[i].setDeskNumber(deskNumber);
				if(Tables[deskNumber].checkSetDown(message,client)){	
					broadcast(seatState());	
				}
			}
			else if(message[0].equals("ready"))						//player ready
			{
				//format£º"ready;tableid"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].doReadyGame(client);
				if(Tables[deskNumber].getIsStart()){				
					broadcast(seatState());	
				}
			}
			else if(message[0].equals("userMessage"))				//user message
			{
				//format£º"userMessage;tableid;msg"
				int deskNumber = Integer.parseInt(message[1]);
				int index = str.indexOf(";");
				index = str.indexOf(";", index + 1);
				String mInfor = str.substring(index + 1);
				if(mInfor.equals(""))	return;
				Tables[deskNumber].doChatting(mInfor,client);
			}
			else if(message[0].equals("userBroadcastMessage"))		//hall message
			{
				//format£º"userBroadcastMessage;msg"
				int index = str.indexOf(";");
				String mInfor = str.substring(index + 1);
				if(mInfor.equals(""))	return; 
				doBroadcastChatting(mInfor,client);
			}
			else if(message[0].equals("userSeparateChatMessage"))	//user to user
			{
				//format£º"userBroadcastMessage;userId;msg"
				String userId = message[1];
				int index = str.indexOf(";");
				index = str.indexOf(";", index + 1);
				String mInfor = str.substring(index + 1);
				if(mInfor.equals(""))	return; 
				doBroadcastChatting(mInfor,client,userId);
			}
			else if(message[0].equals("closeTable"))				
			{
				//format£º"closeTable;tableid"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].viewerExit(client);
				resetUserSeat(client);
				broadcast(seatState());								
			}
			else if(message[0].equals("rollbackRequest"))			
			{
				//format£º"rollbackRequest;tableid;step"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].rollbackForward(message[2],client);
			}
			else if(message[0].equals("replyRBForward"))			
			{
				//format£º"replyRBForward;tableid;reply;step"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].doRollback(Integer.parseInt(message[2]),Integer.parseInt(message[3]),client);
			}
			else if(message[0].equals("refreshGamersInforPartly"))	
			{
				//format£º"refreshGamersInforPartly;table"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].refreshGamersInforPartly(client);
			}
			else if(message[0].equals("refreshViewersInforPartly"))	
			{
				//format£º"refreshViewersInforPartly;tableid"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].refreshViewersInforPartly(client);
			}
			else if(message[0].equals("admitLose"))					
			{
				//format£º"admitLose;tableid;color"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].admitLose(message[2]);
				broadcast(seatState());
			}
			else if(message[0].equals("drawRequest"))				
			{
				//format£º"drawRequest;tableid;"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].drawRequest(client);
			}
			else if(message[0].equals("drawRequestReply"))			
			{
				//format£º"drawRequestReply;tableid;reply"
				int deskNumber = Integer.parseInt(message[1]);
				if(Tables[deskNumber].doDraw(client , Integer.parseInt(message[2]))){
					broadcast(seatState());
				}
			}
			else if(message[0].equals("addFriendRequest"))			
			{
				//format£º"addFriendRequest;userId"
				int i = getUserNum(client);
				int j = getUserNumByUserId(message[1]);
				if(isExsitFriend(Users[i].getId(),Users[i].getName(),Users[j].getId(),Users[j].getName())){
					c.sendInforBack(Users[i].getUserChannel(),"friendExsit;");
					return;
				}
				c.sendInforBack(Users[j].getUserChannel(),"addFriendRequest;" +Users[i].getId() + ";"+ Users[i].getName());
			}
			else if(message[0].equals("agreeAddFriend"))			
			{
				//format£º"agreeAddFriend;userId"
				int j = getUserNum(client);
				int i = getUserNumByUserId(message[1]);
				boolean isAdd = addFriend(Users[i].getId(),Users[i].getName(),Users[j].getId(),Users[j].getName());
				if(isAdd){
					c.sendInforBack(Users[j].getUserChannel(),"addFriendAgree;" +Users[i].getId() + ";"+ Users[i].getName());
					c.sendInforBack(Users[i].getUserChannel(),"addFriendAgree;" +Users[j].getId() + ";"+ Users[j].getName());
				}
			}
			else if(message[0].equals("viewFriends"))				
			{
				//format£º"viewFriends;"
				int i = getUserNum(client);
				c.sendInforBack(client,getFriends(Users[i].getId()));
			}
			else if(message[0].equals("delFriend"))					
			{
				//format£º"delFriend;userId;userName"
				int i = getUserNum(client);
				int j = getUserNumByUserId(message[1]);
				boolean isSuccess = delFriend(Users[i].getId(),Users[i].getName(),message[1],message[2]);
				if(isSuccess){
					c.sendInforBack(client,"delSuccess;");
					if(j >= 0){
						c.sendInforBack(Users[j].getUserChannel(),"delSuccess;" + Users[i].getName());
					}
				}
			}
		}
	}

	public void doUserLogin(String []message,SocketChannel client)	
	{
		boolean isTrueUser = false;
		boolean isAlreadyLogin = false;
		String userId = message[1];
		String password = message[2];
		String userName = "";
		String Score = "0";
																
			try{
				String values[]={userId,password};
				int positions[] = {1,3};
				String item = txtDataBase.isExsitAndGetItem(c.usersPath,values,positions);
				if(!item.equals("")){
					isTrueUser = true;
					String dlls[] = item.split(";");
					for(int i = 0 ; i < dlls.length ; i++){
						dlls[i] = dlls[i].trim();
					}
					userName = dlls[1];
					password = dlls[2];
					Score = dlls[3];
				}
			}
			catch (Exception ee){	
				System.out.println(ee);
				if(c.debug){	ee.printStackTrace();}
			}

			String strRegex = "[\u4e00-\u9fa5a-zA-Z0-9]*";			
			Pattern p = Pattern.compile(strRegex);
			Matcher m = p.matcher(userId); 
			if(!userId.matches(strRegex)){
				isTrueUser = false;
			} 
	

		if(userId != null){
			String result = "";
			if(isTrueUser){ 
				result = "ack;" + userId + "," + userName + "," + Score;
				for(int i = 0;i < Users.length;i++){
					if(Users[i].getId().equals(userId)){
						result="nak_reLogin";
						isAlreadyLogin = true;
						System.out.println("already logged in");
					}
				}
				if(!isAlreadyLogin){								
					for(int i = 0; i < Users.length;i++){
						if(Users[i].getId().equals("")){
							Users[i].setAll(userId,userName,Integer.parseInt(Score),client);	
							System.out.println("player: "+Users[i].getName() + " log in" + "  Id:" + userId + " score:" + Score);
							break;
						}
					}
					userSum++;
				}
				if(c.isShowUser){
					refreshShowUser();
				}
				
				String fStr = getFriends(userId);					
				String fInfors[] = fStr.split(";");
				System.out.println(fStr);
				if(fInfors.length > 1){
					for(int j = 1;j < fInfors.length;j++){
						String infors[] = fInfors[j].split(",");
						int fNum = getUserNumByUserId(infors[0]);
						if(fNum != -1){
							c.sendInforBack(Users[fNum].getUserChannel(),"friendOnLine;" + userName);
						}
					}
				}
			}
			else{													
				result = "nak";
				System.out.println("failed.");
			}
			c.sendInforBack(client,result);			
		}
	}

	public void regist(SocketChannel client,String id, String name ,String password){

	
			try{
				String values[]={id};
				int positions[] = {1};
				String item = txtDataBase.isExsitAndGetItem(c.usersPath,values,positions);
				System.out.println(item);

				if(!item.equals("")){
					c.sendInforBack(client,"idRepeat;");
					return;
				}
				String record = id + ";" + name + ";" + password + ";0;";
				String result = txtDataBase.insert(c.usersPath,record,1);
				String record2 = id + ";0;0;0;";
				String result2 = txtDataBase.insert(c.recordsPath,record2,1);
				if(!result.equals("")){
					System.out.println("new player");
					c.sendInforBack(client,"registSuccess;");
				}
			}
			catch (Exception e){	
				System.out.println(e);
				if(c.debug){	e.printStackTrace();}
			}
	
	}

	public String getFriends(String userId){						//Myfriends;id1,name1;id2,name2;
		String friends = "Myfriends;";
		String friends1 = "";
		String friends2 = "";

		
			try{
				ArrayList<String> result = txtDataBase.getItemsNotKey(c.friendsPath,userId,1);
				for(int i = 0;i < result.size();i++){
					String dlls[] = result.get(i).split(";");
					if(getUserNumByUserId(dlls[1]) == -1){			//friend not online
						friends1 += dlls[1] + "," + dlls[2] + ",0" + ";";
					}
					else{											//friend online
						friends2 += dlls[1] + "," + dlls[2] + ",1" + ";";
					}
				}
				friends = friends + friends2+ friends1;					//Myfriends;id1,name1;id2,name2;
			}
			catch (Exception e){	
				System.out.println(e);
				if(c.debug){	e.printStackTrace();}
			}
		
		return friends;
	}

	public boolean delFriend(String iId,String iName, String jId ,String jName){

		
			String text[]={iId,jId,jName};
			String text2[]={jId,iId,iName};
			String splitMarks[]={";",";"};
			String result = txtDataBase.deleteWithoutKey(c.friendsPath,text,text2,splitMarks);
			if(result.equals("success")){
				System.out.println("delete friend");
				return true;
			}
			return false;
		
	}


	public boolean isExsitFriend(String iId,String iName, String jId ,String jName){
		String text[]={iId,jId,jName};
		String splitMarks[]={";",";"};
		return txtDataBase.isExsitWithoutKey(c.friendsPath,text,splitMarks);
	}

	public boolean addFriend(String iId,String iName, String jId ,String jName){

		
			if(isExsitFriend(iId, iName, jId, jName)){
				return false;
			}
			String text[]={iId,jId,jName};
			String text2[]={jId,iId,iName};
			String splitMarks[]={";",";"};
			return txtDataBase.addWithoutKey(c.friendsPath,text,text2,splitMarks);
		
	}

	public void userExit(SocketChannel client)						
	{
		int i = getUserNum(client);
		int deskNumber = -1;
		int chairNumber = -1;
		if(i >= 0){
			deskNumber = Users[i].getDeskNumber();
			chairNumber = Users[i].getChairNumber();
			
		}
		else{
			System.out.println("exit as a tourist");
			return;
		}


		//friend offline
		String fStr = getFriends(Users[i].getId());					
		String fInfors[] = fStr.split(";");
		System.out.println(fStr);
		if(fInfors.length > 1){
			for(int j = 1;j < fInfors.length;j++){
				String infors[] = fInfors[j].split(",");
				int fNum = getUserNumByUserId(infors[0]);
				if(fNum != -1){
					c.sendInforBack(Users[fNum].getUserChannel(),"friendOffLine;" + Users[i].getName());
				}
			}
		}

		System.out.println("player:" + Users[i].getName() + " offline");
		Users[i].clear();
		userSum--;
		
		if(deskNumber >= 0){	
			System.out.println("escape room:"+deskNumber);
			Tables[deskNumber].viewerExit(client);
		}
		if(chairNumber >= 0){
			if(Tables[deskNumber].seat[chairNumber].userId.equals(Users[i].getId())){
				Tables[deskNumber].seat[chairNumber].clear();
			}
		}
		
		
		broadcast(seatState());										
		refreshGameHallPlayers();
		if(c.isShowUser){
			refreshShowUser();
		}

		
	}

	public void refreshShowUser(){									
		showUsers.setText("");
		showUsers.removeAll();
		for(int i = 0,sum = 0; i < Users.length;i++){
			if(!Users[i].getId().equals("")){
				int perLength = 10;									
				String userFormatId = Users[i].getId();
				int idLenth = userFormatId.getBytes().length;
				if(idLenth > 8){
					userFormatId = userFormatId.substring(0,3)+"¡­";
					idLenth = userFormatId.getBytes().length;
				}
				String idPos = "";
				for(int j= 0;j < perLength - idLenth;j++){
					idPos += "  ";
				}
				String infor = "    ID " + userFormatId + idPos  + "nickname " + Users[i].getName() ;
				viewersInforsID[sum] = userFormatId;
				viewersInfors[sum] = new JLabel(infor);
				viewersInfors[sum].setBounds(0,18 * sum,217,18);
				showUsers.add(viewersInfors[sum]);
				showUsers.append("\r\n");
				viewersInfors[sum].addMouseListener(this);
				sum ++;
			}
		}
		JScrollBar bar = showUsersScroll.getVerticalScrollBar();
		bar.setValue(bar.getMaximum());
	}

	public void resetUserSeat(SocketChannel client){				//clear user seat info
		int i = getUserNum(client);
		int deskNumber = -1;
		int chairNumber = -1;
		if(i >= 0){
			deskNumber = Users[i].getDeskNumber();
			chairNumber = Users[i].getChairNumber();
		}
		if(deskNumber >= 0&&chairNumber >= 0){	
			if(Tables[deskNumber].seat[chairNumber].userId.equals(Users[i].getId())){
				Tables[deskNumber].seat[chairNumber].clear();
			}
		}
	}

	public String seatState(){										
		
		String seatState = "seatState;";
		for(int j = 0; j < Tables.length; j ++){
			for(int k = 0 ; k < 2 ; k++)
			if(!Tables[j].seat[k].userId.equals("")){
				seatState = seatState + j + "¡ý";
				seatState = seatState + k + "¡ý";
				seatState = seatState + Tables[j].seat[k].userId + "¡ý";
				seatState = seatState + Tables[j].seat[k].userName + "¡ý";
				seatState = seatState + Tables[j].seat[k].pictName + "¡ý";
				seatState = seatState + Tables[j].getIsStart() + "¡ü";
			}
		}
		return seatState;
	}

	public int getUserNum(SocketChannel client)						
	{
		for(int i = 0;i < Users.length;i++)		{
			if(!Users[i].getId().equals("")){
				if(Users[i].getUserChannel().isConnected()){
					if(Users[i].getUserChannel().equals(client)){
						return i;
					}
				}
			}
		}
		return -1;
	}

	public void doBroadcastChatting(String infor , SocketChannel client){	//Gamehall chat
		System.out.println("public chat:");
		String userName = "";
		String userId = "";
		int userNum = getUserNum(client);							
		userName = Users[userNum].getName();
		userId = Users[userNum].getId();
		String userMessage = "userBroadcastMessage;1;"+ userId + ";" + userName + ";" +infor;
		for(int i = 0;i < Users.length; i++) {						
			if(!Users[i].getId().equals("")){
				if(Users[i].getUserChannel().isConnected()){		
					c.sendInforBack(Users[i].getUserChannel(),userMessage);
				}
				else																
				{	Users[i].clear();	}
			}
		}
	}

	public void doBroadcastChatting(String infor , SocketChannel client, String targetUserId){	//private chat
		System.out.println("private chat");
		String userName = "";
		String userId = "";
		int userNum = getUserNum(client);							
		userName = Users[userNum].getName();
		userId = Users[userNum].getId();
		String userMessage = "userBroadcastMessage;2;"+ userId + ";" + userName + ";" +infor;
		boolean isConnected = false;
		for(int i = 0;i < Users.length; i++) {						
			if(Users[i].getId().equals(targetUserId)){
				if(Users[i].getUserChannel().isConnected()){		
					isConnected = true;
					c.sendInforBack(Users[i].getUserChannel(),userMessage);
					break;
				}
				else																
				{	Users[i].clear();	}
			}
		}
		c.sendInforBack(client,userMessage);						
		if(!isConnected){
			c.sendInforBack(client,"massage_UserOffLine");					
		}
	}

	public void refreshGameHallPlayers()							
	{
		broadcast(createGameHallPlayersInfor());
	}

	public void refreshGameHallPlayersPartly(SocketChannel client)	//send player gamehall message
	{
		c.sendInforBack(client,createGameHallPlayersInfor());
	}

	public String createGameHallPlayersInfor(){						//generate info userId,usreName,color,score
		String uId = "";
		String uName = "";
		String infor = "refreshGameHallPlayers;";
		int ucolor = -1;
		String userPortrait = "";
		for(int i = 0;i < Users.length; i++){
			if(!Users[i].getId().equals("")){
				uId = Users[i].getId();
				uName = Users[i].getName();
				ucolor = Users[i].getUColor();
				int score = Users[i].getScore();
				userPortrait = Users[i].getPortrait();
				infor += uId + "¡ý" + uName + "¡ý" + ucolor + "¡ý" + score + "¡ý" + userPortrait + "¡ü";
			}
		}
		return infor;
	}


	public int getUserNumByUserId(String Id)						
	{
		for(int i = 0;i < Users.length; i++){
			if(Users[i].getId().equals(Id)){
				return i;
			}
		}
		return -1;
	}

	public void broadcast(String infor)								//bordcast to all users
	{
		System.out.println("gamehall bordcast:");
		for(int i = 0;i < Users.length; i++)						
		{
			if(!Users[i].getId().equals("")){
				if(Users[i].getUserChannel().isConnected()){
					c.sendInforBack(Users[i].getUserChannel(),infor);
				}
				else												
				{	Users[i].clear();	}
			}
		}
	}

	public void showUser(){											

		try {
			ImageIcon img = new ImageIcon(c.SysImgpath + "default.png");
			this.setIconImage(img.getImage());						
		}
		catch (Exception e) {
			System.out.println(e);
			if(c.debug){	e.printStackTrace();}
		}

		ImageIcon img = new ImageIcon(c.SysImgpath + "bg5.jpg");
		JLabel bgLabel = new JLabel(img);
		bgLabel.setBounds(0,0,c.wsizex,c.wsizey);
		this.getLayeredPane().add(bgLabel, new Integer(Integer.MIN_VALUE));
		((JPanel)getContentPane()).setOpaque(false);

		setLayout(null);
		setResizable(false);
		setVisible(true);

		showUsersScroll.setBounds(c.m(30), c.m(50), c.m(220), c.m(260));	//user list
		add(showUsersScroll);
		showUsers.setOpaque(true);
		showUsers.setBackground(c.chatColor);
		showUsers.setEditable(false); 

		this.setBounds(160,0,c.wsizex,c.wsizey);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				try{
					dispose();
				}
				catch(Exception ee){
				}
			}
		});
	}

	public void setImage(String userId){							//show all the users
		ImageIcon icon = new ImageIcon("image/" + userId + ".png");
		icon.setImage(icon.getImage().getScaledInstance(icon.getIconWidth(),
		icon.getIconHeight(), Image.SCALE_DEFAULT));
		playerImage = icon.getImage();
		paint(this.getGraphics());
	}

	public void actionPerformed(ActionEvent e){}

	public void mouseClicked(MouseEvent e)	{
		if(e.getModifiers() != 16) return;							//left click
		for(int i = 0 ; i < viewersInfors.length ; i++){
			if (e.getSource() == viewersInfors[i]){
				setImage(viewersInforsID[i]);
			}
		}
	}
	public void mouseEntered(MouseEvent e){ 
		for(int i=0 ; i<viewersInfors.length ; i++){
			if (e.getSource() == viewersInfors[i]){
				viewersInfors[i].setOpaque(true);
				viewersInfors[i].setBackground(new Color(48,117,174)); 
			}
		}
	}
	public void mouseExited(MouseEvent e) {
		for(int i=0 ; i<viewersInfors.length ; i++){
			if (e.getSource() == viewersInfors[i]){
				 viewersInfors[i].setBackground(c.chatColor);		//gray
			}
		}
	} 
	public void mouseReleased(MouseEvent e){ }
	public void mouseDragged(MouseEvent e){ }
	public void mouseMoved(MouseEvent e){ }
	public void mousePressed(MouseEvent e) { } 
	public void paint(Graphics g){									
		super.paintComponents(g);
		g.drawImage(playerImage,300 ,80 ,90 ,130 ,this); 
	}
}

class ImgTemp {														//save the user's profile photo temporarily
	int userNumber = -1;
	byte []newFiles;												//profile photo data
	int tempLength = 0;												//buffer size

	public void setNewFiles(byte[] readByte, int length){
		for (int i= tempLength; i< tempLength + length; i++ ){
			newFiles[i] = readByte[i - tempLength];
		}
		tempLength += length;
	}

	public byte[] getNewFiles(){
		return newFiles;
	}

	public int getTempLength(){	
		return tempLength; 
	}

	public void setTempLength(int templ){	
		tempLength = templ; 
	}
}