package com.example.modernfurniture;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Summary extends AppCompatActivity {

    public RecyclerView mRecycleView;
    public RecyclerView.LayoutManager mManager;
    FirebaseAuth auth;
    private FirebaseFirestore db;
    ArrayList<getCartData> list = new ArrayList<>();
    public SummaryAdapter mAdapter;
    ImageView back;
    String orderId;
    private static final String TAG = "SummaryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        mRecycleView = findViewById(R.id.recyclerViewO);
        mRecycleView.setHasFixedSize(true);
        mManager = new LinearLayoutManager(this);
        mAdapter = new SummaryAdapter(list);
        mRecycleView.setLayoutManager(mManager);
        mRecycleView.setAdapter(mAdapter);

        Intent i = getIntent();
        orderId = i.getStringExtra("SSdetails");
        Log.d(TAG,"Value is: " +orderId);

        back = findViewById(R.id.Sback);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Admin_orderReport.class));
            }
        });

        // Check if orderId is null before proceeding
        if (orderId == null || orderId.isEmpty()) {
            Log.e(TAG, "orderId is null or empty.");
            Toast.makeText(this, "Error: Order ID is missing.", Toast.LENGTH_SHORT).show();
            // Optionally, you might want to finish() the activity here
            // finish();
            return;
        }

        // Fetch order details from Firestore
        try {
            db.collection("Admin_Orders").document(orderId).collection("Ordered_products").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> ulist = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot d : ulist) {
                                    getCartData p = d.toObject(getCartData.class);
                                    list.add(p);
                                }
                                mAdapter.notifyDataSetChanged();
                            } else {
                                Log.d(TAG, "No documents found in Ordered_products for orderId: " + orderId);
                                Toast.makeText(Summary.this, "No ordered products found.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting documents in Ordered_products for orderId: " + orderId, e);
                        Toast.makeText(Summary.this, "Failed to retrieve order data", Toast.LENGTH_SHORT).show();
                    });
        } catch (NullPointerException e){
            Log.e(TAG, "Document reference failed, order id may be invalid.", e);
            Toast.makeText(Summary.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
        }
    }
}