package com.alha_app.shoppingmemo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

// 全削除時の確認用ダイアログ
public class CheckDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("警告")
                .setMessage(getString(R.string.checkmessage))
                .setPositiveButton("削除", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RemoveAll();
                    }
                })
                .setNegativeButton("キャンセル", null);
        return builder.create();
    }

    // メインレイアウトから全てを削除
    private void RemoveAll(){
        EditActivity editActivity = (EditActivity) getActivity();
        if(editActivity != null){
            editActivity.removeAll();
            editActivity.CreateViews();
        }
    }
}
