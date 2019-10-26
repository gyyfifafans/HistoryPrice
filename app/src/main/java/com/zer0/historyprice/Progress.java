package com.zer0.historyprice;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class Progress extends Dialog {

    private View view;
    private TextView msg;

    public Progress(Context context) {
        super(context);
        init(context);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(view);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            view = inflater.inflate(R.layout.progress, null);
        }
        msg = view.findViewById(R.id.msg);
    }


    public void setMessage(String message) {
        msg.setText(message);
    }
}
