package com.babelsoftware.airnote.di

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.babelsoftware.airnote.data.local.database.NoteDatabaseProvider
import com.babelsoftware.airnote.data.repository.FolderRepositoryImpl
import com.babelsoftware.airnote.data.repository.ImportExportRepository
import com.babelsoftware.airnote.data.repository.NoteRepositoryImpl
import com.babelsoftware.airnote.data.repository.SettingsRepositoryImpl
import com.babelsoftware.airnote.data.source.FolderDao
import com.babelsoftware.airnote.domain.repository.FolderRepository
import com.babelsoftware.airnote.domain.repository.NoteRepository
import com.babelsoftware.airnote.domain.repository.SettingsRepository
import com.babelsoftware.airnote.domain.usecase.FolderUseCase
import com.babelsoftware.airnote.presentation.components.EncryptionHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.Executors
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class WidgetCoroutineScope

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    @WidgetCoroutineScope
    fun providesWidgetCoroutineScope(): CoroutineScope = CoroutineScope(
        Executors.newSingleThreadExecutor().asCoroutineDispatcher(),
    )

    @Provides
    @Singleton
    fun provideNoteDatabaseProvider(application: Application): NoteDatabaseProvider =
        NoteDatabaseProvider(application)

    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Provides
    fun provideMutex(): Mutex = Mutex()

    @Provides
    @Singleton
    fun provideExecutorCoroutineDispatcher(): ExecutorCoroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    @Provides
    @Singleton
    fun provideNoteRepository(noteDatabaseProvider: NoteDatabaseProvider): NoteRepository {
        return NoteRepositoryImpl(noteDatabaseProvider)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideBackupRepository(
        noteDatabaseProvider: NoteDatabaseProvider,
        application: Application,
        mutex: Mutex,
        coroutineScope: CoroutineScope,
        executorCoroutineDispatcher: ExecutorCoroutineDispatcher,
    ): ImportExportRepository {
        return ImportExportRepository(
            provider = noteDatabaseProvider,
            context = application,
            mutex = mutex,
            scope = coroutineScope,
            dispatcher = executorCoroutineDispatcher
        )
    }

    @Provides
    @Singleton
    fun provideMutableVaultPassword(): StringBuilder {
        return StringBuilder()
    }

    @Provides
    @Singleton
    fun provideEncryptionHelper(mutableVaultPassword: StringBuilder): EncryptionHelper {
        return EncryptionHelper(mutableVaultPassword)
    }

    @Provides
    fun provideHandler(): Handler {
        return Handler(Looper.getMainLooper())
    }

    @Provides
    @Singleton
    fun provideFolderDao(noteDatabaseProvider: NoteDatabaseProvider): FolderDao {
        return noteDatabaseProvider.folderDao()
    }

    @Provides
    @Singleton
    fun provideFolderRepository(folderDao: FolderDao): FolderRepository {
        return FolderRepositoryImpl(folderDao)
    }

    @Provides
    @Singleton
    fun provideFolderUseCase(
        folderRepository: FolderRepository,
        noteRepository: NoteRepository
    ): FolderUseCase {
        return FolderUseCase(folderRepository, noteRepository)
    }
}
