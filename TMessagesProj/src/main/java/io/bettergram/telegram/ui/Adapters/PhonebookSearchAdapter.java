/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package io.bettergram.telegram.ui.Adapters;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

import io.bettergram.telegram.PhoneFormat.PhoneFormat;
import io.bettergram.telegram.messenger.AndroidUtilities;
import io.bettergram.telegram.messenger.ContactsController;
import io.bettergram.telegram.messenger.FileLog;
import io.bettergram.telegram.messenger.LocaleController;
import io.bettergram.telegram.messenger.MessagesController;
import io.bettergram.telegram.messenger.UserConfig;
import io.bettergram.telegram.messenger.Utilities;
import io.bettergram.telegram.messenger.support.widget.RecyclerView;
import io.bettergram.telegram.tgnet.TLRPC;
import io.bettergram.telegram.ui.Cells.UserCell;
import io.bettergram.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PhonebookSearchAdapter extends RecyclerListView.SelectionAdapter {

    private Context mContext;
    private ArrayList<Object> searchResult = new ArrayList<>();
    private ArrayList<CharSequence> searchResultNames = new ArrayList<>();
    private Timer searchTimer;

    public PhonebookSearchAdapter(Context context) {
        mContext = context;
    }

    public void search(final String query) {
        try {
            if (searchTimer != null) {
                searchTimer.cancel();
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        if (query == null) {
            searchResult.clear();
            searchResultNames.clear();
            notifyDataSetChanged();
        } else {
            searchTimer = new Timer();
            searchTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        searchTimer.cancel();
                        searchTimer = null;
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                    processSearch(query);
                }
            }, 200, 300);
        }
    }

    private void processSearch(final String query) {
        AndroidUtilities.runOnUIThread(() -> {
            final int currentAccount = UserConfig.selectedAccount;
            final ArrayList<ContactsController.Contact> contactsCopy = new ArrayList<>(ContactsController.getInstance(currentAccount).contactsBook.values());
            final ArrayList<TLRPC.TL_contact> contactsCopy2 = new ArrayList<>(ContactsController.getInstance(currentAccount).contacts);
            Utilities.searchQueue.postRunnable(() -> {
                String search1 = query.trim().toLowerCase();
                if (search1.length() == 0) {
                    updateSearchResults(query, new ArrayList<>(), new ArrayList<>());
                    return;
                }
                String search2 = LocaleController.getInstance().getTranslitString(search1);
                if (search1.equals(search2) || search2.length() == 0) {
                    search2 = null;
                }
                String search[] = new String[1 + (search2 != null ? 1 : 0)];
                search[0] = search1;
                if (search2 != null) {
                    search[1] = search2;
                }

                ArrayList<Object> resultArray = new ArrayList<>();
                ArrayList<CharSequence> resultArrayNames = new ArrayList<>();
                SparseBooleanArray foundUids = new SparseBooleanArray();

                for (int a = 0; a < contactsCopy.size(); a++) {
                    ContactsController.Contact contact = contactsCopy.get(a);
                    String name = ContactsController.formatName(contact.first_name, contact.last_name).toLowerCase();
                    String tName = LocaleController.getInstance().getTranslitString(name);
                    String name2;
                    String tName2;
                    if (contact.user != null) {
                        name2 = ContactsController.formatName(contact.user.first_name, contact.user.last_name).toLowerCase();
                        tName2 = LocaleController.getInstance().getTranslitString(name);
                    } else {
                        name2 = null;
                        tName2 = null;
                    }
                    if (name.equals(tName)) {
                        tName = null;
                    }

                    int found = 0;
                    for (String q : search) {
                        if (name2 != null && (name2.startsWith(q) || name2.contains(" " + q)) || tName2 != null && (tName2.startsWith(q) || tName2.contains(" " + q))) {
                            found = 1;
                        } else if (contact.user != null && contact.user.username != null && contact.user.username.startsWith(q)) {
                            found = 2;
                        } else if (name.startsWith(q) || name.contains(" " + q) || tName != null && (tName.startsWith(q) || tName.contains(" " + q))) {
                            found = 3;
                        }
                        if (found != 0) {
                            if (found == 3) {
                                resultArrayNames.add(AndroidUtilities.generateSearchName(contact.first_name, contact.last_name, q));
                            } else if (found == 1) {
                                resultArrayNames.add(AndroidUtilities.generateSearchName(contact.user.first_name, contact.user.last_name, q));
                            } else {
                                resultArrayNames.add(AndroidUtilities.generateSearchName("@" + contact.user.username, null, "@" + q));
                            }
                            if (contact.user != null) {
                                foundUids.put(contact.user.id, true);
                            }
                            resultArray.add(contact);
                            break;
                        }
                    }
                }

                for (int a = 0; a < contactsCopy2.size(); a++) {
                    TLRPC.TL_contact contact = contactsCopy2.get(a);
                    if (foundUids.indexOfKey(contact.user_id) >= 0) {
                        continue;
                    }
                    TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(contact.user_id);
                    String name = ContactsController.formatName(user.first_name, user.last_name).toLowerCase();
                    String tName = LocaleController.getInstance().getTranslitString(name);
                    if (name.equals(tName)) {
                        tName = null;
                    }

                    int found = 0;
                    for (String q : search) {
                        if (name.startsWith(q) || name.contains(" " + q) || tName != null && (tName.startsWith(q) || tName.contains(" " + q))) {
                            found = 1;
                        } else if (user.username != null && user.username.startsWith(q)) {
                            found = 2;
                        }

                        if (found != 0) {
                            if (found == 1) {
                                resultArrayNames.add(AndroidUtilities.generateSearchName(user.first_name, user.last_name, q));
                            } else {
                                resultArrayNames.add(AndroidUtilities.generateSearchName("@" + user.username, null, "@" + q));
                            }
                            resultArray.add(user);
                            break;
                        }
                    }
                }

                updateSearchResults(query, resultArray, resultArrayNames);
            });
        });
    }

    protected void onUpdateSearchResults(String query) {

    }

    private void updateSearchResults(final String query, final ArrayList<Object> users, final ArrayList<CharSequence> names) {
        AndroidUtilities.runOnUIThread(() -> {
            onUpdateSearchResults(query);
            searchResult = users;
            searchResultNames = names;
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return searchResult.size();
    }

    public Object getItem(int i) {
        return searchResult.get(i);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 0:
            default:
                view = new UserCell(mContext, 8, 0, false);
                ((UserCell) view).setNameTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                break;
        }
        return new RecyclerListView.Holder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == 0) {
            UserCell userCell = (UserCell) holder.itemView;

            Object object = getItem(position);
            TLRPC.User user = null;
            if (object instanceof ContactsController.Contact) {
                ContactsController.Contact contact = (ContactsController.Contact) object;
                if (contact.user != null) {
                    user = contact.user;
                } else {
                    userCell.setCurrentId(contact.contact_id);
                    userCell.setData(null, searchResultNames.get(position), contact.phones.isEmpty() ? "" : PhoneFormat.getInstance().format(contact.phones.get(0)), 0);
                }
            } else {
                user = (TLRPC.User) object;
            }
            if (user != null) {
                userCell.setData(user, searchResultNames.get(position), PhoneFormat.getInstance().format("+" + user.phone), 0);
            }
        }
    }

    @Override
    public boolean isEnabled(RecyclerView.ViewHolder holder) {
        return true;
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }
}
