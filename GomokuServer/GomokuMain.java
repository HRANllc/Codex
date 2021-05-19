
public class GomokuMain {
	
	public static void main(String args[])
	{
		if(args.length!=1)
		{
			System.out.println("usage: java GomokuMain severip");
			return;
		}
		Constant c = new Constant(args[0]);								
		new Server(c);		
	}
}

