package com.example.clubs;

data class Student(
        val id: String = "",
        val name: String = "",
        val email: String = "",
        val clubs: Map<String, Boolean> = emptyMap()
)

