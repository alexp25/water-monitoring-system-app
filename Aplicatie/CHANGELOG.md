## Change log
This changelog tracks changes done by Mihai Draghici in addition to initial developer's work (Ioan-Alexandru Chirita).

Here are functional explanations of changes. For detailed code changes please see GIT commits.

### Water Monitoring System - Android App

#### Changeset #1
SupplierSensormapActivity
* added 'addSensorMode' which is toggled by a right-bottom FAB
* short press action adds a sensor only if 'addSensorMode' is true
* saved map's camera position in SharedPreferences onPause and onDestroy (lat, lon, zoom)
* restored map's camera position if former position is available 
* added "Reset camera" FAB which animates camera to a position in such way all available markers will fit in screen
