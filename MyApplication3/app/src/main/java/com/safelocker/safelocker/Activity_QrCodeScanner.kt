package com.safelocker.safelocker

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator
import com.safelocker.R
import com.safelocker.databinding.ActivityQrCodeScannerBinding

class Activity_QrCodeScanner : AppCompatActivity() {

    private lateinit var binding: ActivityQrCodeScannerBinding
    private var locacaoId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Define a orientação da atividade como retrato
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT



        val intentIntegrator = IntentIntegrator(this@Activity_QrCodeScanner)
        intentIntegrator.setOrientationLocked(true)
        intentIntegrator.setPrompt("Escanear Activity_QrCode")
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        intentIntegrator.initiateScan()

        binding.icVoltar.setOnClickListener {
            startActivity(Intent(this, Activity_Tela_Principal_Gerente::class.java))
        }

        binding.btnProsseguir.setOnClickListener {
            startActivity(Intent(this, Activity_Tela_Escolher_Pessoas::class.java).putExtra("LocacaoId", locacaoId))
        }

        // Desabilitar o botão "Prosseguir" por padrão
        binding.btnProsseguir.isEnabled = false
        binding.btnTentarNovamente.visibility = View.INVISIBLE
        binding.btnTentarNovamente.setOnClickListener {
            ReStartaTela()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (intentResult != null) {
            val contents = intentResult.contents
            if (contents != null) {
                locacaoId = contents
                fetchLocacaoData(locacaoId)
            } else {
                mensagemNegativa(binding.root,"Falha ao escanear QR Code")
                binding.btnTentarNovamente.visibility = View.VISIBLE
                binding.btnProsseguir.backgroundTintList = ContextCompat.getColorStateList(this, R.color.grey_ligth)

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun fetchLocacaoData(locacaoId: String) {
        val db = FirebaseFirestore.getInstance()
        val locacaoRef = db.collection("locacao").document(locacaoId)

        locacaoRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val locacaoData = document.data

                    val tempo = locacaoData?.get("tempo") as? String
                    if (tempo != null) {
                        binding.tvTempo.text = "Tempo de locação: $tempo"
                    } else {
                        mensagemNegativa(binding.root,"Tempo de locação não encontrado no documento de locação")
                    }

                    val userId = locacaoData?.get("userId") as? String
                    if (userId != null) {
                        fetchNomeUsuario(userId)
                    } else {
                        mensagemNegativa(binding.root,"ID de usuário não encontrado no documento de locação" )
                    }

                    val armarioId = locacaoData?.get("armarioId") as? String
                    if (armarioId != null) {
                        fetchArmarioData(armarioId)
                    } else {
                        mensagemNegativa(binding.root,"ID de armário não encontrado no documento de locação" )
                    }

                    // Habilitar o botão "Prosseguir"
                    binding.btnProsseguir.isEnabled = true
                } else {
                    showInvalidQRCodePopup()
                    Log.d("Activity_QrCodeScanner", "Documento não encontrado")
                    binding.btnProsseguir.isEnabled = false
                    binding.btnTentarNovamente.visibility = View.VISIBLE
                    binding.btnProsseguir.backgroundTintList = ContextCompat.getColorStateList(this, R.color.grey_ligth)

                }
            }
            .addOnFailureListener { exception ->
                Log.d("Activity_QrCodeScanner", "Falha ao obter documento", exception)
                mensagemNegativa(binding.root,"Erro ao ler o QR Code")
                binding.btnProsseguir.isEnabled = false
                binding.btnTentarNovamente.visibility = View.VISIBLE
                binding.btnProsseguir.backgroundTintList = ContextCompat.getColorStateList(this, R.color.grey_ligth)
            }
    }

    private fun fetchArmarioData(armarioId: String) {
        val db = FirebaseFirestore.getInstance()
        val armariosRef = db.collection("armarios")

        armariosRef.whereEqualTo("armarioId", armarioId).get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    var armarioDisponivel = false
                    for (document in documents) {
                        val status = document.getString("status")
                        if (status == "disponível") {
                            armarioDisponivel = true
                            break
                        }
                    }
                    if (!armarioDisponivel) {
                        showIndisponibleQRCodePopup()
                    }
                } else {
                    Log.d("Activity_QrCodeScanner", "Nenhum documento encontrado")
                    mensagemNegativa(binding.root,"Nenhum documento encontrado para o ID do armário: $armarioId")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Activity_QrCodeScanner", "Erro ao obter documento", exception)
                mensagemNegativa(binding.root,"Erro ao obter o documento para o ID do armário: $armarioId")
            }
    }

    private fun showInvalidQRCodePopup() {
        val dialogView = layoutInflater.inflate(R.layout.popup_invalid_qr_code, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        builder.setCancelable(true)

        val dialog = builder.create()

        dialog.setOnCancelListener {
            startActivity(Intent(this, Activity_Tela_Principal_Gerente::class.java))
            this.finish()
        }

        val btnOK = dialogView.findViewById<ImageView>(R.id.btnClose)
        btnOK.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, Activity_Tela_Principal_Gerente::class.java))
            this.finish()
        }

        dialog.show()
    }

    private fun showIndisponibleQRCodePopup() {
        val dialogView = layoutInflater.inflate(R.layout.popup_indisponible_qr_code, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        builder.setCancelable(true)

        val dialog = builder.create()

        dialog.setOnCancelListener {
            startActivity(Intent(this, Activity_Tela_Principal_Gerente::class.java))
            this.finish()
        }

        val btnOK = dialogView.findViewById<ImageView>(R.id.btnClose)
        btnOK.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, Activity_Tela_Principal_Gerente::class.java))
            this.finish()
        }

        dialog.show()
    }

    private fun fetchNomeUsuario(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val pessoaRef = db.collection("pessoa").document(userId)

        pessoaRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val nomeUsuario = document.data?.get("nome") as? String
                    if (nomeUsuario != null) {
                        binding.tvNome.text = "Nome: $nomeUsuario"
                    } else {
                        mensagemNegativa(binding.root,"Nome não encontrado para o ID de usuário: $userId")
                    }
                } else {
                    Log.d("Activity_QrCodeScanner", "Nenhum documento encontrado para o ID de usuário: $userId")
                    mensagemNegativa(binding.root,"Nenhum documento encontrado para o ID de usuário: $userId")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Activity_QrCodeScanner", "Falha ao obter documento", exception)
                mensagemNegativa(binding.root,"Erro ao obter o documento para o ID de usuário: $userId")
            }
    }
    private fun mensagemNegativa(view: View, mensagem: String) {
        val snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(Color.parseColor("#F3787A"))
        snackbar.setTextColor(Color.parseColor("#FFFFFF"))
        snackbar.show()
    }

   //@ feito por cicera
    private fun mensagemPositive(view: View, mensagem: String) {
        val snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(Color.parseColor("#78F37A"))
        snackbar.setTextColor(Color.parseColor("#FFFFFF"))
        snackbar.show()
    }
    private fun ReStartaTela(){
        startActivity(Intent(this,Activity_QrCodeScanner::class.java))
        this.finish()
    }

}
