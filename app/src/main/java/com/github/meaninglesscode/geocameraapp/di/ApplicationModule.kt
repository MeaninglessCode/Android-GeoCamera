package com.github.meaninglesscode.geocameraapp.di

import android.content.Context
import androidx.room.Room
import com.github.meaninglesscode.geocameraapp.data.source.DefaultPictureDataRepository
import com.github.meaninglesscode.geocameraapp.data.source.PictureDataDataSource
import com.github.meaninglesscode.geocameraapp.data.source.PictureDataRepository
import com.github.meaninglesscode.geocameraapp.data.source.local.PictureDataDatabase
import com.github.meaninglesscode.geocameraapp.data.source.local.PictureDataLocalDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.annotation.AnnotationRetention.RUNTIME

/**
 * Dagger [Module] that includes [ApplicationModuleBinds] and is used for dependency injection.
 */
@Module(includes = [ApplicationModuleBinds::class])
object ApplicationModule {
    /**
     * [Qualifier] for the annotation class [PictureDataLocalDataSource] with a [RUNTIME]
     * [Retention] policy.
     */
    @Qualifier
    @Retention(RUNTIME)
    annotation class PictureDataLocalDataSource

    /**
     * [JvmStatic] method that [Provides] a [Singleton] injector instance of
     * [PictureDataLocalDataSource] instantiated from the given [PictureDataDatabase] and
     * [CoroutineDispatcher].
     *
     * @param [database] [PictureDataDatabase] used for instantiation of the [PictureDataDataSource]
     * @param [ioDispatcher] [CoroutineDispatcher] used for instantiation of the
     * [PictureDataLocalDataSource]
     * @return [Singleton] instance of [PictureDataDataSource]
     */
    @JvmStatic
    @Singleton
    @PictureDataLocalDataSource
    @Provides
    fun providePicturesLocalDataSource(database: PictureDataDatabase, ioDispatcher: CoroutineDispatcher): PictureDataDataSource {
        return PictureDataLocalDataSource(database.pictureDataDao(), ioDispatcher)
    }

    /**
     * [JvmStatic] method that [Provides] a [Singleton] injector instance of [PictureDataDatabase]
     * via [Room.databaseBuilder] instantiated via the given [Context].
     *
     * @param [context] [Context] used for instantiation of the [PictureDataDatabase]
     * @return [Singleton] instance of [PictureDataDatabase]
     */
    @JvmStatic
    @Singleton
    @Provides
    fun provideDatabase(context: Context): PictureDataDatabase {
        return Room.databaseBuilder(
            context.applicationContext, PictureDataDatabase::class.java, "picture_data.db"
        ).build()
    }

    /**
     * [JvmStatic] method that [Provides] a [Singleton] instance of [Dispatchers.IO] for injection
     * throughout the application.
     *
     * @return [Singleton] instance of [CoroutineDispatcher]
     */
    @JvmStatic
    @Singleton
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO
}

/**
 * Submodule for repository binding to be included by the [ApplicationModule].
 */
@Module
abstract class ApplicationModuleBinds {
    /**
     * Method that [Binds] a [Singleton] instance of [DefaultPictureDataRepository].
     *
     * @return [PictureDataRepository] returned injected parameter
     */
    @Singleton
    @Binds
    abstract fun bindRepository(repo: DefaultPictureDataRepository): PictureDataRepository
}