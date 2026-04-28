@file:Suppress("unused")

package com.ckgin.adeas

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object Adeas {
    private val TAG = Adeas::class.java.simpleName

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var debugMode: Boolean = true

    @Suppress("unused")
    private val appId = "ca-app-pub-8780587618161459~6948608347"

    private val testUnits = AdUnits()
    private var adUnits = AdUnits()

    private var adView: AdView? = null

    private var isEnable = true

    private const val keyEnable = "is_enable"

    private val mutableState = MutableStateFlow(true)
    val state = mutableState.asStateFlow()

    private var onCloseRewarded: (() -> Unit)? = null

    private val isRewardedAdLoaded = MutableStateFlow(false)
    val rewarded = isRewardedAdLoaded.asStateFlow()


    fun createBanner(context: Context): AdView {
        return adView ?: AdView(context).apply {
            val adRequest = AdRequest.Builder().build()
            setAdSize(AdSize.BANNER)
            adUnitId = getAdString(AdType.BANNER)
            loadAd(adRequest)
        }
    }

    fun initialize(context: Context, adUnits: AdUnits, isDebugMode: Boolean) {
        MobileAds.initialize(context)

        debugMode = isDebugMode
        adView = AdView(context)
        this.adUnits = adUnits

        //isEnable = PrefStore(context).readBoolean(keyEnable, true)
        mutableState.value = isEnable
    }

    fun enableAds(context: Context) {
        //PrefStore(context).writeBoolean(keyEnable, true)
        isEnable = true
        mutableState.value = isEnable
    }

    fun disableAds(context: Context) {
        //PrefStore(context).writeBoolean(keyEnable, false)
        isEnable = false
        mutableState.value = isEnable
    }

    fun load(adType: AdType, context: Context, onRewardedLoaded: () -> Unit = {}) {
        val adRequest = AdRequest.Builder().build()

        when (adType) {
            AdType.REWARDED -> loadRewarded(adRequest, context, onRewardedLoaded)
            AdType.BANNER -> loadBanner(adRequest, context)
            AdType.INTERSTITIAL -> loadInterstitial(adRequest, context)
        }
    }

    fun loadAll(context: Context, onRewardedLoaded: () -> Unit = {}) {
        load(AdType.BANNER, context)
        load(AdType.INTERSTITIAL, context)
        load(AdType.REWARDED, context, onRewardedLoaded)
    }

    private fun loadInterstitial(adRequest: AdRequest, context: Context) {
        if (interstitialAd != null) return

        val addLoadCallback = object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, adError.message)
                interstitialAd = null
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                Log.d(TAG, " Ad was loaded ")
                interstitialAd = ad
            }
        }

        val id = getAdString(AdType.INTERSTITIAL)
        InterstitialAd.load(context, id, adRequest, addLoadCallback)
    }

    private fun loadBanner(adRequest: AdRequest, context: Context) {
        adView = AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = getAdString(AdType.BANNER)
            loadAd(adRequest)
        }
    }

    private fun loadRewarded(adRequest: AdRequest, context: Context, onRewardedLoaded: () -> Unit) {
        if (rewardedAd != null) return

        val rewardedAdLoadCallback = object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                super.onAdLoaded(ad)
                Log.d(TAG, "Ad is loaded")
                rewardedAd = ad
                isRewardedAdLoaded.value = true
                onRewardedLoaded()
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                super.onAdFailedToLoad(error)
                Log.e(TAG, error.message)
                rewardedAd = null
                isRewardedAdLoaded.value = false
            }
        }

        val id = getAdString(AdType.REWARDED)
        RewardedAd.load(context, id, adRequest, rewardedAdLoadCallback)

    }

    private fun getAdString(adType: AdType): String {
        return when (adType) {
            AdType.REWARDED -> if (debugMode) testUnits.rewarded
            else adUnits.rewarded

            AdType.INTERSTITIAL -> if (debugMode) testUnits.interstitial
            else adUnits.interstitial

            AdType.BANNER -> if (debugMode) testUnits.banner
            else adUnits.banner
        }
    }

    fun showRewardedAd(activity: Activity, action: (Boolean) -> Unit) {
        if (!isEnable) return

        if (rewardedAd == null) {
            action(false)
            return
        }

        rewardedAd?.apply {
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdClicked() {}

                override fun onAdDismissedFullScreenContent() {
                    onCloseRewarded?.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "${adError.code}: ${adError.message}")
                }

                override fun onAdImpression() {}

                override fun onAdShowedFullScreenContent() {}
            }

            show(activity) {
                action(true)
                rewardedAd = null
                load(AdType.REWARDED, activity)
            }
        }
    }

    fun showInterstitialAd(activity: Activity, action: (result: Boolean) -> Unit) {
        if (!isEnable) return

        if (interstitialAd == null) {
            action(false)
            return
        }

        interstitialAd?.apply {
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdClicked() {}
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    action(true)
                }

                override fun onAdImpression() {}
                override fun onAdShowedFullScreenContent() {}
            }
            show(activity)
        }
    }

    fun onClosedRewarded(action: () -> Unit) {
        onCloseRewarded = action
    }

    fun clearAdView() {
        adView = null
    }
}