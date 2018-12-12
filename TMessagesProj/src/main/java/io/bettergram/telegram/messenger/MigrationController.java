package io.bettergram.telegram.messenger;

import android.app.Activity;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import io.bettergram.telegram.tgnet.TLRPC;

public class MigrationController {

    private SharedPreferences dialogsPreferences;

    private int currentAccount;
    private static volatile MigrationController[] Instance = new MigrationController[UserConfig.MAX_ACCOUNT_COUNT];

    public static MigrationController getInstance(int num) {
        MigrationController localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (MigrationController.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new MigrationController(num);
                }
            }
        }
        return localInstance;
    }

    private MigrationController(int num) {
        currentAccount = num;
        dialogsPreferences = ApplicationLoader.applicationContext.getSharedPreferences("local_dialogs_" + currentAccount, Activity.MODE_PRIVATE);
    }

    public void storePinnedDialog(TLRPC.TL_dialog d) {
        Set<String> string_set = restorePinnedDialogsStringSet();
        Set<String> string_set_copy = new HashSet<>(string_set);
        string_set_copy.add(String.valueOf(d.id) + "," + String.valueOf(d.pinnedNum));
        SharedPreferences.Editor editor = dialogsPreferences.edit();
        editor.putStringSet("stored_pinned_dialog_set", string_set_copy);
        editor.apply();
    }

    public int restorePinnedNum(TLRPC.TL_dialog d) {
        Set<String> string_set = restorePinnedDialogsStringSet();
        Set<String> string_set_copy = new HashSet<>(string_set);
        for (String s : string_set_copy) {
            String[] parts = s.split(",", 2);
            long did = Long.valueOf(parts[0]);
            int pinnedNum = Integer.valueOf(parts[1]);
            if (d.id == did) {
                return pinnedNum;
            }
        }
        return 0;
    }

    public void storeFavoriteDialog(final long did, final int favorite_date) {
        Set<String> string_set = restoreFavoriteDialogsStringSet();
        Set<String> string_set_copy = new HashSet<>(string_set);
        string_set_copy.add(String.valueOf(did) + "," + String.valueOf(favorite_date));
        SharedPreferences.Editor editor = dialogsPreferences.edit();
        editor.putStringSet("stored_favorite_dialog_set", string_set_copy);
        editor.apply();
    }

    public int restoreFavoriteDate(TLRPC.TL_dialog d) {
        Set<String> string_set = restoreFavoriteDialogsStringSet();
        Set<String> string_set_copy = new HashSet<>(string_set);
        for (String s : string_set_copy) {
            String[] parts = s.split(",", 2);
            long did = Long.valueOf(parts[0]);
            int favorite_date = Integer.valueOf(parts[1]);
            if (d.id == did) {
                return favorite_date;
            }
        }
        return 0;
    }

    private Set<String> restorePinnedDialogsStringSet() {
        return dialogsPreferences.getStringSet("stored_pinned_dialog_set", new HashSet<>());
    }

    private Set<String> restoreFavoriteDialogsStringSet() {
        return dialogsPreferences.getStringSet("stored_favorite_dialog_set", new HashSet<>());
    }

    public void migratePinnedDialogs(ArrayList<TLRPC.TL_dialog> dialogs) {
        boolean once = dialogsPreferences.getBoolean("pinned_dialog_migrate_once", true);
        if (once) {
            SharedPreferences.Editor editor = dialogsPreferences.edit();
            editor.putBoolean("pinned_dialog_migrate_once", false);
            editor.apply();
            Set<String> string_set = restorePinnedDialogsStringSet();
            Set<String> string_set_copy = new HashSet<>(string_set);
            for (int i = 0, size = dialogs.size(); i < size; i++) {
                TLRPC.TL_dialog dialog = dialogs.get(i);
                final long did = dialog.id;
                final int pinnedNum = dialog.pinnedNum;
                if (pinnedNum > 0) {
                    string_set_copy.add(String.valueOf(did) + "," + String.valueOf(pinnedNum));
                }
            }
            editor.putStringSet("stored_pinned_dialog_set", string_set_copy);
            editor.apply();
        }
    }

    public void migrateFavoritedDialogs(ArrayList<TLRPC.TL_dialog> dialogs) {
        boolean once = dialogsPreferences.getBoolean("favorite_dialog_migrate_once", true);
        if (once) {
            SharedPreferences.Editor editor = dialogsPreferences.edit();
            editor.putBoolean("favorite_dialog_migrate_once", false);
            editor.apply();
            Set<String> string_set = restoreFavoriteDialogsStringSet();
            Set<String> string_set_copy = new HashSet<>(string_set);
            for (int i = 0, size = dialogs.size(); i < size; i++) {
                TLRPC.TL_dialog dialog = dialogs.get(i);
                final long did = dialog.id;
                final int favorite_date = dialog.favorite_date;
                if (favorite_date > 0) {
                    string_set_copy.add(String.valueOf(did) + "," + String.valueOf(favorite_date));
                }
            }
            editor.putStringSet("stored_favorite_dialog_set", string_set_copy);
            editor.apply();
        }
    }
}
