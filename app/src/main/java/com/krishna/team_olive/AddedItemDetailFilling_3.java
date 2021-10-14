package com.krishna.team_olive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

public class AddedItemDetailFilling_3 extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Button btn_yes,btn_no,btn_ok;
    TextView tv;
    String text="";
    private StorageReference strRefrence;
    private DatabaseReference dataRefrence;
    private StorageTask st;
    String postid2;
    FirebaseAuth auth;
    UriContainer uri;
    AddedItemDescriptionModel model;
    int taskfinished=1;//variable to check button is clicked or not

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_added_item_detail_filling3);
        Spinner mySpinner=findViewById(R.id.spinner_1);
        btn_yes=findViewById(R.id.btn_yes);
        btn_no=findViewById(R.id.btn_no);
        btn_ok=findViewById(R.id.btn_ok);
        btn_ok.setVisibility(View.GONE);
        tv=findViewById(R.id.tv_cateogary);
        tv.setVisibility(View.GONE);
        String name=getIntent().getStringExtra("name");
        String cateogary=getIntent().getStringExtra("cateogary");
        String description=getIntent().getStringExtra("description");
        String adress=getIntent().getStringExtra("adress");
        String landmark=getIntent().getStringExtra("landmark");
        String pincode=getIntent().getStringExtra("pincode");
        String extension=getIntent().getStringExtra("extension");
        String age=getIntent().getStringExtra("age");
        model=new AddedItemDescriptionModel("",cateogary,name,age,description,adress,landmark,"",pincode,"","","",extension);
        if(model.getRatings().equals("")){
            model.setRatings("0");
        }
        uri =getIntent().getParcelableExtra("uriImage");
        strRefrence= FirebaseStorage.getInstance().getReference("uploads");//storing data to a folder uploads
        dataRefrence= FirebaseDatabase.getInstance().getReference();
        auth=FirebaseAuth.getInstance();
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setVisibility(View.VISIBLE);
                btn_ok.setVisibility(View.VISIBLE);
                ArrayAdapter<String> myAdapter=new ArrayAdapter<String>(AddedItemDetailFilling_3.this, android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.names));
                myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mySpinner.setAdapter(myAdapter);
                mySpinner.setOnItemSelectedListener(AddedItemDetailFilling_3.this);

                model.setTypeOfExchange("N");
                uploadData(model);

            }
        });
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_ok.setVisibility(View.VISIBLE);
                model.setTypeOfExchange("Y");
                uploadData(model);
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(AddedItemDetailFilling_3.this,MainActivity.class);
                startActivity(intent);

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        text=parent.getItemAtPosition(position).toString();
        model.setExchangeCateogary(text);
        // Toast.makeText(AddedItemDetailFilling_3.this, text, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    public void uploadData(AddedItemDescriptionModel m)
    {
        StorageReference fileRefrence=strRefrence.child(System.currentTimeMillis()+"."+m.getExtension());
        st=fileRefrence.putFile(uri.getUri()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskfinished++;
                Toast.makeText(AddedItemDetailFilling_3.this, "Item Added!!", Toast.LENGTH_SHORT).show();
                model.setImageurl(taskSnapshot.getMetadata().getReference().getDownloadUrl().toString().trim());
                //             postid2 = dataRefrence.child("allpostswithoutuser").push().getKey();

                model.setPostid("");
                DatabaseReference dataRefrence2 = dataRefrence.child("allpostswithoutuser").push();
                dataRefrence2.setValue(model);
                postid2 = dataRefrence2.getKey().toString();
                dataRefrence2.child("postid").setValue(postid2);
                model.setPostid(postid2);

                dataRefrence.child("mypostswithuser").child(auth.getCurrentUser().getUid()).push().setValue(model);
                if(model.getTypeOfExchange().equals("Y"))
                    dataRefrence.child("NGOposts").child(postid2).setValue(model);
                else
                    dataRefrence.child("nonNGOposts").child(postid2).setValue(model);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddedItemDetailFilling_3.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}