package com.averroes.hanouti.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.averroes.hanouti.include.CameraPermissionMethods;
import com.averroes.hanouti.R;
import com.averroes.hanouti.include.StoragePermissionMethods;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import static com.averroes.hanouti.include.Common.checkDecimalInput;

public class EditProductActivity extends AppCompatActivity implements CameraPermissionMethods, StoragePermissionMethods {

    private String prodId;

    private ImageButton back;
    private ImageView profilePicture;
    private EditText title,description;
    private TextView category, quantity,price,discountedPrice,discountedNote;
    private SwitchCompat discount;
    private Button editProduct;

    private Uri imageUri;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private String[] cameraPerm;
    private String[] storagePerm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        back = findViewById(R.id.backBtn);
        profilePicture = findViewById(R.id.productPictureIV);
        title = findViewById(R.id.titleET);
        description = findViewById(R.id.desctiptioET);
        category = findViewById(R.id.categoryET);
        quantity = findViewById(R.id.quantityET);
        price = findViewById(R.id.priceET);
        discountedPrice = findViewById(R.id.discountPriceET);
        discountedNote = findViewById(R.id.discountNoteET);
        discount = findViewById(R.id.discountSW);
        editProduct = findViewById(R.id.editProductBtn);

        prodId = getIntent().getStringExtra("id");

        discountedPrice.setVisibility(View.GONE);
        discountedNote.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        loadDetails();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.wait);
        progressDialog.setCanceledOnTouchOutside(false);

        cameraPerm = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        storagePerm = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });

        editProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

        category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryDialog();
            }
        });

        discount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    discountedPrice.setVisibility(View.VISIBLE);
                    discountedNote.setVisibility(View.VISIBLE);
                }
                else{
                    discountedPrice.setVisibility(View.GONE);
                    discountedNote.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadDetails() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(firebaseAuth.getUid()).child("products").child(prodId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean discountBool = (boolean) snapshot.child("discount_available").getValue();
                        if(discountBool){
                            discount.setChecked(true);
                            discountedPrice.setVisibility(View.VISIBLE);
                            discountedNote.setVisibility(View.VISIBLE);
                        }
                        else{
                            discount.setChecked(false);
                            discountedPrice.setVisibility(View.GONE);
                            discountedNote.setVisibility(View.GONE);
                        }

                        title.setText(snapshot.child("title").getValue().toString());
                        price.setText(snapshot.child("price").getValue().toString());
                        description.setText(snapshot.child("description").getValue().toString());
                        category.setText(snapshot.child("category").getValue().toString());
                        quantity.setText(snapshot.child("quantity").getValue().toString());
                        discountedPrice.setText(snapshot.child("discount_price").getValue().toString());
                        discountedNote.setText(snapshot.child("discount_note").getValue().toString());

                        try{
                            Picasso.get().load(snapshot.child("product_icon").getValue().toString()).placeholder(R.drawable.ic_shopping_teal).into(profilePicture);
                        }catch(Exception e){
                            profilePicture.setImageResource(R.drawable.ic_shopping_teal);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String productTitle, productDesc, productCat, productQte, productPrice, discountPrice, discountNote;
    private boolean disc = false;

    private void validateData() {

        productTitle = title.getText().toString().trim();
        productDesc = description.getText().toString().trim();
        productCat = category.getText().toString().trim();
        productQte = quantity.getText().toString().trim();
        productPrice = price.getText().toString().trim();
        disc = discount.isChecked();

        if(TextUtils.isEmpty(productTitle) || productTitle.length() > 30){
            Toast.makeText(this, getString(R.string.enter_title), Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(productDesc) || productDesc.length() < 10 || productDesc.length() > 500){
            Toast.makeText(this, getString(R.string.enter_desc), Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(productCat) || productTitle.length() > 50){
            Toast.makeText(this, getString(R.string.select_cat), Toast.LENGTH_LONG).show();
            return;
        }

        checkDecimalInput(productQte, getString(R.string.enter_valid_qte), this);
        checkDecimalInput(productPrice, getString(R.string.enter_valid_price), this);

        if(disc == true){

            discountPrice = discountedPrice.getText().toString().trim();
            discountNote = discountedNote.getText().toString().trim();

            checkDecimalInput(discountPrice, getString(R.string.enter_valid_price_discount), this);

            if(Integer.parseInt(productPrice) <= Integer.parseInt(discountPrice)){
                Toast.makeText(this, getString(R.string.valid_discount_price), Toast.LENGTH_LONG).show();
                return;
            }
        }
        else{
            discountPrice = "0";
            discountNote = "";
        }

        saveProduct();
    }

    private void saveProduct() {

        progressDialog.setMessage(getString(R.string.updating_product));
        progressDialog.show();

        final HashMap<String, Object> data = new HashMap<>();

        data.put("title", productTitle);
        data.put("description", productDesc);
        data.put("category", productCat);
        data.put("quantity", productQte);
        data.put("price", productPrice);
        data.put("discount_price", discountPrice);
        data.put("discount_note", discountNote);
        data.put("discount_available", disc);
        data.put("uid", firebaseAuth.getUid());

        if(imageUri == null){

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
            reference.child(firebaseAuth.getUid()).child("products").child(prodId).updateChildren(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, getString(R.string.product_updated), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        }
        else{

            String filePathAndName = "product_images/" + prodId;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uriTask.isSuccessful());
                            Uri downloadUri = uriTask.getResult();

                            if(uriTask.isSuccessful()){
                                data.put("product_icon", downloadUri.toString());

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                                reference.child(firebaseAuth.getUid()).child("products").child(prodId).updateChildren(data)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProductActivity.this, getString(R.string.product_updated), Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProductActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        }
    }

    private void categoryDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.product_category))
                .setItems(R.array.product_categories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String[] array = getResources().getStringArray(R.array.product_categories);
                        category.setText(array[i]);
                    }
                })
                .create().show();
    }

    public void showImagePickDialog() {
        String[] options = { getString(R.string.camera), getString(R.string.gallery)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pick_image_intent_chooser_title)
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i == 0){
                            if(checkCameraPermission()){
                                pickFromCamera();
                            }else{
                                requestCameraPermission();
                            }
                        }else{
                            if(checkStoragePermission()){
                                pickFromGallery();
                            }
                            else{
                                requestStoragePermission();
                            }
                        }
                    }
                })
                .show();
    }

    public boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void pickFromCamera(){

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "temp_image title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "temp_image description");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA);

    }

    public void pickFromGallery(){

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY);

    }

    @Override
    public boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePerm, STORAGE_REQUEST);
    }

    public void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPerm, CAMERA_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CAMERA_REQUEST : {
                if(grantResults.length > 0){
                    boolean cameraResult = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageResult = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraResult && storageResult){
                        pickFromCamera();
                    }else{
                        Toast.makeText(this, getString(R.string.camera_required), Toast.LENGTH_LONG).show();
                    }
                }
            } break;
            case STORAGE_REQUEST : {
                if(grantResults.length > 0){
                    boolean locationResult = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(locationResult){
                        pickFromGallery();
                    }else{
                        Toast.makeText(this, getString(R.string.storage_required), Toast.LENGTH_LONG).show();
                    }
                }
            } break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_CAMERA){
                profilePicture.setImageURI(imageUri);
            }
            else if(requestCode == IMAGE_PICK_GALLERY){
                imageUri = data.getData();
                profilePicture.setImageURI(imageUri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}