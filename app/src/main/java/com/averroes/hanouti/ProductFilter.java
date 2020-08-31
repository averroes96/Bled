package com.averroes.hanouti;

import android.widget.Filter;

import java.util.ArrayList;

public class ProductFilter extends Filter {

    private ProductAdapter adapter;
    private ArrayList<Product> filteredProducts;

    public ProductFilter(ProductAdapter adapter, ArrayList<Product> products) {
        this.adapter = adapter;
        this.filteredProducts = products;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {

        FilterResults results = new FilterResults();

        if(charSequence != null && charSequence.length() > 0){
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<Product> filtered = new ArrayList<>();
            for(Product product : filteredProducts){

                if(product.getTitle().toUpperCase().contains(charSequence) || product.getCategory().toUpperCase().contains(charSequence)){
                    filtered.add(product);
                }
            }
            results.count = filtered.size();
            results.values = filtered;
        }
        else{
            results.count = filteredProducts.size();
            results.values = filteredProducts;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        adapter.products = (ArrayList<Product>)filterResults.values;
        adapter.notifyDataSetChanged();
    }
}
