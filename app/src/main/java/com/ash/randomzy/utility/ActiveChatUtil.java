package com.ash.randomzy.utility;

import com.ash.randomzy.entity.LocalUser;
import com.ash.randomzy.model.ActiveChat;

public class ActiveChatUtil {
    public static LocalUser getLocalUserFromActiveChat(ActiveChat activeChat){
        LocalUser localUser = new LocalUser();
        localUser.setId(activeChat.getId());
        localUser.setProfilePicUrlServer(activeChat.getProfilePicUrlServer());
        localUser.setProfilePicUrlLocal(activeChat.getProfilePicUrlLocal());
        localUser.setIsFav(activeChat.getIsFav());
        localUser.setName(activeChat.getName());
        return localUser;
    }
}
