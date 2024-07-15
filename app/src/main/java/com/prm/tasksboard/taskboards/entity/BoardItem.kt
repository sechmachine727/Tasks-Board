package com.prm.tasksboard.taskboards.entity

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class BoardItem(
    @get:PropertyName("board_id") @set:PropertyName("board_id") var boardId: String = "",
    @get:PropertyName("created_at") @set:PropertyName("created_at") var createdAt: Timestamp,
    @get:PropertyName("name") @set:PropertyName("name") var name: String,
    @get:PropertyName("updated_at") @set:PropertyName("updated_at") var updatedAt: Timestamp,
    @get:PropertyName("user_id") @set:PropertyName("user_id") var userId: String,
) {
    constructor() : this("", Timestamp.now(), "", Timestamp.now(), "")
}
