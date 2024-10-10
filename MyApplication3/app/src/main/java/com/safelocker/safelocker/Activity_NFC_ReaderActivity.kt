package com.safelocker.safelocker

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.Color
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
import com.google.android.material.snackbar.Snackbar
import com.safelocker.R
import com.safelocker.databinding.ActivityNfcReaderBinding
import java.io.IOException

class Activity_NFC_ReaderActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private lateinit var binding: ActivityNfcReaderBinding
    private lateinit var mNfcAdapter: NfcAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define a orientação da atividade como retrato
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding = ActivityNfcReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa o adaptador NFC
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (mNfcAdapter == null) {
            // Dispositivo não possui NFC
            mensagemNegativa(binding.root,"Este dispositivo não possui tecnologia NFC.")
        } else {
            if (!mNfcAdapter.isEnabled) {
                // NFC está desabilitado, solicita ao usuário para habilitar
                showNfcDisabledDialog()
            } else {
                mensagemPositive(binding.root,"Aproxime o dispositivo da tag NFC.")
            }
        }
    }

    private fun showNfcDisabledDialog() {
        val builder = AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
        builder.setTitle("NFC Desabilitado")
        builder.setMessage("Por favor, habilite NFC")
        builder.setPositiveButton("Config.") { _, _ ->
            startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        }
        val myDialog = builder.create()
        myDialog.setOnShowListener {
            myDialog.getButton(DialogInterface.BUTTON_POSITIVE)?.let { button ->
                button.setTextColor(ContextCompat.getColor(this, R.color.Cor_padrao)) // Define a cor do texto do botão
                button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.SegundaCorPadrao)) // Define a cor de fundo do botão
            }
        }
        myDialog.setCanceledOnTouchOutside(false)
        myDialog.show()
    }

    override fun onTagDiscovered(tag: Tag) {
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()
                val ndefMessage = ndef.ndefMessage
                if (ndefMessage != null && ndefMessage.records.isNotEmpty()) {
                    val ndefRecord = ndefMessage.records[0]
                    val payload = ndefRecord.payload

                    val text = extractTextFromNdefPayload(payload)

                    // Separe o texto em locacaoId e photoUrl
                    val data = text.split(",")
                    if (data.size == 2) {
                        val locacaoId = data[0]
                        val photoUrl = data[1]



                        runOnUiThread {
                            mensagemPositive(binding.root,"Lido com sucesso")
                            // Iniciar uma nova activity
                            val intent = Intent(this, Activity_ConfirmarLocador::class.java)
                            intent.putExtra("LocacaoId", locacaoId)
                            intent.putExtra("PhotoUrl", photoUrl)
                            startActivity(intent)
                        }
                    } else {
                        runOnUiThread {
                           mensagemNegativa(binding.root,"Erro,Tente novmente")
                        }
                    }
                } else {
                    runOnUiThread {
                        mensagemNegativa(binding.root,"Tag NFC vazia,Tente novamente")
                    }
                }
            } catch (e: IOException) {
                runOnUiThread {
                    mensagemNegativa(binding.root,"Erro, Tente novamente")
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

    private fun extractTextFromNdefPayload(payload: ByteArray): String {
        val encoding = if ((payload[0].toInt() and 0x80) == 0) Charsets.UTF_8 else Charsets.UTF_16
        val languageCodeLength = payload[0].toInt() and 0x3F
        return String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, encoding)
    }


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

    override fun onPause() {
        super.onPause()
        mNfcAdapter.disableReaderMode(this) // Desabilita o modo de leitura da NFC
    }
}
