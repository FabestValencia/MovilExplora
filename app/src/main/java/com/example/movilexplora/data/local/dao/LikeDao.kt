package com.example.movilexplora.data.local.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.movilexplora.data.local.entity.LikeEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface LikeDao {
    @Query("SELECT * FROM likes WHERE itemType = 'POST'")
    fun getAllPostLikes(): Flow<List<LikeEntity>>
    @Query("SELECT * FROM likes WHERE itemType = 'EVENT'")
    fun getAllEventLikes(): Flow<List<LikeEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: LikeEntity)
    @Query("DELETE FROM likes WHERE itemId = :itemId AND userId = :userId AND itemType = :itemType")
    suspend fun deleteLike(itemId: String, userId: String, itemType: String)
    @Query("SELECT COUNT(*) FROM likes WHERE itemId = :itemId AND userId = :userId AND itemType = :itemType")
    suspend fun isLiked(itemId: String, userId: String, itemType: String): Int
}
