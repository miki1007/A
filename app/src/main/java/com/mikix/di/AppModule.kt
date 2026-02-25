package com.mikix.di

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.work.WorkManager
import com.mikix.data.MikixDao
import com.mikix.data.MikixDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideDb(@ApplicationContext context: Context): MikixDatabase = Room.databaseBuilder(context, MikixDatabase::class.java, "mikix.db").build()

    @Provides
    fun provideDao(db: MikixDatabase): MikixDao = db.dao()

    @Provides @Singleton
    fun provideDataStore(@ApplicationContext context: Context): androidx.datastore.core.DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO)) { context.preferencesDataStoreFile("mikix_prefs") }

    @Provides
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)
}
