
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.imageio.ImageIO;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RegistJFrame extends JFrame
{
	private Constant c;
	private SocketChannel clientChannel;				 
	public JLabel hint;
	private JLabel jLabel1;
	private JTextField idtext;

	private JLabel jLabel4;
	private JTextField nametext;

	private JLabel jLabel2;
	private JButton loginbutton;
	private JButton registebutton;
	private JPasswordField passtext;
	private JLabel jLabel3;
	private JPasswordField repasstext;
	private InforChange client;

	public void setInforChange(InforChange ic){
		client = ic;
	}

	public void setPasstext(String pt){
		passtext.setText(pt);
	}

	public JTextField getIdtext(){
		return idtext;
	}

	public void setChannel(SocketChannel socketChannel)
	{	clientChannel = socketChannel;}

	RegistJFrame(Constant cc){
		super("Register");											
		c = cc;
		try {
			ImageIcon img = new ImageIcon(c.SysImgpath + "default.png");
			this.setIconImage(img.getImage());						
		} 
		catch (Exception e) {
			System.out.println(e);
		}   
	}

	public void startClient()										
	{
		setResizable(false);
		getContentPane().setLayout(null);
	
		jLabel1 = new JLabel();
		getContentPane().add(jLabel1);
		jLabel1.setText("account");
		jLabel1.setBounds(39, 30, 63, 18);
	
		idtext = new JTextField();
		getContentPane().add(idtext);
		idtext.setBounds(109, 30, 156, 22);
		
		jLabel4 = new JLabel();
		getContentPane().add(jLabel4);
		jLabel4.setText("nickname");
		jLabel4.setBounds(39, 55, 63, 18);

		nametext = new JTextField();
		getContentPane().add(nametext);
		nametext.setBounds(109, 55, 156, 22);
	
		jLabel2 = new JLabel();
		getContentPane().add(jLabel2);
		jLabel2.setText("password");
		jLabel2.setBounds(39, 80, 60, 18);
	
		passtext = new JPasswordField();
		getContentPane().add(passtext);
		passtext.setBounds(109, 80, 156, 22);

		jLabel3 = new JLabel();
		getContentPane().add(jLabel3);
		jLabel3.setText("repeatpass");
		jLabel3.setBounds(39, 105, 80, 18);

		repasstext = new JPasswordField();
		getContentPane().add(repasstext);
		repasstext.setBounds(109, 105, 156, 22);
	
		registebutton = new JButton();
		getContentPane().add(registebutton);
		registebutton.setText("register");
		registebutton.setBounds(109, 130, 80, 28);

		registebutton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String id = idtext.getText().trim();
				String name = nametext.getText().trim();
				String password = new String(passtext.getPassword()).trim();
				String repassword = new String(repasstext.getPassword()).trim();
				if(id.equals("")){
					hint.setText("account");
					name = "";
					return;
				}
				if(name.equals("")){
					hint.setText("nickname");
					name = "";
					return;
				}
				if(password.equals("")){
					hint.setText("password");
					password = "";
					return;
				}
				if(repassword.equals("")){
					hint.setText("repeat password");
					password = "";
					return;
				}
				if(!password.equals(repassword)){
					hint.setText("mismatch");
					return;
				}
				String strRegex = "[\u4e00-\u9fa5a-zA-Z0-9]*";		
				Pattern p = Pattern.compile(strRegex);
				if(!id.matches(strRegex)){
					hint.setText("invalid input");
					return;
				} 
				else if(!name.matches(strRegex)){
					hint.setText("invalid input");
					return;
				} 
				if(name != null&&name.length() > 0){
					String message = "regist;" + id + ";" + name + ";" + password;
					hint.setText("registration....");
					c.sendMessage(clientChannel,message);			
				}
			}
		});

		hint = new JLabel();
		getContentPane().add(hint);
		hint.setBounds(109, 8, 172, 23);
		this.setSize(318, 200);
		this.setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				newLoginJFrame("");
			}
		});
	}

	public void newLoginJFrame(String id){
		client.newLoginJFrame(id);
		dispose();
	}
}