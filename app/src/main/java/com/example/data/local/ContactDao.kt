package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM saved_contacts ORDER BY addedAt DESC")
    fun getAllContactsFlow(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM saved_contacts ORDER BY addedAt DESC")
    suspend fun getAllContacts(): List<ContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateContact(contact: ContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateContacts(contacts: List<ContactEntity>)

    @Query("DELETE FROM saved_contacts WHERE contactUserId = :contactUserId")
    suspend fun deleteContact(contactUserId: String)

    @Query("DELETE FROM saved_contacts")
    suspend fun clearAll()
}
