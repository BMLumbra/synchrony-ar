package com.synchrony.uconn.design.synchronyar;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.*;

public class Cart implements Parcelable
{
    private HashMap<Product, Integer> contentQuantities = new HashMap<>();

    //checkout total
    private double total = 0.00;

    public Cart() {

    }

    public Cart(Parcel in) {
        int size = in.readInt();
        for (int i = 0; i < size; ++i) {
            Product newProduct = in.readParcelable(Product.class.getClassLoader());
            Integer newQuantity = in.readInt();
            contentQuantities.put(newProduct, newQuantity);
        }
        total = in.readDouble();
    }

    public static final Creator<Cart> CREATOR = new Creator<Cart>() {
        @Override
        public Cart createFromParcel(Parcel source) {
            return new Cart(source);
        }

        @Override
        public Cart[] newArray(int size) {
            return new Cart[size];
        }
    };

    //returns true if add to cart is successful, false otherwise
    public boolean addToCart(Product p) {
        if(p.inStock())
        {
            if (contentQuantities.containsKey(p)) return true;

            contentQuantities.put(p, 1);
            total += p.getPrice();
            return true;
        }
        else
            return false;
    }

    public void removeItem(Product item) {
        total -= item.getPrice() * contentQuantities.get(item);
        contentQuantities.remove(item);
    }

    public void setQuantity(Product item, Integer quantity) {
        total -= item.getPrice() * contentQuantities.get(item);
        contentQuantities.put(item, quantity);
        total += item.getPrice() * contentQuantities.get(item);
    }

    public void clear() {
        contentQuantities.clear();
    }

    public double getTotal() {
        return total;
    }

    public Set<Map.Entry<Product, Integer>> getItemQuantitySet() {
        return contentQuantities.entrySet();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(contentQuantities.entrySet().size());
        for (Map.Entry<Product, Integer> e : contentQuantities.entrySet()) {
            dest.writeParcelable(e.getKey(), 0);
            dest.writeInt(e.getValue());
        }
        dest.writeDouble(total);
    }
}
