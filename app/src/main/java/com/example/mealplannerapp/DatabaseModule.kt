package com.example.mealplannerapp

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule{
    @Singleton
    @Provides
    fun provideRealm():Realm{
        val config=RealmConfiguration.Builder(
            schema = setOf(
                User::class,
                Ingredient::class,
                SavedRecipe::class
            )
        )
            .compactOnLaunch()
            .build()
        return Realm.open(config)
    }

    @Singleton
    @Provides
 fun provideMongoRepository(realm: Realm): MongoRepository{
        return MongoRepositoryImpl(realm=realm)
    }
}