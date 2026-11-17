package com.voidtouch.launcher.data

import com.voidtouch.launcher.R

/**
 * Logotipos próprios (vetores brancos) dos apps mais comuns, achados pelo nome do pacote.
 * Gerado a partir do Simple Icons (CC0); as marcas pertencem aos seus donos.
 */
object BrandIcons {

    private val byPackage: Map<String, Int> = mapOf(
        "com.whatsapp" to R.drawable.ic_brand_whatsapp,
        "com.whatsapp.w4b" to R.drawable.ic_brand_whatsapp,
        "com.instagram.android" to R.drawable.ic_brand_instagram,
        "com.instagram.lite" to R.drawable.ic_brand_instagram,
        "com.instagram.barcelona" to R.drawable.ic_brand_threads,
        "com.zhiliaoapp.musically" to R.drawable.ic_brand_tiktok,
        "com.ss.android.ugc.trill" to R.drawable.ic_brand_tiktok,
        "com.zhiliaoapp.musically.go" to R.drawable.ic_brand_tiktok,
        "com.google.android.youtube" to R.drawable.ic_brand_youtube,
        "com.google.android.apps.youtube.music" to R.drawable.ic_brand_youtubemusic,
        "com.spotify.music" to R.drawable.ic_brand_spotify,
        "com.netflix.mediaclient" to R.drawable.ic_brand_netflix,
        "com.nu.production" to R.drawable.ic_brand_nubank,
        "com.mercadopago.wallet" to R.drawable.ic_brand_mercadopago,
        "br.com.brainweb.ifood" to R.drawable.ic_brand_ifood,
        "com.ubercab" to R.drawable.ic_brand_uber,
        "org.telegram.messenger" to R.drawable.ic_brand_telegram,
        "org.telegram.messenger.web" to R.drawable.ic_brand_telegram,
        "com.discord" to R.drawable.ic_brand_discord,
        "org.thoughtcrime.securesms" to R.drawable.ic_brand_signal,
        "com.google.android.apps.photos" to R.drawable.ic_brand_googlephotos,
        "com.android.chrome" to R.drawable.ic_brand_googlechrome,
        "com.google.android.googlequicksearchbox" to R.drawable.ic_brand_google,
        "com.google.android.apps.maps" to R.drawable.ic_brand_googlemaps,
        "com.google.android.gm" to R.drawable.ic_brand_gmail,
        "com.google.android.apps.docs" to R.drawable.ic_brand_googledrive,
        "com.google.android.calendar" to R.drawable.ic_brand_googlecalendar,
        "com.google.android.apps.meetings" to R.drawable.ic_brand_googlemeet,
        "com.android.vending" to R.drawable.ic_brand_googleplay,
        "com.google.android.apps.bard" to R.drawable.ic_brand_googlegemini,
        "com.pinterest" to R.drawable.ic_brand_pinterest,
        "com.twitter.android" to R.drawable.ic_brand_x,
        "com.facebook.katana" to R.drawable.ic_brand_facebook,
        "com.facebook.lite" to R.drawable.ic_brand_facebook,
        "com.facebook.orca" to R.drawable.ic_brand_messenger,
        "com.facebook.mlite" to R.drawable.ic_brand_messenger,
        "com.reddit.frontpage" to R.drawable.ic_brand_reddit,
        "tv.twitch.android.app" to R.drawable.ic_brand_twitch,
        "notion.id" to R.drawable.ic_brand_notion,
        "com.github.android" to R.drawable.ic_brand_github,
        "com.anthropic.claude" to R.drawable.ic_brand_claude,
        "com.shopee.br" to R.drawable.ic_brand_shopee,
        "com.picpay" to R.drawable.ic_brand_picpay,
        "com.waze" to R.drawable.ic_brand_waze,
        "com.valvesoftware.android.steam.community" to R.drawable.ic_brand_steam,
        "com.duolingo" to R.drawable.ic_brand_duolingo,
        "org.mozilla.firefox" to R.drawable.ic_brand_firefox,
        "com.brave.browser" to R.drawable.ic_brand_brave,
        "org.videolan.vlc" to R.drawable.ic_brand_vlcmediaplayer,
        "com.paypal.android.p2pmobile" to R.drawable.ic_brand_paypal,
    )

    /** Recurso do logotipo do app, ou nulo se não houver um. */
    fun resFor(packageName: String): Int? = byPackage[packageName]
}
