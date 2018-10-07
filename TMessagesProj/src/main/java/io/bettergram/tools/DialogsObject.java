package io.bettergram.tools;

import org.telegram.messenger.DialogObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;

public class DialogsObject extends DialogObject {

    public static boolean isGroup(TLRPC.TL_dialog d) {
        return getHigherId(d) != 0;
    }

    public static boolean isDirect(TLRPC.TL_dialog d) {
//        int selfId = UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId();
//        int lower_id = getLowerId(d);
        return getHigherId(d) == 0;
    }

    public static boolean isAnnouncement(TLRPC.TL_dialog d) {
        if (DialogObject.isChannel(d)) {
            MessagesController messagesController = MessagesController.getInstance(UserConfig.selectedAccount);
            TLRPC.Chat chat = messagesController.getChat(-getLowerId(d));
            return (
                    chat != null && (
                            chat.megagroup && (
                                    chat.admin_rights != null && (
                                            chat.admin_rights.post_messages || chat.admin_rights.add_admins
                                    )
                            ) || chat.creator
                    )
            );
        }
        return false;
    }

    private static int getHigherId(TLRPC.TL_dialog d) {
        return (int) (d.id >> 32);
    }

    private static int getLowerId(TLRPC.TL_dialog d) {
        return (int) d.id;
    }


}
