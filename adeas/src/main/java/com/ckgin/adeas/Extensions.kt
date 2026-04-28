@file:Suppress("unused")

package com.ckgin.adeas

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment

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

fun showRewardedAd(activity: Activity, action: (result: Boolean) -> Unit) {
    Adeas.showRewardedAd(activity, action)
}

fun showInterstitialAd(activity: Activity, action: (result: Boolean) -> Unit) {
    Adeas.showInterstitialAd(activity, action)
}

fun Fragment.loadRewardedAd() {
    Adeas.load(AdType.REWARDED, requireContext())
}

fun Fragment.loadInterstitialAd() {
    Adeas.load(AdType.INTERSTITIAL, requireContext())
}

fun Fragment.loadBannerAd() {
    Adeas.load(AdType.BANNER, requireContext())
}

fun Fragment.loadAds(adType: AdType) {
    Adeas.load(adType, requireContext())
}

fun Fragment.loadAllAds() {
    Adeas.loadAll(requireContext())
}

fun Fragment.showRewardedAd(action: (result: Boolean) -> Unit) {
    Adeas.showRewardedAd(requireActivity(), action)
}

fun Fragment.showInterstitialAd(action: (result: Boolean) -> Unit) {
    Adeas.showInterstitialAd(requireActivity(), action)
}

fun onClosedRewarded(action: () -> Unit){
    Adeas.onClosedRewarded(action)
}
