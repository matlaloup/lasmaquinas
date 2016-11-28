package org.mlaloup.lasmaquinas.model.settings;

public class TickListSettings {

    public static final int DEFAULT_MONTH_DURATION = 12;

    public static final int DEFAULT_MAX_ASCENTS_COUNT = 15;

    private int maxAscentsCount = DEFAULT_MAX_ASCENTS_COUNT;

    private int monthDuration = DEFAULT_MONTH_DURATION;

    private transient GradeScoreScale scale = new DefaultGradeScoreScale();

    public static TickListSettings unlimited(){
        TickListSettings result = new TickListSettings();
        result.setMaxAscentsCount(Integer.MAX_VALUE);
        result.setMonthDuration(Integer.MAX_VALUE);
        return result;
    }

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
