package com.example.modernfurniture;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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

public class cart extends AppCompatActivity {

    public RecyclerView mRecycleView;
    FirebaseAuth auth;
    public CartAdapter mAdapter;
    public RecyclerView.LayoutManager mManager;
    StorageReference folder;
    TextView total;
    Button btn;
    ArrayList<getCartData> list = new ArrayList<>();
    private FirebaseFirestore db;
    int amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        auth = FirebaseAuth.getInstance();
        btn = findViewById(R.id.btnbuy);

        btn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Payment.class)));

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setSelectedItemId(R.id.cart);
        
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.wishlist) {
                startActivity(new Intent(getApplicationContext(), wishlist.class));
            } else if (id == R.id.camera) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            } else if (id == R.id.gallery) {
                startActivity(new Intent(getApplicationContext(), gallery.class));
            } else if (id == R.id.profile) {
                startActivity(new Intent(getApplicationContext(), profile.class));
            }
            overridePendingTransition(0, 0);
            return true;
        });

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mMessageReceiver, new IntentFilter("MyTotalAmount"));

        mRecycleView = findViewById(R.id.recyclerView);
        total = findViewById(R.id.total);
        mRecycleView.setHasFixedSize(true);
        mManager = new LinearLayoutManager(this);
        mAdapter = new CartAdapter(list);
        mRecycleView.setLayoutManager(mManager);
        mRecycleView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new CartAdapter.OnItemClickListner() {
            @Override
            public void onDeleteClick(int position) {
                deleteProduct(position);
                DeletePosition(position);
            }
        });

        db = FirebaseFirestore.getInstance();
        folder = FirebaseStorage.getInstance().getReference().child("image");

        db.collection("Cart").document(auth.getCurrentUser().getUid())
                .collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> clist = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot d : clist) {
                            getCartData p = d.toObject(getCartData.class);
                            list.add(p);
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int totalBil = intent.getIntExtra("TotalAmount", 0);
            total.setText("Total Amount: " + totalBil + "/-");
        }
    };

    public void DeletePosition(int position) {
        list.remove(position);
    }

    private void deleteProduct(int position) {
        db.collection("Cart").document(auth.getCurrentUser().getUid())
                .collection("users").document(list.get(position).getId()).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(cart.this, "Product deleted", Toast.LENGTH_LONG).show();
                    }
                    mAdapter.notifyItemRemoved(position);
                });
    }
}
