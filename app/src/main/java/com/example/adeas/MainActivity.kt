package com.example.adeas

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ckgin.adeas.Adeas
import com.ckgin.adeas.BannerView
import com.ckgin.adeas.initializeAdmob
import com.ckgin.adeas.loadAllAds
import com.ckgin.adeas.showInterstitialAd
import com.ckgin.adeas.showRewardedAd
import com.naulian.anhance.showToast

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeAdmob(this, debug = true)

        loadAllAds{
            Log.d("MainActivity", "onCreate: Rewarded Ad Loaded")
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        onShowInterstitial = {
                            showInterstitialAd {
                                if (!it) showToast("Ad is not ready")
                            }
                        },
                        onShowRewarded = {
                            showRewardedAd {
                                if (!it) showToast("Ad is not ready")
                            }
                        },
                        onToggleAds = { isChecked ->
                            if (isChecked) Adeas.enableAds(this)
                            else Adeas.disableAds(this)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    onShowInterstitial: () -> Unit,
    onShowRewarded: () -> Unit,
    onToggleAds: (Boolean) -> Unit
) {
    val isAdsEnabled by Adeas.state.collectAsState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isAdsEnabled) {
                BannerView(modifier = Modifier.padding(16.dp))
            }

            Button(onClick = onShowInterstitial) {
                Text("Show Interstitial")
            }

            Button(onClick = onShowRewarded) {
                Text("Show Rewarded")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Enable Ads", modifier = Modifier.padding(end = 8.dp))
                Switch(
                    checked = isAdsEnabled,
                    onCheckedChange = onToggleAds
                )
            }
        }
    }
}
