package com.safelocker.safelocker

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.safelocker.R

class Adapter_Locacoes(private val context: Context, private var listaLocacoes: MutableList<DataClass_Locacoes>) : RecyclerView.Adapter<Adapter_Locacoes.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAba: TextView = itemView.findViewById(R.id.tvAba)
        val tvAba2: TextView = itemView.findViewById(R.id.tvAba2)
        val imageView2: ImageView = itemView.findViewById(R.id.imageView2)
        val btnQRC: Button = itemView.findViewById(R.id.btnQRC)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_locacoes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = listaLocacoes[position]
        holder.tvAba.text = currentItem.nomeLugar
        holder.tvAba2.text = currentItem.status

        when (currentItem.status) {
            "em andamento" -> {
                holder.tvAba2.setTextColor(Color.GREEN) // Verde
                holder.imageView2.visibility = View.GONE // Oculta a imagem
                holder.btnQRC.isEnabled = false // Desabilita o botão QR Code
            }
            "pendente" -> {
                holder.tvAba2.setTextColor(Color.RED) // Vermelho
                holder.imageView2.visibility = View.VISIBLE // Mostra a imagem
                holder.btnQRC.isEnabled = true // Habilita o botão QR Code
            }
            "finalizado" -> {
                holder.tvAba2.setTextColor(Color.BLACK) // Preto
                holder.imageView2.visibility = View.GONE // Oculta a imagem
                holder.btnQRC.isEnabled = false // Desabilita o botão QR Code
            }
            else -> {
                holder.tvAba2.setTextColor(Color.BLACK) // Cor padrão, preto
                holder.imageView2.visibility = View.GONE // Oculta a imagem
                holder.btnQRC.isEnabled = false // Desabilita o botão QR Code
            }
        }

        holder.btnQRC.setOnClickListener {
            val intent = Intent(context, Activity_QrCode::class.java)
            intent.putExtra("Unidade", currentItem.nomeLugar)
            intent.putExtra("gerente", "Lucas da Silva")
            intent.putExtra("endereco", currentItem.rua)
            intent.putExtra("Armario", currentItem.codigo_armario)
            intent.putExtra("Numero", currentItem.numero)
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int {
        return listaLocacoes.size
    }

    // Método para atualizar a lista de locações e notificar o adaptador
    fun updateList(novaLista: List<DataClass_Locacoes>) {
        listaLocacoes.clear()
        listaLocacoes.addAll(novaLista)
        notifyDataSetChanged()
    }
}
