package com.ash.randomzy.repository;

import android.content.Context;

import com.ash.randomzy.db.RandomzyDatabase;
import com.ash.randomzy.entity.LocalUser;

import java.util.List;

import androidx.room.Room;

public class LocalUserRepository {

    private RandomzyDatabase randomzyDatabase;

    private final String DBNAME = "dandomzy-db";


    public LocalUserRepository(Context context){
        randomzyDatabase = RandomzyDatabase.getInstance(context);
    }

    public void insertLocalUser(LocalUser localUser){
        randomzyDatabase.localUserDao().insertLocalUser(localUser);
    }

    public List<LocalUser> getAll(){
        return randomzyDatabase.localUserDao().getAll();
    }

    public void deleteAllLocalUsers(){
        randomzyDatabase.localUserDao().deleteAllLocalUsers();
    }

    public void deleteLocalUser(String id){
        randomzyDatabase.localUserDao().deleteLocalUser(id);
    }

    public List<LocalUser> getAllFavLocalUsers(){
       return randomzyDatabase.localUserDao().getAllFavLocalUsers();
    }

    public List<String> getAllIds(){
        return randomzyDatabase.localUserDao().getAllIds();
    }
}
