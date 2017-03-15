package edu.uta.cse5320.dao;

/**
 * Created by Akshay on 3/14/2017.
 */

public class BagData {

    private String bagName;
    private int itemQuantity;

    public BagData() {
    }

    public BagData(String bagName, int itemQuantity) {
        this.bagName = bagName;
        this.itemQuantity = itemQuantity;
    }

    public String getBagName() {
        return bagName;
    }

    public void setBagName(String bagName) {
        this.bagName = bagName;
    }

    public int getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(int itemQuantity) {
        this.itemQuantity = itemQuantity;
    }
}
