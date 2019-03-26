package com.websbro.book_zoo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageScrollListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FileUtils;
import com.shockwave.pdfium.PdfPasswordException;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

public class Pdf_Activity extends AppCompatActivity {
    Intent intent;
    PDFView pdfView;
    String password;
    String path;
    Uri uri;
    ActionBar actionBar;

    int pageNumber;
    boolean sourcecheck;
    SharedPreferences sharedpreferences;

    String fileName;
    Dialog addToShelves;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_);

        pdfView = findViewById(R.id.pdf_view);
        actionBar = getSupportActionBar();

        sharedpreferences = getSharedPreferences("pages", Context.MODE_PRIVATE);




        addToShelves = new Dialog(this);

        intent = this.getIntent();

        if(intent.hasExtra("path")){
            sourcecheck = true;
            path = intent.getExtras().getString("path");
            fileName = intent.getStringExtra("name");
            fileName = fileName.substring(0,fileName.length()-4);
            fromInside(path);

        }else {
            sourcecheck = false;
            uri = intent.getData();

            fileName = uri.getPathSegments().get(uri.getPathSegments().size()-1);
            fileName = fileName.substring(0,fileName.length()-4);
            fromOutside(uri);
        }

        actionBar.setTitle(fileName);




    }


    private void fromInside(String path) {


        final File file = new File(path);
        pageNumber = sharedpreferences.getInt(fileName,0);

        if (file.canRead()) {
            pdfView.fromFile(file)
                    .onError(new OnErrorListener() {
                        @Override
                        public void onError(Throwable t) {
                            if(t instanceof PdfPasswordException){
                                passwordHandleWithFile();

                            }
                        }
                    })
                    .defaultPage(pageNumber)
                    .onPageChange(new OnPageChangeListener() {
                        @Override
                        public void onPageChanged(int page, int pageCount) {
                            pageNumber = page;
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putInt(fileName,page);
                            editor.apply();
                        }
                    })
                    .onPageScroll(new OnPageScrollListener() {
                        @Override
                        public void onPageScrolled(int page, float positionOffset) {
                            if(actionBar.isShowing()){
                                actionBar.hide();
                            }
                        }
                    })
                    .password(this.password)
                    .onTap(new OnTapListener() {
                        @Override
                        public boolean onTap(MotionEvent e) {
                            if(!actionBar.isShowing()){
                                actionBar.show();
                            }else {
                                actionBar.hide();
                            }
                            return false;
                        }
                    })
                    .scrollHandle(new DefaultScrollHandle(this))
                    .onLoad(new OnLoadCompleteListener() {
                        @Override
                        public void loadComplete(int nbPages) {
                            Toast.makeText(getApplicationContext(), Integer.toString(nbPages), Toast.LENGTH_SHORT).show(); }
                    }).load();
            }
    }

    private void fromOutside(Uri uri) {
        if(uri == null){
            Toast.makeText(this,"Not able to open File",Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            final InputStream inputStream = getContentResolver().openInputStream(uri);
            pageNumber = sharedpreferences.getInt(fileName,0);
            pdfView.fromStream(inputStream)
                    .defaultPage(pageNumber)
                    .onError(new OnErrorListener() {
                        @Override
                        public void onError(Throwable t) {
                            if(t instanceof PdfPasswordException){
                                passwordHandleWithStream();

                            }
                        }
                    })
                    .onPageChange(new OnPageChangeListener() {
                        @Override
                        public void onPageChanged(int page, int pageCount) {
                            pageNumber = page;
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putInt(fileName,page);
                            editor.apply();
                        }
                    })
                    .onPageScroll(new OnPageScrollListener() {
                        @Override
                        public void onPageScrolled(int page, float positionOffset) {
                            if(actionBar.isShowing()){
                                actionBar.hide();
                            }
                        }
                    })
                    .onTap(new OnTapListener() {
                        @Override
                        public boolean onTap(MotionEvent e) {
                            if(!actionBar.isShowing()){
                                actionBar.show();
                            }else {
                                actionBar.hide();
                            }
                            return false;
                        }
                    })
                    .password(password)
                    .scrollHandle(new DefaultScrollHandle(this))
                    .onLoad(new OnLoadCompleteListener() {
                        @Override
                        public void loadComplete(int nbPages) {
                            Toast.makeText(getApplicationContext(), Integer.toString(nbPages), Toast.LENGTH_SHORT);
                        }
                    }).load();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void passwordHandleWithStream(){
        final Dialog enterPassword = new Dialog(Pdf_Activity.this);
        enterPassword.setContentView(R.layout.password_exception);
        final TextView userPassword = enterPassword.findViewById(R.id.user_password);
        Button passwordOk = enterPassword.findViewById(R.id.password_ok);
        Button passwordCancel = enterPassword.findViewById(R.id.password_cancel);
        passwordOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userPassword.getText().toString().equals("")){
                    Toast.makeText(Pdf_Activity.this,"password should not be empty",Toast.LENGTH_SHORT).show();
                }else {
                    Pdf_Activity.this.password = userPassword.getText().toString();
                    fromOutside(Pdf_Activity.this.uri);
                    enterPassword.dismiss();
                }
            }
        });
        passwordCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterPassword.dismiss();
                Pdf_Activity.super.onBackPressed();
            }
        });
        enterPassword.show();
    }


    private void passwordHandleWithFile(){
        final Dialog enterPassword = new Dialog(Pdf_Activity.this);
        enterPassword.setContentView(R.layout.password_exception);
        final TextView userPassword = enterPassword.findViewById(R.id.user_password);
        Button passwordOk = enterPassword.findViewById(R.id.password_ok);
        Button passwordCancel = enterPassword.findViewById(R.id.password_cancel);
        passwordOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userPassword.getText().toString().equals("")){
                    Toast.makeText(Pdf_Activity.this,"password should not be empty",Toast.LENGTH_SHORT).show();
                }else {
                    Pdf_Activity.this.password = userPassword.getText().toString();
                    fromInside(path);
                    enterPassword.dismiss();
                }
            }
        });
        passwordCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterPassword.dismiss();
                Pdf_Activity.super.onBackPressed();
            }
        });
        enterPassword.show();
    }


    //Menu bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pdf_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_to_shelve:if(!sourcecheck){
                addToShelves();
            }else {
                Toast.makeText(this,"Already in the shelve",Toast.LENGTH_SHORT).show();
            }
                break;
            case R.id.delete:delete();
                break;
            case R.id.share: share();
                break;
        }

        return(super.onOptionsItemSelected(item));
    }

    public void delete(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete")
                .setMessage("Are you sure")
                .setIcon(android.R.drawable.ic_delete)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File file = new File(path);
                        boolean delete = file.delete();
                        if(delete){
                            Toast.makeText(Pdf_Activity.this,"Successful",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Pdf_Activity.this,List_Activity.class);
                            // getting the parent path to create list activity
                            String tempPath = path;
                            int index = tempPath.lastIndexOf("/");
                            tempPath = tempPath.substring(0,index);
                            intent.putExtra("path",tempPath);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();



    }
    public void share(){
        Intent intentShareFile = new Intent("android.intent.action.SEND");
        intentShareFile.setType("application/pdf");
        intentShareFile.putExtra("android.intent.extra.STREAM", Uri.parse(path));
        intentShareFile.putExtra("android.intent.extra.SUBJECT", "Sharing File...");
        intentShareFile.putExtra("android.intent.extra.TEXT", "Sharing PDF...");
        startActivity(Intent.createChooser(intentShareFile, "Share this PDF"));
    }

    public void addToShelves(){
        final EditText editFileName;
        TextView close;
        ListView shelves;

        addToShelves.setContentView(R.layout.shelves_list);
        close = addToShelves.findViewById(R.id.close_shelves);
        shelves = addToShelves.findViewById(R.id.add_shelves);
        editFileName = addToShelves.findViewById(R.id.edit_filename);

        editFileName.setText(fileName);

        ArrayList<String> shelveName = new ArrayList<>();
        for(int i=0;i<getFolders().size();i++){
            shelveName.add(getFolders().get(i).getName());
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,R.layout.shelve_list_item,shelveName);
        shelves.setAdapter(arrayAdapter);
        close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               addToShelves.dismiss();
            }
        });


        shelves.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String editedFileName =editFileName.getText().toString();
                copyFile(uri,getFolders().get(position).getPath(),editedFileName);
                addToShelves.dismiss();
            }
        });

        addToShelves.show();


    }

    public ArrayList<HomeFolder> getFolders(){
        ArrayList<HomeFolder> folders = new ArrayList<>();

        File downloadFolder = new File(Environment.getExternalStorageDirectory(),getString(R.string.app_name));


        if(downloadFolder.exists()){
            File[] files = downloadFolder.listFiles();

            for( int i =0 ; i<files.length;i++){
                File currFile = files[i];

                if(currFile.isDirectory()){

                    HomeFolder homeFolder = new HomeFolder();
                    homeFolder.setName(currFile.getName());
                    homeFolder.setPath(currFile.getPath());
                    folders.add(homeFolder);

                }

            }
        }

        return folders;
    }

    public void copyFile(Uri selectedFile,String filePath,String editedFileName){
        boolean test = false;


        File destination = new File(filePath, editedFileName+".pdf");
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedFile);
            FileUtils.copy(inputStream, destination);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to add File", Toast.LENGTH_LONG).show();
            test = true;
        }


        if (!test) {
            Toast.makeText(this, "File added sucessfully", Toast.LENGTH_LONG).show();
        }
    }




}
