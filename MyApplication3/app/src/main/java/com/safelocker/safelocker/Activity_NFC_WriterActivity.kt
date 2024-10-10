package com.safelocker.safelocker

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.safelocker.R
import com.safelocker.databinding.ActivityNfcactivityBinding
import java.io.IOException

class Activity_NFC_WriterActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private lateinit var binding: ActivityNfcactivityBinding
    private var contador: Double = 0.0
    private lateinit var mNfcAdapter: NfcAdapter
    private var mConcatenatedData: String? = null // Variável para armazenar a string concatenada

    private var locacaoId: String? = null
    private var photoUrl: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define a orientação da atividade como retrato
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

//        enableEdgeToEdge()
        binding = ActivityNfcactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicialização dos elementos de UI

        locacaoId = intent.getStringExtra("LocacaoId")
        photoUrl = intent.getStringExtra("photoUrl")

        // Concatenar locacaoId e photoUrl com um delimitador, por exemplo, uma vírgula
        mConcatenatedData = "$locacaoId,$photoUrl"

        // Inicialização do adaptador NFC
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        VerificaStatusNFC()


        // Checa se o dispositivo possui tecnologia NFC e se está ativada
        if (mNfcAdapter == null) {
            binding.TvAtiveONFC.visibility = View.VISIBLE
            mensagemNegativa(binding.root,"Este dispositivo não possui tecnologia NFC.")

        } else {
            VerificaStatusNFC()
        }

        contador = intent.getIntExtra("contador", 0).toDouble()

        binding.btnProsseguir.setOnClickListener {
            contador -= 1
            val intent = Intent(this, Activity_Camera::class.java)
            intent.putExtra("contador", contador.toInt())
            intent.putExtra("LocacaoId", locacaoId)
            startActivity(intent)
        }
    }

    // Método chamado quando uma tag NFC é descoberta
    override fun onTagDiscovered(tag: Tag) {
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()


                // Escreve na tag NFC
                val record = NdefRecord.createTextRecord(null, mConcatenatedData)
                val message = NdefMessage(record)
                ndef.writeNdefMessage(message)

                runOnUiThread {
                   mensagemPositive(binding.root,"Tag escrita com sucesso.")

                    if(contador == 1.0){
                    // Inicia a nova activity
                    val intent = Intent(this, Activity_LocacaoConcluida::class.java)
                    intent.putExtra("LocacaoId", locacaoId)
                    startActivity(intent)
                    }

                    if(contador == 2.0){
                        binding.btnProsseguir.visibility = View.VISIBLE
                    }
                }

            } catch (e: IOException) {
                runOnUiThread {
                    mensagemNegativa(binding.root,"Ocorreu um erro,Tente novamente")
                }
            } finally {
                try {
                    ndef.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else {
            runOnUiThread {
                mensagemNegativa(binding.root,"Tag NFC inválida,Tente novamente")
            }
        }
    }

    // Método chamado quando a atividade está sendo retomada
    override fun onResume() {
        super.onResume()
        if (mNfcAdapter != null) {
            // Configura o modo de leitura da NFC
            val options = Bundle()
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
            mNfcAdapter.enableReaderMode(
                this,
                this,
                NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_F or
                        NfcAdapter.FLAG_READER_NFC_V,
                options
            )
        }
    }

    // Método chamado quando a atividade é pausada
    override fun onPause() {
        super.onPause()
        mNfcAdapter.disableReaderMode(this) // Desabilita o modo de leitura da NFC
    }


    private fun VerificaStatusNFC(){
        if (!mNfcAdapter.isEnabled) {
            binding.TvAtiveONFC.visibility = View.VISIBLE
            val builder = AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
            builder.setTitle("NFC Desabilitado")
            builder.setMessage("Habilite NFC")
            builder.setPositiveButton("Config.") { _, _ ->
                startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                this.finish()
            }
            val myDialog = builder.create()
            myDialog.setOnShowListener {
                myDialog.getButton(DialogInterface.BUTTON_POSITIVE)?.let { button ->
                    button.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.Cor_padrao
                        )
                    ) // Define a cor do texto do botão
                    button.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this,
                            R.color.SegundaCorPadrao
                        )
                    ) // Define a cor de fundo do botão
                }
            }

            myDialog.setCanceledOnTouchOutside(false)
            myDialog.show()
        } else {
               mensagemPositive(binding.root,"Aproxime o dispositivo da tag NFC.")
        }


    }

    private fun mensagemNegativa(view: View, mensagem: String) {
        val snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(Color.parseColor("#F3787A"))
        snackbar.setTextColor(Color.parseColor("#FFFFFF"))
        snackbar.show()
    }


    private fun mensagemPositive(view: View, mensagem: String) {
        val snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(Color.parseColor("#78F37A"))
        snackbar.setTextColor(Color.parseColor("#FFFFFF"))
        snackbar.show()
    }

    private fun restartActivity() {
        // Reinicia a atividade
        val intent = Intent(this, Activity_NFC_WriterActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}

