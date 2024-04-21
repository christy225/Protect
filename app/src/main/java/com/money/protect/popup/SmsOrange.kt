package com.money.protect.popup

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.provider.Telephony
import android.widget.ImageView
import androidx.annotation.RequiresPermission
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.money.protect.MainActivity
import com.money.protect.R
import com.money.protect.adapter.SmsAdapter
import com.money.protect.OrangeSaveActivity

class SmsOrange(
    private val Contexte: OrangeSaveActivity) : Dialog(Contexte) {
    // Déclarez une liste pour stocker les SMS.
    private lateinit var smsList: ArrayList<String>

    private val SMS_PERMISSION_REQUEST_CODE = 123
    private lateinit var recyclerView: RecyclerView
    private lateinit var smsAdapter: SmsAdapter
    @SuppressLint("MissingInflatedId", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_sms_orange)

        val backButton = findViewById<ImageView>(R.id.backSmsHomeOrange)
        backButton.setOnClickListener{
            dismiss()
        }

        smsList = arrayListOf()
        recyclerView = findViewById(R.id.recyclerViewSmsOrange)
        val context = MainActivity()
        smsAdapter = SmsAdapter(context, smsList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = smsAdapter

        readSms()
    }

    @SuppressLint("SupportAnnotationUsage", "NotifyDataSetChanged")
    @RequiresPermission(android.Manifest.permission.READ_SMS)
    private fun readSms() {
        val contentResolver = context.contentResolver

        val selection = "${Telephony.Sms.ADDRESS} = ?" // Sélectionnez les SMS avec l'adresse spécifique
        val selectionArgs = arrayOf("+454")

        val uri = Telephony.Sms.Inbox.CONTENT_URI
        val cursor = contentResolver.query(uri, null, selection, selectionArgs, null)

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY)
                val smsBody = cursor.getString(bodyIndex)
                smsList.add(smsBody)
            }
            cursor.close()
            smsAdapter.notifyDataSetChanged() // Notifiez l'adaptateur que les données ont changé.
        }
    }
}