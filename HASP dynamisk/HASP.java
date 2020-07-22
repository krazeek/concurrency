import java.io.IOException;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class HASP extends Thread{

	public static boolean verbose;
	public static PartialTour pt;
	public static PriorityBlockingQueue<PartialTour> pq; 	// gjort dette en global variabel
															// saa alle traade kan tilgaa den
	public static PartialTour bestSolution;

	public final static int DEPTH_FIRST_STARTING_LEVEL = 6;

	public static PartialTour findGreedySolution() {
		Problem problem = Problem.getInstance();
		PartialTour best = null;
		int n = problem.getN();

		int[] tour = new int[n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++)
				tour[j] = j;
			tour[0] = i;
			tour[i] = 0;
			for (int j = 1; j < n; j++) {
				int use = -1;
				int dist = Integer.MIN_VALUE;
				for (int k = j; k < n; k++) {
					int distHere = problem.getDistance(tour[j-1], tour[k]);
					if (distHere > dist) {
						use = k;
						dist = distHere;
					}
				}
				int tmp = tour[j];
				tour[j] = tour[use];
				tour[use] = tmp;
			}
			PartialTour tryOut = new PartialTour(tour);
			if (best == null || best.compareTo(tryOut) < 0)
				best = tryOut;
		}
		return best;
	}

	// gjort synkroniseret
	public synchronized static void offerNewSolution(PartialTour solution) {
		if (bestSolution == null || bestSolution.getLength() < solution.getLength()) {
			bestSolution = solution;
			if (verbose) {
				System.out.println("New solution found:");
				System.out.println("   " + solution);
			}
		}
	}

	public static void solveDepthFirst(PartialTour start) {
		if (start.isFullTour()) {
			offerNewSolution(start);
		} else {
			if (start.getUpperBound() < bestSolution.getLength())
				return;

			for (PartialTour child : start.getChildren())
				solveDepthFirst(child);
		}
	}

	public static void HASPSolver(String filename) throws IOException {
		Problem.loadInstance(filename);
		Problem problem = Problem.getInstance();
		bestSolution = findGreedySolution();
		if (verbose)
			System.out.println("Initial value: " + bestSolution);
		pq = new PriorityBlockingQueue<PartialTour>(1024, PartialTour.getReverseComparator());
		for (int i = 1; i < problem.getN(); i++) {
			for (int j = i+1; j < problem.getN(); j++) {
				int[] tmp = {i,0,j};
				pt = new PartialTour(tmp);
				if (pt.getUpperBound() > bestSolution.getLength())
					pq.add(pt);
			}
		}
	}
	public HASP()
	{
	}

	// her foregaar traadenes beregninger
	public void run()
	{
		try{
			while (!pq.isEmpty()) {
				PartialTour pt = pq.poll();
				for (PartialTour child : pt.getChildren()) {

					if (child.isFullTour()) {
						offerNewSolution(child);
					} else if (child.getNumberOfSolvedCities() >= DEPTH_FIRST_STARTING_LEVEL) {
						solveDepthFirst(child);
					} else if (child.getUpperBound() > bestSolution.getLength()) {
							pq.add(child);
					} else {
					}
				}

			}
		}
		catch(Exception e){
			System.exit(1);
		}
	}
	public static void main(String[] args) {
		try {
			if (args.length > 1 && args[0].equals("-v"))
				verbose = true;
			HASPSolver(args[args.length-1]);

			// her oprettes array af threads efter kerner
			Thread[] go = new Thread[Runtime.getRuntime().availableProcessors()];
			for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++)
			{
				go[i] = new Thread(new HASP());
			}

			// her startes array af threads
			for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++)
			{
				go[i].start();
			}

			// her joines arrayet af threads
			for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++)
			{
				go[i].join();
			}
			System.out.println("Solution: "+ bestSolution);

		} catch (Exception e) {
			System.out.println("Usage: java HASP filename");
		}
	}
}
