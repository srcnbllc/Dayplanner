package com.example.dayplanner.tags

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteTag(noteTag: NoteTag): Long

    @Query("SELECT * FROM tag ORDER BY name ASC")
    fun getAllTags(): LiveData<List<Tag>>
}


