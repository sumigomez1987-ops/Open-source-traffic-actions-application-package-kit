package com.example.data.firebase

import android.content.Context
import com.example.data.model.AppSettings
import com.example.data.model.DepositRequest
import com.example.data.model.RemoteJob
import com.example.data.model.User
import com.example.data.model.WithdrawalRequest
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseManager(private val context: Context) {

    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    fun initialize(settings: AppSettings) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey(settings.firebaseApiKey)
                    .setApplicationId(settings.firebaseAppId)
                    .setProjectId(settings.firebaseProjectId)
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isInitialized(): Boolean = auth != null && firestore != null

    suspend fun signIn(email: String, pass: String): Result<String> {
        return try {
            val a = auth ?: throw IllegalStateException("Firebase Auth not initialized")
            val authResult = a.signInWithEmailAndPassword(email.trim(), pass).await()
            val uid = authResult.user?.uid ?: throw IllegalStateException("UID not found")
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, pass: String): Result<String> {
        return try {
            val a = auth ?: throw IllegalStateException("Firebase Auth not initialized")
            val authResult = a.createUserWithEmailAndPassword(email.trim(), pass).await()
            val uid = authResult.user?.uid ?: throw IllegalStateException("UID not found")
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            val a = auth ?: throw IllegalStateException("Firebase Auth not initialized")
            a.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (_: Exception) {}
    }

    suspend fun fetchUser(uid: String): User? {
        return try {
            val db = firestore ?: return null
            val doc = db.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val completedList = (doc.get("completedJobIds") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                User(
                    uid = uid,
                    email = doc.getString("email") ?: "",
                    displayName = doc.getString("displayName") ?: "",
                    nameChanged = doc.getBoolean("nameChanged") ?: false,
                    coins = doc.getLong("coins") ?: 0L,
                    referralCode = doc.getString("referralCode") ?: "",
                    referredBy = doc.getString("referredBy") ?: "",
                    isBlocked = doc.getBoolean("isBlocked") ?: false,
                    completedJobIds = completedList,
                    totalReferrals = doc.getLong("totalReferrals")?.toInt() ?: 0,
                    referralEarnings = doc.getLong("referralEarnings") ?: 0L,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveUser(user: User): Boolean {
        return try {
            val db = firestore ?: return false
            val map = hashMapOf(
                "uid" to user.uid,
                "email" to user.email,
                "displayName" to user.displayName,
                "nameChanged" to user.nameChanged,
                "coins" to user.coins,
                "referralCode" to user.referralCode,
                "referredBy" to user.referredBy,
                "isBlocked" to user.isBlocked,
                "completedJobIds" to user.completedJobIds,
                "totalReferrals" to user.totalReferrals,
                "referralEarnings" to user.referralEarnings,
                "createdAt" to user.createdAt
            )
            db.collection("users").document(user.uid).set(map, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun checkDuplicateTrxId(trxId: String): Boolean {
        return try {
            val db = firestore ?: return false
            val query = db.collection("deposits")
                .whereEqualTo("trxId", trxId.trim())
                .limit(1)
                .get()
                .await()
            !query.isEmpty
        } catch (_: Exception) {
            false
        }
    }

    suspend fun submitDeposit(request: DepositRequest): Boolean {
        return try {
            val db = firestore ?: return false
            val map = hashMapOf(
                "id" to request.id,
                "userId" to request.userId,
                "userEmail" to request.userEmail,
                "method" to request.method,
                "amountBdt" to request.amountBdt,
                "coins" to request.coins,
                "trxId" to request.trxId,
                "senderNumber" to request.senderNumber,
                "status" to request.status,
                "timestamp" to request.timestamp
            )
            db.collection("deposits").document(request.id).set(map).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun submitWithdrawal(request: WithdrawalRequest): Boolean {
        return try {
            val db = firestore ?: return false
            val map = hashMapOf(
                "id" to request.id,
                "userId" to request.userId,
                "userEmail" to request.userEmail,
                "method" to request.method,
                "amountBdt" to request.amountBdt,
                "coins" to request.coins,
                "receiverNumber" to request.receiverNumber,
                "status" to request.status,
                "timestamp" to request.timestamp
            )
            db.collection("withdrawals").document(request.id).set(map).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchRemoteJobs(): List<RemoteJob> {
        return try {
            val db = firestore ?: return emptyList()
            val snap = db.collection("remote_jobs").get().await()
            snap.documents.mapNotNull { doc ->
                RemoteJob(
                    id = doc.getString("id") ?: doc.id,
                    creatorId = doc.getString("creatorId") ?: "",
                    creatorEmail = doc.getString("creatorEmail") ?: "",
                    title = doc.getString("title") ?: "",
                    url = doc.getString("url") ?: "",
                    category = doc.getString("category") ?: "blogger",
                    thumbnail = doc.getString("thumbnail") ?: "",
                    targetVisitors = doc.getLong("targetVisitors")?.toInt() ?: 10,
                    completedCount = doc.getLong("completedCount")?.toInt() ?: 0,
                    rewardCoins = doc.getLong("rewardCoins")?.toInt() ?: 10,
                    youtubeVideoId = doc.getString("youtubeVideoId") ?: "",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveRemoteJob(job: RemoteJob): Boolean {
        return try {
            val db = firestore ?: return false
            val map = hashMapOf(
                "id" to job.id,
                "creatorId" to job.creatorId,
                "creatorEmail" to job.creatorEmail,
                "title" to job.title,
                "url" to job.url,
                "category" to job.category,
                "thumbnail" to job.thumbnail,
                "targetVisitors" to job.targetVisitors,
                "completedCount" to job.completedCount,
                "rewardCoins" to job.rewardCoins,
                "youtubeVideoId" to job.youtubeVideoId,
                "createdAt" to job.createdAt
            )
            db.collection("remote_jobs").document(job.id).set(map).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteRemoteJob(jobId: String): Boolean {
        return try {
            val db = firestore ?: return false
            db.collection("remote_jobs").document(jobId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun initializeCollections(defaultJobs: List<RemoteJob>): Boolean {
        return try {
            val db = firestore ?: return false
            // Seed settings
            val settingsMap = hashMapOf(
                "adminEmail" to "sharbongomes2003@gmail.com",
                "initializedAt" to System.currentTimeMillis()
            )
            db.collection("settings").document("global_config").set(settingsMap, SetOptions.merge()).await()

            // Seed default remote jobs if empty
            val existing = db.collection("remote_jobs").limit(1).get().await()
            if (existing.isEmpty) {
                for (j in defaultJobs) {
                    saveRemoteJob(j)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
