package com.example.litechat.presenter

import android.arch.persistence.room.Room
import android.content.Context
import android.os.Build
import android.provider.ContactsContract
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.View
import com.example.litechat.contracts.ContactFragContract
import com.example.litechat.listeners.CallListenerObject
import com.example.litechat.model.*
import com.example.litechat.model.contactsRoom.AppDatabse
import com.example.litechat.model.contactsRoom.User
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList

class ContactFragPresenter(viewPassed: ContactFragContract.View, contextPassed: Context): ContactFragContract.Presenter{

    private val view = viewPassed
    private val model = ContactListModel()
    private val context = contextPassed


    override fun getContacts() {



        val cursor1 = ContentResolverData.contentResolverPassed.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
        if (cursor1?.count ?: 0 > 0){

            while (cursor1 != null && cursor1.moveToNext()) {

                val id = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                if (cursor1.getInt(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {

                    val cursor2 = ContentResolverData.contentResolverPassed.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),null)

                    while (cursor2!!.moveToNext()){

                        val number = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        val num = number.replace("[^0-9]".toRegex(),"")
                        val numb = num.takeLast(10)
                        val userItem = User(numb, name)
                        Log.d("ContactsAddition" , userItem.toString())

                        ContactDataModel.contactList.add(userItem)
                    }
                    cursor2.close()
                }
            }
        }
        cursor1?.close()
        getUsers()

    }

    override fun passUserList(): List<User> {
        return model.roomGetData(context)
    }

    override fun getUsers() {

        val database = FirebaseFirestore.getInstance()
        database.collection("Users").get().addOnSuccessListener { result ->

            for ( document in result){

                ContactDataModel.usersList.add(document.id)

            }

            compareUserContact()
        }
    }


    private fun compareUserContact() {

        for (user in ContactDataModel.usersList){

            for (contact in ContactDataModel.contactList){

                if (contact.mobileNumber == user /*||  contact.mobileNumber == "0$user" || contact.mobileNumber == "91$user"*/ ){
                    ContactDataModel.contactAndUser.add(contact)
                    Log.d("contact", contact.mobileNumber + contact.name)
                    break
                }
            }
        }
        model.roomDeleteData(context)
        model.roomSetData(context, ContactDataModel.contactAndUser)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun startNewChatFromContact(number: String) {
          Log.d("check22",number.reversed().substring(0,10).reversed())
        model.startChatActivity(number,this)
    }

    override fun passDataForChatActivity(chatObject: ChatObject) {

        val db = Room.databaseBuilder(context, AppDatabse::class.java, "Contact_Database")
            .allowMainThreadQueries().build()
        // if condition
       AllChatDataModel.currentlyChattingWith=db.userDao().getName(chatObject.otherNumber)
        view.startChatActivity(chatObject)
    }

}