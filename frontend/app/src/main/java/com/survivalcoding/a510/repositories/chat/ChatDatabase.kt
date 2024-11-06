package com.survivalcoding.a510.repositories.chat

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.survivalcoding.a510.repositories.chat.ChatDatabaseMigrations.MIGRATION_1_2
import com.survivalcoding.a510.repositories.chat.ChatDatabaseMigrations.MIGRATION_2_3
import com.survivalcoding.a510.repositories.chat.ChatDatabaseMigrations.MIGRATION_3_4
import com.survivalcoding.a510.repositories.chat.ChatDatabaseMigrations.MIGRATION_4_5
import com.survivalcoding.a510.repositories.chat.ChatDatabaseMigrations.MIGRATION_5_6


@Database(entities = [ChatMessage::class, ChatInfo::class], version = 5)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun chatInfoDao(): ChatInfoDao

    companion object {
        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getDatabase(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "chat_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}