package edu.uta.cse5320.dao;

/**
 * Created by Akshay on 3/14/2017.
 */

public class BagData {

    private long id;
    private String bagName;
    private int itemQuantity;
    private String imageUrl1;
    private String imageUrl2;
    private String imageUrl3;

    public BagData() {
    }

    public BagData(String bagName, int itemQuantity, String imageUrl1, String imageUrl2, String imageUrl3) {
        this.bagName = bagName;
        this.itemQuantity = itemQuantity;
        this.imageUrl1 = imageUrl1;
        this.imageUrl2 = imageUrl2;
        this.imageUrl3 = imageUrl3;
    }

    public BagData(long id, String bagName, int itemQuantity, String imageUrl1, String imageUrl2, String imageUrl3) {
        this.id = id;
        this.bagName = bagName;
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
/*
    @Override
    public boolean equals(Object obj) {

        final BagData other = (BagData) obj;
        if (obj == null) {
            return false;
        }
        if (this.id == other.getId()) {
            return true;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.getImageUrl1() != null ? this.name.hashCode() : 0);
        return hash;
    }
*/
}
