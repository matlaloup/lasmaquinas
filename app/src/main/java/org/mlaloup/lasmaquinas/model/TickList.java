package org.mlaloup.lasmaquinas.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import org.mlaloup.lasmaquinas.model.settings.TickListSettings;

public class TickList implements Iterable<Ascent>, Comparable<TickList> {

	private ClimberRef climber;

	private TickListSettings config;

	private SortedSet<Ascent> ascents = new TreeSet<>();

	private int score;

	@Override
	public Iterator<Ascent> iterator() {
		return ascents.iterator();
	}

	public TickList(Climber climber, TickListSettings config) {
		setClimber(climber);
		this.config = config;
	}

	public void addAscent(Ascent ascent) {
		ascents.add(ascent);
		addAscentScore(ascent);
	}

	public int getScore() {
		return score;
	}

	public ClimberRef getClimber() {
		return climber;
	}

	public void setClimber(ClimberRef climber) {
		this.climber = climber;
	}

	@Override
	public int compareTo(TickList o) {
		int result = new Integer(o.score).compareTo(score);
		if (result == 0) {
			return climber.getLogin().compareTo(o.getClimber().getLogin());
		}
		return result;
	}

    /**
     * Tronque la liste en limitant le nombre de croix au nombre max prévu.
     */
	public void truncate() {
		int maxAscentsCount = config.getMaxAscentsCount();
		if (ascents.size() <= maxAscentsCount) {
			return;
		}

		Ascent boundaryAscent = new ArrayList<>(ascents).get(maxAscentsCount);
		ascents = ascents.headSet(boundaryAscent);
		computeScore();
	}

	private void computeScore() {
		score = 0;
		for (Ascent ascent : ascents) {
			addAscentScore(ascent);
		}
	}

	private void addAscentScore(Ascent ascent) {
		int newScore = ascent.getBoulder().getGrade().getScore(config.getScale());
		score += newScore;
	}

	public Map<String, Set<Ascent>> getAscentsByGrade(){
		Map<String, Set<Ascent>> map = new TreeMap<>(Collections.reverseOrder());
		for (Ascent ascent : ascents) {
			Grade grade = ascent.getBoulder().getGrade();
			Set<Ascent> ascentsForGrade = map.get(grade.getGrade());
			if (ascentsForGrade == null) {
				ascentsForGrade = new LinkedHashSet<>();
				map.put(grade.getGrade(), ascentsForGrade);
			}
			ascentsForGrade.add(ascent);
		}
		return map;
	}

	public String getSummary() {
		Map<String, Set<Ascent>> map = getAscentsByGrade();

		StringBuilder result = new StringBuilder();
		Set<Entry<String, Set<Ascent>>> entries = map.entrySet();
		for (Entry<String, Set<Ascent>> entry : entries) {
			String key = entry.getKey();
			Set<Ascent> ascentsForGrade = entry.getValue();
			int count = ascentsForGrade.size();
			result.append(key + "(" + count + "),");
		}
		return StringUtils.removeEnd(result.toString(), ",");
	}

	public SortedSet<Ascent> getAscents() {
		return ascents;
	}

	public TickListSettings getConfig() {
		return config;
	}
}
