package com.babelsoftware.airnote.data.local.database


import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.babelsoftware.airnote.constant.DatabaseConst
import com.babelsoftware.airnote.data.local.dao.NoteDao
import com.babelsoftware.airnote.data.source.FolderDao

class NoteDatabaseProvider(private val application: Application) {

    @Volatile
    private var database: NoteDatabase? = null

    @Synchronized
    fun instance(): NoteDatabase {
        return database ?: synchronized(this) {
            database ?: buildDatabase().also { database = it }
        }
    }

    private fun buildDatabase(): NoteDatabase {
        return Room.databaseBuilder(application.applicationContext,
            NoteDatabase::class.java,
            DatabaseConst.NOTES_DATABASE_FILE_NAME)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_2_4, MIGRATION_4_5, MIGRATION_5_6)
            .build()
    }

    @Synchronized
    fun close() {
        database?.close()
        database = null
    }

    fun noteDao(): NoteDao {
        return instance().noteDao()
    }

    fun folderDao(): FolderDao {
        return instance().folderDao()
    }
}



private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `notes-table` ADD COLUMN `created_at` INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `notes-table` ADD COLUMN `pinned` INTEGER NOT NULL DEFAULT 0")
    }
}

private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `notes-table` ADD COLUMN `encrypted` INTEGER NOT NULL DEFAULT 0")
    }
}

private val MIGRATION_2_4 = object : Migration(2, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `notes-table` ADD COLUMN `pinned` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `notes-table` ADD COLUMN `encrypted` INTEGER NOT NULL DEFAULT 0")
    }
}

private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `Folder` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `color` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
        db.execSQL("ALTER TABLE `notes-table` ADD COLUMN `foldered` INTEGER DEFAULT NULL")
    }
}

private val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE `Folder_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `iconName` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
        db.execSQL("INSERT INTO `Folder_new` (`id`, `name`, `iconName`, `createdAt`) SELECT `id`, `name`, 'Folder', `createdAt` FROM `Folder`")
        db.execSQL("DROP TABLE `Folder`")
        db.execSQL("ALTER TABLE `Folder_new` RENAME TO `Folder`")
    }
}
