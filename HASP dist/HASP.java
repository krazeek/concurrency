import java.io.IOException;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
public class HASP extends Thread{

	public static boolean verbose;

	public static PartialTour pt;

	public static PriorityBlockingQueue<PartialTour> pq;

	public static PartialTour bestSolution;

	public final static int DEPTH_FIRST_STARTING_LEVEL = 6;

	public static Problem problem;

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

	public static void offerNewSolution(PartialTour solution) {
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

	public static PriorityQueue<PartialTour> prioQueue(Problem problem, PartialTour bestSolution){
		PriorityQueue<PartialTour> queue = new PriorityQueue<PartialTour>();

		for (int i = 1; i < problem.getN(); i++) {
			for (int j = i+1; j < problem.getN(); j++) {
				int[] tmp = {i,0,j};
				pt = new PartialTour(tmp);
				if (pt.getUpperBound() > bestSolution.getLength())
					queue.add(pt);
			}
		}
		return queue;
	}
	public void run(){
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

	public HASP(PriorityBlockingQueue<PartialTour> pq, PartialTour bestSolution, Problem problem){
		this.pq = pq;
		this.bestSolution = bestSolution;
		this.problem = problem;
	}

	public static PartialTour HASPSolver(PriorityBlockingQueue<PartialTour> pq, Problem problem) throws IOException {
		PartialTour solution = findGreedySolution();

		Thread[] go = new Thread[Runtime.getRuntime().availableProcessors()];
		for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++){
			go[i] = new Thread(new HASP(pq, solution,problem));
		}
		for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++)
		{
			go[i].start();
		}
		try{
			for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++)
			{
				go[i].join();
			}
		}catch(InterruptedException e){
			System.out.println("InterruptedException");
			System.exit(1);
		}
		return bestSolution;
	}
}