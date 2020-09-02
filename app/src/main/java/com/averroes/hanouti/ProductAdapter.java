package com.averroes.hanouti;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductHolder> implements Filterable {

    private Context context;
    ArrayList<Product> products;
    ArrayList<Product> filteredProducts;

    ProductFilter filter;

    public ProductAdapter(Context context, ArrayList<Product> products) {
        this.context = context;
        this.products = products;
        this.filteredProducts = products;
    }

    @NonNull
    @Override
    public ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductHolder(LayoutInflater.from(context).inflate(R.layout.product_row_seller, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductHolder holder, int position) {
        final Product product = products.get(position);
        holder.title.setText(product.getTitle());
        holder.price.setText(product.getPrice());
        holder.quantity.setText(product.getQuantity());
        holder.discountPrice.setText(product.getDiscount_price());
        holder.discountNote.setText(product.getDiscount_note());

        if(product.getDiscount_available()){
            holder.discountPrice.setVisibility(View.VISIBLE);
            if(!holder.discountNote.getText().equals(""))
                holder.discountNote.setVisibility(View.VISIBLE);
            else
                holder.discountNote.setVisibility(View.GONE);
            holder.price.setPaintFlags(holder.price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else{
            holder.discountPrice.setVisibility(View.GONE);
            holder.discountNote.setVisibility(View.GONE);
        }

        try{
            Picasso.get().load(product.getProduct_icon()).placeholder(R.drawable.ic_shopping_teal).into(holder.picture);
        }catch(Exception e){
            holder.picture.setImageResource(R.drawable.ic_shopping_teal);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDetails(product);
            }
        });
    }

    private void showDetails(final Product product) {

        final BottomSheetDialog dialog = new BottomSheetDialog(context);

        View view = LayoutInflater.from(context).inflate(R.layout.product_details, null);
        dialog.setContentView(view);

        ImageButton back = view.findViewById(R.id.backBtn);
        ImageButton edit = view.findViewById(R.id.editBtn);
        ImageButton delete = view.findViewById(R.id.deleteBtn);
        ImageView image = view.findViewById(R.id.productPictureIV);
        final TextView title = view.findViewById(R.id.titleTV);
        TextView description = view.findViewById(R.id.descriptionTV);
        TextView category = view.findViewById(R.id.categoryTV);
        TextView quantity = view.findViewById(R.id.quantityTV);
        TextView discountNote = view.findViewById(R.id.discountNoteTV);
        TextView discountPrice = view.findViewById(R.id.discountPriceTV);
        TextView price = view.findViewById(R.id.priceTV);

        title.setText(product.getTitle());
        price.setText(product.getPrice());
        description.setText(product.getDescription());
        category.setText(product.getCategory());
        quantity.setText(product.getQuantity());
        discountPrice.setText(product.getDiscount_price());
        discountNote.setText(product.getDiscount_note());

        if(product.getDiscount_available()){
            discountPrice.setVisibility(View.VISIBLE);
            if(!discountNote.getText().equals(""))
                discountNote.setVisibility(View.VISIBLE);
            else
                discountNote.setVisibility(View.GONE);
            price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else{
            discountPrice.setVisibility(View.GONE);
            discountNote.setVisibility(View.GONE);
        }

        try{
            Picasso.get().load(product.getProduct_icon()).placeholder(R.drawable.ic_shopping_white).into(image);
        }catch(Exception e){
            image.setImageResource(R.drawable.ic_shopping_white);
        }

        dialog.show();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra("id", product.getProduct_id());
                context.startActivity(intent);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.delete_product) + " " + title.getText())
                        .setMessage(context.getString(R.string.delete_message) + " " + title.getText())
                        .setPositiveButton(context.getText(R.string.delete_product), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteProduct(product.getProduct_id());
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                .show();
            }
        });


    }

    private void deleteProduct(String product_id) {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(auth.getUid()).child("products").child(product_id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, context.getString(R.string.product_deleted), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new ProductFilter(this, filteredProducts);
        }
        return filter;
    }

    class ProductHolder extends RecyclerView.ViewHolder{

        ImageView picture,next;
        TextView discountNote,title,quantity,discountPrice,price;

        public ProductHolder(@NonNull View itemView) {
            super(itemView);

            picture = itemView.findViewById(R.id.productPictureIV);
            next = itemView.findViewById(R.id.nextIV);
            discountNote = itemView.findViewById(R.id.discountNoteTV);
            discountPrice = itemView.findViewById(R.id.discountPriceTV);
            title = itemView.findViewById(R.id.titleTV);
            quantity = itemView.findViewById(R.id.quantityTV);
            price = itemView.findViewById(R.id.priceTV);
        }
    }
}
