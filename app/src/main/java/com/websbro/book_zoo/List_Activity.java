package com.websbro.book_zoo;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class List_Activity extends AppCompatActivity {

    ListView listView;
    FloatingActionButton fab;
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_);

        TextView textView = findViewById(R.id.no_files_available);
        listView = findViewById(R.id.books);
        fab = findViewById(R.id.fab_add_file);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                String[] mimeType = {"application/pdf"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeType);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                startActivityForResult(Intent.createChooser(intent,"choose"),1);
            }
        });

        Intent intent = this.getIntent();
        path = intent.getExtras().getString("path");

        if(getPdf().size()<1){
            textView.setVisibility(View.VISIBLE);
        }else {
            textView.setVisibility(View.GONE);
        }

        listView.setAdapter(new CustomAdapter(getPdf(),List_Activity.this));




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode==1 && resultCode==RESULT_OK && data!=null) {
            //Getting the uri of file selected by user and get the path to get the filename , convert
            // uri into input stream and create destination file and copy that inputstream into destination file
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri selectedFile = data.getClipData().getItemAt(i).getUri();


                    copyFile(selectedFile);
                }
            } else {
                Uri selectedFile = data.getData();


                copyFile(selectedFile);
            }
        }
    }

    public void copyFile(Uri selectedFile){
        System.out.println("copyFile "+ selectedFile);
        boolean test = false;
        String fileName = getFileName(selectedFile);


        File destination = new File(path, fileName);
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedFile);
            FileUtils.copy(inputStream, destination);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Toast.makeText(this, "Failed to add File", Toast.LENGTH_LONG).show();
            test = true;
        }


        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
        if (!test) {
            Toast.makeText(this, "File added sucessfully", Toast.LENGTH_LONG).show();
        }
    }



    public String getFileName(Uri uri) {
        String result;

        Cursor cursor = null;
        try {
            String[] arr = { MediaStore.Images.Media.DATA };
            cursor = this.getContentResolver().query(uri,  arr, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
            System.out.println("inner "+result);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if(result==null) {
            if (uri.getScheme().equals("content")) {
                cursor = getContentResolver().query(uri, null, null, null, null);
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        System.out.println("inside " + result);
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }


        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        if(result!=null){
            int cut = result.lastIndexOf('/');
            if(cut != -1){
                result = result.substring(cut+1);
            }
        }

        System.out.println(result);
        return result;
    }



//    public String getPath(Uri uri) {
//
//        String path = null;
//        String[] projection = { MediaStore.Files.FileColumns.DATA };
//        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
//
//        if(cursor == null){
//            path = uri.getPath();
//        }
//        else{
//            cursor.moveToFirst();
//            int column_index = cursor.getColumnIndexOrThrow(projection[0]);
//            path = cursor.getString(column_index);
//            cursor.close();
//        }
//
//        return ((path == null || path.isEmpty()) ? (uri.getPath()) : path);
//    }




    private ArrayList<PdfDoc> getPdf() {

        ArrayList<PdfDoc> pdfDocs = new ArrayList<>();

        File downloadFolder = new File(path);


        if(downloadFolder.exists()){
            File[] files = downloadFolder.listFiles();

            for( int i =0 ; i<files.length;i++){
                File currFile = files[i];

                if(currFile.getPath().endsWith("pdf")){

                    PdfDoc pdfDoc = new PdfDoc();
                    pdfDoc.setName(currFile.getName());
                    pdfDoc.setPath(currFile.getPath());

                    pdfDocs.add(pdfDoc);

                }else{
                    currFile.delete();
                }

            }
        }

        return pdfDocs;

    }

}
