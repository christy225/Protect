package com.example.protect.models

data class AccountModel(
    val id: String = "",
    val nomcomplet: String = "",
    val email: String = "",
    val superviseur: String = "",
    val telephone: String = "",
    val role: String = "",
    var statut: Boolean = false,
    val nomcommercial: String = "",
    val ville: String = "",
    val quartier: String = "",
    val creation: String = ""
)
