package mlaloup.lasmaquinas.model;

import java.util.SortedSet;
import java.util.TreeSet;

import mlaloup.lasmaquinas.model.settings.RankingSettings;

public class Ranking {

	private SortedSet<TickList> tickLists = new TreeSet<>();
	private String name;
	private RankingSettings config;

	public Ranking(String name, RankingSettings config) {
		this.name = name;
		this.config = config;
	}

	public void addTickList(TickList tickList) {
		tickLists.add(tickList);
	}


	public String getName() {
		return name;
	}

	public SortedSet<TickList> getTickLists() {
		return tickLists;
	}
}
