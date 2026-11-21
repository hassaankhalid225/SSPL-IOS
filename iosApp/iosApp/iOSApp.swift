import SwiftUI
import shared

@main
struct iOSApp: App {

    @UIApplicationDelegateAdaptor(AppDelegate.self)
    var appDelegate
    
    @StateObject private var notificationManager = NotificationManager.shared

    var body: some Scene {
        WindowGroup {
            ContentView()
                // Deep links → shared navigation (DeepLinkBus). TODO: register the `sspl` URL scheme
                // (CFBundleURLTypes) and Associated Domains (applinks:ssplahore.com) in Info.plist / entitlements.
                .onOpenURL { url in
                    _ = DeepLinkBus.shared.handle(uri: url.absoluteString)
                }
                .fullScreenCover(isPresented: $notificationManager.showWebView) {
                    if let joinUrl = notificationManager.joinUrl {
                        SessionWebViewScreen(joinUrl: joinUrl)
                            .onDisappear {
                                notificationManager.closeSession()
                            }
                    }
                }
        }
    }
}
