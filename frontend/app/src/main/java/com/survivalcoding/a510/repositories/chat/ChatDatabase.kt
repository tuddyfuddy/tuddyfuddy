package com.survivalcoding.a510.repositories.chat

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.survivalcoding.a510.R

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE messages ADD COLUMN roomId INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // chat_info 테이블 생성
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS chat_info (
                id INTEGER PRIMARY KEY NOT NULL,
                profileImage INTEGER NOT NULL,
                name TEXT NOT NULL,
                lastMessage TEXT NOT NULL DEFAULT '',
                lastMessageTime INTEGER NOT NULL DEFAULT 0,
                unreadCount INTEGER NOT NULL DEFAULT 0
            )
        """)

        // 기존 messages 테이블의 마지막 메시지들을 chat_info에 삽입
        database.execSQL("""
            INSERT INTO chat_info (id, profileImage, name, lastMessage, lastMessageTime)
            SELECT DISTINCT 
                roomId as id,
                CASE 
                    WHEN roomId = 1 THEN ${R.drawable.cha}
                    ELSE ${R.drawable.back}
                END as profileImage,
                CASE 
                    WHEN roomId = 1 THEN '활명수'
                    ELSE '백지헌'
                END as name,
                content as lastMessage,
                timestamp as lastMessageTime
            FROM messages
            WHERE timestamp IN (
                SELECT MAX(timestamp)
                FROM messages
                GROUP BY roomId
            )
        """)
    }
}

@Database(entities = [ChatMessage::class, ChatInfo::class], version = 3)
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}