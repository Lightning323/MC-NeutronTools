package org.zipcoder.creativetabs.client.data;

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
        public TabItemEntry(String tabName,
                            CustomCreativeTabJsonHelper.TabItem[] itemsAdd,
                            String[] itemsDelete) {
            this.tabName = tabName;
            this.itemsAdd = itemsAdd;
            this.itemsRemove = itemsDelete;
        }

        @SerializedName("tab_name")
        public String tabName;

        @SerializedName("items_to_add")
        public CustomCreativeTabJsonHelper.TabItem[] itemsAdd;

        @SerializedName("items_to_remove")
        public String[] itemsRemove;
    }

}
