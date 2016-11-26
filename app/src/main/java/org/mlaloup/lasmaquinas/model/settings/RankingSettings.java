package org.mlaloup.lasmaquinas.model.settings;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;

import org.mlaloup.lasmaquinas.activity.util.PreferencesHelper;

public class RankingSettings {

	public static final Set<String>  DEFAULT_CLIMBERS;

	static {
		Set<String> defaultClimbers = new LinkedHashSet<>();
		defaultClimbers.add("matthieu.laloup");
		defaultClimbers.add("guillaume.pourbaix");
		defaultClimbers.add("benoit.guerineau.2");
		defaultClimbers.add("thibaut.gelize");
		defaultClimbers.add("damien.vignes");
		DEFAULT_CLIMBERS = Collections.unmodifiableSet(defaultClimbers);
	}


	private Set<String> users = new LinkedHashSet<>();

	private TickListSettings tickListSettings = new TickListSettings();

	public static final String CLIMBERS_KEY = "climbers";

	public static final String MONTH_DURATION_KEY = "monthDuration";

	public static final String MAX_ASCENTS_COUNT_KEY = "maxAscentsCount";

	private RankingSettings() {

	}

	/**
	 * Charge les préférences à partir d'un contexte applicatif.
	 * @param context
	 * @return
     */
	public static RankingSettings loadFromPreferences(Context context) {
		SharedPreferences preferences = PreferencesHelper.prefs(context);
		RankingSettings settings = new RankingSettings();

		int maxAscents = preferences.getInt(MAX_ASCENTS_COUNT_KEY,TickListSettings.DEFAULT_MAX_ASCENTS_COUNT);
		int monthDuration = preferences.getInt(MONTH_DURATION_KEY,TickListSettings.DEFAULT_MONTH_DURATION);
		Set<String> users = preferences.getStringSet(CLIMBERS_KEY, DEFAULT_CLIMBERS);

		settings.setUsers(users);
        settings.getTickListSettings().setMaxAscentsCount(maxAscents);
        settings.getTickListSettings().setMonthDuration(monthDuration);
        return settings;
	}


	public TickListSettings getTickListSettings() {
		return tickListSettings;
	}

	public void setTickListSettings(TickListSettings tickListSettings) {
		this.tickListSettings = tickListSettings;
	}

	public void setUsers(Set<String> users) {
		this.users = users;
	}

	public Set<String> getUsers() {
		return users;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}


}
