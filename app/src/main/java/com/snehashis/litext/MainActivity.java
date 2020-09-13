package com.snehashis.litext;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    EditText userInput, fileName;
    Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInput = findViewById(R.id.userInput);
        fileName = findViewById(R.id.fileName);
        saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "File Save Clicked!! Saved as: '" + fileName.getText().toString() +"'", Toast.LENGTH_SHORT).show();
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
        });
    }
}