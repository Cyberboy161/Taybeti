package com.nulldata.app.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.nulldata.app.data.entities.LoginInfo;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class LoginDao_Impl implements LoginDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<LoginInfo> __insertionAdapterOfLoginInfo;

  private final SharedSQLiteStatement __preparedStmtOfUpdateFailedAttempts;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public LoginDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfLoginInfo = new EntityInsertionAdapter<LoginInfo>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `login_info` (`id`,`loginSalt`,`loginIv`,`loginCiphertext`,`loginTag`,`decoySalt`,`decoyIv`,`decoyCiphertext`,`decoyTag`,`decoyEnabled`,`failedAttempts`,`lastFailedTime`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LoginInfo entity) {
        statement.bindLong(1, entity.getId());
        statement.bindBlob(2, entity.getLoginSalt());
        statement.bindBlob(3, entity.getLoginIv());
        statement.bindBlob(4, entity.getLoginCiphertext());
        statement.bindBlob(5, entity.getLoginTag());
        if (entity.getDecoySalt() == null) {
          statement.bindNull(6);
        } else {
          statement.bindBlob(6, entity.getDecoySalt());
        }
        if (entity.getDecoyIv() == null) {
          statement.bindNull(7);
        } else {
          statement.bindBlob(7, entity.getDecoyIv());
        }
        if (entity.getDecoyCiphertext() == null) {
          statement.bindNull(8);
        } else {
          statement.bindBlob(8, entity.getDecoyCiphertext());
        }
        if (entity.getDecoyTag() == null) {
          statement.bindNull(9);
        } else {
          statement.bindBlob(9, entity.getDecoyTag());
        }
        final int _tmp = entity.getDecoyEnabled() ? 1 : 0;
        statement.bindLong(10, _tmp);
        statement.bindLong(11, entity.getFailedAttempts());
        statement.bindLong(12, entity.getLastFailedTime());
      }
    };
    this.__preparedStmtOfUpdateFailedAttempts = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE login_info SET failedAttempts = ?, lastFailedTime = ? WHERE id = 1";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM login_info";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final LoginInfo info, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfLoginInfo.insert(info);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateFailedAttempts(final int attempts, final long time,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateFailedAttempts.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, attempts);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, time);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateFailedAttempts.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object get(final Continuation<? super LoginInfo> $completion) {
    final String _sql = "SELECT * FROM login_info WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<LoginInfo>() {
      @Override
      @Nullable
      public LoginInfo call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLoginSalt = CursorUtil.getColumnIndexOrThrow(_cursor, "loginSalt");
          final int _cursorIndexOfLoginIv = CursorUtil.getColumnIndexOrThrow(_cursor, "loginIv");
          final int _cursorIndexOfLoginCiphertext = CursorUtil.getColumnIndexOrThrow(_cursor, "loginCiphertext");
          final int _cursorIndexOfLoginTag = CursorUtil.getColumnIndexOrThrow(_cursor, "loginTag");
          final int _cursorIndexOfDecoySalt = CursorUtil.getColumnIndexOrThrow(_cursor, "decoySalt");
          final int _cursorIndexOfDecoyIv = CursorUtil.getColumnIndexOrThrow(_cursor, "decoyIv");
          final int _cursorIndexOfDecoyCiphertext = CursorUtil.getColumnIndexOrThrow(_cursor, "decoyCiphertext");
          final int _cursorIndexOfDecoyTag = CursorUtil.getColumnIndexOrThrow(_cursor, "decoyTag");
          final int _cursorIndexOfDecoyEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "decoyEnabled");
          final int _cursorIndexOfFailedAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "failedAttempts");
          final int _cursorIndexOfLastFailedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastFailedTime");
          final LoginInfo _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final byte[] _tmpLoginSalt;
            _tmpLoginSalt = _cursor.getBlob(_cursorIndexOfLoginSalt);
            final byte[] _tmpLoginIv;
            _tmpLoginIv = _cursor.getBlob(_cursorIndexOfLoginIv);
            final byte[] _tmpLoginCiphertext;
            _tmpLoginCiphertext = _cursor.getBlob(_cursorIndexOfLoginCiphertext);
            final byte[] _tmpLoginTag;
            _tmpLoginTag = _cursor.getBlob(_cursorIndexOfLoginTag);
            final byte[] _tmpDecoySalt;
            if (_cursor.isNull(_cursorIndexOfDecoySalt)) {
              _tmpDecoySalt = null;
            } else {
              _tmpDecoySalt = _cursor.getBlob(_cursorIndexOfDecoySalt);
            }
            final byte[] _tmpDecoyIv;
            if (_cursor.isNull(_cursorIndexOfDecoyIv)) {
              _tmpDecoyIv = null;
            } else {
              _tmpDecoyIv = _cursor.getBlob(_cursorIndexOfDecoyIv);
            }
            final byte[] _tmpDecoyCiphertext;
            if (_cursor.isNull(_cursorIndexOfDecoyCiphertext)) {
              _tmpDecoyCiphertext = null;
            } else {
              _tmpDecoyCiphertext = _cursor.getBlob(_cursorIndexOfDecoyCiphertext);
            }
            final byte[] _tmpDecoyTag;
            if (_cursor.isNull(_cursorIndexOfDecoyTag)) {
              _tmpDecoyTag = null;
            } else {
              _tmpDecoyTag = _cursor.getBlob(_cursorIndexOfDecoyTag);
            }
            final boolean _tmpDecoyEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfDecoyEnabled);
            _tmpDecoyEnabled = _tmp != 0;
            final int _tmpFailedAttempts;
            _tmpFailedAttempts = _cursor.getInt(_cursorIndexOfFailedAttempts);
            final long _tmpLastFailedTime;
            _tmpLastFailedTime = _cursor.getLong(_cursorIndexOfLastFailedTime);
            _result = new LoginInfo(_tmpId,_tmpLoginSalt,_tmpLoginIv,_tmpLoginCiphertext,_tmpLoginTag,_tmpDecoySalt,_tmpDecoyIv,_tmpDecoyCiphertext,_tmpDecoyTag,_tmpDecoyEnabled,_tmpFailedAttempts,_tmpLastFailedTime);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
