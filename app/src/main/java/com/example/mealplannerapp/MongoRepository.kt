package com.example.mealplannerapp

import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId


interface MongoRepository {
    fun getData(): Flow<List<User>>
    fun filterData( name: String):Flow<List<User>>
    suspend fun insertUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(id: ObjectId)
    suspend fun authenticateUser(username: String, password: String): User?
    suspend fun removeIngredient(user: User, ingredientName: String)
    suspend fun addIngredient(user: User, ingredientName: String, quantity: String, unit: String)
    suspend fun updateIngredientUnit(user: User, ingredientName: String, newUnit: String)
    suspend fun updateIngredientQuantity(user: User, ingredientName: String, newQuantity: String)
}