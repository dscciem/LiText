package com.snehashis.litext;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Toast;

import java.io.File;

public class Dashboard extends AppCompatActivity {


    CardView newFile, openFile;

    private static final int READ_REQ = 0;

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
                startActivityForResult(openDocument, READ_REQ);
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Toast.makeText(Dashboard.this, "Select a file", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Read Uris From Cache
    private void readUriFromCache() {
        File cacheFile = new File(this.getCacheDir(), "RECENT_FILES");
        try {
            if(cacheFile.exists()){
                Toast.makeText(this, "Cache found", Toast.LENGTH_SHORT).show();
                //To be added after finishing the recycler view layout and the adapters
                //Have already tested the ability to read from cache not including in the commit to avoid confusion
            }
            else{
                Toast.makeText(this, "No Cache", Toast.LENGTH_SHORT).show();
                //To be added after finishing the recycler view layout and the adapters
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQ) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                Intent openEditor = new Intent(Dashboard.this, MainActivity.class);
                openEditor.setData(data.getData());
                startActivity(openEditor);
            }
        } else {
            Toast.makeText(this, "Unknown Request Code: " + requestCode, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Auto update the views of recent files change after coming from the main activity
        readUriFromCache();
    }
}
