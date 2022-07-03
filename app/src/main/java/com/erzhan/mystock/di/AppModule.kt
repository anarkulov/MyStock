package com.erzhan.mystock.di

import android.app.Application
import androidx.room.Room
import com.erzhan.mystock.data.local.StockDatabase
import com.erzhan.mystock.data.remote.StockApi
import com.erzhan.mystock.data.remote.StockApi.Companion.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideStockApi(): StockApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(StockApi::class.java)
    }

    @Provides
    @Singleton
    fun provideStockDatabase(app: Application): StockDatabase {
        return Room
            .databaseBuilder(
                app,
                StockDatabase::class.java,
                "stock.db"
            )
            .build()
    }


}