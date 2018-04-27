package com.synchrony.uconn.design.synchronyar;

import android.os.Bundle;
import android.app.Activity;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

public class UserInterface extends Activity {
    Product currentProduct = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interface);

        currentProduct = getIntent().getExtras().getParcelable("currentProduct");
        // Sets each of the TextViews with the information on the product from database
        TextView nameText = (TextView) findViewById(R.id.textView);
        nameText.setText(currentProduct.getName());
        TextView brandText = (TextView) findViewById(R.id.textView2);
        brandText.setText(currentProduct.getBrand());

        // Back button to go back to main activity
        Button button= (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // goes back to main activity which is rendering the camera
                finish();
            }
        });

    }

}
