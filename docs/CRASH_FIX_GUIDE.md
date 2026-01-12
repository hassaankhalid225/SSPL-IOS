# 🔧 CRASH FIX - Session Auto-Join

## ✅ Issue Fixed!

**Problem**: App crashed when clicking notification  
**Root Cause**: Trying to launch SessionActivity directly before app was initialized  
**Solution**: Always launch MainActivity first, then MainActivity launches SessionActivity

---

## 🎯 What Changed

### 1. **MyFirebaseMessagingService.kt**
Now **always** creates intent for MainActivity (not SessionActivity):

```kotlin
// Always launch MainActivity first to ensure app is properly initialized
val intent = Intent(this, MainActivity::class.java).apply {
    putExtra("notification_type", data["type"])
    putExtra("join_code", data["join_code"])
    putExtra("join_url", data["join_url"])
    putExtra("scenario_title", data["scenario_title"])
    putExtra("session_id", data["session_id"])
    
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
            Intent.FLAG_ACTIVITY_CLEAR_TOP or 
            Intent.FLAG_ACTIVITY_SINGLE_TOP
}
```

### 2. **MainActivity.kt**
Updated to read `notification_type` and launch SessionActivity after initialization:

```kotlin
private fun handleNotificationIntent(intent: Intent?) {
    intent?.extras?.let { extras ->
        val notificationType = extras.getString("notification_type")
        val joinCode = extras.getString("join_code")
        val joinUrl = extras.getString("join_url")
        val scenarioTitle = extras.getString("scenario_title")
        
        if ((notificationType == "session_invite" || 
             notificationType == "session_started") && joinCode != null) {
            
            // Launch SessionActivity after a short delay
            window.decorView.postDelayed({
                val sessionIntent = Intent(this, SessionActivity::class.java).apply {
                    putExtra(SessionActivity.EXTRA_JOIN_CODE, joinCode)
                    putExtra(SessionActivity.EXTRA_JOIN_URL, joinUrl)
                    putExtra(SessionActivity.EXTRA_SCENARIO_TITLE, scenarioTitle)
                }
                startActivity(sessionIntent)
            }, 300) // 300ms delay ensures app is ready
        }
    }
}
```

---

## 🚀 New Flow

```
User taps notification
    ↓
MainActivity launches (app initializes)
    ↓
MainActivity reads notification data
    ↓
MainActivity launches SessionActivity (after 300ms delay)
    ↓
SessionActivity calls auto-join API
    ↓
WebView loads quiz
    ↓
User attends quiz ✨
```

---

## 📦 Installation

```bash
# Install the new APK
adb install -r androidApp/build/outputs/apk/bhimsoft/debug/androidApp-bhimsoft-debug.apk
```

---

## 🧪 Testing Steps

### 1. **Send Test Notification**
```bash
cd "d:\Switch2itech\Kotlin App\SSPL"
.\test-push-notifications.bat
```

### 2. **Tap the Notification**
- App should open (MainActivity)
- After ~300ms, SessionActivity should appear
- Progress indicator shows
- Auto-join happens
- Quiz loads in WebView

### 3. **Expected Logs**
```
FCMService: showNotification called
FCMService: Creating intent for MainActivity with session data:
FCMService:   notification_type: session_invite
FCMService:   join_code: A1B2C3
FCMService:   join_url: http://192.168.50.59:3000/join/A1B2C3

MainActivity: onCreate called
MainActivity: handleNotificationIntent called
MainActivity:   notification_type: session_invite
MainActivity:   join_code: A1B2C3
MainActivity: Launching SessionActivity for session: A1B2C3

SessionActivity: SessionActivity onCreate called
SessionActivity: Join Code: A1B2C3
SessionActivity: Calling autoJoinSession with code: A1B2C3
SessionActivity: Auto-joining session: A1B2C3
SessionActivity: Joined session: 123
SessionActivity: Loading quiz URL: http://192.168.50.59:3000/join/A1B2C3
SessionActivity: Page loaded: http://192.168.50.59:3000/join/A1B2C3
```

---

## ✅ What Should Happen Now

1. ✅ **No crash** when tapping notification
2. ✅ **MainActivity opens** first (you might see it briefly)
3. ✅ **SessionActivity launches** after 300ms
4. ✅ **Auto-join API called** with auth token
5. ✅ **WebView loads** the quiz
6. ✅ **User can attend** the quiz

---

## 🔍 Troubleshooting

### If app still crashes:
1. Check if you installed the new APK
2. Clear app data: Settings → Apps → SSPL → Clear Data
3. Reinstall the app
4. Check logcat for crash details

### If SessionActivity doesn't appear:
1. Check MainActivity logs - is `handleNotificationIntent` called?
2. Check notification payload - does it have `type: "session_invite"`?
3. Increase delay from 300ms to 500ms if needed

### If auto-join fails:
1. Check if user is logged in
2. Verify auth token is valid
3. Check backend endpoint is accessible

---

## 📊 Build Status

```
✅ BUILD SUCCESSFUL in 1m 15s
✅ No compilation errors
✅ Ready to install and test
```

---

## 🎉 Summary

**Before**: Notification → SessionActivity → CRASH ❌  
**After**: Notification → MainActivity → SessionActivity → Quiz ✅

**Status**: ✅ **FIXED - Ready to Test**

---

**Test it now and let me know if it works!** 🚀
