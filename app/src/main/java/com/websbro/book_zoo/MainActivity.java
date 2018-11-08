package com.websbro.book_zoo;

import android.Manifest;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Dialog optionDialog;

    //Instance of cardViews
    GridView gridView;

    /** Bug notes
     *  -- able to add files from google drive , but need to do in background and show spinner
     *  -- set different icons to default folder created
     *  -- some pdf files not able to open
     *  -- not using background thread so main thread blocks sometime
     */

//    Button click effects
//    final ImageView v = (ImageView) findViewById(R.id.button0);
//        v.setOnTouchListener(new OnTouchListener() {
//        @Override
//        public boolean onTouch(View arg0, MotionEvent arg1) {
//            switch (arg1.getAction()) {
//                case MotionEvent.ACTION_DOWN: {
//                    v.setImageBitmap(res.getDrawable(R.drawable.img_down));
//                    break;
//                }
//                case MotionEvent.ACTION_CANCEL:{
//                    v.setImageBitmap(res.getDrawable(R.drawable.img_up));
//                    break;
//                }
//            }
//            return true;
//        }
//    });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Shared preferences to check the code runs first time after install only
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean checkFirstTime = pref.getBoolean("first_time",true);


        int permissionCheck1 = ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE");
        int permissionCheck2 = ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE");



        if(permissionCheck1==0 && permissionCheck2==0){
            if(checkFirstTime) {
                createFolders();
                SharedPreferences.Editor editor = pref.edit();
                aboutDialog();
                editor.putBoolean("first_time",false);
                editor.apply();
            }
        }else{

            getReadExternalStoragePermission();
            getWriteExternalStoragePermission();
        }


        gridView = findViewById(R.id.grid_view);



        if(!Environment.getExternalStoragePublicDirectory(getString(R.string.app_name)).exists()){
            createFolders();
        }

        optionDialog = new Dialog(this);


        gridView.setAdapter(new HomeFolderAdapter(getFolders(), MainActivity.this));






    }


    //Getting home predefined folders Users can modify it later
    private ArrayList<HomeFolder> getFolders() {

        ArrayList<HomeFolder> folders = new ArrayList<>();
        HomeFolder addFolder = new HomeFolder();
        addFolder.setDrawable(getDrawable(R.drawable.ic_add_black_50dp));
        addFolder.setName(getString(R.string.addNew));
        addFolder.setId(661998);
        folders.add(addFolder);

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




    //Dialog popup to show options as navigation bar
    public void showPopup(View view) {
        TextView close;
        Button about;

        optionDialog.setContentView(R.layout.popup_options);
        close = optionDialog.findViewById(R.id.close_options);
        about = optionDialog.findViewById(R.id.about);

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionDialog.dismiss();
                aboutDialog();


            }
        });


        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionDialog.dismiss();
            }
        });
        optionDialog.show();
    }

    public void aboutDialog(){
        final Dialog aboutDialog = new Dialog(MainActivity.this);
        aboutDialog.setContentView(R.layout.about_dialog);

        Button closeDialogButton = aboutDialog.findViewById(R.id.about_dialog_close);
        closeDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aboutDialog.dismiss();

            }
        });

        aboutDialog.show();
    }


    private void getReadExternalStoragePermission() {
        //getting permission for external storage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant

            } else {

            }
        }
    }

    private void getWriteExternalStoragePermission() {
        //getting permission for external storage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant

            } else {

            }
        }
    }

    //Creating some predefined folders for user to see on app start , Users can delete these and change according to needs
    private void createFolders() {

        String[] folders = {getString(R.string.my_docs)};


        File directory = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
        directory.mkdir();
        for (int i = 0; i < folders.length; i++) {
            File internal = new File(Environment.getExternalStoragePublicDirectory(getString(R.string.app_name)), folders[i]);
            internal.mkdir();

        }
    }

    boolean backPressed = false;

    @Override
    public void onBackPressed() {
        if(backPressed) {
            moveTaskToBack(true);
            finish();
            System.exit(0);
            return;
        }
        this.backPressed = true;
        Toast.makeText(this,"Press again to exit",Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.backPressed = false;
            }
        },2000);

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==1){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                createFolders();
                gridView.setAdapter(new HomeFolderAdapter(getFolders(), MainActivity.this));
            }else {

            }
        }
    }

}
