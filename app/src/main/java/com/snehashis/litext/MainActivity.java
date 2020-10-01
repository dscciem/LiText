package com.snehashis.litext;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class MainActivity extends AppCompatActivity {

    EditText userInput, fileName;

    ImageButton saveButton, openButton, backButton, settingsButton, undoButton, redoButton;
    Uri currentFileUri;

    String CURRENT_FILE_NAME="";
    int FONT_SIZE = 18;//in sp

    Stack undoStack, redoStack;
    final TextWatcher textWatcher;

    private static final int READ_REQ = 0, WRITE_REQ = 1;
    Boolean isExistingFile = false, isSaved = false, isNotWordWrapped =false;

    public MainActivity() {
        this.textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Do Something
                undoStack.push(s.toString());
                updateUndoRedo();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Do Something
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Do something
            }
        };
    }

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

        undoStack = new Stack();
        redoStack = new Stack();



        userInput.addTextChangedListener(textWatcher);

        openButton = findViewById(R.id.openButton);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);
        undoButton = findViewById(R.id.undoButton);
        redoButton = findViewById(R.id.redoButton);
        settingsButton = findViewById(R.id.settingsButton);

        //Initially Disabling the buttons
        undoButton.setEnabled(false);
        undoButton.setAlpha((float) 0.5);
        redoButton.setEnabled(false);
        redoButton.setAlpha((float) 0.5);

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
                    saveFile();
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                }
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
                if( (isExistingFile && !isSaved) || (!isExistingFile && !userInput.getText().toString().isEmpty())){
                    AlertDialog.Builder savePrompt = new AlertDialog.Builder(MainActivity.this);
                    savePrompt.setTitle("File Not Saved!");
                    savePrompt.setMessage("This file is not saved if you exit without saving all your changes will be discarded.\nDo you want to save the changes?");
                    savePrompt.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(isExistingFile)
                                editFile(currentFileUri);
                            else
                                saveFile();
                            finish();
                        }
                    });
                    savePrompt.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            Toast.makeText(MainActivity.this, "Discarded Changes", Toast.LENGTH_SHORT).show();
                        }
                    });
                    savePrompt.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "Let's keep working", Toast.LENGTH_SHORT).show();
                        }
                    });
                    savePrompt.setCancelable(false);
                    savePrompt.show();
                }
                else {
                    finish();
                }
            }
        });


        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redoStack.push(userInput.getText().toString());
                int pos = userInput.getSelectionStart();
                String tmp = undoStack.pop();
                userInput.removeTextChangedListener(textWatcher);
                userInput.setText(tmp);
                pos = Math.min(pos, tmp.length() - 1);
                userInput.addTextChangedListener(textWatcher);
                updateUndoRedo();
                userInput.setSelection(pos);
            }
        });
        undoButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(MainActivity.this, "Undo", Toast.LENGTH_LONG).show();
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                return false;
            }
        });

        redoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                undoStack.push(userInput.getText().toString());
                int pos = userInput.getSelectionStart();
                String tmp = redoStack.pop();
                userInput.removeTextChangedListener(textWatcher);
                userInput.setText(tmp);
                pos = Math.min(pos, tmp.length() - 1);
                userInput.addTextChangedListener(textWatcher);
                updateUndoRedo();
                userInput.setSelection(pos);
            }
        });
        redoButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(MainActivity.this, "Redo", Toast.LENGTH_LONG).show();
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                return false;
            }
        });


        //Building Settings Dialog
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
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
                final SwitchCompat wordWrap = dialogView.findViewById(R.id.wordWrap);

                fontSize.setText(Integer.toString(FONT_SIZE));
                wordWrap.setChecked(!isNotWordWrapped);

                wordWrap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userInput.setHorizontallyScrolling(!wordWrap.isChecked());
                        Toast.makeText(MainActivity.this, "Horizontal Scrolling : " + (!wordWrap.isChecked()?"On":"Off"), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MainActivity.this, "Settings Saved", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MainActivity.this, "Discarded Settings", Toast.LENGTH_SHORT).show();
                        userInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE);
                        userInput.setHorizontallyScrolling(isNotWordWrapped);
                        //Do Nothing
                    }
                });
                settingsDialog.setIcon(R.drawable.ic_font);
                settingsDialog.setCancelable(false);
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
            userInput.removeTextChangedListener(textWatcher);
            userInput.setText(buffer.toString());
            userInput.addTextChangedListener(textWatcher);
            isExistingFile = true;
            isSaved = true;
            currentFileUri = fileUri;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        saveUriInCache(fileUri);
    }

    private void saveFile() {
        Intent newDocument = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        newDocument.addCategory(Intent.CATEGORY_OPENABLE);
        newDocument.setType("text/*|application/*log*|application/json|application/*xml*|application/*latex*|application/javascript");
        newDocument.putExtra(Intent.EXTRA_TITLE, CURRENT_FILE_NAME.equals("")?"New file.txt":CURRENT_FILE_NAME);
        startActivityForResult(newDocument, WRITE_REQ);
    }

    private void editFile(@NonNull Uri fileUri) {
        try {
            ParcelFileDescriptor fileDescriptor = this.getContentResolver().openFileDescriptor(fileUri,"rwt");
            assert fileDescriptor != null;
            FileOutputStream fileOutputStream = new FileOutputStream(fileDescriptor.getFileDescriptor());
            fileOutputStream.write(userInput.getText().toString().trim().getBytes());
            fileOutputStream.close();
            Toast.makeText(this, "File Saved", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            Toast.makeText(this, "Error check logcat", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        saveUriInCache(fileUri);
    }

    private void updateUndoRedo(){
        if(undoStack.isEmpty){
            undoButton.setEnabled(false);
            undoButton.setAlpha((float) 0.5);
            isSaved = true;
        }
        else {
            undoButton.setEnabled(true);
            undoButton.setAlpha((float)1);
            isSaved = false;
        }
        if(redoStack.isEmpty){
            redoButton.setEnabled(false);
            redoButton.setAlpha((float) 0.5);
        }
        else {
            redoButton.setEnabled(true);
            redoButton.setAlpha((float)1);
        }
    }

    //Save Recently opened files uri
    private void saveUriInCache(Uri fileUri) {
        File cacheFile = new File(this.getCacheDir(), "RECENT_FILES");
        FileOutputStream fileOutputStream;
        try {
            if (cacheFile.exists()) {

                Scanner scanFile = new Scanner(cacheFile);
                List<String> recentFiles = new ArrayList<>();

                while (scanFile.hasNext()) {
                    recentFiles.add(scanFile.nextLine());
                }
                scanFile.close();

                if (!recentFiles.contains(fileUri.toString())) {
                    fileOutputStream = new FileOutputStream(cacheFile, true);
                    fileOutputStream.write(((fileUri.toString() + "\n").getBytes()));
                    fileOutputStream.close();
                }
            } else {
                //noinspection ResultOfMethodCallIgnored
                cacheFile.createNewFile();
                fileOutputStream = new FileOutputStream(cacheFile);
                fileOutputStream.write(((fileUri.toString() + "\n").getBytes()));
                fileOutputStream.close();
            }
        }
        catch (Exception e){
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
        //Do something
        //if (requestCode == 7) {
            //Do something
        //}
    }

    @Override
    public void onBackPressed() {
        backButton.callOnClick();
    }
}




