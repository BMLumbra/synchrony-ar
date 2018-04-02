package com.synchrony.uconn.design.synchronyar;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class UserInterface extends Activity {
    Product currentProduct = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interface);

        currentProduct = getIntent().getExtras().getParcelable("currentProduct");

        Button button= (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // goes back to main activity which is rendering the camera
                finish();
            }
        });
    }

}
