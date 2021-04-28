

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.PopupMenu;
import java.awt.MenuItem;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

class GameHall extends JFrame implements ActionListener, MouseListener, MouseMotionListener
{
	private Constant c;
	private SocketChannel clientChannel;					
	private JLabel game_logo;								
	private JLabel game_name;								
	private JLabel game_sum;								
	private JLabel my_portrait;								
	private JLabel my_name;									
	private JLabel my_infor;								
	private JLabel tButton[];
	private JLabel cButton[];
	private JLabel cName[];
	private JLabel desknum[];								
	private String imageName = "default.PNG";				
	private String myImageName = "default.PNG";				
	private boolean isMyFileSended = false;
	private boolean isSitted = false ; 
	private boolean isWaiting = true;						
	private boolean waitForReply = false ; 
	private String userId;									
	private String userName;
	private String score;
	private JTextArea showUsers = new JTextArea("");
	private JScrollPane	showUsersScroll = new JScrollPane(showUsers);
	private JTextArea chatLabel = new JTextArea("");
	private JScrollPane	chatLabelScroll = new JScrollPane(chatLabel);
	private JTextField messageField = new JTextField();
	private JButton viewFriends = new JButton("friend");
	private JButton chatControl = new JButton("public");
	private JButton sendMessage = new JButton("send");
	private JButton clearMessage = new JButton("clear");
	private JScrollPane	deskPanel = new JScrollPane();
	private PopupMenu viewUserPM = new PopupMenu();
	private MenuItem VUChat = new MenuItem();
	private MenuItem VUAddFriend = new MenuItem();
	private PopupMenu showChatUser = new PopupMenu();
	private MenuItem chatUser = new MenuItem();	
	private PlayerInfor playerInfor = null;
	private boolean alreadyAddListener = false;
	private ImageIcon background;
	private JLabel bgLabel3;
	private ImageIcon imageLogo;
	private ImageIcon tableWaitImage;
	private ImageIcon chairWaitImage;
	private JPanel jPanel;
	private Image image;
	private ImageIcon myPortraitImage;
	private boolean firstOpen = true;
	private String chatLabelLineUserId[];
	private String chatLabelLineUserName[];
	private JLabel chatLabelInfor[];
	private Toolkit toolkit;
	private Dimension screenSize;

	public PlayerInfor getPlayerInfor(){
		return playerInfor;
	}

	public void setUser(String uId,String uName,String sc)
	{
		userId = uId;
		userName = uName;
		score = sc;
	}

	public void setIsWaiting(boolean isW)
	{	isWaiting = isW;}

	public void setChannel(SocketChannel socketChannel)
	{	clientChannel = socketChannel;	}
	public SocketChannel getChannel()
	{	return clientChannel;	}

	public void setIsSitted(boolean isSit)
	{	isSitted = isSit;}

	public void setWaitForReply(boolean wtfp)
	{	waitForReply = wtfp;}

	public void setMyImageName(String myIName)
	{	myImageName = myIName;	}

	public String getMyImageName(){
		return myImageName;
	}

	public boolean getIsMyFileSended()
	{	return isMyFileSended;}

	public void setIsMyFileSended(boolean isfes)
	{	isMyFileSended = isfes; }

	public void setChatTo(String userId , String userName){
		boolean isExist = false;
		for(int i = 0;i < playersInforsID.length;i++){
			if(playersInforsID[i] != null){
				if(playersInforsID[i].equals(userId)){
					isExist = true;
				}
			}
		}
		if(isExist == false){
			addToChatLabel("system info: your friend " + userName + " not online");
			return;
		}
		aimedUserId = userId;
		aimedUserName = userName;
		chatControl.setMargin(new Insets(0,0,0,0));					
		chatControl.setText("private:" + aimedUserName);
		confirmChat = true;
	}

	public void delFriend(String userId,String userName){
		c.sendMessage(clientChannel,"delFriend" + ";" + userId + ";" + userName);
	}

	GameHall(Constant cc)											
	{
		super("Game Hall");											
		c = cc;
		chatLabelLineUserId = new String[200];
		chatLabelLineUserName = new String[200];
		chatLabelInfor = new JLabel[200];
		tButton = new JLabel[c.DeskNum];
		cButton = new JLabel[c.ChairNum];
		cName = new JLabel[c.ChairNum];
		desknum = new JLabel[c.DeskNum];
		toolkit = this.getToolkit();
		screenSize = toolkit.getScreenSize();
		init();
		addToChatLabel("system info: welcome to the Game Hall");
	}

	public void init(){
		if(c.wsizex > screenSize.width + 20){						
			System.out.println("window too large");
			newMultiple("fit");										
		}
		if(!firstOpen){
			remove(game_logo);
			remove(game_name);
			remove(my_portrait);
			this.getLayeredPane().remove(bgLabel3);
			remove(my_name);
			remove(my_infor);
			remove(game_sum);
			paintMyPortrait();
		}
		try {
			ImageIcon img = new ImageIcon(c.SysImgpath + "default.png");
			this.setIconImage(img.getImage());						
		}
		catch (Exception e) {
			System.out.println(e);
		}
		background = new ImageIcon(c.SysImgpath + "bg3.jpg");
		background = new ImageIcon(background.getImage().getScaledInstance(c.wsizex,c.m(75),Image.SCALE_FAST));	
		bgLabel3 = new JLabel(background);
		bgLabel3.setBounds(c.m(0),c.m(-35),c.wsizex,c.m(75));
		this.getLayeredPane().add(bgLabel3, new Integer(Integer.MIN_VALUE));
		((JPanel)getContentPane()).setOpaque(false);

		imageLogo = new ImageIcon(c.SysImgpath + "logo.png");
		imageLogo = new ImageIcon(imageLogo.getImage().getScaledInstance(c.m(33),c.m(33),Image.SCALE_FAST));	
		game_logo = new JLabel(imageLogo);
		add(game_logo);
		game_logo.setBounds(c.m(-5), c.m(-5), c.m(50), c.m(50));	

		game_name = new JLabel("<html><font size = 4><b>Gomoku</b></font></html>");
		add(game_name);
		game_name.setBounds(c.m(42), c.m(-13), c.m(50), c.m(50));	

		game_sum = new JLabel("");
		add(game_sum);
		game_sum.setBounds(c.m(42), c.m(0), c.m(50), c.m(50));		
		if(Players != null){
			addGameSum();
		}

		tableWaitImage = new ImageIcon(c.TableWaitImgPath);
		tableWaitImage = new ImageIcon(tableWaitImage.getImage().getScaledInstance(c.m(30),c.m(30),Image.SCALE_FAST));	

		for(int i=0 ; i < tButton.length ; i++){					
			tButton[i] = new JLabel(tableWaitImage);
		}

		chairWaitImage = new ImageIcon(c.ChairWaitImgPath);
		chairWaitImage = new ImageIcon(chairWaitImage.getImage().getScaledInstance(c.m(20),c.m(20),Image.SCALE_FAST));	
		for(int i=0 ; i< cButton.length ; i++){						
			cButton[i] = new JLabel(chairWaitImage);
			cName[i] = new JLabel("");
		}

		setLayout(null);
		jPanel = new JPanel(null);
		jPanel.setOpaque(false);
		
		for(int i=0 ; i < tButton.length ; i++){
			int x = ( i % c.perLineDesks ) + 1 ;
			int y = ( i / c.perLineDesks ) + 1;
			jPanel.add(tButton[i]);									
			tButton[i].setBounds(c.m(100) * x - c.m(60), c.m(75) * y - c.m(60), c.m(30), c.m(30));			

			desknum[i] = new JLabel("<html><font size = 3 color = 'white'>- "+ (i+1) + " -</font></html>");
			jPanel.add(desknum[i]);
			desknum[i].setBounds(c.m(100) * x - c.m(52), c.m(75) * y - c.m(40), c.m(40), c.m(40));			

			jPanel.add(cButton[2 * i]);								
			jPanel.add(cButton[2 * i + 1]);							
			cButton[2 * i].setBounds(c.m(100) * x - c.m(85), c.m(75) * y - c.m(55), c.m(20), c.m(20));		
			cButton[2 * i + 1].setBounds(c.m(100) * x - c.m(25), c.m(75) * y - c.m(55), c.m(20), c.m(20));	

			jPanel.add(cName[2 * i]);								
			jPanel.add(cName[2 * i + 1]);							
			cName[2 * i].setBounds(c.m(100) * x - c.m(80), c.m(75) * y - c.m(50), c.m(50), c.m(50));		
			cName[2 * i + 1].setBounds(c.m(100) * x - c.m(20), c.m(75) * y - c.m(50), c.m(50), c.m(50));	
					
			tButton[i].addMouseListener(this);						
			cButton[2 * i].addMouseListener(this);
			cButton[2 * i + 1].addMouseListener(this);
		}
		int rownum = ((c.DeskNum + c.perLineDesks - 1)/ c.perLineDesks);
		jPanel.setPreferredSize(new Dimension (c.m(330), c.m(80) * rownum));	
		deskPanel.getViewport().add(jPanel);									
		deskPanel.getViewport().setBackground(new Color(94,131,173));			
		add(deskPanel);
		
		deskPanel.setBounds(c.m(0), c.m(60), c.nowDeskPanelLength, c.m(288));					
		deskPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		showUsersScroll.setBounds(c.extendL(330) + c.dev_x, c.m(60), c.m(140), c.m(100));		
		add(showUsersScroll);
		showUsers.setOpaque(true);
		showUsers.setBackground(c.chatColor);

		chatLabelScroll.setBounds(c.extendL(330) + c.dev_x, c.m(170), c.m(140), c.m(130));		
		add(chatLabelScroll);
		chatLabel.setOpaque(true);
		chatLabel.setBackground(c.chatColor);

		add(messageField);
		messageField.setBounds(c.extendL(360) + c.dev_x, c.m(300), c.m(110), c.m(15));			
		
		messageField.setBorder (BorderFactory.createLineBorder(Color.gray,1));
		messageField.setOpaque(true);
		messageField.setBackground(c.chatColor);

		add(viewFriends);
		viewFriends.setBounds(c.extendL(430) + c.dev_x, c.m(10), c.m(40), c.m(15));				
		
		add(chatControl);
		chatControl.setBounds(c.extendL(330) + c.dev_x, c.m(300), c.m(30), c.m(15));			
		
		add(sendMessage);
		sendMessage.setBounds(c.extendL(330) + c.dev_x, c.m(320), c.m(40), c.m(15));			

		add(clearMessage);
		clearMessage.setBounds(c.extendL(430) + c.dev_x, c.m(320), c.m(40), c.m(15));			
		
		showUsers.setEditable(false); 
		chatLabel.setEditable(false);

		VUChat.setLabel("private chat");
		VUAddFriend.setLabel("add friend");
		
		viewUserPM.add(VUChat);
		viewUserPM.add(VUAddFriend);
		add(viewUserPM);

		showChatUser.add(chatUser);
		add(showChatUser);

		if(firstOpen){
			addMouseListener(this);
			viewFriends.addActionListener(this);
			chatControl.addActionListener(this);
			chatControl.addMouseListener(this);
			sendMessage.addActionListener(this);
			messageField.addActionListener(this);
			clearMessage.addActionListener(this);
			VUChat.addActionListener(this);
			VUAddFriend.addActionListener(this);
		}

		viewFriends.setMargin(new Insets(0,0,0,0));	
		sendMessage.setMargin(new Insets(0,0,0,0));	
		clearMessage.setMargin(new Insets(0,0,0,0));	
		chatControl.setMargin(new Insets(0,0,0,0));	
		
		immediateRefreshSeatState();
		
		this.setSize(c.wsizex,c.wsizey);
		if(c.isFitPosition){
			this.setLocationRelativeTo(null);  
		}
		setVisible(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		if(!alreadyAddListener){
			alreadyAddListener = true;
			this.addWindowListener(new WindowAdapter(){
				public void windowClosing(WindowEvent e){
					if(!isSitted){
						System.exit(0);
					}
					else{
						addToChatLabel("system info: you have unfinished game.");
					}
				}
			});
			
			this.addWindowStateListener(new WindowStateListener () {
				public void windowStateChanged(WindowEvent state) {
					if(state.getNewState() == 0) {					
						int nowX = c.wsizex;
						int nowY = c.wsizey;
						double nowXmultiple = (nowX * 1.0)/c.wsizex * c.multiple;
						double nowYmultiple = (nowY * 1.0)/c.wsizey * c.multiple;
						double newMultiple;
						if(nowXmultiple < nowYmultiple){
							newMultiple = (nowX * 1.0) / 512;
						}
						else{
							newMultiple = (nowY * 1.0) / 364;
						}
						if(newMultiple != c.initMutiple){
							if(c.debug){	System.out.println("window recover");}
							c = new Constant(c.initMutiple,c.serverIp);
							init();
						}
					}
					else if(state.getNewState() == 6) {				
						if(newMultiple("max")){
							init();
						}
					}
				}         
			}); 
		}
		firstOpen = false;
	}

	public boolean newMultiple(String change){
		try{
			int nowX = getWidth();
			int nowY = getHeight();
			if(change.equals("max")){
				nowX = getWidth();
				nowY = getHeight();
			}
			else{
				nowX = screenSize.width - 30;
				nowY = screenSize.height - 30;
			}
			double nowXmultiple = (nowX * 1.0)/c.wsizex * c.multiple;
			double nowYmultiple = (nowY * 1.0)/c.wsizey * c.multiple;
			double newMultiple;
			if(nowXmultiple < nowYmultiple){
				newMultiple = (nowX * 1.0) / 512;
			}
			else{
				newMultiple = (nowY * 1.0) / 364;
			}
			if(newMultiple != c.multiple){
				if(c.debug){
					System.out.println("nowX =" + nowX);
					System.out.println("nowY =" + nowY);
					System.out.println("nowXmultiple =" + nowXmultiple);
					System.out.println("nowYmultiple =" + nowYmultiple);
					System.out.println("newMultiple =" + newMultiple);
				}
				if(change.equals("max")){
					if(c.debug){	System.out.println("window maximum");} 
					c = new Constant(c.initMutiple,newMultiple ,nowX ,nowY ,c.wsizex ,c.wsizey, false, c.nowDeskPanelLength);
				}
				else{
					if(c.debug){	System.out.println("window fitsize");} 
					c = new Constant(newMultiple,c.serverIp);
				}
				return true;
			}
		}
		catch(Exception e){
			System.out.println(e);
			if(c.debug) {	e.printStackTrace();}
		}
		return false;

	}

	public void actionPerformed(ActionEvent e)
	{ 
		if(c.debug){	System.out.println("shot Action");}
		if (e.getSource() == sendMessage || e.getSource() == messageField) {		
			if(aimedUserId.equals("")||confirmChat == false){
				String text = messageField.getText();
				if(text.equals(""))	return;
				String newcontent = "userBroadcastMessage;" + text;	
				c.sendMessage(clientChannel,newcontent);
				messageField.setText("");
			}
			else{
				String text = messageField.getText();
				if(text.equals(""))	return;
				String newcontent = "userSeparateChatMessage;" + aimedUserId + ";" + text;		
				c.sendMessage(clientChannel,newcontent);
				messageField.setText("");
			}
		}
		else if (e.getSource() == clearMessage) {					
			String text = chatLabel.getText();
			if(text.equals(""))	return;
			chatLabel.setText("");
			chatLabel.removeAll();
			chatLabelsum = 0;
		}
		else if (e.getSource() == viewFriends) {					
			c.sendMessage(clientChannel,"viewFriends;");
		}
		else if (e.getSource() == VUChat){							
			if(aimedUserId.equals(userId)){							
				aimedUserId = "";
				aimedUserName = "";
				addToChatLabel("system info: can't chat with yourself");
				chatControl.setText("public");
				confirmChat = false;
				return;
			}
			if(!aimedUserId.equals("")){
				chatControl.setText("private:" + aimedUserName);		
				confirmChat = true;	
			}
		}
		else if (e.getSource() == VUAddFriend){						
			if(aimedUserId.equals(userId)){
				aimedUserId = "";
				aimedUserName = "";
				addToChatLabel("system info: can't add yourself as your friend");
				confirmAF = false;
				return;
			}
			if(!aimedUserId.equals("")){
				int i = JOptionPane.showConfirmDialog(null, "add " + aimedUserName + " as your friend?", "confirm", JOptionPane.YES_NO_OPTION);
				if(i == JOptionPane.YES_OPTION){
					c.sendMessage(clientChannel,"addFriendRequest;" + aimedUserId);
				}
			}
			confirmAF = false;
		}
		else if (e.getSource() == chatControl) {
			if(!chatControl.getText().equals("public")){				
				if(!aimedUserId.equals("")&&confirmChat){			
					formerChatToUserId = aimedUserId;				
					formerChatToUserName = aimedUserName;
				}
				aimedUserId = "";									
				aimedUserName = "";
				chatControl.setText("public");
				confirmChat = false;
			}
			else{													
				if(!formerChatToUserId.equals("")){
					aimedUserId = formerChatToUserId;
					aimedUserName = formerChatToUserName;
					chatControl.setMargin(new Insets(0,0,0,0)); 
					chatControl.setText("private:" + aimedUserName);
					confirmChat = true;
				}
			}
		}
	}

	String formerChatToUserId = "";
	String formerChatToUserName = "";
	

	String aimedUserId = "";
	String aimedUserName = "";
	
	boolean confirmChat = false;
	boolean confirmAF = false;
	public void mouseClicked(MouseEvent e)							
	{
		if(c.debug){	System.out.println("mouseClicked");}
		if(e.getModifiers() == 4){
			for(int i = 0 ; i < playersInfors.length ; i++){
				if (e.getSource() == playersInfors[i]){				
					viewUserPM.show(playersInfors[i],e.getX(),e.getY());
					aimedUserId = playersInforsID[i];
					aimedUserName = playersInforsName[i];
					
					break;
				}
			}

			for(int i = 0 ; i < chatLabelInfor.length ; i++){
				if (e.getSource() == chatLabelInfor[i]){			
					viewUserPM.show(chatLabelInfor[i],e.getX(),e.getY());
					aimedUserId = chatLabelLineUserId[i];
					aimedUserName = chatLabelLineUserName[i];
					break;
				}
			}
		}

		if(e.getModifiers() == 16) {								
			if(isWaiting) {
				addToChatLabel("system info: waiting for system init");
				return;
			}
			if(waitForReply){
				addToChatLabel("system info: waiting for system respond");
				return;
			}
			
			for(int i = 0 ; i < cButton.length ; i++){
				if (e.getSource() == cButton[i]){
					if(!isSitted){
						c.sendMessage(clientChannel,"sitdown2;" + (i / 2) + ";" + (i % 2) + ";" + imageName);
						waitForReply = true;
					}
					else{
						addToChatLabel("system info: you have already opened one game");
					}
					break;
				}
			}
			
			for(int i = 0 ; i < playersInfors.length ; i++){
				if (e.getSource() == playersInfors[i]){				
					waitForReply = true;
					if(playerInfor != null){
						playerInfor.dispose();
						playerInfor = null;
					}
					playerInfor = new PlayerInfor(c);
					playerInfor.setInfor(playersInforsID[i],playersInforsName[i],playersInforsScore[i],playersPortrait[i]);
					c.sendMessage(clientChannel,"viewPlayersInfor;" + playersInforsID[i] + ";GameHall");
				}
			}
			
			if (e.getSource() == my_portrait){						
				JFileChooser chooser = new JFileChooser(c.portrait_path);
				int status = chooser.showOpenDialog(null);
				if(status != JFileChooser.APPROVE_OPTION)
				{	System.out.println("No file chosen!");}
				else{
					try{
						File file = chooser.getSelectedFile();
						String prefix = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
						System.out.println(prefix);
						String allowTypes = "jpg,png,gif";
						String allowtypes[] = allowTypes.split(",");
						boolean filterResult = filterType(prefix,allowtypes);
						if (filterResult) {
							File txt = new File(c.myPortraitPath);					
							FileReader filein = new FileReader(c.myPortraitPath);
							BufferedReader br = new BufferedReader(filein);
							String temp = null;
							String record = "userId = " + userId + ";imageName = " + file.getName() + "\r\n";
							while((temp = br.readLine()) != null) {
								
								String dlls[] = temp.split(";");
								String dllIds[] = dlls[0].split("=");
								String dllId = dllIds[1].trim();
								if(!dllId.equals(userId)){
									record = record + temp + "\r\n";
								}
							}
							byte []contents = (record).getBytes();
							try{
								FileOutputStream out = new FileOutputStream(txt,false);		
								out.write(contents);
								out.close();
							}
							catch (IOException ee)
							{	System.out.println(ee);}
							addToChatLabel("system info:profile updated, take effect next time.");
						}
						else{
							System.out.println("not supported file type.");
						}
					}
					catch(Exception e3)
					{	System.out.println(e3);}
				}
			}
		}
	}
	
	public boolean filterType(String fileType,String[] types) {
		for (String type : types) {
			if (fileType.equals(type)) {
				return true;
			}
		}
		return false;
	}

	public void mouseEntered(MouseEvent e){
		for(int i = 0 ; i< playersInfors.length ; i++){
			if (e.getSource() == playersInfors[i]){
				playersInfors[i].setOpaque(true);
				playersInfors[i].setBackground(c.chatMouseOnColor); 
			}
		}
		if (e.getSource() == chatControl){
			if(!aimedUserName.equals("")&&confirmChat){
				chatUser.setLabel(aimedUserName);
				showChatUser.show(chatControl,0,-30);
			}
		}
	}
	public void mouseExited(MouseEvent e) { 
		for(int i = 0 ; i< playersInfors.length ; i++){
			if (e.getSource() == playersInfors[i]){
				 playersInfors[i].setBackground(c.chatColor); 
			}
		}
	} 
	public void mouseReleased(MouseEvent e){ }
	public void mouseDragged(MouseEvent e){}
	public void mouseMoved(MouseEvent e){ }
	public void mousePressed(MouseEvent e) { } 
	public void paint(Graphics g)
	{
		super.paintComponents(g);									
	} 

	JLabel playersInfors[] = new JLabel[10];
	String playersInforsID[] = new String[10];
	String playersInforsName[] = new String[10];
	String playersInforsScore[] = new String[10];
	String playersPortrait[] = new String[10];
	String Players[];

	public void refreshGameHallPlayers(String Player)				
	{
		//format£º"userId , usreName , color , score"
		Players = Player.split("¡ü");
		String infor = "";
		showUsers.setText("");
		showUsers.removeAll();
		int perLength = 10;
		for(int i = 0 ; i < Players.length ; i++){
			String []mass = Players[i].split("¡ý");
			if(mass[0].equals("")) continue;
			playersInforsID[i] = mass[0];
			playersInforsName[i] = mass[1]; 
			playersInforsScore[i] = mass[3]; 
			playersPortrait[i] = mass[4]; 
			if(playersInforsName[i].equals(userName)){
				my_infor.setText("score:" + playersInforsScore[i]);
			}
			int nameLength = mass[1].getBytes().length;
			if(nameLength > 8){
				mass[1] = mass[1].substring(0,3) + "¡­";
				nameLength = mass[1].getBytes().length;
			}
			int idLength = mass[0].getBytes().length;
			if(idLength > 8){
				mass[0] = mass[0].substring(0,3) + "¡­";
				idLength = mass[0].getBytes().length;
			}
			String namePos = "";
			for(int j = 0;j < perLength - nameLength;j++){
				namePos += "  ";
			}
			String idPos = "";
			for(int j = 0;j < perLength - idLength;j++){
				idPos += "  ";
			}
			infor = "    player: " + mass[1] + namePos + "ID " + mass[0] + idPos +"score: " + mass[3];
			playersInfors[i] = new JLabel(infor);
			//playersInfors[i].setBorder(BorderFactory.createLineBorder(Color.gray)); 
			showUsers.add(playersInfors[i]);
			playersInfors[i].setBounds(0,20 * i , 277,20);
			showUsers.append("\r\n");
			playersInfors[i].addMouseListener(this);
		}
		addGameSum();
	}

	public void addGameSum(){
		game_sum.setText(Players.length + "players online.");
	}

	public void getBroadcastMessage(String type,String uId ,String uName,String userMessage)	
	{
		String text = "";
		int length = 7 + uName.length() + userMessage.length();
		if(type.equals("1")){
			if(userId.equals(uId)){
				text = "<html>"+addColor(">¡¾world¡¿ ","red") + addColor("you","red") + addColor(" said: ","red") + addColor(userMessage, "black")+"</html>";
				addToChatLabel(text,length);
			}
			else{
				text = "<html>"+addColor(">¡¾world¡¿ ","red") + addColor(uName,"red") + addColor("said: ","red") + addColor(userMessage, "black")+"</html>";
				addToChatLabel(text,uId,uName,length);
			}
		}
		else if(type.equals("2")){
			if(userId.equals(uId)){
				text = "<html>"+addColor(">¡¾private¡¿ ","green") + addColor("you","green") + addColor(" said: ","green") + addColor(userMessage, "black")+"</html>";
				addToChatLabel(text,length);
			}
			else{
				text = "<html>"+addColor(">¡¾private¡¿ ","green") + addColor(uName,"green") + addColor(" said: ","green") + addColor(userMessage, "black")+"</html>";
				addToChatLabel(text,uId,uName,length);
			}
		}
	}

	public String addColor(String font , String color){
		
		return "<font color = " + color + ">" + font + "</font>";
	}

	int chatLabelsum = 0;											

	int barLength = 257;		

	public void addToChatLabel(String text ,String userId ,String userName, int length){	
		idNeedClear();
		addChatLabelInfor(text);

		chatLabelLineUserId[chatLabelsum] = userId;
		chatLabelLineUserName[chatLabelsum] = userName;
		chatLabelInfor[chatLabelsum].addMouseListener(this);

		addTakePosition(length);
		sumPlus();
	}

	public void addToChatLabel(String text , int length){			
		idNeedClear();
		addChatLabelInfor(text);
		addTakePosition(length);
		sumPlus();
	}	

	public void addToChatLabel(String text){						
		idNeedClear();
		addChatLabelInfor(text);
		chatLabel.append("\r\n");
		sumPlus();
	}

	public void idNeedClear(){
		if(chatLabelsum >= c.maxChatLabelsum){
			chatLabel.setText("");
			chatLabel.removeAll();
			chatLabelsum = 0;
		}
	}

	public void addChatLabelInfor(String text){
		chatLabelInfor[chatLabelsum] = new JLabel(text);
		chatLabelInfor[chatLabelsum].setBounds(0,18 * chatLabelsum , 2000,18);
		chatLabel.add(chatLabelInfor[chatLabelsum]);
	}

	public void addTakePosition(int length){
		String takePostion = "";
		for(int i = 0 ; i < (9 * length)/10 ;i++){
			takePostion += "     ";
		}
		chatLabel.append(takePostion + "\r\n");
	}

	public void sumPlus(){
		chatLabelsum ++;
		JScrollBar bar = chatLabelScroll.getVerticalScrollBar();
		bar.setValue(bar.getMaximum());
	}

	String formerRSSInfor = "";
	boolean immediateRefresh = false;

	public void immediateRefreshSeatState(){
		immediateRefresh = true;
		refreshSeatState(formerRSSInfor);
		immediateRefresh = false;
	}

	public void refreshSeatState(String infor){						
		if((formerRSSInfor.equals(infor)&&!immediateRefresh)||infor.equals("")){
			return;
		}
		formerRSSInfor = infor;
		try{
			for(int i = 0;i < c.DeskNum; i++){
				for(int j = 0; j < 2 ;j++){
					cName[i * 2 + j].setText("");					
					ImageIcon image = new ImageIcon(c.ChairWaitImgPath);
					image = new ImageIcon(image.getImage().getScaledInstance(c.m(20),c.m(20),Image.SCALE_FAST));
					cButton[i * 2 + j].setIcon(image);				
					if(i%2==0){										
						ImageIcon image2 = new ImageIcon(c.TableWaitImgPath);
						image2 = new ImageIcon(image2.getImage().getScaledInstance(c.m(30),c.m(30),Image.SCALE_FAST));
						tButton[i / 2].setIcon(image2);				
					}
				}
			}
			if(infor.equals("none")) {								
				return;
			}
			String users[] = infor.split("¡ü");
			for(int i = 0;i < users.length; i++){
				String infors[] = users[i].split("¡ý");
				int tableNumber = Integer.parseInt(infors[0]);
				int chairNumber = Integer.parseInt(infors[1]);
				
				String userName = infors[3];
				String pictName = infors[4];
				boolean isStart = Boolean.parseBoolean(infors[5]);
				cName[tableNumber * 2 + chairNumber].setText("<html><font size = 3 color = 'white'>" + userName + "</font></html>");
				ImageIcon image = new ImageIcon(c.portrait_path + pictName);
				image = new ImageIcon(image.getImage().getScaledInstance(c.m(20),c.m(20),Image.SCALE_FAST));
				cButton[tableNumber * 2 + chairNumber].setIcon(image);			
				if(isStart){
					ImageIcon image2 = new ImageIcon(c.TableStartImgPath);
					image2 = new ImageIcon(image2.getImage().getScaledInstance(c.m(30),c.m(30),Image.SCALE_FAST));
					tButton[tableNumber].setIcon(image2);						
				}
				else{
					ImageIcon image2 = new ImageIcon(c.TableWaitImgPath);
					image2 = new ImageIcon(image2.getImage().getScaledInstance(c.m(30),c.m(30),Image.SCALE_FAST));
					tButton[tableNumber].setIcon(image2);						
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	public void showMyportrait(){
		try{
			FileReader filein = new FileReader(c.myPortraitPath);
			BufferedReader br = new BufferedReader(filein);
			String temp = null;
			while((temp = br.readLine()) != null) {
				try{
					
					String dlls[] = temp.split(";");
					String dllIds[] = dlls[0].split("=");
					String dllId = dllIds[1].trim();
					if(dllId.equals(userId)){
						String imageNames[] = dlls[1].split("=");
						if(imageNames[0].trim().equals("imageName")){	
							imageName = imageNames[1].trim();
							System.out.println("my profile image file:" + imageName);
							break;
						}
					}
				}
				catch(Exception e){
					continue;
				}
			}
			c.sendMessage(clientChannel,"setMyPortrait;" + imageName);
			paintMyPortrait();
		}
		catch (Exception e2)
		{	System.out.println(e2);}
	}

	public void paintMyPortrait(){
		myPortraitImage = new ImageIcon(c.portrait_path + imageName);
		myPortraitImage = new ImageIcon(myPortraitImage.getImage().getScaledInstance(c.m(20),c.m(20),Image.SCALE_FAST));
		my_portrait = new JLabel(myPortraitImage);
		add(my_portrait);
		my_portrait.setBounds(c.m(125), c.m(10), c.m(20), c.m(20));	
		my_portrait.addMouseListener(this);

		my_name = new JLabel(userName);
		add(my_name);
		my_name.setBounds(c.m(150), c.m(-10), c.m(50), c.m(50));	
		my_infor = new JLabel("score:"+score);
		add(my_infor);
		my_infor.setBounds(c.m(150), c.m(0), c.m(50), c.m(50));		
	}

	public void initMyImageName(){
		try{
			FileReader filein = new FileReader(c.dllPath);
			BufferedReader br = new BufferedReader(filein);
			String temp = null;
			while((temp = br.readLine()) != null) {
				try{
					
					String dlls[] = temp.split(";");
					String dllIds[] = dlls[0].split("=");
					String dllId = dllIds[1].trim();
					if(dllId.equals(userId)){
						String imageNames[] = dlls[1].split("=");
						if(imageNames[0].trim().equals("imageName")){	
							myImageName = imageNames[1].trim();
							break;
						}
					}
				}
				catch(Exception e){
					continue;
				}
			}
			System.out.println("my profile image:" + myImageName);
			setMyImageName(c.imagepath + myImageName);

			if(!getIsMyFileSended()){										
				c.sendMessage(clientChannel,"ULMImg");					//upLoadMyImage
				sendImage();											
				c.sendMessage(clientChannel,"SMImgD");					//sendMyImageDone
				setIsMyFileSended(true);
			}
		}
		catch (Exception e)
		{	System.out.println(e);}
	}

	public void sendImage(){
		int n = 0;
		try{
			File file =new File(myImageName);
			if(file.length() > c.MaxImageLength){
				System.out.println("file too large");//
				return;
			}
			FileInputStream  fr = new FileInputStream (file);
			byte[] b = new byte[c.ImageBufferSize];
			ByteBuffer sendbuffer; 
			while ((n = fr.read(b)) > 0) {	
				sendbuffer = ByteBuffer.wrap(b,0,n);
				clientChannel.write(sendbuffer);
				sendbuffer.flip();
				Thread.sleep(3);	
			}
			fr.close();
		}
		catch(Exception sendMessageError){
			System.out.println(sendMessageError);
			System.out.println("send data error");
		}
	}

	public void addFriendRequest(String reqId,String reqName){
		int i = JOptionPane.showConfirmDialog(null, "agree " + reqName +"(" + reqId + ")" + " add you as his friend?", "confirm", JOptionPane.YES_NO_OPTION);
		if(i == JOptionPane.YES_OPTION){
			c.sendMessage(clientChannel,"agreeAddFriend;" + reqId);
		}
		else{
			c.sendMessage(clientChannel,"refuseAddFriend;" + reqId);
		}
	}
} 