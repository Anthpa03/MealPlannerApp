package com.example.mealplannerapp

import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId


interface MongoRepository {
    fun getData(): Flow<List<User>>
    fun filterData( name: String):Flow<List<User>>
    suspend fun insertUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(id: ObjectId)
    
}