package io.bettergram.adapters;

import android.content.Context;

import java.util.Collections;
import java.util.List;

import io.bettergram.telegram.messenger.MessagesController;
import io.bettergram.telegram.messenger.UserConfig;
import io.bettergram.telegram.tgnet.TLObject;
import io.bettergram.telegram.tgnet.TLRPC;
import io.bettergram.telegram.ui.Adapters.DialogsAdapter;
import io.bettergram.tools.DialogsObject;
import ru.johnlife.lifetools.optional.Mapper;
import ru.johnlife.lifetools.tools.ListUtil;

public class BetterDialogsAdapter extends DialogsAdapter {
    private static final Mapper<ListUtil.Filter<TLRPC.TL_dialog>> filterMapper = new Mapper<ListUtil.Filter<TLRPC.TL_dialog>>()
            .defaultValue(dialog -> false)
            .add(101, DialogsObject::isDirect)
            .add(102, DialogsObject::isGroup)
            .add(103, DialogsObject::isAnnouncement)
            .add(104, DialogsObject::isFavorite);
    private int currentAccount = UserConfig.selectedAccount;
    private List<TLRPC.TL_dialog> cache = null;

    public BetterDialogsAdapter(Context context, int type, boolean onlySelect) {
        super(context, type, onlySelect);
    }

    public TLObject getItem(int i) {
        return super.getItem(i, getActualDialogsArray());
    }

    public void setDialogsType(int type) {
        cache = null;
        super.setDialogsType(type);
        notifyDataSetChanged();
    }

    @Override
    public List<TLRPC.TL_dialog> getDialogsArray() {
        if (cache == null) {
            int dialogsType = getDialogsType();
            if (dialogsType < 100) {
                cache = super.getDialogsArray();
            } else {
                cache = ListUtil.filter(MessagesController.getInstance(currentAccount).dialogs, filterMapper.get(dialogsType).get());
            }
        }
        return sort(cache);
    }

    private List<TLRPC.TL_dialog> getActualDialogsArray() {
        List<TLRPC.TL_dialog> dialogs;
        int dialogsType = getDialogsType();
        if (dialogsType < 100) {
            dialogs = super.getDialogsArray();
        } else {
            dialogs = ListUtil.filter(MessagesController.getInstance(currentAccount).dialogs, filterMapper.get(dialogsType).get());
        }
        return sort(dialogs);
    }

    private List<TLRPC.TL_dialog> sort(List<TLRPC.TL_dialog> dialogs) {
        Collections.sort(dialogs, MessagesController.getInstance(currentAccount).dialogComparator);
        return dialogs;
    }

    public void onItemMove(int fromPosition, int toPosition) {
        final TLRPC.TL_dialog correct_item = getActualDialogsArray().remove(fromPosition);
        getActualDialogsArray().add(toPosition, correct_item);
        notifyItemMoved(fromPosition, toPosition);
    }
}
