package org.zipcoder.neutrontools.creativetabs.client.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

//@NoArgsConstructor
//@AllArgsConstructor
//@Getter
public class OrderedTabsJsonHelper {

    @SerializedName("tabs")
    public List<String> tabs;
}
