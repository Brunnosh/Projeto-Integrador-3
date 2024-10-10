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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.safelocker.R
import com.safelocker.databinding.ActivityNfcClearBinding
import java.io.IOException

class Activity_NFC_ClearActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {
    private lateinit var binding: ActivityNfcClearBinding
    private lateinit var mNfcAdapter: NfcAdapter
    private var locacaoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define a orientação da atividade como retrato
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

//        enableEdgeToEdge()
        binding = ActivityNfcClearBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialização do adaptador NFC
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)

        locacaoId = intent.getStringExtra("LocacaoId")

        // Checa se o dispositivo possui tecnologia NFC e se está ativada
        if (mNfcAdapter == null) {
            mensagemNegativa(binding.root,"Este dispositivo não possui tecnologia NFC.")
        } else {
            if (!mNfcAdapter.isEnabled) {
                val builder = AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                builder.setTitle("NFC Desabilitado")
                builder.setMessage("Por favor Habilite NFC")
                builder.setPositiveButton("Config.") { _, _ -> startActivity(Intent(Settings.ACTION_NFC_SETTINGS)) }
                val myDialog = builder.create()
                myDialog.setOnShowListener {
                    myDialog.getButton(DialogInterface.BUTTON_POSITIVE)?.let { button ->
                        button.setTextColor(ContextCompat.getColor(this, R.color.Cor_padrao)) // Define a cor do texto do botão
                        button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.SegundaCorPadrao)) // Define a cor de fundo do botão
                    }
                }
                myDialog.setCanceledOnTouchOutside(false)
                myDialog.show()
            } else {
                mensagemNegativa(binding.root,"Dados criados. Aproxime o dispositivo da tag NFC.")
            }
        }

    }

    // Método chamado quando uma tag NFC é descoberta
    override fun onTagDiscovered(tag: Tag) {
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()

                // Escreve na tag NFC
                val record = NdefRecord.createTextRecord(null, "")
                val message = NdefMessage(record)
                ndef.writeNdefMessage(message)

                runOnUiThread {
                    mensagemPositive(binding.root,"Tag Limpada com sucesso")
                    // Inicia a nova activity
                    startActivity(Intent(this, Activity_LocacaoEncerrada::class.java).putExtra("LocacaoId", locacaoId))

                }

            } catch (e: IOException) {
                runOnUiThread {
                    mensagemNegativa(binding.root,"Erro. Tente novamente")
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

    // Método chamado quando a atividade é pausada
    override fun onPause() {
        super.onPause()
        mNfcAdapter.disableReaderMode(this) // Desabilita o modo de leitura da NFC
    }

}
