package org.zipcoder.neutrontools.config.creativeTabs;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
public class DisabledTabsJsonHelper {

    @SerializedName("tabs")
    private ArrayList<String> disabledTabs;

    public ArrayList<String> getDisabledTabs(){
        return disabledTabs;
    }
}
