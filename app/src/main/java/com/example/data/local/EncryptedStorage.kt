package com.example.data.local

import android.content.Context
import com.example.data.model.AppSettings
import com.example.data.model.DepositRequest
import com.example.data.model.RemoteJob
import com.example.data.model.User
import com.example.data.model.WithdrawalRequest
import com.example.data.security.CryptoManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class EncryptedStorage(private val context: Context) {

    private fun getStorageFile(): File {
        // App-specific external files dir: /Android/data/<package_name>/files/
        val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
        return File(baseDir, "globalseo_secure_cache.dat")
    }

    @Synchronized
    fun saveAll(
        currentUser: User?,
        jobs: List<RemoteJob>,
        deposits: List<DepositRequest>,
        withdrawals: List<WithdrawalRequest>,
        settings: AppSettings,
        language: String
    ) {
        try {
            val root = JSONObject()
            root.put("language", language)
            root.put("lastSyncTime", System.currentTimeMillis())

            // User
            if (currentUser != null) {
                val userObj = JSONObject().apply {
                    put("uid", currentUser.uid)
                    put("email", currentUser.email)
                    put("displayName", currentUser.displayName)
                    put("nameChanged", currentUser.nameChanged)
                    put("coins", currentUser.coins)
                    put("referralCode", currentUser.referralCode)
                    put("referredBy", currentUser.referredBy)
                    put("isBlocked", currentUser.isBlocked)
                    put("totalReferrals", currentUser.totalReferrals)
                    put("referralEarnings", currentUser.referralEarnings)
                    put("createdAt", currentUser.createdAt)

                    val completedArr = JSONArray()
                    currentUser.completedJobIds.forEach { completedArr.put(it) }
                    put("completedJobIds", completedArr)
                }
                root.put("user", userObj)
            }

            // Settings
            val settingsObj = JSONObject().apply {
                put("adminEmail", settings.adminEmail)
                put("aboutNoticeBn", settings.aboutNoticeBn)
                put("aboutNoticeEn", settings.aboutNoticeEn)
                put("developerName", settings.developerName)
                put("facebookUrl", settings.facebookUrl)
                put("whatsappNumber", settings.whatsappNumber)
                put("sendMoneyNumber", settings.sendMoneyNumber)
                put("firebaseApiKey", settings.firebaseApiKey)
                put("firebaseProjectId", settings.firebaseProjectId)
                put("firebaseAppId", settings.firebaseAppId)
                put("firebaseAuthDomain", settings.firebaseAuthDomain)
            }
            root.put("settings", settingsObj)

            // Jobs
            val jobsArr = JSONArray()
            for (j in jobs) {
                val obj = JSONObject().apply {
                    put("id", j.id)
                    put("creatorId", j.creatorId)
                    put("creatorEmail", j.creatorEmail)
                    put("title", j.title)
                    put("url", j.url)
                    put("category", j.category)
                    put("thumbnail", j.thumbnail)
                    put("targetVisitors", j.targetVisitors)
                    put("completedCount", j.completedCount)
                    put("rewardCoins", j.rewardCoins)
                    put("youtubeVideoId", j.youtubeVideoId)
                    put("createdAt", j.createdAt)
                }
                jobsArr.put(obj)
            }
            root.put("jobs", jobsArr)

            // Deposits
            val depArr = JSONArray()
            for (d in deposits) {
                val obj = JSONObject().apply {
                    put("id", d.id)
                    put("userId", d.userId)
                    put("userEmail", d.userEmail)
                    put("method", d.method)
                    put("amountBdt", d.amountBdt)
                    put("coins", d.coins)
                    put("trxId", d.trxId)
                    put("senderNumber", d.senderNumber)
                    put("status", d.status)
                    put("timestamp", d.timestamp)
                }
                depArr.put(obj)
            }
            root.put("deposits", depArr)

            // Withdrawals
            val withArr = JSONArray()
            for (w in withdrawals) {
                val obj = JSONObject().apply {
                    put("id", w.id)
                    put("userId", w.userId)
                    put("userEmail", w.userEmail)
                    put("method", w.method)
                    put("amountBdt", w.amountBdt)
                    put("coins", w.coins)
                    put("receiverNumber", w.receiverNumber)
                    put("status", w.status)
                    put("timestamp", w.timestamp)
                }
                withArr.put(obj)
            }
            root.put("withdrawals", withArr)

            // Encrypt and write
            val jsonString = root.toString()
            val encrypted = CryptoManager.encrypt(jsonString)
            getStorageFile().writeText(encrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    data class LoadedData(
        val user: User?,
        val jobs: List<RemoteJob>,
        val deposits: List<DepositRequest>,
        val withdrawals: List<WithdrawalRequest>,
        val settings: AppSettings,
        val language: String,
        val lastSyncTime: Long
    )

    @Synchronized
    fun loadAll(): LoadedData {
        val file = getStorageFile()
        if (!file.exists()) {
            return LoadedData(null, emptyList(), emptyList(), emptyList(), AppSettings(), "bn", 0L)
        }
        return try {
            val encrypted = file.readText(Charsets.UTF_8)
            val jsonString = CryptoManager.decrypt(encrypted)
            if (jsonString.isEmpty()) {
                return LoadedData(null, emptyList(), emptyList(), emptyList(), AppSettings(), "bn", 0L)
            }
            val root = JSONObject(jsonString)
            val language = root.optString("language", "bn")
            val lastSyncTime = root.optLong("lastSyncTime", 0L)

            // User
            var user: User? = null
            if (root.has("user")) {
                val u = root.getJSONObject("user")
                val completedList = mutableListOf<String>()
                val compArr = u.optJSONArray("completedJobIds")
                if (compArr != null) {
                    for (i in 0 until compArr.length()) {
                        completedList.add(compArr.getString(i))
                    }
                }
                user = User(
                    uid = u.optString("uid"),
                    email = u.optString("email"),
                    displayName = u.optString("displayName"),
                    nameChanged = u.optBoolean("nameChanged", false),
                    coins = u.optLong("coins", 0),
                    referralCode = u.optString("referralCode"),
                    referredBy = u.optString("referredBy"),
                    isBlocked = u.optBoolean("isBlocked", false),
                    completedJobIds = completedList,
                    totalReferrals = u.optInt("totalReferrals", 0),
                    referralEarnings = u.optLong("referralEarnings", 0),
                    createdAt = u.optLong("createdAt", System.currentTimeMillis())
                )
            }

            // Settings
            var settings = AppSettings()
            if (root.has("settings")) {
                val s = root.getJSONObject("settings")
                settings = AppSettings(
                    adminEmail = s.optString("adminEmail", settings.adminEmail),
                    aboutNoticeBn = s.optString("aboutNoticeBn", s.optString("aboutNotice", settings.aboutNoticeBn)),
                    aboutNoticeEn = s.optString("aboutNoticeEn", settings.aboutNoticeEn),
                    developerName = s.optString("developerName", settings.developerName),
                    facebookUrl = s.optString("facebookUrl", settings.facebookUrl),
                    whatsappNumber = s.optString("whatsappNumber", settings.whatsappNumber),
                    sendMoneyNumber = s.optString("sendMoneyNumber", settings.sendMoneyNumber),
                    firebaseApiKey = s.optString("firebaseApiKey", settings.firebaseApiKey),
                    firebaseProjectId = s.optString("firebaseProjectId", settings.firebaseProjectId),
                    firebaseAppId = s.optString("firebaseAppId", settings.firebaseAppId),
                    firebaseAuthDomain = s.optString("firebaseAuthDomain", settings.firebaseAuthDomain)
                )
            }

            // Jobs
            val jobsList = mutableListOf<RemoteJob>()
            val jobsArr = root.optJSONArray("jobs")
            if (jobsArr != null) {
                for (i in 0 until jobsArr.length()) {
                    val j = jobsArr.getJSONObject(i)
                    jobsList.add(
                        RemoteJob(
                            id = j.optString("id"),
                            creatorId = j.optString("creatorId"),
                            creatorEmail = j.optString("creatorEmail"),
                            title = j.optString("title"),
                            url = j.optString("url"),
                            category = j.optString("category", "blogger"),
                            thumbnail = j.optString("thumbnail"),
                            targetVisitors = j.optInt("targetVisitors", 10),
                            completedCount = j.optInt("completedCount", 0),
                            rewardCoins = j.optInt("rewardCoins", 10),
                            youtubeVideoId = j.optString("youtubeVideoId"),
                            createdAt = j.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            // Deposits
            val depList = mutableListOf<DepositRequest>()
            val depArr = root.optJSONArray("deposits")
            if (depArr != null) {
                for (i in 0 until depArr.length()) {
                    val d = depArr.getJSONObject(i)
                    depList.add(
                        DepositRequest(
                            id = d.optString("id"),
                            userId = d.optString("userId"),
                            userEmail = d.optString("userEmail"),
                            method = d.optString("method"),
                            amountBdt = d.optInt("amountBdt"),
                            coins = d.optInt("coins"),
                            trxId = d.optString("trxId"),
                            senderNumber = d.optString("senderNumber"),
                            status = d.optString("status"),
                            timestamp = d.optLong("timestamp")
                        )
                    )
                }
            }

            // Withdrawals
            val withList = mutableListOf<WithdrawalRequest>()
            val withArr = root.optJSONArray("withdrawals")
            if (withArr != null) {
                for (i in 0 until withArr.length()) {
                    val w = withArr.getJSONObject(i)
                    withList.add(
                        WithdrawalRequest(
                            id = w.optString("id"),
                            userId = w.optString("userId"),
                            userEmail = w.optString("userEmail"),
                            method = w.optString("method"),
                            amountBdt = w.optInt("amountBdt"),
                            coins = w.optInt("coins"),
                            receiverNumber = w.optString("receiverNumber"),
                            status = w.optString("status"),
                            timestamp = w.optLong("timestamp")
                        )
                    )
                }
            }

            LoadedData(user, jobsList, depList, withList, settings, language, lastSyncTime)
        } catch (e: Exception) {
            e.printStackTrace()
            LoadedData(null, emptyList(), emptyList(), emptyList(), AppSettings(), "bn", 0L)
        }
    }
}
