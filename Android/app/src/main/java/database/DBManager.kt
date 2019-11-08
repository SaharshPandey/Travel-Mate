package database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.util.Log
import android.widget.Toast

class DBManager {
    val dbName = "AddFriends"
    val dbTable="FriendsInfo"
    val colId = "_id"
    val colName = "name"
    val colcontact = "contact"
    val dbVersion = 1

    //CREATE TABLE IF NOT EXISTS NOTES (ID INTEGER PRIMARY KEY, title TEXT, descp TEXT);
    val sqlCreateTable = "CREATE TABLE IF NOT EXISTS "+dbTable+" ("+ colId +" INTEGER PRIMARY KEY, "+
            colName + " TEXT, "+ colcontact +" TEXT );"

    var sqlDB: SQLiteDatabase? =null

    constructor(context: Context?){
        var db = DatabaseHelperNotes(context)
        sqlDB = db.writableDatabase

    }

    inner class DatabaseHelperNotes : SQLiteOpenHelper{

        var context: Context? = null
        constructor(context: Context?):super(context, dbName, null, dbVersion){
        this.context = context
        }

        override fun onCreate(p0: SQLiteDatabase?) {
        p0!!.execSQL(sqlCreateTable)
            Log.d("DB","DATABASE CREATED")
            //Toast.makeText(this.context, "database created", Toast.LENGTH_SHORT).show()

        }

        override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
            Log.d("DB","DATABASE UPGRADED")
            p0!!.execSQL("DROP TABLE IF EXISTS"+ dbTable)

        }

    }

    fun Insert(values:ContentValues): Long{
        val ID = sqlDB!!.insert(dbTable, "", values)
        return ID
    }

    fun Query(projection: Array<String>, selection: String, selectionArgs: Array<String>, sorOrder: String?): Cursor{
        val qb = SQLiteQueryBuilder()
        qb.tables = dbTable
        val cursor = qb.query(sqlDB, projection, null, null, null, null, sorOrder)
        return  cursor
    }

}
