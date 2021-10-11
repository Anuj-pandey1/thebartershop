package com.krishna.team_olive;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class FragmentHome extends Fragment {

    int SEARCH_RETURN = 01;
    private RecyclerView recyclerView_posts;
    private PostAdapter postAdapter, postAdapter_search;
    public static ArrayList<AddedItemDescriptionModel> addedItemDescriptionModelArrayList, addedItemDescriptionModelArrayList_search;
    public ArrayList<String> arrayListString;
    FirebaseDatabase firebaseDatabase ;
    RelativeLayout search_bar;
    String isNGO;
    searchselected activity;

    public interface searchselected{
        void onsearchselected();
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity=(FragmentHome.searchselected) context;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView_posts = view.findViewById(R.id.recyclerview_posts);
        recyclerView_posts.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        recyclerView_posts.setLayoutManager(linearLayoutManager);
        addedItemDescriptionModelArrayList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(),addedItemDescriptionModelArrayList);
        postAdapter_search = new PostAdapter(getContext(),addedItemDescriptionModelArrayList_search);
        recyclerView_posts.setAdapter(postAdapter);

        search_bar = (RelativeLayout) view.findViewById(R.id.rl_search_bar);

        FirebaseDatabase.getInstance().getReference().child("users").child("isNGO").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    isNGO = snapshot.getKey();
                }
            }
            @Override
            public void onCancelled( DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        if(isNGO == "Y"){
            showNGOposts();
        }else{
            showNormalPosts();
        }

        search_bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onsearchselected();
            }
        });
        for(int i=0;i<addedItemDescriptionModelArrayList.size();i++){
            arrayListString.add(addedItemDescriptionModelArrayList.get(i).getName());
        }

        return view;
    }

    private void showNGOposts() {
        FirebaseDatabase.getInstance().getReference().child("NGOposts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                addedItemDescriptionModelArrayList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    AddedItemDescriptionModel object = snapshot.getValue(AddedItemDescriptionModel.class);
                    addedItemDescriptionModelArrayList.add(object);
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void showNormalPosts(){
        FirebaseDatabase.getInstance().getReference().child("nonNGOposts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                addedItemDescriptionModelArrayList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    AddedItemDescriptionModel object = snapshot.getValue(AddedItemDescriptionModel.class);
                    addedItemDescriptionModelArrayList.add(object);
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == SEARCH_RETURN && data != null){
            addedItemDescriptionModelArrayList_search.clear();
            for(int i=0;i<addedItemDescriptionModelArrayList.size();i++){
                if(data.getStringExtra("postname")==addedItemDescriptionModelArrayList.get(i).getName()){
                    addedItemDescriptionModelArrayList_search.add(addedItemDescriptionModelArrayList.get(i));
                }
            }
            recyclerView_posts.setAdapter(postAdapter_search);
        }
    }
}