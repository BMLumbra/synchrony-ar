package com.synchrony.uconn.design.synchronyar;

import java.util.*;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable
{
    private int id;

    private String name;

    private String brand;

    private String miscInfo;

    private double price;

    private int stock;

    private int colorIDCounter = 0;

    private ArrayList<String> imageURLs = new ArrayList<>();

    private ArrayList<String> tags = new ArrayList<>();


    public Product(int _id, String _name, String _brand, String _miscInfo, double _price, int _stock, ArrayList<String> _imageURLs, ArrayList<String> _tags)
    {
        id = _id;
        name = _name;
        brand = _brand;
        miscInfo = _miscInfo;
        price = _price;
        stock = _stock;
        imageURLs = _imageURLs;
        tags = _tags;
    }

    public Product(int _id, String _name, String _brand, String _miscInfo, double _price, int _stock)
    {
        id = _id;
        name = _name;
        brand = _brand;
        miscInfo = _miscInfo;
        stock = _stock;
        price = _price;
    }

    public Product(int _id, String _name, String _brand, double _price, int _stock)
    {
        id = _id;
        name = _name;
        brand = _brand;
        price = _price;
        stock = _stock;
    }

    public Product(Parcel in) {
        id = in.readInt();
        name = in.readString();
        brand = in.readString();
        miscInfo = in.readString();
        price = in.readDouble();
        stock = in.readInt();
        colorIDCounter = in.readInt();
        in.readList(imageURLs, ArrayList.class.getClassLoader());
        in.readList(tags, ArrayList.class.getClassLoader());
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    public int getID()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getBrand()
    {
        return brand;
    }

    public String getInfo()
    {
        if (miscInfo == null || miscInfo.isEmpty())
            return "Not Available";
        else
            return miscInfo;
    }

    public ArrayList<String> getTags()
    {
            return tags;
    }

    public double getPrice()
    {
        return price;
    }

    public int getStock()
    {
        return stock;
    }

    public boolean inStock()
    {
        if (stock > 0)
            return true;
        else
            return false;
    }

    private void addImgUrl(String url) {
        imageURLs.add(url);
    }

    public boolean searchTag(String s)
    {
        return tags.contains(s);
    }

    public static Product getProductById(int id) {
        Product result = new Product(0, "Peanut Butter", "Jif", "", 3, 3);
        result.addImgUrl("https://s3.us-east-2.amazonaws.com/jms-s3-cx-rel-p-pmc4/assets/jif/images/products/main-images/product_pb_natural_creamy.png");
        return result;
    }

    public ArrayList<String> getImageURLs() {
        return imageURLs;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(brand);
        dest.writeString(miscInfo);
        dest.writeDouble(price);
        dest.writeInt(stock);
        dest.writeInt(colorIDCounter);
        dest.writeList(imageURLs);
        dest.writeList(tags);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Product && (((Product) other).getID() == this.getID());
    }

    @Override
    public int hashCode() {
        return id;
    }
}