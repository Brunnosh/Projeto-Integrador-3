import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.safelocker.R
import com.safelocker.safelocker.Model_Card

// Classe Adapter para o RecyclerView que exibe os cartões
class Adapter_Card(private val cardList: List<Model_Card>, private val listener : OnItemClickListener) :
    RecyclerView.Adapter<Adapter_Card.CardViewHolder>() {

    // ViewHolder para representar cada item de cartão no RecyclerView
    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNome: TextView = itemView.findViewById(R.id.textViewNome)
        val textViewNumero: TextView = itemView.findViewById(R.id.textViewNumero)
        val textViewValidade: TextView = itemView.findViewById(R.id.textViewValidade)
        val ImageViewChipe: ImageView=itemView.findViewById(R.id.ImgChipe)
        val btnApagar : Button = itemView.findViewById(R.id.btnApagar)
    }

    // Método chamado quando o ViewHolder precisa ser criado
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        // Infla o layout do item de cartão
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.cartao_item, parent, false)
        return CardViewHolder(itemView)
    }

    // Método chamado para associar dados a um ViewHolder
    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val currentItem = cardList[position]
        // Define os valores dos campos de texto com os dados do cartão atual
        holder.textViewNome.text = currentItem.nomecartao
        holder.textViewNumero.text = currentItem.numcartao
        holder.textViewValidade.text = currentItem.validadecartao
        holder.ImageViewChipe.imageAlpha = currentItem.ImgChipe

        holder.btnApagar.setOnClickListener{
            listener.onButtonClick(currentItem)
        }

    }

    // Método chamado para obter o número total de itens no conjunto de dados
    override fun getItemCount() = cardList.size

    interface OnItemClickListener{
        fun onButtonClick(card : Model_Card)
    }
}
