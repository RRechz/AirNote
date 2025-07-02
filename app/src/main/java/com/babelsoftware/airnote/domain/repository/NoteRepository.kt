package com.babelsoftware.airnote.domain.repository

import com.babelsoftware.airnote.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    suspend fun addNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun unlinkNotesFromFolder(folderId: Long)
    fun getNoteById(id: Int): Flow<Note>
    fun getLastNoteId(): Long?
}