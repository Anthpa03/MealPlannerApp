package com.example.mealplannerapp

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: MongoRepository) : ViewModel() {

    // State variables
    private var name = mutableStateOf("")
    private var password = mutableStateOf("")
    private var objectId = mutableStateOf("")
    private var filtered = mutableStateOf(false)
    var data = mutableStateOf(emptyList<User>())

    init {
        viewModelScope.launch {
            repository.getData().collect { users ->
                data.value = users
                Log.d("HomeViewModel", "Loaded ${users.size} users from Realm")
            }
        }
    }

    // @HiltViewModel - Helper Functions
    fun updateName(name: String) {
        this.name.value = name
    }

    fun updatePassword(password: String) {
        this.password.value = password
    }

    fun updateObjectId(id: String) {
        this.objectId.value = id
    }

    // @HiltViewModel - User Functions
    fun insertUser() {
        viewModelScope.launch(Dispatchers.IO) {
            if (name.value.isNotEmpty()) {
                repository.insertUser(user = User().apply {
                    Username = this@HomeViewModel.name.value
                    Password = this@HomeViewModel.password.value
                })
            }
        }
    }
    fun changePassword(username: String, newPassword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByUsername(username)
            if (user != null) {
                repository.updatePassword(user._id, newPassword)
                // Update local cache
                updatePassword(newPassword)
            }
        }
    }
    fun updateUser() {
        viewModelScope.launch(Dispatchers.IO) {
            if (name.value.isNotEmpty()) {
                repository.updateUser(user = User().apply {
                    _id = ObjectId(hexString = this@HomeViewModel.objectId.value)
                    Username = this@HomeViewModel.name.value
                    Password = this@HomeViewModel.password.value
                })
            }
        }
    }
    fun changeUsername(oldUsername: String, newUsername: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByUsername(oldUsername)
            if (user != null) {
                repository.updateUsername(user._id, newUsername)
                // Update local cache
                updateName(newUsername)
            }
        }
    }

    fun deleteUserByUsername(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByUsername(username)
            if (user != null) {
                repository.deleteUser(user._id)
                Log.d("HomeViewModel", "Deleted user '$username'")
            } else {
                Log.e("HomeViewModel", "Cannot delete: no user found for '$username'")
            }
        }
    }

    fun filterData() {
        viewModelScope.launch(Dispatchers.IO) {
            if (filtered.value) {
                repository.getData().collect {
                    filtered.value = false
                    name.value = ""
                    data.value = it
                }
            } else {
                repository.filterData(name = name.value).collect {
                    filtered.value = true
                    data.value = it
                }
            }
        }
    }

    suspend fun authenticateUser(): Boolean {
        return withContext(Dispatchers.IO) {
            val username = name.value
            val pass = password.value
            val user = repository.authenticateUser(username, pass)
            val isAuthenticated = user != null
            Log.d("AuthDebug", "Authentication result: $isAuthenticated")
            isAuthenticated
        }
    }

    // @HiltViewModel - Ingredient Functions
    suspend fun getIngredientsForUser(username: String): List<Ingredient> {
        // Directly query Realm for the latest user data.
        val user = repository.getUserByUsername(username)
        return user?.ingredients?.toList() ?: emptyList()
    }

    fun updateIngredient(username: String, ingredientName: String, newQuantity: String, newUnit: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("HomeViewModel", "updateIngredient called for username: '$username', ingredient: '$ingredientName'")
            // Get the user directly from Realm via the repository.
            val user = repository.getUserByUsername(username)
            if (user == null) {
                Log.e("HomeViewModel", "User not found for username: '$username'")
                return@launch
            }
            Log.d("HomeViewModel", "User found: '${user.Username}' with id: ${user._id}")
            // Delegate the update to the repository.
            repository.updateIngredient(user._id, ingredientName, newQuantity, newUnit)
        }
    }

    fun addIngredient(username: String, ingredientName: String, quantity: String, unit: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(
                "HomeViewModel",
                "addIngredient called with username: '$username', ingredientName: '$ingredientName', quantity: '$quantity', unit: '$unit'"
            )
            // Query Realm directly for the user.
            val user = repository.getUserByUsername(username)
            if (user == null) {
                Log.e("HomeViewModel", "Direct query: User not found for username: '$username'")
                return@launch
            } else {
                Log.d("HomeViewModel", "Direct query: User found: '${user.Username}' with id: ${user._id}")
            }
            // Delegate the addition of the ingredient to the repository.
            repository.addIngredient(user._id, ingredientName, quantity, unit)
        }
    }

    fun removeIngredient(username: String, ingredientName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("HomeViewModel", "removeIngredient called for username: '$username', ingredient: '$ingredientName'")
            val user = repository.getUserByUsername(username)
            if (user == null) {
                Log.e("HomeViewModel", "User not found for username: '$username'")
                return@launch
            }
            Log.d("HomeViewModel", "User found: '${user.Username}' with id: ${user._id}")
            // Delegate the removal to the repository.
            repository.removeIngredient(user._id, ingredientName)
        }
    }

    // @HiltViewModel - Recipe Functions
    fun saveRecipeForUser(
        username: String,
        recipeId: Int,
        name: String,
        ingredients: List<Ingredient>,
        cookTime: String,
        instructions: String,
        image: String,
        dateString: String            // your already‑formatted "EEE MMM dd …" string
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1) Look up the user
            val user = repository.getUserByUsername(username)
            if (user == null) {
                Log.e("HomeViewModel", "User not found for username: '$username'")
                return@launch
            }

            // 2) Build a new SavedRecipe (its own _id is auto‑generated)
            val savedRecipe = SavedRecipe().apply {
                this.recipeId       = recipeId
                this.name           = name
                this.cookTime       = cookTime
                this.instructions   = instructions
                this.image          = image
                this.ingredients.addAll(ingredients)
                this.saveDate= dateString   // unchanged
            }

            // 3) Delegate to repository
            repository.addSavedRecipe(user._id, savedRecipe)
            Log.d("HomeViewModel", "Saved recipe '$name' for user: ${user.Username}")
        }
    }

    /** Load all recipes whose saveDateString exactly matches the target date. */
    suspend fun getSavedRecipesForDay(
        username: String,
        targetDateString: String
    ): List<SavedRecipe> {
        val user = repository.getUserByUsername(username) ?: return emptyList()
        return repository.getSavedRecipesByDate(user._id, targetDateString)
    }

    /** Load the 3 most recently saved recipes for the given user. */
    suspend fun getRecentlySavedRecipesForUser(
        username: String
    ): List<SavedRecipe> {
        val user = repository.getUserByUsername(username) ?: return emptyList()
        return repository.getRecentlySavedRecipes(user._id)
    }

    /** Simply re‑expose your repository lookup if you need it elsewhere. */
    suspend fun getUserByUsername(username: String): User? {
        return repository.getUserByUsername(username)
    }
}
