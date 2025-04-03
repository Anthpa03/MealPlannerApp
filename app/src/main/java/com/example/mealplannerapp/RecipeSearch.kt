package com.example.mealplannerapp

import com.google.gson.Gson
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object RecipeSearch {
    private val client = OkHttpClient()
    private val gson = Gson()
    // Use the API key injected via BuildConfig
    private val apiKey: String = BuildConfig.API_KEY
    private const val rapidApiHost = "spoonacular-recipe-food-nutrition-v1.p.rapidapi.com"

    data class RecipeSummary(
        val id: Int,
        val title: String,
        val image: String,
        val readyInMinutes: Int
    )

    data class Ingredient(
        val name: String,
        val amount: Double,
        val unit: String
    )

    data class RecipeDetails(
        val id: Int,
        val title: String,
        val image: String,
        val readyInMinutes: Int,
        val extendedIngredients: List<Ingredient>,
        val instructions: String?
    )

    data class RecipeDisplayInfo(
        val title: String,
        val imageUrl: String,
        val cookTime: String,
        val recipeId: Int
    )

    data class ComplexSearchResult(
        val results: List<RecipeSummary>,
        val offset: Int,
        val number: Int,
        val totalResults: Int
    )

    // Delay before each API call to help avoid rate limiting.
    private suspend fun rateLimitDelay() {
        delay(300)  // Delay 300 milliseconds. Adjust if needed.
    }

    suspend fun searchRecipesByQuery(query: String, number: Int = 15): List<RecipeSummary>? {
        rateLimitDelay()
        val baseUrl = "https://$rapidApiHost/recipes/complexSearch"
        val urlBuilder = baseUrl.toHttpUrlOrNull()?.newBuilder() ?: return null
        urlBuilder.addQueryParameter("query", query)
        urlBuilder.addQueryParameter("number", number.toString())
        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("x-rapidapi-key", apiKey)
            .addHeader("x-rapidapi-host", rapidApiHost)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("Error fetching recipes: ${response.code}")
                return null
            }
            val responseBody = response.body?.string() ?: return null
            val wrapper = gson.fromJson(responseBody, ComplexSearchResult::class.java)
            return wrapper.results
        }
    }

    suspend fun searchRecipesByIngredients(
        ingredients: List<String>,
        number: Int = 50,
        apiKey: String
    ): List<RecipeSummary>? {
        rateLimitDelay()
        val baseUrl = "https://$rapidApiHost/recipes/findByIngredients"
        val urlBuilder = baseUrl.toHttpUrlOrNull()?.newBuilder() ?: return null
        urlBuilder.addQueryParameter("ingredients", ingredients.joinToString(","))
        urlBuilder.addQueryParameter("number", number.toString())
        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("x-rapidapi-key", this.apiKey)
            .addHeader("x-rapidapi-host", rapidApiHost)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("Error fetching recipe summaries: ${response.code}")
                return null
            }
            val responseBody = response.body?.string() ?: return null
            return gson.fromJson(responseBody, Array<RecipeSummary>::class.java).toList()
        }
    }

    suspend fun getRecipeDetails(recipeId: Int): RecipeDetails? {
        rateLimitDelay()
        val baseUrl = "https://$rapidApiHost/recipes/$recipeId/information"
        val urlBuilder = baseUrl.toHttpUrlOrNull()?.newBuilder() ?: return null
        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("x-rapidapi-key", apiKey)
            .addHeader("x-rapidapi-host", rapidApiHost)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("Error fetching recipe details: ${response.code}")
                return null
            }
            val responseBody = response.body?.string() ?: return null
            return gson.fromJson(responseBody, RecipeDetails::class.java)
        }
    }

    suspend fun getRecipeDisplayInfo(recipeId: Int): RecipeDisplayInfo? {
        val details = getRecipeDetails(recipeId) ?: return null
        return RecipeDisplayInfo(
            title = details.title,
            imageUrl = details.image,
            cookTime = "${details.readyInMinutes} mins",
            recipeId = details.id
        )
    }

    suspend fun searchRecipesByFilters(
        ingredients: List<String>,
        diet: String?,
        minReadyTime: Int?,
        maxReadyTime: Int?,
        number: Int = 15
    ): List<RecipeSummary>? {
        rateLimitDelay()
        val baseUrl = "https://$rapidApiHost/recipes/complexSearch"
        val urlBuilder = baseUrl.toHttpUrlOrNull()?.newBuilder() ?: return null
        urlBuilder.addQueryParameter("includeIngredients", ingredients.joinToString(","))
        if (!diet.isNullOrEmpty()) {
            urlBuilder.addQueryParameter("diet", diet)
        }
        if (minReadyTime != null) {
            urlBuilder.addQueryParameter("minReadyTime", minReadyTime.toString())
        }
        if (maxReadyTime != null) {
            urlBuilder.addQueryParameter("maxReadyTime", maxReadyTime.toString())
        }
        urlBuilder.addQueryParameter("number", number.toString())
        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("x-rapidapi-key", apiKey)
            .addHeader("x-rapidapi-host", rapidApiHost)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("Error fetching filtered recipes: ${response.code}")
                return null
            }
            val responseBody = response.body?.string() ?: return null
            val wrapper = gson.fromJson(responseBody, ComplexSearchResult::class.java)
            return wrapper.results
        }
    }

    suspend fun getIngredientSuggestions(query: String, number: Int = 20): List<String>? {
        rateLimitDelay()
        val baseUrl = "https://$rapidApiHost/food/ingredients/autocomplete"
        val urlBuilder = baseUrl.toHttpUrlOrNull()?.newBuilder() ?: return null
        urlBuilder.addQueryParameter("query", query)
        urlBuilder.addQueryParameter("number", number.toString())
        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("x-rapidapi-key", apiKey)
            .addHeader("x-rapidapi-host", rapidApiHost)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("Error fetching ingredient suggestions: ${response.code}")
                return null
            }
            val responseBody = response.body?.string() ?: return null
            val suggestions = gson.fromJson(responseBody, Array<IngredientSuggestion>::class.java).toList()
            return suggestions.map { it.name }
        }
    }

    data class IngredientSuggestion(
        val name: String
    )
}
