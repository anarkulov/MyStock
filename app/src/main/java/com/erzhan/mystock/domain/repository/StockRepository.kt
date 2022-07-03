package com.erzhan.mystock.domain.repository

import com.erzhan.mystock.domain.model.CompanyInfo
import com.erzhan.mystock.domain.model.CompanyListing
import com.erzhan.mystock.domain.model.IntradayInfo
import com.erzhan.mystock.util.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {

    suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>>

    suspend fun getIntradayInfo(
        symbol: String
    ): Resource<List<IntradayInfo>>

    suspend fun getCompanyInfo(
        fetchFromRemote: Boolean,
        symbol: String
    ): Flow<Resource<CompanyInfo>>

}