package com.synchrony.uconn.design.synchronyar;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class CheckoutActivity extends AppCompatActivity {
    private Cart cart = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cart = getIntent().getExtras().getParcelable("cart");

        Button checkoutButton = findViewById(R.id.checkout_button);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Display checkout info

                cart.clear();
            }
        });

        ListView cartListView = findViewById(R.id.cart_list_view);
        Set<Map.Entry<Product, Integer>> cartItemQuantitySet = cart.getItemQuantitySet();
        Product[] cartProductList = new Product[cartItemQuantitySet.size()];
        Integer[] cartQuantityList = new Integer[cartItemQuantitySet.size()];

        int i = 0;
        for (Map.Entry<Product, Integer> e : cartItemQuantitySet) {
            cartProductList[i] = e.getKey();
            cartQuantityList[i] = e.getValue();
            ++i;
        }

        CartListItemAdapter cartListAdapter = new CartListItemAdapter(this, cartProductList, cartQuantityList);
        cartListView.setAdapter(cartListAdapter);
    }
}
