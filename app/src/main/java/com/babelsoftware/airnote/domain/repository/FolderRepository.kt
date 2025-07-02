/*
 * Copyright (c) 2025 Babel Software.
 */

package com.babelsoftware.airnote.domain.repository

import com.babelsoftware.airnote.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    fun getAllFolders(): Flow<List<Folder>>
    suspend fun insertFolder(folder: Folder)
    suspend fun updateFolder(folder: Folder)
    suspend fun deleteFolder(folder: Folder)
}