package org.zipcoder.neutrontools.creativetabs.client.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

//@NoArgsConstructor
//@AllArgsConstructor
//@Getter
public class CustomCreativeTabJsonHelper {

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

    private boolean replace;

    public boolean isReplace() {
        return replace;
    }


    //    @Setter
    private boolean keepExisting;

    public void setKeepExisting(boolean b) {
        keepExisting = b;
    }

    @SerializedName("tab_items")
    private ArrayList<TabItem> tabItems;

    public ArrayList<TabItem> getTabItems() {
        return tabItems;
    }

    //    @AllArgsConstructor
//    @NoArgsConstructor
//    @Getter
    public static class TabItem {
        private String name;

        @SerializedName("hide_old_tab")
        private boolean hideOldTab;

        private String nbt;

        public String getName() {
            return name;
        }

        public String getNbt() {
            return nbt;
        }

        public boolean isHideOldTab() {
            return hideOldTab;
        }
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
