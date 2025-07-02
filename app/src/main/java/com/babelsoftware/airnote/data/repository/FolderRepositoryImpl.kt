/*
 * Copyright (c) 2025 Babel Software.
 */

package com.babelsoftware.airnote.data.repository

import com.babelsoftware.airnote.data.source.FolderDao
import com.babelsoftware.airnote.domain.model.Folder
import com.babelsoftware.airnote.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FolderRepositoryImpl @Inject constructor(
    private val folderDao: FolderDao
) : FolderRepository {
    override fun getAllFolders(): Flow<List<Folder>> = folderDao.getAllFolders()
    override suspend fun insertFolder(folder: Folder) = folderDao.insertFolder(folder)
    override suspend fun updateFolder(folder: Folder) = folderDao.updateFolder(folder)
    override suspend fun deleteFolder(folder: Folder) = folderDao.deleteFolder(folder)
}