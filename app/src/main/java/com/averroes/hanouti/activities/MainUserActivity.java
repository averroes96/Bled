package com.averroes.hanouti.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.averroes.hanouti.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainUserActivity extends AppCompatActivity {

    private TextView nameTV, emailTV,phoneTV, shopsTV, ordersTV, countProductsTV;
    private ImageView profileIV;
    private ImageButton logoutBtn, editProfileBtn, filterProductsIB;
    private ConstraintLayout shopsTab, ordersTab;
    private EditText searchProductsET;
    private RecyclerView shopsRV;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);


        nameTV = findViewById(R.id.nameTV);
        emailTV = findViewById(R.id.emailTV);
        phoneTV = findViewById(R.id.phoneTV);
        shopsTV = findViewById(R.id.shopsTV);
        ordersTV = findViewById(R.id.ordersTV);
        countProductsTV = findViewById(R.id.countProductsTV);
        logoutBtn = findViewById(R.id.logoutBtn);
        profileIV = findViewById(R.id.profileIV);
        filterProductsIB = findViewById(R.id.filterProductsIB);
        shopsTab = findViewById(R.id.shopsTab);
        ordersTab = findViewById(R.id.ordersTab);
        searchProductsET = findViewById(R.id.searchProductsET);
        shopsRV = findViewById(R.id.shopsRV);
        editProfileBtn = findViewById(R.id.editProfileBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.wait));
        progressDialog.setCanceledOnTouchOutside(false);

        checkUser();
        
        showShopsTab();

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUserOffline();
            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainUserActivity.this, EditUserActivity.class));
            }
        });

        shopsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showShopsTab();
            }
        });

        ordersTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrdersTab();
            }
        });
    }

    private void showShopsTab() {

        shopsTab.setVisibility(View.VISIBLE);
        ordersTab.setVisibility(View.GONE);

        shopsTV.setTextColor(getResources().getColor(R.color.colorBlack));
        shopsTV.setBackgroundResource(R.drawable.shape_selected);

        ordersTV.setTextColor(getResources().getColor(R.color.colorWhite));
        ordersTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showOrdersTab() {

        shopsTab.setVisibility(View.GONE);
        ordersTab.setVisibility(View.VISIBLE);

        ordersTV.setTextColor(getResources().getColor(R.color.colorBlack));
        ordersTV.setBackgroundResource(R.drawable.shape_selected);

        shopsTV.setTextColor(getResources().getColor(R.color.colorWhite));
        shopsTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void checkUser() {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user == null){
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
            finish();
        }
        else{
            loadUserInfo();
        }
    }

    private void setUserOffline() {

        progressDialog.setMessage(getString(R.string.logging_out));

        HashMap<String, Object> data = new HashMap<>();
        data.put("online", "false");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(firebaseAuth.getUid()).updateChildren(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firebaseAuth.signOut();
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainUserActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadUserInfo() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            String fullname = dataSnapshot.child("fullname").getValue().toString();
                            String userType = dataSnapshot.child("account_type").getValue().toString();

                            nameTV.setText(fullname);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}