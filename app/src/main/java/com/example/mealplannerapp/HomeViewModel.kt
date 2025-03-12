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
    private var data = mutableStateOf(emptyList<User>())

    init{
        viewModelScope.launch {
            repository.getData().collect{
                data.value=it
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
    fun getIngredientsForUser(username: String): List<Ingredient> {
        // Find the first user whose Username matches the parameter.
        val user = data.value.find { it.Username == username }
        // Return a copy of the ingredients list (or an empty list if not found).
        return user?.ingredients?.toList() ?: emptyList()
    }
    suspend fun authenticateUser(): Boolean {
        return withContext(Dispatchers.IO) {
            val username = name.value ?: ""
            val pass = password.value ?: ""
            val user = repository.authenticateUser(username, pass)
            val isAuthenticated = user != null
            Log.d("AuthDebug", "Authentication result: $isAuthenticated")
            isAuthenticated
        }
    }
    // Update the quantity of a given ingredient for a specific user.
    fun modifyIngredientQuantity(username: String, ingredientName: String, newQuantity: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = data.value.find { it.Username == username }
            user?.let { u ->
                // Find the ingredient by its primary key (name).
                u.ingredients.firstOrNull { it.name == ingredientName }?.let { ingredient ->
                    ingredient.quantity = newQuantity
                    // Update the user in the repository, which should perform the write transaction.
                    repository.updateUser(u)
                }
            }
        }
    }

    // Add a new ingredient to the specified user's ingredient list.
    fun addIngredient(username: String, ingredientName: String, quantity: String, unit: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = data.value.find { it.Username == username }
            user?.let { u ->
                // Create the new ingredient piece by piece
                val newIngredient = Ingredient().apply {
                    name = ingredientName
                    this.quantity = quantity
                    this.unit = unit
                }
                // Add the new ingredient to the user's list
                u.ingredients.add(newIngredient)
                // Persist the change via the repository
                repository.updateUser(u)
            }
        }
    }


    // Remove an ingredient from the specified user's ingredient list.
    fun removeIngredient(username: String, ingredientName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = data.value.find { it.Username == username }
            user?.let { u ->
                // Locate the ingredient to remove.
                val ingredient = u.ingredients.firstOrNull { it.name == ingredientName }
                if (ingredient != null) {
                    u.ingredients.remove(ingredient)
                    repository.updateUser(u)
                }
            }
        }
    }
}