package com.example.offlinechat.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@SuppressWarnings({"unchecked", "deprecation"})
public final class ChatDao_Impl implements ChatDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Conversation> __insertionAdapterOfConversation;

  private final EntityInsertionAdapter<Message> __insertionAdapterOfMessage;

  private final EntityInsertionAdapter<BufferedPacket> __insertionAdapterOfBufferedPacket;

  private final SharedSQLiteStatement __preparedStmtOfUpdateMessageStatus;

  private final SharedSQLiteStatement __preparedStmtOfClearAllMessages;

  private final SharedSQLiteStatement __preparedStmtOfDeleteBufferedPacket;

  private final SharedSQLiteStatement __preparedStmtOfDeleteExpiredBufferedPackets;

  private final SharedSQLiteStatement __preparedStmtOfIncrementRetryCount;

  public ChatDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfConversation = new EntityInsertionAdapter<Conversation>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `conversations` (`id`,`peerId`,`createdAt`,`lastMessageAt`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Conversation entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getPeerId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getPeerId());
        }
        statement.bindLong(3, entity.getCreatedAt());
        statement.bindLong(4, entity.getLastMessageAt());
      }
    };
    this.__insertionAdapterOfMessage = new EntityInsertionAdapter<Message>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `messages` (`id`,`conversationId`,`senderId`,`encryptedPayload`,`timestamp`,`status`,`hopTrace`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Message entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getConversationId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getConversationId());
        }
        if (entity.getSenderId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSenderId());
        }
        if (entity.getEncryptedPayload() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getEncryptedPayload());
        }
        statement.bindLong(5, entity.getTimestamp());
        if (entity.getStatus() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getStatus());
        }
        if (entity.getHopTrace() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getHopTrace());
        }
      }
    };
    this.__insertionAdapterOfBufferedPacket = new EntityInsertionAdapter<BufferedPacket>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `buffered_packets` (`packetId`,`messageId`,`recipientId`,`conversationId`,`priority`,`ttl`,`createdAt`,`expiresAt`,`retryCount`,`rawJsonPayload`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BufferedPacket entity) {
        if (entity.getPacketId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getPacketId());
        }
        if (entity.getMessageId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getMessageId());
        }
        if (entity.getRecipientId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getRecipientId());
        }
        if (entity.getConversationId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getConversationId());
        }
        statement.bindLong(5, entity.getPriority());
        statement.bindLong(6, entity.getTtl());
        statement.bindLong(7, entity.getCreatedAt());
        statement.bindLong(8, entity.getExpiresAt());
        statement.bindLong(9, entity.getRetryCount());
        if (entity.getRawJsonPayload() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getRawJsonPayload());
        }
      }
    };
    this.__preparedStmtOfUpdateMessageStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE messages SET status = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAllMessages = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM messages";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteBufferedPacket = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM buffered_packets WHERE packetId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteExpiredBufferedPackets = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM buffered_packets WHERE expiresAt <= ?";
        return _query;
      }
    };
    this.__preparedStmtOfIncrementRetryCount = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE buffered_packets SET retryCount = retryCount + 1 WHERE packetId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertConversation(final Conversation conversation,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfConversation.insert(conversation);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMessage(final Message message, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMessage.insert(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertBufferedPacket(final BufferedPacket packet,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfBufferedPacket.insert(packet);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMessageStatus(final String messageId, final String status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateMessageStatus.acquire();
        int _argIndex = 1;
        if (status == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, status);
        }
        _argIndex = 2;
        if (messageId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, messageId);
        }
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
          __preparedStmtOfUpdateMessageStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAllMessages(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAllMessages.acquire();
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
          __preparedStmtOfClearAllMessages.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteBufferedPacket(final String packetId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteBufferedPacket.acquire();
        int _argIndex = 1;
        if (packetId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, packetId);
        }
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
          __preparedStmtOfDeleteBufferedPacket.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteExpiredBufferedPackets(final long now,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteExpiredBufferedPackets.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, now);
        try {
          __db.beginTransaction();
          try {
            final Integer _result = _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteExpiredBufferedPackets.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object incrementRetryCount(final String packetId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfIncrementRetryCount.acquire();
        int _argIndex = 1;
        if (packetId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, packetId);
        }
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
          __preparedStmtOfIncrementRetryCount.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Conversation>> getConversations() {
    final String _sql = "SELECT * FROM conversations ORDER BY lastMessageAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"conversations"}, new Callable<List<Conversation>>() {
      @Override
      @NonNull
      public List<Conversation> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPeerId = CursorUtil.getColumnIndexOrThrow(_cursor, "peerId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfLastMessageAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageAt");
          final List<Conversation> _result = new ArrayList<Conversation>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Conversation _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpPeerId;
            if (_cursor.isNull(_cursorIndexOfPeerId)) {
              _tmpPeerId = null;
            } else {
              _tmpPeerId = _cursor.getString(_cursorIndexOfPeerId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpLastMessageAt;
            _tmpLastMessageAt = _cursor.getLong(_cursorIndexOfLastMessageAt);
            _item = new Conversation(_tmpId,_tmpPeerId,_tmpCreatedAt,_tmpLastMessageAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Message>> getMessagesForConversation(final String conversationId) {
    final String _sql = "SELECT * FROM messages WHERE conversationId = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (conversationId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, conversationId);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<List<Message>>() {
      @Override
      @NonNull
      public List<Message> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfEncryptedPayload = CursorUtil.getColumnIndexOrThrow(_cursor, "encryptedPayload");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfHopTrace = CursorUtil.getColumnIndexOrThrow(_cursor, "hopTrace");
          final List<Message> _result = new ArrayList<Message>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Message _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpConversationId;
            if (_cursor.isNull(_cursorIndexOfConversationId)) {
              _tmpConversationId = null;
            } else {
              _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            }
            final String _tmpSenderId;
            if (_cursor.isNull(_cursorIndexOfSenderId)) {
              _tmpSenderId = null;
            } else {
              _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            }
            final String _tmpEncryptedPayload;
            if (_cursor.isNull(_cursorIndexOfEncryptedPayload)) {
              _tmpEncryptedPayload = null;
            } else {
              _tmpEncryptedPayload = _cursor.getString(_cursorIndexOfEncryptedPayload);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpHopTrace;
            if (_cursor.isNull(_cursorIndexOfHopTrace)) {
              _tmpHopTrace = null;
            } else {
              _tmpHopTrace = _cursor.getString(_cursorIndexOfHopTrace);
            }
            _item = new Message(_tmpId,_tmpConversationId,_tmpSenderId,_tmpEncryptedPayload,_tmpTimestamp,_tmpStatus,_tmpHopTrace);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getPendingMessages(final String conversationId,
      final Continuation<? super List<Message>> $completion) {
    final String _sql = "SELECT * FROM messages WHERE conversationId = ? AND status = 'PENDING' ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (conversationId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, conversationId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Message>>() {
      @Override
      @NonNull
      public List<Message> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfEncryptedPayload = CursorUtil.getColumnIndexOrThrow(_cursor, "encryptedPayload");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfHopTrace = CursorUtil.getColumnIndexOrThrow(_cursor, "hopTrace");
          final List<Message> _result = new ArrayList<Message>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Message _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpConversationId;
            if (_cursor.isNull(_cursorIndexOfConversationId)) {
              _tmpConversationId = null;
            } else {
              _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            }
            final String _tmpSenderId;
            if (_cursor.isNull(_cursorIndexOfSenderId)) {
              _tmpSenderId = null;
            } else {
              _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            }
            final String _tmpEncryptedPayload;
            if (_cursor.isNull(_cursorIndexOfEncryptedPayload)) {
              _tmpEncryptedPayload = null;
            } else {
              _tmpEncryptedPayload = _cursor.getString(_cursorIndexOfEncryptedPayload);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpHopTrace;
            if (_cursor.isNull(_cursorIndexOfHopTrace)) {
              _tmpHopTrace = null;
            } else {
              _tmpHopTrace = _cursor.getString(_cursorIndexOfHopTrace);
            }
            _item = new Message(_tmpId,_tmpConversationId,_tmpSenderId,_tmpEncryptedPayload,_tmpTimestamp,_tmpStatus,_tmpHopTrace);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Integer> getTotalMessageCount() {
    final String _sql = "SELECT COUNT(*) FROM messages";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Message>> getAllMessages() {
    final String _sql = "SELECT * FROM messages ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<List<Message>>() {
      @Override
      @NonNull
      public List<Message> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfEncryptedPayload = CursorUtil.getColumnIndexOrThrow(_cursor, "encryptedPayload");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfHopTrace = CursorUtil.getColumnIndexOrThrow(_cursor, "hopTrace");
          final List<Message> _result = new ArrayList<Message>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Message _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpConversationId;
            if (_cursor.isNull(_cursorIndexOfConversationId)) {
              _tmpConversationId = null;
            } else {
              _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            }
            final String _tmpSenderId;
            if (_cursor.isNull(_cursorIndexOfSenderId)) {
              _tmpSenderId = null;
            } else {
              _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            }
            final String _tmpEncryptedPayload;
            if (_cursor.isNull(_cursorIndexOfEncryptedPayload)) {
              _tmpEncryptedPayload = null;
            } else {
              _tmpEncryptedPayload = _cursor.getString(_cursorIndexOfEncryptedPayload);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpHopTrace;
            if (_cursor.isNull(_cursorIndexOfHopTrace)) {
              _tmpHopTrace = null;
            } else {
              _tmpHopTrace = _cursor.getString(_cursorIndexOfHopTrace);
            }
            _item = new Message(_tmpId,_tmpConversationId,_tmpSenderId,_tmpEncryptedPayload,_tmpTimestamp,_tmpStatus,_tmpHopTrace);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getBufferedPacketsForRecipient(final String recipientId,
      final Continuation<? super List<BufferedPacket>> $completion) {
    final String _sql = "SELECT * FROM buffered_packets WHERE recipientId = ? OR recipientId = 'ALL' ORDER BY priority DESC, createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (recipientId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, recipientId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<BufferedPacket>>() {
      @Override
      @NonNull
      public List<BufferedPacket> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPacketId = CursorUtil.getColumnIndexOrThrow(_cursor, "packetId");
          final int _cursorIndexOfMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "messageId");
          final int _cursorIndexOfRecipientId = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientId");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfTtl = CursorUtil.getColumnIndexOrThrow(_cursor, "ttl");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retryCount");
          final int _cursorIndexOfRawJsonPayload = CursorUtil.getColumnIndexOrThrow(_cursor, "rawJsonPayload");
          final List<BufferedPacket> _result = new ArrayList<BufferedPacket>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BufferedPacket _item;
            final String _tmpPacketId;
            if (_cursor.isNull(_cursorIndexOfPacketId)) {
              _tmpPacketId = null;
            } else {
              _tmpPacketId = _cursor.getString(_cursorIndexOfPacketId);
            }
            final String _tmpMessageId;
            if (_cursor.isNull(_cursorIndexOfMessageId)) {
              _tmpMessageId = null;
            } else {
              _tmpMessageId = _cursor.getString(_cursorIndexOfMessageId);
            }
            final String _tmpRecipientId;
            if (_cursor.isNull(_cursorIndexOfRecipientId)) {
              _tmpRecipientId = null;
            } else {
              _tmpRecipientId = _cursor.getString(_cursorIndexOfRecipientId);
            }
            final String _tmpConversationId;
            if (_cursor.isNull(_cursorIndexOfConversationId)) {
              _tmpConversationId = null;
            } else {
              _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            }
            final int _tmpPriority;
            _tmpPriority = _cursor.getInt(_cursorIndexOfPriority);
            final int _tmpTtl;
            _tmpTtl = _cursor.getInt(_cursorIndexOfTtl);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpExpiresAt;
            _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            final int _tmpRetryCount;
            _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
            final String _tmpRawJsonPayload;
            if (_cursor.isNull(_cursorIndexOfRawJsonPayload)) {
              _tmpRawJsonPayload = null;
            } else {
              _tmpRawJsonPayload = _cursor.getString(_cursorIndexOfRawJsonPayload);
            }
            _item = new BufferedPacket(_tmpPacketId,_tmpMessageId,_tmpRecipientId,_tmpConversationId,_tmpPriority,_tmpTtl,_tmpCreatedAt,_tmpExpiresAt,_tmpRetryCount,_tmpRawJsonPayload);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTopBufferedPackets(final long now, final int limit,
      final Continuation<? super List<BufferedPacket>> $completion) {
    final String _sql = "SELECT * FROM buffered_packets WHERE expiresAt > ? ORDER BY priority DESC, createdAt ASC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, now);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<BufferedPacket>>() {
      @Override
      @NonNull
      public List<BufferedPacket> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPacketId = CursorUtil.getColumnIndexOrThrow(_cursor, "packetId");
          final int _cursorIndexOfMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "messageId");
          final int _cursorIndexOfRecipientId = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientId");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfTtl = CursorUtil.getColumnIndexOrThrow(_cursor, "ttl");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retryCount");
          final int _cursorIndexOfRawJsonPayload = CursorUtil.getColumnIndexOrThrow(_cursor, "rawJsonPayload");
          final List<BufferedPacket> _result = new ArrayList<BufferedPacket>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BufferedPacket _item;
            final String _tmpPacketId;
            if (_cursor.isNull(_cursorIndexOfPacketId)) {
              _tmpPacketId = null;
            } else {
              _tmpPacketId = _cursor.getString(_cursorIndexOfPacketId);
            }
            final String _tmpMessageId;
            if (_cursor.isNull(_cursorIndexOfMessageId)) {
              _tmpMessageId = null;
            } else {
              _tmpMessageId = _cursor.getString(_cursorIndexOfMessageId);
            }
            final String _tmpRecipientId;
            if (_cursor.isNull(_cursorIndexOfRecipientId)) {
              _tmpRecipientId = null;
            } else {
              _tmpRecipientId = _cursor.getString(_cursorIndexOfRecipientId);
            }
            final String _tmpConversationId;
            if (_cursor.isNull(_cursorIndexOfConversationId)) {
              _tmpConversationId = null;
            } else {
              _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            }
            final int _tmpPriority;
            _tmpPriority = _cursor.getInt(_cursorIndexOfPriority);
            final int _tmpTtl;
            _tmpTtl = _cursor.getInt(_cursorIndexOfTtl);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpExpiresAt;
            _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            final int _tmpRetryCount;
            _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
            final String _tmpRawJsonPayload;
            if (_cursor.isNull(_cursorIndexOfRawJsonPayload)) {
              _tmpRawJsonPayload = null;
            } else {
              _tmpRawJsonPayload = _cursor.getString(_cursorIndexOfRawJsonPayload);
            }
            _item = new BufferedPacket(_tmpPacketId,_tmpMessageId,_tmpRecipientId,_tmpConversationId,_tmpPriority,_tmpTtl,_tmpCreatedAt,_tmpExpiresAt,_tmpRetryCount,_tmpRawJsonPayload);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Integer> getBufferedPacketCount() {
    final String _sql = "SELECT COUNT(*) FROM buffered_packets";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"buffered_packets"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
