package com.example.util

import java.util.regex.Pattern

object UrlUtils {

    // Regex to extract 11-char YouTube Video ID from any youtube link
    // e.g. youtube.com/watch?v=xxxx, youtu.be/xxxx, youtube.com/embed/xxxx, youtube.com/shorts/xxxx
    private val YOUTUBE_PATTERN = Pattern.compile(
        "^.*(?:(?:youtu\\.be\\/|v\\/|vi\\/|u\\/\\w\\/|embed\\/|shorts\\/)|(?:(?:watch)?\\?v(?:i)?=|\\&v(?:i)?=))([^#\\&\\?]*).*",
        Pattern.CASE_INSENSITIVE
    )

    fun extractYouTubeId(url: String): String? {
        val trimmed = url.trim()
        val matcher = YOUTUBE_PATTERN.matcher(trimmed)
        return if (matcher.matches()) {
            val id = matcher.group(1)
            if (id != null && id.length == 11) id else null
        } else {
            null
        }
    }

    fun isYouTubeUrl(url: String): Boolean {
        return extractYouTubeId(url) != null
    }

    // Blogger / Website regex
    private val BLOGGER_PATTERN = Pattern.compile(
        "^(https?:\\/\\/)?([a-zA-Z0-9-]+\\.)*(blogspot\\.com|[a-zA-Z0-9-]+\\.[a-zA-Z]{2,})(:[0-9]+)?(\\/.*)?$",
        Pattern.CASE_INSENSITIVE
    )

    fun isValidWebOrBloggerUrl(url: String): Boolean {
        val trimmed = url.trim()
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            return false
        }
        return BLOGGER_PATTERN.matcher(trimmed).matches()
    }

    fun formatHttpsUrl(url: String): String {
        val trimmed = url.trim()
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    fun buildYouTubeEmbedHtml(videoId: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    html, body { width: 100%; height: 100%; background-color: #000; overflow: hidden; display: flex; align-items: center; justify-content: center; }
                    .video-wrapper { position: relative; width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; }
                    iframe { width: 100%; height: 100%; border: 0; }
                </style>
            </head>
            <body>
                <div class="video-wrapper">
                    <iframe 
                        id="ytplayer"
                        type="text/html"
                        src="https://www.youtube-nocookie.com/embed/$videoId?autoplay=1&playsinline=1&enablejsapi=1&rel=0&modestbranding=1&fs=1&origin=https://www.youtube-nocookie.com" 
                        frameborder="0" 
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" 
                        allowfullscreen>
                    </iframe>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
