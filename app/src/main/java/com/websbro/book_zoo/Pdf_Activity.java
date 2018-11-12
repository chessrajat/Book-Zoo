package com.websbro.book_zoo;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfPasswordException;

import java.io.File;
import java.io.InputStream;

public class Pdf_Activity extends AppCompatActivity {
    Intent intent;
    PDFView pdfView;
    String password;
    String path;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_);

        pdfView = findViewById(R.id.pdf_view);

        intent = this.getIntent();

        if(intent.hasExtra("path")){

            path = intent.getExtras().getString("path");
            fromInside(path);

        }else {
            uri = intent.getData();
            fromOutside(uri);
        }



    }


    private void fromInside(String path) {


        final File file = new File(path);


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
                    .password(this.password)
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
            pdfView.fromStream(inputStream)
                    .onError(new OnErrorListener() {
                        @Override
                        public void onError(Throwable t) {
                            if(t instanceof PdfPasswordException){
                                passwordHandleWithStream();

                            }
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



}
