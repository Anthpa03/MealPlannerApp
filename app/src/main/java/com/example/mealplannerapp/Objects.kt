package com.example.mealplannerapp

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class User: RealmObject{
    @PrimaryKey
    var _id: ObjectId= ObjectId.invoke()
    var Username: String=""
    @Index
    var Password: String=""

}
class Recipe:RealmObject{
    var holder:String=""
}
class Ingredient:RealmObject{
    var holder:String=""
}