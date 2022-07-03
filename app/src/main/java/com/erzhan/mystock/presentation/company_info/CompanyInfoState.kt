package com.erzhan.mystock.presentation.company_info

import com.erzhan.mystock.domain.model.CompanyInfo
import com.erzhan.mystock.domain.model.IntradayInfo

data class CompanyInfoState(
    val stockInfos: List<IntradayInfo> = emptyList(),
    val company: CompanyInfo? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)