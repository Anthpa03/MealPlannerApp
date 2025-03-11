package com.example.mealplannerapp


import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

// Define the Ingredient class as a RealmObject.
class Ingredient : RealmObject {
    var name: String = ""
    var quantity: String = ""
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
}