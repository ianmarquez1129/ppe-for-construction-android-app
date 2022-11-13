package com.example.zmci.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import com.example.zmci.camera.CameraData
import java.util.*
import com.example.zmci.model.User
import kotlin.collections.ArrayList

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // create table sql query
    private val CREATE_USER_TABLE = ("CREATE TABLE " + TABLE_USER + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_USER_NAME + " TEXT,"
            + COLUMN_USER_EMAIL + " TEXT," + COLUMN_USER_PASSWORD + " TEXT" + ")")
    private val CREATE_CAMERA_TABLE = ("CREATE TABLE " + TABLE_CAMERA + "("
            + COLUMN_CAMERA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_CAMERA_NAME + " TEXT)")

    // drop table sql query
    private val DROP_USER_TABLE = "DROP TABLE IF EXISTS $TABLE_USER"
    private val DROP_CAMERA_TABLE = "DROP TABLE IF EXISTS $TABLE_CAMERA"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_USER_TABLE)
        db.execSQL(CREATE_CAMERA_TABLE)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        //Drop User Table if exist
        db.execSQL(DROP_USER_TABLE)
        db.execSQL(DROP_CAMERA_TABLE)

        // Create tables again
        onCreate(db)

    }

    /**
     * This method is to fetch all user and return the list of user records
     *
     * @return list
     */
    fun getAllUser(): List<User> {

        // array of columns to fetch
        val columns = arrayOf(COLUMN_USER_ID, COLUMN_USER_EMAIL, COLUMN_USER_NAME, COLUMN_USER_PASSWORD)

        // sorting orders
        val sortOrder = "$COLUMN_USER_NAME ASC"
        val userList = ArrayList<User>()

        val db = this.readableDatabase

        // query the user table
        val cursor = db.query(TABLE_USER, //Table to query
            columns,            //columns to return
            null,     //columns for the WHERE clause
            null,  //The values for the WHERE clause
            null,      //group the rows
            null,       //filter by row groups
            sortOrder)         //The sort order
        if (cursor.moveToFirst()) {
            do {
                val user = User(id = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID)).toInt(),
                    name = cursor.getString(cursor.getColumnIndex(COLUMN_USER_NAME)),
                    email = cursor.getString(cursor.getColumnIndex(COLUMN_USER_EMAIL)),
                    password = cursor.getString(cursor.getColumnIndex(COLUMN_USER_PASSWORD)))

                userList.add(user)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return userList
    }


    /**
     * This method is to create user record
     *
     * @param user
     */
    fun addUser(user: User) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(COLUMN_USER_NAME, user.name)
        values.put(COLUMN_USER_EMAIL, user.email)
        values.put(COLUMN_USER_PASSWORD, user.password)

        // Inserting Row
        db.insert(TABLE_USER, null, values)
        db.close()
    }

    /**
     * This method to update user record
     *
     * @param user
     */
    fun updateUser(user: User) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(COLUMN_USER_NAME, user.name)
        values.put(COLUMN_USER_EMAIL, user.email)
        values.put(COLUMN_USER_PASSWORD, user.password)

        // updating row
        db.update(TABLE_USER, values, "$COLUMN_USER_ID = ?",
            arrayOf(user.id.toString()))
        db.close()
    }

    /**
     * This method is to delete user record
     *
     * @param user
     */
    fun deleteUser(user: User) {

        val db = this.writableDatabase
        // delete user record by id
        db.delete(TABLE_USER, "$COLUMN_USER_ID = ?",
            arrayOf(user.id.toString()))
        db.close()


    }

    /**
     * This method to check user exist or not
     *
     * @param email
     * @return true/false
     */
    fun checkUser(email: String): Boolean {

        // array of columns to fetch
        val columns = arrayOf(COLUMN_USER_ID)
        val db = this.readableDatabase

        // selection criteria
        val selection = "$COLUMN_USER_EMAIL = ?"

        // selection argument
        val selectionArgs = arrayOf(email)

        // query user table with condition
        /**
         * Here query function is used to fetch records from user table this function works like we use sql query.
         * SQL query equivalent to this query function is
         * SELECT user_id FROM user WHERE user_email = 'jack@androidtutorialshub.com';
         */
        val cursor = db.query(TABLE_USER, //Table to query
            columns,        //columns to return
            selection,      //columns for the WHERE clause
            selectionArgs,  //The values for the WHERE clause
            null,  //group the rows
            null,   //filter by row groups
            null)  //The sort order


        val cursorCount = cursor.count
        cursor.close()
        db.close()

        if (cursorCount > 0) {
            return true
        }

        return false
    }

    /**
     * This method to check user exist or not
     *
     * @param email
     * @param password
     * @return true/false
     */
    fun checkUser(email: String, password: String): Boolean {

        // array of columns to fetch
        val columns = arrayOf(COLUMN_USER_ID)

        val db = this.readableDatabase

        // selection criteria
        val selection = "$COLUMN_USER_EMAIL = ? AND $COLUMN_USER_PASSWORD = ?"

        // selection arguments
        val selectionArgs = arrayOf(email, password)

        // query user table with conditions
        /**
         * Here query function is used to fetch records from user table this function works like we use sql query.
         * SQL query equivalent to this query function is
         * SELECT user_id FROM user WHERE user_email = 'jack@androidtutorialshub.com' AND user_password = 'qwerty';
         */
        val cursor = db.query(TABLE_USER, //Table to query
            columns, //columns to return
            selection, //columns for the WHERE clause
            selectionArgs, //The values for the WHERE clause
            null,  //group the rows
            null, //filter by row groups
            null) //The sort order

        val cursorCount = cursor.count
        cursor.close()
        db.close()

        if (cursorCount > 0)
            return true

        return false

    }
    fun getAllCamera(context: Context): ArrayList<CameraData> {

        val qry = "SELECT * FROM $TABLE_CAMERA"
        val db = this.readableDatabase
        val cursor = db.rawQuery(qry, null)
        val cameraList = ArrayList<CameraData>()

        if (cursor.count == 0)
            Toast.makeText(context, "No Records Found", Toast.LENGTH_SHORT).show() else {
                cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val camera = CameraData()
                camera.id = cursor.getInt(cursor.getColumnIndex(COLUMN_CAMERA_ID))
                camera.cameraName = cursor.getString(cursor.getColumnIndex(COLUMN_CAMERA_NAME))
                cameraList.add(camera)
                cursor.moveToNext()
            }
            Toast.makeText(context,"${cursor.count.toString()} Records Found", Toast.LENGTH_SHORT).show()
        }
        cursor.close()
        db.close()
        return cameraList
    }

    /**
     * This method is to create camera record
     *
     * @param camera
     */
    fun addCamera(context: Context, camera: CameraData) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(/* key = */ COLUMN_CAMERA_NAME, /* value = */ camera.cameraName)

        try {
            db.insert(TABLE_CAMERA, null, values)
            Toast.makeText(context, "Camera Added", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
        db.close()
    }

    /**
     * This method to update camera record
     *
     * @param camera
     */
    fun updateCamera(id : String, cameraName: String) : Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        var result : Boolean = false
        values.put(COLUMN_CAMERA_NAME, cameraName)
        try {
            db.update(TABLE_CAMERA, values, "$COLUMN_CAMERA_ID = ?", arrayOf(id))
            result = true
        } catch (e : Exception){
            Log.e(ContentValues.TAG, "Error Updating")
            result = false
        }
        return result
    }

    /**
     * This method is to delete camera record
     *
     * @param camera
     */
    fun deleteCamera(cameraID : Int): Boolean {

        val db = this.writableDatabase
        var result : Boolean = false
        try {
            val cursor = db.delete(TABLE_CAMERA, "$COLUMN_CAMERA_ID = ?",
                arrayOf(cameraID.toString()))
            result = true
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error Deleting")
        }
        db.close()
        return result
    }

    /**
     * This method to check camera exist or not
     *
     * @param camera
     * @return true/false
     */
    fun checkCamera(camera: String): Boolean {

        // array of columns to fetch
        val columns = arrayOf(COLUMN_CAMERA_ID)
        val db = this.readableDatabase

        // selection criteria
        val selection = "$COLUMN_CAMERA_NAME = ?"

        // selection argument
        val selectionArgs = arrayOf(camera)

        // query camera table with condition
        /**
         * Here query function is used to fetch records from camera table this function works like we use sql query.
         * SQL query equivalent to this query function is
         * SELECT camera_id FROM camera WHERE camera_name = 'camera1';
         */
        val cursor = db.query(TABLE_CAMERA, //Table to query
            columns,        //columns to return
            selection,      //columns for the WHERE clause
            selectionArgs,  //The values for the WHERE clause
            null,  //group the rows
            null,   //filter by row groups
            null)  //The sort order


        val cursorCount = cursor.count
        cursor.close()
        db.close()

        if (cursorCount > 0) {
            return true
        }

        return false
    }

    companion object {

        // Database Version
        private val DATABASE_VERSION = 2

        // Database Name
        private val DATABASE_NAME = "UserManager.db"

        // User table name
        private val TABLE_USER = "user"

        // User Table Columns names
        private val COLUMN_USER_ID = "user_id"
        private val COLUMN_USER_NAME = "user_name"
        private val COLUMN_USER_EMAIL = "user_email"
        private val COLUMN_USER_PASSWORD = "user_password"

        // Camera table name
        private val TABLE_CAMERA = "camera"

        // Camera Table Columns names
        private val COLUMN_CAMERA_ID = "camera_id"
        private val COLUMN_CAMERA_NAME = "camera_name"
    }
}