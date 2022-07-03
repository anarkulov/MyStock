package com.erzhan.mystock.presentation.company_info

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erzhan.mystock.domain.repository.StockRepository
import com.erzhan.mystock.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanyInfoViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: StockRepository
) : ViewModel() {

    var state by mutableStateOf(CompanyInfoState())

    init {
        getCompanyInfo()
    }

    fun onEvent(event: CompanyInfoEvent) {
        if (event is CompanyInfoEvent.OnRefresh) {
            getCompanyInfo(true)
        }
    }

    private fun getCompanyInfo(
        fetchFromRemote: Boolean = false
    ) {
        viewModelScope.launch {
            val symbol = savedStateHandle.get<String>("symbol") ?: return@launch
            state = state.copy(isLoading = true)
            val companyInfoResult = async {
                repository.getCompanyInfo(fetchFromRemote, symbol)
            }
            val intradayInfoResult = async {
                repository.getIntradayInfo(symbol)
            }

            companyInfoResult.await().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d("ConpanyInfoViewModel", "Success: ${result.data != null}")
                        state = state.copy(
                            company = result.data,
                            isLoading = false,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        Log.d("ConpanyInfoViewModel", "Error: ${result.message}")
                        state = state.copy(
                            isLoading = false,
                            error = result.message,
                            company = null
                        )
                    }
                    is Resource.Loading -> {
                        Log.d("ConpanyInfoViewModel", "Loading: ${result.isLoading}")
                        state = state.copy(
                            isLoading = true
                        )
                    }
                }
            }

            when (val result = intradayInfoResult.await()) {
                is Resource.Success -> {
                    Log.d("ConpanyInfoViewModel: intraday", "Success")
                    state = state.copy(
                        stockInfos = result.data ?: emptyList(),
                        isLoading = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    Log.d("ConpanyInfoViewModel: intraday", "Error: ${result.message}")

                    state = state.copy(
                        isLoading = false,
                        error = result.message,
                        company = null,
                        stockInfos = emptyList()
                    )
                }
                is Resource.Loading -> Unit
            }
        }
    }
}