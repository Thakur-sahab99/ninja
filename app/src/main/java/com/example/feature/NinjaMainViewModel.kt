package com.example.feature

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.NinjaDatabase
import com.example.core.database.entity.CallEntity
import com.example.core.database.entity.ConversationEntity
import com.example.core.database.entity.MessageEntity
import com.example.core.database.entity.StatusEntity
import com.example.core.model.SecuritySettings
import com.example.core.model.UserProfile
import com.example.data.repository.ChatRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    CHATS, STATUS, CALLS, SETTINGS
}

enum class ActiveScreen {
    ONBOARDING, MAIN_TABS, CHAT_ROOM, SAFETY_NUMBER, ACTIVE_CALL, ARCHIVED_CHATS
}

data class ActiveCallState(
    val contactName: String = "",
    val contactPhone: String = "",
    val isVideo: Boolean = false,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isVideoPaused: Boolean = false,
    val durationSeconds: Int = 0,
    val isConnected: Boolean = false,
    val isRelayed: Boolean = true
)

class NinjaMainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NinjaDatabase.getInstance(application)
    val chatRepository = ChatRepository(database, viewModelScope)
    val settingsRepository = SettingsRepository()

    val conversations: StateFlow<List<ConversationEntity>> =
        chatRepository.conversationsFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val archivedConversations: StateFlow<List<ConversationEntity>> =
        chatRepository.archivedConversationsFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val calls: StateFlow<List<CallEntity>> =
        chatRepository.callsFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val statuses: StateFlow<List<StatusEntity>> =
        chatRepository.getStatusFlow(System.currentTimeMillis()).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val settings: StateFlow<SecuritySettings> = settingsRepository.settings
    val userProfile: StateFlow<UserProfile> = settingsRepository.userProfile

    private val _currentScreen = MutableStateFlow(ActiveScreen.MAIN_TABS)
    val currentScreen: StateFlow<ActiveScreen> = _currentScreen.asStateFlow()

    private val _selectedTab = MutableStateFlow(MainTab.CHATS)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _currentConversation = MutableStateFlow<ConversationEntity?>(null)
    val currentConversation: StateFlow<ConversationEntity?> = _currentConversation.asStateFlow()

    private val _currentMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val currentMessages: StateFlow<List<MessageEntity>> = _currentMessages.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Voice note recording simulation
    private val _isRecordingVoiceNote = MutableStateFlow(false)
    val isRecordingVoiceNote: StateFlow<Boolean> = _isRecordingVoiceNote.asStateFlow()

    private val _voiceRecordingDuration = MutableStateFlow(0)
    val voiceRecordingDuration: StateFlow<Int> = _voiceRecordingDuration.asStateFlow()
    private var voiceTimerJob: Job? = null

    // Quoting / editing
    private val _replyingToMessage = MutableStateFlow<MessageEntity?>(null)
    val replyingToMessage: StateFlow<MessageEntity?> = _replyingToMessage.asStateFlow()

    private val _editingMessage = MutableStateFlow<MessageEntity?>(null)
    val editingMessage: StateFlow<MessageEntity?> = _editingMessage.asStateFlow()

    // Active call state
    private val _activeCall = MutableStateFlow<ActiveCallState?>(null)
    val activeCall: StateFlow<ActiveCallState?> = _activeCall.asStateFlow()
    private var callTimerJob: Job? = null

    // View Once active dialog
    private val _viewOnceMessage = MutableStateFlow<MessageEntity?>(null)
    val viewOnceMessage: StateFlow<MessageEntity?> = _viewOnceMessage.asStateFlow()

    init {
        viewModelScope.launch {
            chatRepository.seedInitialDataIfEmpty()
        }
        // Periodic worker for expiring disappearing messages
        viewModelScope.launch {
            while (true) {
                delay(2000)
                chatRepository.purgeExpiredMessages()
            }
        }
    }

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openConversation(conversationId: String) {
        _activeConversationId.value = conversationId
        _currentScreen.value = ActiveScreen.CHAT_ROOM
        viewModelScope.launch {
            chatRepository.markAsRead(conversationId)
            _currentConversation.value = chatRepository.getConversation(conversationId)
            chatRepository.getMessagesFlow(conversationId).collect { msgs ->
                _currentMessages.value = msgs
            }
        }
    }

    fun closeConversation() {
        _activeConversationId.value = null
        _currentConversation.value = null
        _replyingToMessage.value = null
        _editingMessage.value = null
        _currentScreen.value = ActiveScreen.MAIN_TABS
    }

    fun openArchivedChats() {
        _currentScreen.value = ActiveScreen.ARCHIVED_CHATS
    }

    fun openSafetyNumberScreen() {
        _currentScreen.value = ActiveScreen.SAFETY_NUMBER
    }

    fun navigateBack() {
        when (_currentScreen.value) {
            ActiveScreen.SAFETY_NUMBER -> _currentScreen.value = ActiveScreen.CHAT_ROOM
            ActiveScreen.CHAT_ROOM -> closeConversation()
            ActiveScreen.ARCHIVED_CHATS -> _currentScreen.value = ActiveScreen.MAIN_TABS
            ActiveScreen.ACTIVE_CALL -> endCall()
            ActiveScreen.ONBOARDING -> _currentScreen.value = ActiveScreen.MAIN_TABS
            ActiveScreen.MAIN_TABS -> {}
        }
    }

    fun sendMessage(
        content: String,
        mediaType: String = "TEXT",
        mediaUri: String? = null,
        mediaDurationSec: Int = 0,
        isViewOnce: Boolean = false
    ) {
        val convId = _activeConversationId.value ?: return
        val conv = _currentConversation.value
        val reply = _replyingToMessage.value
        val edit = _editingMessage.value

        viewModelScope.launch {
            if (edit != null) {
                chatRepository.editMessage(edit.id, content)
                _editingMessage.value = null
            } else {
                chatRepository.sendMessage(
                    conversationId = convId,
                    content = content,
                    mediaType = mediaType,
                    mediaUri = mediaUri,
                    mediaDurationSeconds = mediaDurationSec,
                    replyToId = reply?.id,
                    replyToContent = reply?.content,
                    isViewOnce = isViewOnce,
                    isDisappearing = (conv?.disappearingTimerSeconds ?: 0L) > 0,
                    disappearingTimerSeconds = conv?.disappearingTimerSeconds ?: 0L
                )
                _replyingToMessage.value = null
            }
        }
    }

    fun startVoiceRecording() {
        _isRecordingVoiceNote.value = true
        _voiceRecordingDuration.value = 0
        voiceTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _voiceRecordingDuration.value += 1
            }
        }
    }

    fun stopVoiceRecording(send: Boolean) {
        voiceTimerJob?.cancel()
        val duration = _voiceRecordingDuration.value
        val wasRecording = _isRecordingVoiceNote.value
        _isRecordingVoiceNote.value = false
        _voiceRecordingDuration.value = 0

        if (send && wasRecording && duration > 0) {
            sendMessage(
                content = "Voice note (${duration}s)",
                mediaType = "VOICE_NOTE",
                mediaDurationSec = duration
            )
        }
    }

    fun setReplyMessage(message: MessageEntity?) {
        _replyingToMessage.value = message
        _editingMessage.value = null
    }

    fun setEditingMessage(message: MessageEntity?) {
        _editingMessage.value = message
        _replyingToMessage.value = null
    }

    fun addReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            chatRepository.addReaction(messageId, emoji)
        }
    }

    fun deleteForEveryone(messageId: String) {
        viewModelScope.launch {
            chatRepository.deleteForEveryone(messageId)
        }
    }

    fun deleteForMe(messageId: String) {
        viewModelScope.launch {
            chatRepository.deleteForMe(messageId)
        }
    }

    fun toggleStar(message: MessageEntity) {
        viewModelScope.launch {
            chatRepository.toggleStar(message.id, message.isStarred)
        }
    }

    fun openViewOnce(message: MessageEntity) {
        _viewOnceMessage.value = message
        viewModelScope.launch {
            chatRepository.markViewOnceOpened(message.id)
        }
    }

    fun closeViewOnce() {
        _viewOnceMessage.value = null
    }

    fun updateDisappearingTimer(seconds: Long) {
        val convId = _activeConversationId.value ?: return
        viewModelScope.launch {
            chatRepository.updateDisappearingTimer(convId, seconds)
            _currentConversation.value = _currentConversation.value?.copy(disappearingTimerSeconds = seconds)
        }
    }

    fun verifySafetyNumber(verified: Boolean) {
        val convId = _activeConversationId.value ?: return
        viewModelScope.launch {
            chatRepository.setSafetyNumberVerified(convId, verified)
            _currentConversation.value = _currentConversation.value?.copy(safetyNumberVerified = verified)
        }
    }

    fun togglePin(conversation: ConversationEntity) {
        viewModelScope.launch {
            chatRepository.togglePin(conversation.id, conversation.isPinned)
        }
    }

    fun toggleArchive(conversation: ConversationEntity) {
        viewModelScope.launch {
            chatRepository.toggleArchive(conversation.id, conversation.isArchived)
        }
    }

    fun toggleShadowMode() {
        settingsRepository.toggleShadowMode()
    }

    fun startCall(contactName: String, contactPhone: String, isVideo: Boolean) {
        _activeCall.value = ActiveCallState(
            contactName = contactName,
            contactPhone = contactPhone,
            isVideo = isVideo,
            isConnected = true,
            isRelayed = settings.value.relayAllCallsEnabled
        )
        _currentScreen.value = ActiveScreen.ACTIVE_CALL

        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _activeCall.value = _activeCall.value?.let {
                    it.copy(durationSeconds = it.durationSeconds + 1)
                }
            }
        }
    }

    fun toggleMuteCall() {
        _activeCall.value = _activeCall.value?.let {
            it.copy(isMuted = !it.isMuted)
        }
    }

    fun toggleSpeakerCall() {
        _activeCall.value = _activeCall.value?.let {
            it.copy(isSpeakerOn = !it.isSpeakerOn)
        }
    }

    fun toggleVideoPause() {
        _activeCall.value = _activeCall.value?.let {
            it.copy(isVideoPaused = !it.isVideoPaused)
        }
    }

    fun endCall() {
        callTimerJob?.cancel()
        val current = _activeCall.value
        if (current != null) {
            viewModelScope.launch {
                chatRepository.addCallLog(
                    contactName = current.contactName,
                    phone = current.contactPhone,
                    isVideo = current.isVideo,
                    durationSec = current.durationSeconds
                )
            }
        }
        _activeCall.value = null
        if (_activeConversationId.value != null) {
            _currentScreen.value = ActiveScreen.CHAT_ROOM
        } else {
            _currentScreen.value = ActiveScreen.MAIN_TABS
        }
    }

    fun createNewChat(name: String, phone: String, isGroup: Boolean, emoji: String) {
        viewModelScope.launch {
            val id = chatRepository.createNewConversation(name, phone, isGroup, emoji)
            openConversation(id)
        }
    }

    fun addStatus(caption: String, bgHex: String) {
        viewModelScope.launch {
            chatRepository.addStatus(caption, bgHex)
        }
    }
}
