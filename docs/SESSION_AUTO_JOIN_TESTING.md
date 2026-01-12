# Session Auto-Join Testing Guide

## Quick Test Steps

### Prerequisites
✅ Build successful  
✅ Device/Emulator with Google Play Services  
✅ Firebase project configured  
✅ Backend server running with auto-join endpoint  

---

## Test 1: Push Notification Reception

### Steps:
1. Install the app on your device
2. Login to the app (to get authentication token)
3. Have an admin create a quiz session on the backend
4. Verify you receive a push notification

### Expected Result:
```
Notification Title: "🎯 New Quiz Session Available!"
Notification Body: "General Knowledge Quiz - Aapko invite kiya ja raha hai! Join karein."
```

### Verify:
- [ ] Notification appears in notification tray
- [ ] Notification has correct title and body
- [ ] Notification icon is visible

---

## Test 2: Tap Notification - Auto Join

### Steps:
1. Tap the received notification
2. Observe the app behavior

### Expected Result:
1. SessionActivity launches
2. Progress indicator shows briefly
3. Auto-join API is called
4. WebView loads the quiz session
5. User is automatically joined (no name/email prompt)

### Verify:
- [ ] SessionActivity opens
- [ ] No name/email input screen shown
- [ ] Quiz loads in WebView
- [ ] User can interact with quiz

### Logs to Check:
```
SessionActivity: Auto-joining session: A1B2C3
SessionActivity: Joined session: 123
SessionActivity: Participant: John Doe
SessionActivity: Loading quiz URL: http://192.168.50.59:3000/join/A1B2C3
WebView: Page loaded: http://192.168.50.59:3000/join/A1B2C3
```

---

## Test 3: Already Joined Scenario

### Steps:
1. Join a session (using Test 2)
2. Receive another notification for the same session
3. Tap the notification again

### Expected Result:
- App should load the same session
- No duplicate participant created
- User sees their existing session state

### Verify:
- [ ] Session loads successfully
- [ ] No error shown
- [ ] User's previous progress is maintained

---

## Test 4: Error Handling - Unauthorized (401)

### Steps:
1. Logout from the app (clear auth token)
2. Tap a session notification

### Expected Result:
```
Toast: "Please login to join the session"
Activity closes after 2 seconds
```

### Verify:
- [ ] Error message shown
- [ ] Activity closes automatically
- [ ] No crash occurs

---

## Test 5: Error Handling - Session Not Found (404)

### Steps:
1. Create a notification with invalid join code
2. Tap the notification

### Expected Result:
```
Toast: "Session not found. Please check the join code."
Activity closes after 2 seconds
```

### Verify:
- [ ] Error message shown
- [ ] Activity closes automatically
- [ ] No crash occurs

---

## Test 6: Error Handling - Session Expired (400)

### Steps:
1. Create a session and let it expire
2. Tap the notification

### Expected Result:
```
Toast: "This session has expired."
Activity closes after 2 seconds
```

### Verify:
- [ ] Error message shown
- [ ] Activity closes automatically
- [ ] No crash occurs

---

## Test 7: WebView Functionality

### Steps:
1. Successfully join a session
2. Interact with the quiz in WebView

### Verify:
- [ ] JavaScript works correctly
- [ ] Can answer quiz questions
- [ ] Can navigate within the quiz
- [ ] Back button works (navigates within WebView)
- [ ] Progress is saved
- [ ] Real-time updates work (via Pusher)

---

## Test 8: Network Error Handling

### Steps:
1. Turn off WiFi/Mobile data
2. Tap a session notification

### Expected Result:
```
Toast: "Failed to join session. Please try again."
Activity closes after 2 seconds
```

### Verify:
- [ ] Error message shown
- [ ] No crash occurs
- [ ] Graceful error handling

---

## Test 9: Back Button Navigation

### Steps:
1. Join a session successfully
2. Navigate within the quiz (multiple pages)
3. Press back button multiple times

### Expected Result:
- First presses: Navigate back within WebView
- Final press: Close SessionActivity

### Verify:
- [ ] Back button navigates within WebView
- [ ] Final back press closes activity
- [ ] No crash occurs

---

## Test 10: Notification Types

### Test both notification types:

#### A. session_invite
```json
{
  "type": "session_invite",
  "join_code": "A1B2C3",
  "join_url": "http://192.168.50.59:3000/join/A1B2C3",
  "scenario_title": "General Knowledge Quiz"
}
```

#### B. session_started
```json
{
  "type": "session_started",
  "join_code": "A1B2C3",
  "join_url": "http://192.168.50.59:3000/join/A1B2C3",
  "scenario_title": "General Knowledge Quiz"
}
```

### Verify:
- [ ] Both types launch SessionActivity
- [ ] Both types auto-join successfully
- [ ] WebView loads correctly for both

---

## Manual Testing with cURL

### Test Auto-Join API Directly

```bash
# Replace with your actual token and join code
curl -X POST http://192.168.50.59:3000/api/sessions/A1B2C3/auto-join \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### Expected Response:
```json
{
  "id": 456,
  "session_id": 123,
  "name": "John Doe",
  "email": "john@example.com",
  "joined_at": "2026-01-09T15:30:00.000Z",
  "join_url": "http://192.168.50.59:3000/join/A1B2C3"
}
```

---

## Send Test Push Notification

### Using Firebase Console:
1. Go to Firebase Console → Cloud Messaging
2. Click "Send test message"
3. Add your device FCM token
4. Use this payload:

```json
{
  "notification": {
    "title": "🎯 New Quiz Session Available!",
    "body": "Test Quiz - Aapko invite kiya ja raha hai! Join karein."
  },
  "data": {
    "type": "session_invite",
    "join_code": "TEST123",
    "join_url": "http://192.168.50.59:3000/join/TEST123",
    "scenario_title": "Test Quiz"
  }
}
```

### Using test-push-notifications.bat:
```bash
cd "d:\Switch2itech\Kotlin App\SSPL"
.\test-push-notifications.bat
```

---

## Debugging Tips

### Enable Verbose Logging:
```kotlin
// In SessionActivity.kt, add more logs
Log.d(TAG, "Intent extras: ${intent.extras}")
Log.d(TAG, "Join code: $joinCode")
Log.d(TAG, "Join URL: $joinUrl")
```

### Check Logcat Filters:
```
SessionActivity
FCMService
AutoJoinSessionUseCase
```

### WebView Debugging:
1. Enable WebView debugging in SessionActivity:
```kotlin
WebView.setWebContentsDebuggingEnabled(true)
```

2. Open Chrome DevTools:
   - Navigate to `chrome://inspect`
   - Find your WebView
   - Click "Inspect"

---

## Common Issues & Solutions

### Issue: Notification not received
**Solution:** 
- Check FCM token is registered
- Verify Firebase project configuration
- Check device has internet connection

### Issue: Auto-join fails with 401
**Solution:**
- Verify user is logged in
- Check auth token is valid
- Ensure token is being sent in header

### Issue: WebView shows blank page
**Solution:**
- Check join_url is valid
- Verify JavaScript is enabled
- Check network connectivity
- Enable WebView debugging

### Issue: App crashes on notification tap
**Solution:**
- Check SessionActivity is registered in manifest
- Verify all required extras are present
- Check Koin DI is initialized

---

## Performance Checks

- [ ] App launches quickly from notification
- [ ] Auto-join completes in < 2 seconds
- [ ] WebView loads smoothly
- [ ] No memory leaks
- [ ] Battery usage is normal

---

## Checklist Summary

### Core Functionality:
- [ ] Notification received
- [ ] Notification tap opens SessionActivity
- [ ] Auto-join works without manual input
- [ ] WebView loads quiz
- [ ] User can complete quiz

### Error Handling:
- [ ] 401 Unauthorized handled
- [ ] 404 Not Found handled
- [ ] 400 Bad Request handled
- [ ] Network errors handled
- [ ] No crashes on errors

### User Experience:
- [ ] Smooth navigation
- [ ] Clear error messages
- [ ] Back button works correctly
- [ ] Progress indicator shows during loading
- [ ] Activity closes gracefully on errors

---

**Testing Date**: _____________  
**Tested By**: _____________  
**Device/Emulator**: _____________  
**Android Version**: _____________  
**App Version**: _____________  

**Overall Status**: ⬜ PASS | ⬜ FAIL  
**Notes**: _____________________________________________
