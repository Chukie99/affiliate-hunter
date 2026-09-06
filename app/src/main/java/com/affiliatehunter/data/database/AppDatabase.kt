package com.affiliatehunter.data.database
import androidx.room.*
import com.affiliatehunter.data.model.FavoriteEntity
@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC") suspend fun getAll(): List<FavoriteEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(e: FavoriteEntity)
    @Query("DELETE FROM favorites WHERE id=:id") suspend fun delete(id: String)
    @Query("DELETE FROM favorites") suspend fun clear()
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id=:id)") suspend fun exists(id:String): Boolean
}
@Database(entities=[FavoriteEntity::class], version=1, exportSchema=false)
abstract class AppDatabase: RoomDatabase(){ abstract fun favoriteDao(): FavoriteDao }
