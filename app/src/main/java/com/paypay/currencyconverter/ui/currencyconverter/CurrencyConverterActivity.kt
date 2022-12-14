package com.paypay.currencyconverter.ui.currencyconverter

import androidx.core.view.isVisible
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.lifecycleScope
import com.paypay.currencyconverter.R
import com.paypay.currencyconverter.utils.ConverterIntent
import com.paypay.currencyconverter.database.models.CurrencyResponse
import com.paypay.currencyconverter.utils.DataState
import com.paypay.currencyconverter.utils.DataState.*
import com.paypay.currencyconverter.database.models.ExchangeRate
import com.paypay.currencyconverter.databinding.ActivityCurrencyConvertBinding
import com.paypay.currencyconverter.di.components.ActivityComponent
import com.paypay.currencyconverter.ui.base.BaseActivity
import com.paypay.currencyconverter.utils.getUserInput
import kotlinx.android.synthetic.main.activity_currency_convert.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class CurrencyConverterActivity : BaseActivity<ActivityCurrencyConvertBinding>() {
    @Inject
    lateinit var currencyConverterViewModel: CurrencyConverterViewModel

    @Inject
    lateinit var currencyViewAdapter: CurrencyViewAdapter

    override fun injectActivity(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun getLayoutResource(): Int {
        return R.layout.activity_currency_convert
    }

    override fun initComponents() {
        binding.apply {
            viewModel = currencyConverterViewModel
            adapter = currencyViewAdapter
            activity = this@CurrencyConverterActivity
        }

        lifecycleScope.launch {
            launch {
                currencyConverterViewModel.intentAction.send(ConverterIntent.Fetch)
            }
            launch {
                currencyConverterViewModel.dataState.collect { render(it) }
            }
        }
    }

    private fun render(it: DataState) {
        progressBar.isVisible = it is Loading
        when (it) {
            is Success -> updateBaseRateSpinner(it.data)
            is ConversionSuccess -> updateExchangeRateList(it.data)
            is Error -> toast(it.error)
        }
    }

    private fun updateExchangeRateList(it: List<ExchangeRate>?) {
        currencyViewAdapter.setCurrencyList(it)
    }

    private fun updateBaseRateSpinner(it: CurrencyResponse?) {
        lifecycleScope.launch {
            currencyConverterViewModel.intentAction.send(ConverterIntent.UpdateSpinner(it))
        }
    }

    var changeOfCurrency = ObservableField<ExchangeRate>().apply {
        addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                this@apply.get()?.let { inputSendToConverter(it.rate) }
            }
        })
    }

    private fun inputSendToConverter(selectedRate: Double?) {
        lifecycleScope.launch {
            val startConversion = ConverterIntent.Convert(getUserInput(binding), selectedRate)
            currencyConverterViewModel.intentAction.send(startConversion)
        }
    }

}