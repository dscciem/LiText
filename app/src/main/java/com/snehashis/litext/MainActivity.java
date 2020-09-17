package com.snehashis.litext;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;
import java.io.*;


public class MainActivity extends AppCompatActivity {

    EditText userInput, fileName;

    ImageButton saveButton, openButton, backButton, settingsButton;
    Uri currentFileUri;

    String CURRENT_FILE_NAME="";
    int FONT_SIZE = 18;//in sp

    private static final int READ_REQ = 0, WRITE_REQ = 1;
    Boolean isExistingFile = false, isNotWordWrapped =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInput = findViewById(R.id.userInput);
        userInput.setHorizontallyScrolling(true);//Word Wrap Off
        isNotWordWrapped = true;
        fileName = findViewById(R.id.fileName);

        fileName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Do something if needed

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Do something
                
            }

            @Override
            public void afterTextChanged(Editable s) {
                CURRENT_FILE_NAME = s.toString().trim();
                isExistingFile = false;
            }
        });

        openButton = findViewById(R.id.openButton);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);
        settingsButton = findViewById(R.id.settingsButton);

        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openDocument = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                openDocument.addCategory(Intent.CATEGORY_OPENABLE);
                openDocument.setType("text/*");
                startActivityForResult(openDocument, READ_REQ);
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
        });

        openButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(MainActivity.this, "Open an existing file", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExistingFile) {
                    Toast.makeText(MainActivity.this, "Saving Existing File...", Toast.LENGTH_SHORT).show();
                    editFile(currentFileUri);
                }
                else {
                    Toast.makeText(MainActivity.this, "Saving as..." + CURRENT_FILE_NAME, Toast.LENGTH_SHORT).show();
                    Intent newDocument = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    newDocument.addCategory(Intent.CATEGORY_OPENABLE);
                    newDocument.setType("text/*|application/*log*|application/json|application/*xml*|application/*latex*|application/javascript");
                    newDocument.putExtra(Intent.EXTRA_TITLE, CURRENT_FILE_NAME);
                    startActivityForResult(newDocument, WRITE_REQ);
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                }
                Toast.makeText(MainActivity.this, "File Saved", Toast.LENGTH_SHORT).show();
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
        });

        saveButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(MainActivity.this, "Save / Save as", Toast.LENGTH_SHORT).show();
                return false;
            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                finish();
            }
        });

        //Building Settings Dialog
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                AlertDialog.Builder settingsDialog =new AlertDialog.Builder(MainActivity.this);
                settingsDialog.setTitle("Settings");
                settingsDialog.setMessage("Change Editor Settings");
                final View dialogView = getLayoutInflater().inflate(R.layout.edtior_settings_dialog,null);
                settingsDialog.setView(dialogView);
                final EditText fontSize = dialogView.findViewById(R.id.textSize);
                final Button posi = dialogView.findViewById(R.id.posiButton), nega = dialogView.findViewById(R.id.negaButton);
                final Switch wordWrap =dialogView.findViewById(R.id.wordWrap);

                fontSize.setText(Integer.toString(FONT_SIZE));
                wordWrap.setChecked(!isNotWordWrapped);

                wordWrap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userInput.setHorizontallyScrolling(!wordWrap.isChecked());
                    }
                });

                posi.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(Integer.parseInt(fontSize.getText().toString()) <= 62) {
                            int TMP_SIZE=Integer.parseInt(fontSize.getText().toString()) + 2;
                            fontSize.setText(Integer.toString(TMP_SIZE));
                            userInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, TMP_SIZE);
                        }
                        else
                            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    }
                });
                nega.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(Integer.parseInt(fontSize.getText().toString()) >= 10){
                            int TMP_SIZE=Integer.parseInt(fontSize.getText().toString()) - 2;
                            fontSize.setText(Integer.toString(TMP_SIZE));
                            userInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, TMP_SIZE);
                        }
                        else
                            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    }
                });
                settingsDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "Settings Ok", Toast.LENGTH_SHORT).show();
                        FONT_SIZE = Integer.parseInt(fontSize.getText().toString());
                        isNotWordWrapped = !wordWrap.isChecked();
                        //Do something
                    }
                });

                settingsDialog.setNeutralButton("Default", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "Restored Default Values", Toast.LENGTH_SHORT).show();
                        FONT_SIZE = 18;
                        userInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        isNotWordWrapped = true;
                        userInput.setHorizontallyScrolling(true);
                        //Restore Defaults
                    }
                });
                settingsDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE);
                        userInput.setHorizontallyScrolling(isNotWordWrapped);
                        //Do Nothing
                    }
                });
                settingsDialog.setIcon(R.drawable.ic_font);
                settingsDialog.show();
            }
        });



        //Handle incoming intent for opening files from outside the app
        Intent handleIntent = getIntent();
        if (handleIntent.getData() != null) {
            readFile(handleIntent.getData());
        }

        //Will Also reopen the last file from here from the cache
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                 checkWritePermission();
        }

    }

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
                }
                break;
            }
            case WRITE_REQ:{
                if (resultCode == RESULT_OK) {
                    assert data != null;
                    currentFileUri = data.getData();
                    assert currentFileUri != null;
                    editFile(currentFileUri);
                }
                break;
            }

            default:{
                Toast.makeText(this, "Unknown Request Code: " + requestCode, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void readFile(@NonNull Uri fileUri) {
        try {
            String pathToFile = fileUri.getLastPathSegment();
            assert pathToFile != null;
            CURRENT_FILE_NAME = pathToFile.substring(pathToFile.lastIndexOf('/') + 1);
            fileName.setText(CURRENT_FILE_NAME);
            Toast.makeText(this, "Selected: " + pathToFile , Toast.LENGTH_SHORT).show();

            //Reading File
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            StringBuilder buffer = new StringBuilder();
            assert inputStream != null;
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ( (line = br.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }
            br.close();
            inputStream.close();
            userInput.setText(buffer.toString());
            isExistingFile = true;
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void editFile(@NonNull Uri fileUri) {
        try {
            ParcelFileDescriptor fileDescriptor = this.getContentResolver().openFileDescriptor(fileUri,"rwt");
            assert fileDescriptor != null;
            FileOutputStream fileOutputStream = new FileOutputStream(fileDescriptor.getFileDescriptor());
            fileOutputStream.write(userInput.getText().toString().trim().getBytes());
            fileOutputStream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Permission Handling
    private void checkWritePermission() {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                new AlertDialog.Builder(this).setTitle("Require Storage permission").setMessage("This is required in order to read/write your files").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 7);
                    }
                }).create().show();
            }
            else{
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 7);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 7:{
                //Do something

                break;
            }
            default:
                //Do something
                break;
        }
    }
}




