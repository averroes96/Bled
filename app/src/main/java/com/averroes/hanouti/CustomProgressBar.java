package com.averroes.hanouti;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class CustomProgressBar {

    private Activity activity;
    private AlertDialog alertDialog;

    public CustomProgressBar(Activity activity) {
        this.activity = activity;
    }

    public void start(){

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog, null));
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.show();
    }

    public void dismiss(){

        alertDialog.dismiss();

    }
}
