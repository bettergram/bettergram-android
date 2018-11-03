package io.bettergram.tools;

import io.bettergram.telegram.messenger.ChatObject;
import io.bettergram.telegram.messenger.DialogObject;
import io.bettergram.telegram.messenger.MessagesController;
import io.bettergram.telegram.messenger.UserConfig;
import io.bettergram.telegram.tgnet.TLRPC;

public class DialogsObject extends DialogObject {

    public static boolean isGroup(TLRPC.TL_dialog d) {
        return !isAnnouncement(d) && getHigherId(d) != 0;
    }

    public static boolean isDirect(TLRPC.TL_dialog d) {
        return getHigherId(d) == 0;
    }

    public static boolean isAnnouncement(TLRPC.TL_dialog d) {
//        if (DialogObject.isChannel(d)) {
//            MessagesController messagesController = MessagesController.getInstance(UserConfig.selectedAccount);
//            TLRPC.Chat chat = messagesController.getChat(-getLowerId(d));
//            return (
//                    chat != null && (
//                            chat.megagroup && (
//                                    chat.admin_rights != null && (
//                                            chat.admin_rights.post_messages || chat.admin_rights.add_admins
//                                    )
//                            ) || chat.creator
//                    )
//            );
//        }
        if (isChannel(d)) {
            MessagesController messagesController = MessagesController.getInstance(UserConfig.selectedAccount);
            TLRPC.Chat chat = messagesController.getChat(-getLowerId(d));
            return (chat.id < 0 || ChatObject.isChannel(chat) && !chat.megagroup);
        }
        return false;
    }

    public static boolean isFavorite(TLRPC.TL_dialog d) {
        return d.favorite_date > 0;
    }

    private static int getHigherId(TLRPC.TL_dialog d) {
        return (int) (d.id >> 32);
    }

    private static int getLowerId(TLRPC.TL_dialog d) {
        return (int) d.id;
    }


}
