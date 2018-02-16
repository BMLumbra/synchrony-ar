package com.synchrony.uconn.design.synchronyar;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

public class UserInterface extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interface);

        Button button= (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // goes back to main activity which is rendering the camera
                finish();
            }
        });
    }

}
