package com.tdcolvin.contentproviderdemo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDate

data class UserBirthdate(
    val name: String,
    val birthdate: LocalDate
)

class BirthdatesDatabase(context: Context) : SQLiteOpenHelper(context, "birthdates.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE birthdates (id INTEGER PRIMARY KEY, name TEXT, birthdate TEXT)")
        db.insert("birthdates", null, ContentValues().apply {
            put("name", "Tom")
            put("birthdate", "1960-01-01")
        })
        db.insert("birthdates", null, ContentValues().apply {
            put("name", "Joan")
            put("birthdate", "1998-01-01")
        })
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS birthdates")
        onCreate(db)
    }

    fun getBirthdates(): List<UserBirthdate> {
        val users = mutableListOf<UserBirthdate>()
        readableDatabase.rawQuery("SELECT * FROM birthdates", null).use { cursor ->
            val ct = cursor.count
            println(ct)
            while (cursor.moveToNext()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val birthdate = cursor.getString(cursor.getColumnIndexOrThrow("birthdate"))
                users.add(UserBirthdate(name, LocalDate.parse(birthdate)))
            }
            cursor.close()
        }
        return users.toList()
    }
}