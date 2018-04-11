package com.synchrony.uconn.design.synchronyar;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CheckoutActivity extends AppCompatActivity {
    private Cart cart = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Checkout");
        toolbar.setTitleTextColor(getResources().getColor(R.color.black));
        setSupportActionBar(toolbar);

        cart = getIntent().getExtras().getParcelable("cart");

        TextView subtotal = findViewById(R.id.checkout_subtotal);
        subtotal.setText(String.format(Locale.US, "$%.2f", cart.getTotal()));

        final ListView cartListView = findViewById(R.id.cart_list_view);
        final CartListItemAdapter cartListAdapter = new CartListItemAdapter(this, cart);
        cartListView.setAdapter(cartListAdapter);

        Button checkoutButton = findViewById(R.id.checkout_button);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Display checkout info

                cart.clear();
                cartListAdapter.notifyDataSetChanged();

                Snackbar finishedSnackbar = Snackbar.make(CheckoutActivity.this.findViewById(android.R.id.content), "Cart sent to store for processing", Snackbar.LENGTH_LONG);
                finishedSnackbar.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("cart", cart);
        setResult(RESULT_OK, intent);

        super.onBackPressed();
    }
}
