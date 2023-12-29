package com.money.protect.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.money.protect.MainActivity
import com.money.protect.R
import com.money.protect.fragment_assistant.national.MoovFragment
import com.money.protect.fragment_assistant.national.TresorFragment
import com.money.protect.popup.OrangePopup
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class SmsOrangeAdapter(
    private val context: MainActivity,
    private val smsList: ArrayList<String>,
) : RecyclerView.Adapter<SmsOrangeAdapter.SmsViewHolder>() {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    class SmsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val typeOperation: TextView = itemView.findViewById(R.id.operationOrange)
        val numeroOrange: TextView = itemView.findViewById(R.id.numeroOrange)
        val montantOrange: TextView = itemView.findViewById(R.id.montantOrange)
        val idTransaction: TextView = itemView.findViewById(R.id.idTransactionOrange)
        val soldeOrange: TextView = itemView.findViewById(R.id.soldeOrange)
        val buttonPopup: Button = itemView.findViewById(R.id.openOrangePopup)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxOrange_compte2)
        val checkBoxSyntaxe: CheckBox = itemView.findViewById(R.id.checkBoxOrange_syntaxe)
        val sectionUpload: CardView = itemView.findViewById(R.id.section_upload_input_orange_compte2)
        val buttonregister: Button = itemView.findViewById(R.id.btn_register_input_orange_compte2)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar_input_orange_compte2)
        val upload: Button = itemView.findViewById(R.id.uploadPhotoOrange_compte2)
        val link1: ImageView = itemView.findViewById(R.id.assistant_link_tresor_compte2)
        val link2: ImageView = itemView.findViewById(R.id.assistant_link_moov_compte2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_sms_orange, parent, false)
        return SmsViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: SmsViewHolder, position: Int) {

        val sms = smsList[position]
        val message = sms
        var montantTransac: String? = null

        val format = DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.getDefault()))

        if (message != null) {
            if (message.contains("Retrait"))
            {
                holder.typeOperation.text = "Retrait"
                val tableauDeChaines = message.split(".")
                val chaineDesireeNumero = tableauDeChaines.getOrNull(3)

                val textNumero = chaineDesireeNumero?.split(" ")
                val valNumero = textNumero?.getOrNull(2)

                holder.numeroOrange.text = valNumero

                val chaineDesireeMontant = tableauDeChaines.getOrNull(4)
                val textMontant = chaineDesireeMontant?.split(" ")
                val valMontant = textMontant?.getOrNull(2)

                montantTransac = valMontant
                holder.montantOrange.text = format.format(valMontant?.toInt()).toString()

                val chaineDesireeTransaction = tableauDeChaines.getOrNull(6)
                val textIdTransac = chaineDesireeTransaction?.split(" ")

                val valIdTransac1= textIdTransac?.getOrNull(2)
                val valIdTransac2= tableauDeChaines.getOrNull(7)
                val valIdTransac3= tableauDeChaines.getOrNull(8)

                holder.idTransaction.text = "$valIdTransac1.$valIdTransac2.$valIdTransac3"

                val chaineDesireeSolde = tableauDeChaines.getOrNull(1)
                val textSolde = chaineDesireeSolde?.split(" ")
                val valSolde = textSolde?.getOrNull(3)
                holder.soldeOrange.text = format.format(valSolde?.toInt()).toString()

            }else if (message.contains("Depot")){
                holder.typeOperation.text = "Dépôt"
                val tableauDeChaines2 = message.split(".")
                val chaineDesiree2 = tableauDeChaines2.getOrNull(0)

                val textNumero2 = chaineDesiree2?.split(" ")
                val valNumero2 = textNumero2?.getOrNull(2)

                holder.numeroOrange.text = valNumero2

                val chaineDesiree3 = tableauDeChaines2.getOrNull(1)
                val textMontant3 = chaineDesiree3?.split(" ")
                val valMontant3 = textMontant3?.getOrNull(2)

                montantTransac = valMontant3
                holder.montantOrange.text = format.format(valMontant3?.toInt()).toString()

                val chaineDesireeTransactionX = tableauDeChaines2.getOrNull(4)
                val textIdTransac1 = chaineDesireeTransactionX?.split(" ")

                val valIdTransac1= textIdTransac1?.getOrNull(4)
                val valIdTransac2= tableauDeChaines2.getOrNull(5)

                val xx = tableauDeChaines2.getOrNull(6)
                val dd = xx?.split(',')
                val valIdTransac3 = dd?.getOrNull(0)

                holder.idTransaction.text = "$valIdTransac1.$valIdTransac2.$valIdTransac3"

                val chaineDesireeSolde = tableauDeChaines2.getOrNull(6)
                val textSolde = chaineDesireeSolde?.split(" ")
                val valSolde = textSolde?.getOrNull(3)
                holder.soldeOrange.text = format.format(valSolde?.toInt()).toString()
            }
        }

        holder.link1.setOnClickListener {
            if (holder.buttonregister.text == "enregistrer opération")
            {
                Toast.makeText(context, "Vous n'avez pas enregistré la transaction", Toast.LENGTH_SHORT).show()
            }else {
                context.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, TresorFragment(context))
                    .addToBackStack(null)
                    .commit()
            }
        }

        holder.link2.setOnClickListener {
            if (holder.buttonregister.text == "enregistrer opération")
            {
                Toast.makeText(context, "Vous n'avez pas enregistré la transaction", Toast.LENGTH_SHORT).show()
            }else {
                context.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, MoovFragment(context))
                    .addToBackStack(null)
                    .commit()
            }
        }

        holder.buttonPopup.setOnClickListener {
            OrangePopup(context).show()
        }

        holder.buttonregister.text = "effectuer la transaction"

        holder.upload.setOnClickListener {
            ImagePicker.with(context)
                .crop()
                .cameraOnly()    			//Crop image(Optional), Check Customization for more option
                .compress(100)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(480, 450)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        holder.buttonregister.setOnClickListener {
            if (holder.buttonregister.text == "effectuer la transaction"){
                if (holder.checkBoxSyntaxe.isChecked)
                {
                    if (holder.typeOperation.text == "Dépôt")
                    {
                        val syntaxe = Uri.encode("#") + "145*1" + Uri.encode("#")
                        val callIntent = Intent(Intent.ACTION_CALL)
                        callIntent.data = Uri.parse("tel:$syntaxe")
                        context.startActivity(callIntent)
                        Handler(Looper.getMainLooper()).postDelayed({
                            holder.buttonregister.text = "enregistrer opération" // Remplacez "Nouveau Texte" par le texte que vous souhaitez afficher
                        }, 5000)
                    }else if(holder.typeOperation.text == "Retrait"){
                        val syntaxe = Uri.encode("#") + "145*2" + Uri.encode("#")
                        val callIntent = Intent(Intent.ACTION_CALL)
                        callIntent.data = Uri.parse("tel:$syntaxe")
                        context.startActivity(callIntent)
                        Handler(Looper.getMainLooper()).postDelayed({
                            holder.buttonregister.text = "enregistrer opération" // Remplacez "Nouveau Texte" par le texte que vous souhaitez afficher
                        }, 5000)
                    }
                }else{
                    val packageName = "com.orange.ci.ompdv"  // Package name de Facebook
                    val className = "com.orange.ci.ompdv.MainActivity"  // Nom de la classe de l'activité que vous voulez démarrer

                    val intent = Intent()
                    intent.component = ComponentName(packageName, className)
                    try {
                        context.startActivity(intent)
                        Handler(Looper.getMainLooper()).postDelayed({
                            holder.buttonregister.text = "enregistrer opération" // Remplacez "Nouveau Texte" par le texte que vous souhaitez afficher
                        }, 3000)

                    } catch (e: ActivityNotFoundException) {
                        // Gérez le cas où L'Application WAVE Agent n'est pas installé ou ne permet pas d'ouvrir cette activité
                        val intent = AlertDialog.Builder(context)
                        intent.setTitle("Infos")
                        intent.setMessage("Veuillez installer l'application Orange pour PDV")
                        intent.show()
                    }
                }
            }else if (holder.buttonregister.text == "enregistrer opération"){

                holder.progressBar.visibility = View.VISIBLE
                holder.buttonregister.isEnabled = false

                // Générer la date
                val current = LocalDateTime.now()
                val formatterDate = DateTimeFormatter.ofPattern("d-M-yyyy")
                val dateFormatted = current.format(formatterDate)

                // Générer l'heure
                val formatterHour = DateTimeFormatter.ofPattern("HH:mm")
                val hourFormatted = current.format(formatterHour)

                val uid = UUID.randomUUID().toString()

                val operationMap = hashMapOf(
                    "id" to auth.currentUser?.uid,
                    "date" to dateFormatted,
                    "heure" to hourFormatted,
                    "operateur" to "orange",
                    "telephone" to holder.numeroOrange.text.toString(),
                    "montant" to montantTransac.toString(),
                    "typeoperation" to holder.typeOperation.text.toString(),
                    "statut" to true,
                    "url" to "null",
                    "idDoc" to uid
                )

                db.collection("operation")
                    .document(uid)
                    .set(operationMap)
                    .addOnCompleteListener{
                        if (it.isSuccessful)
                        {
                            Toast.makeText(context, "Enregistré avec succès", Toast.LENGTH_SHORT).show()
                            holder.buttonregister.text = "effectuer la transaction"
                            holder.buttonregister.isEnabled = true

                            holder.progressBar.visibility = View.GONE
                        }
                    }.addOnFailureListener {
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("Alerte")
                        builder.setMessage(R.string.onFailureText)
                        builder.show()
                        holder.buttonregister.text = "effectuer la transaction"
                        holder.buttonregister.isEnabled = true
                    }
            }
        }

        holder.checkBox.setOnClickListener {
            if (holder.checkBox.isChecked)
            {
                holder.sectionUpload.visibility = View.VISIBLE
            }else{
                holder.sectionUpload.visibility = View.GONE
            }
        }

    }

    override fun getItemCount(): Int {
        return smsList.take(1).size
    }

}


