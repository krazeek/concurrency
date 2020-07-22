import java.net.*;
import java.io.*;
import java.util.PriorityQueue;
public class Server{
	public static Socket[] client;

	public static int numDist;

	public static int reqDist=0;

	public static PartialTour bestSolution;

	public static PriorityQueue<PartialTour> pq;

	public static int yourTurn = 0;

	public static String chosenTour;

	public static ObjectOutputStream[] out;
	public static ObjectInputStream[] in;

	public static void offerNewSolution(PartialTour solution) {
		if (bestSolution == null || bestSolution.getLength() < solution.getLength()) {
			bestSolution = solution;
		}
	}
	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		try {
			// Her modtages socket man vil lytte paa, antal distributioner
			// og hvilken fil hvorpaa der skal beregenes.
			// Derefter beregnes en greedysolution og oprettes en prioritetskoe
			serverSocket = new ServerSocket(Integer.parseInt(args[0]));
			numDist = Integer.parseInt(args[1]);
			chosenTour = args[2];
			Problem.loadInstance(chosenTour);
			Problem probInstance = Problem.getInstance();
			bestSolution = HASP.findGreedySolution();

			pq = HASP.prioQueue(probInstance, bestSolution);

			// array af ObjectOutPutStreams, ObjectInputStreams og klienter oprettes
			out = new ObjectOutputStream[numDist];
			in = new ObjectInputStream[numDist];

			client = new Socket[numDist];
			for (int i = 0; i < numDist; i++){
				client[i] = new Socket();
			}
			System.out.println("Waiting for connections...");
		}
		catch (IOException e) {
			System.err.println("Could not listen on port:" + Integer.parseInt(args[0])); System.exit(1);
		}

		// Her bliver der forbundet til klienter indtil de paakraevede maengde
		// er tilstede
		while (reqDist != numDist) {
			try {
				client[reqDist] = serverSocket.accept();
				System.out.println("Accepting connection with "+ client[reqDist].getInetAddress().getCanonicalHostName());
				reqDist++;
			}
			catch (IOException e) {
				System.err.println("Accept failed."); System.exit(1);
			}
		}

		// her dannes streams til klienterne og filnavnet paa problemet sendes
		try{
			for (int i = 0; i < numDist; i++){
					out[i] = new ObjectOutputStream(client[i].getOutputStream());
					in[i] = new ObjectInputStream(client[i].getInputStream());
					out[i].writeObject(chosenTour);
			}
		}catch(IOException e){
			e.printStackTrace();
		}

		System.out.println("Number of required computers now correct");
		System.out.println("Distributing work to " + numDist + " computer(s).");
		reqDist = 0;
		try{

			// her toemmes priorityqueue og partialTours bliver sendt til klienterne
			while(!pq.isEmpty()){
				PartialTour ninja = pq.poll();
				out[yourTurn].writeObject(ninja);
				yourTurn = (yourTurn + 1) % numDist;

			}
			// her faar klienterne at vide, at de kan begynde beregniner

			for (int i = 0; i < numDist; i++){
				out[i].writeObject(null);
			}

			// serveren modtager bestSolutions fra klienterne og saetter
			// den bedste som loesning
			while(reqDist != numDist){
				offerNewSolution((PartialTour) in[reqDist].readObject());
				reqDist++;
				System.out.println("reqdist " + reqDist);

			}

			// serveren goer rent efter klienterne
			for (int i = 0; i < numDist; i++){
				out[i].close();
				in[i].close();
				client[i].close();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}

		System.out.println("Solution: " + bestSolution);

	}
}