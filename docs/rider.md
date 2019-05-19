#Android rider application

##Components

[Service Diagram](https://drive.google.com/file/d/1rAKAyIGHA4Pk85nkn6dI1WpKTYQy_QGt/view?usp=sharing)

####1. RideStatusService

Foreground service which listens to ride status changes.
Application will keep `RideStatusService` alive (using `startForeground` and `STICKY` mode) during the ride.  
When there is no active ride service is shut down.    

####2. StateManager

Keeps track of current ride and unrated ride (if any).  
Exposes reactive API for publish-subscribe pattern. Model in MVVM.   
`StateManager` is bound to Android `Application`'s lifecycle.

####3. DataManager

Stores session data and encapsulates networking.  
Exposes reactive API for publish-subscribe pattern. Model in MVVM.  
`DataManager` is bound to Android `Application`'s lifecycle.

####4. Representation

UI-related part uses MVVM pattern with data binding.  
Views and ViewModels both bound to Android `Activity`/`Fragment` lifecycle.  
Views are recreated on configuration changes, ViewModels - mostly not.  
Approach is similar to [Android Architecture Components](https://developer.android.com/topic/libraries/architecture/index.html)
 
####5. Managers

There is a bunch of specialized managers which names are mostly self-explaining:

* `ConfigurationManager` updates configuration based on location
* `LocationManager` provides location updates
* `PrefManager` persists user data
* `AppNotificationManager` manages device notifications and in-app messages
* `ConnectionStateManager` listens to network and server reachability