package edu.uta.cse5320.dao;


public class ItemData {

    private long id;
    private String itemName;
    private int itemQuantity;
    private String imageUrl1;
    private String imageUrl2;
    private String imageUrl3;

    public ItemData() {
    }

    public ItemData(String itemName, int itemQuantity, String imageUrl1, String imageUrl2, String imageUrl3) {
        this.itemName = itemName;
        this.itemQuantity = itemQuantity;
        this.imageUrl1 = imageUrl1;
        this.imageUrl2 = imageUrl2;
        this.imageUrl3 = imageUrl3;
    }

    public ItemData(long id, String itemName, int itemQuantity, String imageUrl1, String imageUrl2, String imageUrl3) {
        this.id = id;
        this.itemName = itemName;
        this.itemQuantity = itemQuantity;
        this.imageUrl1 = imageUrl1;
        this.imageUrl2 = imageUrl2;
        this.imageUrl3 = imageUrl3;
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

    public String getImageUrl1() {
        return imageUrl1;
    }

    public void setImageUrl1(String imageUrl1) {
        this.imageUrl1 = imageUrl1;
    }

    public String getImageUrl2() {
        return imageUrl2;
    }

    public void setImageUrl2(String imageUrl2) {
        this.imageUrl2 = imageUrl2;
    }

    public String getImageUrl3() {
        return imageUrl3;
    }

    public void setImageUrl3(String imageUrl3) {
        this.imageUrl3 = imageUrl3;
    }
}
