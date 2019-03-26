package com.websbro.book_zoo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {

    private ArrayList<PdfDoc> pdfDocs;
    Context context;

    public CustomAdapter(ArrayList<PdfDoc> pdfDocs,Context context) {
        this.pdfDocs = pdfDocs;
        this.context = context;
    }

    @Override
    public int getCount() {
        return pdfDocs.size();
    }

    @Override
    public Object getItem(int position) {
        return pdfDocs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {



        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_layout,parent,false);
        }

        final PdfDoc pdfDoc = (PdfDoc) this.getItem(position);
        TextView textView = convertView.findViewById(R.id.file_name);
        textView.setText(pdfDoc.getName());
        ImageView sharePdf = convertView.findViewById(R.id.share_pdf);

        sharePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentShareFile = new Intent("android.intent.action.SEND");
                intentShareFile.setType("application/pdf");
                intentShareFile.putExtra("android.intent.extra.STREAM", Uri.parse(pdfDoc.getPath()));
                intentShareFile.putExtra("android.intent.extra.SUBJECT", "Sharing File...");
                intentShareFile.putExtra("android.intent.extra.TEXT", "Sharing PDF...");
                context.startActivity(Intent.createChooser(intentShareFile, "Share this PDF"));
            }
        });


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPdfView(pdfDoc.getPath(),pdfDoc.getName());

            }
        });
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                new android.support.v7.app.AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Are you sure?")
                        .setIcon(android.R.drawable.ic_delete)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                File file = new File(pdfDoc.getPath());
                                boolean delete = file.delete();
                                if(delete){
                                    Toast.makeText(context,"Successful",Toast.LENGTH_SHORT).show();
                                    pdfDocs.remove(position);
                                    CustomAdapter.this.notifyDataSetChanged();
                                }else {
                                    Toast.makeText(context,"Failed",Toast.LENGTH_SHORT).show();
                                }

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                return true;
            }
        });

        return convertView;
    }



    public void openPdfView(String path, String name){
        Intent intent = new Intent(context,Pdf_Activity.class);
        intent.putExtra("path",path);
        intent.putExtra("name",name);
        context.startActivity(intent);

    }
}
