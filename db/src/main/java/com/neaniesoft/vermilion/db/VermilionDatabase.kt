package com.neaniesoft.vermilion.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.neaniesoft.vermilion.dbentities.comments.CommentDao
import com.neaniesoft.vermilion.dbentities.comments.CommentRecord
import com.neaniesoft.vermilion.dbentities.communities.CommunityDao
import com.neaniesoft.vermilion.dbentities.communities.CommunityRecord
import com.neaniesoft.vermilion.dbentities.posts.PostDao
import com.neaniesoft.vermilion.dbentities.posts.PostHistoryDao
import com.neaniesoft.vermilion.dbentities.posts.PostHistoryRecord
import com.neaniesoft.vermilion.dbentities.posts.PostRecord
import com.neaniesoft.vermilion.dbentities.posts.PostRemoteKey
import com.neaniesoft.vermilion.dbentities.posts.PostRemoteKeyDao
import com.neaniesoft.vermilion.dbentities.tabs.TabStateDao
import com.neaniesoft.vermilion.dbentities.tabs.TabStateRecord
import com.neaniesoft.vermilion.dbentities.useraccount.UserAccountDao
import com.neaniesoft.vermilion.dbentities.useraccount.UserAccountRecord

@Database(
    entities = [
        UserAccountRecord::class,
        PostRecord::class,
        PostRemoteKey::class,
        CommentRecord::class,
        TabStateRecord::class,
        CommunityRecord::class,
        PostHistoryRecord::class
    ],
    version = 22
)
abstract class VermilionDatabase : RoomDatabase() {
    abstract fun userAccountDao(): UserAccountDao
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeysDao(): PostRemoteKeyDao
    abstract fun commentDao(): CommentDao
    abstract fun tabStateDao(): TabStateDao
    abstract fun communityDao(): CommunityDao
    abstract fun postHistoryDao(): PostHistoryDao
}
