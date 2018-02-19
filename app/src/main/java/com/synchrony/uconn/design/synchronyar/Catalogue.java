package com.synchrony.uconn.design.synchronyar;

/**
 * Created by ahouw on 2/19/2018.
 */
import java.util.*;
public class Catalogue
{
    private ArrayList<ArrayList<Product>> hashTable = new ArrayList();
    private int maxSize = 1;

    //Returns the hash code given a string id
    private int hashCode(String id)
    {
        return hashCode(id.hashCode());
    }

    //Returns the hash code given an int id
    private int hashCode(int id)
    {
        return id%maxSize;
    }

    //Adds a product to the hash table using a index
    private void addProduct(int index, Product p)
    {
        ArrayList<Product> temp = hashTable.get(index);
        temp.add(p);
        hashTable.set(index, temp);
    }
}
