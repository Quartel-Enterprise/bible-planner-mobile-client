import FirebaseCrashlytics
import Shared

class IosCrashReporter: CrashReporter {
    func setCollectionEnabled(enabled: Bool) {
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(enabled)
    }

    func recordException(throwable: KotlinThrowable) {
        let error = NSError(
            domain: "KotlinException",
            code: 0,
            userInfo: [NSLocalizedDescriptionKey: throwable.description()]
        )
        Crashlytics.crashlytics().record(error: error)
    }
}
