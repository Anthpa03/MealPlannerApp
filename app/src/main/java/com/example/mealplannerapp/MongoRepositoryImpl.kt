package com.example.mealplannerapp

import android.util.Log
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId

class MongoRepositoryImpl(val realm: Realm):MongoRepository {
    override fun getData(): Flow<List<User>> {
        return realm.query<User>().asFlow().map {it.list}
    }

    override fun filterData(name: String): Flow<List<User>> {
        return realm.query<User>(query="name CONTAINS[c] $0",name).asFlow().map{it.list}
    }

    override suspend fun insertUser(user: User) {
        realm.write { copyToRealm(user) }
    }

    override suspend fun updateUser(user: User) {
        realm.write {val queriedUser=query<User>(query="_id==$0",user._id).first().find()
        queriedUser?.Username=user.Username
        }
    }

    override suspend fun deleteUser(id: ObjectId) {
        realm.write {
            val user= query<User>(query="_id==$0", id).first().find()
            try{
                user?.let{delete(it)}
            } catch (e:Exception){
                Log.d("MongoRepositoryImpl","${e.message}")
            }
        }
    }

}