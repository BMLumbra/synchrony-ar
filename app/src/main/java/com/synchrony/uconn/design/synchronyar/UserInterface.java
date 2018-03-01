package com.synchrony.uconn.design.synchronyar;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

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

        Spinner spinner_color = (Spinner) findViewById(R.id.spinner_colors);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Colors, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_color.setPrompt("Select for Colors");

        Spinner spinner_size = (Spinner) findViewById(R.id.spinner_size);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.Sizes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_color.setPrompt("Select for sizes");

/*
        spinner.setAdapter(
                new NothingSelectedSpinnerAdapter(
                        adapter,
                        R.layout.contact_spinner_row_nothing_selected,
                        // R.layout.contact_spinner_nothing_selected_dropdown, // Optional
                        this));*/
    }

}
