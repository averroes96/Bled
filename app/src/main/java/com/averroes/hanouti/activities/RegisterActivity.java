package com.averroes.hanouti.activities;

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
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.averroes.hanouti.include.CameraPermissionMethods;
import com.averroes.hanouti.include.LocationPermissionMethods;
import com.averroes.hanouti.R;
import com.averroes.hanouti.include.StoragePermissionMethods;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity implements LocationListener, LocationPermissionMethods, CameraPermissionMethods, StoragePermissionMethods {

    private ImageView profilePic;
    private EditText fullname,phone,dayra,baladiya,fullAddress,email,password,confirmPass;

    private Uri imageUri;

    private String[] locationPerm;
    private String[] cameraPerm;
    private String[] storagePerm;
    private double latitude=0.0,longitude=0.0;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    String fullnameText, phoneText, dayraText, baladiyaText, addressText, emailText,passwordText,confirmText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ImageButton backBtn = findViewById(R.id.registerBack);
        ImageButton gpsBtn = findViewById(R.id.gpsBtn);
        profilePic = findViewById(R.id.profileRegister);
        fullname = findViewById(R.id.nameInput);
        phone = findViewById(R.id.phoneInput);
        dayra = findViewById(R.id.dayraInput);
        baladiya = findViewById(R.id.baladiyaInput);
        fullAddress = findViewById(R.id.fullAddress);
        email = findViewById(R.id.emailRegister);
        password = findViewById(R.id.passwordInput);
        confirmPass = findViewById(R.id.confirmPassword);
        TextView registerSeller = findViewById(R.id.sellerRegister);
        Button registerBtn = findViewById(R.id.registerBtn);

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

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData();
            }
        });
        registerSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, RegisterSellerActivity.class));
            }
        });

    }

    private void inputData() {

        fullnameText = fullname.getText().toString().trim();
        phoneText = phone.getText().toString().trim();
        dayraText = dayra.getText().toString().trim();
        baladiyaText = baladiya.getText().toString().trim();
        addressText = fullAddress.getText().toString().trim();
        emailText = email.getText().toString().trim();
        passwordText = password.getText().toString().trim();
        confirmText = confirmPass.getText().toString().trim();

        if(TextUtils.isEmpty(fullnameText)){
            Toast.makeText(this, getString(R.string.enter_valid_name), Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(phoneText)){
            Toast.makeText(this, getString(R.string.enter_valid_phone), Toast.LENGTH_LONG).show();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()){
            Toast.makeText(this, getString(R.string.enter_valid_email), Toast.LENGTH_LONG).show();
            return;
        }
        if(passwordText.length() < 6){
            Toast.makeText(this, getString(R.string.short_password), Toast.LENGTH_LONG).show();
            return;
        }
        if(!passwordText.equals(confirmText)){
            Toast.makeText(this, getString(R.string.unmatched_passwords), Toast.LENGTH_LONG).show();
            return;
        }

        createAccount();


    }

    private void createAccount() {

        progressDialog.setMessage(getString(R.string.creating_account));
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        saveFireBaseData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                });


    }

    private void saveFireBaseData(){

        progressDialog.setMessage(getString(R.string.saving_info));

        String timestamp = "" + System.currentTimeMillis();
        final HashMap<String, Object> data = new HashMap<>();

        data.put("uid", firebaseAuth.getUid());
        data.put("fullname", fullnameText);
        data.put("phone", phoneText);
        data.put("email", emailText);
        data.put("dayra", dayraText);
        data.put("baladiya", baladiyaText);
        data.put("address", addressText);
        data.put("password", passwordText);
        data.put("account_type", "user");
        data.put("timestamp", timestamp);
        data.put("online", "true");

        if(imageUri == null){

            data.put("profile_image", "");

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
            reference.child(firebaseAuth.getUid()).setValue(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterActivity.this, MainUserActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterActivity.this, MainUserActivity.class));
                            finish();
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
                                reference.child(firebaseAuth.getUid()).setValue(data)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterActivity.this, MainUserActivity.class));
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterActivity.this, MainUserActivity.class));
                                                finish();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegisterActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        }


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

    public boolean checkLocationPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkStoragePermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkCameraPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission(){
        ActivityCompat.requestPermissions(this, locationPerm, LOCATION_REQUEST);
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

    public void detectLocation() {

        Toast.makeText(this, getString(R.string.wait), Toast.LENGTH_LONG).show();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        catch(SecurityException e){
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG).show();
            }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();
    }

    public void findAddress() {
        Geocoder geocoder;
        List<Address> addrs;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addrs = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addrs.get(0).getAddressLine(0);
            String dayraLoc = addrs.get(0).getLocality();
            String baladiyaLoc = addrs.get(0).getSubLocality();

            dayra.setText(dayraLoc);
            baladiya.setText(baladiyaLoc);
            fullAddress.setText(address);

        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
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