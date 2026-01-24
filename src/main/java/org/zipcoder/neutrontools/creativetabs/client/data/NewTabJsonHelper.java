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


    @SerializedName("tab_background")
    private String tabBackground;

    public String getTabBackground() {
        return tabBackground;
    }

    @SerializedName("replace_tab")
    public String replaceTab;

    @SerializedName("tab_items")
    public ArrayList<TabItem> itemsToAdd;

    @SerializedName("items_to_remove")
    public ArrayList<TabItem> itemsToRemove;

    int existingTabIndex = Integer.MIN_VALUE;

    public boolean isShouldKeepExisting() {
        return existingTabIndex != Integer.MIN_VALUE;
    }

    public void setKeepExisting(int i) {
        existingTabIndex = i;
    }

    public int getExistingIndex() {
        return existingTabIndex;
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
