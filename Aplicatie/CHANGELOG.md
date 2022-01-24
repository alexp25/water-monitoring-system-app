## Change log
This changelog tracks changes done by Mihai Draghici in addition to initial developer's work (Ioan-Alexandru Chirita).

Here are functional explanations of changes. For detailed code changes please see GIT commits.

### Water Monitoring System - Android App

#### Changeset #1
SupplierSensormapActivity
* [x] added 'addSensorMode' which is toggled by a right-bottom FAB
* [x] short press action adds a sensor only if 'addSensorMode' is true
* [x] saved map's camera position in SharedPreferences onPause and onDestroy (lat, lon, zoom)
* [x] restored map's camera position if former position is available 
* [x] added "Reset camera" FAB which animates camera to a position in such way all available markers will fit in screen

#### Changeset #2
SupplierSensorMapActivity & sensors_module_info_content.xml
* [x] onMarkerClick opens sensors' popup with info (not on long click)
* [x] added SensorId on popup info view
* [x] sensors without data (channels) are no longer showed on the map



### Caveats
* API endpoints over HTTP
* authentication missing
* requests do not return per-client data
* code should be refactored completly