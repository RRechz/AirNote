package com.babelsoftware.airnote.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.babelsoftware.airnote.constant.DatabaseConst
import com.babelsoftware.airnote.data.local.dao.NoteDao
import com.babelsoftware.airnote.data.source.FolderDao
import com.babelsoftware.airnote.domain.model.Folder
import com.babelsoftware.airnote.domain.model.Note

@Database(
    entities = [Note::class, Folder::class],
    version = DatabaseConst.NOTES_DATABASE_VERSION,
    exportSchema = false
)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
}