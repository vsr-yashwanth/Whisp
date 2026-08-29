package com.example.offlinechat.data;

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
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "deprecation"})
public final class ChatDatabase_Impl extends ChatDatabase {
  private volatile ChatDao _chatDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(4) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `conversations` (`id` TEXT NOT NULL, `peerId` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `lastMessageAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `messages` (`id` TEXT NOT NULL, `conversationId` TEXT NOT NULL, `senderId` TEXT NOT NULL, `encryptedPayload` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `status` TEXT NOT NULL, `hopTrace` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`conversationId`) REFERENCES `conversations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_conversationId` ON `messages` (`conversationId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `buffered_packets` (`packetId` TEXT NOT NULL, `messageId` TEXT NOT NULL, `recipientId` TEXT NOT NULL, `conversationId` TEXT NOT NULL, `priority` INTEGER NOT NULL, `ttl` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `expiresAt` INTEGER NOT NULL, `retryCount` INTEGER NOT NULL, `rawJsonPayload` TEXT NOT NULL, PRIMARY KEY(`packetId`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_buffered_packets_recipientId` ON `buffered_packets` (`recipientId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_buffered_packets_expiresAt` ON `buffered_packets` (`expiresAt`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_buffered_packets_priority` ON `buffered_packets` (`priority`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `dtn_bundles` (`bundleId` TEXT NOT NULL, `messageId` TEXT NOT NULL, `source` TEXT NOT NULL, `destination` TEXT NOT NULL, `creationTime` INTEGER NOT NULL, `expirationTime` INTEGER NOT NULL, `ttl` INTEGER NOT NULL, `priority` INTEGER NOT NULL, `hopCount` INTEGER NOT NULL, `replicationCount` INTEGER NOT NULL, `maxReplications` INTEGER NOT NULL, `payload` TEXT NOT NULL, `payloadHash` TEXT NOT NULL, `custodyState` TEXT NOT NULL, `deliveryProbability` REAL NOT NULL, `sizeBytes` INTEGER NOT NULL, `rawJson` TEXT NOT NULL, PRIMARY KEY(`bundleId`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_dtn_bundles_destination` ON `dtn_bundles` (`destination`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_dtn_bundles_expirationTime` ON `dtn_bundles` (`expirationTime`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_dtn_bundles_priority` ON `dtn_bundles` (`priority`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_dtn_bundles_custodyState` ON `dtn_bundles` (`custodyState`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `peer_encounters` (`peerId` TEXT NOT NULL, `firstSeen` INTEGER NOT NULL, `lastSeen` INTEGER NOT NULL, `encounterCount` INTEGER NOT NULL, `averageIntervalSeconds` INTEGER NOT NULL, `lastTransport` TEXT NOT NULL, `estimatedStability` REAL NOT NULL, PRIMARY KEY(`peerId`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_peer_encounters_peerId` ON `peer_encounters` (`peerId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_peer_encounters_lastSeen` ON `peer_encounters` (`lastSeen`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `crdt_operations` (`opId` TEXT NOT NULL, `documentId` TEXT NOT NULL, `actorId` TEXT NOT NULL, `lamportClock` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `operationType` TEXT NOT NULL, `key` TEXT NOT NULL, `valueJson` TEXT NOT NULL, PRIMARY KEY(`opId`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_crdt_operations_documentId` ON `crdt_operations` (`documentId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_crdt_operations_lamportClock` ON `crdt_operations` (`lamportClock`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_crdt_operations_actorId` ON `crdt_operations` (`actorId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `network_epochs` (`epochNumber` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `detectedPartitionCount` INTEGER NOT NULL, `knownMemberCount` INTEGER NOT NULL, `stateHash` TEXT NOT NULL, PRIMARY KEY(`epochNumber`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '7c275ae0ac03e271372009573bc2da5f')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `conversations`");
        db.execSQL("DROP TABLE IF EXISTS `messages`");
        db.execSQL("DROP TABLE IF EXISTS `buffered_packets`");
        db.execSQL("DROP TABLE IF EXISTS `dtn_bundles`");
        db.execSQL("DROP TABLE IF EXISTS `peer_encounters`");
        db.execSQL("DROP TABLE IF EXISTS `crdt_operations`");
        db.execSQL("DROP TABLE IF EXISTS `network_epochs`");
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
        db.execSQL("PRAGMA foreign_keys = ON");
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
        final HashMap<String, TableInfo.Column> _columnsConversations = new HashMap<String, TableInfo.Column>(4);
        _columnsConversations.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("peerId", new TableInfo.Column("peerId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("lastMessageAt", new TableInfo.Column("lastMessageAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysConversations = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesConversations = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoConversations = new TableInfo("conversations", _columnsConversations, _foreignKeysConversations, _indicesConversations);
        final TableInfo _existingConversations = TableInfo.read(db, "conversations");
        if (!_infoConversations.equals(_existingConversations)) {
          return new RoomOpenHelper.ValidationResult(false, "conversations(com.example.offlinechat.data.Conversation).\n"
                  + " Expected:\n" + _infoConversations + "\n"
                  + " Found:\n" + _existingConversations);
        }
        final HashMap<String, TableInfo.Column> _columnsMessages = new HashMap<String, TableInfo.Column>(7);
        _columnsMessages.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("conversationId", new TableInfo.Column("conversationId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("senderId", new TableInfo.Column("senderId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("encryptedPayload", new TableInfo.Column("encryptedPayload", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("hopTrace", new TableInfo.Column("hopTrace", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMessages = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysMessages.add(new TableInfo.ForeignKey("conversations", "CASCADE", "NO ACTION", Arrays.asList("conversationId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesMessages = new HashSet<TableInfo.Index>(1);
        _indicesMessages.add(new TableInfo.Index("index_messages_conversationId", false, Arrays.asList("conversationId"), Arrays.asList("ASC")));
        final TableInfo _infoMessages = new TableInfo("messages", _columnsMessages, _foreignKeysMessages, _indicesMessages);
        final TableInfo _existingMessages = TableInfo.read(db, "messages");
        if (!_infoMessages.equals(_existingMessages)) {
          return new RoomOpenHelper.ValidationResult(false, "messages(com.example.offlinechat.data.Message).\n"
                  + " Expected:\n" + _infoMessages + "\n"
                  + " Found:\n" + _existingMessages);
        }
        final HashMap<String, TableInfo.Column> _columnsBufferedPackets = new HashMap<String, TableInfo.Column>(10);
        _columnsBufferedPackets.put("packetId", new TableInfo.Column("packetId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBufferedPackets.put("messageId", new TableInfo.Column("messageId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBufferedPackets.put("recipientId", new TableInfo.Column("recipientId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBufferedPackets.put("conversationId", new TableInfo.Column("conversationId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBufferedPackets.put("priority", new TableInfo.Column("priority", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBufferedPackets.put("ttl", new TableInfo.Column("ttl", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBufferedPackets.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBufferedPackets.put("expiresAt", new TableInfo.Column("expiresAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBufferedPackets.put("retryCount", new TableInfo.Column("retryCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBufferedPackets.put("rawJsonPayload", new TableInfo.Column("rawJsonPayload", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBufferedPackets = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBufferedPackets = new HashSet<TableInfo.Index>(3);
        _indicesBufferedPackets.add(new TableInfo.Index("index_buffered_packets_recipientId", false, Arrays.asList("recipientId"), Arrays.asList("ASC")));
        _indicesBufferedPackets.add(new TableInfo.Index("index_buffered_packets_expiresAt", false, Arrays.asList("expiresAt"), Arrays.asList("ASC")));
        _indicesBufferedPackets.add(new TableInfo.Index("index_buffered_packets_priority", false, Arrays.asList("priority"), Arrays.asList("ASC")));
        final TableInfo _infoBufferedPackets = new TableInfo("buffered_packets", _columnsBufferedPackets, _foreignKeysBufferedPackets, _indicesBufferedPackets);
        final TableInfo _existingBufferedPackets = TableInfo.read(db, "buffered_packets");
        if (!_infoBufferedPackets.equals(_existingBufferedPackets)) {
          return new RoomOpenHelper.ValidationResult(false, "buffered_packets(com.example.offlinechat.data.BufferedPacket).\n"
                  + " Expected:\n" + _infoBufferedPackets + "\n"
                  + " Found:\n" + _existingBufferedPackets);
        }
        final HashMap<String, TableInfo.Column> _columnsDtnBundles = new HashMap<String, TableInfo.Column>(17);
        _columnsDtnBundles.put("bundleId", new TableInfo.Column("bundleId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDtnBundles.put("messageId", new TableInfo.Column("messageId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDtnBundles.put("source", new TableInfo.Column("source", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDtnBundles.put("destination", new TableInfo.Column("destination", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDtnBundles.put("creationTime", new TableInfo.Column("creationTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDtnBundles.put("expirationTime", new TableInfo.Column("expirationTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDtnBundles.put("ttl", new TableInfo.Column("ttl", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDtnBundles.put("priority", new TableInfo.Column("priority", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDtnBundles.put("hopCount", new TableInfo.Column("hopCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDtnBundles.put("replicationCount", new TableInfo.Column("replicationCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDtnBundles.put("maxReplications", new TableInfo.Column("maxReplications", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDtnBundles.put("payload", new TableInfo.Column("payload", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDtnBundles.put("payloadHash", new TableInfo.Column("payloadHash", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDtnBundles.put("custodyState", new TableInfo.Column("custodyState", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDtnBundles.put("deliveryProbability", new TableInfo.Column("deliveryProbability", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDtnBundles.put("sizeBytes", new TableInfo.Column("sizeBytes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDtnBundles.put("rawJson", new TableInfo.Column("rawJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDtnBundles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDtnBundles = new HashSet<TableInfo.Index>(4);
        _indicesDtnBundles.add(new TableInfo.Index("index_dtn_bundles_destination", false, Arrays.asList("destination"), Arrays.asList("ASC")));
        _indicesDtnBundles.add(new TableInfo.Index("index_dtn_bundles_expirationTime", false, Arrays.asList("expirationTime"), Arrays.asList("ASC")));
        _indicesDtnBundles.add(new TableInfo.Index("index_dtn_bundles_priority", false, Arrays.asList("priority"), Arrays.asList("ASC")));
        _indicesDtnBundles.add(new TableInfo.Index("index_dtn_bundles_custodyState", false, Arrays.asList("custodyState"), Arrays.asList("ASC")));
        final TableInfo _infoDtnBundles = new TableInfo("dtn_bundles", _columnsDtnBundles, _foreignKeysDtnBundles, _indicesDtnBundles);
        final TableInfo _existingDtnBundles = TableInfo.read(db, "dtn_bundles");
        if (!_infoDtnBundles.equals(_existingDtnBundles)) {
          return new RoomOpenHelper.ValidationResult(false, "dtn_bundles(com.example.offlinechat.data.DtnBundleEntity).\n"
                  + " Expected:\n" + _infoDtnBundles + "\n"
                  + " Found:\n" + _existingDtnBundles);
        }
        final HashMap<String, TableInfo.Column> _columnsPeerEncounters = new HashMap<String, TableInfo.Column>(7);
        _columnsPeerEncounters.put("peerId", new TableInfo.Column("peerId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeerEncounters.put("firstSeen", new TableInfo.Column("firstSeen", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeerEncounters.put("lastSeen", new TableInfo.Column("lastSeen", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeerEncounters.put("encounterCount", new TableInfo.Column("encounterCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeerEncounters.put("averageIntervalSeconds", new TableInfo.Column("averageIntervalSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeerEncounters.put("lastTransport", new TableInfo.Column("lastTransport", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeerEncounters.put("estimatedStability", new TableInfo.Column("estimatedStability", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPeerEncounters = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPeerEncounters = new HashSet<TableInfo.Index>(2);
        _indicesPeerEncounters.add(new TableInfo.Index("index_peer_encounters_peerId", false, Arrays.asList("peerId"), Arrays.asList("ASC")));
        _indicesPeerEncounters.add(new TableInfo.Index("index_peer_encounters_lastSeen", false, Arrays.asList("lastSeen"), Arrays.asList("ASC")));
        final TableInfo _infoPeerEncounters = new TableInfo("peer_encounters", _columnsPeerEncounters, _foreignKeysPeerEncounters, _indicesPeerEncounters);
        final TableInfo _existingPeerEncounters = TableInfo.read(db, "peer_encounters");
        if (!_infoPeerEncounters.equals(_existingPeerEncounters)) {
          return new RoomOpenHelper.ValidationResult(false, "peer_encounters(com.example.offlinechat.data.PeerEncounterEntity).\n"
                  + " Expected:\n" + _infoPeerEncounters + "\n"
                  + " Found:\n" + _existingPeerEncounters);
        }
        final HashMap<String, TableInfo.Column> _columnsCrdtOperations = new HashMap<String, TableInfo.Column>(8);
        _columnsCrdtOperations.put("opId", new TableInfo.Column("opId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCrdtOperations.put("documentId", new TableInfo.Column("documentId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCrdtOperations.put("actorId", new TableInfo.Column("actorId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCrdtOperations.put("lamportClock", new TableInfo.Column("lamportClock", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCrdtOperations.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCrdtOperations.put("operationType", new TableInfo.Column("operationType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCrdtOperations.put("key", new TableInfo.Column("key", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCrdtOperations.put("valueJson", new TableInfo.Column("valueJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCrdtOperations = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCrdtOperations = new HashSet<TableInfo.Index>(3);
        _indicesCrdtOperations.add(new TableInfo.Index("index_crdt_operations_documentId", false, Arrays.asList("documentId"), Arrays.asList("ASC")));
        _indicesCrdtOperations.add(new TableInfo.Index("index_crdt_operations_lamportClock", false, Arrays.asList("lamportClock"), Arrays.asList("ASC")));
        _indicesCrdtOperations.add(new TableInfo.Index("index_crdt_operations_actorId", false, Arrays.asList("actorId"), Arrays.asList("ASC")));
        final TableInfo _infoCrdtOperations = new TableInfo("crdt_operations", _columnsCrdtOperations, _foreignKeysCrdtOperations, _indicesCrdtOperations);
        final TableInfo _existingCrdtOperations = TableInfo.read(db, "crdt_operations");
        if (!_infoCrdtOperations.equals(_existingCrdtOperations)) {
          return new RoomOpenHelper.ValidationResult(false, "crdt_operations(com.example.offlinechat.data.CrdtOperationEntity).\n"
                  + " Expected:\n" + _infoCrdtOperations + "\n"
                  + " Found:\n" + _existingCrdtOperations);
        }
        final HashMap<String, TableInfo.Column> _columnsNetworkEpochs = new HashMap<String, TableInfo.Column>(5);
        _columnsNetworkEpochs.put("epochNumber", new TableInfo.Column("epochNumber", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNetworkEpochs.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNetworkEpochs.put("detectedPartitionCount", new TableInfo.Column("detectedPartitionCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNetworkEpochs.put("knownMemberCount", new TableInfo.Column("knownMemberCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNetworkEpochs.put("stateHash", new TableInfo.Column("stateHash", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysNetworkEpochs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesNetworkEpochs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoNetworkEpochs = new TableInfo("network_epochs", _columnsNetworkEpochs, _foreignKeysNetworkEpochs, _indicesNetworkEpochs);
        final TableInfo _existingNetworkEpochs = TableInfo.read(db, "network_epochs");
        if (!_infoNetworkEpochs.equals(_existingNetworkEpochs)) {
          return new RoomOpenHelper.ValidationResult(false, "network_epochs(com.example.offlinechat.data.NetworkEpochEntity).\n"
                  + " Expected:\n" + _infoNetworkEpochs + "\n"
                  + " Found:\n" + _existingNetworkEpochs);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "7c275ae0ac03e271372009573bc2da5f", "035728a87feabfaf8127f8848615b6d1");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "conversations","messages","buffered_packets","dtn_bundles","peer_encounters","crdt_operations","network_epochs");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `conversations`");
      _db.execSQL("DELETE FROM `messages`");
      _db.execSQL("DELETE FROM `buffered_packets`");
      _db.execSQL("DELETE FROM `dtn_bundles`");
      _db.execSQL("DELETE FROM `peer_encounters`");
      _db.execSQL("DELETE FROM `crdt_operations`");
      _db.execSQL("DELETE FROM `network_epochs`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
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
    _typeConvertersMap.put(ChatDao.class, ChatDao_Impl.getRequiredConverters());
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
  public ChatDao chatDao() {
    if (_chatDao != null) {
      return _chatDao;
    } else {
      synchronized(this) {
        if(_chatDao == null) {
          _chatDao = new ChatDao_Impl(this);
        }
        return _chatDao;
      }
    }
  }
}
