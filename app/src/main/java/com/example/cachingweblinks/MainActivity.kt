package com.example.cachingweblinks

import android.content.ComponentName
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cachingweblinks.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var mCustomTabsClient: CustomTabsClient
    private val mPackageNameToBind: String?="com.android.chrome"
    lateinit var binding : ActivityMainBinding
    var  connection:CustomTabsServiceConnection?=null
    lateinit var  intentBuilder: CustomTabsIntent.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerview.layoutManager=LinearLayoutManager(this)

        binding.launchNormalTabButton.setOnClickListener {
            intentBuilder = CustomTabsIntent.Builder()
            intentBuilder.build().launchUrl(this, Uri.parse("https://play.google.com/store/apps"))
        }

        binding.lauchPreloadedTabButton.setOnClickListener {
            intentBuilder.build().launchUrl(this, Uri.parse("https://play.google.com/store/apps"))
        }

        binding.setupService.setOnClickListener {
            setupCustomTabs()
        }
    }

    private fun setupCustomTabs() {
        connection= object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(
                name: ComponentName,
                client: CustomTabsClient
            ) {
                mCustomTabsClient = client
                client.warmup(0L)
                val callback = RabbitCallback()
                val customTabsSession = client.newSession(callback)
                customTabsSession?.mayLaunchUrl(Uri.parse("https://play.google.com/store/apps"),null,null)

                intentBuilder= CustomTabsIntent.Builder()
                if (customTabsSession != null) {
                    intentBuilder.setSession(customTabsSession)
                }
                Toast.makeText(this@MainActivity ,"connected",Toast.LENGTH_LONG).show()

            }
            override fun onServiceDisconnected(name: ComponentName) {
                Toast.makeText(this@MainActivity ,"disconnected",Toast.LENGTH_LONG).show()
            }
        }

        CustomTabsClient.bindCustomTabsService(this, mPackageNameToBind,
            connection as CustomTabsServiceConnection
        )
    }

    class RabbitCallback : CustomTabsCallback() {
        override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
            super.onNavigationEvent(navigationEvent, extras)
            Log.d("Nav", navigationEvent.toString())
            when (navigationEvent) {
                1 -> Log.d("Navigation", "Start") // NAVIGATION_STARTED
                2 -> Log.d("Navigation", "Finished") // NAVIGATION_FINISHED
                3 -> Log.d("Navigation", "Failed") // NAVIGATION_FAILED
                4 -> Log.d("Navigation", "Aborted") // NAVIGATION_ABORTED
                5 -> Log.d("Navigation", "Tab Shown") // TAB_SHOWN
                6 -> Log.d("Navigation", "Tab Hidden") // TAB_HIDDEN
                else -> Log.d("Navigation", "Else")
            }
        }
    }

}
