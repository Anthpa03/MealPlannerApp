package com.example.mealplannerapp

import android.util.Log
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class MongoRepositoryImpl(val realm: Realm) : MongoRepository {

    // ========================
    // User Operations
    // ========================
    override fun getData(): Flow<List<User>> {
        return realm.query<User>().asFlow().map { it.list }
    }

    override fun filterData(name: String): Flow<List<User>> {
        return realm.query<User>(query = "name CONTAINS[c] $0", name).asFlow().map { it.list }
    }

    override suspend fun insertUser(user: User) {
        val hashedPassword = hashPassword(user.Password)  // Hash password before inserting
        realm.write {
            copyToRealm(user.apply { Password = hashedPassword })
        }
    }

    override suspend fun updateUser(user: User) {
        withContext(Dispatchers.Main) {
            Log.d("MongoRepositoryImpl", "updateUser called on thread: ${Thread.currentThread().name}")
            realm.write {
                val queriedUser = query<User>(query = "_id == $0", user._id).first().find()
                queriedUser?.let {
                    it.Username = user.Username
                    if (user.Password.isNotEmpty()) {
                        it.Password = hashPassword(user.Password)
                    }
                    Log.d("MongoRepositoryImpl", "User updated in Realm: ${it.Username}")
                } ?: Log.e("MongoRepositoryImpl", "User not found in Realm for update")
            }
        }
    }

    override suspend fun deleteUser(id: ObjectId) {
        realm.write {
            val user = query<User>(query = "_id==$0", id).first().find()
            try {
                user?.let { delete(it) }
            } catch (e: Exception) {
                Log.d("MongoRepositoryImpl", "${e.message}")
            }
        }
    }

    override suspend fun authenticateUser(username: String, password: String): User? {
        val hashedPassword = hashPassword(password)
        Log.d("AuthDebug", "Attempting to authenticate user: $username with password: $hashedPassword")
        return realm.query<User>("Username == $0 AND Password == $1", username, hashedPassword)
            .first()
            .find()
    }

    override suspend fun getUserByUsername(username: String): User? {
        return withContext(Dispatchers.Main) {
            realm.query<User>("Username ==[c] $0", username.trim())
                .first()
                .find()
        }
    }

    // ========================
    // Ingredient Operations
    // ========================
    override suspend fun addIngredient(userId: ObjectId, ingredientName: String, quantity: String, unit: String) {
        withContext(Dispatchers.Main) { // Must run on the main thread
            realm.write {
                val user = query<User>("_id == $0", userId).first().find()
                if (user == null) {
                    Log.e("MongoRepositoryImpl", "User not found for id: $userId")
                    return@write
                }
                val newIngredient = Ingredient().apply {
                    name = ingredientName
                    this.quantity = quantity
                    this.unit = unit
                }
                user.ingredients.add(newIngredient)
                Log.d("MongoRepositoryImpl", "Added ingredient: $newIngredient to user: ${user.Username}. Total ingredients: ${user.ingredients.size}")
            }
        }
    }

    override suspend fun updateIngredient(userId: ObjectId, ingredientName: String, newQuantity: String, newUnit: String) {
        withContext(Dispatchers.Main) { // Ensure write is on the main thread if required.
            realm.write {
                val user = query<User>("_id == $0", userId).first().find()
                if (user == null) {
                    Log.e("MongoRepositoryImpl", "User not found for id: $userId")
                    return@write
                }
                val ingredient = user.ingredients.firstOrNull { it.name.trim().equals(ingredientName.trim(), ignoreCase = true) }
                if (ingredient == null) {
                    Log.e("MongoRepositoryImpl", "Ingredient '$ingredientName' not found for user: ${user.Username}")
                    return@write
                }
                ingredient.quantity = newQuantity
                ingredient.unit = newUnit
                Log.d("MongoRepositoryImpl", "Updated ingredient: $ingredient for user: ${user.Username}")
            }
        }
    }

    override suspend fun removeIngredient(userId: ObjectId, ingredientName: String) {
        withContext(Dispatchers.Main) {
            realm.write {
                val user = query<User>("_id == $0", userId).first().find()
                if (user == null) {
                    Log.e("MongoRepositoryImpl", "User not found for id: $userId")
                    return@write
                }
                val ingredient = user.ingredients.firstOrNull { it.name.trim().equals(ingredientName.trim(), ignoreCase = true) }
                if (ingredient == null) {
                    Log.e("MongoRepositoryImpl", "Ingredient '$ingredientName' not found for user: ${user.Username}")
                    return@write
                }
                user.ingredients.remove(ingredient)
                Log.d("MongoRepositoryImpl", "Removed ingredient: $ingredient from user: ${user.Username}. Total ingredients now: ${user.ingredients.size}")
            }
        }
    }

    // ========================
    // Recipe Operations
    // ========================
    override suspend fun addSavedRecipe(userId: ObjectId, recipe: SavedRecipe) {
        withContext(Dispatchers.Main) {  // Ensure writes run on the required thread.
            realm.write {
                val user = query<User>("_id == $0", userId).first().find()
                if (user == null) {
                    Log.e("MongoRepositoryImpl", "User not found for id: $userId")
                    return@write
                }
                user.savedRecipes.add(recipe)
                // Add to recently saved queue: if already 3 items, remove the oldest.
                if (user.recentlysavedRecipes.size >= 3) {
                    user.recentlysavedRecipes.removeAt(0)
                }
                user.recentlysavedRecipes.add(recipe)
                Log.d("MongoRepositoryImpl", "Saved recipe '${recipe.name}' added for user '${user.Username}'")
            }
        }
    }

    override suspend fun updateSavedRecipe(userId: ObjectId, recipe: SavedRecipe) {
        withContext(Dispatchers.Main) {
            realm.write {
                val user = query<User>("_id == $0", userId).first().find()
                if (user == null) {
                    Log.e("MongoRepositoryImpl", "User not found for id: $userId")
                    return@write
                }
                val existingRecipe = user.savedRecipes.firstOrNull { it.recipeId == recipe.recipeId }
                if (existingRecipe != null) {
                    existingRecipe.name = recipe.name
                    existingRecipe.cookTime = recipe.cookTime
                    existingRecipe.instructions = recipe.instructions
                    existingRecipe.image = recipe.image
                    existingRecipe.bookmarked = recipe.bookmarked
                    existingRecipe.ingredients.clear()
                    existingRecipe.ingredients.addAll(recipe.ingredients)
                    existingRecipe.saveDate = recipe.saveDate
                    Log.d("MongoRepositoryImpl", "Updated recipe '${recipe.name}' for user '${user.Username}'")
                } else {
                    Log.e("MongoRepositoryImpl", "Recipe with id ${recipe.recipeId} not found for user '${user.Username}'")
                }
            }
        }
    }
    override suspend fun getRecentlySavedRecipes(userId: ObjectId): List<SavedRecipe> {
        return withContext(Dispatchers.Main) {
            realm.query<User>("_id == $0", userId).first().find()?.recentlysavedRecipes?.toList() ?: emptyList()
        }
    }

    // ========================
    // Helper Functions
    // ========================
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val encodedHash = digest.digest(password.toByteArray(StandardCharsets.UTF_8))
        return encodedHash.joinToString("") { String.format("%02x", it) }
    }
}
