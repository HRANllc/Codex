
import java.nio.channels.SocketChannel;		
import java.awt.Image;
class UAS														//UserAndSocketChannel info
{
	private String id = "";										//player id
	private String name = "";									//nick name
	private SocketChannel userChannel = null;					
	private int deskNumber = -1;								
	private int chair = -1;										
	private int uColor = -1;									
	private int score;											

	private boolean isFile = false;								//image or not
	private boolean isFileEnd = false;							//file transfered over ?
	private boolean isFileSended = false;						//sent before ?
	
	private String portrait = "";
	public int imgTempNum = -1;
	
	
	public void clear(){
		id = "";
		name = "";
		userChannel = null;
		uColor = -1;
		deskNumber = -1;
		chair = -1;
		isFile = false;
		isFileEnd = false;
		isFileSended = false;
		portrait = "";
		score = -1;
	}

	public String getId()
	{	return id; }

	public void setId(String nId)
	{	id = nId; }

	public SocketChannel getUserChannel()
	{	return userChannel; }

	public void setUserChannel(SocketChannel channel)
	{	userChannel = channel; }

	public String getName()
	{	return name; }

	public void setName(String nName)
	{	name = nName; }

	public int getScore()
	{	return score; }

	public void setScore(int s)
	{	score = s; }

	public int getDeskNumber()
	{	return deskNumber; }

	public int getChairNumber()
	{	return chair; }

	public void setDeskNumber(int desk)
	{	deskNumber = desk; }

	public void setDeskNumber(int desk, int ch)
	{	
		deskNumber = desk; 
		chair = ch;
	}

	public void setPortrait(String p)
	{	portrait = p;}

	public String getPortrait()
	{	return portrait;}

	public int getUColor()
	{	return uColor; }

	public void setUColor(int color)
	{	uColor = color; }

	public void setAll(String nId,String nName,int s,SocketChannel channel){
		id = nId;
		name = nName;
		score = s;
		userChannel = channel;
	}

	public boolean getIsFile()
	{	return isFile;}

	public void setIsFile(boolean isf)
	{	isFile = isf; }

	public boolean getIsFileEnd()
	{	return isFileEnd;}

	public void setIsFileEnd(boolean isfe)
	{	isFileEnd = isfe; }

	public boolean getIsFileSended()
	{	return isFileSended;}

	public void setIsFileSended(boolean isfes)
	{	isFileSended = isfes; }
}