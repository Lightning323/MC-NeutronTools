package org.zipcoder.neutrontools.creativetabs.client.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

//@NoArgsConstructor
//@AllArgsConstructor
//@Getter
public class NewTabJsonHelper {

    @SerializedName("tab_enabled")
    private boolean tabEnabled;

    public boolean isTabEnabled() {
        return tabEnabled;
    }

    @SerializedName("tab_name")
    private String tabName;

    public String getTabName() {
        return tabName;
    }

    @SerializedName("tab_stack")
    private TabIcon tabIcon;

    public boolean isKeepExisting() {
        return keepExisting;
    }


    @SerializedName("tab_background")
    private String tabBackground;

    public String getTabBackground() {
        return tabBackground;
    }

    @SerializedName("replace_tab")
    public String replaceTab;


    //    @Setter
    private boolean keepExisting;

    public void setKeepExisting(boolean b) {
        keepExisting = b;
    }

    @SerializedName("tab_items")
    private ArrayList<TabItem> tabItems;

    @SerializedName("hide_matches_from_other_tabs")
    public boolean hideMatchesFromOtherTabs;

    public ArrayList<TabItem> getTabItems() {
        return tabItems;
    }

    //    @AllArgsConstructor
//    @NoArgsConstructor
//    @Getter
    public static class TabIcon {
        private String name;
        private String nbt;

        public String getName() {
            return name;
        }

        public String getNbt() {
            return nbt;
        }
    }

    public TabIcon getTabIcon() {
        return tabIcon;
    }
}
