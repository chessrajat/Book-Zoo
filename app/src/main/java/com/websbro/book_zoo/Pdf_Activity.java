package com.websbro.book_zoo;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.File;
import java.io.InputStream;

public class Pdf_Activity extends AppCompatActivity {
    Intent intent;
    PDFView pdfView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_);

        pdfView = findViewById(R.id.pdf_view);

        intent = this.getIntent();

        if(intent.hasExtra("path")){

            fromInside();

        }else {
            fromOutside();
        }



    }


    private void fromInside() {

        String path = intent.getExtras().getString("path");

        File file = new File(path);


        if (file.canRead()) {
            pdfView.fromFile(file)
                    .scrollHandle(new DefaultScrollHandle(this))
                    .onLoad(new OnLoadCompleteListener() {
                        @Override
                        public void loadComplete(int nbPages) {
                            Toast.makeText(getApplicationContext(), Integer.toString(nbPages), Toast.LENGTH_SHORT).show(); }
                    }).load();
            }
    }

    private void fromOutside() {
        Uri uri = intent.getData();
        if(uri == null){
            Toast.makeText(this,"Not able to open File",Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            pdfView.fromStream(inputStream)
                    .defaultPage(0)
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



}
