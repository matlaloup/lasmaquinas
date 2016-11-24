package mlaloup.lasmaquinas.model;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class BleauInfoParser {

	public static final String MAIN_URL = "http://bleau.info/";

	public static final String PROFILE_SUB_URL = "profiles/";

	private static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

	static {

		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
		}
	}

	public Ranking parseTickLists(BleauRankSettings config) throws Exception {
		Set<String> users = config.getUsers();
		Period period = new Period(config);
		Ranking ranking = new Ranking("Las maquinas " + period, config);

		for (String user : users) {
			TickList tickList = parseTickList(config, user, period);
			ranking.addTickList(tickList);
		}
		return ranking;
	}

	private static class Period {

		private Date startDate;

		private Date endDate;

		public Period(BleauRankSettings config) {
			Calendar calendar = Calendar.getInstance();
			Date time = calendar.getTime();
			endDate = time;

			int monthDuration = config.getTickListSettings().getMonthDuration();
			calendar.add(Calendar.MONTH, -monthDuration);

			startDate = calendar.getTime();
		}

		public boolean isAllowed(Date date) {
			return date.after(startDate);
		}

		@Override
		public String toString() {
			return "[" + formatter.format(startDate) + " - " + formatter.format(endDate) + "]";
		}
	}

	public TickList parseTickList(BleauRankSettings config, String userName) throws Exception {
		Period period = new Period(config);
		return parseTickList(config, userName, period);
	}

	protected TickList parseTickList(BleauRankSettings config, String userName, Period period)
			throws IOException, ParseException {
		TickList result = new TickList(userName, config.getTickListSettings());
		Document doc = Jsoup.connect(MAIN_URL + PROFILE_SUB_URL + userName).get();

		Elements repetitions = doc.select("#tab_by_date").select("div.repetition");
		for (Element repetition : repetitions) {
			List<TextNode> textNodes = repetition.textNodes();
			String textPart1 = textNodes.get(0).text();
			String dateString = StringUtils.remove(StringUtils.trim(textPart1), ":");
			Date ascentDate = formatter.parse(dateString);
			if (!period.isAllowed(ascentDate)) {
				break;
			}
			String textPart2 = textNodes.get(1).text();
			String gradeString = StringUtils.trim(textPart2);
			Grade grade = new Grade(gradeString);
			Elements links = repetition.select("a");
			String name = links.get(0).textNodes().get(0).text();
			String area = links.get(1).textNodes().get(0).text();

			Ascent ascent = new Ascent(name, grade, false, area, ascentDate);
			result.addAscent(ascent);
		}
		result.truncate();

		return result;
	}

}
