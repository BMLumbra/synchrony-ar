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

public class CartListItemAdapter extends ArrayAdapter {
    private final Activity context;
    private final Product[] products;
    private final Integer[] quantities;

    public CartListItemAdapter(Activity context, Product[] productArray, Integer[] quantityArray) {
        super(context, R.layout.cart_item, productArray);

        this.context = context;
        this.products = productArray;
        this.quantities = quantityArray;
    }

    @NonNull public View getView(int position, View view, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        view = inflater.inflate(R.layout.cart_item, parent, false);

        ImageView productImageView = view.findViewById(R.id.cart_item_image);
        productImageView = products[position].getPreviewImageView();

        TextView productNameView = view.findViewById(R.id.cart_item_name);
        productNameView.setText(products[position].getName());

        EditText quantityEditTextView = view.findViewById(R.id.cart_item_quantity);
        quantityEditTextView.setText(quantities[position]);

        return view;
    }
}
