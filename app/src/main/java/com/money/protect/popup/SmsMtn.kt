package com.money.protect.popup

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.widget.ImageView
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.money.protect.MainActivity
import com.money.protect.R
import com.money.protect.adapter.SmsAdapter
import com.money.protect.fragment_assistant.national.MtnFragment

class SmsMtn(
    private val context: MainActivity,
    private val fragment: MtnFragment
) : Dialog(fragment.requireContext()) {
    // Déclarez une liste pour stocker les SMS.
    lateinit var smsList: ArrayList<String>

    private val SMS_PERMISSION_REQUEST_CODE = 123
    private lateinit var recyclerView: RecyclerView
    private lateinit var smsAdapter: SmsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_sms)

        val backButton = findViewById<ImageView>(R.id.backSmsHome)
        backButton.setOnClickListener{
            dismiss()
        }

        smsList = arrayListOf()
        recyclerView = findViewById(R.id.recyclerViewSmsMtn)
        smsAdapter = SmsAdapter(context, smsList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = smsAdapter

        if (hasReadSmsPermission()) {
            // Vous avez déjà la permission pour lire les SMS.
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_SMS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Ne rien faire
            }
            readSms()
        } else {
            requestSmsPermission()
        }
    }

    private fun hasReadSmsPermission(): Boolean {
        val permission = Manifest.permission.READ_SMS
        val granted = PackageManager.PERMISSION_GRANTED
        return ContextCompat.checkSelfPermission(context, permission) == granted
    }

    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(
            context,
            arrayOf(Manifest.permission.READ_SMS),
            SMS_PERMISSION_REQUEST_CODE
        )
    }

    @SuppressLint("SupportAnnotationUsage", "NotifyDataSetChanged")
    @RequiresPermission(android.Manifest.permission.READ_SMS)
    private fun readSms() {
        val contentResolver = context.contentResolver

        val selection = "${Telephony.Sms.ADDRESS} = ?" // Sélectionnez les SMS avec l'adresse spécifique
        val selectionArgs = arrayOf("Mobile Money")

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