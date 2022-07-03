package com.erzhan.mystock.di

import com.erzhan.mystock.data.csv.CSVParser
import com.erzhan.mystock.data.csv.CompanyListingsParser
import com.erzhan.mystock.data.csv.IntradayInfoParser
import com.erzhan.mystock.data.repository.StockRepositoryImpl
import com.erzhan.mystock.domain.model.CompanyListing
import com.erzhan.mystock.domain.model.IntradayInfo
import com.erzhan.mystock.domain.repository.StockRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCompanyListingsParser(
        companyListingsParser: CompanyListingsParser
    ): CSVParser<CompanyListing>

     @Binds
    @Singleton
    abstract fun bindIntradayInfoParser(
        companyListingsParser: IntradayInfoParser
    ): CSVParser<IntradayInfo>

    @Binds
    @Singleton
    abstract fun bindStockRepository(
        stockRepositoryImpl: StockRepositoryImpl
    ): StockRepository

}