package com.safelocker.safelocker

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.safelocker.databinding.ActivityQrCodeBinding

private lateinit var binding:ActivityQrCodeBinding
class Activity_QrCode : AppCompatActivity() {

    // Referência ao Firestore
    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        // Inflar o layout usando o ViewBinding
        binding = ActivityQrCodeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        // Define a orientação da atividade como retrato
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT


        // Recebendo os dados do intent
        val id = intent.getStringExtra("Unidade")
        val gerente = intent.getStringExtra("gerente")
        val endereco = intent.getStringExtra("endereco")
        val armario = intent.getStringExtra("Armario")
        val numero = intent.getDoubleExtra("Numero", 0.0)
        val nomeDoUsuario = intent.getStringExtra("emailuser")
        val locacaoId = intent.getStringExtra("LocacaoId")

        val numeroSemDecimal = if (numero % 1 == 0.0) {
            // Se o número for um número inteiro, converte para Int
            numero.toInt().toString()
        } else {
            // Se o número tiver decimais, mantém como Double
            numero.toString()
        }

        // Definindo os valores nos TextViews usando o ViewBinding
        binding.TvUnidade.text = id
        binding.TvRua.text= endereco
        binding.TvGerente.text = gerente
        binding.TvNumero.text = ", $numeroSemDecimal"

        generateQRCode("$locacaoId")

        // Voltar para a tela inicial
        binding.icVoltar.setOnClickListener {
            val intent = Intent(this,Activity_Tela_principal_cliente::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            this.finish()
        }

    }
    // Método para gerar o QR code
    private fun generateQRCode(content: String) {
        try {

            val width = 400
            val height = 400
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            binding.dynamicQrCodeImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
