package com.example.offlinechat.data;

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
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
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

  private final EntityInsertionAdapter<DtnBundleEntity> __insertionAdapterOfDtnBundleEntity;

  private final EntityInsertionAdapter<PeerEncounterEntity> __insertionAdapterOfPeerEncounterEntity;

  private final EntityInsertionAdapter<CrdtOperationEntity> __insertionAdapterOfCrdtOperationEntity;

  private final EntityInsertionAdapter<NetworkEpochEntity> __insertionAdapterOfNetworkEpochEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateMessageStatus;

  private final SharedSQLiteStatement __preparedStmtOfClearAllMessages;

  private final SharedSQLiteStatement __preparedStmtOfDeleteBufferedPacket;

  private final SharedSQLiteStatement __preparedStmtOfDeleteExpiredBufferedPackets;

  private final SharedSQLiteStatement __preparedStmtOfIncrementRetryCount;

  private final SharedSQLiteStatement __preparedStmtOfUpdateDtnBundleCustodyState;

  private final SharedSQLiteStatement __preparedStmtOfDeleteDtnBundle;

  private final SharedSQLiteStatement __preparedStmtOfDeleteExpiredDtnBundles;

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
    this.__insertionAdapterOfDtnBundleEntity = new EntityInsertionAdapter<DtnBundleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `dtn_bundles` (`bundleId`,`messageId`,`source`,`destination`,`creationTime`,`expirationTime`,`ttl`,`priority`,`hopCount`,`replicationCount`,`maxReplications`,`payload`,`payloadHash`,`custodyState`,`deliveryProbability`,`sizeBytes`,`rawJson`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DtnBundleEntity entity) {
        if (entity.getBundleId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getBundleId());
        }
        if (entity.getMessageId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getMessageId());
        }
        if (entity.getSource() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSource());
        }
        if (entity.getDestination() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDestination());
        }
        statement.bindLong(5, entity.getCreationTime());
        statement.bindLong(6, entity.getExpirationTime());
        statement.bindLong(7, entity.getTtl());
        statement.bindLong(8, entity.getPriority());
        statement.bindLong(9, entity.getHopCount());
        statement.bindLong(10, entity.getReplicationCount());
        statement.bindLong(11, entity.getMaxReplications());
        if (entity.getPayload() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getPayload());
        }
        if (entity.getPayloadHash() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getPayloadHash());
        }
        if (entity.getCustodyState() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getCustodyState());
        }
        statement.bindDouble(15, entity.getDeliveryProbability());
        statement.bindLong(16, entity.getSizeBytes());
        if (entity.getRawJson() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getRawJson());
        }
      }
    };
    this.__insertionAdapterOfPeerEncounterEntity = new EntityInsertionAdapter<PeerEncounterEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `peer_encounters` (`peerId`,`firstSeen`,`lastSeen`,`encounterCount`,`averageIntervalSeconds`,`lastTransport`,`estimatedStability`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PeerEncounterEntity entity) {
        if (entity.getPeerId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getPeerId());
        }
        statement.bindLong(2, entity.getFirstSeen());
        statement.bindLong(3, entity.getLastSeen());
        statement.bindLong(4, entity.getEncounterCount());
        statement.bindLong(5, entity.getAverageIntervalSeconds());
        if (entity.getLastTransport() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getLastTransport());
        }
        statement.bindDouble(7, entity.getEstimatedStability());
      }
    };
    this.__insertionAdapterOfCrdtOperationEntity = new EntityInsertionAdapter<CrdtOperationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `crdt_operations` (`opId`,`documentId`,`actorId`,`lamportClock`,`timestamp`,`operationType`,`key`,`valueJson`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CrdtOperationEntity entity) {
        if (entity.getOpId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getOpId());
        }
        if (entity.getDocumentId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getDocumentId());
        }
        if (entity.getActorId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getActorId());
        }
        statement.bindLong(4, entity.getLamportClock());
        statement.bindLong(5, entity.getTimestamp());
        if (entity.getOperationType() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getOperationType());
        }
        if (entity.getKey() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getKey());
        }
        if (entity.getValueJson() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getValueJson());
        }
      }
    };
    this.__insertionAdapterOfNetworkEpochEntity = new EntityInsertionAdapter<NetworkEpochEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `network_epochs` (`epochNumber`,`timestamp`,`detectedPartitionCount`,`knownMemberCount`,`stateHash`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final NetworkEpochEntity entity) {
        statement.bindLong(1, entity.getEpochNumber());
        statement.bindLong(2, entity.getTimestamp());
        statement.bindLong(3, entity.getDetectedPartitionCount());
        statement.bindLong(4, entity.getKnownMemberCount());
        if (entity.getStateHash() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getStateHash());
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
    this.__preparedStmtOfUpdateDtnBundleCustodyState = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE dtn_bundles SET custodyState = ? WHERE bundleId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteDtnBundle = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM dtn_bundles WHERE bundleId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteExpiredDtnBundles = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM dtn_bundles WHERE expirationTime <= ?";
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
  public Object insertDtnBundle(final DtnBundleEntity bundle,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDtnBundleEntity.insert(bundle);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertOrUpdatePeerEncounter(final PeerEncounterEntity encounter,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPeerEncounterEntity.insert(encounter);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertCrdtOperation(final CrdtOperationEntity op,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCrdtOperationEntity.insert(op);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertNetworkEpoch(final NetworkEpochEntity epoch,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfNetworkEpochEntity.insert(epoch);
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
  public Object updateDtnBundleCustodyState(final String bundleId, final String state,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateDtnBundleCustodyState.acquire();
        int _argIndex = 1;
        if (state == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, state);
        }
        _argIndex = 2;
        if (bundleId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, bundleId);
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
          __preparedStmtOfUpdateDtnBundleCustodyState.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteDtnBundle(final String bundleId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteDtnBundle.acquire();
        int _argIndex = 1;
        if (bundleId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, bundleId);
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
          __preparedStmtOfDeleteDtnBundle.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteExpiredDtnBundles(final long now,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteExpiredDtnBundles.acquire();
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
          __preparedStmtOfDeleteExpiredDtnBundles.release(_stmt);
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

  @Override
  public Object getDtnBundlesForDestination(final String destination,
      final Continuation<? super List<DtnBundleEntity>> $completion) {
    final String _sql = "SELECT * FROM dtn_bundles WHERE destination = ? OR destination = 'ALL' ORDER BY priority DESC, creationTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (destination == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, destination);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DtnBundleEntity>>() {
      @Override
      @NonNull
      public List<DtnBundleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfBundleId = CursorUtil.getColumnIndexOrThrow(_cursor, "bundleId");
          final int _cursorIndexOfMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "messageId");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfDestination = CursorUtil.getColumnIndexOrThrow(_cursor, "destination");
          final int _cursorIndexOfCreationTime = CursorUtil.getColumnIndexOrThrow(_cursor, "creationTime");
          final int _cursorIndexOfExpirationTime = CursorUtil.getColumnIndexOrThrow(_cursor, "expirationTime");
          final int _cursorIndexOfTtl = CursorUtil.getColumnIndexOrThrow(_cursor, "ttl");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "hopCount");
          final int _cursorIndexOfReplicationCount = CursorUtil.getColumnIndexOrThrow(_cursor, "replicationCount");
          final int _cursorIndexOfMaxReplications = CursorUtil.getColumnIndexOrThrow(_cursor, "maxReplications");
          final int _cursorIndexOfPayload = CursorUtil.getColumnIndexOrThrow(_cursor, "payload");
          final int _cursorIndexOfPayloadHash = CursorUtil.getColumnIndexOrThrow(_cursor, "payloadHash");
          final int _cursorIndexOfCustodyState = CursorUtil.getColumnIndexOrThrow(_cursor, "custodyState");
          final int _cursorIndexOfDeliveryProbability = CursorUtil.getColumnIndexOrThrow(_cursor, "deliveryProbability");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfRawJson = CursorUtil.getColumnIndexOrThrow(_cursor, "rawJson");
          final List<DtnBundleEntity> _result = new ArrayList<DtnBundleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DtnBundleEntity _item;
            final String _tmpBundleId;
            if (_cursor.isNull(_cursorIndexOfBundleId)) {
              _tmpBundleId = null;
            } else {
              _tmpBundleId = _cursor.getString(_cursorIndexOfBundleId);
            }
            final String _tmpMessageId;
            if (_cursor.isNull(_cursorIndexOfMessageId)) {
              _tmpMessageId = null;
            } else {
              _tmpMessageId = _cursor.getString(_cursorIndexOfMessageId);
            }
            final String _tmpSource;
            if (_cursor.isNull(_cursorIndexOfSource)) {
              _tmpSource = null;
            } else {
              _tmpSource = _cursor.getString(_cursorIndexOfSource);
            }
            final String _tmpDestination;
            if (_cursor.isNull(_cursorIndexOfDestination)) {
              _tmpDestination = null;
            } else {
              _tmpDestination = _cursor.getString(_cursorIndexOfDestination);
            }
            final long _tmpCreationTime;
            _tmpCreationTime = _cursor.getLong(_cursorIndexOfCreationTime);
            final long _tmpExpirationTime;
            _tmpExpirationTime = _cursor.getLong(_cursorIndexOfExpirationTime);
            final int _tmpTtl;
            _tmpTtl = _cursor.getInt(_cursorIndexOfTtl);
            final int _tmpPriority;
            _tmpPriority = _cursor.getInt(_cursorIndexOfPriority);
            final int _tmpHopCount;
            _tmpHopCount = _cursor.getInt(_cursorIndexOfHopCount);
            final int _tmpReplicationCount;
            _tmpReplicationCount = _cursor.getInt(_cursorIndexOfReplicationCount);
            final int _tmpMaxReplications;
            _tmpMaxReplications = _cursor.getInt(_cursorIndexOfMaxReplications);
            final String _tmpPayload;
            if (_cursor.isNull(_cursorIndexOfPayload)) {
              _tmpPayload = null;
            } else {
              _tmpPayload = _cursor.getString(_cursorIndexOfPayload);
            }
            final String _tmpPayloadHash;
            if (_cursor.isNull(_cursorIndexOfPayloadHash)) {
              _tmpPayloadHash = null;
            } else {
              _tmpPayloadHash = _cursor.getString(_cursorIndexOfPayloadHash);
            }
            final String _tmpCustodyState;
            if (_cursor.isNull(_cursorIndexOfCustodyState)) {
              _tmpCustodyState = null;
            } else {
              _tmpCustodyState = _cursor.getString(_cursorIndexOfCustodyState);
            }
            final float _tmpDeliveryProbability;
            _tmpDeliveryProbability = _cursor.getFloat(_cursorIndexOfDeliveryProbability);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final String _tmpRawJson;
            if (_cursor.isNull(_cursorIndexOfRawJson)) {
              _tmpRawJson = null;
            } else {
              _tmpRawJson = _cursor.getString(_cursorIndexOfRawJson);
            }
            _item = new DtnBundleEntity(_tmpBundleId,_tmpMessageId,_tmpSource,_tmpDestination,_tmpCreationTime,_tmpExpirationTime,_tmpTtl,_tmpPriority,_tmpHopCount,_tmpReplicationCount,_tmpMaxReplications,_tmpPayload,_tmpPayloadHash,_tmpCustodyState,_tmpDeliveryProbability,_tmpSizeBytes,_tmpRawJson);
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
  public Object getDtnBundlesByState(final String state,
      final Continuation<? super List<DtnBundleEntity>> $completion) {
    final String _sql = "SELECT * FROM dtn_bundles WHERE custodyState = ? ORDER BY priority DESC, creationTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (state == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, state);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DtnBundleEntity>>() {
      @Override
      @NonNull
      public List<DtnBundleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfBundleId = CursorUtil.getColumnIndexOrThrow(_cursor, "bundleId");
          final int _cursorIndexOfMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "messageId");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfDestination = CursorUtil.getColumnIndexOrThrow(_cursor, "destination");
          final int _cursorIndexOfCreationTime = CursorUtil.getColumnIndexOrThrow(_cursor, "creationTime");
          final int _cursorIndexOfExpirationTime = CursorUtil.getColumnIndexOrThrow(_cursor, "expirationTime");
          final int _cursorIndexOfTtl = CursorUtil.getColumnIndexOrThrow(_cursor, "ttl");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "hopCount");
          final int _cursorIndexOfReplicationCount = CursorUtil.getColumnIndexOrThrow(_cursor, "replicationCount");
          final int _cursorIndexOfMaxReplications = CursorUtil.getColumnIndexOrThrow(_cursor, "maxReplications");
          final int _cursorIndexOfPayload = CursorUtil.getColumnIndexOrThrow(_cursor, "payload");
          final int _cursorIndexOfPayloadHash = CursorUtil.getColumnIndexOrThrow(_cursor, "payloadHash");
          final int _cursorIndexOfCustodyState = CursorUtil.getColumnIndexOrThrow(_cursor, "custodyState");
          final int _cursorIndexOfDeliveryProbability = CursorUtil.getColumnIndexOrThrow(_cursor, "deliveryProbability");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfRawJson = CursorUtil.getColumnIndexOrThrow(_cursor, "rawJson");
          final List<DtnBundleEntity> _result = new ArrayList<DtnBundleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DtnBundleEntity _item;
            final String _tmpBundleId;
            if (_cursor.isNull(_cursorIndexOfBundleId)) {
              _tmpBundleId = null;
            } else {
              _tmpBundleId = _cursor.getString(_cursorIndexOfBundleId);
            }
            final String _tmpMessageId;
            if (_cursor.isNull(_cursorIndexOfMessageId)) {
              _tmpMessageId = null;
            } else {
              _tmpMessageId = _cursor.getString(_cursorIndexOfMessageId);
            }
            final String _tmpSource;
            if (_cursor.isNull(_cursorIndexOfSource)) {
              _tmpSource = null;
            } else {
              _tmpSource = _cursor.getString(_cursorIndexOfSource);
            }
            final String _tmpDestination;
            if (_cursor.isNull(_cursorIndexOfDestination)) {
              _tmpDestination = null;
            } else {
              _tmpDestination = _cursor.getString(_cursorIndexOfDestination);
            }
            final long _tmpCreationTime;
            _tmpCreationTime = _cursor.getLong(_cursorIndexOfCreationTime);
            final long _tmpExpirationTime;
            _tmpExpirationTime = _cursor.getLong(_cursorIndexOfExpirationTime);
            final int _tmpTtl;
            _tmpTtl = _cursor.getInt(_cursorIndexOfTtl);
            final int _tmpPriority;
            _tmpPriority = _cursor.getInt(_cursorIndexOfPriority);
            final int _tmpHopCount;
            _tmpHopCount = _cursor.getInt(_cursorIndexOfHopCount);
            final int _tmpReplicationCount;
            _tmpReplicationCount = _cursor.getInt(_cursorIndexOfReplicationCount);
            final int _tmpMaxReplications;
            _tmpMaxReplications = _cursor.getInt(_cursorIndexOfMaxReplications);
            final String _tmpPayload;
            if (_cursor.isNull(_cursorIndexOfPayload)) {
              _tmpPayload = null;
            } else {
              _tmpPayload = _cursor.getString(_cursorIndexOfPayload);
            }
            final String _tmpPayloadHash;
            if (_cursor.isNull(_cursorIndexOfPayloadHash)) {
              _tmpPayloadHash = null;
            } else {
              _tmpPayloadHash = _cursor.getString(_cursorIndexOfPayloadHash);
            }
            final String _tmpCustodyState;
            if (_cursor.isNull(_cursorIndexOfCustodyState)) {
              _tmpCustodyState = null;
            } else {
              _tmpCustodyState = _cursor.getString(_cursorIndexOfCustodyState);
            }
            final float _tmpDeliveryProbability;
            _tmpDeliveryProbability = _cursor.getFloat(_cursorIndexOfDeliveryProbability);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final String _tmpRawJson;
            if (_cursor.isNull(_cursorIndexOfRawJson)) {
              _tmpRawJson = null;
            } else {
              _tmpRawJson = _cursor.getString(_cursorIndexOfRawJson);
            }
            _item = new DtnBundleEntity(_tmpBundleId,_tmpMessageId,_tmpSource,_tmpDestination,_tmpCreationTime,_tmpExpirationTime,_tmpTtl,_tmpPriority,_tmpHopCount,_tmpReplicationCount,_tmpMaxReplications,_tmpPayload,_tmpPayloadHash,_tmpCustodyState,_tmpDeliveryProbability,_tmpSizeBytes,_tmpRawJson);
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
  public Object getActiveDtnBundles(final long now,
      final Continuation<? super List<DtnBundleEntity>> $completion) {
    final String _sql = "SELECT * FROM dtn_bundles WHERE expirationTime > ? ORDER BY priority DESC, creationTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, now);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DtnBundleEntity>>() {
      @Override
      @NonNull
      public List<DtnBundleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfBundleId = CursorUtil.getColumnIndexOrThrow(_cursor, "bundleId");
          final int _cursorIndexOfMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "messageId");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfDestination = CursorUtil.getColumnIndexOrThrow(_cursor, "destination");
          final int _cursorIndexOfCreationTime = CursorUtil.getColumnIndexOrThrow(_cursor, "creationTime");
          final int _cursorIndexOfExpirationTime = CursorUtil.getColumnIndexOrThrow(_cursor, "expirationTime");
          final int _cursorIndexOfTtl = CursorUtil.getColumnIndexOrThrow(_cursor, "ttl");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "hopCount");
          final int _cursorIndexOfReplicationCount = CursorUtil.getColumnIndexOrThrow(_cursor, "replicationCount");
          final int _cursorIndexOfMaxReplications = CursorUtil.getColumnIndexOrThrow(_cursor, "maxReplications");
          final int _cursorIndexOfPayload = CursorUtil.getColumnIndexOrThrow(_cursor, "payload");
          final int _cursorIndexOfPayloadHash = CursorUtil.getColumnIndexOrThrow(_cursor, "payloadHash");
          final int _cursorIndexOfCustodyState = CursorUtil.getColumnIndexOrThrow(_cursor, "custodyState");
          final int _cursorIndexOfDeliveryProbability = CursorUtil.getColumnIndexOrThrow(_cursor, "deliveryProbability");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfRawJson = CursorUtil.getColumnIndexOrThrow(_cursor, "rawJson");
          final List<DtnBundleEntity> _result = new ArrayList<DtnBundleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DtnBundleEntity _item;
            final String _tmpBundleId;
            if (_cursor.isNull(_cursorIndexOfBundleId)) {
              _tmpBundleId = null;
            } else {
              _tmpBundleId = _cursor.getString(_cursorIndexOfBundleId);
            }
            final String _tmpMessageId;
            if (_cursor.isNull(_cursorIndexOfMessageId)) {
              _tmpMessageId = null;
            } else {
              _tmpMessageId = _cursor.getString(_cursorIndexOfMessageId);
            }
            final String _tmpSource;
            if (_cursor.isNull(_cursorIndexOfSource)) {
              _tmpSource = null;
            } else {
              _tmpSource = _cursor.getString(_cursorIndexOfSource);
            }
            final String _tmpDestination;
            if (_cursor.isNull(_cursorIndexOfDestination)) {
              _tmpDestination = null;
            } else {
              _tmpDestination = _cursor.getString(_cursorIndexOfDestination);
            }
            final long _tmpCreationTime;
            _tmpCreationTime = _cursor.getLong(_cursorIndexOfCreationTime);
            final long _tmpExpirationTime;
            _tmpExpirationTime = _cursor.getLong(_cursorIndexOfExpirationTime);
            final int _tmpTtl;
            _tmpTtl = _cursor.getInt(_cursorIndexOfTtl);
            final int _tmpPriority;
            _tmpPriority = _cursor.getInt(_cursorIndexOfPriority);
            final int _tmpHopCount;
            _tmpHopCount = _cursor.getInt(_cursorIndexOfHopCount);
            final int _tmpReplicationCount;
            _tmpReplicationCount = _cursor.getInt(_cursorIndexOfReplicationCount);
            final int _tmpMaxReplications;
            _tmpMaxReplications = _cursor.getInt(_cursorIndexOfMaxReplications);
            final String _tmpPayload;
            if (_cursor.isNull(_cursorIndexOfPayload)) {
              _tmpPayload = null;
            } else {
              _tmpPayload = _cursor.getString(_cursorIndexOfPayload);
            }
            final String _tmpPayloadHash;
            if (_cursor.isNull(_cursorIndexOfPayloadHash)) {
              _tmpPayloadHash = null;
            } else {
              _tmpPayloadHash = _cursor.getString(_cursorIndexOfPayloadHash);
            }
            final String _tmpCustodyState;
            if (_cursor.isNull(_cursorIndexOfCustodyState)) {
              _tmpCustodyState = null;
            } else {
              _tmpCustodyState = _cursor.getString(_cursorIndexOfCustodyState);
            }
            final float _tmpDeliveryProbability;
            _tmpDeliveryProbability = _cursor.getFloat(_cursorIndexOfDeliveryProbability);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final String _tmpRawJson;
            if (_cursor.isNull(_cursorIndexOfRawJson)) {
              _tmpRawJson = null;
            } else {
              _tmpRawJson = _cursor.getString(_cursorIndexOfRawJson);
            }
            _item = new DtnBundleEntity(_tmpBundleId,_tmpMessageId,_tmpSource,_tmpDestination,_tmpCreationTime,_tmpExpirationTime,_tmpTtl,_tmpPriority,_tmpHopCount,_tmpReplicationCount,_tmpMaxReplications,_tmpPayload,_tmpPayloadHash,_tmpCustodyState,_tmpDeliveryProbability,_tmpSizeBytes,_tmpRawJson);
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
  public Flow<Long> getTotalDtnStorageBytes() {
    final String _sql = "SELECT COALESCE(SUM(sizeBytes), 0) FROM dtn_bundles";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"dtn_bundles"}, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
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
  public Object getEvictionCandidates(final int limit,
      final Continuation<? super List<DtnBundleEntity>> $completion) {
    final String _sql = "SELECT * FROM dtn_bundles ORDER BY priority ASC, creationTime ASC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DtnBundleEntity>>() {
      @Override
      @NonNull
      public List<DtnBundleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfBundleId = CursorUtil.getColumnIndexOrThrow(_cursor, "bundleId");
          final int _cursorIndexOfMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "messageId");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfDestination = CursorUtil.getColumnIndexOrThrow(_cursor, "destination");
          final int _cursorIndexOfCreationTime = CursorUtil.getColumnIndexOrThrow(_cursor, "creationTime");
          final int _cursorIndexOfExpirationTime = CursorUtil.getColumnIndexOrThrow(_cursor, "expirationTime");
          final int _cursorIndexOfTtl = CursorUtil.getColumnIndexOrThrow(_cursor, "ttl");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "hopCount");
          final int _cursorIndexOfReplicationCount = CursorUtil.getColumnIndexOrThrow(_cursor, "replicationCount");
          final int _cursorIndexOfMaxReplications = CursorUtil.getColumnIndexOrThrow(_cursor, "maxReplications");
          final int _cursorIndexOfPayload = CursorUtil.getColumnIndexOrThrow(_cursor, "payload");
          final int _cursorIndexOfPayloadHash = CursorUtil.getColumnIndexOrThrow(_cursor, "payloadHash");
          final int _cursorIndexOfCustodyState = CursorUtil.getColumnIndexOrThrow(_cursor, "custodyState");
          final int _cursorIndexOfDeliveryProbability = CursorUtil.getColumnIndexOrThrow(_cursor, "deliveryProbability");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfRawJson = CursorUtil.getColumnIndexOrThrow(_cursor, "rawJson");
          final List<DtnBundleEntity> _result = new ArrayList<DtnBundleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DtnBundleEntity _item;
            final String _tmpBundleId;
            if (_cursor.isNull(_cursorIndexOfBundleId)) {
              _tmpBundleId = null;
            } else {
              _tmpBundleId = _cursor.getString(_cursorIndexOfBundleId);
            }
            final String _tmpMessageId;
            if (_cursor.isNull(_cursorIndexOfMessageId)) {
              _tmpMessageId = null;
            } else {
              _tmpMessageId = _cursor.getString(_cursorIndexOfMessageId);
            }
            final String _tmpSource;
            if (_cursor.isNull(_cursorIndexOfSource)) {
              _tmpSource = null;
            } else {
              _tmpSource = _cursor.getString(_cursorIndexOfSource);
            }
            final String _tmpDestination;
            if (_cursor.isNull(_cursorIndexOfDestination)) {
              _tmpDestination = null;
            } else {
              _tmpDestination = _cursor.getString(_cursorIndexOfDestination);
            }
            final long _tmpCreationTime;
            _tmpCreationTime = _cursor.getLong(_cursorIndexOfCreationTime);
            final long _tmpExpirationTime;
            _tmpExpirationTime = _cursor.getLong(_cursorIndexOfExpirationTime);
            final int _tmpTtl;
            _tmpTtl = _cursor.getInt(_cursorIndexOfTtl);
            final int _tmpPriority;
            _tmpPriority = _cursor.getInt(_cursorIndexOfPriority);
            final int _tmpHopCount;
            _tmpHopCount = _cursor.getInt(_cursorIndexOfHopCount);
            final int _tmpReplicationCount;
            _tmpReplicationCount = _cursor.getInt(_cursorIndexOfReplicationCount);
            final int _tmpMaxReplications;
            _tmpMaxReplications = _cursor.getInt(_cursorIndexOfMaxReplications);
            final String _tmpPayload;
            if (_cursor.isNull(_cursorIndexOfPayload)) {
              _tmpPayload = null;
            } else {
              _tmpPayload = _cursor.getString(_cursorIndexOfPayload);
            }
            final String _tmpPayloadHash;
            if (_cursor.isNull(_cursorIndexOfPayloadHash)) {
              _tmpPayloadHash = null;
            } else {
              _tmpPayloadHash = _cursor.getString(_cursorIndexOfPayloadHash);
            }
            final String _tmpCustodyState;
            if (_cursor.isNull(_cursorIndexOfCustodyState)) {
              _tmpCustodyState = null;
            } else {
              _tmpCustodyState = _cursor.getString(_cursorIndexOfCustodyState);
            }
            final float _tmpDeliveryProbability;
            _tmpDeliveryProbability = _cursor.getFloat(_cursorIndexOfDeliveryProbability);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final String _tmpRawJson;
            if (_cursor.isNull(_cursorIndexOfRawJson)) {
              _tmpRawJson = null;
            } else {
              _tmpRawJson = _cursor.getString(_cursorIndexOfRawJson);
            }
            _item = new DtnBundleEntity(_tmpBundleId,_tmpMessageId,_tmpSource,_tmpDestination,_tmpCreationTime,_tmpExpirationTime,_tmpTtl,_tmpPriority,_tmpHopCount,_tmpReplicationCount,_tmpMaxReplications,_tmpPayload,_tmpPayloadHash,_tmpCustodyState,_tmpDeliveryProbability,_tmpSizeBytes,_tmpRawJson);
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
  public Flow<Integer> getDtnBundleCount() {
    final String _sql = "SELECT COUNT(*) FROM dtn_bundles";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"dtn_bundles"}, new Callable<Integer>() {
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
  public Object getPeerEncounter(final String peerId,
      final Continuation<? super PeerEncounterEntity> $completion) {
    final String _sql = "SELECT * FROM peer_encounters WHERE peerId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (peerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, peerId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PeerEncounterEntity>() {
      @Override
      @Nullable
      public PeerEncounterEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPeerId = CursorUtil.getColumnIndexOrThrow(_cursor, "peerId");
          final int _cursorIndexOfFirstSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "firstSeen");
          final int _cursorIndexOfLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeen");
          final int _cursorIndexOfEncounterCount = CursorUtil.getColumnIndexOrThrow(_cursor, "encounterCount");
          final int _cursorIndexOfAverageIntervalSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "averageIntervalSeconds");
          final int _cursorIndexOfLastTransport = CursorUtil.getColumnIndexOrThrow(_cursor, "lastTransport");
          final int _cursorIndexOfEstimatedStability = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedStability");
          final PeerEncounterEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpPeerId;
            if (_cursor.isNull(_cursorIndexOfPeerId)) {
              _tmpPeerId = null;
            } else {
              _tmpPeerId = _cursor.getString(_cursorIndexOfPeerId);
            }
            final long _tmpFirstSeen;
            _tmpFirstSeen = _cursor.getLong(_cursorIndexOfFirstSeen);
            final long _tmpLastSeen;
            _tmpLastSeen = _cursor.getLong(_cursorIndexOfLastSeen);
            final int _tmpEncounterCount;
            _tmpEncounterCount = _cursor.getInt(_cursorIndexOfEncounterCount);
            final long _tmpAverageIntervalSeconds;
            _tmpAverageIntervalSeconds = _cursor.getLong(_cursorIndexOfAverageIntervalSeconds);
            final String _tmpLastTransport;
            if (_cursor.isNull(_cursorIndexOfLastTransport)) {
              _tmpLastTransport = null;
            } else {
              _tmpLastTransport = _cursor.getString(_cursorIndexOfLastTransport);
            }
            final float _tmpEstimatedStability;
            _tmpEstimatedStability = _cursor.getFloat(_cursorIndexOfEstimatedStability);
            _result = new PeerEncounterEntity(_tmpPeerId,_tmpFirstSeen,_tmpLastSeen,_tmpEncounterCount,_tmpAverageIntervalSeconds,_tmpLastTransport,_tmpEstimatedStability);
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

  @Override
  public Flow<List<PeerEncounterEntity>> getAllPeerEncounters() {
    final String _sql = "SELECT * FROM peer_encounters ORDER BY lastSeen DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"peer_encounters"}, new Callable<List<PeerEncounterEntity>>() {
      @Override
      @NonNull
      public List<PeerEncounterEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPeerId = CursorUtil.getColumnIndexOrThrow(_cursor, "peerId");
          final int _cursorIndexOfFirstSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "firstSeen");
          final int _cursorIndexOfLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeen");
          final int _cursorIndexOfEncounterCount = CursorUtil.getColumnIndexOrThrow(_cursor, "encounterCount");
          final int _cursorIndexOfAverageIntervalSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "averageIntervalSeconds");
          final int _cursorIndexOfLastTransport = CursorUtil.getColumnIndexOrThrow(_cursor, "lastTransport");
          final int _cursorIndexOfEstimatedStability = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedStability");
          final List<PeerEncounterEntity> _result = new ArrayList<PeerEncounterEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PeerEncounterEntity _item;
            final String _tmpPeerId;
            if (_cursor.isNull(_cursorIndexOfPeerId)) {
              _tmpPeerId = null;
            } else {
              _tmpPeerId = _cursor.getString(_cursorIndexOfPeerId);
            }
            final long _tmpFirstSeen;
            _tmpFirstSeen = _cursor.getLong(_cursorIndexOfFirstSeen);
            final long _tmpLastSeen;
            _tmpLastSeen = _cursor.getLong(_cursorIndexOfLastSeen);
            final int _tmpEncounterCount;
            _tmpEncounterCount = _cursor.getInt(_cursorIndexOfEncounterCount);
            final long _tmpAverageIntervalSeconds;
            _tmpAverageIntervalSeconds = _cursor.getLong(_cursorIndexOfAverageIntervalSeconds);
            final String _tmpLastTransport;
            if (_cursor.isNull(_cursorIndexOfLastTransport)) {
              _tmpLastTransport = null;
            } else {
              _tmpLastTransport = _cursor.getString(_cursorIndexOfLastTransport);
            }
            final float _tmpEstimatedStability;
            _tmpEstimatedStability = _cursor.getFloat(_cursorIndexOfEstimatedStability);
            _item = new PeerEncounterEntity(_tmpPeerId,_tmpFirstSeen,_tmpLastSeen,_tmpEncounterCount,_tmpAverageIntervalSeconds,_tmpLastTransport,_tmpEstimatedStability);
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
  public Object getCrdtOperationsForDocument(final String documentId,
      final Continuation<? super List<CrdtOperationEntity>> $completion) {
    final String _sql = "SELECT * FROM crdt_operations WHERE documentId = ? ORDER BY lamportClock ASC, timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (documentId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, documentId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CrdtOperationEntity>>() {
      @Override
      @NonNull
      public List<CrdtOperationEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfOpId = CursorUtil.getColumnIndexOrThrow(_cursor, "opId");
          final int _cursorIndexOfDocumentId = CursorUtil.getColumnIndexOrThrow(_cursor, "documentId");
          final int _cursorIndexOfActorId = CursorUtil.getColumnIndexOrThrow(_cursor, "actorId");
          final int _cursorIndexOfLamportClock = CursorUtil.getColumnIndexOrThrow(_cursor, "lamportClock");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfOperationType = CursorUtil.getColumnIndexOrThrow(_cursor, "operationType");
          final int _cursorIndexOfKey = CursorUtil.getColumnIndexOrThrow(_cursor, "key");
          final int _cursorIndexOfValueJson = CursorUtil.getColumnIndexOrThrow(_cursor, "valueJson");
          final List<CrdtOperationEntity> _result = new ArrayList<CrdtOperationEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CrdtOperationEntity _item;
            final String _tmpOpId;
            if (_cursor.isNull(_cursorIndexOfOpId)) {
              _tmpOpId = null;
            } else {
              _tmpOpId = _cursor.getString(_cursorIndexOfOpId);
            }
            final String _tmpDocumentId;
            if (_cursor.isNull(_cursorIndexOfDocumentId)) {
              _tmpDocumentId = null;
            } else {
              _tmpDocumentId = _cursor.getString(_cursorIndexOfDocumentId);
            }
            final String _tmpActorId;
            if (_cursor.isNull(_cursorIndexOfActorId)) {
              _tmpActorId = null;
            } else {
              _tmpActorId = _cursor.getString(_cursorIndexOfActorId);
            }
            final long _tmpLamportClock;
            _tmpLamportClock = _cursor.getLong(_cursorIndexOfLamportClock);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpOperationType;
            if (_cursor.isNull(_cursorIndexOfOperationType)) {
              _tmpOperationType = null;
            } else {
              _tmpOperationType = _cursor.getString(_cursorIndexOfOperationType);
            }
            final String _tmpKey;
            if (_cursor.isNull(_cursorIndexOfKey)) {
              _tmpKey = null;
            } else {
              _tmpKey = _cursor.getString(_cursorIndexOfKey);
            }
            final String _tmpValueJson;
            if (_cursor.isNull(_cursorIndexOfValueJson)) {
              _tmpValueJson = null;
            } else {
              _tmpValueJson = _cursor.getString(_cursorIndexOfValueJson);
            }
            _item = new CrdtOperationEntity(_tmpOpId,_tmpDocumentId,_tmpActorId,_tmpLamportClock,_tmpTimestamp,_tmpOperationType,_tmpKey,_tmpValueJson);
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
  public Object getMaxLamportClock(final String documentId,
      final Continuation<? super Long> $completion) {
    final String _sql = "SELECT COALESCE(MAX(lamportClock), 0) FROM crdt_operations WHERE documentId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (documentId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, documentId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
            }
            _result = _tmp;
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

  @Override
  public Object getLatestNetworkEpoch(final Continuation<? super NetworkEpochEntity> $completion) {
    final String _sql = "SELECT * FROM network_epochs ORDER BY epochNumber DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<NetworkEpochEntity>() {
      @Override
      @Nullable
      public NetworkEpochEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfEpochNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "epochNumber");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfDetectedPartitionCount = CursorUtil.getColumnIndexOrThrow(_cursor, "detectedPartitionCount");
          final int _cursorIndexOfKnownMemberCount = CursorUtil.getColumnIndexOrThrow(_cursor, "knownMemberCount");
          final int _cursorIndexOfStateHash = CursorUtil.getColumnIndexOrThrow(_cursor, "stateHash");
          final NetworkEpochEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpEpochNumber;
            _tmpEpochNumber = _cursor.getLong(_cursorIndexOfEpochNumber);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final int _tmpDetectedPartitionCount;
            _tmpDetectedPartitionCount = _cursor.getInt(_cursorIndexOfDetectedPartitionCount);
            final int _tmpKnownMemberCount;
            _tmpKnownMemberCount = _cursor.getInt(_cursorIndexOfKnownMemberCount);
            final String _tmpStateHash;
            if (_cursor.isNull(_cursorIndexOfStateHash)) {
              _tmpStateHash = null;
            } else {
              _tmpStateHash = _cursor.getString(_cursorIndexOfStateHash);
            }
            _result = new NetworkEpochEntity(_tmpEpochNumber,_tmpTimestamp,_tmpDetectedPartitionCount,_tmpKnownMemberCount,_tmpStateHash);
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
