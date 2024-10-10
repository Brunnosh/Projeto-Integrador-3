package com.safelocker.safelocker

import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.safelocker.R
import com.safelocker.databinding.ActivityCameraBinding
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Activity_Camera : AppCompatActivity() {

    // Variáveis para a câmera
    private lateinit var binding: ActivityCameraBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private var imageCapture: ImageCapture? = null
    private lateinit var imgCaptureExecutor: ExecutorService
    private var currentPhotoFile: File? = null

    // Variável para a contagem de pessoas
    private var contador: Double = 0.0

    // Variável para o diálogo de progresso
    private lateinit var progressDialog: Dialog
    private val handler = android.os.Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Define a orientação da atividade como retrato
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        handler.postDelayed({
            hideProgressDialog()
        }, 15000)

        // Inicializa as variáveis
        contador = intent.getIntExtra("contador", 0).toDouble()
        val locacaoId = intent.getStringExtra("LocacaoId")
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        imgCaptureExecutor = Executors.newSingleThreadExecutor()

        // Inicia a câmera
        startCamera()

        // Inicializa o diálogo de progresso
        InicializarProgressBar()

        // Configura os botões
        binding.btnTakePhoto.setOnClickListener {
            takePhoto()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                blinkPreview()
            }
        }

        // Botão para cancelar a foto atual e tirar outra
        binding.btnCancel.setOnClickListener {
            binding.ivPhotoPreview.visibility = View.GONE
            binding.btnAccept.visibility = View.GONE
            binding.btnCancel.visibility = View.GONE
            binding.btnTakePhoto.visibility = View.VISIBLE
        }

        // Botão para aceitar a foto tirada
        binding.btnAccept.setOnClickListener {
            currentPhotoFile?.let { file ->
                // Verifica o contador e carrega as fotos correspondentes
                if (contador == 1.0) {
                    showProgressDialog()
                    uploadToFirebaseStorage(file, locacaoId, 1) { photoUrl ->
                        startNFCActivity(photoUrl, locacaoId)
                    }
                } else {
                    showProgressDialog()
                    uploadToFirebaseStorage(file, locacaoId, 2) { photoUrl ->
                        startNFCActivity(photoUrl, locacaoId)
                    }
                }
            }
        }
    }

    // Inicia a câmera
    private fun startCamera() {
        cameraProviderFuture.addListener({
            imageCapture = ImageCapture.Builder().build()

            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.e("CameraPreview", "Falha ao abrir a câmera")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // Tira uma foto
    private fun takePhoto() {
        imageCapture?.let {
            val fileName = "FOTO_JPEG_${System.currentTimeMillis()}.jpeg"
            val file = File(externalMediaDirs[0], fileName)
            currentPhotoFile = file
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

            it.takePicture(outputFileOptions, imgCaptureExecutor, object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.i("CameraPreview", "A imagem foi salva no diretório")
                    runOnUiThread {
                        binding.ivPhotoPreview.setImageURI(Uri.fromFile(file))
                        binding.ivPhotoPreview.visibility = View.VISIBLE
                        binding.btnAccept.visibility = View.VISIBLE
                        binding.btnCancel.visibility = View.VISIBLE
                        binding.btnTakePhoto.visibility = View.GONE
                    }
                }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(binding.root.context, "Erro ao tirar foto", Toast.LENGTH_LONG).show()
                        Log.e("CameraPreview", "Erro ao gravar arquivo da foto: $exception")
                    }
                }
            )
        }
    }

    // Salva a foto no FirebaseStorage
    private fun uploadToFirebaseStorage(file: File, locacaoId: String?, photoNumber: Int, callback: (String) -> Unit) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val photoRef: StorageReference = storageRef.child("photos/${file.name}")

        val uri = Uri.fromFile(file)
        val uploadTask = photoRef.putFile(uri)

        uploadTask.addOnSuccessListener {
            Log.i("FirebaseStorage", "Upload bem-sucedido")
            photoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                Log.i("FirebaseStorage", "URL da imagem: $downloadUrl")
                locacaoId?.let { id ->
                    if (photoNumber == 1) {
                        updateLocacaoWithPhotoUrl(id, downloadUrl.toString(), 1)
                    } else if (photoNumber == 2) {
                        updateLocacaoWithPhotoUrl(id, downloadUrl.toString(), 2)
                    }
                }
                callback(downloadUrl.toString())
            }.addOnFailureListener {
                Log.e("FirebaseStorage", "Erro ao obter URL da imagem: ${it.message}")
            }
        }.addOnFailureListener {
            Log.e("FirebaseStorage", "Erro ao fazer upload: ${it.message}")
            Toast.makeText(this, "Erro ao fazer upload da foto", Toast.LENGTH_SHORT).show()
        }
    }

    // Salva a foto no FirebaseFirestore
    private fun updateLocacaoWithPhotoUrl(locacaoId: String, photoUrl: String, photoNumber: Int) {
        val db = FirebaseFirestore.getInstance()
        val locacaoRef = db.collection("locacao").document(locacaoId)

        val fieldName = if (photoNumber == 1) "photoUrl" else "photoUrl2"

        locacaoRef.update(fieldName, photoUrl)
            .addOnSuccessListener {
                Log.i("Firestore", "URL da foto $photoNumber atualizada na locação")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Erro ao atualizar URL da foto $photoNumber na locação: ${e.message}")
            }
    }

    // Inicia a tela de gravar dados no NFC
    private fun startNFCActivity(photoUrl: String, locacaoId: String?) {
        val intent = Intent(this, Activity_NFC_WriterActivity::class.java)
        intent.putExtra("contador", contador.toInt())
        intent.putExtra("LocacaoId", locacaoId)
        intent.putExtra("photoUrl", photoUrl)
        startActivity(intent)
        hideProgressDialog()
    }

    // Mostra um blink na tela para simular uma câmera
    @RequiresApi(Build.VERSION_CODES.M)
    private fun blinkPreview() {
        binding.root.postDelayed({
            binding.root.foreground = ColorDrawable(Color.WHITE)
            binding.root.postDelayed({
                binding.root.foreground = null
            }, 50)
        }, 100)
    }

    // Inicia o dialogo de progresso (barra de progresso com o texto carregando)
    private fun InicializarProgressBar(){

        progressDialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_progress, null)

        progressDialog.setContentView(view)
        progressDialog.setCancelable(false)

        hideProgressDialog()


    }

    // Mostra o diálogo de progresso
    private fun showProgressDialog() {
        progressDialog.show()
        val textView: TextView = progressDialog.findViewById(R.id.text)
        textView.text = "Carregando..."
    }

    // Esconde o dialogo de progresso
    private fun hideProgressDialog() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }
}
