package com.snehashis.litext;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Toast;

public class Dashboard extends AppCompatActivity {


    CardView newFile, openFile;

    private static final int READ_REQ = 0, WRITE_REQ = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        newFile = findViewById(R.id.createFileCard);
        openFile = findViewById(R.id.openFileCard);

        newFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emptyEditor = new Intent(Dashboard.this, MainActivity.class);
                startActivity(emptyEditor);
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
        });

        openFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openDocument = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                openDocument.addCategory(Intent.CATEGORY_OPENABLE);
                openDocument.setType("text/*");
                //startActivityForResult(openDocument, READ_REQ);
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Toast.makeText(Dashboard.this, "Adding Soon", Toast.LENGTH_SHORT).show();
            }
        });
    }

/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case READ_REQ :{
                if(resultCode == RESULT_OK) {
                    assert data != null;
                    currentFileUri = data.getData();
                    assert currentFileUri != null;
                    readFile(currentFileUri);
                    isExistingFile=true;
                }
                break;
            }
            case WRITE_REQ:{
                if (resultCode == RESULT_OK) {
                    //Do something
                }
                break;
            }

            default:{
                Toast.makeText(this, "Unknown Request Code: " + requestCode, Toast.LENGTH_SHORT).show();
            }
        }
    }
*/

}