package com.synchrony.uconn.design.synchronyar;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class CartListItemAdapter extends ArrayAdapter {
    private final Activity activity;
    private final Cart cart;
    private ListView cartListView;

    public CartListItemAdapter(Activity activity, Cart cart, ListView cartListView) {
        super(activity, R.layout.cart_item, cart.getItemQuantitySet().toArray());

        this.activity = activity;
        this.cart = cart;
        this.cartListView = cartListView;
    }

    @NonNull public View getView(final int position, View view, @NonNull ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        view = inflater.inflate(R.layout.cart_item, parent, false);

        ArrayList<Map.Entry<Product, Integer>> productQuantityPairs = new ArrayList<>(cart.getItemQuantitySet());
        final Product product = productQuantityPairs.get(position).getKey();
        Integer quantity = productQuantityPairs.get(position).getValue();

        ImageView productImageView = view.findViewById(R.id.cart_item_image);
        Glide.with(activity).load(product.getImageURLs().get(0)).into(productImageView);

        TextView productNameView = view.findViewById(R.id.cart_item_name);
        String productNameText = product.getBrand() + " " + product.getName();
        productNameView.setText(productNameText);

        final EditText quantityEditTextView = view.findViewById(R.id.cart_item_quantity);
        quantityEditTextView.setText(String.format(Locale.US, "%d", quantity));

        TextView productUnitPriceView = view.findViewById(R.id.cart_item_unit_price);
        String productUnitPriceText = String.format(Locale.US, "$%.2f", product.getPrice());
        productUnitPriceView.setText(productUnitPriceText);

        final TextView productTotalPriceView = view.findViewById(R.id.cart_item_total_price);
        String productTotalPriceText = String.format(Locale.US, "$%.2f", product.getPrice() * quantity);
        productTotalPriceView.setText(productTotalPriceText);

        quantityEditTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    CharSequence newText = v.getText();
                    if (newText.length() != 0) {
                        int newQuantity = Integer.parseInt(newText.toString());
                        int originalLength = productTotalPriceView.getText().length();
                        float originalTextSize = productTotalPriceView.getTextSize();
                        productTotalPriceView.setText(String.format(Locale.US, "$%.2f", product.getPrice() * newQuantity));
                        int newLength = productTotalPriceView.getText().length();
                        float newTextSize = ((float)originalLength / newLength) * originalTextSize;
                        productTotalPriceView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
                        cart.setQuantity(product, newQuantity);
                    }
                    handled = true;
                }

                return handled;
            }
        });

        ImageButton removeButton = view.findViewById(R.id.cart_item_remove);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cart.removeItem(product);
                notifyDataSetChanged();
            }
        });

        return view;
    }

    @Override
    public int getCount() {
        return cart.getItemQuantitySet().size();
    }
}
