# Mobile App Integration Guide - Session Notifications & Auto-Join

## Overview

This guide explains how to integrate session notifications and auto-join functionality in the mobile app. When an admin creates a quiz session, users receive a push notification. Tapping the notification automatically joins them to the session using their authenticated profile (name and email from token), and loads the quiz in an iframe/WebView.

---

## Complete User Flow

```
┌─────────┐     ┌──────────┐     ┌──────────┐     ┌────────┐     ┌───────┐
│ Admin   │────▶│  Server  │────▶│ Firebase │────▶│ Mobile │────▶│WebView│
│         │     │          │     │          │     │  App   │     │       │
└─────────┘     └──────────┘     └──────────┘     └────────┘     └───────┘
    │                │                 │               │              │
    │ Create         │                 │               │              │
    │ Session        │                 │               │              │
    │                │                 │               │              │
    │                │ Send Push       │               │              │
    │                │ Notification    │               │              │
    │                │────────────────▶│               │              │
    │                │                 │               │              │
    │                │                 │ Notification  │              │
    │                │                 │ Received      │              │
    │                │                 │──────────────▶│              │
    │                │                 │               │              │
    │                │                 │               │ User Taps    │
    │                │                 │               │ Notification │
    │                │                 │               │              │
    │                │ Auto-Join       │               │              │
    │                │ (POST /sessions/:joinCode/auto-join)           │
    │                │◀────────────────────────────────│              │
    │                │                 │               │              │
    │                │ Extract user    │               │              │
    │                │ info from token │               │              │
    │                │                 │               │              │
    │                │ Return          │               │              │
    │                │ participant +   │               │              │
    │                │ join_url        │               │              │
    │                │─────────────────────────────────▶│              │
    │                │                 │               │              │
    │                │                 │               │ Load         │
    │                │                 │               │ join_url     │
    │                │                 │               │─────────────▶│
    │                │                 │               │              │
    │                │                 │               │              │ User
    │                │                 │               │              │ Attends
    │                │                 │               │              │ Quiz
```

---

## Step-by-Step Implementation

### Step 1: Receive Push Notification

When a session is created, the mobile app receives a Firebase push notification with the following payload:

#### Notification Payload

```json
{
  "notification": {
    "title": "🎯 New Quiz Session Available!",
    "body": "\"General Knowledge Quiz\" - Aapko invite kiya ja raha hai! Join karein."
  },
  "data": {
    "type": "session_invite",
    "join_code": "A1B2C3",
    "join_url": "http://192.168.50.59:3000/join/A1B2C3",
    "scenario_title": "General Knowledge Quiz"
  }
}
```

#### Kotlin Example (Android)

```kotlin
class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        
        if (data["type"] == "session_invite") {
            val joinCode = data["join_code"] ?: return
            val joinUrl = data["join_url"] ?: return
            val scenarioTitle = data["scenario_title"] ?: ""
            
            // Show notification to user
            showNotification(
                title = remoteMessage.notification?.title ?: "New Quiz Session",
                body = remoteMessage.notification?.body ?: "Join now!",
                joinCode = joinCode,
                joinUrl = joinUrl
            )
        }
    }
    
    private fun showNotification(
        title: String, 
        body: String, 
        joinCode: String, 
        joinUrl: String
    ) {
        val intent = Intent(this, SessionActivity::class.java).apply {
            putExtra("JOIN_CODE", joinCode)
            putExtra("JOIN_URL", joinUrl)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
    }
}
```

#### Swift Example (iOS)

```swift
func userNotificationCenter(
    _ center: UNUserNotificationCenter,
    didReceive response: UNNotificationResponse,
    withCompletionHandler completionHandler: @escaping () -> Void
) {
    let userInfo = response.notification.request.content.userInfo
    
    if let type = userInfo["type"] as? String, type == "session_invite" {
        let joinCode = userInfo["join_code"] as? String ?? ""
        let joinUrl = userInfo["join_url"] as? String ?? ""
        
        // Navigate to session screen
        navigateToSession(joinCode: joinCode, joinUrl: joinUrl)
    }
    
    completionHandler()
}
```

---

### Step 2: Auto-Join Session (When User Taps Notification)

When the user taps the notification, call the auto-join endpoint with their authentication token. The server will extract the user's name and email from the token automatically.

#### API Endpoint: `POST /sessions/:joinCode/auto-join`

**Request:**

```http
POST http://192.168.50.59:3000/api/sessions/A1B2C3/auto-join
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

> **IMPORTANT:** No request body needed! The server extracts user information (name, email) from the authentication token automatically.

**Response (Success - 201 Created):**

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

**Response (Already Joined - 201 Created):**

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

> **NOTE:** If the user already joined the session, the endpoint returns the existing participant record with the join URL. No duplicate participants are created.

#### Kotlin Example

```kotlin
class SessionActivity : AppCompatActivity() {
    
    private lateinit var webView: WebView
    private val apiService: ApiService by inject() // Your API service
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)
        
        webView = findViewById(R.id.webView)
        setupWebView()
        
        val joinCode = intent.getStringExtra("JOIN_CODE") ?: return
        autoJoinSession(joinCode)
    }
    
    private fun autoJoinSession(joinCode: String) {
        lifecycleScope.launch {
            try {
                // Get auth token from secure storage
                val authToken = getAuthToken()
                
                // Call auto-join endpoint
                val response = apiService.autoJoinSession(joinCode, authToken)
                
                // Load the join URL in WebView
                val joinUrl = response.join_url
                webView.loadUrl(joinUrl)
                
                Log.d("Session", "Joined session: ${response.session_id}")
                Log.d("Session", "Participant: ${response.name}")
                
            } catch (e: Exception) {
                Log.e("Session", "Failed to join session", e)
                showError("Failed to join session: ${e.message}")
            }
        }
    }
    
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }
        
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false // Load all URLs in WebView
            }
        }
    }
}

// API Service Interface
interface ApiService {
    @POST("sessions/{joinCode}/auto-join")
    suspend fun autoJoinSession(
        @Path("joinCode") joinCode: String,
        @Header("Authorization") authToken: String
    ): AutoJoinResponse
}

data class AutoJoinResponse(
    val id: Int,
    val session_id: Int,
    val name: String,
    val email: String,
    val joined_at: String,
    val join_url: String
)
```

#### Swift Example

```swift
import WebKit

class SessionViewController: UIViewController {
    
    private var webView: WKWebView!
    private let apiService = APIService.shared
    
    var joinCode: String?
    var joinUrl: String?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        setupWebView()
        
        if let joinCode = joinCode {
            autoJoinSession(joinCode: joinCode)
        }
    }
    
    private func setupWebView() {
        let webConfiguration = WKWebViewConfiguration()
        webView = WKWebView(frame: .zero, configuration: webConfiguration)
        webView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(webView)
        
        NSLayoutConstraint.activate([
            webView.topAnchor.constraint(equalTo: view.topAnchor),
            webView.bottomAnchor.constraint(equalTo: view.bottomAnchor),
            webView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            webView.trailingAnchor.constraint(equalTo: view.trailingAnchor)
        ])
    }
    
    private func autoJoinSession(joinCode: String) {
        guard let authToken = getAuthToken() else {
            showError("Not authenticated")
            return
        }
        
        apiService.autoJoinSession(joinCode: joinCode, authToken: authToken) { result in
            switch result {
            case .success(let response):
                DispatchQueue.main.async {
                    if let url = URL(string: response.join_url) {
                        let request = URLRequest(url: url)
                        self.webView.load(request)
                    }
                    print("Joined session: \(response.session_id)")
                    print("Participant: \(response.name)")
                }
            case .failure(let error):
                DispatchQueue.main.async {
                    self.showError("Failed to join: \(error.localizedDescription)")
                }
            }
        }
    }
}

// API Service
class APIService {
    static let shared = APIService()
    private let baseURL = "http://192.168.50.59:3000/api"
    
    func autoJoinSession(
        joinCode: String,
        authToken: String,
        completion: @escaping (Result<AutoJoinResponse, Error>) -> Void
    ) {
        let url = URL(string: "\(baseURL)/sessions/\(joinCode)/auto-join")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("Bearer \(authToken)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            
            guard let data = data else {
                completion(.failure(NSError(domain: "", code: -1)))
                return
            }
            
            do {
                let response = try JSONDecoder().decode(AutoJoinResponse.self, from: data)
                completion(.success(response))
            } catch {
                completion(.failure(error))
            }
        }.resume()
    }
}

struct AutoJoinResponse: Codable {
    let id: Int
    let session_id: Int
    let name: String
    let email: String
    let joined_at: String
    let join_url: String
}
```

---

### Step 3: Load Quiz in WebView/Iframe

After successfully joining the session, load the `join_url` in a WebView (mobile) or iframe (web).

**The join URL format:** `http://192.168.50.59:3000/join/{joinCode}`

#### WebView Configuration

**Android:**

```kotlin
private fun setupWebView() {
    webView.settings.apply {
        javaScriptEnabled = true          // Required for quiz functionality
        domStorageEnabled = true          // Required for local storage
        databaseEnabled = true            // Required for IndexedDB
        loadWithOverviewMode = true
        useWideViewPort = true
        setSupportZoom(false)             // Disable zoom for better UX
    }
    
    // Handle page navigation
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Log.d("WebView", "Page loaded: $url")
        }
        
        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            Log.e("WebView", "Error loading page: ${error?.description}")
        }
    }
}
```

**iOS:**

```swift
private func setupWebView() {
    let webConfiguration = WKWebViewConfiguration()
    
    // Enable JavaScript
    webConfiguration.preferences.javaScriptEnabled = true
    
    // Allow inline media playback
    webConfiguration.allowsInlineMediaPlayback = true
    
    webView = WKWebView(frame: .zero, configuration: webConfiguration)
    webView.navigationDelegate = self
}

extension SessionViewController: WKNavigationDelegate {
    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        print("Page loaded successfully")
    }
    
    func webView(
        _ webView: WKWebView,
        didFail navigation: WKNavigation!,
        withError error: Error
    ) {
        print("Failed to load page: \(error.localizedDescription)")
    }
}
```

---

## Authentication Flow

The auto-join endpoint uses the authentication token to identify the user. Here's how it works:

1. **User logs in** → Receives JWT token
2. **Token contains:** `user_id`, `email`, `name`, etc.
3. **Auto-join endpoint** → Extracts user info from token
4. **No need to ask user for name/email again**

### Token Storage (Recommended)

- **Android:** Use `EncryptedSharedPreferences` or `KeyStore`
- **iOS:** Use `Keychain`

#### Example Token Retrieval

**Kotlin:**

```kotlin
private fun getAuthToken(): String {
    val sharedPreferences = getSharedPreferences("auth", Context.MODE_PRIVATE)
    return "Bearer ${sharedPreferences.getString("auth_token", "")}"
}
```

**Swift:**

```swift
private func getAuthToken() -> String? {
    // Retrieve from Keychain
    let query: [String: Any] = [
        kSecClass as String: kSecClassGenericPassword,
        kSecAttrAccount as String: "auth_token",
        kSecReturnData as String: true
    ]
    
    var result: AnyObject?
    let status = SecItemCopyMatching(query as CFDictionary, &result)
    
    guard status == errSecSuccess,
          let data = result as? Data,
          let token = String(data: data, encoding: .utf8) else {
        return nil
    }
    
    return "Bearer \(token)"
}
```

---

## Error Handling

### Common Errors

| Status Code | Error | Solution |
|-------------|-------|----------|
| 401 | Authentication required | User not logged in. Redirect to login. |
| 404 | Session not found | Invalid join code. Show error message. |
| 400 | Session expired | Session has expired. Show error message. |
| 500 | Internal server error | Server issue. Retry or contact support. |

### Example Error Handling

```kotlin
try {
    val response = apiService.autoJoinSession(joinCode, authToken)
    webView.loadUrl(response.join_url)
} catch (e: HttpException) {
    when (e.code()) {
        401 -> {
            // Redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        404 -> {
            showError("Session not found. Please check the join code.")
        }
        400 -> {
            showError("This session has expired.")
        }
        else -> {
            showError("Failed to join session. Please try again.")
        }
    }
}
```

---

## Testing Checklist

- [ ] Notification received when admin creates session
- [ ] Notification tapped opens the app
- [ ] Auto-join API called with auth token
- [ ] No name/email prompt shown to user
- [ ] WebView loads the quiz successfully
- [ ] User can answer questions in the quiz
- [ ] Session state syncs in real-time (via Pusher)
- [ ] Already joined scenario works (no duplicate participants)

---

## Complete Flow Example (Kotlin)

```kotlin
// 1. Receive notification
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        if (data["type"] == "session_invite") {
            showNotification(
                joinCode = data["join_code"]!!,
                joinUrl = data["join_url"]!!,
                title = remoteMessage.notification?.title ?: "New Quiz"
            )
        }
    }
}

// 2. User taps notification → Auto-join
class SessionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)
        
        val joinCode = intent.getStringExtra("JOIN_CODE")!!
        autoJoinAndLoadQuiz(joinCode)
    }
    
    private fun autoJoinAndLoadQuiz(joinCode: String) {
        lifecycleScope.launch {
            try {
                // Auto-join (name/email from token)
                val response = apiService.autoJoinSession(
                    joinCode = joinCode,
                    authToken = getAuthToken()
                )
                
                // Load quiz in WebView
                webView.loadUrl(response.join_url)
                
            } catch (e: Exception) {
                showError("Failed to join: ${e.message}")
            }
        }
    }
}
```

---

## Summary

1. **Admin creates session** → Users receive push notification
2. **User taps notification** → App calls `POST /sessions/:joinCode/auto-join` with auth token
3. **Server extracts user info from token** (no name/email input needed)
4. **Server returns `join_url`** → App loads it in WebView
5. **User attends quiz** in the WebView

### Key Points

✅ **No manual name/email entry required**  
✅ **Authentication token provides all user info**  
✅ **One-tap join from notification to quiz**  
✅ **Works for both new and existing participants**

---

## Additional Resources

- [Firebase Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging)
- [Android WebView Guide](https://developer.android.com/guide/webapps/webview)
- [iOS WKWebView Documentation](https://developer.apple.com/documentation/webkit/wkwebview)
- [JWT Authentication Best Practices](https://jwt.io/introduction)

---

**Last Updated:** 2026-01-09  
**Version:** 1.0.0
