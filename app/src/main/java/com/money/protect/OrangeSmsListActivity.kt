package com.money.protect

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.money.protect.adapter.SmsOrangeAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrangeSmsListActivity : AppCompatActivity() {
    private lateinit var smsList: ArrayList<String>
    private lateinit var recyclerView: RecyclerView
    private lateinit var smsAdapter: SmsOrangeAdapter
    private lateinit var progressBar: ProgressBar
    @SuppressLint("MissingInflatedId", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orange_sms_list)

        val context = MainActivity()

        smsList = arrayListOf()
        recyclerView = findViewById(R.id.recyclerViewListSmsOrange)
        progressBar = findViewById(R.id.progressBarSmsList)
        smsAdapter = SmsOrangeAdapter(this, smsList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = smsAdapter

        val backButton = findViewById<ImageView>(R.id.backButtonTolauncherApps)
        backButton.setOnClickListener {
            startActivity(Intent(this, OrangeRedirectionActivity::class.java))
        }

        val btn = findViewById<Button>(R.id.buttonRefresh)

        btn.setOnClickListener {
                smsList = arrayListOf()
                recyclerView = findViewById(R.id.recyclerViewListSmsOrange)
                smsAdapter = SmsOrangeAdapter(this, smsList)
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = smsAdapter
                retrieveSms()
        }

        retrieveSms()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun retrieveSms() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(2000)
            val contentResolver = contentResolver

            val selection = "${Telephony.Sms.ADDRESS} = ?" // Sélectionnez les SMS avec l'adresse spécifique
            val selectionArgs = arrayOf("+454")

            val uri = Telephony.Sms.Inbox.CONTENT_URI
            val cursor = contentResolver.query(uri, null, selection, selectionArgs, null)

            withContext(Dispatchers.Main){
                smsList.clear()
                if (cursor != null) {
                    progressBar.visibility = View.INVISIBLE
                    while (cursor.moveToNext()) {
                        val bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY)
                        val smsBody = cursor.getString(bodyIndex)
                        smsList.add(smsBody)
                    }
                    cursor.close()
                    // Notifiez l'adaptateur que les données ont changé.
                    smsAdapter.notifyDataSetChanged()
                }
            }
        }
    }

}