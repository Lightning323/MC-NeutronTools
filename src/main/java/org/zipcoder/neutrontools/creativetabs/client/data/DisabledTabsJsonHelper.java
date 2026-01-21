package org.zipcoder.neutrontools.creativetabs.client.data;

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
