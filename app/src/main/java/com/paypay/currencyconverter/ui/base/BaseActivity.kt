package com.paypay.currencyconverter.ui.base

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.paypay.currencyconverter.di.modules.ActivityModule
import com.paypay.currencyconverter.di.components.ActivityComponent
import com.paypay.currencyconverter.utils.enableIntervalAPICallAlarmService

abstract class BaseActivity<T : ViewDataBinding> : AppCompatActivity() {
    lateinit var binding: T
    abstract fun getLayoutResource(): Int
    abstract fun initComponents()
    abstract fun injectActivity(activityComponent: ActivityComponent)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, getLayoutResource())
        binding.lifecycleOwner = this
        setContentView(binding.root)
        injectActivity(getActivityComponent())
        initComponents()
        enableIntervalAPICallAlarmService()
    }

    private fun getActivityComponent(): ActivityComponent {
        return getAppComponent().activityComponent(ActivityModule())
    }

    private fun getAppComponent() = BaseApplication.appComponent

    open fun startActivity(cls: Class<*>?, finishSelf: Boolean) {
        val intent = Intent(this, cls)
        startActivity(intent)
        if (finishSelf) {
            finish()
        }
    }

    fun toast(message: String) {
        Toast.makeText(this, "Toast: $message", Toast.LENGTH_SHORT).show()
    }

}