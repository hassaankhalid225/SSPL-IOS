# Debugging Guide - Session Auto-Join Issue

## Issue
When clicking on notification, the app opens but SessionActivity doesn't launch.

## Changes Made

### 1. Added `onNewIntent` to MainActivity
**Problem**: When the app is already running, notifications don't trigger SessionActivity.

**Solution**: Added `onNewIntent` method to handle notifications when app is in foreground.

```kotlin
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    Log.d("MainActivity", "onNewIntent called")
    handleNotificationIntent(intent)
}
```

### 2. Updated `handleNotificationIntent` in MainActivity
**Problem**: MainActivity wasn't launching SessionActivity for session notifications.

**Solution**: Updated to launch SessionActivity for both `session_invite` and `session_started` types.

```kotlin
if ((type == "session_invite" || type == "session_started") && joinCode != null) {
    Log.d("MainActivity", "Launching SessionActivity for session: $joinCode")
    
    val sessionIntent = Intent(this, SessionActivity::class.java).apply {
        putExtra(SessionActivity.EXTRA_JOIN_CODE, extras.getString("join_code"))
        putExtra(SessionActivity.EXTRA_JOIN_URL, extras.getString("join_url"))
        putExtra(SessionActivity.EXTRA_SCENARIO_TITLE, extras.getString("scenario_title"))
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(sessionIntent)
}
```

### 3. Added Comprehensive Debug Logging

**MyFirebaseMessagingService.kt**:
```kotlin
Log.d(TAG, "showNotification called")
Log.d(TAG, "Title: $title")
Log.d(TAG, "Body: $body")
Log.d(TAG, "Data: $data")
Log.d(TAG, "Notification type: $notificationType")
Log.d(TAG, "Creating intent for SessionActivity")
Log.d(TAG, "  JOIN_CODE: ${data["join_code"]}")
Log.d(TAG, "  JOIN_URL: ${data["join_url"]}")
Log.d(TAG, "  SCENARIO_TITLE: ${data["scenario_title"]}")
```

**MainActivity.kt**:
```kotlin
Log.d("MainActivity", "handleNotificationIntent - type: $type, joinCode: $joinCode")
Log.d("MainActivity", "Launching SessionActivity for session: $joinCode")
Log.d("MainActivity", "onNewIntent called")
```

**SessionActivity.kt**:
```kotlin
Log.d(TAG, "SessionActivity onCreate called")
Log.d(TAG, "Intent extras: ${intent.extras}")
Log.d(TAG, "Join Code: $joinCode")
Log.d(TAG, "Join URL: $joinUrl")
Log.d(TAG, "Scenario Title: $scenarioTitle")
Log.d(TAG, "Calling autoJoinSession with code: $joinCode")
```

---

## How to Debug

### Step 1: Install the New Build
```bash
adb install -r androidApp/build/outputs/apk/bhimsoft/debug/androidApp-bhimsoft-debug.apk
```

### Step 2: Clear Logcat
```bash
adb logcat -c
```

### Step 3: Start Logcat Monitoring
```bash
adb logcat | findstr /C:"FCMService" /C:"MainActivity" /C:"SessionActivity"
```

Or filter by tags:
```bash
adb logcat FCMService:D MainActivity:D SessionActivity:D *:S
```

### Step 4: Send Test Notification
Run your test script:
```bash
cd "d:\Switch2itech\Kotlin App\SSPL"
.\test-push-notifications.bat
```

### Step 5: Tap the Notification

### Step 6: Check the Logs

You should see logs in this order:

**1. Notification Received:**
```
FCMService: From: ...
FCMService: Message data payload: {type=session_invite, join_code=A1B2C3, ...}
FCMService: showNotification called
FCMService: Title: 🎯 New Quiz Session Available!
FCMService: Body: ...
FCMService: Data: {type=session_invite, join_code=A1B2C3, ...}
FCMService: Notification type: session_invite
FCMService: Creating intent for SessionActivity
FCMService:   JOIN_CODE: A1B2C3
FCMService:   JOIN_URL: http://192.168.50.59:3000/join/A1B2C3
FCMService:   SCENARIO_TITLE: General Knowledge Quiz
```

**2. Notification Tapped (App Closed):**
```
SessionActivity: SessionActivity onCreate called
SessionActivity: Intent extras: Bundle[...]
SessionActivity: Join Code: A1B2C3
SessionActivity: Join URL: http://192.168.50.59:3000/join/A1B2C3
SessionActivity: Scenario Title: General Knowledge Quiz
SessionActivity: Calling autoJoinSession with code: A1B2C3
SessionActivity: Auto-joining session: A1B2C3
```

**3. Notification Tapped (App Running):**
```
MainActivity: onNewIntent called
MainActivity: handleNotificationIntent - type: session_invite, joinCode: A1B2C3
MainActivity: Launching SessionActivity for session: A1B2C3
SessionActivity: SessionActivity onCreate called
SessionActivity: Intent extras: Bundle[...]
SessionActivity: Join Code: A1B2C3
...
```

---

## Common Issues & Solutions

### Issue 1: No Logs from SessionActivity
**Possible Cause**: SessionActivity not being launched

**Check**:
1. Look for MainActivity logs
2. Check if `handleNotificationIntent` is called
3. Verify notification type is `session_invite` or `session_started`

**Solution**: Verify the notification payload has correct `type` field

---

### Issue 2: "No session information provided" Error
**Possible Cause**: Intent extras not being passed correctly

**Check Logs**:
```
SessionActivity: Join Code: null
SessionActivity: Join URL: null
```

**Solution**: 
1. Check MyFirebaseMessagingService logs to see what data is in the notification
2. Verify the extra keys match:
   - `SessionActivity.EXTRA_JOIN_CODE` = "JOIN_CODE"
   - `SessionActivity.EXTRA_JOIN_URL` = "JOIN_URL"
   - `SessionActivity.EXTRA_SCENARIO_TITLE` = "SCENARIO_TITLE"

---

### Issue 3: MainActivity Opens Instead of SessionActivity
**Possible Cause**: Notification type not matching

**Check**:
```
FCMService: Notification type: <something_else>
FCMService: Creating intent for MainActivity
```

**Solution**: Ensure notification payload has `type: "session_invite"` or `type: "session_started"`

---

### Issue 4: App Opens But Nothing Happens
**Possible Cause**: MainActivity not handling the intent

**Check**:
```
MainActivity: handleNotificationIntent - type: session_invite, joinCode: A1B2C3
```

If you see this but no SessionActivity launch:
- Check if there's an exception
- Verify SessionActivity is registered in AndroidManifest.xml

---

## Test Notification Payload

Make sure your test notification has this structure:

```json
{
  "notification": {
    "title": "🎯 New Quiz Session Available!",
    "body": "General Knowledge Quiz - Join now!"
  },
  "data": {
    "type": "session_invite",
    "join_code": "A1B2C3",
    "join_url": "http://192.168.50.59:3000/join/A1B2C3",
    "scenario_title": "General Knowledge Quiz"
  }
}
```

**IMPORTANT**: The `type` field must be `"session_invite"` or `"session_started"`

---

## Expected Log Flow (Complete)

```
# 1. Notification Received
FCMService: From: projects/...
FCMService: Message data payload: {type=session_invite, join_code=A1B2C3, join_url=http://..., scenario_title=General Knowledge Quiz}
FCMService: showNotification called
FCMService: Title: 🎯 New Quiz Session Available!
FCMService: Body: General Knowledge Quiz - Join now!
FCMService: Data: {type=session_invite, join_code=A1B2C3, join_url=http://..., scenario_title=General Knowledge Quiz}
FCMService: Notification type: session_invite
FCMService: Creating intent for SessionActivity
FCMService:   JOIN_CODE: A1B2C3
FCMService:   JOIN_URL: http://192.168.50.59:3000/join/A1B2C3
FCMService:   SCENARIO_TITLE: General Knowledge Quiz

# 2. User Taps Notification
SessionActivity: SessionActivity onCreate called
SessionActivity: Intent extras: Bundle[{JOIN_CODE=A1B2C3, JOIN_URL=http://..., SCENARIO_TITLE=General Knowledge Quiz}]
SessionActivity: Join Code: A1B2C3
SessionActivity: Join URL: http://192.168.50.59:3000/join/A1B2C3
SessionActivity: Scenario Title: General Knowledge Quiz
SessionActivity: Calling autoJoinSession with code: A1B2C3

# 3. Auto-Join Process
SessionActivity: Auto-joining session: A1B2C3
SessionActivity: Joined session: 123
SessionActivity: Participant: John Doe
SessionActivity: Loading quiz URL: http://192.168.50.59:3000/join/A1B2C3

# 4. WebView Loading
SessionActivity: Page loading started: http://192.168.50.59:3000/join/A1B2C3
SessionActivity: Page loaded: http://192.168.50.59:3000/join/A1B2C3
```

---

## Quick Checklist

- [ ] New APK installed
- [ ] Logcat running with correct filters
- [ ] Test notification sent
- [ ] Notification appears in notification tray
- [ ] Notification tapped
- [ ] Check logs for "SessionActivity onCreate called"
- [ ] Check logs for "Join Code: A1B2C3" (not null)
- [ ] Check logs for "Calling autoJoinSession"
- [ ] Check logs for "Auto-joining session"
- [ ] Check logs for "Loading quiz URL"
- [ ] WebView appears with quiz

---

## If Still Not Working

1. **Share the complete logcat output** from when you tap the notification
2. **Check the notification payload** - what `type` value is being sent?
3. **Verify AndroidManifest.xml** - is SessionActivity registered?
4. **Check if app is in foreground or background** when notification is tapped

---

**Build Status**: ✅ Successful  
**Changes**: MainActivity updated, debug logging added  
**Next Step**: Install APK and check logs
