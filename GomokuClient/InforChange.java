import java.net.UnknownHostException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.io.File;

class InforChange implements Runnable								
{
	private Constant c;
	private SocketChannel clientChannel;
	private Selector selector;
	private SocketAddress address;
	private boolean openLogin = false;								
	private boolean isLogin=false;									
	private boolean isOpenWin=false;								
	private String userId;											
	private String userName;										
	private int score;												
	private LoginJFrame login = null;								
	private RegistJFrame regist;									
	private Wuziqi wuziqi;											
	private GameHall gameHall;										
	private ViewFriends viewFriends = null;							
	private boolean isHaveOppoImage = false;						
	private boolean isPlayerImage = false;							
	private byte []newPlayerFiles;											
	private int playerTempLength = 0;								
	private boolean isMyFileSended = false;							
	private boolean isOverBufferSizeLength = false;					
	private byte [] overLengthStatementBytes;						

	public void setRegistJFrame(RegistJFrame rjf){
		regist = rjf;
	}

	public void setViewFriends(ViewFriends vf){
		viewFriends = vf;
	}

	public boolean getIsMyFileSended()
	{	return isMyFileSended;}

	public void setIsMyFileSended(boolean isfes)
	{	isMyFileSended = isfes; }

	public void setNewPlayerFiles(byte[] readByte, int length){		
		try{
			for (int i= playerTempLength; i< playerTempLength + length; i++ ){
				newPlayerFiles[i] = readByte[i - playerTempLength];
			}
			playerTempLength += length;
		}
		catch(Exception e){
			System.out.println("saving image failed" + e);
		}
	}

	public byte[] getNewPlayerFiles(){
		return newPlayerFiles;
	}
	
	public void setUser(String uId,String uName,int sc)
	{
		userId=uId;
		userName=uName;
		score=sc;
	}

	public String getUserId()
	{	return userId ; }

	public String getUserName()
	{	return userName ; }

	public int getScore()
	{	return score ; }

	public void setOpenLogin(boolean open)
	{ openLogin = open; }

	InforChange(Constant cc)										
	{
		c = cc;
		overLengthStatementBytes = new byte[c.MaxByteLength];		
		newPlayerFiles = new byte[c.MaxImageLength];
		openLogin = true;
		try{
			selector = Selector.open();								
			address = new InetSocketAddress(c.serverIp, c.serverPort);		
			clientChannel = SocketChannel.open(address);					
			clientChannel.configureBlocking(false);					
			clientChannel.register(selector, SelectionKey.OP_READ);	
			System.out.println("connect to server OK");
			run();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	int pictsum = 0;

	public void newLoginJFrame(String id){							
		login = new LoginJFrame(c);
		login.setChannel(clientChannel);
		login.setInforChange(this);
		openLogin = false;
		login.startClient();
		login.setIdtext(id);
	}

	public void run()												
	{
		try{
			if(openLogin){		
				newLoginJFrame("");
			}
			ByteBuffer readBuffer = ByteBuffer.allocate(c.BUFFER_SIZE);
			int readInt = 0;
			while (true) {
				
				if (!clientChannel.isOpen())	{					
					System.out.println("listen error");
					break;
				}
				int shijian = selector.select();					
				
				SocketChannel sc;									
				for (final SelectionKey key : selector.selectedKeys())	
				{
					if (key.isReadable())	{						
						sc = (SocketChannel) key.channel();			
						readBuffer.clear();							
						try {
							while ((readInt = sc.read(readBuffer)) > 0)	{	
								
								byte[] readByte = new byte[readInt];	
								for(int i = 0;i < readInt;i++)			
								{	readByte[i] = readBuffer.get(i);	}
								prepareParse(readByte);
								readBuffer.clear();						
								readBuffer.flip();
							}
							if(readInt < 0){
								System.out.println("server closed!");
							}
						}
						catch(Exception ee){
							System.out.println("server closed!");
							System.exit(0);								
						}
					}
				}
				selector.selectedKeys().remove(selector.selectedKeys());
				
			}
		}
		catch (Exception e)
		{	System.out.println(e);	} 
	}	

	public void prepareParse(byte[] readByte)						
	{	
		String dirtyResult = new String(readByte);
		imageSavePlayerControl(readByte);							
		
		String results[];
		try{
			results = dirtyResult.split(c.STATEND);
		}
		catch(Exception e)
		{	return; }

		if(!isPlayerImage) {										
			if(overLengthStatementBytesLength != 0&&isRemain){
				
				readByte = preAdd(overLengthStatementBytes,overLengthStatementBytesLength,readByte);
				dirtyResult = new String(readByte);
				results = dirtyResult.split(c.STATEND);
				overLengthStatementBytesLength = 0;
				isRemain = false;
			}

			if(dirtyResult.endsWith(c.STATEND)){					
				for(int i = 0;i < results.length;i++){				
					System.out.println("parse: " + results[i]);
					Parse(results[i]);	
				}
			}
			else if(!dirtyResult.endsWith(c.STATEND)){				
				isOverBufferSizeLength = true;
			}

			if(isOverBufferSizeLength){								
				overLengthStatementBytesAdd(readByte);
				
				if(OLSBtoString().contains(c.STATEND)){
					overLengthStatementParse();
				}
			}
		}
	}

	
	public void overLengthStatementParse(){							
		String results[];
		try{
			results = OLSBtoString().split(c.STATEND);
		}
		catch(Exception e)
		{	return; }
		if(OLSBtoString().endsWith(c.STATEND)){
			for(int i = 0;i < results.length;i++){					
				System.out.println("too long: " + results[i]);
				Parse(results[i]);	
			}
			overLengthStatementBytesLength = 0;						
		}
		else if(!OLSBtoString().endsWith(c.STATEND)){				
			for(int i = 0;i < results.length - 1;i++){				
				Parse(results[i]);	
			}
			isRemain = true;
			String aa = results[results.length - 1];
			int position = OLSBtoISOString().lastIndexOf(c.STATEND);
			byte remainBytes [] = new byte[overLengthStatementBytesLength - position - c.STATEND.length()];
			for(int i = 0 ; i< remainBytes.length ; i ++){
				remainBytes [i] = overLengthStatementBytes[position + c.STATEND.length() + i];
			}
			

			overLengthStatementBytesLength = 0;						
			overLengthStatementBytesAdd(remainBytes);				
		}
		isOverBufferSizeLength = false;
	}

	public boolean isRemain = false;
	int overLengthStatementBytesLength = 0;							

	public void overLengthStatementBytesAdd(byte[] addBytes){		
		for(int i = overLengthStatementBytesLength ;i< overLengthStatementBytesLength + addBytes.length; i++ ){
			overLengthStatementBytes[i] = addBytes[i - overLengthStatementBytesLength];
		}
		overLengthStatementBytesLength += addBytes.length;
	}

	public String OLSBtoString(){									
		return new String(overLengthStatementBytes,0,overLengthStatementBytesLength);
	}

	public String OLSBtoISOString(){											
		String newString  = "";
		try{
			newString  = new String(overLengthStatementBytes,0,overLengthStatementBytesLength,"ISO-8859-1");
		}
		catch(Exception e){
			System.out.println(e);
			e.printStackTrace();
		}
		return newString;
	}

	public byte[] preAdd(byte[] addBytes,int addLength ,byte[] origBytes){			
		byte newBytes[] = new byte[origBytes.length + addLength];
		for(int i = 0 ;i < addLength; i++){
			newBytes[i] = addBytes[i];
		}
		for(int i = addLength ;i < newBytes.length; i++){
			newBytes[i] = origBytes[i - addLength];
		}
		return newBytes;
	}

	/** image transfer **/
	String from = "";
	public void imageSavePlayerControl(byte[] readByte){			
		
		try{
			String dirtyResult = new String(readByte,"ISO-8859-1");	
			
			if(dirtyResult.contains("DLPImg")){						//downLoadPlayersImage

				isPlayerImage  = true;								
				int l = ("DLPImg" + c.STATEND).length();	
				int position = dirtyResult.indexOf("DLPImg");
				int StrLen = "DLPImg".length();
				String remain = dirtyResult.substring(position + l + 1);				
				from = dirtyResult.substring(position + StrLen,position + StrLen +1);	
				if(from.equals("0")){
					gameHall.setWaitForReply(true);					
				}
				else if(from.equals("1")){
					wuziqi.setWaitForReply(true);					
				}
				else if(from.equals("9")){							
					wuziqi.setWaitForReply(true);					
				}
				
				if(remain.length() > 0){
					System.out.println("^^^^^");
					byte [] remains = remain.getBytes("ISO-8859-1");
					
					setNewPlayerFiles(remains,remains.length);
					byte another[] = dirtyResult.substring(0,position).getBytes("ISO-8859-1");
					if(another.length > 0){
						prepareParse(another);
					}
				}
			}
			else if(dirtyResult.contains("LPImgD")){				//loadPlayersImageDone
				isPlayerImage = false;
				if(from.equals("0")){
					gameHall.setWaitForReply(false);				
				}
				else if(from.equals("1")){
					wuziqi.setWaitForReply(false);					
				}
				else if(from.equals("9")){							
					wuziqi.setWaitForReply(false);
					isHaveOppoImage = true;
				}
				int l = ("LPImgD" + c.STATEND).length();
				int position = dirtyResult.indexOf("LPImgD");
				String remain = dirtyResult.substring(0,position);
				if(remain.length() > 0){
					System.out.println("vvvvv");
					byte [] remains = remain.getBytes("ISO-8859-1");
					setNewPlayerFiles(remains,remains.length);
					byte another[] = dirtyResult.substring(position + l).getBytes("ISO-8859-1");
					if(another.length > 0){
						prepareParse(another);
					}
				}
				System.out.println("image size:" + playerTempLength);
				playerTempLength = 0;
				if(from.equals("0")){								
					gameHall.getPlayerInfor().setImage(newPlayerFiles);
				}
				else if(from.equals("1")){							
					wuziqi.playerInfor.setImage(newPlayerFiles);
				}
				else if(from.equals("9")){							
					wuziqi.setOppoImageByte(newPlayerFiles);
					wuziqi.initOppoImage();
				}
				newPlayerFiles = new byte[c.MaxImageLength];		
			}

			if(isPlayerImage){
				if((!dirtyResult.contains("DLPImg")) && (!dirtyResult.contains("LPImgD"))) {
					setNewPlayerFiles(readByte,readByte.length);
				}
			}

			if(dirtyResult.contains("ImgIllegal")){					
				isPlayerImage  = false;		
				playerTempLength = 0;
				int l = ("ImgIllegal" + c.STATEND).length();	
				int position = dirtyResult.indexOf("ImgIllegal");
				int StrLen = "ImgIllegal".length();
				String ImgIllegal = dirtyResult.substring(position + StrLen,position + StrLen +1);	
				if(from.equals("0")){
					gameHall.setWaitForReply(false);	
					gameHall.addToChatLabel("system info: can't get user's profile image");
					gameHall.getPlayerInfor().setImage(new byte[1]);
				}
				else if(from.equals("1")){
					wuziqi.setWaitForReply(false);	
					wuziqi.addToChatLabel("system info: can't get user's profile image");
					wuziqi.playerInfor.setImage(new byte[1]);
				}
				if(from.equals("9")){										
					wuziqi.setWaitForReply(false);
					wuziqi.addToChatLabel("system info: can't get user's profile image");
					wuziqi.setOppoImageByte(new byte[1]);
					wuziqi.initOppoImage();
				}
			}
		}
		catch(Exception e){
			System.out.println(e);
			e.printStackTrace();
		}
	}

	
	public void Parse(String result)								
	{
		if(result.equals("")||result==null) return;
		boolean isok=false;
		String [] message = null;
		try{
			message = result.split(";");
		}
		catch(Exception e){ 
			System.out.println(e);
			return; 
		}
		if(message.length>0){
			String []serverInfor = result.split(";");
			if(serverInfor[0].equals("ack")){						
				if(serverInfor.length < 2)	return;
				if(!isLogin){
					String []userInfor = serverInfor[1].split(",");
					login.dispose();								
					gameHall =new GameHall(c);
					gameHall.setChannel(clientChannel);
					//System.out.println(userInfor[0] + " " + userInfor[1] + " " + userInfor[2]);				
					setUser( userInfor[0],userInfor[1],Integer.parseInt(userInfor[2]));
					gameHall.setUser(userInfor[0],userInfor[1],userInfor[2]);
					isLogin=true;
				}
				gameHall.showMyportrait();							
				gameHall.initMyImageName();							
				gameHall.setIsMyFileSended(isMyFileSended);			
				gameHall.setIsWaiting(false);						
				c.sendMessage(clientChannel,"initGameHallOk;");
			}
			else if(serverInfor[0].equals("nak"))					
			{
				login.hint.setText("user name or password error");
				login.setPasstext("");
			}
			else if(serverInfor[0].equals("userRecord"))					
			{
				if(serverInfor[1].equals("0")){								
					gameHall.getPlayerInfor().setRecord(result);
				}
				else if(serverInfor[1].equals("1")){							
					wuziqi.playerInfor.setRecord(result);
				}
			}
			else if(serverInfor[0].equals("nak_reLogin"))					
			{
				login.hint.setText("already login");
				login.setPasstext("");
			}
			else if(serverInfor[0].equals("start"))					
			{
				if(serverInfor.length < 2)	return;
				wuziqi.start(serverInfor[1]);
			}
			else if(serverInfor[0].equals("ackSit"))				
			{
				if(serverInfor.length < 3)	return;
				if(isOpenWin)
				{ return; }
				wuziqi = new Wuziqi(c);								
				wuziqi.setChannel(clientChannel);
				wuziqi.setUser(getUserId(),getUserName(),getScore());
				wuziqi.setTableNumber(Integer.parseInt(serverInfor[1]));
				wuziqi.setMyImageName(gameHall.getMyImageName());
				wuziqi.init();
				wuziqi.myInfor.setText(userName + "  " + score);
				if(serverInfor[2].equals("AreadyStart")){
					wuziqi.setColor(-2);
					wuziqi.startGame.setEnabled(false);
					wuziqi.admitLose.setEnabled(false);
					wuziqi.retract.setEnabled(false);
					wuziqi.draw.setEnabled(false);
					wuziqi.initGame(result);
					wuziqi.setIsStart(true);
				}
				isOpenWin = true;
				gameHall.setIsSitted(true);
				gameHall.setWaitForReply(false);					
			}
			else if(serverInfor[0].equals("NoSit"))					
			{
				if(serverInfor.length < 1)	return;
				gameHall.setWaitForReply(false);					
				gameHall.addToChatLabel("game info: seat unavailable");
			}
			else if(serverInfor[0].equals("idRepeat"))				
			{
				if(serverInfor.length < 1)	return;
				try{
					regist.hint.setText("id already registered");
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			else if(serverInfor[0].equals("registSuccess"))			
			{
				if(serverInfor.length < 1)	return;
				try{
					regist.newLoginJFrame(regist.getIdtext().getText().trim());
					login.hint.setText("registered success");
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			else if(serverInfor[0].equals("seatState"))				
			{
				if(serverInfor.length > 1){
					gameHall.refreshSeatState(serverInfor[1]);
				}
				else{
					gameHall.refreshSeatState("none");
				}
			}
			else if(serverInfor[0].equals("refreshGameHallPlayers"))
			{
				if(serverInfor.length < 2)	return;
				gameHall.refreshGameHallPlayers(serverInfor[1]);
			}
			else if(serverInfor[0].equals("readyGame"))				
			{
				if(serverInfor.length < 2)	return;
				wuziqi.addToChatLabel("game info:" + serverInfor[1] + " ready");
			}
			else if(serverInfor[0].equals("initGame"))				
			{
				wuziqi.initGame(result);
			}
			else if(serverInfor[0].equals("located"))				
			{	
				if(serverInfor.length < 4)	return;
				int xi = Integer.parseInt(serverInfor[1]);
				int yj = Integer.parseInt(serverInfor[2]);
				int set_color = Integer.parseInt(serverInfor[3]);
				wuziqi.setDown(xi, yj , set_color);
			}
			else if(serverInfor[0].equals("gameEnd"))				
			{
				if(serverInfor.length < 6)	return;
				wuziqi.lblWin.setText(wuziqi.startColor(Integer.parseInt(serverInfor[1])) + "win!"); 
				wuziqi.addToChatLabel("game info: " + wuziqi.startColor(Integer.parseInt(serverInfor[1])) + "win");
				wuziqi.SetWinLineRecord(serverInfor[2],serverInfor[3],serverInfor[4],serverInfor[5]);
				wuziqi.drawWinLine();
				wuziqi.gameEnd();
			}
			else if(serverInfor[0].equals("gameDraw"))				
			{
				wuziqi.lblWin.setText("draw!"); 
				wuziqi.addToChatLabel("game info: draw");
				wuziqi.gameEnd();
			}
			else if(serverInfor[0].equals("mycolor"))				
			{
				if(serverInfor.length < 2)	return;
				int user_color = Integer.parseInt(serverInfor[1]);
				wuziqi.setColor(user_color);
			}
			else if(serverInfor[0].equals("gamerExited"))
			{
				isHaveOppoImage = false;							
				wuziqi.oppsInfor.setText("");
				wuziqi.lblWin.setText("player leave the game");
				wuziqi.setIsHaveOppoImage(false);
				wuziqi.oppoImage = null;
				wuziqi.myPaint();
				wuziqi.gameEnd();
			}
			else if(serverInfor[0].equals("userMessage"))			
			{
				int index = result.indexOf(";");
				index = result.indexOf(";", index + 1);
				index = result.indexOf(";", index + 1);
				String mInfor = result.substring(index + 1);
				if(mInfor.equals(""))	return; 
				wuziqi.getMessage(serverInfor[1],serverInfor[2],mInfor);			//userMessage;userId;userName ;infor
			}
			else if(serverInfor[0].equals("userBroadcastMessage"))	
			{
				int index = result.indexOf(";");
				index = result.indexOf(";", index + 1);
				index = result.indexOf(";", index + 1);
				index = result.indexOf(";", index + 1);
				String mInfor = result.substring(index + 1);
				if(mInfor.equals(""))	return; 
				gameHall.getBroadcastMessage(serverInfor[1],serverInfor[2],serverInfor[3],mInfor);	//userBroadcastMessage;type;userId;userName ;infor
			}
			else if(serverInfor[0].equals("reSetIsOpenWin"))
			{
				isOpenWin=false;
				gameHall.setIsSitted(false);
				isHaveOppoImage = false;							
			}
			else if(serverInfor[0].equals("rollbackForward"))		
			{
				if(serverInfor.length < 2)	return;
				wuziqi.replyRBForward(serverInfor[1]);
			}
			else if(serverInfor[0].equals("rollbackReply"))			
			{
				if(serverInfor.length < 4)	return;
				if(serverInfor[1].equals("yes")){
					wuziqi.rollback(Integer.parseInt(serverInfor[2]),Integer.parseInt(serverInfor[3]));
				}
				else if(serverInfor[1].equals("no")){
					wuziqi.noRollback(Integer.parseInt(serverInfor[2]));
				}
			}
			else if(serverInfor[0].equals("drawRequest"))			
			{
				wuziqi.drawRequest();
			}
			else if(serverInfor[0].equals("noDraw"))				
			{
				wuziqi.addToChatLabel("game info: opponent refused to draw");
				wuziqi.setWaitForDrawReply(false);
				wuziqi.draw.setEnabled(true);
			}
			else if(serverInfor[0].equals("refreshGamersInfor"))	
			{
				if(serverInfor.length < 2)	return;
				if(serverInfor.length == 3)
				{	wuziqi.refreshGamersInfor(serverInfor[1],serverInfor[2]);}
				if(serverInfor.length == 2)							
				{	wuziqi.refreshGamersInfor(serverInfor[1]);}
			}
			else if(serverInfor[0].equals("refreshViewersInfor"))	
			{
				if(serverInfor.length < 2)	return;
				wuziqi.refreshViewersInfor(serverInfor[1]);
			}
			else if(serverInfor[0].equals("setIsFileSended"))		
			{	
				if(serverInfor.length < 2)	return;
				boolean isMyFileSended = Boolean.parseBoolean(serverInfor[1]);
				setIsMyFileSended(isMyFileSended);
			}
			else if(serverInfor[0].equals("addFriendRequest"))		
			{	
				if(serverInfor.length < 3)	return;
				gameHall.addFriendRequest(serverInfor[1],serverInfor[2]);
			}
			else if(serverInfor[0].equals("addFriendAgree"))		
			{	
				if(serverInfor.length < 3)	return;
				gameHall.addToChatLabel("game info: you have added " + serverInfor[2] + "(" + serverInfor[1] + ") as your friend successfully");
			}
			else if(serverInfor[0].equals("Myfriends"))				
			{	
				if(serverInfor.length < 1)	return;
				if(viewFriends != null){
					return;
				}
				viewFriends = new  ViewFriends(c,result);
				viewFriends.setInforChange(this);
				viewFriends.setGameHall(gameHall);
			}
			else if(serverInfor[0].equals("friendExsit"))			
			{
				gameHall.addToChatLabel("game info: the player is already your friend");
			}
			else if(serverInfor[0].equals("delSuccess"))			
			{
				if(serverInfor.length == 1){
					gameHall.addToChatLabel("game info: friend deleted successfully");
				}
				if(serverInfor.length == 2){
					gameHall.addToChatLabel("game info: " + serverInfor[1] +" remove you from his friend list");
				}
			}	
			else if(serverInfor[0].equals("massage_UserOffLine"))	
			{
				gameHall.addToChatLabel("system info:  user offline, send message failed");
			}
			else if(serverInfor[0].equals("friendOnLine"))		
			{	
				if(serverInfor.length < 2)	return;
				gameHall.addToChatLabel("game info: your friend " + serverInfor[1] + " is online");
			}
			else if(serverInfor[0].equals("friendOffLine"))		
			{	
				if(serverInfor.length < 2)	return;
				gameHall.addToChatLabel("game info: your friend " + serverInfor[1] + " is offline");
			}
			
		}
	} 
}
