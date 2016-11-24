package mlaloup.lasmaquinas.model;

import java.util.SortedSet;
import java.util.TreeSet;

public class Ranking {

	private SortedSet<TickList> tickLists = new TreeSet<>();
	private String name;
	private BleauRankSettings config;

	public Ranking(String name, BleauRankSettings config) {
		this.name = name;
		this.config = config;
	}

	public void addTickList(TickList tickList) {
		tickLists.add(tickList);
	}

	public String computeRankings() {

		StringBuilder result = new StringBuilder();

		result.append(name + " : ");
		result.append("\n");
		result.append("\n");
		int rank = 1;
		for (TickList tickList : tickLists) {
			int score = tickList.getScore();
			String user = tickList.getUser();
			result.append(rank + " : " + user + " | " + score + " points | détails : ");
			result.append(tickList.getSummary());
			result.append("\n");
			rank++;
		}

		return result.toString();
	}

	public String getName() {
		return name;
	}

	public SortedSet<TickList> getTickLists() {
		return tickLists;
	}
}
