package org.zipcoder.neutrontools.creativetabs.client.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

//@NoArgsConstructor
//@AllArgsConstructor
//@Getter
public class TabItemsJsonHelper {

    @SerializedName("tabs")
    private ArrayList<TabItemEntry> tabs;

    public ArrayList<TabItemEntry> getTabs() {
        return tabs;
    }

    public static class TabItemEntry {
        @SerializedName("tab_name")
        public String tabName;

        @SerializedName("items_to_add")
        public TabItem[] itemsAdd;

        @SerializedName("items_to_remove")
        public TabItem[] itemsRemove;
    }

}
