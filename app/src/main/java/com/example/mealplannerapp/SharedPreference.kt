package com.example.mealplannerapp

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesManager {

    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USERNAME = "username"
    private const val KEY_PASSWORD = "password"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- User credentials ---
    fun saveUserCredentials(context: Context, username: String, password: String) {
        val sharedPreferences = getSharedPreferences(context)
        sharedPreferences.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    fun saveUsername(context: Context, username: String) {
        getSharedPreferences(context).edit()
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun savePassword(context: Context, password: String) {
        getSharedPreferences(context).edit()
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    fun getUsername(context: Context): String? =
        getSharedPreferences(context).getString(KEY_USERNAME, null)

    fun getPassword(context: Context): String? =
        getSharedPreferences(context).getString(KEY_PASSWORD, null)

    fun clearUserCredentials(context: Context) {
        getSharedPreferences(context).edit()
            .remove(KEY_USERNAME)
            .remove(KEY_PASSWORD)
            .apply()
    }

    // --- Bookmarks ---
    fun addBookmark(context: Context, username: String, itemName: String) {
        val prefs = getSharedPreferences(context)
        val userKey = "bookmarks_$username"
        val bookmarks = prefs.getStringSet(userKey, mutableSetOf()) ?: mutableSetOf()
        bookmarks.add(itemName)
        prefs.edit().putStringSet(userKey, bookmarks).apply()
    }

    fun removeBookmark(context: Context, username: String, itemName: String) {
        val prefs = getSharedPreferences(context)
        val userKey = "bookmarks_$username"
        val bookmarks = prefs.getStringSet(userKey, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        bookmarks.remove(itemName)
        prefs.edit().putStringSet(userKey, bookmarks).apply()
    }

    fun isBookmarked(context: Context, username: String, itemName: String): Boolean {
        val prefs = getSharedPreferences(context)
        val userKey = "bookmarks_$username"
        return prefs.getStringSet(userKey, mutableSetOf())?.contains(itemName) == true
    }
}
