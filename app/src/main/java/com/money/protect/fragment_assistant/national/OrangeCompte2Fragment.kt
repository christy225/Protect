package com.money.protect.fragment_assistant.national

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.money.protect.MainActivity
import com.money.protect.R
import com.money.protect.adapter.SmsOrangeAdapter

class OrangeCompte2Fragment(private val context: MainActivity) : Fragment() {
    // Déclarez une liste pour stocker les SMS.
    private lateinit var smsList: ArrayList<String>

    private lateinit var recyclerView: RecyclerView
    private lateinit var smsAdapterOrange: SmsOrangeAdapter
    private val SMS_PERMISSION_REQUEST_CODE = 123

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId", "SetTextI18n", "NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_assistant_operation_orange_compte2, container, false)

        smsList = arrayListOf()
        recyclerView = view.findViewById(R.id.recyclerViewSmsOrange)

        smsAdapterOrange = SmsOrangeAdapter(context, smsList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = smsAdapterOrange

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

        return view
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
        val selectionArgs = arrayOf("+2250747033014")

        val uri = Telephony.Sms.Inbox.CONTENT_URI
        val cursor = contentResolver.query(uri, null, selection, selectionArgs, null)

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY)
                val smsBody = cursor.getString(bodyIndex)
                smsList.add(smsBody)
            }
            cursor.close()
            smsAdapterOrange.notifyDataSetChanged() // Notifiez l'adaptateur que les données ont changé.
        }
    }

}