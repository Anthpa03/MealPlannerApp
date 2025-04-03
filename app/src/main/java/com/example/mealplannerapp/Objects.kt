package com.example.mealplannerapp


import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
import java.util.Date

// Define the Ingredient class as a RealmObject.
class Ingredient : RealmObject {
    var name: String = ""

    var quantity: String = ""
    var unit: String = ""
}
// This RealmObject will store a saved recipe.
class SavedRecipe : RealmObject {
    @PrimaryKey
    var recipeId: Int = 0  // Using the recipe ID as a primary key.
    var name: String = ""
    // Store the recipe’s ingredients using the existing Ingredient objects.
    var ingredients: RealmList<Ingredient> = realmListOf()
    var cookTime: String = ""
    var instructions: String = ""
    var image: String = ""
    // Persist the date as epoch milliseconds.
    var saveDateEpoch: Long = System.currentTimeMillis()

    // Provide a computed property for a Date object.
    @Ignore
    var saveDate: Date
        get() = Date(saveDateEpoch)
        set(value) { saveDateEpoch = value.time }
}
// Update the User class to include a list of ingredients.
class User : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId.invoke()
    var Username: String = ""
    @Index
    var Password: String = ""

    // A RealmList to hold the ingredients for the user.
    var ingredients: RealmList<Ingredient> = realmListOf()
    var savedRecipes: RealmList<SavedRecipe> = realmListOf()

}