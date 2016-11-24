package mlaloup.lasmaquinas;

import com.google.gson.Gson;

import org.junit.Assert;
import org.junit.Test;

import java.util.SortedSet;

import mlaloup.lasmaquinas.model.Ascent;
import mlaloup.lasmaquinas.model.Ranking;
import mlaloup.lasmaquinas.model.TickList;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class RankingJsonTest {
    @Test
    public void test_json_work_fine()  {
        String json ="{\"config\":{\"tickListSettings\":{\"maxAscentsCount\":3,\"monthDuration\":12},\"users\":[\"benoit.guerineau.2\",\"damien.vignes\",\"guillaume.pourbaix\",\"matthieu.laloup\",\"thibaut.gelize\"]},\"name\":\"Las maquinas ranking [23-11-2015 - 23-11-2016]\",\"tickLists\":[{\"ascents\":[{\"area\":\"Roche d\\u0027Hercule\",\"date\":\"Sep 18, 2016 12:00:00 AM\",\"flash\":false,\"grade\":{\"grade\":\"7b\"},\"name\":\"Fenbren le Fou\"},{\"area\":\"Franchard Hautes Plaines\",\"date\":\"Oct 22, 2016 12:00:00 AM\",\"flash\":false,\"grade\":{\"grade\":\"7b\"},\"name\":\"Tom et Géry\"},{\"area\":\"Cuisinière\",\"date\":\"Oct 9, 2016 12:00:00 AM\",\"flash\":false,\"grade\":{\"grade\":\"7a+\"},\"name\":\"Hot Hot\"}],\"config\":{\"maxAscentsCount\":3,\"monthDuration\":12},\"score\":300,\"user\":\"damien.vignes\"},{\"ascents\":[{\"area\":\"Cuisinière\",\"date\":\"Oct 9, 2016 12:00:00 AM\",\"flash\":false,\"grade\":{\"grade\":\"7b\"},\"name\":\"Pensées Cachées\"},{\"area\":\"Franchard Hautes Plaines\",\"date\":\"Oct 22, 2016 12:00:00 AM\",\"flash\":false,\"grade\":{\"grade\":\"7b\"},\"name\":\"Tom et Géry\"},{\"area\":\"Rocher Canon Ouest\",\"date\":\"Dec 20, 2015 12:00:00 AM\",\"flash\":false,\"grade\":{\"grade\":\"7a+\"},\"name\":\"Chute de Pierre\"}],\"config\":{\"maxAscentsCount\":3,\"monthDuration\":12},\"score\":300,\"user\":\"guillaume.pourbaix\"},{\"ascents\":[{\"area\":\"Rocher Canon Ouest\",\"date\":\"Oct 16, 2016 12:00:00 AM\",\"flash\":false,\"grade\":{\"grade\":\"7a+\"},\"name\":\"La Traversée de Chute de Pierre\"},{\"area\":\"Rocher Canon\",\"date\":\"May 1, 2016 12:00:00 AM\",\"flash\":false,\"grade\":{\"grade\":\"7a\"},\"name\":\"Magifix (assis)\"},{\"area\":\"Roche d\\u0027Hercule\",\"date\":\"May 15, 2016 12:00:00 AM\",\"flash\":false,\"grade\":{\"grade\":\"7a\"},\"name\":\"Fenbren le Gauche\"}],\"config\":{\"maxAscentsCount\":3,\"monthDuration\":12},\"score\":100,\"user\":\"benoit.guerineau.2\"},{\"ascents\":[{\"area\":\"Rocher Canon Ouest\",\"date\":\"Apr 16, 2016 12:00:00 AM\",\"flash\":false,\"grade\":{\"grade\":\"7a+\"},\"name\":\"La Traversée de Chute de Pierre\"},{\"area\":\"Roche d\\u0027Hercule\",\"date\":\"Sep 18, 2016 12:00:00 AM\",\"flash\":false,\"grade\":{\"grade\":\"7a\"},\"name\":\"Rince-Doigts\"},{\"area\":\"Franchard Isatis\",\"date\":\"Oct 15, 2016 12:00:00 AM\",\"flash\":false,\"grade\":{\"grade\":\"7a\"},\"name\":\"Abdolobotomy\"}],\"config\":{\"maxAscentsCount\":3,\"monthDuration\":12},\"score\":100,\"user\":\"matthieu.laloup\"},{\"ascents\":[{\"area\":\"Rocher Canon Ouest\",\"date\":\"Apr 16, 2016 12:00:00 AM\",\"flash\":false,\"grade\":{\"grade\":\"7a+\"},\"name\":\"La Traversée de Chute de Pierre\"},{\"area\":\"Rocher Canon\",\"date\":\"May 1, 2016 12:00:00 AM\",\"flash\":false,\"grade\":{\"grade\":\"7a\"},\"name\":\"Magifix (assis)\"},{\"area\":\"Franchard Isatis\",\"date\":\"Oct 22, 2016 12:00:00 AM\",\"flash\":false,\"grade\":{\"grade\":\"7a\"},\"name\":\"Abdolobotomy\"}],\"config\":{\"maxAscentsCount\":3,\"monthDuration\":12},\"score\":100,\"user\":\"thibaut.gelize\"}]}";
        Gson gson = new Gson();
        Ranking ranking = gson.fromJson(json, Ranking.class);
        SortedSet<TickList> tickLists = ranking.getTickLists();

        Assert.assertEquals(5, tickLists.size());
        SortedSet<Ascent> ascents = tickLists.first().getAscents();
        Assert.assertEquals(3, ascents.size());
        Ascent ascent = ascents.first();
        Assert.assertEquals("7b",ascent.getGrade().getGrade());
        Assert.assertEquals(300,tickLists.first().getScore());

        String jsonAgain = gson.toJson(ranking);
        //l'ordre des propriétés est différent..
        Assert.assertEquals(json.length(),jsonAgain.length());
    }


}