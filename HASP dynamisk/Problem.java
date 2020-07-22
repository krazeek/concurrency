import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Problem {

	private int n;


	private String[] names;


	private int[][] d;
	private Problem(String filename) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(filename));

		n = Integer.parseInt(in.readLine());

		names = new String[n];
		for (int i = 0 ; i < n ; i++)
			names[i] = in.readLine();

		d = new int[n][n];
		for (int i = 0 ; i < n ; i++) {
			d[i][i] = 0;
			for (int j = i+1 ; j < n ; j++)
				d[i][j] = d[j][i] = Integer.parseInt(in.readLine());
		}
	}

	public static void loadInstance(String fileName) throws IOException {
		instance = new Problem(fileName);
	}

	private static Problem instance;

	public static Problem getInstance() {
		return instance;
	}

	public int getN() {
		return n;
	}

	public String getName(int index) {
		return names[index];
	}

	public int getDistance(int i, int j) {
		return d[i][j];
	}
}
