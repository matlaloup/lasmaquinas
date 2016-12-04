package org.mlaloup.lasmaquinas.model;

import org.mlaloup.lasmaquinas.parser.BleauInfoParser;

/**
 * Created by Matthieu on 02/12/2016.
 */

public class Area {

    private String id;

    private String name;

    public Area(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getURL(){
        return BleauInfoParser.MAIN_URL+"/"+id;
    }
}
