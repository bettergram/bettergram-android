package io.bettergram.ui.adapters;

import android.content.Context;

import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Adapters.DialogsAdapter;

import java.util.List;

import io.bettergram.tools.DialogsObject;
import ru.johnlife.lifetools.optional.Mapper;
import ru.johnlife.lifetools.tools.ListUtil;

public class BetterDialogsAdapter extends DialogsAdapter {
    private static final Mapper<ListUtil.Filter<TLRPC.TL_dialog>> filterMapper = new Mapper<ListUtil.Filter<TLRPC.TL_dialog>>()
            .defaultValue(dialog -> false)
            .add(101, DialogsObject::isDirect)
            .add(102, DialogsObject::isGroup)
            .add(103, DialogsObject::isAnnouncement);
    private int currentAccount = UserConfig.selectedAccount;
    private List<TLRPC.TL_dialog> cache = null;

    public BetterDialogsAdapter(Context context, int type, boolean onlySelect) {
        super(context, type, onlySelect);
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
        return cache;
    }
}
