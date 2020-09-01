package com.averroes.hanouti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainSellerActivity extends AppCompatActivity {

    private ImageButton filterCategory;
    private EditText searchProducts;
    private TextView name, shopName, email, products,orders, counter;
    private ImageView profilePicture;
    private ConstraintLayout productsTab,ordersTab;
    private RecyclerView productsList;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<Product> selectedProducts;
    private ProductAdapter productAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);

        name = findViewById(R.id.nameTV);
        ImageButton logoutBtn = findViewById(R.id.logoutBtn);
        ImageButton editBtn = findViewById(R.id.editProfileBtn);
        shopName = findViewById(R.id.shopNameTV);
        email = findViewById(R.id.emailTV);
        ImageButton addProduct = findViewById(R.id.addProductBtn);
        profilePicture = findViewById(R.id.profileIV);
        products = findViewById(R.id.productsTV);
        orders = findViewById(R.id.ordersTV);
        productsTab = findViewById(R.id.productsTab);
        filterCategory = findViewById(R.id.filterProductsIB);
        searchProducts = findViewById(R.id.searchProductsET);
        productsList = findViewById(R.id.productsListRV);
        ordersTab = findViewById(R.id.ordersTab);
        counter = findViewById(R.id.countProductsTV);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.wait));
        progressDialog.setCanceledOnTouchOutside(false);

        checkUser();
        loadSellerProducts();
        showProductsTab();

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setUserOffline();
            }
        });

        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainSellerActivity.this, AddProductActivity.class));
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainSellerActivity.this, EditSellerActivity.class));
            }
        });

        filterCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryDialog();
            }
        });

        searchProducts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    productAdapter.getFilter().filter(charSequence);

                } catch (Exception e) {
                    Toast.makeText(MainSellerActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        products.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProductsTab();
            }
        });

        orders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrdersTab();
            }
        });
    }

    private void categoryDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.product_category))
                .setItems(R.array.product_categories_all, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String[] array = getResources().getStringArray(R.array.product_categories_all);
                        counter.setText(array[i]);
                        if(i == 0){
                            loadSellerProducts();
                        }
                        else
                            loadFilteredProducts(array[i]);
                    }
                })
                .show();

    }

    private void loadFilteredProducts(final String s) {

        progressDialog.setMessage(getString(R.string.loading_products));
        progressDialog.show();
        selectedProducts = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(firebaseAuth.getUid()).child("products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        selectedProducts.clear();
                        Toast.makeText(MainSellerActivity.this, s, Toast.LENGTH_LONG).show();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            if(s.equals(dataSnapshot.child("category").getValue().toString())){
                                Product product = dataSnapshot.getValue(Product.class);
                                selectedProducts.add(product);
                            }
                        }

                        productAdapter = new ProductAdapter(MainSellerActivity.this, selectedProducts);

                        productsList.setAdapter(productAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadSellerProducts() {

        progressDialog.setMessage(getString(R.string.loading_products));
        progressDialog.show();
        selectedProducts = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(firebaseAuth.getUid()).child("products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        selectedProducts.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Product product = dataSnapshot.getValue(Product.class);
                            selectedProducts.add(product);
                        }

                        productAdapter = new ProductAdapter(MainSellerActivity.this, selectedProducts);

                        productsList.setAdapter(productAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void showOrdersTab() {

        productsTab.setVisibility(View.GONE);
        ordersTab.setVisibility(View.VISIBLE);

        orders.setTextColor(getResources().getColor(R.color.colorBlack));
        orders.setBackgroundResource(R.drawable.shape_selected);

        products.setTextColor(getResources().getColor(R.color.colorWhite));
        products.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }

    private void showProductsTab() {

        productsTab.setVisibility(View.VISIBLE);
        ordersTab.setVisibility(View.GONE);

        products.setTextColor(getResources().getColor(R.color.colorBlack));
        products.setBackgroundResource(R.drawable.shape_selected);

        orders.setTextColor(getResources().getColor(R.color.colorWhite));
        orders.setBackgroundColor(getResources().getColor(android.R.color.transparent));

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
                        Toast.makeText(MainSellerActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkUser() {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user == null){
            startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));
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
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            String fullname = dataSnapshot.child("fullname").getValue().toString();
                            String shopNameText = dataSnapshot.child("shop_name").getValue().toString();
                            String emailText = dataSnapshot.child("email").getValue().toString();
                            String profilePic = dataSnapshot.child("profile_image").getValue().toString();

                            name.setText(fullname);
                            shopName.setText(shopNameText);
                            email.setText(emailText);

                            try {
                                Picasso.get().load(profilePic).placeholder(R.drawable.ic_store_grey).into(profilePicture);

                            }catch( Exception e){
                                profilePicture.setImageResource(R.drawable.ic_store_grey);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


}