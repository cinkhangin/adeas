@file:Suppress("unused")

package com.ckgin.adeas

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.rewarded.RewardedAd

fun initializeAdmob(
    context: Context,
    adUnits: AdUnits = AdUnits(),
    debug: Boolean = false
) = Adeas.initialize(context, adUnits, debug)


fun loadRewardedAd(context: Context) {
    Adeas.load(AdType.REWARDED, context)
}

fun loadInterstitialAd(context: Context) {
    Adeas.load(AdType.INTERSTITIAL, context)
}

fun loadBannerAd(context: Context) {
    Adeas.load(AdType.BANNER, context)
}

fun loadAds(context: Context, adType: AdType) {
    Adeas.load(adType, context)
}

fun loadAllAds(context: Context) {
    Adeas.loadAll(context)
}

fun Activity.loadRewardedAd(onRewardedLoaded: (RewardedAd) -> Unit = {}) {
    Adeas.load(AdType.REWARDED, context = this, onRewardedLoaded = onRewardedLoaded)
}

fun Activity.loadInterstitialAd() {
    Adeas.load(AdType.INTERSTITIAL, context = this)
}

fun Activity.loadBannerAd() {
    Adeas.load(AdType.BANNER, context = this)
}

fun Activity.loadAds(adType: AdType, onRewardedLoaded: (RewardedAd) -> Unit = {}) {
    Adeas.load(adType, context = this, onRewardedLoaded = onRewardedLoaded)
}

fun Activity.loadAllAds(onRewardedLoaded: (RewardedAd) -> Unit = {}) {
    Adeas.loadAll(context = this, onRewardedLoaded = onRewardedLoaded)
}

fun Activity.showRewardedAd(action: (result: Boolean) -> Unit) {
    Adeas.showRewardedAd(activity = this, action)
}

fun Activity.showInterstitialAd(action: (result: Boolean) -> Unit) {
    Adeas.showInterstitialAd(activity = this, action)
}

fun onClosedRewarded(action: () -> Unit) {
    Adeas.onClosedRewarded(action)
}
