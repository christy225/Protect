package com.money.protect.popup

import android.annotation.SuppressLint
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.money.protect.MainDetailOperationActivity
import com.money.protect.R
import com.ortiz.touchview.TouchImageView

class AfficheIdentite2(
    private val context: MainDetailOperationActivity
) : Dialog(context) {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.affiche_identite2_popup)

        val imagePreview = findViewById<TouchImageView>(R.id.imagePreviewDetails2)

        val closeButton = findViewById<ImageView>(R.id.imageView10)
        closeButton.setOnClickListener{
            dismiss()
        }
        val url2 = context.lienUrl2()

        Glide.with(context).load(Uri.parse(url2)).into(imagePreview)
    }
}