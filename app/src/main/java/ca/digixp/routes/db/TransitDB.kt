package ca.digixp.routes.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import ca.digixp.routes.util.SqliteDatabaseOpenFlag
import ca.digixp.routes.util.query
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.FileOutputStream
import javax.inject.Inject


private const val VERSION = 1

class TransitDB @Inject constructor(
  @ApplicationContext private val context: Context,
  private val userDB: UserDB,
  city: String
) :
  SQLiteOpenHelper(context, "$city.sqlite", null, VERSION) {

  private val dbName = "$city.sqlite"
  private val dbPath = "databases/$dbName"

  private val isDatabaseOpen
    get() = database?.isOpen ?: false
  private var database: SQLiteDatabase? = null

  private var isInitializing = false

  override fun onCreate(db: SQLiteDatabase?) {
  }

  override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    TODO("Not yet implemented")
  }

  override fun getWritableDatabase(): SQLiteDatabase {
    if (isDatabaseOpen && database?.isReadOnly == false) {
      return this.database!!
    }

    if (isInitializing) {
      throw IllegalStateException("getWritableDatabase called recursively")
    }

    var isSuccessful = false
    var database: SQLiteDatabase? = null
    try {
      isInitializing = true

      database = createOrOpenDatabase(SqliteDatabaseOpenFlag.READWRITE)
      val currentVersion = database.version
      Log.d("TransitDB", "current database version: $currentVersion")

      if (currentVersion != VERSION) {
        initializeDatabase()
        database.version = VERSION
      }
      onOpen(database)
      isSuccessful = true
      return database
    } finally {
      isInitializing = false
      if (isSuccessful) {
        try {
          // close old instance of database
          this.database?.close()
        } catch (e: Exception) {
          Log.e("TransitDB", "Failed to close old instance of TransitDB", e)
        }
        this.database = database
        attachUserDb()
      } else {
        database?.close()
      }
    }
  }

  override fun getReadableDatabase(): SQLiteDatabase {
    if (isDatabaseOpen) {
      return this.database!!
    }

    if (isInitializing) {
      throw IllegalStateException("getReadableDatabase called recursively")
    }

    try {
      return writableDatabase
    } catch (e: Exception) {
      Log.e("TransitDB", "Couldn't open $dbName for writing (will try read-only):", e)
    }

    var database: SQLiteDatabase? = null
    try {
      isInitializing = true
      database = createOrOpenDatabase(SqliteDatabaseOpenFlag.READONLY)
      Log.d("TransitDB", "current database version: ${database.version}")
      if (database.version != VERSION) {
        initializeDatabase()
      }

      onOpen(database)
      Log.w("TransitDB", "Opened $dbName in read-only mode")

      this.database = database
      attachUserDb()
      return this.database!!
    } finally {
      isInitializing = false
      if (this.database != database) {
        database?.close()
      }
    }
  }

  private fun attachUserDb() {
    /**
     * database needs to be created before attaching
     * and the only way to get it to create is by querying the database
     * and trigger onCreate in UserDB
     */
    userDB.readableDatabase.query(TABLE_FAVOURITE_TYPE_NAME, limit = 1).close()

    val userDbPath = context.getDatabasePath("db.sqlite").absolutePath
    this.database?.execSQL("ATTACH DATABASE '$userDbPath' AS userDb;")
  }

  private fun initializeDatabase() {
    context.resources.assets.open(dbPath).use { dbStream ->
      val dbFile = context.getDatabasePath(dbName)
      Log.d("TransitDB", "database init: ${dbFile.absolutePath}")
      FileOutputStream(dbFile).use { outputStream ->
        dbStream.copyTo(outputStream)
      }
    }
  }

  private fun createOrOpenDatabase(databaseOpenFlag: SqliteDatabaseOpenFlag): SQLiteDatabase {
    var db: SQLiteDatabase? = null
    val file = context.getDatabasePath(dbName)
    if (file.exists()) {
      db = loadExistingDatabase(databaseOpenFlag)
    }

    if (db != null) {
      return db
    }

    initializeDatabase()
    return loadExistingDatabase(databaseOpenFlag)!!
  }

  private fun loadExistingDatabase(openFlag: SqliteDatabaseOpenFlag): SQLiteDatabase? {
    return try {
      val db = SQLiteDatabase.openDatabase(
        context.getDatabasePath(dbName).absolutePath,
        null,
        openFlag.ordinal
      )
      Log.i("TransitDB", "successfully opened database $dbName")
      db
    } catch (e: SQLiteException) {
      Log.e("TransitDB", "could not open database $dbName", e)
      null
    }
  }
}