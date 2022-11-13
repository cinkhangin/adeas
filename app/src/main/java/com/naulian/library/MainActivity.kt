package com.naulian.library

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.naulian.adeas.AdUnits
import com.naulian.adeas.Adeas
import com.naulian.adeas.BannerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adUnits = AdUnits()
        Adeas.initialize(this, adUnits, true)
        Adeas.loadAll(this)

        findViewById<BannerView>(R.id.bannerView).loadAd()

        findViewById<Button>(R.id.btnInterstitial).setOnClickListener {
            Adeas.showInterstitialAd(this){}
        }

        findViewById<Button>(R.id.btnRewarded).setOnClickListener {
            Adeas.showRewardedAd(this){}
        }
    }
}