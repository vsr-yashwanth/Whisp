package com.example.offlinechat.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Conversation::class,
        Message::class,
        BufferedPacket::class,
        DtnBundleEntity::class,
        PeerEncounterEntity::class,
        CrdtOperationEntity::class,
        NetworkEpochEntity::class,
        FriendContact::class,
        TouristProfile::class,
        GeoFenceZone::class,
        TripItinerary::class,
        SafetyIncident::class,
        CctvCamera::class,
        BlockchainBlockEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getDatabase(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "offlinechat_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
