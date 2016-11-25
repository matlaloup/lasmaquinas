package mlaloup.lasmaquinas.model;


import org.apache.commons.lang3.StringUtils;

public class Climber {

    private String login;

    private String fullName;

    private String countryBigram;

    public Climber(String login){
        this.login = login;
        if(login==null){
            throw new IllegalArgumentException("Null login !");
        }
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCountryBigram() {
        return countryBigram;
    }

    public void setCountryBigram(String countryBigram) {
        this.countryBigram = countryBigram;
    }

    public String getLogin() {
        return login;
    }

    public String getDisplayName(){
        if(StringUtils.isNotBlank(fullName)){
            return fullName;
        }
        return login;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Climber climber = (Climber) o;

        return login.equals(climber.login);

    }

    @Override
    public int hashCode() {
        return login.hashCode();
    }
}
