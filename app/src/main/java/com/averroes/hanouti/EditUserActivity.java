package com.averroes.hanouti;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class EditUserActivity extends AppCompatActivity implements LocationListener {

    private ImageView profilePic;
    private EditText fullnameET,phoneET,dayraET,baladiyaET,addressET;

    private Uri imageUri;

    private static final int LOCATION_REQUEST = 100;
    private static final int CAMERA_REQUEST = 200;
    private static final int STORAGE_REQUEST = 300;
    private static final int IMAGE_PICK_GALLERY = 400;
    private static final int IMAGE_PICK_CAMERA = 500;


    private String[] locationPerm;
    private String[] cameraPerm;
    private String[] storagePerm;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private double latitude = 0.0,longitude = 0.0;

    private String fullnameText, phoneText, dayraText, baladiyaText, addressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        ImageButton backBtn = findViewById(R.id.backBtn);
        ImageButton gpsBtn = findViewById(R.id.gpsBtn);
        Button updateBtn = findViewById(R.id.updateBtn);
        fullnameET = findViewById(R.id.fullnameET);
        phoneET = findViewById(R.id.phoneET);
        dayraET = findViewById(R.id.dayraET);
        baladiyaET = findViewById(R.id.baladiyaET);
        addressET = findViewById(R.id.addressET);
        profilePic = findViewById(R.id.profilePicture);

        locationPerm = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPerm = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        storagePerm = new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.wait);
        progressDialog.setCanceledOnTouchOutside(false);

        checkUser();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkLocationPermission()){
                    detectLocation();
                }
                else{
                    requestLocationPermission();
                }
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData();
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });
    }

    private void inputData() {

        fullnameText = fullnameET.getText().toString().trim();
        phoneText = phoneET.getText().toString().trim();
        dayraText = dayraET.getText().toString().trim();
        baladiyaText = baladiyaET.getText().toString().trim();
        addressText = addressET.getText().toString().trim();

        if(TextUtils.isEmpty(fullnameText)){
            Toast.makeText(this, getString(R.string.enter_valid_name), Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(phoneText)){
            Toast.makeText(this, getString(R.string.enter_valid_phone), Toast.LENGTH_LONG).show();
            return;
        }

        updateAccount();

    }

    private void updateAccount() {
        progressDialog.setMessage(getString(R.string.updating_profile));
        progressDialog.show();

        final HashMap<String, Object> data = new HashMap<>();

        data.put("uid", firebaseAuth.getUid());
        data.put("fullname", fullnameText);
        data.put("phone", phoneText);
        data.put("dayra", dayraText);
        data.put("baladiya", baladiyaText);
        data.put("address", addressText);
        data.put("account_type", "user");
        data.put("online", "true");

        if(imageUri == null){

            data.put("profile_image", "");

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
            reference.child(firebaseAuth.getUid()).updateChildren(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(EditUserActivity.this, getString(R.string.profile_updated), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditUserActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        }
        else{

            String filePathAndName = "profile_images/" + firebaseAuth.getUid();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uriTask.isSuccessful());
                            Uri downloadUri = uriTask.getResult();

                            if(uriTask.isSuccessful()){
                                data.put("profile_image", downloadUri.toString());

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                                reference.child(firebaseAuth.getUid()).updateChildren(data)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                Toast.makeText(EditUserActivity.this, getString(R.string.profile_updated), Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(EditUserActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();

                                            }
                                        });

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditUserActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }

    }

    private void checkUser() {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user == null){
            startActivity(new Intent(EditUserActivity.this, LoginActivity.class));
            finish();
        }
        else{
            loadUserInfo();
        }
    }

    private void loadUserInfo() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            fullnameET.setText(dataSnapshot.child("fullname").getValue().toString());
                            phoneET.setText(dataSnapshot.child("phone").getValue().toString());
                            addressET.setText(dataSnapshot.child("address").getValue().toString());
                            baladiyaET.setText(dataSnapshot.child("baladiya").getValue().toString());
                            dayraET.setText(dataSnapshot.child("dayra").getValue().toString());

                            String profileImage = dataSnapshot.child("profile_image").getValue().toString();

                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_grey).into(profilePic);

                            }catch( Exception e){
                                profilePic.setImageResource(R.drawable.ic_person_grey);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void detectLocation() {

        Toast.makeText(this, getString(R.string.wait), Toast.LENGTH_LONG).show();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        catch(SecurityException e){
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(this, getString(R.string.enable_location), Toast.LENGTH_LONG).show();
    }

    private void findAddress() {
        Geocoder geocoder;
        List<Address> addrs;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addrs = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addrs.get(0).getAddressLine(0);
            String dayraLoc = addrs.get(0).getLocality();
            String baladiyaLoc = addrs.get(0).getSubLocality();

            dayraET.setText(dayraLoc);
            baladiyaET.setText(baladiyaLoc);
            addressET.setText(address);

        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showImagePickDialog() {
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

    private boolean checkLocationPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkStoragePermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkCameraPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this, locationPerm, LOCATION_REQUEST);
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePerm, STORAGE_REQUEST);
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPerm, CAMERA_REQUEST);
    }

    private void pickFromCamera(){

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "temp_image title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "temp_image description");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA);

    }

    private void pickFromGallery(){

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case LOCATION_REQUEST : {
                if(grantResults.length > 0){
                    boolean locationResult = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(locationResult){
                        detectLocation();
                    }else{
                        Toast.makeText(this, getString(R.string.location_needed), Toast.LENGTH_LONG).show();
                    }
                }
            } break;
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
                profilePic.setImageURI(imageUri);
            }
            else if(requestCode == IMAGE_PICK_GALLERY){
                assert data != null;
                imageUri = data.getData();
                profilePic.setImageURI(imageUri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


}