package org.zipcoder.neutrontools.creativetabs.client.data;

import com.google.gson.annotations.SerializedName;

public class TabItem {
    @SerializedName("name")
    public String name;

    @SerializedName("hide_old_tab")
    public boolean hideFromOtherTabs;

    @SerializedName("nbt")
    public String nbt;

    //For matching items
    @SerializedName("match_name")
    public String nameRegex;

    @SerializedName("match_tags")
    public String[] tags;

}