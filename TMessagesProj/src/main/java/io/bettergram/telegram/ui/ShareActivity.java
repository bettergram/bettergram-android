/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package io.bettergram.telegram.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import io.bettergram.telegram.messenger.AndroidUtilities;
import io.bettergram.telegram.messenger.ApplicationLoader;
import io.bettergram.telegram.messenger.FileLog;
import io.bettergram.telegram.messenger.MessageObject;
import io.bettergram.messenger.R;
import io.bettergram.telegram.messenger.UserConfig;
import io.bettergram.telegram.messenger.Utilities;
import io.bettergram.telegram.tgnet.SerializedData;
import io.bettergram.telegram.tgnet.TLRPC;
import io.bettergram.telegram.ui.Components.ShareAlert;

public class ShareActivity extends Activity {

    private Dialog visibleDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ApplicationLoader.postInitApplication();
        AndroidUtilities.checkDisplaySize(this, getResources().getConfiguration());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTheme(R.style.Theme_TMessages_Transparent);
        super.onCreate(savedInstanceState);
        setContentView(new View(this), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        Intent intent = getIntent();
        if (intent == null || !Intent.ACTION_VIEW.equals(intent.getAction()) || intent.getData() == null) {
            finish();
            return;
        }
        Uri data = intent.getData();
        String scheme = data.getScheme();
        String url = data.toString();
        String hash = data.getQueryParameter("hash");
        if (!"tgb".equals(scheme) || !url.toLowerCase().startsWith("tgb://share_game_score") || TextUtils.isEmpty(hash)) {
            finish();
            return;
        }

        SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("botshare", Activity.MODE_PRIVATE);
        String message = sharedPreferences.getString(hash + "_m", null);
        if (TextUtils.isEmpty(message)) {
            finish();
            return;
        }
        SerializedData serializedData = new SerializedData(Utilities.hexToBytes(message));
        TLRPC.Message mess = TLRPC.Message.TLdeserialize(serializedData, serializedData.readInt32(false), false);
        mess.readAttachPath(serializedData, 0);
        serializedData.cleanup();
        if (mess == null) {
            finish();
            return;
        }
        String link = sharedPreferences.getString(hash + "_link", null);
        MessageObject messageObject = new MessageObject(UserConfig.selectedAccount, mess, false);
        messageObject.messageOwner.with_my_score = true;

        try {
            visibleDialog = ShareAlert.createShareAlert(this, messageObject, null, false, link, false);
            visibleDialog.setCanceledOnTouchOutside(true);
            visibleDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!isFinishing()) {
                        finish();
                    }
                    visibleDialog = null;
                }
            });
            visibleDialog.show();
        } catch (Exception e) {
            FileLog.e(e);
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (visibleDialog != null && visibleDialog.isShowing()) {
                visibleDialog.dismiss();
                visibleDialog = null;
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }
}
