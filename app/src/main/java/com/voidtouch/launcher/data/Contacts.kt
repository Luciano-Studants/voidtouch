package com.voidtouch.launcher.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

/** Busca de contatos pelo nome. Só funciona com a permissão de contatos, que você liga quando quiser. */
object ContactsReader {
    fun hasAccess(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    /** Até [limit] contatos cujo nome combina com o texto. Lista vazia sem permissão ou em erro. */
    fun search(context: Context, query: String, limit: Int = 3): List<Hit.Contact> {
        if (!hasAccess(context) || query.trim().length < 2) return emptyList()
        return runCatching {
            val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(query.trim()))
            val projection = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            )
            val out = ArrayList<Hit.Contact>()
            context.contentResolver.query(uri, projection, null, null, "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC")?.use { c ->
                while (c.moveToNext() && out.size < limit) {
                    val lookup = ContactsContract.Contacts.getLookupUri(c.getLong(0), c.getString(1))
                    out += Hit.Contact(c.getString(2).orEmpty(), lookup.toString())
                }
            }
            out
        }.getOrDefault(emptyList())
    }
}
