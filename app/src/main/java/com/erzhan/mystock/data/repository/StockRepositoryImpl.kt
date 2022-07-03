package com.erzhan.mystock.data.repository

import android.util.Log
import com.erzhan.mystock.data.csv.CSVParser
import com.erzhan.mystock.data.csv.CompanyListingsParser
import com.erzhan.mystock.data.csv.IntradayInfoParser
import com.erzhan.mystock.data.local.StockDatabase
import com.erzhan.mystock.data.mapper.toCompanyInfo
import com.erzhan.mystock.data.mapper.toCompanyInfoEntity
import com.erzhan.mystock.data.mapper.toCompanyListing
import com.erzhan.mystock.data.mapper.toCompanyListingEntity
import com.erzhan.mystock.data.remote.StockApi
import com.erzhan.mystock.domain.model.CompanyInfo
import com.erzhan.mystock.domain.model.CompanyListing
import com.erzhan.mystock.domain.model.IntradayInfo
import com.erzhan.mystock.domain.repository.StockRepository
import com.erzhan.mystock.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val api: StockApi,
    private val db: StockDatabase,
    private val companyListingsParser: CSVParser<CompanyListing>,
    private val intradayInfoParser: CSVParser<IntradayInfo>
) : StockRepository {

    private val dao = db.dao

    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow {
            emit(Resource.Loading(true))
            val localListings = dao.searchCompanyListing(query)
            emit(Resource.Success(localListings.map { it.toCompanyListing() }))

            val isDbEmpty = localListings.isEmpty() && query.isBlank()
            val shouldJustLoadFromCache = !isDbEmpty && !fetchFromRemote
            if (shouldJustLoadFromCache) {
                emit(Resource.Loading(false))
                return@flow
            }

            val remoteListings = try {
                val response = api.getListings()
                companyListingsParser.parse(response.byteStream())
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("IOException"))
                null
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("HttpException"))
                null
            }

            remoteListings?.let { list: List<CompanyListing> ->
                dao.clearCompanyListings()
                dao.insetCompanyListings(
                    list.map { it.toCompanyListingEntity() }
                )
                emit(
                    Resource.Success(
                        data = dao
                            .searchCompanyListing("")
                            .map { it.toCompanyListing() })
                )
            }
        }
    }

    override suspend fun getIntradayInfo(symbol: String): Resource<List<IntradayInfo>> {
        return try {
            val response = api.getIntradayInfo(symbol)
            val result = intradayInfoParser.parse(response.byteStream())
            Resource.Success(result)
        } catch (e: IOException) {
            e.printStackTrace()
            Resource.Error("IOException")
        } catch (e: HttpException) {
            e.printStackTrace()
            Resource.Error("HttpException")
        }
    }

    override suspend fun getCompanyInfo(
        fetchFromRemote: Boolean,
        symbol: String
    ): Flow<Resource<CompanyInfo>> {
        return flow {
            emit(Resource.Loading(true))
            val localCompanyInfo = dao.getCompanyInfo(symbol = symbol)

            val isDbEmpty = localCompanyInfo == null
            val shouldJustLoadFromCache = !isDbEmpty && !fetchFromRemote
            Log.d("StockRepository", "local: $localCompanyInfo, $shouldJustLoadFromCache")

            if (shouldJustLoadFromCache) {
                emit(Resource.Success(data = localCompanyInfo?.toCompanyInfo()))
                emit(Resource.Loading(false))
                return@flow
            }

            val remoteCompanyInfo = try {
                api.getCompanyInfo(symbol).toCompanyInfoEntity()
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("IOException"))
                null
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("HttpException"))
                null
            }

            remoteCompanyInfo?.let { companyInfoEntity ->
                dao.clearCompanyInfo(companyInfoEntity)
                dao.insertCompanyInfo(companyInfoEntity)
                Log.d("StockRepository", "remote: $companyInfoEntity")
                emit(
                    Resource.Success(
                        data = dao.getCompanyInfo(symbol)?.toCompanyInfo()
                    )
                )
            }
        }
    }
}