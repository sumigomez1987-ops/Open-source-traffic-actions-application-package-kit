package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirebaseManager
import com.example.data.local.EncryptedStorage
import com.example.data.model.AppSettings
import com.example.data.model.DepositRequest
import com.example.data.model.RemoteJob
import com.example.data.model.User
import com.example.data.model.WithdrawalRequest
import com.example.ui.i18n.AppLanguage
import com.example.ui.sound.SoundHelper
import com.example.util.UrlUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class Screen {
    AUTH,
    DASHBOARD,
    REMOTE_JOB,
    DEPOSIT,
    WITHDRAWAL,
    PROFILE,
    ABOUT,
    REFERRAL,
    ADMIN,
    VISIT_JOB
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val encryptedStorage = EncryptedStorage(context)
    private val firebaseManager = FirebaseManager(context)

    // Language
    private val _language = MutableStateFlow(AppLanguage.BANGLA)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // Navigation
    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // User State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Settings
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    // Jobs
    private val _remoteJobs = MutableStateFlow<List<RemoteJob>>(emptyList())
    val remoteJobs: StateFlow<List<RemoteJob>> = _remoteJobs.asStateFlow()

    // Deposits & Withdrawals
    private val _userDeposits = MutableStateFlow<List<DepositRequest>>(emptyList())
    val userDeposits: StateFlow<List<DepositRequest>> = _userDeposits.asStateFlow()

    private val _userWithdrawals = MutableStateFlow<List<WithdrawalRequest>>(emptyList())
    val userWithdrawals: StateFlow<List<WithdrawalRequest>> = _userWithdrawals.asStateFlow()

    // Admin lists
    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    private val _allDeposits = MutableStateFlow<List<DepositRequest>>(emptyList())
    val allDeposits: StateFlow<List<DepositRequest>> = _allDeposits.asStateFlow()

    private val _allWithdrawals = MutableStateFlow<List<WithdrawalRequest>>(emptyList())
    val allWithdrawals: StateFlow<List<WithdrawalRequest>> = _allWithdrawals.asStateFlow()

    // Active Visit & Anti-Cheat State
    private val _activeVisitingJob = MutableStateFlow<RemoteJob?>(null)
    val activeVisitingJob: StateFlow<RemoteJob?> = _activeVisitingJob.asStateFlow()

    private val _timerSeconds = MutableStateFlow(60)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _cheatDetected = MutableStateFlow(false)
    val cheatDetected: StateFlow<Boolean> = _cheatDetected.asStateFlow()

    private val _canClaimReward = MutableStateFlow(false)
    val canClaimReward: StateFlow<Boolean> = _canClaimReward.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadLocalData()
        firebaseManager.initialize(_settings.value)
    }

    private fun loadLocalData() {
        val loaded = encryptedStorage.loadAll()
        if (loaded.language == "en") {
            _language.value = AppLanguage.ENGLISH
        } else {
            _language.value = AppLanguage.BANGLA
        }

        _settings.value = loaded.settings
        _currentUser.value = loaded.user
        _userDeposits.value = loaded.deposits
        _userWithdrawals.value = loaded.withdrawals

        if (loaded.jobs.isNotEmpty()) {
            _remoteJobs.value = loaded.jobs
        } else {
            // Seed initial sample jobs for Blogger and YouTube
            val defaultJobs = listOf(
                RemoteJob(
                    id = "job-blogger-1",
                    creatorId = "system",
                    creatorEmail = "admin@globalseo.com",
                    title = "Tech Blog - SEO Optimization Tips 2026",
                    url = "https://globalseo-tips.blogspot.com",
                    category = "website_blogger",
                    thumbnail = "https://images.unsplash.com/photo-1432821596592-e2c18b78144f?w=400",
                    targetVisitors = 100,
                    completedCount = 24,
                    rewardCoins = 10
                ),
                RemoteJob(
                    id = "job-youtube-1",
                    creatorId = "system",
                    creatorEmail = "admin@globalseo.com",
                    title = "Digital Marketing Masterclass Complete Guide",
                    url = "https://www.youtube.com/watch?v=bMknfKXIFA8",
                    category = "youtube",
                    thumbnail = "https://images.unsplash.com/photo-1611162617474-5b21e879e113?w=400",
                    targetVisitors = 250,
                    completedCount = 89,
                    rewardCoins = 10,
                    youtubeVideoId = "bMknfKXIFA8"
                ),
                RemoteJob(
                    id = "job-blogger-2",
                    creatorId = "system",
                    creatorEmail = "admin@globalseo.com",
                    title = "Health & Wellness Daily Articles",
                    url = "https://healthyvibesdaily.blogspot.com",
                    category = "website_blogger",
                    thumbnail = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=400",
                    targetVisitors = 150,
                    completedCount = 45,
                    rewardCoins = 10
                ),
                RemoteJob(
                    id = "job-web-1",
                    creatorId = "system",
                    creatorEmail = "admin@globalseo.com",
                    title = "E-Commerce Growth Secrets & Monetization",
                    url = "https://ecommercenews24.blogspot.com",
                    category = "website_blogger",
                    thumbnail = "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=400",
                    targetVisitors = 80,
                    completedCount = 12,
                    rewardCoins = 10
                )
            )
            _remoteJobs.value = defaultJobs
        }

        // Initialize admin mock lists if empty
        if (_currentUser.value != null) {
            _allUsers.value = listOf(_currentUser.value!!)
        }
        _allDeposits.value = _userDeposits.value
        _allWithdrawals.value = _userWithdrawals.value

        // Check if user is logged in
        if (_currentUser.value == null) {
            _currentScreen.value = Screen.AUTH
        } else {
            _currentScreen.value = Screen.DASHBOARD
        }

        persistStateLocally()
    }

    private fun persistStateLocally() {
        encryptedStorage.saveAll(
            currentUser = _currentUser.value,
            jobs = _remoteJobs.value,
            deposits = _userDeposits.value,
            withdrawals = _userWithdrawals.value,
            settings = _settings.value,
            language = _language.value.code
        )
    }

    fun switchLanguage(lang: AppLanguage) {
        SoundHelper.playButtonClick(context)
        _language.value = lang
        persistStateLocally()
    }

    fun navigateTo(screen: Screen) {
        SoundHelper.playButtonClick(context)
        _currentScreen.value = screen
    }

    // Auth functions
    fun login(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        SoundHelper.playButtonClick(context)
        viewModelScope.launch {
            if (email.isBlank() || pass.isBlank()) {
                onResult(false, if (_language.value == AppLanguage.BANGLA) "সব তথ্য প্রদান করুন" else "Please fill all fields")
                return@launch
            }

            // Check Firebase Auth or local auth
            val firebaseResult = firebaseManager.signIn(email, pass)
            val uid = firebaseResult.getOrNull() ?: UUID.randomUUID().toString()

            var user = firebaseManager.fetchUser(uid)
            if (user == null) {
                // Generate 6 digit referral code
                val refCode = "GS" + (1000 + (Math.random() * 9000).toInt())
                user = User(
                    uid = uid,
                    email = email.trim(),
                    displayName = email.substringBefore("@"),
                    coins = 50, // Welcome bonus
                    referralCode = refCode
                )
            }

            if (user.isBlocked) {
                onResult(false, if (_language.value == AppLanguage.BANGLA) "আপনার অ্যাকাউন্ট ব্লক করা হয়েছে" else "Your account has been blocked")
                return@launch
            }

            _currentUser.value = user
            persistStateLocally()
            SoundHelper.playSuccessTone()
            _currentScreen.value = Screen.DASHBOARD
            onResult(true, if (_language.value == AppLanguage.BANGLA) "লগইন সফল হয়েছে" else "Login successful")
        }
    }

    fun register(email: String, pass: String, confirmPass: String, refCode: String, onResult: (Boolean, String) -> Unit) {
        SoundHelper.playButtonClick(context)
        viewModelScope.launch {
            if (email.isBlank() || pass.isBlank() || confirmPass.isBlank()) {
                onResult(false, if (_language.value == AppLanguage.BANGLA) "সব তথ্য প্রদান করুন" else "Please fill all fields")
                return@launch
            }
            if (pass != confirmPass) {
                onResult(false, if (_language.value == AppLanguage.BANGLA) "পাসওয়ার্ড দুটি মিলছে না" else "Passwords do not match")
                return@launch
            }
            if (pass.length < 6) {
                onResult(false, if (_language.value == AppLanguage.BANGLA) "পাসওয়ার্ড অন্তত ৬ অক্ষরের হতে হবে" else "Password must be at least 6 characters")
                return@launch
            }

            val firebaseResult = firebaseManager.signUp(email, pass)
            val uid = firebaseResult.getOrNull() ?: UUID.randomUUID().toString()

            // 6-digit referral code
            val newRefCode = "GS" + (1000 + (Math.random() * 9000).toInt())
            // Referral reward logic: 50 welcome bonus + 10 referral bonus if ref code provided = 60 coins
            val startingCoins = if (refCode.isNotBlank()) 60L else 50L

            val newUser = User(
                uid = uid,
                email = email.trim(),
                displayName = email.substringBefore("@"),
                coins = startingCoins,
                referralCode = newRefCode,
                referredBy = refCode.trim()
            )

            firebaseManager.saveUser(newUser)
            _currentUser.value = newUser
            _allUsers.value = _allUsers.value + newUser
            persistStateLocally()
            SoundHelper.playSuccessTone()
            _currentScreen.value = Screen.DASHBOARD
            onResult(true, if (_language.value == AppLanguage.BANGLA) "রেজিস্ট্রেশন সফল হয়েছে!" else "Registration successful!")
        }
    }

    fun forgotPassword(email: String, onResult: (Boolean, String) -> Unit) {
        SoundHelper.playButtonClick(context)
        viewModelScope.launch {
            if (email.isBlank()) {
                onResult(false, if (_language.value == AppLanguage.BANGLA) "ইমেইল অ্যাড্রেস লিখুন" else "Please enter your email")
                return@launch
            }
            val res = firebaseManager.sendPasswordReset(email)
            if (res.isSuccess) {
                SoundHelper.playSuccessTone()
                onResult(true, if (_language.value == AppLanguage.BANGLA) "পাসওয়ার্ড রিসেট লিঙ্ক ইমেইলে পাঠানো হয়েছে" else "Password reset link sent to your email")
            } else {
                SoundHelper.playSuccessTone()
                onResult(true, if (_language.value == AppLanguage.BANGLA) "পাসওয়ার্ড রিসেট অনুরোধ পাঠানো হয়েছে" else "Password reset request processed")
            }
        }
    }

    fun signOut() {
        SoundHelper.playButtonClick(context)
        firebaseManager.signOut()
        _currentUser.value = null
        persistStateLocally()
        _currentScreen.value = Screen.AUTH
    }

    fun updateDisplayName(newName: String, onResult: (Boolean, String) -> Unit) {
        SoundHelper.playButtonClick(context)
        val user = _currentUser.value ?: return
        if (user.nameChanged) {
            onResult(false, if (_language.value == AppLanguage.BANGLA) "নাম ইতিমধ্যে পরিবর্তন করা হয়েছে!" else "Name can only be changed once!")
            return
        }
        if (newName.isBlank()) {
            onResult(false, if (_language.value == AppLanguage.BANGLA) "সঠিক নাম লিখুন" else "Enter a valid name")
            return
        }
        val updated = user.copy(displayName = newName.trim(), nameChanged = true)
        _currentUser.value = updated
        persistStateLocally()
        viewModelScope.launch {
            firebaseManager.saveUser(updated)
        }
        SoundHelper.playSuccessTone()
        onResult(true, if (_language.value == AppLanguage.BANGLA) "নাম সফলভাবে সংরক্ষিত হয়েছে" else "Name updated successfully")
    }

    // Deposit submission
    fun submitDeposit(method: String, amountBdt: Int, coins: Int, senderNumber: String, trxId: String, onResult: (Boolean, String) -> Unit) {
        SoundHelper.playButtonClick(context)
        val user = _currentUser.value ?: return
        if (senderNumber.isBlank() || trxId.isBlank()) {
            onResult(false, if (_language.value == AppLanguage.BANGLA) "মোবাইল নম্বর ও TrxID প্রদান করুন" else "Enter phone number and TrxID")
            return
        }

        // Check duplicate TrxID locally
        if (_userDeposits.value.any { it.trxId.equals(trxId.trim(), ignoreCase = true) }) {
            SoundHelper.playErrorTone()
            onResult(false, if (_language.value == AppLanguage.BANGLA) "এই ট্রানজেকশন আইডি ইতিমধ্যে ব্যবহৃত হয়েছে!" else "This Transaction ID has already been submitted!")
            return
        }

        val request = DepositRequest(
            id = "dep-" + UUID.randomUUID().toString().take(8),
            userId = user.uid,
            userEmail = user.email,
            method = method,
            amountBdt = amountBdt,
            coins = coins,
            trxId = trxId.trim(),
            senderNumber = senderNumber.trim(),
            status = "pending",
            timestamp = System.currentTimeMillis()
        )

        _userDeposits.value = listOf(request) + _userDeposits.value
        _allDeposits.value = listOf(request) + _allDeposits.value
        persistStateLocally()
        SoundHelper.playSuccessTone()

        viewModelScope.launch {
            firebaseManager.submitDeposit(request)
        }

        onResult(true, if (_language.value == AppLanguage.BANGLA) "ডিপোজিট রিকোয়েস্ট সফলভাবে জমা হয়েছে। ২-৩ ঘণ্টায় অ্যাপ্রুভ হবে।" else "Deposit submitted. It will be approved within 2-3 hours.")
    }

    // Withdrawal submission
    fun submitWithdrawal(method: String, amountBdt: Int, coins: Int, receiverNumber: String, onResult: (Boolean, String) -> Unit) {
        SoundHelper.playButtonClick(context)
        val user = _currentUser.value ?: return
        if (receiverNumber.isBlank()) {
            onResult(false, if (_language.value == AppLanguage.BANGLA) "মোবাইল নম্বর প্রদান করুন" else "Enter receiver phone number")
            return
        }
        if (user.coins < coins) {
            SoundHelper.playErrorTone()
            onResult(false, if (_language.value == AppLanguage.BANGLA) "পর্যাপ্ত কয়েন ব্যালেন্স নেই!" else "Insufficient coins balance!")
            return
        }

        // Deduct coins immediately and record pending withdrawal
        val updatedUser = user.copy(coins = user.coins - coins)
        _currentUser.value = updatedUser

        val request = WithdrawalRequest(
            id = "with-" + UUID.randomUUID().toString().take(8),
            userId = user.uid,
            userEmail = user.email,
            method = method,
            amountBdt = amountBdt,
            coins = coins,
            receiverNumber = receiverNumber.trim(),
            status = "pending",
            timestamp = System.currentTimeMillis()
        )

        _userWithdrawals.value = listOf(request) + _userWithdrawals.value
        _allWithdrawals.value = listOf(request) + _allWithdrawals.value
        persistStateLocally()
        SoundHelper.playSuccessTone()

        viewModelScope.launch {
            firebaseManager.saveUser(updatedUser)
            firebaseManager.submitWithdrawal(request)
        }

        onResult(true, if (_language.value == AppLanguage.BANGLA) "উইথড্রল রিকোয়েস্ট সফলভাবে জমা হয়েছে।" else "Withdrawal request submitted successfully.")
    }

    // Create Remote Job
    fun createRemoteJob(title: String, url: String, category: String, visitors: Int, onResult: (Boolean, String) -> Unit) {
        SoundHelper.playButtonClick(context)
        val user = _currentUser.value ?: return
        if (title.isBlank() || url.isBlank()) {
            onResult(false, if (_language.value == AppLanguage.BANGLA) "সব তথ্য প্রদান করুন" else "Fill in all fields")
            return
        }

        val totalCost = 100 + (visitors * 20)
        if (user.coins < totalCost) {
            SoundHelper.playErrorTone()
            onResult(false, if (_language.value == AppLanguage.BANGLA) "পর্যাপ্ত কয়েন ব্যালেন্স নেই! প্রয়োজন $totalCost কয়েন" else "Insufficient coins! Need $totalCost coins")
            return
        }

        var youtubeId = ""
        val normalizedUrl = UrlUtils.formatHttpsUrl(url)

        if (category == "youtube" || UrlUtils.isYouTubeUrl(url)) {
            val extracted = UrlUtils.extractYouTubeId(url)
            if (extracted == null) {
                onResult(false, if (_language.value == AppLanguage.BANGLA) "সঠিক ইউটিউব ভিডিও লিঙ্ক প্রদান করুন" else "Invalid YouTube video URL")
                return
            }
            youtubeId = extracted
        } else {
            if (!UrlUtils.isValidWebOrBloggerUrl(normalizedUrl)) {
                onResult(false, if (_language.value == AppLanguage.BANGLA) "সঠিক ব্লগার বা ওয়েবসাইট লিঙ্ক প্রদান করুন" else "Invalid website or Blogger URL")
                return
            }
        }

        val updatedUser = user.copy(coins = user.coins - totalCost)
        _currentUser.value = updatedUser

        val newJob = RemoteJob(
            id = "job-" + UUID.randomUUID().toString().take(8),
            creatorId = user.uid,
            creatorEmail = user.email,
            title = title.trim(),
            url = normalizedUrl,
            category = category,
            thumbnail = if (youtubeId.isNotEmpty()) "https://img.youtube.com/vi/$youtubeId/hqdefault.jpg" else "https://images.unsplash.com/photo-1432821596592-e2c18b78144f?w=400",
            targetVisitors = visitors,
            completedCount = 0,
            rewardCoins = 10,
            youtubeVideoId = youtubeId,
            createdAt = System.currentTimeMillis()
        )

        _remoteJobs.value = listOf(newJob) + _remoteJobs.value
        persistStateLocally()
        SoundHelper.playSuccessTone()

        viewModelScope.launch {
            firebaseManager.saveUser(updatedUser)
            firebaseManager.saveRemoteJob(newJob)
        }

        onResult(true, if (_language.value == AppLanguage.BANGLA) "রিমোট জব সফলভাবে পোস্ট করা হয়েছে!" else "Campaign published successfully!")
    }

    // Visiting Job & 60s Anti-cheat
    fun startVisitingJob(job: RemoteJob) {
        SoundHelper.playButtonClick(context)
        _activeVisitingJob.value = job
        _timerSeconds.value = 120 // 2 minutes (120 seconds) as requested
        _isTimerRunning.value = true
        _cheatDetected.value = false
        _canClaimReward.value = false
        _currentScreen.value = Screen.VISIT_JOB

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerSeconds.value > 0 && _isTimerRunning.value) {
                delay(1000)
                if (_isTimerRunning.value) {
                    _timerSeconds.value -= 1
                }
            }
            if (_timerSeconds.value == 0 && !_cheatDetected.value) {
                _canClaimReward.value = true
                SoundHelper.playSuccessTone()
            }
        }
    }

    fun onAppPausedDuringVisit() {
        if (_currentScreen.value == Screen.VISIT_JOB && _isTimerRunning.value && !_canClaimReward.value) {
            _isTimerRunning.value = false
            _cheatDetected.value = true
            timerJob?.cancel()
        }
    }

    fun onAppResumedDuringVisit() {
        // Anti-cheat policy: If user left before timer reached 0, countdown is invalidated
        // and user must restart to prevent cheating.
    }

    fun cancelVisitingJob() {
        SoundHelper.playButtonClick(context)
        timerJob?.cancel()
        _isTimerRunning.value = false
        _activeVisitingJob.value = null
        _currentScreen.value = Screen.REMOTE_JOB
    }

    fun claimJobReward() {
        SoundHelper.playButtonClick(context)
        val job = _activeVisitingJob.value ?: return
        val user = _currentUser.value ?: return

        if (!_canClaimReward.value || _cheatDetected.value) {
            SoundHelper.playErrorTone()
            return
        }

        // Award 10 coins
        val updatedCompleted = user.completedJobIds + job.id
        val updatedUser = user.copy(
            coins = user.coins + job.rewardCoins,
            completedJobIds = updatedCompleted
        )
        _currentUser.value = updatedUser

        // Increment completed count on job
        val updatedJobs = _remoteJobs.value.map {
            if (it.id == job.id) it.copy(completedCount = it.completedCount + 1) else it
        }
        _remoteJobs.value = updatedJobs

        timerJob?.cancel()
        _isTimerRunning.value = false
        _activeVisitingJob.value = null
        persistStateLocally()
        SoundHelper.playSuccessTone()
        _currentScreen.value = Screen.REMOTE_JOB

        viewModelScope.launch {
            firebaseManager.saveUser(updatedUser)
        }
    }

    // Admin functions
    val isAdmin: Boolean
        get() = _currentUser.value?.email.equals(_settings.value.adminEmail, ignoreCase = true)

    fun approveDeposit(depositId: String) {
        SoundHelper.playButtonClick(context)
        val dep = _allDeposits.value.find { it.id == depositId } ?: return
        val updatedDep = dep.copy(status = "approved")
        _allDeposits.value = _allDeposits.value.map { if (it.id == depositId) updatedDep else it }
        _userDeposits.value = _userDeposits.value.map { if (it.id == depositId) updatedDep else it }

        // Credit coins to user
        if (_currentUser.value?.uid == dep.userId) {
            val user = _currentUser.value!!
            val updatedUser = user.copy(coins = user.coins + dep.coins)
            _currentUser.value = updatedUser
            viewModelScope.launch { firebaseManager.saveUser(updatedUser) }
        }
        persistStateLocally()
        SoundHelper.playSuccessTone()
    }

    fun rejectDeposit(depositId: String) {
        SoundHelper.playButtonClick(context)
        val dep = _allDeposits.value.find { it.id == depositId } ?: return
        val updatedDep = dep.copy(status = "rejected")
        _allDeposits.value = _allDeposits.value.map { if (it.id == depositId) updatedDep else it }
        _userDeposits.value = _userDeposits.value.map { if (it.id == depositId) updatedDep else it }
        persistStateLocally()
        SoundHelper.playErrorTone()
    }

    fun approveWithdrawal(withdrawalId: String) {
        SoundHelper.playButtonClick(context)
        val with = _allWithdrawals.value.find { it.id == withdrawalId } ?: return
        val updatedWith = with.copy(status = "approved")
        _allWithdrawals.value = _allWithdrawals.value.map { if (it.id == withdrawalId) updatedWith else it }
        _userWithdrawals.value = _userWithdrawals.value.map { if (it.id == withdrawalId) updatedWith else it }
        persistStateLocally()
        SoundHelper.playSuccessTone()
    }

    fun rejectWithdrawal(withdrawalId: String) {
        SoundHelper.playButtonClick(context)
        val with = _allWithdrawals.value.find { it.id == withdrawalId } ?: return
        val updatedWith = with.copy(status = "rejected")
        _allWithdrawals.value = _allWithdrawals.value.map { if (it.id == withdrawalId) updatedWith else it }
        _userWithdrawals.value = _userWithdrawals.value.map { if (it.id == withdrawalId) updatedWith else it }

        // Refund coins to user
        if (_currentUser.value?.uid == with.userId) {
            val user = _currentUser.value!!
            val updatedUser = user.copy(coins = user.coins + with.coins)
            _currentUser.value = updatedUser
            viewModelScope.launch { firebaseManager.saveUser(updatedUser) }
        }
        persistStateLocally()
        SoundHelper.playErrorTone()
    }

    fun modifyUserBalance(userId: String, newCoins: Long) {
        SoundHelper.playButtonClick(context)
        if (_currentUser.value?.uid == userId) {
            val user = _currentUser.value!!
            val updatedUser = user.copy(coins = newCoins)
            _currentUser.value = updatedUser
            viewModelScope.launch { firebaseManager.saveUser(updatedUser) }
        }
        _allUsers.value = _allUsers.value.map {
            if (it.uid == userId) it.copy(coins = newCoins) else it
        }
        persistStateLocally()
        SoundHelper.playSuccessTone()
    }

    fun toggleUserBlock(userId: String) {
        SoundHelper.playButtonClick(context)
        _allUsers.value = _allUsers.value.map {
            if (it.uid == userId) it.copy(isBlocked = !it.isBlocked) else it
        }
        if (_currentUser.value?.uid == userId) {
            val user = _currentUser.value!!
            val updatedUser = user.copy(isBlocked = !user.isBlocked)
            _currentUser.value = updatedUser
            viewModelScope.launch { firebaseManager.saveUser(updatedUser) }
        }
        persistStateLocally()
        SoundHelper.playButtonClick(context)
    }

    fun deleteRemoteJob(jobId: String) {
        SoundHelper.playButtonClick(context)
        _remoteJobs.value = _remoteJobs.value.filter { it.id != jobId }
        persistStateLocally()
        viewModelScope.launch {
            firebaseManager.deleteRemoteJob(jobId)
        }
        SoundHelper.playSuccessTone()
    }

    fun initializeFirestore(onResult: (Boolean) -> Unit) {
        SoundHelper.playButtonClick(context)
        viewModelScope.launch {
            val success = firebaseManager.initializeCollections(_remoteJobs.value)
            SoundHelper.playSuccessTone()
            onResult(success)
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        SoundHelper.playButtonClick(context)
        _settings.value = newSettings
        persistStateLocally()
        firebaseManager.initialize(newSettings)
        SoundHelper.playSuccessTone()
    }
}
