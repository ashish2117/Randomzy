package com.ash.randomzy.db;

import android.content.Context;

import com.ash.randomzy.dao.ActiveChatDao;
import com.ash.randomzy.dao.LocalUserDao;
import com.ash.randomzy.dao.MessageDao;
import com.ash.randomzy.entity.LocalUser;
import com.ash.randomzy.entity.Message;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Message.class, LocalUser.class}, version = 3, exportSchema = false)
public abstract class RandomzyDatabase extends RoomDatabase {
    public abstract ActiveChatDao activeChatDao();
    public abstract MessageDao messageDao();
    public abstract LocalUserDao localUserDao();

    private static final String DBNAME = "dandomzy-db";

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS LocalUser(name TEXT, id TEXT PRIMARY KEY NOT NULL, profilePicUrlLocal TEXT, isFav INTEGER NOT NULL, profilePicUrlServer TEXT)");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2,3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE ActiveChat");
        }
    };

    public static RandomzyDatabase getInstance(Context context){
        return  Room.databaseBuilder(context, RandomzyDatabase.class,DBNAME)
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .build();
    }
}
