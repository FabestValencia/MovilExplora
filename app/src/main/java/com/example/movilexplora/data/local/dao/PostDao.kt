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
    suspend fun insertPost(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Query("UPDATE posts SET status = :status WHERE id = :postId")
    suspend fun updatePostStatus(postId: String, status: String)

    @Query("UPDATE posts SET status = :status, rejectionReason = :reason WHERE id = :postId")
    suspend fun updatePostStatusWithReason(postId: String, status: String, reason: String?)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: String)
}
