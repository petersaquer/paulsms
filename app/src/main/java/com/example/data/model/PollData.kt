package com.example.data.model

data class PollOption(
    val id: Int,
    val text: String,
    val voteCount: Int = 0
)

data class PollData(
    val id: String,
    val question: String,
    val options: List<PollOption>,
    val totalVotes: Int = 0,
    val userVotedOptionId: Int? = null,
    val isAnonymous: Boolean = true
)
