package com.aivora.app.data.local

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

data class DeviceContact(val name: String, val phone: String)

object DeviceContacts {
    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED

    /** Up to [limit] phone contacts with a number, sorted by name. */
    fun load(context: Context, limit: Int = 200): List<DeviceContact> {
        if (!hasPermission(context)) return emptyList()
        val out = LinkedHashMap<String, DeviceContact>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
        )
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} COLLATE NOCASE ASC",
        )?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (cursor.moveToNext() && out.size < limit) {
                val name = cursor.getString(nameIdx)?.trim().orEmpty()
                val phone = cursor.getString(phoneIdx)?.trim().orEmpty()
                if (name.isBlank() || phone.isBlank()) continue
                val key = phone.replace(Regex("[^0-9+]"), "")
                if (key.length < 6) continue
                out.putIfAbsent(key, DeviceContact(name = name, phone = phone))
            }
        }
        return out.values.toList()
    }
}
