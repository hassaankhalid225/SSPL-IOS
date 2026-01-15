# Mobile Banners Implementation - SSPL Android App

## Overview

This document describes the implementation of the banner display feature in the SSPL Kotlin Multiplatform mobile application. Banners are displayed in a popup dialog when the app opens, showing active banners from the server.

## Architecture

The implementation follows the existing SSPL app architecture with clean separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                         App.kt                               │
│  (Fetches banners on launch & shows BannerDialog)           │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   GetBannersUseCase                          │
│         (Business logic & error handling)                    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   BannerRepository                           │
│              (API communication layer)                       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    BannerRequest                             │
│              (Ktor resource definition)                      │
└─────────────────────────────────────────────────────────────┘
```

## Files Created/Modified

### 1. **Banner.kt** - Data Model
**Location:** `shared/src/commonMain/kotlin/com/sspl/core/models/Banner.kt`

```kotlin
@Serializable
data class Banner(
    val id: Int,
    val title: String? = null,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("org_id") val orgId: Int? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class BannersResponse(
    val banners: List<Banner>
)
```

### 2. **BannerRequest.kt** - API Resource
**Location:** `shared/src/commonMain/kotlin/com/sspl/core/apis/BannerRequest.kt`

Defines the `/banners` endpoint using Ktor resources.

### 3. **BannerRepository.kt** - Repository Layer
**Location:** `shared/src/commonMain/kotlin/com/sspl/core/repositories/BannerRepository.kt`

Handles API communication to fetch banners with authentication.

### 4. **GetBannersUseCase.kt** - Use Case
**Location:** `shared/src/commonMain/kotlin/com/sspl/core/usecases/GetBannersUseCase.kt`

Wraps repository calls with `ApiStates` for proper state management.

### 5. **BannerDialog.kt** - UI Component
**Location:** `shared/src/commonMain/kotlin/com/sspl/ui/components/BannerDialog.kt`

**Features:**
- Displays banners in a Material3 dialog
- Horizontal pager for multiple banners
- Page indicators for navigation
- Loading states with CircularProgressIndicator
- Error handling with fallback UI
- Close button to dismiss
- Responsive design with proper aspect ratio
- Smooth animations

### 6. **App.kt** - Integration
**Location:** `shared/src/commonMain/kotlin/com/sspl/App.kt`

**Changes:**
- Added banner state management
- Fetches banners when user is authenticated
- Shows BannerDialog when active banners are available
- Silently fails if banner fetch fails (non-critical feature)

### 7. **Dependency Injection Updates**

**RepositoryModules.kt:**
```kotlin
factoryOf(::BannerRepository)
```

**UseCaseModules.kt:**
```kotlin
factory { GetBannersUseCase(get()) }
```

## API Integration

### Endpoint
```
GET /banners/
```

### Headers
```
Authorization: Bearer <token>
Content-Type: application/json
```

### Response Format
```json
{
  "banners": [
    {
      "id": 1,
      "title": "Welcome Banner",
      "image_url": "/uploads/banners/123/456_789.jpg",
      "is_active": true,
      "org_id": 123,
      "created_at": "2023-01-01T00:00:00.000Z",
      "updated_at": "2023-01-01T00:00:00.000Z"
    }
  ]
}
```

## User Flow

1. **App Launch**
   - User opens the app
   - App checks authentication status

2. **Banner Fetch** (if authenticated)
   - `GetBannersUseCase` is invoked
   - API call made to `/banners/` endpoint
   - Response parsed into `Banner` objects

3. **Banner Display**
   - Only active banners (`isActive = true`) are shown
   - If banners exist, `BannerDialog` is displayed
   - User can swipe through multiple banners
   - User can dismiss by:
     - Tapping the close button
     - Tapping outside the dialog
     - Pressing back button

4. **Error Handling**
   - If banner fetch fails, error is logged
   - App continues normal operation (banners are non-critical)
   - No error message shown to user

## UI/UX Features

### BannerDialog Component

**Layout:**
- Full-width dialog (90% of screen width)
- Rounded corners (16dp)
- Material3 design
- Elevated card with shadow

**Single Banner:**
- Image displayed with 16:9 aspect ratio
- Optional title below image
- Close button in top-right corner

**Multiple Banners:**
- Horizontal pager for swiping
- Page indicators at bottom
- Smooth transitions between pages

**Loading State:**
- Circular progress indicator while image loads
- Centered in image area

**Error State:**
- Error container with message
- "Failed to load banner" text
- Material3 error colors

### Image Handling

- Uses Coil3 for async image loading
- Proper content scaling (Fit)
- Lazy loading with placeholders
- Error fallback UI
- Automatic caching

## Configuration

### Base URL
Currently hardcoded in `App.kt`:
```kotlin
baseUrl = "https://bhimsoft.com/sspl/api"
```

**Note:** This matches the base URL in `HttpClientConfigurations.kt`. For production, consider:
- Moving to a configuration file
- Using environment variables
- Implementing build variants

## Testing Checklist

- [ ] Banner displays on app launch (authenticated users)
- [ ] Single banner shows correctly
- [ ] Multiple banners can be swiped
- [ ] Page indicators update correctly
- [ ] Close button dismisses dialog
- [ ] Back button dismisses dialog
- [ ] Tap outside dismisses dialog
- [ ] Loading state shows while fetching
- [ ] Error state handled gracefully
- [ ] Images load correctly
- [ ] Titles display when available
- [ ] Works with different image sizes
- [ ] Works with different screen sizes
- [ ] No crash if API fails
- [ ] No banner shown if none active

## Future Enhancements

1. **Analytics**
   - Track banner impressions
   - Track banner interactions
   - A/B testing support

2. **Caching**
   - Cache banner data locally
   - Reduce API calls
   - Offline support

3. **Click Actions**
   - Add clickable banners
   - Deep linking support
   - External URL opening

4. **Customization**
   - Banner display frequency
   - "Don't show again" option
   - User preferences

5. **Animation**
   - Auto-advance banners
   - Transition effects
   - Entry/exit animations

6. **Performance**
   - Image optimization
   - Lazy loading improvements
   - Memory management

## Troubleshooting

### Banners Not Showing

1. **Check authentication**
   - Banners only show for authenticated users
   - Verify `userSession.token` is not null

2. **Check API response**
   - Verify `/banners/` endpoint is working
   - Check server logs for errors
   - Ensure banners have `is_active = true`

3. **Check network**
   - Verify device can reach server
   - Check base URL is correct
   - Verify firewall/proxy settings

### Images Not Loading

1. **Check image URLs**
   - Verify `image_url` paths are correct
   - Ensure images exist on server
   - Check file permissions

2. **Check base URL**
   - Verify base URL matches server
   - Ensure protocol (http/https) is correct

3. **Check Coil configuration**
   - Verify image loader is initialized
   - Check for Coil errors in logs

## Dependencies

The banner feature uses:
- **Ktor** - HTTP client for API calls
- **Kotlinx Serialization** - JSON parsing
- **Compose** - UI framework
- **Coil3** - Image loading
- **Koin** - Dependency injection
- **Material3** - UI components

## Security Considerations

1. **Authentication**
   - All banner requests require valid JWT token
   - Token automatically included via `HttpClient` configuration

2. **Image URLs**
   - Validate image URLs before loading
   - Use HTTPS in production
   - Implement content security policy

3. **Data Validation**
   - Validate banner data from API
   - Handle malformed responses gracefully
   - Sanitize user-facing content

## Performance Considerations

1. **API Calls**
   - Single API call on app launch
   - Cached by Ktor if configured
   - Fails silently if network unavailable

2. **Image Loading**
   - Async loading with Coil
   - Automatic memory management
   - Disk caching enabled

3. **UI Rendering**
   - Lazy composition
   - Efficient recomposition
   - Minimal overdraw

## Build Information

**Build Status:** ✅ Successful

**Build Time:** 3m 50s

**Tasks Executed:** 64 actionable tasks
- 11 executed
- 7 from cache
- 46 up-to-date

**Warnings:**
- Kotlin Multiplatform compatibility warning (non-critical)
- Deprecated API usage in SessionActivity (non-critical)

## Contact

For questions or issues related to the banner feature:
- Check the implementation files listed above
- Review API documentation
- Contact the backend team for server-side issues
