package com.money.protect.repository

import android.annotation.SuppressLint
import com.money.protect.repository.AssistantRepository.singleton.query
import com.money.protect.models.AccountModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AssistantRepository {
    object singleton{
        @SuppressLint("StaticFieldLeak")
        val db = Firebase.firestore
        val query = db.collection("account")
    }

    fun updateStatut(assistant: AccountModel)
    {
        val assistantMap = hashMapOf(
            "id" to assistant.id,
            "nomcomplet" to assistant.nomcomplet,
            "telephone" to assistant.telephone,
            "email" to assistant.email,
            "role" to "assistant",
            "statut" to assistant.statut,
            "module" to assistant.module,
            "nomcommercial" to assistant.nomcommercial,
            "ville" to assistant.ville,
            "quartier" to assistant.quartier,
            "superviseur" to assistant.superviseur,
            "creation" to assistant.creation,

        )
        query.document(assistant.id).set(assistantMap)
    }
}