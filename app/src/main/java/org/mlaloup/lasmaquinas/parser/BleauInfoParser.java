package org.mlaloup.lasmaquinas.parser;

import android.support.annotation.NonNull;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.mlaloup.lasmaquinas.model.Area;
import org.mlaloup.lasmaquinas.model.Ascent;
import org.mlaloup.lasmaquinas.model.Boulder;
import org.mlaloup.lasmaquinas.model.Climber;
import org.mlaloup.lasmaquinas.model.settings.RankingSettings;
import org.mlaloup.lasmaquinas.model.Grade;
import org.mlaloup.lasmaquinas.model.Ranking;
import org.mlaloup.lasmaquinas.model.TickList;
import org.mlaloup.lasmaquinas.model.settings.TickListSettings;

public class BleauInfoParser {

    /** L'url doit absolument être en HTTPS pour la partie login car la redirection n'est pas automatique dans ce cas */
    public static final String MAIN_URL = "https://bleau.info";

    private static final String PUBLIC_PROFILE_SUB_URL = "/profiles/";
    private static final String PRIVATE_PROFILE_SUB_URL = "/profile";

    private static final String LOGIN_ACTION_SUB_URL = "/user_session";
    private static final String LOGIN_SUB_URL = "/login";
    private static final String LOGIN_USER_PARAM = "user_session[username]";
    private static final String LOGIN_PASSWORD_PARAM = "user_session[password]";
    private static final String LOGIN_FORM_SELECTOR = "form.new_user_session";
    private static final String LOGIN_AUTHENTICITY_TOKEN_SELECTOR = "input[name=authenticity_token]";
    private static final String AUTHENTICITY_TOKEN_PARAM = "authenticity_token";
    private static final String LOGIN_LINK_SELECTOR = "a[href='/login']";


    private static final String ASCENTS_BY_DATE_SELECTOR = "#tab_by_date";
    private static final String PROJECTS_BY_AREA_SELECTOR = "#tab_wl_by_area";
    private static final String REPETITION_SELECTOR = "div.repetition";

    private static final String DROPDOWN_MENU_SELECTOR = "ul.dropdown-menu";

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
     *
     * @param config
     * @return
     * @throws Exception en cas de problème d'accès internet
     */
    public Ranking parseTickLists(RankingSettings config) throws Exception {
        Set<String> users = config.getUsers();
        TickListSettings tickListSettings = config.getTickListSettings();
        Period period = new Period(tickListSettings);
        Ranking ranking = new Ranking(period.toString() + "\n" + tickListSettings.getMaxAscentsCount() + " meilleurs blocs", config);

        for (String user : users) {
            TickList tickList = parseTickList(config, user, period);
            if (tickList == null) {
                continue;
            }
            ranking.addTickList(tickList);
        }
        return ranking;
    }


    /**
     * Parse une ticklist d'un profil bleau.info. Renvoie null si le profil n'existe pas
     *
     * @param config
     * @param userName
     * @return
     * @throws Exception en cas de problème d'accès internet
     */
    public TickList parseTickList(RankingSettings config, String userName) throws Exception {
        Period period = new Period(config.getTickListSettings());
        return parseTickList(config, userName, period);
    }

    /**
     * Renvoie vrai si le profil existe.
     *
     * @param userName
     * @return
     * @throws IOException
     */
    public boolean checkProfileExists(String userName) throws IOException {
        Document document = publicProfile(userName);
        return isValidProfilePage(document);
    }

    protected TickList parseTickList(RankingSettings config, String userName, Period period)
            throws IOException, ParseException {
        Document doc = publicProfile(userName);

        boolean isValid = isValidProfilePage(doc);
        if (!isValid) {
            Log.w(TAG, "No bleau info profile for " + userName);
            return null;
        }


        Climber climber = parseClimber(userName, doc);
        TickList result = parseTickList(config.getTickListSettings(), period, climber, doc);
        return result;
    }

    //si titre h1, on a été redirigé car le profil n'existe pas.

    /**
     * Ce test est valide aussi bien pour un profil public que privé.
     * @param doc
     * @return
     */
    private boolean isValidProfilePage(Document doc) {
        return doc.select("h1").isEmpty();
    }

    private Document publicProfile(String userName) throws IOException {
        return Jsoup.connect(MAIN_URL + PUBLIC_PROFILE_SUB_URL + userName).get();
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
    protected TickList parseTickList(TickListSettings tickListSettings, Period period, Climber climber, Document doc) throws ParseException {
        TickList result = new TickList(climber, tickListSettings);
        parseAscents(new Period(tickListSettings), doc, result);
        return result;
    }

    protected void parseAscents(Period period, Document doc, TickList result) throws ParseException {
        Elements repetitions = doc.select(ASCENTS_BY_DATE_SELECTOR).select(REPETITION_SELECTOR);
        for (Element repetition : repetitions) {
            Ascent ascent = parseAscent(repetition);
            if (!period.isAllowed(ascent.getDate())) {
                break;
            }

            result.addAscent(ascent);
        }
        //Borne au nombre max prévu.
        result.truncate();
    }


    protected void parseProjects(Document doc, Climber climber) throws ParseException {
        Elements repetitions = doc.select(PROJECTS_BY_AREA_SELECTOR).select(REPETITION_SELECTOR);
        Set<Boulder> projects = new HashSet<>();
        for (Element repetition : repetitions) {
            Boulder boulder = parseBoulder(repetition);
            Ascent ascent = parseAscent(repetition);

            projects.add(boulder);
        }
        climber.setProjects(projects);
    }

    protected Ascent parseAscent(Element repetition) throws ParseException {
        Boulder boulder = parseBoulder(repetition);
        Ascent ascent = parseAscent(repetition, boulder);

        return ascent;
    }

    @NonNull
    protected Ascent parseAscent(Element repetition, Boulder boulder) throws ParseException {
        //Deuxieme premier, mais parfois, commence par un noeud blank
        int index = 0;
        List<TextNode> textNodes = repetition.textNodes();
        if(textNodes.get(0).isBlank()){
            index=1;
        }
        String textPart1 = textNodes.get(index).text();
        String dateString = StringUtils.remove(StringUtils.trim(textPart1), ":");
        Date ascentDate = formatter.parse(dateString);
        return new Ascent(boulder,ascentDate,false);
    }

    @NonNull
    protected Boulder parseBoulder(Element repetition) {
        //Deuxieme noeud, mais parfois, commence par un noeud blank
        List<TextNode> textNodes = repetition.textNodes();
        int index = 1;
        if(textNodes.get(0).isBlank()){
            index=2;
        }

        String textPart2 = textNodes.get(index).text();
        String gradeString = StringUtils.trim(textPart2);
        Grade grade = new Grade(gradeString);
        Elements links = repetition.select("a");
        Element boulderLink = links.get(0);
        Element areaLink = links.get(1);
        String boulderRelativeUrl = boulderLink.attr("href");
        String areaRelativeUrl = areaLink.attr("href");

        String boulderId = StringUtils.removeEnd(StringUtils.substringAfterLast(boulderRelativeUrl,"/"),".html");
        String areaId = StringUtils.substringAfterLast(areaRelativeUrl,"/");


        String name = boulderLink.textNodes().get(0).text();
        String areaName = areaLink.textNodes().get(0).text();

        Area area = new Area(areaId,areaName);
        return new Boulder(grade,area,boulderId,name);
    }



    /**
     * Classe représentant une période.
     */
    private static class Period {

        private Date startDate;

        private Date endDate;

        public Period(TickListSettings tickListSettings) {
            Calendar calendar = Calendar.getInstance();
            Date time = calendar.getTime();
            endDate = time;

            int monthDuration = tickListSettings.getMonthDuration();
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

    /**
     * Se log sur le site bleau info. Renvoie les cookies de session associés
     * @param login
     * @param password
     * @throws CredentialsException si le couple login/mot de passe est incorrect.
     * @throws IOException
     */
    public Map<String, String> logIn(String login, String password) throws CredentialsException, IOException {
        Connection.Response loginResponse = Jsoup.connect(MAIN_URL + LOGIN_SUB_URL)
                .method(Connection.Method.GET)
                .execute();
        Map<String, String> cookies = loginResponse.cookies();
        Document loginForm = loginResponse.parse();

        String authenticityToken = loginForm.select(LOGIN_FORM_SELECTOR)
                .select(LOGIN_AUTHENTICITY_TOKEN_SELECTOR).first().val();

        Connection loginConnection = Jsoup.connect(MAIN_URL + LOGIN_ACTION_SUB_URL)
                .data(LOGIN_USER_PARAM, login)
                .data(LOGIN_PASSWORD_PARAM, password)
                .data(AUTHENTICITY_TOKEN_PARAM,authenticityToken)
                .cookies(cookies);

        Connection.Response loginActionResponse = loginConnection.method(Connection.Method.POST).execute();


        Document document = loginActionResponse.parse();
        boolean loggedIn = document.select(LOGIN_LINK_SELECTOR).isEmpty();
        if (loggedIn) {
            return loginActionResponse.cookies();
        }
        throw new CredentialsException("Login ou mot de passe bleau.info invalide !");
    }

    /**
     * Récupère la page du profile privé d'un utilisateur, en utilisant les cookies session en paramètre.
     * @param userCookies
     * @return
     * @throws CredentialsException
     * @throws IOException
     */
    public Climber parsePrivateProfile(Map<String,String> userCookies) throws CredentialsException, IOException,ParseException {
        Document doc = Jsoup.connect(MAIN_URL + PRIVATE_PROFILE_SUB_URL).cookies(userCookies).get();
        if (!isValidProfilePage(doc)) {
            throw new CredentialsException("Cookie de session invalide ou expiré !");
        }

        //Récuperation de l'url du profil public
        String publicProfileRelativeUrl = doc.select(DROPDOWN_MENU_SELECTOR).select("li").select("a").first().attr("href");
        Document publicProfileDoc = Jsoup.connect(MAIN_URL+publicProfileRelativeUrl).get();
        String userName = StringUtils.substringAfterLast(publicProfileRelativeUrl, "/");
        //recuperation du user à partir du profil public
        Climber climber = parseClimber(userName,publicProfileDoc);
        TickList tickList = climber.getTickList();

        Period unlimitedPeriod = new Period(tickList.getConfig());
        //remplit la tickList
        parseAscents(unlimitedPeriod, doc,tickList);
        //remplit les projets
        parseProjects(doc,climber);
        return climber;
    }




}
