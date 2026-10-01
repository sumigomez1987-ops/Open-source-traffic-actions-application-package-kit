package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.Strings
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldCoin
import com.example.ui.viewmodel.AppViewModel
import com.example.util.UrlUtils

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun JobVisitViewer(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val job by viewModel.activeVisitingJob.collectAsState()
    val secondsRemaining by viewModel.timerSeconds.collectAsState()
    val canClaim by viewModel.canClaimReward.collectAsState()
    val cheatDetected by viewModel.cheatDetected.collectAsState()

    var pageLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Intercept back button to warn/cancel
    BackHandler {
        viewModel.cancelVisitingJob()
    }

    if (job == null) return

    val currentJob = job!!
    val isYouTube = currentJob.category == "youtube" || currentJob.youtubeVideoId.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Timer & Header Bar
        Surface(
            tonalElevation = 6.dp,
            shadowElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Close / Exit button
                    IconButton(
                        onClick = { viewModel.cancelVisitingJob() },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("close_visit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // 2-Minute (120s) Countdown Badge
                    val minutes = secondsRemaining / 60
                    val secs = secondsRemaining % 60
                    val timeFormatted = String.format("%02d:%02d", minutes, secs)

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when {
                            canClaim -> EmeraldSuccess.copy(alpha = 0.2f)
                            cheatDetected -> DangerRed.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (canClaim) Icons.Default.CheckCircle else Icons.Default.Timer,
                                contentDescription = "Timer",
                                tint = when {
                                    canClaim -> EmeraldSuccess
                                    cheatDetected -> DangerRed
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    canClaim -> Strings.get("claim_reward", language)
                                    cheatDetected -> if (language == AppLanguage.BANGLA) "চিটিং সনাক্ত হয়েছে!" else "Cheating Detected!"
                                    else -> "${Strings.get("timer_waiting", language)}$timeFormatted"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    canClaim -> EmeraldSuccess
                                    cheatDetected -> DangerRed
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                        }
                    }

                    // Reward Indicator
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GoldCoin.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🪙", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${currentJob.rewardCoins}",
                                fontWeight = FontWeight.Bold,
                                color = GoldCoin,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                // Linear Progress Indicator
                Spacer(modifier = Modifier.height(6.dp))
                val progress = if (canClaim) 1f else (60 - secondsRemaining) / 60f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (canClaim) EmeraldSuccess else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // Cheat detected warning banner
        AnimatedVisibility(visible = cheatDetected) {
            Surface(
                color = DangerRed,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Cheat warning",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (language == AppLanguage.BANGLA)
                            "অ্যান্টি-চিট সক্রিয়: আপনি অন্য অ্যাপে চলে গিয়েছিলেন! কাউন্টডাউন বাতিল করা হয়েছে।"
                        else
                            "Anti-cheat alert: App was paused or minimized! Countdown invalidated.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Anti-cheat policy reminder
        if (!cheatDetected && !canClaim) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = Strings.get("cheat_warning", language),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 12.dp),
                    fontSize = 11.sp
                )
            }
        }

        // Main Web / Video Player Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            javaScriptCanOpenWindowsAutomatically = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            builtInZoomControls = true
                            displayZoomControls = false
                            mediaPlaybackRequiresUserGesture = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                        }

                        val cookieManager = android.webkit.CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        val vid = if (isYouTube) {
                            currentJob.youtubeVideoId.ifEmpty {
                                UrlUtils.extractYouTubeId(currentJob.url) ?: "bMknfKXIFA8"
                            }
                        } else ""

                        fun applyYouTubeIsolation(v: WebView?) {
                            if (!isYouTube || v == null) return
                            val js = """
                                (function() {
                                    var css = 'ytm-item-section-renderer, ytm-comment-section-renderer, ytm-pivot-bar-renderer, ytm-app-header-renderer, #related, #comments, .related-items-container, .carousel, ytm-reel-shelf-renderer, ytm-video-description-header-renderer, ytm-single-column-watch-next-results-renderer > *:not(:first-child), ytm-watch > *:not(#player-container-id):not(.player-container):not(#player) { display: none !important; visibility: hidden !important; height: 0 !important; max-height: 0 !important; overflow: hidden !important; } html, body, ytm-app, ytm-watch { background-color: #000000 !important; overflow: hidden !important; } #player-container-id, .player-container, #player { position: absolute !important; top: 50% !important; left: 0 !important; transform: translateY(-50%) !important; width: 100vw !important; max-height: 100vh !important; z-index: 999999 !important; }';
                                    var existing = document.getElementById('yt-isolation-style');
                                    if (!existing) {
                                        var s = document.createElement('style');
                                        s.id = 'yt-isolation-style';
                                        s.type = 'text/css';
                                        s.appendChild(document.createTextNode(css));
                                        (document.head || document.documentElement).appendChild(s);
                                    }
                                })();
                            """.trimIndent()
                            v.evaluateJavascript(js, null)
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                pageLoading = true
                                if (isYouTube) {
                                    applyYouTubeIsolation(view)
                                }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                pageLoading = false
                                if (isYouTube) {
                                    applyYouTubeIsolation(view)
                                    view?.postDelayed({ applyYouTubeIsolation(view) }, 1500)
                                }
                            }

                            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                pageLoading = false
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val urlStr = request?.url?.toString() ?: return false
                                if (isYouTube && vid.isNotEmpty()) {
                                    val clickedVid = UrlUtils.extractYouTubeId(urlStr)
                                    if (clickedVid != null && clickedVid != vid) {
                                        // Block navigation to other videos so only creator's target video receives watch time!
                                        return true
                                    }
                                }
                                return if (urlStr.startsWith("http://") || urlStr.startsWith("https://")) {
                                    false
                                } else {
                                    try {
                                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlStr)))
                                    } catch (_: Exception) {}
                                    true
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                if (isYouTube && newProgress > 30) {
                                    applyYouTubeIsolation(view)
                                }
                                if (newProgress > 70) {
                                    pageLoading = false
                                }
                            }
                        }

                        webViewRef = this

                        // Load either YouTube directly or website URL
                        if (isYouTube) {
                            loadUrl("https://m.youtube.com/watch?v=$vid")
                        } else {
                            loadUrl(UrlUtils.formatHttpsUrl(currentJob.url))
                        }
                    }
                },
                update = { webView ->
                    // Keep view active
                },
                modifier = Modifier.fillMaxSize()
            )

            // Loading indicator
            if (pageLoading && !isYouTube) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            // If YouTube, convenient "Open in YouTube App" button
            if (isYouTube) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xDD000000),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(14.dp)
                        .clickable {
                            try {
                                val vid = currentJob.youtubeVideoId.ifEmpty {
                                    UrlUtils.extractYouTubeId(currentJob.url) ?: "bMknfKXIFA8"
                                }
                                val ytIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$vid"))
                                context.startActivity(ytIntent)
                            } catch (_: Exception) {}
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "YouTube App",
                            tint = Color(0xFFFF0000),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.BANGLA) "ইউটিউব অ্যাপে দেখুন" else "Watch in YouTube App",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Bottom Claim Reward Bar when 60 seconds completed
        AnimatedVisibility(visible = canClaim) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { viewModel.claimJobReward() },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("claim_job_reward_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Strings.get("claim_reward", language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
