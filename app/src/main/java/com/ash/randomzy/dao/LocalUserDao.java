package com.ash.randomzy.dao;

import com.ash.randomzy.entity.LocalUser;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface LocalUserDao {

    @Insert
    void insertLocalUser(LocalUser localUser);

    @Query("SELECT * FROM LocalUser")
    List<LocalUser> getAll();

    @Query("DELETE FROM LocalUser")
    void deleteAllLocalUsers();

    @Query("DELETE FROM localuser WHERE id =:id")
    void deleteLocalUser(String id);

    @Query("SELECT * FROM localuser WHERE isFav =1")
    List<LocalUser> getAllFavLocalUsers();

    @Query("SELECT id FROM LocalUser")
    List<String> getAllIds();

}
