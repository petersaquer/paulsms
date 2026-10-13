package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * AppDatabase para almacenamiento local seguro y offline de Paul Messenger.
 * Version 6: Soporte para borrado por usuario (deletedFor) y favoritos multiusuario (starredBy).
 */
@Database(entities = [ChatEntity::class, MessageEntity::class, ContactEntity::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Esquemas canónicos unificados para chats, messages y saved_contacts
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE chats ADD COLUMN deletedFor TEXT NOT NULL DEFAULT ''")
                } catch (e: Exception) {}
                try {
                    db.execSQL("ALTER TABLE messages ADD COLUMN starredBy TEXT NOT NULL DEFAULT ''")
                } catch (e: Exception) {}
                try {
                    db.execSQL("ALTER TABLE messages ADD COLUMN deletedFor TEXT NOT NULL DEFAULT ''")
                } catch (e: Exception) {}
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "paul_messenger.db"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
