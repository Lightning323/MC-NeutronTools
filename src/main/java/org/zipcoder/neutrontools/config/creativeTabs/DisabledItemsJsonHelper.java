package org.zipcoder.neutrontools.config.creativeTabs;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
public class DisabledItemsJsonHelper {

    @SerializedName("items")
    private ArrayList<String> disabledItems;

    public ArrayList<String> getDisabledItems(){
        return disabledItems;
    }
}
