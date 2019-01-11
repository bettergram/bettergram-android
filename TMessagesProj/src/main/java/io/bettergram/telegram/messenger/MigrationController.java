package io.bettergram.telegram.messenger;

import android.app.Activity;
import android.content.SharedPreferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.bettergram.Global;
import io.bettergram.data.StoredDialog;
import io.bettergram.data.StoredDialogList;
import io.bettergram.data.StoredDialogList__JsonHelper;
import io.bettergram.telegram.tgnet.TLRPC;

import static android.text.TextUtils.isEmpty;

public class MigrationController {

    private static MigrationController instance = null;
    private SharedPreferences dialogsPreferences;

    public static MigrationController getInstance() {
        if (instance == null) {
            synchronized (MigrationController.class) {
                instance = new MigrationController();
            }
        }
        return instance;
    }

    private MigrationController() {
        dialogsPreferences = ApplicationLoader.applicationContext.getSharedPreferences("local_dialogs_" + Global.getInstance().userId, Activity.MODE_PRIVATE);
    }

    public boolean isFirstRun() {
        return dialogsPreferences.getBoolean("first_run", true);
    }

    public void toggleFirstRun() {
        dialogsPreferences.edit().putBoolean("first_run", false).apply();
    }

    public void resetFirstRun() {
        dialogsPreferences.edit().putBoolean("first_run", true).apply();
    }

    public void storePinnedDialog(TLRPC.TL_dialog d) {
        StoredDialogList storedDialogList = new StoredDialogList();
        storedDialogList.dialogs = restorePinnedDialogList();
        for (int i = 0; i < storedDialogList.dialogs.size(); i++) {
            if (storedDialogList.dialogs.get(i).did == d.id) {
                storedDialogList.dialogs.remove(i);
                i--;
            }
        }
        StoredDialog sd = new StoredDialog();
        sd.did = d.id;
        sd.pinned_num = d.pinnedNum;
        storedDialogList.dialogs.add(sd);
        try {
            String json = StoredDialogList__JsonHelper.serializeToJson(storedDialogList);
            SharedPreferences.Editor editor = dialogsPreferences.edit();
            editor.putString("stored_pinned_dialog_list", json);
            editor.apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int restorePinnedNum(TLRPC.TL_dialog d) {
        StoredDialogList storedDialogList = new StoredDialogList();
        storedDialogList.dialogs = restorePinnedDialogList();
        for (int i = 0; i < storedDialogList.dialogs.size(); i++) {
            if (storedDialogList.dialogs.get(i).did == d.id) {
                return storedDialogList.dialogs.get(i).pinned_num;
            }
        }
        return 0;
    }

    public void storeFavoriteDialog(final long did, final int favorite_date) {
        StoredDialogList storedDialogList = new StoredDialogList();
        storedDialogList.dialogs = restoreFavoriteDialogList();
        for (int i = 0; i < storedDialogList.dialogs.size(); i++) {
            if (storedDialogList.dialogs.get(i).did == did) {
                storedDialogList.dialogs.remove(i);
                i--;
            }
        }
        StoredDialog sd = new StoredDialog();
        sd.did = did;
        sd.favorited_date = favorite_date;
        storedDialogList.dialogs.add(sd);
        try {
            String json = StoredDialogList__JsonHelper.serializeToJson(storedDialogList);
            SharedPreferences.Editor editor = dialogsPreferences.edit();
            editor.putString("stored_favorite_dialog_list", json);
            editor.apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int restoreFavoriteDate(TLRPC.TL_dialog d) {
        StoredDialogList storedDialogList = new StoredDialogList();
        storedDialogList.dialogs = restoreFavoriteDialogList();
        for (int i = 0; i < storedDialogList.dialogs.size(); i++) {
            if (storedDialogList.dialogs.get(i).did == d.id) {
                return storedDialogList.dialogs.get(i).favorited_date;
            }
        }
        return 0;
    }

    private List<StoredDialog> restorePinnedDialogList() {
        String json = dialogsPreferences.getString("stored_pinned_dialog_list", null);
        if (!isEmpty(json)) {
            try {
                StoredDialogList list = StoredDialogList__JsonHelper.parseFromJson(json);
                if (list != null && list.dialogs != null) {
                    return cleanList(list.dialogs);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    private List<StoredDialog> restoreFavoriteDialogList() {
        String json = dialogsPreferences.getString("stored_favorite_dialog_list", null);
        if (!isEmpty(json)) {
            try {
                StoredDialogList list = StoredDialogList__JsonHelper.parseFromJson(json);
                if (list != null && list.dialogs != null) {
                    return cleanList(list.dialogs);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    private List<StoredDialog> cleanList(List<StoredDialog> dialogs) {
        for (int i = 0; i < dialogs.size(); i++) {
            for (int j = i + 1; j < dialogs.size(); j++) {
                if (dialogs.get(i).did == dialogs.get(j).did) {
                    dialogs.remove(j);
                    j--;
                }
            }
        }
        return dialogs;
    }

    public void migratePinnedDialogs(ArrayList<TLRPC.TL_dialog> dialogs) {
        boolean once = dialogsPreferences.getBoolean("pinned_dialog_migrate_run_once", true);
        if (once) {
            SharedPreferences.Editor editor = dialogsPreferences.edit();
            editor.putBoolean("pinned_dialog_migrate_run_once", false);
            editor.apply();
            StoredDialogList storedDialogList = new StoredDialogList();
            storedDialogList.dialogs = restorePinnedDialogList();
            for (int i = 0, size = dialogs.size(); i < size; i++) {
                TLRPC.TL_dialog dialog = dialogs.get(i);
                final long did = dialog.id;
                final int pinnedNum = dialog.pinnedNum;
                if (pinnedNum > 0) {
                    StoredDialog sd = new StoredDialog();
                    sd.did = did;
                    sd.pinned_num = pinnedNum;
                    storedDialogList.dialogs.add(sd);
                }
            }
            storedDialogList.dialogs = cleanList(storedDialogList.dialogs);
            try {
                String json = StoredDialogList__JsonHelper.serializeToJson(storedDialogList);
                editor.putString("stored_pinned_dialog_list", json);
                editor.apply();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void migrateFavoritedDialogs(ArrayList<TLRPC.TL_dialog> dialogs) {
        boolean once = dialogsPreferences.getBoolean("favorite_dialog_migrate_run_once", true);
        if (once) {
            SharedPreferences.Editor editor = dialogsPreferences.edit();
            editor.putBoolean("favorite_dialog_migrate_run_once", false);
            editor.apply();
            StoredDialogList storedDialogList = new StoredDialogList();
            storedDialogList.dialogs = restoreFavoriteDialogList();
            for (int i = 0, size = dialogs.size(); i < size; i++) {
                TLRPC.TL_dialog dialog = dialogs.get(i);
                final long did = dialog.id;
                final int favorite_date = dialog.favorite_date;
                if (favorite_date > 0) {
                    StoredDialog sd = new StoredDialog();
                    sd.did = did;
                    sd.favorited_date = favorite_date;
                    storedDialogList.dialogs.add(sd);
                }
            }
            storedDialogList.dialogs = cleanList(storedDialogList.dialogs);
            try {
                String json = StoredDialogList__JsonHelper.serializeToJson(storedDialogList);
                editor.putString("stored_favorite_dialog_list", json);
                editor.apply();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void clear() {
        dialogsPreferences.edit().clear().apply();
    }
}
