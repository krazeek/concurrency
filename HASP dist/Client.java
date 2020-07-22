import java.io.*;
import java.net.*;
import java.util.concurrent.PriorityBlockingQueue;

public class Client {
	public static void main(String[] args) throws IOException, ObjectStreamException {
		Socket hasp = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		String hostname = args[0];

		// her faar klienten at vide hvor den skal
		// finde serveren og danner input og output streams
		try {
			hasp = new Socket(hostname, Integer.parseInt(args[1])); // hostname, portnumber
			out = new ObjectOutputStream(hasp.getOutputStream());
			in = new ObjectInputStream(hasp.getInputStream());
		}
		catch (UnknownHostException e) {
			System.err.println("Don't know about host: " + hostname);
			System.exit(1);
		}
		catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: " + hostname);
			System.exit(1);
		}

		// klienten danner sin egen prioritetskoe til dens egne beregninger

		PriorityBlockingQueue<PartialTour> pq = new PriorityBlockingQueue<PartialTour>(1024, PartialTour.getReverseComparator());
		PartialTour newBest;
		PartialTour temp;
		boolean work = true;
		try{

				String chosenTour = (String) in.readObject();
				Problem.loadInstance(chosenTour);
				Problem problem = Problem.getInstance();
				// her modtages PartialTour objekter indtil der ikke er flere at modtage
				while(work){
					temp = (PartialTour)in.readObject();
					if (temp == null){
						break;
					}
					else{
						pq.add(temp);
					}
				}
				// ny bestSolution beregnes og sendes tilbage til serveren
				System.out.println("The size of the received PriorityQueue is " + pq.size());
				newBest = HASP.HASPSolver(pq,  problem);
				out.writeObject(newBest);
				System.out.println("Result written back to server.");
		}
		catch(ClassNotFoundException e){
			System.out.println("Class not found error");
		}
		catch(NullPointerException e){
			System.out.println("NullPointerException");
			e.printStackTrace();
		}
	out.close();
	in.close();
	hasp.close();
	}
}