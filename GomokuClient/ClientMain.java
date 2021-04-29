
public class ClientMain {
	public static void main(String args[])
	{
		if(args.length!=1)
		{
			System.out.println("usage: java ClientMain severip");
			return;
		}
		double initMutiple = 2.0;
		Constant c  = new Constant(initMutiple,args[0]);								
		new InforChange(c);										
	}
}


