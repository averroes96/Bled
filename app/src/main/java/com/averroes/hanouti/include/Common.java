package com.averroes.hanouti.include;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

}
