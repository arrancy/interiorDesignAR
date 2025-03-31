package com.example.modernfurniture;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Add_products extends AppCompatActivity {

    ImageView upload;
    Button btnchoose, btnAdd;
    EditText name, price, type;
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    private FirebaseFirestore db;
    private Uri imagedata;
    StorageReference folder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_products);

        db = FirebaseFirestore.getInstance();
        folder = FirebaseStorage.getInstance().getReference().child("image");

        // Views
        upload = findViewById(R.id.imageupload);
        btnchoose = findViewById(R.id.btnchoose);
        btnAdd = findViewById(R.id.btnAdd);
        name = findViewById(R.id.name);
        price = findViewById(R.id.price);
        type = findViewById(R.id.type);

        BottomNavigationView bottomNavigationView = findViewById(R.id.Anavigation);
        bottomNavigationView.setSelectedItemId(R.id.addP);

        // Navigation bar using if-else instead of switch-case
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.addP) {
                    return true;
                } else if (id == R.id.order) {
                    startActivity(new Intent(getApplicationContext(), Admin_orderReport.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.log) {
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.ureport) {
                    startActivity(new Intent(getApplicationContext(), Admin_userReport.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name1 = name.getText().toString().trim();
                String price1 = price.getText().toString().trim();
                String type1 = type.getText().toString().trim();

                if (name1.equals("") || price1.equals("") || type1.equals("") || imagedata == null) {
                    Toast.makeText(Add_products.this, "Enter all fields", Toast.LENGTH_SHORT).show();
                } else {
                    StorageReference imagename = folder.child("image" + imagedata.getLastPathSegment());
                    // Adding to cloud storage
                    imagename.putFile(imagedata).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Converting to uri
                            imagename.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl1 = String.valueOf(uri);
                                    CollectionReference dbProducts = db.collection("products");
                                    Products product = new Products(
                                            name1,
                                            type1,
                                            imageUrl1,
                                            Double.parseDouble(price1)
                                    );

                                    dbProducts.add(product)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Toast.makeText(Add_products.this, "Product Added", Toast.LENGTH_LONG).show();
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(Add_products.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }
                            });
                        }
                    });
                }
            }
        });

        // Handle button click for image selection
        btnchoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check runtime permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13
                    if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_DENIED) {
                        requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_CODE);
                    } else {
                        pickImageFromGallery();
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android 6 to 12
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
                    } else {
                        pickImageFromGallery();
                    }
                } else {
                    // System OS is less than Marshmallow
                    pickImageFromGallery();
                }
            }
        });
    }

    private void pickImageFromGallery() {
        // Intent to pick image
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    // Handle result of runtime permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                pickImageFromGallery();
            } else {
                // Denied
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Handle result of picked image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            // Set image to image view
            upload.setImageURI(data.getData());
            imagedata = data.getData();
        }
    }
}
