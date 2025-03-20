package com.example.mealplannerapp

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object RecipeSearch {
    private val client = OkHttpClient()
    private val gson = Gson()

    // Data class representing summary info for recipes returned by findByIngredients
    data class RecipeSummary(
        val id: Int,
        val title: String,
        val image: String,
        val readyInMinutes: Int
    )

    // Data class representing detailed recipe information
    data class RecipeDetails(
        val id: Int,
        val title: String,
        val image: String,
        val readyInMinutes: Int
        // You can add more fields if needed.
    )

    // Data class used for displaying information in your RecyclerView
    data class RecipeDisplayInfo(
        val title: String,
        val imageUrl: String,
        val cookTime: String
    )

    /**
     * Searches for recipes that can be made with the given ingredients.
     *
     * @param ingredients List of ingredient names.
     * @param number Number of recipes to return.
     * @param apiKey Your Spoonacular API key.
     * @return A list of RecipeSummary objects or null if an error occurs.
     */
    data class ComplexSearchResult(
        val results: List<RecipeSummary>,
        val offset: Int,
        val number: Int,
        val totalResults: Int
    )

    /**
     * Searches for recipes by a text query (e.g., dish name) using the Spoonacular complexSearch endpoint.
     *21
     * @return A list of RecipeSummary objects or null if an error occurs.
     */
    fun searchRecipesByQuery(
        query: String,
        number: Int = 15,
        apiKey: String = "0c2296339d27412a8d9afdf7557ee6a7"
    ): List<RecipeSummary>? {
        val baseUrl = "https://api.spoonacular.com/recipes/complexSearch"
        val urlBuilder = baseUrl.toHttpUrlOrNull()?.newBuilder() ?: return null
        urlBuilder.addQueryParameter("query", query)
        urlBuilder.addQueryParameter("number", number.toString())
        urlBuilder.addQueryParameter("apiKey", apiKey)

        val request = Request.Builder()
            .url(urlBuilder.build())
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

    fun searchRecipesByIngredients(
        ingredients: List<String>,
        number: Int = 50,
        apiKey: String = "0c2296339d27412a8d9afdf7557ee6a7"
    ): List<RecipeSummary>? {
        val baseUrl = "https://api.spoonacular.com/recipes/findByIngredients"
        val urlBuilder = baseUrl.toHttpUrlOrNull()?.newBuilder() ?: return null
        urlBuilder.addQueryParameter("ingredients", ingredients.joinToString(","))
        urlBuilder.addQueryParameter("number", number.toString())
        urlBuilder.addQueryParameter("apiKey", apiKey)

        val request = Request.Builder()
            .url(urlBuilder.build())
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

    /**
     * Fetches detailed recipe information for the given recipe ID.
     *
     * @param recipeId The ID of the recipe.
     * @param apiKey Your Spoonacular API key.
     * @return A RecipeDetails object or null if an error occurs.
     */
    fun getRecipeDetails(recipeId: Int, apiKey: String): RecipeDetails? {
        val baseUrl = "https://api.spoonacular.com/recipes/$recipeId/information"
        val urlBuilder = baseUrl.toHttpUrlOrNull()?.newBuilder() ?: return null
        urlBuilder.addQueryParameter("apiKey", apiKey)

        val request = Request.Builder()
            .url(urlBuilder.build())
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

    /**
     * Retrieves a simplified display object containing the recipe title, image URL, and cook time.
     *
     * @param recipeId The ID of the recipe.
     * @param apiKey Your Spoonacular API key.
     * @return A RecipeDisplayInfo object or null if an error occurs.
     */
    fun getRecipeDisplayInfo(recipeId: Int, apiKey: String): RecipeDisplayInfo? {
        val details = getRecipeDetails(recipeId, apiKey) ?: return null
        return RecipeDisplayInfo(
            title = details.title,
            imageUrl = details.image,
            cookTime = "${details.readyInMinutes} mins"
        )
    }
}
