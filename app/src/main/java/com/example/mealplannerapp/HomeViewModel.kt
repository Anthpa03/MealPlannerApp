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
class HomeViewModel @Inject constructor(private val repository:MongoRepository):ViewModel(){
    private var name = mutableStateOf("")
    private var password = mutableStateOf("")
    private var objectId = mutableStateOf("")
    private var filtered = mutableStateOf(false)
    var data = mutableStateOf(emptyList<User>())

    init {
        viewModelScope.launch {
            repository.getData().collect {
                data.value = it
                Log.d("HomeViewModel", "Loaded ${it.size} users from Realm")
            }
        }
    }

    fun updateName (name:String)
    {
        this.name.value=name
    }
    fun updatePassword(password: String) {
        this.password.value = password
    }
    fun updateObjectId(id:String)
    {
        this.objectId.value=id
    }
    fun insertUser(){
        viewModelScope.launch(Dispatchers.IO){
            if (name.value.isNotEmpty()){
                repository.insertUser(user = User().apply {
                    Username = this@HomeViewModel.name.value
                    Password = this@HomeViewModel.password.value})
            }
        }
    }
    fun updateUser(){
        viewModelScope.launch(Dispatchers.IO) {
            if (name.value.isNotEmpty()) {
                repository.updateUser(user = User().apply {
                    _id = ObjectId(hexString = this@HomeViewModel.objectId.value)
                    Username = this@HomeViewModel.name.value
                    Password = this@HomeViewModel.password.value})
            }
        }
    }
    fun deleteUser(){
        viewModelScope.launch{
            if(objectId.value.isNotEmpty()){
                repository.deleteUser(id = ObjectId(hexString = objectId.value))
            }
        }
    }
    fun filterData(){
        viewModelScope.launch(Dispatchers.IO){
            if(filtered.value){
                repository.getData().collect{
                    filtered.value=false
                    name.value=""
                    data.value=it
                }

            }
            else{
                repository.filterData(name = name.value).collect{
                    filtered.value=true
                    data.value=it
                }
            }
        }
    }
    suspend fun getIngredientsForUser(username: String): List<Ingredient> {
        // Directly query Realm for the latest user data.
        val user = repository.getUserByUsername(username)
        return user?.ingredients?.toList() ?: emptyList()
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
    // Update the quantity of a given ingredient for a specific user.
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
            Log.d("HomeViewModel", "addIngredient called with username: '$username', ingredientName: '$ingredientName', quantity: '$quantity', unit: '$unit'")

            // Query Realm directly for the user.
            val user = repository.getUserByUsername(username)
            if (user == null) {
                Log.e("HomeViewModel", "Direct query: User not found for username: '$username'")
                return@launch
            } else {
                Log.d("HomeViewModel", "Direct query: User found: '${user.Username}' with id: ${user._id}")
            }

            // Now delegate the addition of the ingredient to the repository.
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
}