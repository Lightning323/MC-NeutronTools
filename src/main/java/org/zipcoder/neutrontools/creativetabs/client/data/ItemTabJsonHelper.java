package org.zipcoder.neutrontools.creativetabs.client.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

//@NoArgsConstructor
//@AllArgsConstructor
//@Getter
public class ItemTabJsonHelper {

    @SerializedName("tabs")
    private ArrayList<TabItemEntry> tabs;

    public ArrayList<TabItemEntry> getTabs(){
        return tabs;
    }

    public static class TabItemEntry {
        @SerializedName("tab_name")
        public String tabName;

        @SerializedName("items_to_add")
        public CustomCreativeTabJsonHelper.TabItem[] itemsAdd;

        @SerializedName("items_to_remove")
        public String[] itemsRemove;

        @SerializedName("regex_matches_to_add")
        public String[] regexMatchesToAdd;

        @SerializedName("regex_matches_to_remove")
        public String[] regexMatchesToRemove;
    }

}
