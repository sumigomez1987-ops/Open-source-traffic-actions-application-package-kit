package com.example.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val nameChanged: Boolean = false,
    val coins: Long = 0,
    val referralCode: String = "",
    val referredBy: String = "",
    val isBlocked: Boolean = false,
    val completedJobIds: List<String> = emptyList(),
    val totalReferrals: Int = 0,
    val referralEarnings: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class RemoteJob(
    val id: String = "",
    val creatorId: String = "",
    val creatorEmail: String = "",
    val title: String = "",
    val url: String = "",
    val category: String = "website_blogger", // "website_blogger", "youtube"
    val thumbnail: String = "",
    val targetVisitors: Int = 10,
    val completedCount: Int = 0,
    val rewardCoins: Int = 10,
    val youtubeVideoId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class DepositRequest(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val method: String = "bkash", // "bkash", "nagad"
    val amountBdt: Int = 100,
    val coins: Int = 1000,
    val trxId: String = "",
    val senderNumber: String = "",
    val status: String = "pending", // "pending", "approved", "rejected"
    val timestamp: Long = System.currentTimeMillis()
)

data class WithdrawalRequest(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val method: String = "bkash",
    val amountBdt: Int = 100,
    val coins: Int = 2000,
    val receiverNumber: String = "",
    val status: String = "pending",
    val timestamp: Long = System.currentTimeMillis()
)

data class AppSettings(
    val adminEmail: String = "sharbongomes2003@gmail.com",
    val aboutNoticeBn: String = "সম্পূর্ণ এপ্লিকেশন টি কাজ করছে ওয়েবসাইট/ব্লগার এবং adsterra বিজ্ঞাপন থেকে রেভিনিউ অর্জন করার জন্য। আসুন একে অপরের সাথে একসাথে মিলে কাজ করি সামনের দিকে এগিয়ে যাই। এছাড়াও আপনার ওয়েবসাইট/ব্লগার এবং ইউটিউব ভিডিও ওয়াচ টাইম ও ভিউ প্রমোশন চালানোর জন্য অ্যাপ্লিকেশনটি খুবই কার্যকর।",
    val aboutNoticeEn: String = "GlobalSEO Engine is designed for website/blogger traffic monetization and Adsterra revenue growth. Let's work together to grow our audience. This app is highly effective for promoting your website/blogger visitors and YouTube watch time and views.",
    val developerName: String = "shrabonofficial",
    val facebookUrl: String = "https://www.facebook.com/profile.php?id=61592642930215",
    val whatsappNumber: String = "+880 1627096941",
    val sendMoneyNumber: String = "01627096941",
    val firebaseApiKey: String = "AIzaSyCNgg8rIxvXriAyBtuqmCIjFnEJ_xRTsjA",
    val firebaseProjectId: String = "globalseo-engine",
    val firebaseAppId: String = "1:290843368838:web:13b67a3a5658312bad6007",
    val firebaseAuthDomain: String = "globalseo-engine.firebaseapp.com"
) {
    fun getNotice(isBangla: Boolean): String {
        return if (isBangla) {
            aboutNoticeBn.ifBlank { "সম্পূর্ণ এপ্লিকেশন টি কাজ করছে ওয়েবসাইট/ব্লগার এবং adsterra বিজ্ঞাপন থেকে রেভিনিউ অর্জন করার জন্য। আসুন একে অপরের সাথে একসাথে মিলে কাজ করি সামনের দিকে এগিয়ে যাই। এছাড়াও আপনার ওয়েবসাইট/ব্লগার এবং ইউটিউব ভিডিও ওয়াচ টাইম ও ভিউ প্রমোশন চালানোর জন্য অ্যাপ্লিকেশনটি খুবই কার্যকর।" }
        } else {
            aboutNoticeEn.ifBlank { "GlobalSEO Engine is designed for website/blogger traffic monetization and Adsterra revenue growth. Let's work together to grow our audience. This app is highly effective for promoting your website/blogger visitors and YouTube watch time and views." }
        }
    }
}
