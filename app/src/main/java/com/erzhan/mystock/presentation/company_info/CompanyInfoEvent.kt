package com.erzhan.mystock.presentation.company_info

sealed class CompanyInfoEvent {
    object OnRefresh: CompanyInfoEvent()
}