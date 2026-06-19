import FirebaseAnalytics
import Shared

class IosAnalyticsService: AnalyticsService {
    func setUserProperty(name: String, value: String?) {
        Analytics.setUserProperty(value, forName: name)
    }
}
