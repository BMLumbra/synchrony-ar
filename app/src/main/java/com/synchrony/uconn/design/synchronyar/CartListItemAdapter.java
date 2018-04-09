package com.synchrony.uconn.design.synchronyar;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Locale;

public class CartListItemAdapter extends ArrayAdapter {
    private final Activity activity;
    private final Product[] products;
    private final Integer[] quantities;

    public CartListItemAdapter(Activity activity, Product[] productArray, Integer[] quantityArray) {
        super(activity, R.layout.cart_item, productArray);

        this.activity = activity;
        this.products = productArray;
        this.quantities = quantityArray;
    }

    @NonNull public View getView(int position, View view, @NonNull ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        view = inflater.inflate(R.layout.cart_item, parent, false);

        ImageView productImageView = view.findViewById(R.id.cart_item_image);
        Glide.with(activity).load(products[position].getImageURLs().get(0)).into(productImageView);

        TextView productNameView = view.findViewById(R.id.cart_item_name);
        productNameView.setText(products[position].getName());

        EditText quantityEditTextView = view.findViewById(R.id.cart_item_quantity);
        quantityEditTextView.setText(String.format(Locale.US, "%d", quantities[position]));

        return view;
    }
}
