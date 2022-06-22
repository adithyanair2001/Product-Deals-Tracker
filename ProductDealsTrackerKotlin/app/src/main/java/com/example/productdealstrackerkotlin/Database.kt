package com.example.productdealstrackerkotlin

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.ArrayList as ArrayList

class Database(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, "tracker", factory, 22) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS items")
        db.execSQL("CREATE TABLE items (link TEXT PRIMARY KEY,budget INTEGER DEFAULT 0)")
    }
    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS items")
//        if (p2 > p1) {
//            db.execSQL("ALTER TABLE items ADD COLUMN budget INTEGER DEFAULT 0");
//        }

    }
    fun isExist(): Boolean {
        val dbr = this.readableDatabase
        val cursor = dbr.rawQuery(
            "select DISTINCT tbl_name from sqlite_master where tbl_name = 'items'",
            null
        )
        var out = false
        if (cursor != null && cursor.count > 0) {
            out = true
        }
        cursor.close()
        return out
    }

    fun addURL(URL: String,budgetValue: Int) {
        val values = ContentValues()
        val dbw = this.writableDatabase
        values.put("link", URL)
        values.put("budget",budgetValue)
        dbw.insert("items", null, values)
        dbw.close()
    }

    fun getURL(): MutableList<Pair<String,Int>> {
        val results: MutableList<Pair<String,Int>> = ArrayList()
//        val results: MutableList<String> = ArrayList()
        val dbr = this.readableDatabase
        val c = dbr.rawQuery("SELECT link,budget FROM items", null)
//        val userBudget = dbr.rawQuery("SELECT budget FROM items", null)
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    var link = c.getString(c.getColumnIndex("link"))
//                    results.add(link)
                    var budget = c.getInt(c.getColumnIndex("budget"))

                    results.add(Pair(link,budget))


                } while (c.moveToNext())
            }
        }
        c.close()
        return results
    }
    fun delete(URL: String): Boolean{
        val dbw = this.writableDatabase
        return dbw.delete("items","link=?", Array<String>(1){URL}) > 0
    }
    }
