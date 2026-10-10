package com.example.data

import android.annotation.SuppressLint
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import com.example.data.model.DeviceContact

class RealContactsHelper(private val context: Context) {

    @SuppressLint("Range")
    fun readDeviceContacts(): List<DeviceContact> {
        val contactList = mutableListOf<DeviceContact>()
        val seenPhones = mutableSetOf<String>()

        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            cursor?.use {
                while (it.moveToNext()) {
                    val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) ?: "Sin nombre"
                    val number = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: ""
                    val cleanNumber = number.replace(" ", "").replace("-", "").replace("(", "").replace(")", "")

                    if (cleanNumber.isNotBlank() && !seenPhones.contains(cleanNumber)) {
                        seenPhones.add(cleanNumber)
                        contactList.add(DeviceContact(name = name, phoneNumber = number))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RealContactsHelper", "Error reading contacts: ${e.message}")
        }

        return contactList
    }
}
