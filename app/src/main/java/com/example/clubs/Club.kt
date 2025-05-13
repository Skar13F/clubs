package com.example.clubs;

data class Club(
        val id: String = "",
        val name: String = "",
        val members: Map<String, Boolean> = emptyMap()
)