package com.example.mealplannerapp

import android.util.Log
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class MongoRepositoryImpl(val realm: Realm):MongoRepository {
    override fun getData(): Flow<List<User>> {
        return realm.query<User>().asFlow().map {it.list}
    }

    override fun filterData(name: String): Flow<List<User>> {
        return realm.query<User>(query="name CONTAINS[c] $0",name).asFlow().map{it.list}
    }

    override suspend fun insertUser(user: User) {
        val hashedPassword = hashPassword(user.Password) // Hashes password before inserting it into db
        realm.write { copyToRealm(user.apply { Password = hashedPassword })
        }
    }

    override suspend fun updateUser(user: User) {
        realm.write {val queriedUser=query<User>(query="_id==$0",user._id).first().find()
            queriedUser?.let {
                it.Username = user.Username
                if (user.Password.isNotEmpty()) { // Hash and update password if provided
                    it.Password = hashPassword(user.Password)
                }
            }
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

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val encodedHash = digest.digest(password.toByteArray(StandardCharsets.UTF_8))
        return encodedHash.joinToString("") { String.format("%02x", it) }
    }

    override suspend fun authenticateUser(username: String, password: String): User? {
        val hashedPassword = hashPassword(password)
        Log.d("AuthDebug", "Attempting to authenticate user: $username with password: $hashedPassword")
        return realm.query<User>("Username == $0 AND Password == $1", username, hashedPassword)
            .first()
            .find()
    }

}