package com.example.modernfurniture;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class wishlist extends AppCompatActivity {

    public RecyclerView mRecycleView;
    public WishlistAdapter mAdapter;
    public RecyclerView.LayoutManager mManager;
    StorageReference folder;
    ArrayList<getWishlistData> list = new ArrayList<>();
    private FirebaseFirestore db;
    FirebaseAuth auth;
    ImageView btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setSelectedItemId(R.id.wishlist);

        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.gallery) {
                startActivity(new Intent(getApplicationContext(), gallery.class));
            } else if (id == R.id.camera) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            } else if (id == R.id.cart) {
                startActivity(new Intent(getApplicationContext(), cart.class));
            } else if (id == R.id.profile) {
                startActivity(new Intent(getApplicationContext(), profile.class));
            } else if (id == R.id.wishlist) {
                return true;
            }
            overridePendingTransition(0, 0);
            return false;
        });

        btn = findViewById(R.id.Wdelete);

        mRecycleView = findViewById(R.id.recyclerViewW);
        mRecycleView.setHasFixedSize(true);
        mManager = new LinearLayoutManager(this);
        mAdapter = new WishlistAdapter(list);
        mRecycleView.setLayoutManager(mManager);
        mRecycleView.setAdapter(mAdapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        folder = FirebaseStorage.getInstance().getReference().child("image");

        // Delete product
        mAdapter.setOnItemClickListener(position -> {
            deleteProduct(position);
            DeletePosition(position);
        });

        db.collection("Wishlist").document(auth.getCurrentUser().getUid())
                .collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> clist = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot d : clist) {
                            getWishlistData p = d.toObject(getWishlistData.class);
                            list.add(p);
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }

    public void DeletePosition(int position) {
        list.remove(position);
        mAdapter.notifyItemRemoved(position);
    }

    private void deleteProduct(int position) {
        db.collection("Wishlist").document(auth.getCurrentUser().getUid())
                .collection("users").document(list.get(position).getId()).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(wishlist.this, "Product deleted", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
