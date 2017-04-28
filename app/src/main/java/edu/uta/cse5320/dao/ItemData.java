package edu.uta.cse5320.dao;


public class ItemData {

    private long id;
    private String itemName;
    private int itemQuantity;
    public ItemData() {
    }

    public ItemData(String itemName, int itemQuantity) {
        this.itemName = itemName;
        this.itemQuantity = itemQuantity;
    }

    public ItemData(long id, String itemName, int itemQuantity) {
        this.id = id;
        this.itemName = itemName;
        this.itemQuantity = itemQuantity;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(int itemQuantity) {
        this.itemQuantity = itemQuantity;
    }
}
