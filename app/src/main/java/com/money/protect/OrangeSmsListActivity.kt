package com.money.protect

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.money.protect.adapter.SmsAdapter
import com.money.protect.adapter.SmsOrangeAdapter

class OrangeSmsListActivity : AppCompatActivity() {
    private lateinit var smsList: ArrayList<String>
    private lateinit var recyclerView: RecyclerView
    private lateinit var smsAdapter: SmsOrangeAdapter
    @SuppressLint("MissingInflatedId", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orange_sms_list)

        val context = MainActivity()

        smsList = arrayListOf()
        recyclerView = findViewById(R.id.recyclerViewListSmsOrange)
        smsAdapter = SmsOrangeAdapter(this, smsList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = smsAdapter

        val btn = findViewById<Button>(R.id.buttonRefresh)

        btn.setOnClickListener {
            smsList = arrayListOf()
            recyclerView = findViewById(R.id.recyclerViewListSmsOrange)
            smsAdapter = SmsOrangeAdapter(this, smsList)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = smsAdapter
            readSms()
        }

        readSms()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun readSms() {
        val contentResolver = contentResolver

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