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
import android.widget.Toast;

import cn.stj.fphealth.app.Constants;
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

/**
 * The dialog fragment for confirm device bind created by hhj@20160816
 */
public class DeviceBindDialog extends DialogFragment {
    private static final String TAG = "DeviceBindDialog";

    private Button mButtonConfirm = null;
    private Button mButtonCancel = null;
    private int mStatus;
    private OnDeviceBindDialogClickListener mListener = null;

    public DeviceBindDialog() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDeviceBindDialogClickListener) activity;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setStyle(STYLE_NO_TITLE, 0);
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_device_bind_dialog,
                null);
        mButtonConfirm = (Button) view.findViewById(R.id.button_confirm);
        mButtonConfirm.setOnClickListener(mButtonOnClickListener);

        mButtonCancel = (Button) view.findViewById(R.id.button_cancel);
        mButtonCancel.setOnClickListener(mButtonOnClickListener);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(view);
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mListener != null) {
            mListener.onDeviceBindDialogClick(mStatus);
            mListener = null;
        }
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
        dialog.setOnKeyListener(new SaveDialogKeyListener());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    private OnClickListener mButtonOnClickListener = new OnClickListener() {
        /**
         * Define the button operation
         */
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.button_confirm:
                    onClickBind();
                    break;

                case R.id.button_cancel:
                    onClickCancle();
                    break;

                default:
                    break;
            }
        }
    };

    /**
     * The listener for click bind or not
     */
    public interface OnDeviceBindDialogClickListener {
        /**
         * device bind confirm dialog click callback
         * 
         * @param status (0:do not bind 1:bind)
         */
        void onDeviceBindDialogClick(int status);
    }

    private void onClickCancle() {
        mStatus = Constants.DEVICE_NOTBIND_STATUS;
        dismissAllowingStateLoss();
    }

    private void onClickBind() {
        mStatus = Constants.DEVICE_BIND_STATUS;
        dismissAllowingStateLoss();
    }

    private class SaveDialogKeyListener implements DialogInterface.OnKeyListener {

        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
            {
                onClickCancle();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0) {
                onClickBind();
                return true;
            }
            return false;
        }

    }

}
