package com.tdcolvin.contentproviderdemo

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log

class BirthdatesContentProvider: ContentProvider() {
    companion object {
        private const val AUTHORITY = "com.tdcolvin.contentproviderdemo"
        private const val TABLE_NAME = "birthdates"
        private const val URI_MATCH_CODE = 1

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, TABLE_NAME, URI_MATCH_CODE)
        }
    }

    private lateinit var dbHelper: BirthdatesDatabase

    // Called when the content provider is loaded, as a result of the client application
    // calling in.
    override fun onCreate(): Boolean {
        val context = context ?: return false

        // Object to help us manage the underlying SQLite database
        dbHelper = BirthdatesDatabase(context)
        return true
    }

    // This function is called by older Android versions. We stub it and call the query function
    // below instead.
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val queryArgs = Bundle().apply {
            putString("selection", selection)
            putStringArray("selectionArgs", selectionArgs)
            putString("sortOrder", sortOrder)
        }
        return query(uri, projection, queryArgs, null)
    }

    // Called when a client wants to query our content resolver
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        queryArgs: Bundle?,
        cancellationSignal: CancellationSignal?
    ): Cursor? {
        Log.v("content", "Content query on uri: $uri")

        // If it's not a URI we recognise, don't continue. This is an important security step as
        // well as a helpful way to match against known URIs.
        val match = uriMatcher.match(uri)
        if (match != URI_MATCH_CODE) {
            return null
        }

        // NOTE: This function supports getting *all* birthdates via the /birthdates URI. By
        // convention, it should also support getting a *single* birthdate via /birthdates/id.
        // Exercise to the reader :)

        // We have to return a Cursor, which is a pointer to a record in a database. We
        // could create our own completely custom logic here, but for the sake of the demo we are
        // keeping the data inside a SQLite database. The benefit of that is that it returns a
        // Cursor anyway, so we can use it to do the heavy lifting.
        val qb = SQLiteQueryBuilder()
        qb.tables = TABLE_NAME
        val db = dbHelper.readableDatabase

        // Extract arguments from the Bundle if needed
        val selection = queryArgs?.getString("selection")
        val selectionArgs = queryArgs?.getStringArray("selectionArgs")
        val sortOrder = queryArgs?.getString("sortOrder")

        val cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder, null, cancellationSignal)

        return cursor
    }

    // Called when the client needs to know what type of data it's getting
    override fun getType(uri: Uri): String? {
        val match = uriMatcher.match(uri)
        return when (match) {
            URI_MATCH_CODE -> "vnd.android.cursor.dir/vnd.com.tdcolvin.contentproviderdemo.birthdates"
            else -> null
        }
    }

    // Called when the client inserts data via this content provider. Again, we just piggy-back off
    // a SQLite database, which does the heavy lifting.
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val qb = SQLiteQueryBuilder()
        qb.tables = TABLE_NAME
        val db = dbHelper.writableDatabase

        // Security check as well has helpful matching system
        val match = uriMatcher.match(uri)
        if (match != URI_MATCH_CODE) {
            return null
        }

        // Insert the new record into the SQLite database.
        val rowId = db.insert(TABLE_NAME, null, values)

        // We need to return a URI which points to the new record. In our defined schema, that's
        // as follows:
        val newUri = Uri.parse("content://$AUTHORITY/$TABLE_NAME/$rowId")

        context?.contentResolver?.notifyChange(newUri, null)
        db.close()

        return newUri
    }

    // Not used in the demo:
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

}