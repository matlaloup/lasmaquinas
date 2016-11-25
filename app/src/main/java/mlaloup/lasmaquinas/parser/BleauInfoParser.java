package mlaloup.lasmaquinas.parser;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

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
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import mlaloup.lasmaquinas.model.Ascent;
import mlaloup.lasmaquinas.model.Climber;
import mlaloup.lasmaquinas.model.settings.RankingSettings;
import mlaloup.lasmaquinas.model.Grade;
import mlaloup.lasmaquinas.model.Ranking;
import mlaloup.lasmaquinas.model.TickList;

public class BleauInfoParser {

    public static final String MAIN_URL = "http://bleau.info/";

    public static final String PROFILE_SUB_URL = "profiles/";

    private static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

    private static String TAG = "BleauInfoParser";


    private static final Pattern fullNamePattern = Pattern.compile("(.+)\\(([A-Z][A-Z])\\)\\s*");

    static {
        hackHTTPS();
    }

    /**
     * Permet de bypasser les contrôles de certificat HTTPS.
     */
    private static void hackHTTPS() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
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
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
    }

    /**
     * Parse une classement issus d'un ensemble de profifls bleau.info. Les profils inconnus sont ignorés.
     * @param config
     * @return
     * @throws Exception en cas de problème d'accès internet
     */
    public Ranking parseTickLists(RankingSettings config) throws Exception {
        Set<String> users = config.getUsers();
        Period period = new Period(config);
        Ranking ranking = new Ranking(period.toString() + " | " + config.getTickListSettings().getMaxAscentsCount() + " meilleurs blocs", config);

        for (String user : users) {
            TickList tickList = parseTickList(config, user, period);
            if(tickList == null){
                continue;
            }
            ranking.addTickList(tickList);
        }
        return ranking;
    }


    /**
     * Parse une ticklist d'un profil bleau.info. Renvoie null si le profil n'existe pas
     * @param config
     * @param userName
     * @return
     * @throws Exception en cas de problème d'accès internet
     */
    public TickList parseTickList(RankingSettings config, String userName) throws Exception {
        Period period = new Period(config);
        return parseTickList(config, userName, period);
    }

    /**
     * Renvoie vrai si le profil existe.
     * @param userName
     * @return
     * @throws IOException
     */
    public boolean checkProfileExists(String userName) throws IOException {
        Document document = getDocument(userName);
        return isValidProfilePage(document);
    }

    protected TickList parseTickList(RankingSettings config, String userName, Period period)
            throws IOException, ParseException {
        Document doc = getDocument(userName);

        boolean isValid = isValidProfilePage(doc);
        if (!isValid) {
            Log.w(TAG, "No bleau info profile for " + userName);
            return null;
        }


        Climber climber = parseClimber(userName, doc);
        TickList result = parseTickList(config, period, climber, doc);
        return result;
    }

    //si titre h1, on a été redirigé car le profil n'existe pas.
    private boolean isValidProfilePage(Document doc) {
        return doc.select("h1").isEmpty();
    }

    private Document getDocument(String userName) throws IOException {
        return Jsoup.connect(MAIN_URL + PROFILE_SUB_URL + userName).get();
    }

    protected Climber parseClimber(String userName, Document doc) {
        String fullNameWithBigram = doc.select("h3").first().textNodes().get(0).text();

        Matcher matcher = fullNamePattern.matcher(fullNameWithBigram);
        String fullName = fullNameWithBigram.trim();
        String bigram = null;
        if (matcher.matches()) {
            fullName = matcher.group(1).trim();
            bigram = matcher.group(2);
        }

        Climber climber = new Climber(userName);
        climber.setFullName(fullName);
        climber.setCountryBigram(bigram);
        return climber;
    }

    @NonNull
    protected TickList parseTickList(RankingSettings config, Period period, Climber climber, Document doc) throws ParseException {
        TickList result = new TickList(climber, config.getTickListSettings());
        Elements repetitions = doc.select("#tab_by_date").select("div.repetition");
        for (Element repetition : repetitions) {
            Ascent ascent = parseAscent(repetition);
            if (!period.isAllowed(ascent.getDate())) {
                break;
            }

            result.addAscent(ascent);
        }
        //Borne au nombre max prévu.
        result.truncate();
        return result;
    }

    protected Ascent parseAscent(Element repetition) throws ParseException {
        List<TextNode> textNodes = repetition.textNodes();
        String textPart1 = textNodes.get(0).text();
        String dateString = StringUtils.remove(StringUtils.trim(textPart1), ":");
        Date ascentDate = formatter.parse(dateString);

        String textPart2 = textNodes.get(1).text();
        String gradeString = StringUtils.trim(textPart2);
        Grade grade = new Grade(gradeString);
        Elements links = repetition.select("a");
        String name = links.get(0).textNodes().get(0).text();
        String area = links.get(1).textNodes().get(0).text();
        Ascent ascent = new Ascent(name, grade, false, area, ascentDate);

        return ascent;
    }

    /**
     * Classe représentant une période.
     */
    private static class Period {

        private Date startDate;

        private Date endDate;

        public Period(RankingSettings config) {
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
            return "Période du " + formatter.format(startDate) + " au " + formatter.format(endDate);
        }
    }

}
