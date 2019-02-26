package com.example.litechat.model.contactsRoom

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface UserDao {

    @Insert
    fun insertContact(vararg users: User)

    @Query("SELECT * FROM Contacts")
    fun getContactList(): List<User>

   /* @Query("SELECT * FROM Contacts where Number:$no")
    fun getName(no: String): String */

}