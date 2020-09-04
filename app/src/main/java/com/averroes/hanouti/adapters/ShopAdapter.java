package com.averroes.hanouti.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.averroes.hanouti.R;
import com.averroes.hanouti.modals.Product;
import com.averroes.hanouti.modals.Shop;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopHolder> {

    private Context context;
    ArrayList<Shop> shopsList;

    public ShopAdapter(Context context, ArrayList<Shop> shopsList) {
        this.context = context;
        this.shopsList = shopsList;
    }

    @NonNull
    @Override
    public ShopHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ShopAdapter.ShopHolder(LayoutInflater.from(context).inflate(R.layout.shop_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ShopHolder holder, int position) {
        final Shop shop = shopsList.get(position);

        try{
            Picasso.get().load(shop.getProfile_image()).placeholder(R.drawable.ic_store_grey).into(holder.shopLogoIV);
        }catch(Exception e){
            holder.shopLogoIV.setImageResource(R.drawable.ic_store_grey);
        }

        holder.shopNameTV.setText(shop.getShop_name());
        holder.shopPhoneTV.setText(shop.getPhone());
        holder.shopAddressTV.setText(shop.getAddress());

        if(shop.getOnline().equals("true")){
            holder.onlineIV.setVisibility(View.VISIBLE);
        }
        else{
            holder.onlineIV.setVisibility(View.GONE);
        }

        if(shop.getShop_open().equals("true")){
            holder.closedTV.setVisibility(View.GONE);
        }
        else{
            holder.onlineIV.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return shopsList.size();
    }

    class ShopHolder extends RecyclerView.ViewHolder{

        private ImageView onlineIV, shopLogoIV;
        private TextView closedTV, shopNameTV, shopPhoneTV, shopAddressTV;
        private RatingBar ratingRB;

        public ShopHolder(@NonNull View itemView) {
            super(itemView);

            onlineIV = itemView.findViewById(R.id.onlineIV);
            shopLogoIV = itemView.findViewById(R.id.shopLogoIV);
            closedTV = itemView.findViewById(R.id.closedTV);
            shopNameTV = itemView.findViewById(R.id.shopNameTV);
            shopPhoneTV = itemView.findViewById(R.id.shopPhoneTV);
            shopAddressTV = itemView.findViewById(R.id.shopAddressTV);
            ratingRB = itemView.findViewById(R.id.ratingRB);

        }
    }
}
