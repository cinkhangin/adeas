@file:Suppress("unused")

package com.ckgin.adeas

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.ckgin.keeper.Keeper
import com.ckgin.keeper.datastore
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions
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

    private val isAdsEnabledKey = booleanPreferencesKey("is_ads_enabled")

    private val _isAdsEnabled = MutableStateFlow(true)
    val isAdsEnabled = _isAdsEnabled.asStateFlow()

    private var onCloseRewarded: (() -> Unit)? = null

    private val _isRewardedAdLoaded = MutableStateFlow(false)
    val isRewardedAdLoaded = _isRewardedAdLoaded.asStateFlow()

    private var ssvOptions: ServerSideVerificationOptions? = null

    fun setSSVOptions(userId: String = "", customData: String = "") {
        val builder = ServerSideVerificationOptions.Builder()

        if (userId.isNotEmpty()) {
            builder.setUserId(userId)
        }

        if (customData.isNotEmpty()) {
            builder.setCustomData(customData)
        }
        val options = builder.build()
        ssvOptions = options
        rewardedAd?.setServerSideVerificationOptions(options)
    }

    fun createBanner(context: Context): AdView {
        adView?.let { return it }
        val view = AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = getAdString(AdType.BANNER)
            loadAd(AdRequest.Builder().build())
        }
        adView = view
        return view
    }

    fun initialize(context: Context, adUnits: AdUnits, isDebugMode: Boolean) {
        MobileAds.initialize(context)

        debugMode = isDebugMode
        adView = null
        this.adUnits = adUnits

        isEnable = isAdsEnabled(context)
        _isAdsEnabled.value = isEnable
    }

    suspend fun enableAds(context: Context) {
        val keeper = Keeper(context.datastore)
        keeper.keep(isAdsEnabledKey, true)

        isEnable = true
        _isAdsEnabled.value = isEnable
    }

    suspend fun disableAds(context: Context) {
        val keeper = Keeper(context.datastore)
        keeper.keep(isAdsEnabledKey, false)

        isEnable = false
        _isAdsEnabled.value = isEnable
    }

    fun isAdsEnabled(context: Context): Boolean {
        val keeper = Keeper(context.datastore)
        return keeper.take(isAdsEnabledKey, true)
    }

    fun load(adType: AdType, context: Context, onRewardedLoaded: (RewardedAd) -> Unit = {}) {
        val adRequest = AdRequest.Builder().build()

        when (adType) {
            AdType.REWARDED -> loadRewarded(adRequest, context, onRewardedLoaded)
            AdType.BANNER -> loadBanner(adRequest, context)
            AdType.INTERSTITIAL -> loadInterstitial(adRequest, context)
        }
    }

    fun loadAll(context: Context, onRewardedLoaded: (RewardedAd) -> Unit = {}) {
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
        adView?.destroy()
        adView = AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = getAdString(AdType.BANNER)
            loadAd(adRequest)
        }
    }

    private fun loadRewarded(
        adRequest: AdRequest,
        context: Context,
        onRewardedLoaded: (RewardedAd) -> Unit
    ) {
        if (rewardedAd != null) return

        val rewardedAdLoadCallback = object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                super.onAdLoaded(ad)
                Log.d(TAG, "Ad is loaded")
                ssvOptions?.let { ad.setServerSideVerificationOptions(it) }
                onRewardedLoaded(ad)
                rewardedAd = ad
                _isRewardedAdLoaded.value = true
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                super.onAdFailedToLoad(error)
                Log.e(TAG, error.message)
                rewardedAd = null
                _isRewardedAdLoaded.value = false
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

    fun showRewardedAd(
        activity: Activity,
        action: (Boolean) -> Unit,
        loadAfterWatch: Boolean = true
    ) {
        if (!isEnable) return

        if (rewardedAd == null) {
            action(false)
            return
        }

        rewardedAd?.apply {
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdClicked() {}

                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    onCloseRewarded?.invoke()

                    if (loadAfterWatch) {
                        load(AdType.REWARDED, activity)
                    }
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "${adError.code}: ${adError.message}")
                    rewardedAd = null
                    load(AdType.REWARDED, activity)
                }

                override fun onAdImpression() {}

                override fun onAdShowedFullScreenContent() {}
            }

            show(activity) {
                action(true)
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
                    load(AdType.INTERSTITIAL, activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "${adError.code}: ${adError.message}")
                    interstitialAd = null
                    load(AdType.INTERSTITIAL, activity)
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
        adView?.destroy()
        adView = null
    }
}
