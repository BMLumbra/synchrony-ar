package com.synchrony.uconn.design.synchronyar;

import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Locale;

public class InfoOverlay {
    private MainActivity activity;
    private ViewGroup parent;
    private ConstraintLayout overlayLayout;
    private Cart cart;
    private Product product;

    InfoOverlay(MainActivity activity, ViewGroup parent, Cart cart, Product product) {
        this.activity = activity;
        this.parent = parent;
        this.cart = cart;
        this.product = product;

        LayoutInflater inflater = LayoutInflater.from(activity);
        overlayLayout = (ConstraintLayout) inflater.inflate(R.layout.info_overlay, parent, false);
    }

    public void display() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (parent.findViewById(R.id.info_overlay_constraint) == null) {
                    parent.addView(overlayLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                }
            }
        });
    }

    private void update() {
        setPrice();
        setAvailability();
        setImage();
        initAddToCartButton();
    }

    private void setPrice() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView infoOverlayPrice = overlayLayout.findViewById(R.id.info_overlay_price);
                infoOverlayPrice.setText(String.format(Locale.US, "$%.2f", product.getPrice()));
            }
        });
    }

    private void setAvailability() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView infoOverlayAvailability = overlayLayout.findViewById(R.id.info_overlay_availability);
                if (product.inStock()) {
                    infoOverlayAvailability.setText(activity.getResources().getText(R.string.info_overlay_availability_yes));
                    infoOverlayAvailability.setTextColor(activity.getResources().getColor(R.color.overlay_text_available));
                } else {
                    infoOverlayAvailability.setText(activity.getResources().getText(R.string.info_overlay_availability_no));
                    infoOverlayAvailability.setTextColor(activity.getResources().getColor(R.color.overlay_text_unavailable));
                }
            }
        });
    }

    private void setImage() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView infoOverlayImage = overlayLayout.findViewById(R.id.info_overlay_picture);
                Glide.with(activity).load(product.getImageURLs().get(0)).into(infoOverlayImage);
            }
        });
    }

    private void initAddToCartButton() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageButton addToCartButton = overlayLayout.findViewById(R.id.info_overlay_cart_button);
                addToCartButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Snackbar addedToCartMessage;
                        if (cart.addToCart(product)) {
                            addedToCartMessage = Snackbar.make(overlayLayout, R.string.added_to_cart, Snackbar.LENGTH_LONG);
                        } else {
                            addedToCartMessage = Snackbar.make(overlayLayout, R.string.not_added_to_cart, Snackbar.LENGTH_LONG);
                        }
                        addedToCartMessage.setAction(R.string.checkout, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                activity.showCheckoutActivity();
                            }
                        });
                        addedToCartMessage.show();
                    }
                });
            }
        });
    }

    public void remove() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (parent.findViewById(R.id.info_overlay_constraint) != null) {
                    parent.removeView(overlayLayout);
                }
            }
        });
    }

    public void setProduct(Product newProduct) {
        if (product != newProduct) {
            product = newProduct;
            update();
        }
    }
}
