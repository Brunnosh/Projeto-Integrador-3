package com.safelocker.safelocker

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.safelocker.R
import com.safelocker.databinding.ActivityAlugarArmarioBinding
import java.util.Calendar

private lateinit var binding: ActivityAlugarArmarioBinding
private lateinit var auth: FirebaseAuth

class Activity_Alugar_Armario : AppCompatActivity() {

    // Referência ao Firestore
    private val db = FirebaseFirestore.getInstance()
    private var checkBoxText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Inicialização do Firebase Auth
        auth = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)

        // Define a orientação da atividade como retrato
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Inflar o layout usando o ViewBinding
        binding = ActivityAlugarArmarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recebendo os dados do intent
        val id = intent.getStringExtra("id")
        val nome = intent.getStringExtra("nome")
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)
        val codigo_armario = intent.getStringExtra("codigo_armario")
        val numero = intent.getDoubleExtra("numero", 0.0)
        val preco = intent.getStringExtra("preco")
        val preco_trinta_min = intent.getStringExtra("preco_trinta_min")
        val preco_uma_hora = intent.getStringExtra("preco_uma_hora")
        val preco_duas_horas = intent.getStringExtra("preco_duas_horas")
        val preco_quatro_horas = intent.getStringExtra("preco_quatro_horas")
        val referencia = intent.getStringExtra("referencia")
        val rua = intent.getStringExtra("rua")
        val distancia = intent.getDoubleExtra("distancia", 0.0)



        // Formatação da distância com duas casas decimais
        val distanciaFormatada = String.format("%.2f", distancia)

        val numeroSemDecimal = if (numero % 1 == 0.0) {
            // Se o número for um número inteiro, converte para Int
            numero.toInt().toString()
        } else {
            // Se o número tiver decimais, mantém como Double
            numero.toString()
        }

        // Definindo os valores nos TextViews usando o ViewBinding
        binding.tvAba.text = "A $distanciaFormatada Km de você"
        binding.tvAba2.text = "Ponto ${codigo_armario}"
        binding.tvAba3.text = "${rua}, ${numeroSemDecimal}. Próximo a ${referencia}"
        binding.check1.text = "$preco_trinta_min"
        binding.check2.text = "$preco_uma_hora"
        binding.check3.text = "$preco_duas_horas"
        binding.check4.text = "$preco_quatro_horas"
        binding.check5.text = "$preco"
        binding.informativo.text = "Um calção de $preco será cobrado e"


        // Configurando os OnClickListener para os CheckBoxes
        binding.checkBox1.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
            val currentMinute = currentTime.get(Calendar.MINUTE)

            // Verifica se o horário atual é depois de 17:30
            if (currentHour > 17 || (currentHour == 17 && currentMinute >= 30)) {
                // Exibe uma mensagem informando que não é possível alugar o armário por 30 minutos após 17:30
                mensagem(binding.root, "Não é possível alugar o armário por 30 minutos após as 17:30")
                // Desmarca o checkbox de 30 minutos
                binding.checkBox1.isChecked = false
            } else {
                // Permite que o usuário prossiga com a locação do armário por 30 minutos
                binding.checkBox1.isChecked = true
                binding.checkBox2.isChecked = false
                binding.checkBox3.isChecked = false
                binding.checkBox4.isChecked = false
                binding.checkBox5.isChecked = false
                checkBoxText = "30 minutos"
            }
        }


        binding.checkBox2.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
            val currentMinute = currentTime.get(Calendar.MINUTE)

            // Verifica se o horário atual é depois de 17:00
            if (currentHour > 17 || (currentHour == 17 && currentMinute >= 0)) {
                // Exibe uma mensagem informando que não é possível alugar o armário por 1 hora após 17:00
                mensagem(binding.root, "Não é possível alugar o armário por 1 hora após as 17:00")
                // Desmarca o checkbox de 1 hora
                binding.checkBox2.isChecked = false
            } else {

                binding.checkBox1.isChecked = false
                binding.checkBox2.isChecked = true
                binding.checkBox3.isChecked = false
                binding.checkBox4.isChecked = false
                binding.checkBox5.isChecked = false
                checkBoxText = "1 hora"
            }
        }

        binding.checkBox3.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
            val currentMinute = currentTime.get(Calendar.MINUTE)

            // Verifica se o horário atual é depois de 16:00
            if (currentHour > 16 || (currentHour == 16 && currentMinute >= 0)) {
                // Exibe uma mensagem informando que não é possível alugar o armário por 2 horas após 16:00
                mensagem(binding.root, "Não é possível alugar o armário por 2 horas após as 16:00")
                // Desmarca o checkbox de 2 horas
                binding.checkBox3.isChecked = false
            } else {

                binding.checkBox1.isChecked = false
                binding.checkBox2.isChecked = false
                binding.checkBox3.isChecked = true
                binding.checkBox4.isChecked = false
                binding.checkBox5.isChecked = false
                checkBoxText = "2 horas"
            }
        }

        binding.checkBox4.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
            val currentMinute = currentTime.get(Calendar.MINUTE)

            // Verifica se o horário atual é depois de 14:00
            if (currentHour > 14 || (currentHour == 14 && currentMinute >= 0)) {
                // Exibe uma mensagem informando que não é possível alugar o armário por 4 horas após 14:00
                mensagem(binding.root, "Não é possível alugar o armário por 4 horas após as 14:00")
                // Desmarca o checkbox de 4 horas
                binding.checkBox4.isChecked = false
            } else {

                binding.checkBox1.isChecked = false
                binding.checkBox2.isChecked = false
                binding.checkBox3.isChecked = false
                binding.checkBox4.isChecked = true
                binding.checkBox5.isChecked = false
                checkBoxText = "4 horas"
            }
        }

        binding.checkBox5.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)

            // Verifica se o horário atual é entre 7 e 8 horas
            if (currentHour < 7 || currentHour > 8) {
                // Exibe uma mensagem informando que só é possível escolher essa opção entre 7:00 e 8:00 horas
                mensagem(binding.root, "Só é possível escolher essa opção entre 7:00 e 8:00 horas")
                // Desmarca o checkbox
                binding.checkBox5.isChecked = false
            } else {

                val remainingHours = 18 - currentHour
                binding.checkBox1.isChecked = false
                binding.checkBox2.isChecked = false
                binding.checkBox3.isChecked = false
                binding.checkBox4.isChecked = false
                binding.checkBox5.isChecked = true
                checkBoxText = "$remainingHours horas"
                }
            }
        

        // Configurando o OnClickListener para o botão de adicionar cartão
        binding.cartaoAdicionar.setOnClickListener{
            startActivity(Intent(this,Activity_Tela_Cartao::class.java))
        }

        // Configurando o OnClickListener para o ícone de voltar
        binding.icVoltar.setOnClickListener{
                startActivity(Intent(this, Activity_Tela_principal_cliente::class.java))
        }

        // Obtendo o usuário atualmente autenticado
        val listaCartao : MutableList<String> = mutableListOf()

        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid

            // Verificando se o usuário tem cartões cadastrados
            db.collection("cartoes")
                .whereEqualTo("UserID", uid)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty()) {
                        // Se o usuário não tiver nenhum cartão cadastrado, exibe uma mensagem Snackbar
                        binding.btnPagar.setOnClickListener {
                            mensagem(it, "Você não tem nenhum cartão cadastrado")
                        }
                    } else {
                        // Se o usuário tiver pelo menos um cartão cadastrado
                        documents.forEach { document ->
                            val numdoc = document.getString("num")
                            val cartaoMascarado = numdoc?.let { mascaraCartao(it) }
                                listaCartao.add(cartaoMascarado.toString())



                        }

                        // Configuração do OnClickListener do botão de pagamento
                        binding.btnPagar.setOnClickListener {
                            if (binding.checkBox1.isChecked || binding.checkBox2.isChecked || binding.checkBox3.isChecked || binding.checkBox4.isChecked || binding.checkBox5.isChecked) {

                                if(!binding.autocompleteText.text.isEmpty()){
                                    val locacaoData = hashMapOf(
                                        "userId" to uid,
                                        "armarioId" to id,
                                        "tempo" to checkBoxText,
                                        "status" to "pendente"
                                    )

                                    Toast.makeText(this, "${binding.autocompleteText.text}", Toast.LENGTH_LONG).show()
                                    // Salvando a locação no Firestore
                                    db.collection("locacao")
                                        .add(locacaoData)
                                        .addOnSuccessListener { documentReference ->
                                            val locacaoId = documentReference.id
                                            // Iniciando a Activity do código QR com os dados necessários
                                            startActivity(Intent(this, Activity_QrCode::class.java).putExtra("Unidade", "$nome")
                                                .putExtra("gerente", "Lucas da Silva").putExtra("endereco", "$rua")
                                                .putExtra("Armario", "$codigo_armario").putExtra("Numero",numero).putExtra("LocacaoId", locacaoId))
                                        }
                                        .addOnFailureListener { e ->
                                            // Exibindo mensagem Snackbar em caso de erro
                                            mensagem(it, "Erro ao salvar a locação")
                                        }

                                }else{
                                    mensagem(it, "Selecione um cartão")
                                }


                            } else {
                                // Se nenhuma opção foi selecionada, exibe mensagem Snackbar
                                mensagem(it, "Selecione uma opção")
                            }
                        }
                    }
                }

            val autocomplete = binding.autocompleteText
            val adapteritems = ArrayAdapter(this, R.layout.list_item, listaCartao)

            autocomplete.setAdapter(adapteritems)

            autocomplete.setOnItemClickListener { parent, view, position, id ->

                val selecionado = parent.getItemAtPosition(position) as String
            }


        }
    }

    // Função para mascarar o número do cartão
    fun mascaraCartao(numeroCartao: String): String {
        val parteVisivel = numeroCartao.takeLast(4)
        val parteMascarada = "X".repeat(numeroCartao.length - 4)
        return "$parteMascarada$parteVisivel"
    }

    // Função para exibir uma mensagem em forma de Snackbar
    private fun mensagem(view: View, mensagem: String) {
        val snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(Color.parseColor("#F3787A"))
        snackbar.setTextColor(Color.parseColor("#FFFFFF"))
        snackbar.show()
    }

}
