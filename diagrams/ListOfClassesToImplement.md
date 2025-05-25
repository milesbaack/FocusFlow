
    
# Service Interfaces

    class ITaskManagementService {
        <<interface>>
        +createTask(String name, String description, LocalDateTime dueDate, TaskPriority priority) Task
        +updateTask(Task task) boolean
        +deleteTask(UUID taskId) boolean
        +getTaskById(UUID taskId) Task
        +getAllTasks() List~Task~
        +getTasksByStatus(TaskStatus status) List~Task~
        +getTasksByPriority(TaskPriority priority) List~Task~
        +getTasksByCategory(TaskCategory category) List~Task~
        +getTasksDueToday() List~Task~
        +getTasksOverdue() List~Task~
        +markTaskAsCompleted(UUID taskId) boolean
        +markTaskAsIncomplete(UUID taskId) boolean
        +createSubTask(UUID parentTaskId, String name, String description) SubTask
        +updateSubTask(SubTask subTask) boolean
        +deleteSubTask(UUID subTaskId) boolean
        +getSubTasksForTask(UUID taskId) List~SubTask~
        +markSubTaskCompleted(UUID subTaskId) boolean
        +createCategory(String name, String colorCode) TaskCategory
        +updateCategory(TaskCategory category) boolean
        +deleteCategory(UUID categoryId) boolean
        +getAllCategories() List~TaskCategory~
        +addEventListener(TaskObserver listener)
        +removeEventListener(TaskObserver listener)
    }
    
    class ITimerService {
        <<interface>>
        +startTimer() boolean
        +pauseTimer() boolean
        +resumeTimer() boolean
        +stopTimer() boolean
        +resetTimer() boolean
        +getTimerState() TimerState
        +getRemainingTime() Duration
        +getElapsedTime() Duration
        +getCurrentSession() TimerSession
        +getCompletedSessions() List~TimerSession~
        +getCompletedSessionsForDay(LocalDate date) List~TimerSession~
        +getCompletedSessionsCount() int
        +getDailySessionCount() int
        +associateTaskWithSession(UUID taskId) boolean
        +addInterruptionToSession() boolean
        +setTimerType(TimerType timerType) void
        +configureTimerDurations(Duration workDuration, Duration shortBreakDuration, Duration longBreakDuration) void
        +setSessionsUntilLongBreak(int count) void
        +addEventListener(TimerEventListener listener) void
        +removeEventListener(TimerEventListener listener) void
    }
    
    class IUserService {
        <<interface>>
        +getCurrentUser() User
        +createUser(String username, String email) User
        +updateUser(User user) boolean
        +updateUserPreferences(UserPreferences preferences) boolean
        +getUserPreferences() UserPreferences
        +saveUserData() boolean
        +loadUserData() boolean
        +addExperiencePoints(int points) boolean
        +getCurrentLevel() int
        +getPointsForNextLevel() int
        +getPointsProgress() double
        +getAchievements() List~Achievement~
        +getUnlockedAchievements() List~Achievement~
        +unlockAchievement(UUID achievementId) boolean
        +getActiveQuests() List~Quest~
        +getCompletedQuests() List~Quest~
        +completeQuest(UUID questId) boolean
        +assignNewQuests(int count) void
    }
    
    class INotificationService {
        <<interface>>
        +showNotification(String title, String message) void
        +showNotification(String title, String message, NotificationType type) void
        +playSound(SoundType soundType) void
        +stopSound() void
        +showTimerCompleteNotification(TimerSession session) void
        +showBreakStartNotification() void
        +showBreakEndNotification() void
        +showTaskDueNotification(Task task) void
        +showAchievementUnlockedNotification(Achievement achievement) void
        +showQuestCompletedNotification(Quest quest) void
        +isNotificationEnabled(NotificationType type) boolean
        +isSoundEnabled(SoundType soundType) boolean
        +enableNotificationType(NotificationType type, boolean enabled) void
        +enableSoundType(SoundType soundType, boolean enabled) void
        +setVolume(float volume) void
    }
    
    class IAnalyticsService {
        <<interface>>
        +getCompletionRate(LocalDateTime startDate, LocalDateTime endDate) double
        +getAverageDailyTasksCompleted(LocalDateTime startDate, LocalDateTime endDate) double
        +getAverageFocusSessionsPerDay(LocalDateTime startDate, LocalDateTime endDate) double
        +getAverageFocusTimePerDay(LocalDateTime startDate, LocalDateTime endDate) Duration
        +getTaskCompletionByCategory(LocalDateTime startDate, LocalDateTime endDate) Map~TaskCategory, Integer~
        +getTaskCompletionByPriority(LocalDateTime startDate, LocalDateTime endDate) Map~TaskPriority, Integer~
        +getProductivityByDayOfWeek(LocalDateTime startDate, LocalDateTime endDate) Map~DayOfWeek, Double~
        +getProductivityByTimeOfDay(LocalDateTime startDate, LocalDateTime endDate) Map~Integer, Double~
        +getMostProductiveTimeOfDay(LocalDateTime startDate, LocalDateTime endDate) int
        +getMostProductiveDayOfWeek(LocalDateTime startDate, LocalDateTime endDate) DayOfWeek
        +generateProductivityReport(LocalDateTime startDate, LocalDateTime endDate) ProductivityReport
        +getInterruptionFrequency(LocalDateTime startDate, LocalDateTime endDate) double
        +getProductivityTrend(LocalDateTime startDate, LocalDateTime endDate) List~DataPoint~
        +getCompletionRateTrend(LocalDateTime startDate, LocalDateTime endDate) List~DataPoint~
        +getFocusSessionDurationTrend(LocalDateTime startDate, LocalDateTime endDate) List~DataPoint~
    }
    
    class IFocusToolsService {
        <<interface>>
        +playAmbientSound(SoundType soundType) void
        +stopAmbientSound() void
        +setAmbientVolume(float volume) void
        +getAvailableSoundTypes() List~SoundType~
        +addToDistractionList(String item) DistractionItem
        +getDistractionList() List~DistractionItem~
        +markDistractionAsHandled(UUID distractionId) boolean
        +getDistractionItemsForBreak() List~DistractionItem~
        +clearDistractionList() void
        +showTutorial() void
        +showPomodoroTechniqueBenefits() void
        +isPomodoroTutorialCompleted() boolean
        +markTutorialAsCompleted() void
    }
    
    
  # Task Management Domain
    class Task {
        -UUID id
        -String name
        -String description
        -LocalDateTime creationDate
        -LocalDateTime lastModifiedDate
        -LocalDateTime dueDate
        -boolean completed
        -boolean inProgress
        -boolean postponed
        -TaskPriority priority
        -TaskCategory category
        -List~UUID~ subTaskIds
        +Task(String name, String description)
        +Task(String name, String description, LocalDateTime dueDate)
        +Task(String name, String description, LocalDateTime dueDate, TaskPriority priority)
        +UUID getId()
        +String getName()
        +void setName(String name)
        +String getDescription()
        +void setDescription(String description)
        +LocalDateTime getCreationDate()
        +LocalDateTime getLastModifiedDate()
        +LocalDateTime getDueDate()
        +void setDueDate(LocalDateTime dueDate)
        +boolean hasDueDate()
        +boolean isCompleted()
        +void markAsCompleted()
        +void markAsIncomplete()
        +boolean isInProgress()
        +void setInProgress(boolean inProgress)
        +boolean isPostponed()
        +void setPostponed(boolean postponed)
        +TaskPriority getPriority()
        +void setPriority(TaskPriority priority)
        +TaskCategory getCategory()
        +void setCategory(TaskCategory category)
        +List~UUID~ getSubTaskIds()
        +void addSubTaskId(UUID subTaskId)
        +void removeSubTaskId(UUID subTaskId)
        +boolean isOverdue()
        +TaskStatus getStatus()
        +boolean equals(Object obj)
        +int hashCode()
        +String toString()
    }
    
    class SubTask {
        -UUID id
        -UUID parentTaskId
        -String name
        -String description
        -boolean completed
        -LocalDateTime creationDate
        -LocalDateTime lastModifiedDate
        +SubTask(String name, String description, UUID parentTaskId)
        +UUID getId()
        +UUID getParentTaskId()
        +String getName()
        +void setName(String name)
        +String getDescription()
        +void setDescription(String description)
        +boolean isCompleted()
        +void markAsCompleted()
        +void markAsIncomplete()
        +LocalDateTime getCreationDate()
        +LocalDateTime getLastModifiedDate()
        +boolean equals(Object obj)
        +int hashCode()
        +String toString()
    }
    
    class TaskCategory {
        -UUID id
        -String name
        -String colorCode
        +TaskCategory(String name)
        +TaskCategory(String name, String colorCode)
        +UUID getId()
        +String getName()
        +void setName(String name)
        +String getColorCode()
        +void setColorCode(String colorCode)
        +boolean equals(Object obj)
        +int hashCode()
        +String toString()
    }
    
    class TaskPriority {
        <<enumeration>>
        LOW
        MEDIUM
        HIGH
        URGENT
        -int value
        -String displayName
        -String colorCode
        +int getValue()
        +String getDisplayName()
        +String getColorCode()
    }
    
    class TaskStatus {
        <<enumeration>>
        NOT_STARTED
        IN_PROGRESS
        COMPLETED
        OVERDUE
        DUE_TODAY
        DUE_SOON
        POSTPONED
        +String getDisplayName()
        +String getColorCode()
        +static TaskStatus fromTask(Task task)
    }
    
    class TaskObserver {
        <<interface>>
        +onTaskCreated(UUID taskId)
        +onTaskUpdated(UUID taskId, String propertyName, Object oldValue, Object newValue)
        +onTaskCompleted(UUID taskId)
        +onTaskDeleted(UUID taskId)
        +onSubTaskAdded(UUID taskId, UUID subTaskId)
        +onSubTaskCompleted(UUID taskId, UUID subTaskId)
        +onSubTaskRemoved(UUID taskId, UUID subTaskId)
    }
    
  # Timer System
    class Timer {
        <<interface>>
        +start() boolean
        +pause() boolean
        +resume() boolean
        +stop() boolean
        +reset() boolean
        +getRemainingTime() Duration
        +getElapsedTime() Duration
        +getState() TimerState
        +getTimerType() TimerType
        +addEventListener(TimerEventListener listener)
        +removeEventListener(TimerEventListener listener)
    }
    
    class PomodoroTimer {
        -Duration workDuration
        -Duration shortBreakDuration
        -Duration longBreakDuration
        -int sessionsBeforeLongBreak
        -int completedSessions
        -TimerState state
        -LocalDateTime startTime
        -LocalDateTime pauseTime
        -Duration remainingTime
        -List~TimerEventListener~ eventListeners
        +PomodoroTimer(Duration workDuration, Duration shortBreakDuration, Duration longBreakDuration, int sessionsBeforeLongBreak)
        +start() boolean
        +pause() boolean
        +resume() boolean
        +stop() boolean
        +reset() boolean
        +getRemainingTime() Duration
        +getElapsedTime() Duration
        +getState() TimerState
        +getTimerType() TimerType
        +getCompletedSessions() int
        +isWorkSession() boolean
        +isBreakSession() boolean
        +isLongBreak() boolean
        -fireTimerEvent(TimerEvent event)
        -transitionToNextState()
    }
    
    class TimerState {
        <<enumeration>>
        READY
        RUNNING
        PAUSED
        STOPPED
        COMPLETED
        WORK_SESSION
        SHORT_BREAK
        LONG_BREAK
    }
    
    class TimerType {
        <<enumeration>>
        POMODORO
        FLOWTIME
        CUSTOM
    }
    
    class TimerSession {
        -UUID id
        -LocalDateTime startTime
        -LocalDateTime endTime
        -Duration duration
        -UUID associatedTaskId
        -boolean completed
        -int interruptions
        -TimerType timerType
        +TimerSession(TimerType timerType)
        +TimerSession(TimerType timerType, UUID associatedTaskId)
        +UUID getId()
        +LocalDateTime getStartTime()
        +LocalDateTime getEndTime()
        +void setEndTime(LocalDateTime endTime)
        +Duration getDuration()
        +UUID getAssociatedTaskId()
        +void setAssociatedTaskId(UUID taskId)
        +boolean isCompleted()
        +void setCompleted(boolean completed)
        +int getInterruptions()
        +void incrementInterruptions()
        +TimerType getTimerType()
        +boolean equals(Object obj)
        +int hashCode()
        +String toString()
    }
    
    class TimerEventListener {
        <<interface>>
        +onTimerStarted(TimerEvent event)
        +onTimerPaused(TimerEvent event)
        +onTimerResumed(TimerEvent event)
        +onTimerCompleted(TimerEvent event)
        +onTimerStateChanged(TimerEvent event)
        +onTimerTick(TimerEvent event)
    }
    
  # User and Preferences
    class User {
        -UUID id
        -String username
        -String email
        -UserPreferences preferences
        -int experiencePoints
        -int level
        -LocalDateTime registrationDate
        -LocalDateTime lastLoginDate
        +User(String username, String email)
        +UUID getId()
        +String getUsername()
        +void setUsername(String username)
        +String getEmail()
        +void setEmail(String email)
        +UserPreferences getPreferences()
        +void setPreferences(UserPreferences preferences)
        +int getExperiencePoints()
        +void addExperiencePoints(int points)
        +int getLevel()
        +void setLevel(int level)
        +LocalDateTime getRegistrationDate()
        +LocalDateTime getLastLoginDate()
        +void setLastLoginDate(LocalDateTime lastLoginDate)
        +boolean equals(Object obj)
        +int hashCode()
        +String toString()
    }
    
    class UserPreferences {
        -Theme theme
        -boolean notificationsEnabled
        -boolean soundEnabled
        -Duration workDuration
        -Duration shortBreakDuration
        -Duration longBreakDuration
        -int sessionsUntilLongBreak
        -Map~NotificationType, Boolean~ notificationPreferences
        -Map~SoundType, Boolean~ soundPreferences
        -float globalVolume
        +UserPreferences()
        +Theme getTheme()
        +void setTheme(Theme theme)
        +boolean isNotificationsEnabled()
        +void setNotificationsEnabled(boolean notificationsEnabled)
        +boolean isSoundEnabled()
        +void setSoundEnabled(boolean soundEnabled)
        +Duration getWorkDuration()
        +void setWorkDuration(Duration workDuration)
        +Duration getShortBreakDuration()
        +void setShortBreakDuration(Duration shortBreakDuration)
        +Duration getLongBreakDuration()
        +void setLongBreakDuration(Duration longBreakDuration)
        +int getSessionsUntilLongBreak()
        +void setSessionsUntilLongBreak(int sessionsUntilLongBreak)
        +boolean isNotificationTypeEnabled(NotificationType type)
        +void setNotificationPreference(NotificationType type, boolean enabled)
        +boolean isSoundTypeEnabled(SoundType soundType)
        +void setSoundPreference(SoundType soundType, boolean enabled)
        +float getGlobalVolume()
        +void setGlobalVolume(float volume)
    }
    
    class Theme {
        -boolean darkMode
        -String primaryColor
        -String secondaryColor
        -String accentColor
        -String textColor
        -String backgroundColor
        +Theme(boolean darkMode)
        +boolean isDarkMode()
        +void setDarkMode(boolean darkMode)
        +String getPrimaryColor()
        +void setPrimaryColor(String primaryColor)
        +String getSecondaryColor()
        +void setSecondaryColor(String secondaryColor)
        +String getAccentColor()
        +void setAccentColor(String accentColor)
        +String getTextColor()
        +void setTextColor(String textColor)
        +String getBackgroundColor()
        +void setBackgroundColor(String backgroundColor)
        +Theme createCopy()
        +static Theme createDefaultLightTheme()
        +static Theme createDefaultDarkTheme()
    }
    
  # Gamification
    class GamificationSystem {
        -UUID userId
        -List~Achievement~ achievements
        -List~Quest~ activeQuests
        -List~Quest~ completedQuests
        -AchievementRepository achievementRepository
        -QuestRepository questRepository
        +GamificationSystem(UUID userId, AchievementRepository achievementRepository, QuestRepository questRepository)
        +List~Achievement~ getAchievements()
        +List~Achievement~ getUnlockedAchievements()
        +List~Achievement~ getLockedAchievements()
        +boolean unlockAchievement(UUID achievementId)
        +List~Quest~ getActiveQuests()
        +List~Quest~ getCompletedQuests()
        +boolean completeQuest(UUID questId)
        +void assignNewQuests(int count)
        +boolean hasUnlockedAchievement(UUID achievementId)
        +int calculateLevelFromXP(int experiencePoints)
        +int getRequiredXPForNextLevel(int currentLevel)
    }
    
    class Achievement {
        -UUID id
        -String title
        -String description
        -int experienceReward
        -boolean unlocked
        -LocalDateTime unlockDate
        -AchievementType type
        -String iconName
        +Achievement(String title, String description, int experienceReward, AchievementType type)
        +UUID getId()
        +String getTitle()
        +String getDescription()
        +int getExperienceReward()
        +boolean isUnlocked()
        +void unlock()
        +LocalDateTime getUnlockDate()
        +AchievementType getType()
        +String getIconName()
        +void setIconName(String iconName)
        +boolean equals(Object obj)
        +int hashCode()
        +String toString()
    }
    
    class AchievementType {
        <<enumeration>>
        TASK_COMPLETION
        TASK_STREAK
        FOCUS_SESSION
        FOCUS_STREAK
        SPECIAL
    }
    
    class Quest {
        -UUID id
        -String title
        -String description
        -int experienceReward
        -LocalDateTime assignedDate
        -LocalDateTime completionDate
        -LocalDateTime expirationDate
        -boolean completed
        -QuestType type
        -int requiredCount
        -int currentCount
        +Quest(String title, String description, int experienceReward, QuestType type)
        +UUID getId()
        +String getTitle()
        +String getDescription()
        +int getExperienceReward()
        +LocalDateTime getAssignedDate()
        +LocalDateTime getCompletionDate()
        +LocalDateTime getExpirationDate()
        +void setExpirationDate(LocalDateTime expirationDate)
        +boolean isCompleted()
        +void setCompleted(boolean completed)
        +void markAsCompleted()
        +QuestType getType()
        +int getRequiredCount()
        +void setRequiredCount(int requiredCount)
        +int getCurrentCount()
        +void setCurrentCount(int currentCount)
        +void incrementCurrentCount()
        +double getProgressPercentage()
        +boolean isExpired()
        +boolean equals(Object obj)
        +int hashCode()
        +String toString()
    }
    
    class QuestType {
        <<enumeration>>
        COMPLETE_TASKS
        COMPLETE_PRIORITY_TASKS
        FOCUS_SESSIONS
        USE_FEATURES
        STREAK
        SPECIAL
    }
    
  # Notifications
    class NotificationManager {
        -UserPreferences userPreferences
        +NotificationManager(UserPreferences userPreferences)
        +showNotification(String title, String message)
        +showNotification(String title, String message, NotificationType type)
        +isNotificationEnabled(NotificationType type)
        +enableNotificationType(NotificationType type, boolean enabled)
    }
    
    class NotificationType {
        <<enumeration>>
        TIMER_COMPLETE
        BREAK_START
        BREAK_END
        TASK_DUE
        ACHIEVEMENT_UNLOCKED
        QUEST_COMPLETED
        SYSTEM
    }
    
    class SoundManager {
        -UserPreferences userPreferences
        -Map~SoundType, AudioResource~ soundResources
        -float globalVolume
        +SoundManager(UserPreferences userPreferences)
        +playSound(SoundType soundType)
        +stopSound()
        +isSoundEnabled(SoundType soundType)
        +enableSoundType(SoundType soundType, boolean enabled)
        +setGlobalVolume(float volume)
        +float getGlobalVolume()
        -loadSoundResources()
    }
    
    class SoundType {
        <<enumeration>>
        TIMER_COMPLETE
        BREAK_START
        BREAK_END
        TASK_COMPLETE
        ACHIEVEMENT_UNLOCKED
        WHITE_NOISE
        RAIN_SOUNDS
        CAFE_AMBIENCE
        FOREST_SOUNDS
        BINAURAL_BEATS_FOCUS
        BINAURAL_BEATS_CREATIVITY
        BINAURAL_BEATS_RELAXATION
    }
    
  # Focus Tools
    class DistractionManager {
        -List~DistractionItem~ distractionItems
        +DistractionManager()
        +addDistractionItem(String content) DistractionItem
        +getDistractionItems() List~DistractionItem~
        +getDistractionItemsForBreak() List~DistractionItem~
        +markItemAsHandled(UUID itemId) boolean
        +clearDistractionItems() void
    }
    
    class DistractionItem {
        -UUID id
        -String content
        -LocalDateTime creationTime
        -boolean handled
        +DistractionItem(String content)
        +UUID getId()
        +String getContent()
        +void setContent(String content)
        +LocalDateTime getCreationTime()
        +boolean isHandled()
        +void setHandled(boolean handled)
        +boolean equals(Object obj)
        +int hashCode()
        +String toString()
    }
    
    class TutorialSystem {
        -boolean tutorialCompleted
        -int currentTutorialStep
        -List~TutorialStep~ tutorialSteps
        +TutorialSystem()
        +boolean isTutorialCompleted()
        +void markTutorialAsCompleted()
        +int getCurrentStep()
        +void nextStep()
        +void previousStep()
        +void skipTutorial()
        +void showTutorial()
        +void showPomodoroTechniqueBenefits()
        -initializeTutorialSteps()
    }
    
    class TutorialStep {
        -String title
        -String content
        -String imageResource
        +TutorialStep(String title, String content)
        +TutorialStep(String title, String content, String imageResource)
        +String getTitle()
        +String getContent()
        +String getImageResource()
        +boolean hasImage()
    }
    
  # Analytics
    class ProductivityMetricsCalculator {
        +calculateCompletionRate(List~Task~ tasks) double
        +calculateAverageDailyTasksCompleted(List~Task~ tasks, int days) double
        +calculateAverageFocusSessionsPerDay(List~TimerSession~ sessions, int days) double
        +calculateAverageFocusTimePerDay(List~TimerSession~ sessions, int days) Duration
        +calculateTaskCompletionByCategory(List~Task~ tasks) Map~TaskCategory, Integer~
        +calculateTaskCompletionByPriority(List~Task~ tasks) Map~TaskPriority, Integer~
        +calculateProductivityByDayOfWeek(List~Task~ tasks, List~TimerSession~ sessions) Map~DayOfWeek, Double~
        +calculateProductivityByTimeOfDay(List~Task~ tasks, List~TimerSession~ sessions) Map~Integer, Double~
        +calculateInterruptionFrequency(List~TimerSession~ sessions) double
    }
    
    class ProductivityReport {
        -LocalDateTime startDate
        -LocalDateTime endDate
        -int totalTasksCreated
        -int totalTasksCompleted
        -double completionRate
        -double averageDailyTasksCompleted
        -int totalFocusSessions
        -Duration totalFocusTime
        -double averageFocusSessionsPerDay
        -Duration averageFocusTimePerDay
        -Map~TaskCategory, Integer~ tasksByCategory
        -Map~TaskPriority, Integer~ tasksByPriority
        -Map~DayOfWeek, Double~ productivityByDayOfWeek
        -Map~Integer, Double~ productivityByHour
        -double interruptionFrequency
        +ProductivityReport(LocalDateTime startDate, LocalDateTime endDate)
        // Getters for all fields
        +String getSummary()
    }
    
    class DataPoint {
        -LocalDateTime timestamp
        -double value
        +DataPoint(LocalDateTime timestamp, double value)
        +LocalDateTime getTimestamp()
        +double getValue()
    }
    
  # Repositories
    class TaskRepository {
        <<interface>>
        +save(Task task) Task
        +findById(UUID id) Optional~Task~
        +findAll() List~Task~
        +findByStatus(TaskStatus status) List~Task~
        +findByPriority(TaskPriority priority) List~Task~
        +findByCategory(TaskCategory category) List~Task~
        +findByDueDate(LocalDateTime startDate, LocalDateTime endDate) List~Task~
        +findCompleted() List~Task~
        +findCompletedInDateRange(LocalDateTime startDate, LocalDateTime endDate) List~Task~
        +findOverdue() List~Task~
        +delete(UUID id) boolean
        +saveSubTask(SubTask subTask) SubTask
        +findSubTaskById(UUID id) Optional~SubTask~
        +findSubTasksByParentId(UUID parentId) List~SubTask~
        +deleteSubTask(UUID id) boolean
        +saveCategory(TaskCategory category) TaskCategory
        +findCategoryById(UUID id) Optional~TaskCategory~
        +findAllCategories() List~TaskCategory~
        +deleteCategory(UUID id) boolean
    }
    
    class TimerRepository {
        <<interface>>
        +save(TimerSession session) TimerSession
        +findById(UUID id) Optional~TimerSession~
        +findAll() List~TimerSession~
        +findByCompletionStatus(boolean completed) List~TimerSession~
        +findByDateRange(LocalDateTime start, LocalDateTime end) List~TimerSession~
        +findByAssociatedTaskId(UUID taskId) List~TimerSession~
        +findByDay(LocalDate date) List~TimerSession~
        +delete(UUID id) boolean
    }
    
    class UserRepository {
        <<interface>>
        +save(User user) User
        +findById(UUID id) Optional~User~
        +findByUsername(String username) Optional~User~
        +findByEmail(String email) Optional~User~
        +delete(UUID id) boolean
    }
    
    class AchievementRepository {
        <<interface>>
        +save(Achievement achievement) Achievement
        +findById(UUID id) Optional~Achievement~
        +findAll() List~Achievement~
        +findByType(AchievementType type) List~Achievement~
        +findByUnlockStatus(boolean unlocked) List~Achievement~
        +delete(UUID id) boolean
    }
    
    class QuestRepository {
        <<interface>>
        +save(Quest quest) Quest
        +findById(UUID id) Optional~Quest~
        +findAll() List~Quest~
        +findByType(QuestType type) List~Quest~
        +findByCompletionStatus(boolean completed) List~Quest~
        +findActive() List~Quest~
        +findExpired() List~Quest~
        +delete(UUID id) boolean
    }
    
  # Data Serialization
    class DataSerializer {
        +serialize(Object data, String filePath) boolean
        +deserialize(String filePath, Class<?> type) Object
        +backupData(String sourcePath, String backupPath) boolean
        +restoreBackup(String backupPath, String destinationPath) boolean
        +isValidSerializedData(String filePath, Class<?> expectedType) boolean
    }
    
  # UI Components
    class MainDashboard {
        -TaskListView taskListView
        -TimerDisplay timerDisplay
        -StatisticsSummaryView statisticsView
        +MainDashboard()
        +initializeComponents()
        +refreshData()
    }
    
    class TaskListView {
        -ITaskManagementService taskService
        -List~Task~ currentTasks
        -TaskFilter currentFilter
        -TaskSorter currentSorter
        +TaskListView(ITaskManagementService taskService)
        +initializeView()
        +refreshTaskList()
        +setTaskFilter(TaskFilter filter)
        +setTaskSorter(TaskSorter sorter)
        +onTaskClick(UUID taskId)
        +onAddTaskClick()
        +onEditTaskClick(UUID taskId)
        +onDeleteTaskClick(UUID taskId)
        +onMarkTaskCompletedClick(UUID taskId)
    }
    
    class TimerDisplay {
        -ITimerService timerService
        -IUserService userService
        -TimerState currentState
        -Duration remainingTime
        -int completedSessions
        +TimerDisplay(ITimerService timerService, IUserService userService)
        +initializeDisplay()
        +updateTimerDisplay()
        +onStartButtonClick()
        +onPauseButtonClick()
        +onResumeButtonClick()
        +onResetButtonClick()
        +onStopButtonClick()
        +formatRemainingTime(Duration time)
    }
    
    class AnalyticsDashboard {
        -IAnalyticsService analyticsService
        -LocalDateTime startDate
        -LocalDateTime endDate
        -ProductivityReport currentReport
        +AnalyticsDashboard(IAnalyticsService analyticsService)
        +initializeDashboard()
        +refreshData()
        +setDateRange(LocalDateTime startDate, LocalDateTime endDate)
        +createTaskCompletionChart()
        +createFocusSessionChart()
        +createProductivityByDayChart()
        +createProductivityByTimeChart()
        +createCategoryDistributionChart()
        +exportReportData(String format)
    }
    
    class AchievementsView {
        -IUserService userService
        -List~Achievement~ achievements
        +AchievementsView(IUserService userService)
        +initializeView()
        +refreshAchievements()
        +displayAchievement(Achievement achievement)
        +onAchievementClick(UUID achievementId)
    }
    
    class QuestsView {
        -IUserService userService
        -List~Quest~ activeQuests
        -List~Quest~ completedQuests
        +QuestsView(IUserService userService)
        +initializeView()
        +refreshQuests()
        +displayQuest(Quest quest)
        +onQuestClick(UUID questId)
    }
    
    class SettingsView {
        -IUserService userService
        -UserPreferences currentPreferences
        +SettingsView(IUserService userService)
        +initializeView()
        +refreshSettings()
        +onThemeChange(Theme theme)
        +onTimerDurationChange(Duration workDuration, Duration shortBreakDuration, Duration longBreakDuration)
        +onNotificationSettingsChange(Map~NotificationType, Boolean~ settings)
        +onSoundSettingsChange(Map~SoundType, Boolean~ settings)
        +onSaveSettings()
        +onResetToDefaults()
    }
    
    class TutorialView {
        -IFocusToolsService focusToolsService
        -List~TutorialStep~ tutorialSteps
        -int currentStep
        +TutorialView(IFocusToolsService focusToolsService)
        +initializeView()
        +showTutorial()
        +showPomodoroInfo()
        +onNextStepClick()
        +onPreviousStepClick()
        +onSkipTutorialClick()
        +onCompleteTutorialClick()
    }
    
    class SoundControlsView {
        -IFocusToolsService focusToolsService
        -SoundType currentSound
        -float volume
        +SoundControlsView(IFocusToolsService focusToolsService)
        +initializeView()
        +onSoundTypeChange(SoundType soundType)
        +onVolumeChange(float volume)
        +onPlayButtonClick()
        +onStopButtonClick()
        +onMuteButtonClick()
    }
    
    class DistractionListView {
        -IFocusToolsService focusToolsService
        -List~DistractionItem~ distractionItems
        +DistractionListView(IFocusToolsService focusToolsService)
        +initializeView()
        +refreshDistractionList()
        +onAddDistractionClick()
        +onMarkHandledClick(UUID itemId)
        +onClearListClick()
    }
    
  # Factories and Utilities
    class TaskFactory {
        +createTask(String name, String description) Task
        +createTask(String name, String description, LocalDateTime dueDate) Task
        +createTask(String name, String description, LocalDateTime dueDate, TaskPriority priority) Task
        +createTask(String name, String description, LocalDateTime dueDate, TaskPriority priority, TaskCategory category) Task
        +createSubTask(String name, String description, UUID parentTaskId) SubTask
        +createTaskCategory(String name, String colorCode) TaskCategory
    }
    
    class TimerFactory {
        +createTimer(TimerType type, UserPreferences preferences) Timer
        +createPomodoroTimer(Duration workDuration, Duration shortBreakDuration, Duration longBreakDuration, int sessionsUntilLongBreak) PomodoroTimer
        +createCustomTimer(Duration duration) Timer
        +createTimerSession(TimerType timerType) TimerSession
        +createTimerSession(TimerType timerType, UUID associatedTaskId) TimerSession
    }
    
    class DateTimeUtils {
        <<utility>>
        +static formatDateTime(LocalDateTime dateTime) String
        +static formatDuration(Duration duration) String
        +static isToday(LocalDateTime dateTime) boolean
        +static isTomorrow(LocalDateTime dateTime) boolean
        +static isThisWeek(LocalDateTime dateTime) boolean
        +static daysUntil(LocalDateTime dateTime) long
        +static getStartOfDay(LocalDateTime dateTime) LocalDateTime
        +static getEndOfDay(LocalDateTime dateTime) LocalDateTime
        +static getStartOfWeek(LocalDateTime dateTime) LocalDateTime
        +static getEndOfWeek(LocalDateTime dateTime) LocalDateTime
    }
    
    class ValidationUtils {
        <<utility>>
        +static validateNotNull(Object obj, String fieldName)
        +static validateNotBlank(String value, String fieldName)
        +static validateMinLength(String value, int minLength, String fieldName)
        +static validateMaxLength(String value, int maxLength, String fieldName)
        +static validateFutureDate(LocalDateTime date, String fieldName)
        +static validateEmail(String email, String fieldName)
    }
    
    class FileUtils {
        <<utility>>
        +static createDirectoryIfNotExists(String directoryPath) boolean
        +static fileExists(String filePath) boolean
        +static deleteFile(String filePath) boolean
        +static getFileSize(String filePath) long
        +static getLastModifiedDate(String filePath) LocalDateTime
        +static listFiles(String directoryPath) List~String~
    }
    
  # Application Configuration
    class ConfigurationService {
        -Properties appProperties
        -String configFilePath
        +ConfigurationService(String configFilePath)
        +initialize() boolean
        +getString(String key) String
        +getString(String key, String defaultValue) String
        +getInteger(String key) Integer
        +getInteger(String key, Integer defaultValue) Integer
        +getBoolean(String key) Boolean
        +getBoolean(String key, Boolean defaultValue) Boolean
        +getLong(String key) Long
        +getLong(String key, Long defaultValue) Long
        +getDouble(String key) Double
        +getDouble(String key, Double defaultValue) Double
        +setProperty(String key, String value)
        +saveConfiguration() boolean
        +reloadConfiguration() boolean
        +getDataDirectory() String
        +getBackupDirectory() String
    }
    
    class ApplicationController {
        -ITaskManagementService taskService
        -ITimerService timerService
        -IUserService userService
        -INotificationService notificationService
        -IAnalyticsService analyticsService
        -IFocusToolsService focusToolsService
        -ConfigurationService configService
        -UIManager uiManager
        -boolean isInitialized
        +ApplicationController()
        +initialize() boolean
        +shutdown() boolean
        +getTaskManagementService() ITaskManagementService
        +getTimerService() ITimerService
        +getUserService() IUserService
        +getNotificationService() INotificationService
        +getAnalyticsService() IAnalyticsService
        +getFocusToolsService() IFocusToolsService
        +getConfigurationService() ConfigurationService
        +getUIManager() UIManager
        +saveAllData() boolean
        +loadAllData() boolean
    }
    
    class FocusFlowApp {
        +main(String[] args)
        +initialize() boolean
        +launch()
        +handleCommandLineArguments(String[] args)
        +configureLogging()
        +setupExceptionHandling()
        +showSplashScreen()
        +loadInitialConfiguration()
        +createController() ApplicationController
    }
    
    class UIManager {
        -Stage primaryStage
        -Scene currentScene
        -Map~String, Parent~ viewCache
        +UIManager(Stage primaryStage)
        +initialize() boolean
        +showMainDashboard()
        +showTaskListView()
        +showAnalyticsDashboard()
        +showAchievementsView()
        +showQuestsView()
        +showSettingsView()
        +showTutorialView()
        +showAboutView()
        +showView(String viewName)
        +showDialog(String title, String message)
        +showConfirmDialog(String title, String message) boolean
        +showInputDialog(String title, String message) String
        +showErrorDialog(String title, String message, Exception exception)
        +applyTheme(Theme theme)
    }
    
    class ErrorHandler {
        -Logger logger
        +handleException(Exception exception)
        +handleRuntimeException(RuntimeException exception)
        +handleUIException(Exception exception)
        +handleRepositoryException(Exception exception)
        +logError(String message, Exception exception)
        +showErrorToUser(String message, Exception exception)
        +createErrorReport(Exception exception) String
    }
    

