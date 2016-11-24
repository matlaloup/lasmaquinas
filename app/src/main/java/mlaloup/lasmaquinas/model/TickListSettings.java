package mlaloup.lasmaquinas.model;

/**
 * Created by Matthieu on 23/11/2016.
 */

public class TickListSettings {

    private int maxAscentsCount = 15;

    private int monthDuration = 12;

    private transient GradeScoreScale scale = new DefaultGradeScoreScale();

    public void setMaxAscentsCount(int maxAscentsCount) {
        this.maxAscentsCount = maxAscentsCount;
    }

    public int getMaxAscentsCount() {
        return maxAscentsCount;
    }

    public void setMonthDuration(int monthDuration) {
        this.monthDuration = monthDuration;
    }

    public int getMonthDuration() {
        return monthDuration;
    }

    public GradeScoreScale getScale() {
        return scale;
    }

    public void setScale(GradeScoreScale scale) {
        this.scale = scale;
    }
}
