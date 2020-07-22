import java.util.Comparator;
import java.util.LinkedList;
import java.io.*;

public class PartialTour implements Comparable<PartialTour>, Serializable {

	private int[] cities;

	private int[] pending;

	private int length;

	private int upperBound;

	public PartialTour(int[] cities) {
		if (cities == null || cities.length == 0)
			throw new IllegalArgumentException("Need to know at least one city!");

		Problem problem = Problem.getInstance();
		int n = problem.getN();
		this.cities = cities.clone();
		this.pending = new int[n - this.cities.length];
		boolean[] isUsed = new boolean[n]; // initially all false
		for (int city: cities)
			isUsed[city] = true;
		for (int i = 0, j = 0; i < n; i++) {
			if (!isUsed[i])
				this.pending[j++] = i;
		}

		int prevCity = cities.length == n ? cities[n-1] : cities[0];
		length = 0;
		for (int city: cities) {
			length += problem.getDistance(prevCity, city);
			prevCity = city;
		}
		calculateUpperBound();
	}

	private void calculateUpperBound() {
		if (pending.length == 0) {
			upperBound = length;
			return;
		}
		float tmpBound = this.length;
		Problem problem = Problem.getInstance();

		int bound0 = Integer.MIN_VALUE;
		int boundk = Integer.MIN_VALUE;

		for (int city : pending) {
			bound0 = Math.max(bound0, problem.getDistance(cities[0], city));
			boundk = Math.max(bound0, problem.getDistance(cities[cities.length-1], city));
		}
		tmpBound += (bound0 + boundk) / 2.;

		for (int pcity : pending) {
			int tmp0 = problem.getDistance(pcity, cities[0]);
			int tmp1 = problem.getDistance(pcity, cities[cities.length-1]);
			if (tmp0 < tmp1) {
				int tmp = tmp0;
				tmp0 = tmp1;
				tmp1 = tmp;
			}

			for (int ocity : pending) {
				if (ocity != pcity) {
					int tmp = problem.getDistance(pcity, ocity);
					if (tmp > tmp0) {
						tmp1 = tmp0; tmp0 = tmp;
					} else if (tmp > tmp1) {
						tmp1 = tmp;
					}
				}
			}
			tmpBound += (tmp0+tmp1)/2.;
		}
		upperBound = (int) tmpBound;
	}

	public Iterable<PartialTour> getChildren() {
		LinkedList<PartialTour> list = new LinkedList<PartialTour>();
		if (isFullTour())
			return list;

		Problem problem = Problem.getInstance();

		if (pending.length == 2) {
			int n = problem.getN();
			int[] tmp = new int[n];
			System.arraycopy(cities, 0, tmp, 0, n-2);


			tmp[n-2] = pending[0];
			tmp[n-1] = pending[1];
			list.add(new PartialTour(tmp));

			tmp[n-2] = pending[1];
			tmp[n-1] = pending[0];
			list.add(new PartialTour(tmp));
		} else {
			int[] tmp = new int[cities.length+1];
			System.arraycopy(cities, 0, tmp, 0, cities.length);

			for (int pCity : pending) {
				tmp[tmp.length-1] = pCity;
				PartialTour child = new PartialTour(tmp);
				list.add(child);
			}
		}
		return list;
	}

	public boolean isFullTour() {
		return pending.length == 0;
	}

	public int getLength() {
		return length;
	}

	public int getUpperBound() {
		return upperBound;
	}

	public int getNumberOfSolvedCities() {
		return cities.length;
	}

	public String toString() {
		Problem problem = Problem.getInstance();

		StringBuilder sb = new StringBuilder();
		sb.append("[PartialTour: length=");
		sb.append(length);
		if (!isFullTour()) {
			sb.append(" upper=");
			sb.append(upperBound);
		}
		sb.append(" cities=");
		for (int city: cities)
			sb.append(problem.getName(city) + ",");
		sb.append("]");

		return sb.toString();
	}

	public int compareTo(PartialTour other) {
		return this.upperBound - other.upperBound;
	}

	public static Comparator<PartialTour> getReverseComparator() {
		return new Comparator<PartialTour>() {
			public int compare(PartialTour arg0, PartialTour arg1) {
				return -arg0.compareTo(arg1);
			}
		};
	}
}
