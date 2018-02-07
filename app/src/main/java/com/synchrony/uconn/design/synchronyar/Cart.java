package com.synchrony.uconn.design.synchronyar;

import java.util.*;

public class Cart
{
    private ArrayList<Product> contents = new ArrayList<>();

    //checkout total
    private double total = 0.00;

    //Uniquee customer ID
    private int customerID;

    //returns true if add to cart is successful, false otherwise
    public boolean addToCart(Product p)
    {
        if(p.inStock())
        {
            contents.add(p);
            total += p.getPrice();
            return true;
        }
        else
            return false;
    }

    public void removeItem(int index)
    {
        final int i = index - 1;
        contents.remove(i);
    }

    public void removeItem(String name)
    {
        int size = contents.size();
        for(int i = 0; i < size; i++)
        {
            if(contents.get(i).getName().equalsIgnoreCase(name))
            {
                contents.remove(i);
                break;
            }
        }
    }

    public double updatesPrice()
    {
        total = 0;
        for(Product p : contents)
        {
            total += p.getPrice();
        }

        return total;
    }
}
