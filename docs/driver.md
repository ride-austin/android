#Android driver application

##Components

[Service Diagram](https://drive.google.com/file/d/1Wgjw_5SR7rUIJyOY5zhmAoZ6CKkSTFDO/view?usp=sharing)

####1. EngineService

Entry point which starts main application's components.  
When driver is online or in ride, application will keep `EngineService` alive (using `startForeground` and `STICKY` mode).  
When driver is offline, service will run no longer than any visible activity.

####2. StateManager

Application uses state machine to manage driver states.  

`StateManager`'s responsibilities are:

* switching states 
* synchronizing with server state 
* utilizing server events

`StateManager` lifecycle is bound to `EngineService`

####3. EngineState

Driver state representation eg. `OnlineState`, `PendingAcceptState`, `AcceptedState` etc.  
Each state is responsible for actions specific to this state:

* using local and third-party services (location, tracking, direction)
* invoking server API requests (go online, accept ride, etc)
* caching data during network failures

`EngineState` can be treated as data model.
 
####4. UIStrategy

Each state has corresponding UI strategy, which reflects data changes on screen.  
`UIStrategy` is bound to Android lifecycle and is only active during `onStart`/`onStop`.

####5. Managers

There is a bunch of specialized managers which names are mostly self-explaining:

* `LongPollingManager` listens to LP events
* `PendingEventsManager` works with cached data which must be sent when connection restored
* `ConfigurationManager` updates configuration based on location
* `DriverLocationManager` sends location updates based on current state and configuration settings
* `RideRequestManager` provides data about request types which driver is eligible to work with
* `AirportQueueManager` keeps track of driver's position in airport queue
* `PrefManager` persists user data
* `AppNotificationManager` manages device notifications and in-app messages
* `ConnectionStateManager` listens to network and server reachability
* `DataManager` stores session data and encapsulates networking
