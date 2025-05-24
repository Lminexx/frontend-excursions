package com.example.projectexcursions.utilies;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface.OnCancelListener;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.projectexcursions.R;

public final class CustomProgressBar {

    private Dialog dialog;

    public Dialog show(Context context) {
        return show(context, null);
    }

    public Dialog show(Context context, CharSequence title) {
        return show(context, title, false);
    }

    public Dialog show(Context context, CharSequence title, boolean cancelable) {
        return show(context, title, cancelable, null);
    }

    public Dialog show(Context context, CharSequence title, boolean cancelable,
                       OnCancelListener cancelListener) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.custom_progress_bar, null);
        if(title != null) {
            final TextView tv = view.findViewById(R.id.id_title);
            tv.setText(title);
        }

        dialog = new Dialog(context, R.style.NewDialog);
        dialog.setContentView(view);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.show();

        return dialog;
    }

    @SuppressLint("SetTextI18n")
    public void setProgressDialog(Context context, String message) {
        int padding = 30;
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding, padding, padding, padding);
        ll.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;

        ProgressBar pb = new ProgressBar(context);
        pb.setIndeterminate(true);
        pb.setPadding(0, 0, padding, 0);
        pb.setLayoutParams(params);

        TextView tv = new TextView(context);
        tv.setText(message);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(20f);
        tv.setLayoutParams(params);

        ll.addView(pb);
        ll.addView(tv);

        dialog = new Dialog(context, R.style.NewDialog);
        dialog.setContentView(ll);
        dialog.setCancelable(true);
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            );
        }
    }

    public Dialog getDialog() {
        return dialog;
    }
}