package com.synchrony.uconn.design.synchronyar;

import java.util.*;
import android.widget.ImageView;


public abstract class Product
{
    private String name;

    private String brand;

    private String miscInfo;

    private double price;

    private int stock;

    private ArrayList<ArrayList<ImageView>> images = new ArrayList();

    private ArrayList<String> tags = new ArrayList<>();


    public Product(String _name, String _brand, String _miscInfo, double _price, int _stock, ArrayList<String> _tags)
    {
        name = _name;
        brand = _brand;
        miscInfo = _miscInfo;
        price = _price;
        stock = _stock;
        tags = _tags;
    }

    public Product(String _name, String _brand, String _miscInfo, double _price, int _stock)
    {
        name = _name;
        brand = _brand;
        miscInfo = _miscInfo;
        stock = _stock;
        price = _price;
    }

    public Product(String _name, String _brand, double _price, int _stock)
    {
        name = _name;
        brand = _brand;
        price = _price;
        stock = _stock;
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
        if(stock > 0)
            return true;
        else
            return false;
    }

    public void addImg(int ColorID, ImageView img)
    {
        images.get(ColorID).add(img);
    }

    public boolean searchTag(String s)
    {
        return tags.contains(s);
    }

}