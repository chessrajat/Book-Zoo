package com.websbro.book_zoo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.goodiebag.carouselpicker.CarouselPicker;

public class HomeFolderAdapter extends BaseAdapter {

    ArrayList<HomeFolder> homeFolders;
    Context context;
    Dialog selectorDialog;
    int SELECTED_POSITION=0;

    SharedPreferences sharedPreferences;



    public HomeFolderAdapter(ArrayList<HomeFolder> homeFolders, Context context) {
        this.homeFolders = homeFolders;
        this.context = context;
    }


    //Adapter to fill home layout gridview

    @Override
    public int getCount() {
        return homeFolders.size();
    }

    @Override
    public Object getItem(int position) {
        return homeFolders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        sharedPreferences = context.getSharedPreferences("com.websbro.yourlib",Context.MODE_PRIVATE);


        if(convertView==null){
            convertView = LayoutInflater.from(context).inflate(R.layout.topic_container_folder,parent,false);
        }
        final HomeFolder homeFolder = (HomeFolder) this.getItem(position);
        TextView folderName = convertView.findViewById(R.id.folder_name);
        saveId(homeFolder.getName(),Integer.toString(position));
        ImageView folderIcon = convertView.findViewById(R.id.folder_icon);
        if(homeFolder.getDrawable()!=null){
            folderIcon.setImageDrawable(homeFolder.getDrawable());
        }else {
            folderIcon.setImageDrawable(ContextCompat.getDrawable(context,Icons.ICONS[Integer.parseInt(getId(homeFolder.getName()))]));
        }
        folderName.setText(homeFolder.getName());


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Not good way to  check , I can't get tag to check at this moment
                if(homeFolder.getId()==661998){
                    selectorDialog = new Dialog(context);
                    showAddFolderDialog(v);

                }else {
                    openFolder(homeFolder.getPath());
                }
            }
        });

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(homeFolder.getId()==661998){
                    Toast.makeText(context,"why you want to delete this",Toast.LENGTH_SHORT).show();
                }else {
                    new AlertDialog.Builder(context)
                            .setTitle("Delete Folder")
                            .setMessage("This will delete all the files inside this folder")
                            .setIcon(android.R.drawable.ic_delete)
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    File file = new File(homeFolder.getPath());
                                    if (file.isDirectory()) {
                                        String[] children = file.list();
                                        if (children.length > 0) {
                                            for (int i = 0; i < children.length; i++) {
                                                new File(file, children[i]).delete();
                                            }
                                        }
                                        boolean delete = file.delete();
                                        if (delete) {
                                            Toast.makeText(context, "Sucessfull", Toast.LENGTH_SHORT).show();
                                            homeFolders.remove(position);
                                            HomeFolderAdapter.this.notifyDataSetChanged();
                                        } else {
                                            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                }

                return true;
            }
        });


        return convertView;
    }

    public void openFolder(String path){
        Intent intent = new Intent(context,List_Activity.class);
        intent.putExtra("path",path);
        context.startActivity(intent);
    }



    public void showAddFolderDialog(View view){

        selectorDialog.setContentView(R.layout.add_folder_dialog);
        CarouselPicker carouselPicker = selectorDialog.findViewById(R.id.icon_carousel);
        final EditText folderName = selectorDialog.findViewById(R.id.folder_name);
        Button cancelButton = selectorDialog.findViewById(R.id.cancel_creating_folder);
        Button createButton = selectorDialog.findViewById(R.id.create_folder);

        List<CarouselPicker.PickerItem> iconPicker = new ArrayList<>();
        for(int i=0;i<Icons.ICONS.length;i++){
            iconPicker.add(new CarouselPicker.DrawableItem(Icons.ICONS[i]));
        }
        CarouselPicker.CarouselViewAdapter imageAdapter = new CarouselPicker.CarouselViewAdapter(context, iconPicker, 0);
        carouselPicker.setAdapter(imageAdapter);

        carouselPicker.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                HomeFolderAdapter.this.SELECTED_POSITION =i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectorDialog.dismiss();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(folderName.getText().length()<1){
                    Toast.makeText(context,"Folder Name can not be empty",Toast.LENGTH_SHORT).show();
                }else {
                    String mfolderName = folderName.getText().toString();
                    HomeFolder tempHomeFolder = new HomeFolder();
                    tempHomeFolder.setName(mfolderName);
//                    tempHomeFolder.setDrawable(ContextCompat.getDrawable(context,Icons.ICONS[SELECTED_POSITION]));
                    tempHomeFolder.setId(SELECTED_POSITION);
                    //Creating file first then adding the path to array list item
                    File internal = new File(Environment.getExternalStoragePublicDirectory(context.getString(R.string.app_name)), mfolderName);
                    boolean directoryCreated = internal.mkdir();
                    saveId(mfolderName,Integer.toString(SELECTED_POSITION));
                    tempHomeFolder.setPath(internal.getPath());

                    homeFolders.add(tempHomeFolder);

                    //Just a test in case file not gets created
                    if(!directoryCreated){
                        directoryCreated = internal.mkdir();
                    }

                    HomeFolderAdapter.this.notifyDataSetChanged();



                    selectorDialog.dismiss();

                }



            }
        });



        selectorDialog.show();

    }

    public void saveId(String name,String id){
        sharedPreferences.edit().putString(name,id).apply();
    }

    public  String getId(String name){
        String id = sharedPreferences.getString(name,"0");
        return id;
    }
}
