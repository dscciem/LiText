package com.snehashis.litext;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Dashboard extends AppCompatActivity implements RecentFileAdapter.ItemClicked {


    CardView newFile, openFile, recentCard;
    RecyclerView recentFilesView;
    RecyclerView.Adapter recentFilesAdapter;
    RecyclerView.LayoutManager recentFilesLayoutManager;
    LinearLayout qsLayout, recentsLayout;

    TextView recentTitle , noRecents;

    ArrayList<RecentFile> recentFiles;

    private static final int READ_REQ = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        newFile = findViewById(R.id.createFileCard);
        openFile = findViewById(R.id.openFileCard);
        recentCard = findViewById(R.id.recentFilesCard);

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

        recentFilesView = findViewById(R.id.recentsList);
        recentFilesView.setHasFixedSize(true);

        recentFilesLayoutManager = new LinearLayoutManager(this);
        recentFilesView.setLayoutManager(recentFilesLayoutManager);
        recentFiles = new ArrayList<>();
        readUriFromCache();
        recentFilesAdapter = new RecentFileAdapter(this, recentFiles);
        recentFilesView.setAdapter(recentFilesAdapter);

        noRecents = findViewById(R.id.noRecentMessage);

        qsLayout = findViewById(R.id.quickStartLayout);
        qsLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        recentsLayout = findViewById(R.id.recentsLayout);
        recentTitle = findViewById(R.id.recentsTitle);

        recentCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recentsLayout.setVisibility(View.VISIBLE);
                qsLayout.setOrientation(LinearLayout.HORIZONTAL);
                recentCard.setVisibility(View.GONE);
            }
        });

        recentTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recentCard.setVisibility(View.VISIBLE);
                qsLayout.setOrientation(LinearLayout.VERTICAL);
                recentsLayout.setVisibility(View.GONE);
            }
        });

    }

    private void openFileFromUri(Uri fileUri) {
        Intent openEditor = new Intent(Dashboard.this, MainActivity.class);
        openEditor.setData(fileUri);
        startActivity(openEditor);
    }

    //Read Uris From Cache
    private void readUriFromCache() {
        File cacheFile = new File(this.getCacheDir(), "RECENT_FILES");
        try {
            recentFiles.clear();
            if(cacheFile.exists()){
                noRecents.setVisibility(View.GONE);
                Scanner scanFile = new Scanner(cacheFile);
                while (scanFile.hasNext())
                    recentFiles.add(new RecentFile(Uri.parse(scanFile.nextLine())));
            }
            else noRecents.setVisibility(View.VISIBLE);
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
                openFileFromUri(data.getData());
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
        recentFilesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemCLicked(int index) {
        openFileFromUri(recentFiles.get(index).getFileUri());
    }
}
