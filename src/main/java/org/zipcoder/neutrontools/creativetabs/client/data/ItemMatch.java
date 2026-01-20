package org.zipcoder.neutrontools.creativetabs.client.data;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public  class ItemMatch {
    @SerializedName("name_regex")
    public String nameRegex;

    @SerializedName("tags")
    public String[] tags;

    @SerializedName("hide_old_tab")
    public boolean hideFromOtherTabs;

    @Override
    public String toString() {
        return "ItemMatch{" +
                "nameRegex='" + nameRegex + '\'' +
                ", tags=" + Arrays.toString(tags) +
                '}';
    }
}