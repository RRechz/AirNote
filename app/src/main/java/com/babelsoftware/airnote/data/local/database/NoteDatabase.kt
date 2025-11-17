package com.babelsoftware.airnote.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.babelsoftware.airnote.constant.DatabaseConst
import com.babelsoftware.airnote.data.local.converter.Converters
import com.babelsoftware.airnote.data.local.dao.AiChatDao
import com.babelsoftware.airnote.data.local.dao.NoteDao
import com.babelsoftware.airnote.data.source.FolderDao
import com.babelsoftware.airnote.domain.model.AiChatMessage
import com.babelsoftware.airnote.domain.model.AiChatSession
import com.babelsoftware.airnote.domain.model.Folder
import com.babelsoftware.airnote.domain.model.Note

@Database(
    entities = [
        Note::class,
        Folder::class,
        AiChatSession::class,
        AiChatMessage::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun aiChatDao(): AiChatDao
}
