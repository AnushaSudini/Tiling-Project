import java.io.IOException;


public class main {

	public static void main(String[] args){
		
		BoardFilling boardFilling = new BoardFilling();
		try{
		boardFilling.parse(args[0]);
		boardFilling.solve();
		//boardFilling.printSolutions();
		System.out.println("num of soln: "+boardFilling.sols.size());
		
		
		
		
		}catch(IOException iO){
			iO.printStackTrace();
		}
		
	}
}
