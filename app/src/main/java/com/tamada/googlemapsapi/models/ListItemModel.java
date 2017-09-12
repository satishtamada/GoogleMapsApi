package com.tamada.googlemapsapi.models;

/**
 * Created by inventbird on 31/8/17.
 */
public class ListItemModel {
    public String itemName;
    public String itemDesc;

    public ListItemModel(String itemName, String itemDesc) {
        this.itemName = itemName;
        this.itemDesc = itemDesc;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }
}
