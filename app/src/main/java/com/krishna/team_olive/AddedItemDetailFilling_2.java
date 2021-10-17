package com.krishna.team_olive;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.krishna.team_olive.ml.MobilenetV110224Quant;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class AddedItemDetailFilling_2 extends AppCompatActivity {

    VideoView vv_upload;
    RecyclerView rv_img;
    FloatingActionButton fac_img;
    AddedItemDescriptionModel model;
    Button btn_next;

    Uri vdo_uri = null;
    String postid;

    String ml_predict;
    String category;
    private List<Uri> list;

    FirebaseDatabase database;
    FirebaseStorage storage;
    private ImageUploadAdapter imageUploadAdapter;
    Uri imguri1;
    int i = 0,j=0;

    ArrayList<String> wordList = new ArrayList<>();
    Bitmap bitmap;

    ArrayList<Uri> vdo_uri_remaining = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_added_item_detail_filling2);

        model = (AddedItemDescriptionModel) getIntent().getSerializableExtra("model");
        postid = model.getPostid();
        category = model.getCateogary();


        fac_img = findViewById(R.id.fac_add);
        rv_img = findViewById(R.id.rv_img);
        vv_upload = findViewById(R.id.vv_upload);
        btn_next = findViewById(R.id.btn_next);


        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        rv_img = findViewById(R.id.rv_img);
        rv_img.setHasFixedSize(true);

        GridLayoutManager layoutManager = new GridLayoutManager(AddedItemDetailFilling_2.this, 2);
        rv_img.setLayoutManager(layoutManager);

//            vv_upload.setVideoURI(video_list.get(0));
//            vv_upload.seekTo(1);


        String filename = "label.txt";

        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(getApplicationContext().getAssets().open(filename)));

            String line;

            while ((line = br.readLine())!=null){
                wordList.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        list = new ArrayList<>();

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(i==0){
                    Toast.makeText(AddedItemDetailFilling_2.this, "Upload atleast 1 image in your post", Toast.LENGTH_LONG).show();
                }

                Intent intent = new Intent(AddedItemDetailFilling_2.this, AddedItemDetailFilling_3.class);
                intent.putExtra("model",model);
                intent.putExtra("postid",postid);
                startActivity(intent);
                finish();
            }
        });

        fac_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder alert = new AlertDialog.Builder(AddedItemDetailFilling_2.this);
                View alertView = getLayoutInflater().inflate(R.layout.custom_alert_layout, null);



                //Set the view
                alert.setView(alertView);
                //Show alert
                final AlertDialog alertDialog = alert.show();
                //Can not close the alert by touching outside.
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                ImageView closeButton = (ImageView) alertView.findViewById(R.id.closeButton);
                ImageView iv_gallery_upload = (ImageView) alertView.findViewById(R.id.iv_gallery_uploado);
                ImageView iv_vdo_upload = (ImageView) alertView.findViewById(R.id.iv_vdo_upload);
                ImageView iv_camera_upload = (ImageView) alertView.findViewById(R.id.iv_alert_ml_image);

                if(j>0)
                    iv_vdo_upload.setVisibility(View.GONE);

                if(i==0)
                    iv_vdo_upload.setVisibility(View.GONE);


                iv_gallery_upload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, 30);
                        alertDialog.dismiss();
                    }
                });

                iv_vdo_upload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("video/*");
                        startActivityForResult(intent, 32);
                        alertDialog.dismiss();
                    }
                });
/*
                iv_camera_upload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, 34);
                        alertDialog.dismiss();
                    }
                });

 */

                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

            }
        });

        if(vdo_uri_remaining.size()!=0){
            vv_upload.setVideoURI(vdo_uri_remaining.get(0));
            vv_upload.seekTo(1);
        }

        imageUploadAdapter = new ImageUploadAdapter(this, list);
        rv_img.setAdapter(imageUploadAdapter);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 30 && resultCode == RESULT_OK) {
            if (data.getData() != null) {

                imguri1 = data.getData();

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imguri1);
                } catch (IOException e) {
                    e.printStackTrace();
                }



                Bitmap resized = Bitmap.createScaledBitmap(bitmap,224, 224,true );

                try {
                    MobilenetV110224Quant model = MobilenetV110224Quant.newInstance(AddedItemDetailFilling_2.this);

                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.UINT8);

                    TensorImage tbuffer =  TensorImage.fromBitmap(resized);
                    ByteBuffer byteBuffer = tbuffer.getBuffer();

                    inputFeature0.loadBuffer( byteBuffer);

                    // Runs model inference and gets result.
                    MobilenetV110224Quant.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                    int idx = getMax(outputFeature0.getFloatArray());


                    ml_predict = wordList.get(idx);

                    if(ml_predict.equals(category)){
                        Toast.makeText(AddedItemDetailFilling_2.this, ml_predict,Toast.LENGTH_SHORT).show();
                        i++;
                        list.add(imguri1);
                        imageUploadAdapter.notifyDataSetChanged();

                        final StorageReference ref = storage.getReference().child("post_files").child(postid).child("images").child(i+"");
                        ref.putFile(imguri1).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        if(i==1){
                                            database.getReference().child("allpostswithoutuser").child(postid).child("imageurl").setValue(uri.toString());
                                        }

                                        database.getReference().child("post_files").child(postid).push().setValue(uri.toString());

                                        Toast.makeText(AddedItemDetailFilling_2.this, "IMAGE SUCESSFULLY ADDED", Toast.LENGTH_SHORT).show();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AddedItemDetailFilling_2.this, "IMAGE NOT ADDED", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });

                    }
                    else{
                        AlertDialog.Builder alert_isValid = new AlertDialog.Builder(AddedItemDetailFilling_2.this);
                        View alertView = getLayoutInflater().inflate(R.layout.alert_image_validation_layout, null);



                        //Set the view
                        alert_isValid.setView(alertView);
                        final AlertDialog alertDialog = alert_isValid.show();
                        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                        ImageView iv_alert_ml = (ImageView) alertView.findViewById(R.id.iv_alert_ml_image);
                        Button btn_select_anyway = alertView.findViewById(R.id.btn_select_anyway);
                        Button btn_cancel = alertView.findViewById(R.id.btn_cancel);
                        TextView tv_ml_predict = alertView.findViewById(R.id.tv_ml_predict);

                        iv_alert_ml.setImageURI(imguri1);

                        tv_ml_predict.setText("You have selected "+category+" but image is like "+ml_predict);
                        iv_alert_ml.setImageURI(data.getData());

                        btn_select_anyway.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                i++;
                                list.add(imguri1);
                                imageUploadAdapter.notifyDataSetChanged();
                                alertDialog.dismiss();

                                final StorageReference ref = storage.getReference().child("post_files").child(postid).child("images").child(i+"");
                                ref.putFile(imguri1).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {

                                                if(i==1){
                                                    database.getReference().child("allpostswithoutuser").child(postid).child("imageurl").setValue(uri.toString());
                                                }

                                                database.getReference().child("post_files").child(postid).push().setValue(uri.toString());
                                                Toast.makeText(AddedItemDetailFilling_2.this, "IMAGE SUCESSFULLY ADDED", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(AddedItemDetailFilling_2.this, "IMAGE NOT ADDED", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });

                            }

                        });


                        btn_cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                            }
                        });

                    }

                    // Releases model resources if no longer used.
                    model.close();
                } catch (IOException e) {
                    // TODO Handle the exception
                }





//                i++;
//                list.add(imguri1);
//                imageUploadAdapter.notifyDataSetChanged();
//
//                final StorageReference ref = storage.getReference().child("post_files").child(postid).child("images").child(i+"");
//                ref.putFile(imguri1).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                            @Override
//                            public void onSuccess(Uri uri) {
//
//                                database.getReference().child("post_files").child(postid).push().setValue(uri.toString());
//                                Toast.makeText(AddedItemDetailFilling_2.this, "IMAGE SUCESSFULLY ADDED", Toast.LENGTH_SHORT).show();
//
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Toast.makeText(AddedItemDetailFilling_2.this, "IMAGE NOT ADDED", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//                });

            }
        }

        if (requestCode == 32 && resultCode == RESULT_OK) {
            if (data.getData() != null) {

                vdo_uri = data.getData();
                vdo_uri_remaining.add(vdo_uri);
                vv_upload.setVideoURI(vdo_uri);
                vv_upload.seekTo(1);
                j++;

                final StorageReference ref = storage.getReference().child("post_files").child(postid).child("videos").child(j+"");
                ref.putFile(vdo_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                database.getReference().child("post_files").child(postid).push().setValue(uri.toString());
                                Toast.makeText(AddedItemDetailFilling_2.this, "VIDEO SUCESSFULLY ADDED", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AddedItemDetailFilling_2.this, "IMAGE NOT ADDED", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }
        }

        if (requestCode == 34 && resultCode == RESULT_OK) {
            if (data.getData() != null) {

                Uri uri = data.getData();
                Toast.makeText(AddedItemDetailFilling_2.this, uri.toString(), Toast.LENGTH_LONG).show();
                i++;
                list.add(uri);
                imageUploadAdapter.notifyDataSetChanged();

                final StorageReference ref = storage.getReference().child("post_files").child(postid).child("images").child(i+"");
                ref.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if(i==1){
                                    database.getReference().child("allpostswithoutuser").child(postid).child("imageurl").setValue(uri.toString());
                                }
                                database.getReference().child("post_files").child(postid).push().setValue(uri.toString());
                                Toast.makeText(AddedItemDetailFilling_2.this, "IMAGE SUCESSFULLY ADDED", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AddedItemDetailFilling_2.this, "IMAGE NOT ADDED", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }
        }


    }

//    public boolean isValid(Uri uri){
//        AlertDialog.Builder alert_isValid = new AlertDialog.Builder(AddedItemDetailFilling_2.this);
//        View alertView = getLayoutInflater().inflate(R.layout.alert_image_validation_layout, null);
//
//
//
//        //Set the view
//        alert_isValid.setView(alertView);
//        final AlertDialog alertDialog = alert_isValid.show();
//        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//
//        ImageView iv_alert_ml = (ImageView) alertView.findViewById(R.id.iv_alert_ml_image);
//        Button btn_select_anyway = alertView.findViewById(R.id.btn_select_anyway);
//        Button btn_cancel = alertView.findViewById(R.id.btn_cancel);
//
//        iv_alert_ml.setImageURI(uri);

//        btn_select_anyway.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public boolean onClick(View v) {
//
//            }
//        });

    int getMax(float[] arr){
        int ind = 0;
        float min = 0.0F;

        for(int i = 0; i< arr.length; i++){
            if(arr[i]>min){
                ind = i;
                min = arr[i];
            }
        }
        return ind;
    }




}