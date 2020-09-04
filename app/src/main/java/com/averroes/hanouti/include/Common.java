package com.averroes.hanouti.include;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.averroes.hanouti.R;
import com.squareup.picasso.Picasso;

public class Common extends AppCompatActivity {

    public static void checkDecimalInput(String text, String message, Context context){
        if(TextUtils.isEmpty(text) ||  TextUtils.isDigitsOnly(text)){
            try {
                Integer.parseInt(text);
                if(Integer.parseInt(text) <= 0){
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    return;
                }
            }
            catch(NumberFormatException e){
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    public static void loadImage(String url, int placeHolderID, ImageView imageView){

        try{
            Picasso.get().load(url).placeholder(placeHolderID).into(imageView);
        }catch(Exception e){
            imageView.setImageResource(placeHolderID);
        }

    }

}
