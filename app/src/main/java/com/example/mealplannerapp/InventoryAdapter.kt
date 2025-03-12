import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mealplannerapp.databinding.InventoryRecyclerViewCardBinding

class InventoryAdapter(
    private val inventoryList: List<InventoryItem>,
    private val onEditClick: (InventoryItem) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    inner class InventoryViewHolder(val binding: InventoryRecyclerViewCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val binding = InventoryRecyclerViewCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InventoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val item = inventoryList[position]

        holder.binding.textViewIngredientName.text = item.name
        holder.binding.amountTextView.text = String.format("%d", item.amount)

        holder.binding.buttonEdit.setOnClickListener {
            onEditClick(item)
        }

        holder.binding.imageButtonBookmark.setOnClickListener { button ->
            button.isSelected = !button.isSelected  // Toggle bookmark selection
        }
    }

    override fun getItemCount(): Int = inventoryList.size
}

//TODO:REMOVE WHEN FINISHED IMPLEMENTING DB FETCHING LOGIC
data class InventoryItem(
    val name: String,
    val amount: Int
)
