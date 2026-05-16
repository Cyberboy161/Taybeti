package com.nulldata.app.data.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.nulldata.app.data.dao.LoginDao;
import com.nulldata.app.data.dao.LoginDao_Impl;
import com.nulldata.app.data.dao.NoteDao;
import com.nulldata.app.data.dao.NoteDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile LoginDao _loginDao;

  private volatile NoteDao _noteDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `login_info` (`id` INTEGER NOT NULL, `loginSalt` BLOB NOT NULL, `loginIv` BLOB NOT NULL, `loginCiphertext` BLOB NOT NULL, `loginTag` BLOB NOT NULL, `decoySalt` BLOB, `decoyIv` BLOB, `decoyCiphertext` BLOB, `decoyTag` BLOB, `decoyEnabled` INTEGER NOT NULL, `failedAttempts` INTEGER NOT NULL, `lastFailedTime` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `notes` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `salt` BLOB NOT NULL, `iv` BLOB NOT NULL, `ciphertext` BLOB NOT NULL, `tag` BLOB NOT NULL, `isEncrypted` INTEGER NOT NULL, `isFavorite` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, `isDecoyNote` INTEGER NOT NULL, `createdDate` INTEGER NOT NULL, `modifiedDate` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'b49fda3ada77555c3d9156f68f8d29b0')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `login_info`");
        db.execSQL("DROP TABLE IF EXISTS `notes`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsLoginInfo = new HashMap<String, TableInfo.Column>(12);
        _columnsLoginInfo.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoginInfo.put("loginSalt", new TableInfo.Column("loginSalt", "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoginInfo.put("loginIv", new TableInfo.Column("loginIv", "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoginInfo.put("loginCiphertext", new TableInfo.Column("loginCiphertext", "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoginInfo.put("loginTag", new TableInfo.Column("loginTag", "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoginInfo.put("decoySalt", new TableInfo.Column("decoySalt", "BLOB", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoginInfo.put("decoyIv", new TableInfo.Column("decoyIv", "BLOB", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoginInfo.put("decoyCiphertext", new TableInfo.Column("decoyCiphertext", "BLOB", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoginInfo.put("decoyTag", new TableInfo.Column("decoyTag", "BLOB", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoginInfo.put("decoyEnabled", new TableInfo.Column("decoyEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoginInfo.put("failedAttempts", new TableInfo.Column("failedAttempts", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoginInfo.put("lastFailedTime", new TableInfo.Column("lastFailedTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLoginInfo = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLoginInfo = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLoginInfo = new TableInfo("login_info", _columnsLoginInfo, _foreignKeysLoginInfo, _indicesLoginInfo);
        final TableInfo _existingLoginInfo = TableInfo.read(db, "login_info");
        if (!_infoLoginInfo.equals(_existingLoginInfo)) {
          return new RoomOpenHelper.ValidationResult(false, "login_info(com.nulldata.app.data.entities.LoginInfo).\n"
                  + " Expected:\n" + _infoLoginInfo + "\n"
                  + " Found:\n" + _existingLoginInfo);
        }
        final HashMap<String, TableInfo.Column> _columnsNotes = new HashMap<String, TableInfo.Column>(12);
        _columnsNotes.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("salt", new TableInfo.Column("salt", "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("iv", new TableInfo.Column("iv", "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("ciphertext", new TableInfo.Column("ciphertext", "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("tag", new TableInfo.Column("tag", "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("isEncrypted", new TableInfo.Column("isEncrypted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("isFavorite", new TableInfo.Column("isFavorite", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("isDecoyNote", new TableInfo.Column("isDecoyNote", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("createdDate", new TableInfo.Column("createdDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("modifiedDate", new TableInfo.Column("modifiedDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysNotes = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesNotes = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoNotes = new TableInfo("notes", _columnsNotes, _foreignKeysNotes, _indicesNotes);
        final TableInfo _existingNotes = TableInfo.read(db, "notes");
        if (!_infoNotes.equals(_existingNotes)) {
          return new RoomOpenHelper.ValidationResult(false, "notes(com.nulldata.app.data.entities.NoteEntity).\n"
                  + " Expected:\n" + _infoNotes + "\n"
                  + " Found:\n" + _existingNotes);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "b49fda3ada77555c3d9156f68f8d29b0", "68b533a0e78b1004a4c34d197782c36a");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "login_info","notes");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `login_info`");
      _db.execSQL("DELETE FROM `notes`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(LoginDao.class, LoginDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(NoteDao.class, NoteDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public LoginDao loginDao() {
    if (_loginDao != null) {
      return _loginDao;
    } else {
      synchronized(this) {
        if(_loginDao == null) {
          _loginDao = new LoginDao_Impl(this);
        }
        return _loginDao;
      }
    }
  }

  @Override
  public NoteDao noteDao() {
    if (_noteDao != null) {
      return _noteDao;
    } else {
      synchronized(this) {
        if(_noteDao == null) {
          _noteDao = new NoteDao_Impl(this);
        }
        return _noteDao;
      }
    }
  }
}
