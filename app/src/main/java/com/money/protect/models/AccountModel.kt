package com.money.protect.models

data class AccountModel(
    val id: String = "",
    val nomcomplet: String = "",
    val email: String = "",
    val superviseur: String = "",
    val telephone: String = "",
    val role: String = "",
    var statut: Boolean = false,
    val module: String = "",
    val nomcommercial: String = "",
    val ville: String = "",
    val quartier: String = "",
    val creation: String = "",
    val duration: String = "",
    val abonnement: String = "",
    val capital: String = ""
)
