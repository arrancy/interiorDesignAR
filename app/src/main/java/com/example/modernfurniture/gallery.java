package com.example.modernfurniture;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class gallery extends AppCompatActivity {

    public RecyclerView mRecycleView;
    public GalleryAdapter mAdapter;
    public RecyclerView.LayoutManager mManager;
    StorageReference folder;
    ArrayList<Products> list = new ArrayList<>();
    private FirebaseFirestore db;
    EditText search;
    CharSequence searchC = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setSelectedItemId(R.id.gallery);

        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.wishlist) {
                startActivity(new Intent(getApplicationContext(), wishlist.class));
            } else if (id == R.id.camera) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            } else if (id == R.id.cart) {
                startActivity(new Intent(getApplicationContext(), cart.class));
            } else if (id == R.id.profile) {
                startActivity(new Intent(getApplicationContext(), profile.class));
            } else if (id == R.id.gallery) {
                return true;
            }
            overridePendingTransition(0, 0);
            return false;
        });

        mRecycleView = findViewById(R.id.recyclerView3);
        mRecycleView.setHasFixedSize(true);
        mManager = new LinearLayoutManager(this);
        mAdapter = new GalleryAdapter(list);
        mRecycleView.setLayoutManager(mManager);
        mRecycleView.setAdapter(mAdapter);
        search = findViewById(R.id.Gsearch);

        db = FirebaseFirestore.getInstance();
        folder = FirebaseStorage.getInstance().getReference().child("image");

        db.collection("products").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> clist = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot d : clist) {
                            Products p = d.toObject(Products.class);
                            list.add(p);
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                });

        // Search functionality
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mAdapter.getFilter().filter(s);
                searchC = s;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}
