package com.example.movilexplora.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.movilexplora.data.local.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :id")
    fun getPostById(id: String): Flow<PostEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>): List<Long>

    @Query("UPDATE posts SET status = :status WHERE id = :postId")
    suspend fun updatePostStatus(postId: String, status: String): Int

    @Query("UPDATE posts SET status = :status, rejectionReason = :reason WHERE id = :postId")
    suspend fun updatePostStatusWithReason(postId: String, status: String, reason: String?): Int

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: String): Int

    @Query("DELETE FROM posts")
    suspend fun clearAll(): Int

    @Query("SELECT * FROM posts WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLon AND :maxLon")
    fun getPostsInRegion(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts ORDER BY id DESC")
    fun getPostsPagingSource(): androidx.paging.PagingSource<Int, PostEntity>

    @Query("SELECT * FROM posts WHERE (:category IS NULL OR category = :category) AND (:priceLimit = 4 OR length(replace(price, '$', '')) <= :priceLimit) ORDER BY id DESC")
    fun getFilteredPostsPagingSource(category: String?, priceLimit: Int): androidx.paging.PagingSource<Int, PostEntity>
}
