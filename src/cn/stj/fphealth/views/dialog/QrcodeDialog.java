/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.stj.fphealth.views.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.util.FileUtil;
import cn.stj.fphealth.views.dialog.DeviceBindDialog.OnDeviceBindDialogClickListener;
import cn.stj.fphealth.R;

import java.io.File;

/**
 * SPRD:
 *
 * @{
 */
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

/**
 * The dialog fragment for show qrcode by hhj@20160816
 */
public class QrcodeDialog extends DialogFragment {

    private ImageView mQrCodeImageView;
    private OnQrcodeDialogClickListener mListener;
    private Bitmap mQrcodeBitmap;

    public QrcodeDialog(Bitmap qrcodeBitmap) {
        mQrcodeBitmap = qrcodeBitmap;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnQrcodeDialogClickListener) activity;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setStyle(STYLE_NO_TITLE, 0);
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_qrcode_dialog,
                null);
        mQrCodeImageView = (ImageView) view.findViewById(R.id.iv_qrcode);
        mQrCodeImageView.setImageBitmap(mQrcodeBitmap);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(view);
        return builder.create();
    }
    
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mListener != null) {
            mListener.onQrcodeDialogClick();
            mListener = null;
        }
    }

    public interface OnQrcodeDialogClickListener {
        void onQrcodeDialogClick();
    }

    /**
     * Set the dialog edit text and other attribute
     */
    @Override
    public void onResume() {
        super.onResume();

        Dialog dialog = getDialog();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        dialog.setCancelable(false);
        dialog.setOnKeyListener(new QrcodeDialogKeyListener());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    private class QrcodeDialogKeyListener implements DialogInterface.OnKeyListener {

        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
            {
                dismissAllowingStateLoss();
                return true;
            }
            return false;
        }

    }

}
