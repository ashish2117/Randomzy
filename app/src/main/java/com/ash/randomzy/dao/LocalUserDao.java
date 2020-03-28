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

    @Query("DELETE FROM Localuser WHERE id =:id")
    void deleteLocalUser(String id);

    @Query("SELECT * FROM Localuser WHERE isFav =1")
    List<LocalUser> getAllFavLocalUsers();

    @Query("SELECT id FROM LocalUser")
    List<String> getAllIds();

    @Query("UPDATE LocalUser SET isFav =:isFav WHERE id =:id")
    void updateIsFav(String id, int isFav);
}
