import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Image;
import java.awt.Color;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.JTextArea;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.Date;
import java.text.SimpleDateFormat;

class Wuziqi extends JFrame implements ActionListener, MouseListener, MouseMotionListener {
	private Constant c;
	private StepRecord stepRecords[] = new StepRecord[255];			
	private SocketChannel clientChannel;							
	private int tableNumber = -1;									
	private String userId;											
	private String userName;										
	private int score;												
	private int user_color = -1;									
	private int nowColor = -1;										
	private int step = 0;											
	private boolean isStart = false;								
	private String thisId;
	private String thisName;
	private String thisColor;
	private byte myImageBytes [];
	private String oppoId;
	private String oppoName;
	private String oppoColor;
	private byte oppoImageByte[];									
	boolean isHaveOppoImage = false;								
	private String firstStartColor;									
	private boolean waitForReply = false ;							
	private boolean waitForRetractReply = false;					
	private boolean waitForDrawReply = false;						
	private String myImageName = "default.PNG";						
	private String imageName = "default.PNG";						
	public JButton startGame = new JButton("start game");
	public JButton admitLose = new JButton("give up");
	public JButton retract = new JButton("withdraw");
	public JButton draw  = new JButton("draw");
	private JButton review  = new JButton("load replay");
	private JButton setImage  = new JButton("change profile");
	private JTextArea showUsers = new JTextArea("");
	private JScrollPane	showUsersScroll = new JScrollPane(showUsers);
	private JTextArea chatLabel = new JTextArea("");
	private JScrollPane	chatLabelScroll = new JScrollPane(chatLabel);
	private JTextField messageField = new JTextField();
	private JButton sendMessage = new JButton("send");
	private JButton clearMessage = new JButton("clear");
	public JLabel lblWin = new JLabel(" ");
	public JLabel oppsInfor = new JLabel(" ");
	public JLabel myInfor = new JLabel(" ");
	private Image myImage;
	public Image oppoImage;
	private BufferedImage oppoBufferedImage;

	public void setTableNumber(int tNumber)
	{	tableNumber = tNumber;	}
	public int getTableNumber()
	{	return tableNumber;	}

	public void setWaitForRetractReply(boolean wfrr){
		waitForRetractReply = wfrr;
	}

	public void setWaitForDrawReply(boolean wfdr){
		waitForDrawReply = wfdr;
	}

	public void setMyImageName(String myIName)
	{	myImageName = myIName;	}

	public void setOppoImageByte(byte[] oImage){
		oppoImageByte = oImage;
	}

	public void setWaitForReply(boolean wtfp)
	{	waitForReply = wtfp;}

	public void setScore(int Score)
	{	score = Score;	}
	public int getScore()
	{	return score;	}

	public void setIsStart(boolean start)
	{	isStart = start;	}
	public boolean getIsStart()
	{	return isStart;	}

	public void setIsHaveOppoImage(boolean isHOI)
	{	isHaveOppoImage = isHOI;	}

	public boolean getIsHaveOppoImage()
	{	return isHaveOppoImage;	}

	public void setChannel(SocketChannel socketChannel)
	{	clientChannel = socketChannel;	}
	public SocketChannel getChannel()
	{	return clientChannel;	}

	public void setUser(String uId,String uName,int sc){
		userId = uId;
		userName = uName;
		score = sc;
	}
	public void setColor(int uColor)
	{	user_color = uColor;}

	Wuziqi(Constant cc){
		super("Gomoku");											
		c = cc;
		try {
			ImageIcon img = new ImageIcon(c.SysImgpath + "default.png");
			this.setIconImage(img.getImage());						
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}

	public void init()												
	{
		for(int i = 0; i < stepRecords.length ; i++)
		{	stepRecords[i] = new StepRecord();	}

		ImageIcon img = new ImageIcon(c.SysImgpath + "bg2.jpg");
		JLabel bgLabel = new JLabel(img);
		bgLabel.setBounds(c.m(0), c.m(-8), c.m(512), c.m(425));
		this.getLayeredPane().add(bgLabel, new Integer(Integer.MIN_VALUE));
		((JPanel)getContentPane()).setOpaque(false);

		setLayout(null);
		addMouseListener(this);
		setResizable(false);

		add(oppsInfor);
		oppsInfor.setBounds(c.m(0) + c.dev_x, c.m(20), c.m(70), c.m(30));			

		add(setImage);
		setImage.setBounds(c.m(3) + c.dev_x, c.m(320), c.m(45), c.m(20));			
		setImage.addActionListener(this);

		add(startGame);
		startGame.setBounds(c.m(70) + c.dev_x, c.m(320), c.m(45), c.m(20));			
		startGame.addActionListener(this);

		add(admitLose);
		admitLose.setBounds(c.m(150) + c.dev_x, c.m(320), c.m(45), c.m(20));		
		admitLose.addActionListener(this);
		admitLose.setEnabled(false);

		add(retract);
		retract.setBounds(c.m(230) + c.dev_x, c.m(320), c.m(45), c.m(20));			
		retract.addActionListener(this);
		retract.setEnabled(false);

		add(draw);
		draw.setBounds(c.m(310) + c.dev_x, c.m(320), c.m(45), c.m(20));				
		draw.addActionListener(this);
		draw.setEnabled(false);

		add(review);
		review.setBounds(c.m(390) + c.dev_x, c.m(320), c.m(45), c.m(20));			
		review.addActionListener(this);

		showUsersScroll.setBounds(c.m(380) + c.dev_x, c.m(30), c.m(110), c.m(70));	
		add(showUsersScroll);
		showUsers.setOpaque(true);
		showUsers.setBackground(c.chatColor);
		showUsers.setEditable(false);

		chatLabelScroll.setBounds(c.m(380) + c.dev_x, c.m(120), c.m(110), c.m(150));
		add(chatLabelScroll);
		chatLabel.setOpaque(true);
		chatLabel.setBackground(c.chatColor);
		chatLabel.setEditable(false);

		add(messageField);
		messageField.setBounds(c.m(380) + c.dev_x, c.m(270), c.m(110), c.m(15));	
		messageField.addActionListener(this);
		messageField.setBorder (BorderFactory.createLineBorder(Color.gray,1));
		messageField.setOpaque(true);
		messageField.setBackground(c.chatColor);

		add(sendMessage);
		sendMessage.setBounds(c.m(380) + c.dev_x, c.m(290), c.m(40), c.m(15));		
		sendMessage.addActionListener(this);

		add(clearMessage);
		clearMessage.setBounds(c.m(450) + c.dev_x, c.m(290), c.m(40), c.m(15));		
		clearMessage.addActionListener(this);

		add(lblWin);
		lblWin.setBounds(c.m(0) + c.dev_x, c.m(160), c.m(70), c.m(30));				

		add(myInfor);
		myInfor.setBounds(c.m(0) + c.dev_x, c.m(220), c.m(70), c.m(30));			

		startGame.setMargin(new Insets(0,0,0,0));
		admitLose.setMargin(new Insets(0,0,0,0));
		retract.setMargin(new Insets(0,0,0,0));
		draw.setMargin(new Insets(0,0,0,0));
		review.setMargin(new Insets(0,0,0,0));
		sendMessage.setMargin(new Insets(0,0,0,0));
		clearMessage.setMargin(new Insets(0,0,0,0));
		setImage.setMargin(new Insets(0,0,0,0));

		ImageIcon icon = new ImageIcon(myImageName);
		icon.setImage(icon.getImage().getScaledInstance(icon.getIconWidth(),icon.getIconHeight(), Image.SCALE_DEFAULT));
		myImage = icon.getImage();									

		this.setSize(c.wsizex,c.wsizey);
		this.setLocationRelativeTo(null);

		setVisible(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){					
			public void windowClosing(WindowEvent e){
				if(isStart && user_color >= 0){
					int i = JOptionPane.showConfirmDialog(null, "exit game?", "exit game", JOptionPane.YES_NO_OPTION);
					if(i == JOptionPane.YES_OPTION){
						String newcontent = "closeTable;" + getTableNumber();
						c.sendMessage(clientChannel,newcontent);
						dispose();
					}
				}
				else{
					String newcontent = "closeTable;" + getTableNumber();
					c.sendMessage(clientChannel,newcontent);
					dispose();
				}
			}
		});
		addToChatLabel("system info: you have entered room " + (tableNumber + 1));	
	}

	public void initOppoImage(){									
		ImageIcon icon = new ImageIcon(oppoImageByte);

		icon.setImage(icon.getImage().getScaledInstance(icon.getIconWidth(),
		icon.getIconHeight(), Image.SCALE_DEFAULT));
		oppoImage = icon.getImage();
		isHaveOppoImage = true;
		paint(this.getGraphics());									

		System.out.println(icon.getIconHeight() + "," + icon.getIconWidth());
		c.sendMessage(clientChannel,"initOppoPictOk;" + tableNumber);
	}

	public void initGame(String result)								
	{
		isStart = false;
		clearRecords();
		startGame.setEnabled(false);
		String []serverInfor = result.split(";");
		int initStep = Integer.parseInt(serverInfor[3]);
		for(int i = 4;i < initStep + 4; i++)
		{
			String []locate =serverInfor[i].split(",");
			int initi = Integer.parseInt(locate[0]);
			int initj = Integer.parseInt(locate[1]);
			int initColor = Integer.parseInt(locate[2]);
			stepRecords[i - 4].setStepRecord(initi,initj,initColor);
		}
		step = initStep;
		paint(this.getGraphics());
	}

	public void clearRecords()										
	{
		for(int i = 0; i < stepRecords.length ; i++)
		{	stepRecords[i].setStepRecord(-1,-1,-1);	}
	}

	public void start(String tcolor)								
	{
		if(isStart){												
			return;
		}
		firstStartColor = tcolor;
		String ucolor = startColor(Integer.parseInt(tcolor));
		lblWin.setText("game started " + ucolor + " first");
		step = 0;
		isStart = true;
		startGame.setEnabled(false);
		admitLose.setEnabled(false);
		retract.setEnabled(true);
		draw.setEnabled(true);
		addToChatLabel("game info: game started");
		if(user_color == -2){										
			startGame.setEnabled(false);
			admitLose.setEnabled(false);
			retract.setEnabled(false);
			draw.setEnabled(false);
			clearRecords();
			SetWinLineRecord("-1","-1","-1","-1");
			repaint();												
		}
	}

	private int winLineX = -1;
	private int winLineY = -1;
	private int winLineDirection = -1;
	private int winLinePlusSum = -1;

	public void SetWinLineRecord(String sx,String sy,String sdirec,String splus){					
		winLineX = Integer.parseInt(sx);
		winLineY = Integer.parseInt(sy);
		winLineDirection = Integer.parseInt(sdirec);
		winLinePlusSum = Integer.parseInt(splus);
	}

	public void drawWinLine(){										

		if(winLineDirection < 0) return;

		int wx = c.WLen * winLineY;
		int wy = c.WLen * winLineX;									
		int lineLength = winLinePlusSum * c.WLen;
		int opplineLength = (4 - winLinePlusSum) * c.WLen;

		Graphics graphics = this.getGraphics();
		Graphics2D g2D = (Graphics2D)graphics;
		float lineWidth = 3.0f;
		g2D.setStroke(new BasicStroke(lineWidth));

		g2D.setColor(Color.red);

		if(winLineDirection == 1){
			g2D.drawLine( wx + c.dev_y , wy + c.dev_x + lineLength ,  wx +c.dev_y, wy + c.dev_x);
			g2D.drawLine( wx + c.dev_y , wy + c.dev_x - opplineLength ,  wx +c.dev_y, wy + c.dev_x);
		}

		if(winLineDirection == 2){
			g2D.drawLine( wx + c.dev_y , wy + c.dev_x,  wx + c.dev_y + lineLength , wy + c.dev_x);
			g2D.drawLine( wx + c.dev_y , wy + c.dev_x,  wx + c.dev_y - opplineLength , wy + c.dev_x);
		}

		if(winLineDirection == 3){
			g2D.drawLine( wx + c.dev_y , wy + c.dev_x,  wx + c.dev_y - lineLength , wy + c.dev_x + lineLength);
			g2D.drawLine( wx + c.dev_y , wy + c.dev_x,  wx + c.dev_y + opplineLength, wy + c.dev_x - opplineLength);
		}

		if(winLineDirection == 4){
			g2D.drawLine( wx + c.dev_y, wy + c.dev_x,  wx + c.dev_y + lineLength, wy + c.dev_x + lineLength);
			g2D.drawLine( wx + c.dev_y, wy + c.dev_x,  wx + c.dev_y - opplineLength, wy + c.dev_x - opplineLength);
		}
	}

	public void gameEnd()											
	{
		startGame.setEnabled(true);
		admitLose.setEnabled(false);
		retract.setEnabled(false);
		draw.setEnabled(false);
		if(user_color == -2)										
		{	startGame.setText("continue viewing");}
		if(!isStart) return;										
		isStart = false;
		if(step > 0) {
			int j = JOptionPane.showConfirmDialog(null, "save the chess manual?", "save", JOptionPane.YES_NO_OPTION);
			if(j == JOptionPane.YES_OPTION){
				String saveMessage = "";
				for(int i = 0;i < step;i++){
					saveMessage += stepRecords[i].getX() + "," + stepRecords[i].getY() + ","+stepRecords[i].getColor() + ";";
				}
				Date now = new Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
				String nowtime = dateFormat.format(now);
				File newfile = new File(c.chessManualPath + thisName + " vs " + oppoName + " " + nowtime + ".txt");
				byte []gameRcords = (firstStartColor + ";" + thisId + "," + thisName + "," + thisColor + ";" + oppoId + "," + oppoName + "," + oppoColor + ";" + step + ";" +winLineDirection + ";" + winLinePlusSum + ";" + saveMessage).getBytes();
				try{
					FileOutputStream out = new FileOutputStream(newfile);
					out.write(gameRcords);
					out.close();
					addToChatLabel("gameinfo: save successfully");
				}
				catch (IOException e){
					System.out.println(e);
				}
			}
		}
		c.sendMessage(clientChannel,"saveDone;" + tableNumber + ";");
	}

	public void actionPerformed(ActionEvent e)
	{
		if(waitForReply){											
			addToChatLabel("system inof: waiting for server repond...");
			return;
		}
		if (e.getSource() == startGame && user_color <= 1) {		
			if(isStart)	return;
			String newcontent = "ready;" + tableNumber + ";";
			c.sendMessage(clientChannel,newcontent);
			clearRecords();
			step = 0;
			SetWinLineRecord("-1","-1","-1","-1");
			repaint();
			startGame.setEnabled(false);
		}
		else if (e.getSource() == sendMessage || e.getSource() == messageField) {		
			String text = messageField.getText();
			if(text.equals(""))	return;
			String newcontent = "userMessage;" + tableNumber + ";" + text;
			c.sendMessage(clientChannel,newcontent);
			messageField.setText("");
		}
		else if (e.getSource() == clearMessage) {
			String text = chatLabel.getText();
			if(text.equals(""))	return;
			chatLabel.setText("");
		}
		else if (e.getSource() == retract) {						
			if(waitForRetractReply){
				addToChatLabel("game info: waiting for reply...");
				return;
			}
			if((isStart!=true)||(step < 1&&user_color == nowColor)||(step < 2&&user_color != nowColor)||(user_color < 0)){
				addToChatLabel("game info: can't do it");
				return;
			}
			waitForRetractReply = true;
			retract.setEnabled(false);
			String newcontent = "rollbackRequest;" + tableNumber + ";" + step + ";";
			c.sendMessage(clientChannel,newcontent);
		}
		else if (e.getSource() == admitLose) {						

			if((step < 7)||(!isStart)||(user_color < 0 ))	{
				addToChatLabel("game info: can't give up now");
				return;
			}
			int i = JOptionPane.showConfirmDialog(null, "give up?", "give up", JOptionPane.YES_NO_OPTION);
			if(i == JOptionPane.YES_OPTION){
				String newcontent = "admitLose;" + tableNumber + ";" + user_color;
				c.sendMessage(clientChannel,newcontent);
			}
		}
		else if (e.getSource() == draw) {							
			if(waitForDrawReply){
				addToChatLabel("game info: waiting for reply...");
				return;
			}
			if((user_color < 0 )||(!isStart))	{
				addToChatLabel("game info: can't do it");
				return;
			}
			
			waitForDrawReply = true;
			draw.setEnabled(false);
			String newcontent = "drawRequest;" + tableNumber + ";";
			c.sendMessage(clientChannel,newcontent);
		}
		else if (e.getSource() == review) {							
			if(isStart){
				addToChatLabel("game info: can't do it!");
				return;
			}
			JFileChooser chooser = new JFileChooser(c.chessManualPath);
			int status = chooser.showOpenDialog(null);
			if(status != JFileChooser.APPROVE_OPTION){
				System.out.println("No file chosen!");
				return;
			}
			try{
				File file = chooser.getSelectedFile();
				Scanner scan = new Scanner(file);
				String info = "";
				while(scan.hasNext())
				{	info += scan.nextLine();}
				System.out.println(info);
				scan.close();
				Review review = new Review(c);
				review.initGame(info);
			}
			catch(Exception e3)
			{	System.out.println(e3);}
		}
		if (e.getSource() == setImage) {							
			JFileChooser chooser = new JFileChooser();
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
						int byteread = 0;
						if (file.exists()) {									
							InputStream inStream = new FileInputStream(file);	
							FileOutputStream fs = new FileOutputStream(c.imagepath + userId + "-" + file.getName());
							byte[] buffer = new byte[1024];
							//int length;
							while ( (byteread = inStream.read(buffer)) != -1) {
								fs.write(buffer, 0, byteread);
							}
							inStream.close();
							fs.close();
						}
						File txt = new File(c.dllPath);							
						FileReader filein = new FileReader(c.dllPath);
						BufferedReader br = new BufferedReader(filein);
						String temp = null;
						String record = "userId = " + userId +";imageName = " + userId + "-" + file.getName()+ "\r\n";
						while((temp = br.readLine()) != null) {
							if(temp.trim().equals("")) continue;
							System.out.println("temp = "+temp);
							String dlls[] = temp.split(";");
							String dllIds[] = dlls[0].split("=");
							String dllId = dllIds[1].trim();
							if(!dllId.equals(userId)){
								record = record + temp + "\r\n";
							}
						}
						filein.close();
						br.close();
						byte []contents = (record).getBytes();
						try{
							FileOutputStream out = new FileOutputStream(txt,false);		
							out.write(contents);
							out.close();
						}
						catch (IOException ee)
						{	System.out.println(ee);}
						addToChatLabel("system inof: restart the game to take effect");
					}
					else{
						System.out.println("file type not supported");
					}
				}
				catch(Exception e3)
				{	System.out.println(e3);}
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

	PlayerInfor playerInfor = null;

	public void mouseClicked(MouseEvent e)							
	{
		if(e.getModifiers() != 16) return;							
		
		if(waitForReply == true){
			addToChatLabel("system info: waiting for server's reply...");
			return;
		}
		for(int i = 0 ; i < viewersInfors.length ; i++){
			if (e.getSource() == viewersInfors[i]){					
				waitForReply = true;
				if(playerInfor != null){
					playerInfor.dispose();
					playerInfor = null;
				}
				playerInfor = new PlayerInfor(c);
				playerInfor.setInfor(viewersInforsID[i],viewersInforsName[i],viewersInforsScore[i],viewersPortrait[i]);
				c.sendMessage(clientChannel,"viewPlayersInfor;" + viewersInforsID[i] + ";Wuziqi");
			}
		}
		if (isStart){												
			int x, y;
			final int localx,localy;
			final String sx,sy;
			x = e.getX(); y = e.getY();
			x -= c.dev_y; y -= c.dev_x;
			if (x < c.halflength || x > c.maxlength + c.halflength || y < c.halflength || y > c.maxlength +c.halflength )
			{  return;  }
			if (x % c.WLen > c.halflength)
			{  x += c.WLen;  }
			if (y % c.WLen > c.halflength)
			{  y += c.WLen;  }
			x = x / c.WLen;
			y = y / c.WLen;
			sx=x + "";
			sy=y + "";
			localx = x;
			localy = y;
			String newcontent = "started;" + tableNumber + ";" + sy + ";" + sx + ";" + user_color + ";";		
			c.sendMessage(clientChannel,newcontent);								
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
				 viewersInfors[i].setBackground(c.chatColor);		
			}
		}
	}
	public void mouseReleased(MouseEvent e){ }
	public void mouseDragged(MouseEvent e){ }
	public void mouseMoved(MouseEvent e){ }
	public void mousePressed(MouseEvent e) { }
	public void paint(Graphics g)									
	{
		super.paintComponents(g);									
		g.setColor(Color.lightGray);								
		
		g.fill3DRect(c.halflength+c.dev_y, c.halflength+c.dev_x, c.maxlength, c.maxlength, true);		
		g.setColor(Color.black);									
		//g.setColor(Color.white);									
		for (int i = 1; i < 16; i++){
			g.drawLine( c.WLen+c.dev_y,  c.WLen*i+c.dev_x,  c.maxlength+c.dev_y,  c.WLen*i+c.dev_x);	
			g.drawLine(c.WLen*i+c.dev_y,  c.WLen+c.dev_x,   c.WLen*i+c.dev_y, c.maxlength+c.dev_x);	
		}
		for(int i = 0 ; i < stepRecords.length ; i++)				
		{
			if(stepRecords[i].getX() < 0)
			{	break;}
			int wy = c.WLen*stepRecords[i].getX();					
			int wx = c.WLen*stepRecords[i].getY();
			int set_color  = stepRecords[i].getColor();
			if (set_color == 0)	{									
				g.setColor(Color.white);
				g.fillOval(wx - c.halflength + c.dev_y, wy - c.halflength + c.dev_x, c.WLen, c.WLen);   
				g.setColor(Color.black);							
			}
			else{
				g.setColor(Color.black);
				g.fillOval(wx - c.halflength + c.dev_y, wy - c.halflength + c.dev_x, c.WLen, c.WLen);   
				g.setColor(Color.white);							
			}
			g.fillOval(c.m(20) + c.dev_x, c.m(150), c.WLen, c.WLen);						

			if(step > 1){
				int formerY = stepRecords[step - 2].getX();
				int formerX = stepRecords[step - 2].getY();
				int formerColor = stepRecords[step - 2].getColor();

				int formerx = c.WLen * formerX;
				int formery = c.WLen * formerY;
				if (set_color == 1){								
					g.setColor(Color.white);						
					g.fillOval(formerx - c.halflength + c.dev_y, formery - c.halflength + c.dev_x, c.WLen, c.WLen);   
				}
				else{
					g.setColor(Color.black);						
					g.fillOval(formerx - c.halflength + c.dev_y, formery - c.halflength + c.dev_x, c.WLen, c.WLen);   
				}
			}
			if(i == step - 1){
				g.setColor(Color.red);
				g.drawLine(wx + c.dev_y , wy + c.dev_x - c.redlength, wx + c.dev_y , wy + c.dev_x + c.redlength);
				g.drawLine(wx + c.dev_y - c.redlength, wy + c.dev_x , wx + c.dev_y + c.redlength , wy + c.dev_x );
			}
		}
		drawWinLine();
		g.drawImage(myImage,c.m(20) ,c.m(270) ,c.m(45) ,c.m(65) ,this);
		g.drawImage(oppoImage,c.m(20) ,c.m(73) ,c.m(45) ,c.m(65) ,this);
	}

	public void myPaint(){
		paint(this.getGraphics());
	}
	public void setDown(int x, int y, int set_color)				
	{
		stepRecords[step].setStepRecord(x,y,set_color);				
		Graphics s_graphics = this.getGraphics();
		//System.out.println(x + "," + y);
		int wy = c.WLen * x;										
		int wx = c.WLen * y;
		if (set_color == 0){										
			s_graphics.setColor(Color.white);						
			s_graphics.fillOval(wx - c.halflength + c.dev_y, wy - c.halflength + c.dev_x, c.WLen, c.WLen);   
			s_graphics.setColor(Color.black);						
		}
		else{
			s_graphics.setColor(Color.black);						
			s_graphics.fillOval(wx - c.halflength + c.dev_y, wy - c.halflength + c.dev_x, c.WLen, c.WLen);   
			s_graphics.setColor(Color.white);						
		}
		s_graphics.fillOval(c.m(20) + c.dev_x, c.m(150), c.WLen, c.WLen);						
		step ++;
		if((step >= 7)&&(isStart)&&(user_color >= 0 ))	{
			admitLose.setEnabled(true);
		}
		nowColor = (set_color + 1)%2;

		if(step > 1){												
			int formerY = stepRecords[step - 2].getX();
			int formerX = stepRecords[step - 2].getY();
			int formerColor = stepRecords[step - 2].getColor();

			int formerx = c.WLen * formerX;
			int formery = c.WLen * formerY;
			if (set_color == 1){									
				s_graphics.setColor(Color.white);
				s_graphics.fillOval(formerx - c.halflength + c.dev_y, formery - c.halflength + c.dev_x, c.WLen, c.WLen);   
			}
			else{
				s_graphics.setColor(Color.black);
				s_graphics.fillOval(formerx - c.halflength + c.dev_y, formery - c.halflength + c.dev_x, c.WLen, c.WLen);   
			}
		}
		s_graphics.setColor(Color.red);								
		s_graphics.drawLine(wx + c.dev_y , wy + c.dev_x - c.redlength, wx + c.dev_y , wy + c.dev_x + c.redlength);
		s_graphics.drawLine(wx + c.dev_y - c.redlength, wy + c.dev_x , wx + c.dev_y + c.redlength , wy + c.dev_x );

		lblWin.setText("step " + step + " " + startColor((set_color + 1) % 2) + " on move");
	}
	public String startColor(int x)
	{
		if (x == 0) { return "white"; }
		else { return "black"; }
	}

	public void sendImage(byte [] myImageBytes)						
	{
		int n = 0;
		try{
			File file =new File(myImageName);
			if(file.length() > c.MaxImageLength){
				System.out.println("file too large");//
				return;
			}
			FileInputStream  fr = new FileInputStream (file);
			byte[] b = new byte[1024];
			ByteBuffer sendbuffer;
			while ((n = fr.read(b)) > 0) {
				sendbuffer = ByteBuffer.wrap(b,0,n);
				clientChannel.write(sendbuffer);
				sendbuffer.flip();
				Thread.sleep(3);	
			}
			fr.close();
		}
		catch(Exception error){
			System.out.println(error);
			System.out.println("send data error");
		}
	}

	public void getMessage(String uId ,String uName,String userMessage)		
	{
		String text = "";
		if(userId.equals(uId)){
			text = "you said: " + userMessage;
		}
		else{
			text = uName + "said: " + userMessage;
		}
		addToChatLabel(text);
	}

	public void addToChatLabel(String text){						
		if(chatLabel.getText().equals(""))
		{	chatLabel.append(text);}
		else chatLabel.append("\r\n"+text );
		JScrollBar bar = chatLabelScroll.getVerticalScrollBar();
		bar.setValue(bar.getMaximum());
	}

	public void getViewersInforPartly()								
	{
		String newcontent = "refreshViewersInforPartly;" + tableNumber + ";";				
		c.sendMessage(clientChannel,newcontent);
	}

	String formerViewersInfor = "";									
	JLabel viewersInfors[] = new JLabel[10];
	String viewersInforsID[] = new String[10];
	String viewersInforsName[] = new String[10];
	String viewersInforsScore[] = new String[10];
	String viewersPortrait[] = new String[10];

	public void refreshViewersInfor(String Viewer)					
	{
		//format："userId , usreName , color , score"
		String []viewers = Viewer.split("↑");
		int viewersLen = 0;
		String []formerViewers = formerViewersInfor.split("↑");
		int formersLen = 0;

		for(int i = 0;i< viewers.length ;i++){
			if(viewers[i].trim().equals("")){
				viewersLen = i;
				break;
			}
			else{
				viewersLen++;
			}
		}

		for(int i = 0;i< formerViewers.length ;i++){
			if(formerViewers[i].trim().equals("")){
				formersLen = i;
				break;
			}
			else{
				formersLen++;
			}
		}

		if(!Viewer.equals(formerViewersInfor)){
			for(int i = 0;i< viewersLen ;i++){
				boolean isExist = false;
				String []viewerMasses = viewers[i].split("↓");
				for(int j = 0;j< formersLen ;j++){
					String []formerMasses = formerViewers[j].split("↓");
					if(viewerMasses[0].trim().equals(formerMasses[0].trim())){
						isExist = true;
						break;										
					}
				}
				if(!isExist){
					if(!viewers[i].trim().equals("")){
						String []mass = viewers[i].split("↓");
						addToChatLabel("game info: " + mass[1]+" entered");
					}
				}
			}
			for(int i = 0;i < formersLen ;i++){
				boolean isExist = false;
				String []formerMasses = formerViewers[i].split("↓");
				for(int j = 0;j < viewersLen ;j++){
					String []viewerMasses = viewers[j].split("↓");
					if(viewerMasses[0].equals(formerMasses[0].trim())){
						isExist = true;
						break;										
					}
				}
				if(!isExist){
					if(!formerViewers[i].trim().equals("")){
						String []mass = formerViewers[i].split("↓");
						addToChatLabel("game info: " + mass[1]+" left");
					}
				}
			}
			formerViewersInfor = Viewer;
		}

		String infor = "";
		showUsers.setText("");
		int perLength = 10;											
		showUsers.removeAll();
		for(int i = 0 ; i < viewersLen ; i++)						
		{
			String []mass = viewers[i].split("↓");
			if(mass[0].equals("")) continue;
			viewersInforsID[i] = mass[0];
			viewersInforsName[i] = mass[1];
			viewersInforsScore[i] = mass[3];
			viewersPortrait[i] = mass[4];
			int nameLength = mass[1].getBytes().length;
			if(nameLength > 8){
				mass[1] = mass[1].substring(0,3)+"…";
				nameLength = mass[1].getBytes().length;
			}
			String namePos = "";
			for(int j= 0;j < perLength - nameLength;j++){
				namePos += "  ";
			}
			if(!isStart && (mass[2].equals("0")||mass[2].equals("1")) && !mass[1].equals(userName) && user_color == -1){
				addToChatLabel("game info: " + mass[1]+" ready");
			}

			String vColor = "";
			if(mass[2].equals("0")&&(user_color == -2)){			
				vColor = " white";
			}
			else if(mass[2].equals("1")&&(user_color == -2)){		
				vColor = " black";
			}
			infor = "    player " + mass[1] + namePos  +"score " + mass[3] + vColor;
			viewersInfors[i] = new JLabel(infor);
			
			showUsers.add(viewersInfors[i]);						
			viewersInfors[i].setBounds(0,20 * i,217,20);
			showUsers.append("\r\n");
			viewersInfors[i].addMouseListener(this);
		}
	}

	public void getGamersInforPartly()
	{
		String newcontent = "refreshGamersInforPartly;" + tableNumber + ";";				
		c.sendMessage(clientChannel,newcontent);
	}

	public void refreshGamersInfor(String infor1,String infor2)		
	{
		//format："userId , usreName , color , score"
		String thisScore = "";
		String oppoScore = "";
		if(user_color < 0) {										
			String []mass1 = infor1.split(",");
			thisId = mass1[0];
			thisName = mass1[1];
			thisColor = mass1[2];
			thisScore = mass1[3];

			String []mass2 = infor2.split(",");
			oppoId = mass2[0];
			oppoName = mass2[1];
			oppoColor = mass2[2];
			oppoScore = mass2[3];
			return ;												
		}
		String []mass1 = infor1.split(",");							
		if(mass1[0].equals(userId)){								
			myInfor.setText(mass1[1] + "  " + mass1[3]);
			thisId = mass1[0];
			thisName = mass1[1];
			thisColor = mass1[2];
			thisScore = mass1[3];
		}
		else {														
			oppsInfor.setText(mass1[1] + "  " + mass1[3]);
			oppoId = mass1[0];
			oppoName = mass1[1];
			oppoColor = mass1[2];
			oppoScore = mass1[3];
		}

		String []mass2 = infor2.split(",");							
		if(mass2[0].equals(userId)){								
			myInfor.setText(mass2[1] + "  " + mass2[3]);
			thisId = mass2[0];
			thisName = mass2[1];
			thisColor = mass2[2];
			thisScore = mass2[3];
		}
		else {														
			oppsInfor.setText(mass2[1] + "  " + mass2[3]);
			oppoId = mass2[0];
			oppoName = mass2[1];
			oppoColor = mass2[2];
			oppoScore = mass2[3];
		}

		if(!isStart) return ;										
		String mycolor = startColor(user_color);
		String infor = thisName + "  " + thisScore;
		infor = infor + "  is " + mycolor;
		myInfor.setText(infor);										

		String oppositecolor;
		if(user_color==0){
			oppositecolor = "black";
		}
		else{
			oppositecolor = "white";
		}
		infor = oppoName + "  " + oppoScore;
		infor = infor + " is " + oppositecolor;
		oppsInfor.setText(infor);									
	}

	public void refreshGamersInfor(String infor1)					
	{
		//format："userId , usreName , color , score"
		if(user_color < 0) {
			return ;
		}
		String []mass1 = infor1.split(",");
		if(Integer.parseInt(mass1[2]) == user_color){
			myInfor.setText(mass1[1] + "  " + mass1[3]);
			thisId = mass1[0];
			thisName = mass1[1];
			thisColor = mass1[2];
		}

		if(!isStart) return ;
		String mycolor = startColor(user_color);
		String infor = mass1[1] + "  " + mass1[3];
		infor = infor + "  is" + mycolor;
		myInfor.setText(infor);
	}

	public void replyRBForward(String step)							
	{
		int reply;
		int i = JOptionPane.showConfirmDialog(null, "do you agree to withdraw?", "agree", JOptionPane.YES_NO_OPTION);
		if(i == JOptionPane.YES_OPTION){
			reply = 1;
		}
		else reply = 0;
		String newcontent = "replyRBForward;" + getTableNumber() + ";" + reply + ";" + step + "";
		c.sendMessage(clientChannel,newcontent);
	}

	public void rollback(int rstep,int St)
	{
		if(St == 1){
			stepRecords[step - 1].setStepRecord(-1,-1,-1);
			step = step - 1;
		}
		else if(St == 2){
			stepRecords[step - 1].setStepRecord(-1,-1,-1);
			stepRecords[step - 2].setStepRecord(-1,-1,-1);
			step = step - 2;
		}
		waitForRetractReply = false;
		retract.setEnabled(true);
		paint(this.getGraphics());
	}

	public void noRollback(int requestColor){
		if(user_color == requestColor){
			addToChatLabel("game info: opponent refused to withdraw");
			setWaitForRetractReply(false);
			retract.setEnabled(true);
		}
	}

	public void drawRequest()										
	{
		int reply;
		int i = JOptionPane.showConfirmDialog(null, "do you agree to draw?", "agree", JOptionPane.YES_NO_OPTION);
		if(i == JOptionPane.YES_OPTION){
			reply = 1;
		}
		else reply = 0;
		String newcontent = "drawRequestReply;" + getTableNumber() + ";" + reply + ";";
		c.sendMessage(clientChannel,newcontent);
	}
}

class StepRecord													
{
	private int x = -1;
	private int y = -1;
	private int color = -1;
	public void setStepRecord(int rx,int ry,int rcolor)
	{
		x = rx;
		y = ry;
		color = rcolor;
	}
	public int getX()
	{	return x;	}
	public int getY()
	{	return y;	}
	public int getColor()
	{	return color;	}
}
