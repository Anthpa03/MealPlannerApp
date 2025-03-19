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
    suspend fun updateIngredient(userId: ObjectId, ingredientName: String, newQuantity: String, newUnit: String)
    suspend fun removeIngredient(userId: ObjectId, ingredientName: String)
    suspend fun addIngredient(userId: ObjectId, ingredientName: String, quantity: String, unit: String)
    suspend fun getUserByUsername(username: String): User?

}