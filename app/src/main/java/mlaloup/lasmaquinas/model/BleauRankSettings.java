package mlaloup.lasmaquinas.model;

import android.content.res.Resources;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import mlaloup.lasmaquinas.activity.R;

public class BleauRankSettings {

	private Set<String> users = new LinkedHashSet<>();

	private TickListSettings tickListSettings = new TickListSettings();

	public static final String USERS_KEY = "bleau.userNames";

	public static final String MONTH_DURATION_KEY = "bleau.ranking.monthDuration";

	public static final String MAX_ASCENT_COUNT_KEY = "bleau.ranking.maxAscentsCount";

	private BleauRankSettings() {
	}


	public static BleauRankSettings load(Resources resources) {
		try {
			Properties properties = new Properties();
			InputStream is = resources.openRawResource(R.raw.config);
			properties.load(is);
			BleauRankSettings bleauRaceConfig = new BleauRankSettings();
			int defaultMaxAscents = bleauRaceConfig.getTickListSettings().getMaxAscentsCount();
			int defaultDuration = bleauRaceConfig.getTickListSettings().getMonthDuration();

			String stringDuration = properties.getProperty(MONTH_DURATION_KEY, String.valueOf(defaultDuration));
			String stringMaxAscents = properties.getProperty(MAX_ASCENT_COUNT_KEY, String.valueOf(defaultMaxAscents));
			String stringUsers = properties.getProperty(USERS_KEY, "matthieu.laloup");

			bleauRaceConfig.getTickListSettings().setMaxAscentsCount(Integer.valueOf(stringMaxAscents));
			bleauRaceConfig.getTickListSettings().setMonthDuration(Integer.valueOf(stringDuration));
			String[] usersArray = StringUtils.split(stringUsers, ',');
			bleauRaceConfig.addUsers(usersArray);

			return bleauRaceConfig;
		} catch (Exception e) {
			throw new IllegalStateException("No configuration provided !", e);
		}
	}

	public TickListSettings getTickListSettings() {
		return tickListSettings;
	}

	public void setTickListSettings(TickListSettings tickListSettings) {
		this.tickListSettings = tickListSettings;
	}

	public void addUsers(String... userNames) {
		for (String user : userNames) {
			users.add(user);
		}
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
