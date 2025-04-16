
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mealplannerapp.SharedPreferencesManager
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
        holder.binding.amountTextView.text = item.quantity
        holder.binding.unitTextView.text = item.unit

        val context = holder.itemView.context
        val username = SharedPreferencesManager.getUsername(context) ?: ""

        // Handles bookmark toggle
        val isBookmarked = SharedPreferencesManager.isBookmarked(context, username, item.name)
        holder.binding.imageButtonBookmark.isSelected = isBookmarked
        holder.binding.imageButtonBookmark.setOnClickListener { button ->
            if (isBookmarked) {
                SharedPreferencesManager.removeBookmark(context, username, item.name)
                button.isSelected = false
            } else {
                SharedPreferencesManager.addBookmark(context, username, item.name)
                button.isSelected = true
            }
        }

        // When the edit button is clicked, trigger the onEditClick lambda.
        holder.binding.buttonEdit.setOnClickListener {
            onEditClick(item)
        }
    }

    override fun getItemCount(): Int = inventoryList.size
}

//TODO:REMOVE WHEN FINISHED IMPLEMENTING DB FETCHING LOGIC
data class InventoryItem(
    val name: String,
    val quantity: String,
    val unit: String
)
