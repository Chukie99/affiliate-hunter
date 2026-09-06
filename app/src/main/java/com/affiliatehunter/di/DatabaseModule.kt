package com.affiliatehunter.di
import android.content.Context
import androidx.room.Room
import com.affiliatehunter.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDb(@ApplicationContext c: Context) = Room.databaseBuilder(c, AppDatabase::class.java, "affiliate.db").fallbackToDestructiveMigration().build()
    @Provides fun provideDao(db: AppDatabase) = db.favoriteDao()
}
