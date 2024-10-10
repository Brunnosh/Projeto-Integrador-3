package com.safelocker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder


class Adapter_swipe(private val dataClassSwipeList:List<DataClass_swipe> ) :
    RecyclerView.Adapter<Adapter_swipe.swipeViewHolder>() {
    class swipeViewHolder(itemView: View): ViewHolder(itemView){
        val swipeTextView: TextView = itemView.findViewById(R.id.tv_Descrição)
        val swipeImage: ImageView = itemView.findViewById(R.id.ImageView)
        val swipeTitle: TextView = itemView.findViewById(R.id.tv_title)
        val swipeSubTitle : TextView = itemView.findViewById(R.id.Titulo_menor)
        val swipeBackground: ImageView = itemView.findViewById(R.id.Img_Background)
        val swipeSubTitle2: TextView = itemView.findViewById(R.id.Titulo_menor2)
    }

//    onCreateViewHolder(): RecyclerView chama esse método sempre que precisa criar um novo ViewHolder.
//    O método cria e inicializa o ViewHolder e o View associado, mas não preenche
//    o conteúdo da visualização. O ViewHolder ainda não foi vinculado a dados específicos.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): swipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row, parent, false)
        return  swipeViewHolder(view)
    }
//getItemCount(): RecyclerView chama esse método para conferir o tamanho do conjunto de dados.
// Por exemplo, em um aplicativo de catálogo de endereços, isso pode ser o número total de endereços.
// O RecyclerView usa isso para determinar quando não há mais itens que possam ser mostrados.
    override fun getItemCount(): Int {
        return dataClassSwipeList.size
    }
//onBindViewHolder(): RecyclerView chama esse método para associar um ViewHolder aos dados.
// O método busca os dados apropriados e usa esses dados para preencher o layout do fixador de visualização. Por exemplo, se a RecyclerView exibir uma lista de nomes,
// o método poderá encontrar o nome apropriado na lista e preencher o widget
// TextView do armazenador de visualização.
    override fun onBindViewHolder(holder: swipeViewHolder, position: Int) {
        val swipe = dataClassSwipeList[position]
        holder.swipeTextView.text = swipe.swipeText
        holder.swipeTitle.text = swipe.swipeTitle
        holder.swipeImage.setImageResource(swipe.swipeImage)
        holder.swipeSubTitle.text = swipe.swipeSubTitle
        holder.swipeBackground.setImageResource(swipe.swipeBackground)
        holder.swipeSubTitle2.text=swipe.swipeSubTitle2
    }

}