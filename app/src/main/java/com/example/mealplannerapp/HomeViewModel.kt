package com.example.mealplannerapp

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository:MongoRepository
):ViewModel(){
    var name= mutableStateOf("")
    var objectId = mutableStateOf("")
    var filtered= mutableStateOf(false)
    var data= mutableStateOf(emptyList<User>())

    init{
        viewModelScope.launch {
            repository.getData().collect{
                data.value=it
            }
        }
    }

    fun updatename (name:String)
    {
        this.name.value=name
    }
    fun updateObjectId(id:String)
    {
    this.objectId.value=id
    }
    fun insertUser(){
        viewModelScope.launch(Dispatchers.IO){
            if (name.value.isNotEmpty()){
                repository.insertUser(user = User().apply {
                    Username = this@HomeViewModel.name.value})
            }

        }
    }
    fun updateUser(){
        viewModelScope.launch(Dispatchers.IO) {
            if (name.value.isNotEmpty()) {
                repository.updateUser(user = User().apply {
                    _id = ObjectId(hexString = this@HomeViewModel.objectId.value)
                    Username = this@HomeViewModel.name.value
                })
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
}