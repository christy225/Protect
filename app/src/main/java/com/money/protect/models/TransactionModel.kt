package com.money.protect.models

data class TransactionModel(
    val id: String = "",
    val date: String = "",
    val heure: String = "",
    val montant: String = "",
    val operateur: String = "",
    val telephone: String = "",
    val typeoperation: String = "",
    val statut: Boolean = false,
    val url: String = "",
    val idDoc: String = ""
)