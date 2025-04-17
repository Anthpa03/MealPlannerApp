package com.example.mealplannerapp

import com.google.gson.Gson
import kotlinx.coroutines.delay
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.File
import okhttp3.Response
object RecipeSearch {
    // Set up a cache; replace "cache" with context.cacheDir in production.
    private const val CACHESIZE = 10 * 1024 * 1024L // 10 MB
    private val cacheDir = File("cache") // For production use a proper cache directory.
    private val client = OkHttpClient.Builder()
        .cache(Cache(cacheDir, CACHESIZE))
        .build()

    private val gson = Gson()
    private val apiKey: String = BuildConfig.API_KEY
    private const val RAPIDAPIHOST = "spoonacular-recipe-food-nutrition-v1.p.rapidapi.com"

    // Data classes
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

    data class IngredientSuggestion(
        val name: String
    )

    // -------------------------
    // Dynamic Backoff Helper
    // -------------------------
    /**
     * Attempts to execute the request up to [maxAttempts] times.
     * If a response with code 429 is encountered, it will delay with exponential backoff.
     */
    private suspend fun executeRequestWithBackoff(
        request: Request,
        maxAttempts: Int = 3,
        initialDelay: Long = 50L
    ): Response? {
        var attempt = 0
        var delayTime = initialDelay
        while (attempt < maxAttempts) {
            val response = client.newCall(request).execute()
            if (response.isSuccessful || response.code != 429) {
                return response
            }
            // Close the response and delay if it's a rate-limit error.
            response.close()
            delay(delayTime)
            delayTime *= 2
            attempt++
        }
        return null
    }

    // -------------------------
    // API Methods
    // -------------------------
    // New wrapper that holds detailed recipes
    data class ComplexSearchResultWithInfo(
        val results: List<RecipeDetails>,
        val offset: Int,
        val number: Int,
        val totalResults: Int
    )

    /**
     * Single‐call search that returns full RecipeDetails (incl. ingredients & instructions).
     */
    suspend fun searchRecipesByQuery(
        query: String,
        number: Int = 15
    ): List<RecipeDetails>? {
        val baseUrl = "https://$RAPIDAPIHOST/recipes/complexSearch"
        val url = baseUrl.toHttpUrlOrNull()
            ?.newBuilder()
            ?.addQueryParameter("query", query)
            ?.addQueryParameter("number", number.toString())
            // ← ask Spoonacular to inline all recipe info
            ?.addQueryParameter("addRecipeInformation", "true")
            ?.build()
            ?: return null

        val request = Request.Builder()
            .url(url)
            .addHeader("x-rapidapi-key", apiKey)
            .addHeader("x-rapidapi-host", RAPIDAPIHOST)
            .build()

        val response = executeRequestWithBackoff(request) ?: return null
        response.use {
            if (!it.isSuccessful) {
                println("Error fetching recipes: ${it.code}")
                return null
            }
            val body = it.body!!.string()
            val wrapper = gson.fromJson(body, ComplexSearchResultWithInfo::class.java)
            return wrapper.results
        }
    }

    /**
     * Convenience to turn the above details into your RecipeDisplayInfo model.
     */
    suspend fun searchDisplayInfoByQuery(
        query: String,
        number: Int = 15
    ): List<RecipeDisplayInfo>? {
        return searchRecipesByQuery(query, number)
            ?.map { details ->
                RecipeDisplayInfo(
                    title    = details.title,
                    imageUrl = details.image,
                    cookTime = "${details.readyInMinutes} mins",
                    recipeId = details.id
                )
            }
    }
    suspend fun searchRecipesWithInfoAndFilters(
        query: String,
        diet: String?,
        minReadyTime: Int?,
        maxReadyTime: Int?,
        number: Int = 15
    ): List<RecipeDetails>? {
        val urlBuilder = "https://$RAPIDAPIHOST/recipes/complexSearch".toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter("query", query)
            .addQueryParameter("number", number.toString())
            .addQueryParameter("addRecipeInformation", "true")

        diet?.takeIf(String::isNotEmpty)?.let { urlBuilder.addQueryParameter("diet", it) }
        minReadyTime?.let { urlBuilder.addQueryParameter("minReadyTime", it.toString()) }
        maxReadyTime?.let { urlBuilder.addQueryParameter("maxReadyTime", it.toString()) }

        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("x-rapidapi-key", apiKey)
            .addHeader("x-rapidapi-host", RAPIDAPIHOST)
            .build()

        val resp = executeRequestWithBackoff(request) ?: return null
        resp.use {
            if (!it.isSuccessful) return null
            val wrapper = gson.fromJson(it.body!!.string(), ComplexSearchResultWithInfo::class.java)
            return wrapper.results
        }
    }
    suspend fun searchRecipesByIngredients(
        ingredients: List<String>,
        number: Int = 50
    ): List<RecipeSummary>? {
        val baseUrl = "https://$RAPIDAPIHOST/recipes/findByIngredients"
        val urlBuilder = baseUrl.toHttpUrlOrNull()?.newBuilder() ?: return null
        urlBuilder.addQueryParameter("ingredients", ingredients.joinToString(","))
        urlBuilder.addQueryParameter("number", number.toString())
        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("x-rapidapi-key", this.apiKey)
            .addHeader("x-rapidapi-host", RAPIDAPIHOST)
            .build()
        val response = executeRequestWithBackoff(request) ?: return null
        response.use {
            if (!it.isSuccessful) {
                println("Error fetching recipe summaries: ${it.code}")
                return null
            }
            val responseBody = it.body?.string() ?: return null
            return gson.fromJson(responseBody, Array<RecipeSummary>::class.java).toList()
        }
    }

    suspend fun getRecipeDetails(recipeId: Int): RecipeDetails? {
        val baseUrl = "https://$RAPIDAPIHOST/recipes/$recipeId/information"
        val urlBuilder = baseUrl.toHttpUrlOrNull()?.newBuilder() ?: return null
        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("x-rapidapi-key", apiKey)
            .addHeader("x-rapidapi-host", RAPIDAPIHOST)
            .build()
        val response = executeRequestWithBackoff(request) ?: return null
        response.use {
            if (!it.isSuccessful) {
                println("Error fetching recipe details: ${it.code}")
                return null
            }
            val responseBody = it.body?.string() ?: return null
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
        val baseUrl = "https://$RAPIDAPIHOST/recipes/complexSearch"
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
            .addHeader("x-rapidapi-host", RAPIDAPIHOST)
            .build()
        val response = executeRequestWithBackoff(request) ?: return null
        response.use {
            if (!it.isSuccessful) {
                println("Error fetching filtered recipes: ${it.code}")
                return null
            }
            val responseBody = it.body?.string() ?: return null
            val wrapper = gson.fromJson(responseBody, ComplexSearchResult::class.java)
            return wrapper.results
        }
    }

    suspend fun getIngredientSuggestions(query: String, number: Int = 20): List<String>? {
        val baseUrl = "https://$RAPIDAPIHOST/food/ingredients/autocomplete"
        val urlBuilder = baseUrl.toHttpUrlOrNull()?.newBuilder() ?: return null
        urlBuilder.addQueryParameter("query", query)
        urlBuilder.addQueryParameter("number", number.toString())
        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("x-rapidapi-key", apiKey)
            .addHeader("x-rapidapi-host", RAPIDAPIHOST)
            .build()
        val response = executeRequestWithBackoff(request) ?: return null
        response.use {
            if (!it.isSuccessful) {
                println("Error fetching ingredient suggestions: ${it.code}")
                return null
            }
            val responseBody = it.body?.string() ?: return null
            val suggestions = gson.fromJson(responseBody, Array<IngredientSuggestion>::class.java).toList()
            return suggestions.map { suggestion -> suggestion.name }
        }
    }
}
