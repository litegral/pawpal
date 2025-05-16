package com.litegral.pawpal

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Repository to manage liked pets data
 */
class PetRepository private constructor(private val context: Context) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val gson = Gson()
    private val likedPetsKey = "liked_pets"

    /**
     * Get all liked pets
     */
    fun getLikedPets(): List<CardItem> {
        val json = sharedPreferences.getString(likedPetsKey, null) ?: return emptyList()
        val type = object : TypeToken<List<CardItem>>() {}.type
        return gson.fromJson(json, type)
    }

    /**
     * Save a pet to liked pets list
     */
    fun likePet(pet: CardItem) {
        val currentLikedPets = getLikedPets().toMutableList()

        // Only add if not already in the list
        if (currentLikedPets.none { it.id == pet.id }) {
            currentLikedPets.add(pet)
            saveLikedPets(currentLikedPets)
        }
    }

    /**
     * Remove a pet from liked pets list
     */
    fun unlikePet(petId: String) {
        val currentLikedPets = getLikedPets().toMutableList()
        currentLikedPets.removeAll { it.id == petId }
        saveLikedPets(currentLikedPets)
    }

    /**
     * Check if a pet is already liked
     */
    fun isPetLiked(petId: String): Boolean {
        return getLikedPets().any { it.id == petId }
    }

    /**
     * Save the entire list of liked pets
     */
    private fun saveLikedPets(pets: List<CardItem>) {
        val json = gson.toJson(pets)
        sharedPreferences.edit().putString(likedPetsKey, json).apply()
    }

    companion object {
        @Volatile
        private var INSTANCE: PetRepository? = null

        fun getInstance(context: Context): PetRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PetRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
