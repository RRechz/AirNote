/*
 * Copyright (c) 2025 Babel Software.
 */

package com.babelsoftware.airnote.domain.usecase

import com.babelsoftware.airnote.domain.model.Folder
import com.babelsoftware.airnote.domain.repository.FolderRepository
import com.babelsoftware.airnote.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FolderUseCase @Inject constructor(
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository
) {
    fun getAllFolders(): Flow<List<Folder>> {
        return folderRepository.getAllFolders()
    }

    suspend fun addFolder(folder: Folder) {
        folderRepository.insertFolder(folder)
    }

    suspend fun updateFolder(folder: Folder) {
        folderRepository.updateFolder(folder)
    }

    suspend fun deleteFolder(folder: Folder) {
        folderRepository.deleteFolder(folder)
    }

    suspend fun deleteFolderAndUnlinkNotes(folder: Folder) {
        noteRepository.unlinkNotesFromFolder(folder.id)
        folderRepository.deleteFolder(folder)
    }
}