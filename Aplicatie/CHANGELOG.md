## Change log
This changelog tracks changes done by Mihai Draghici in addition to initial developer's work (Ioan-Alexandru Chirita).

Here are functional explanations of changes. For detailed code changes please see GIT commits.

### Water Monitoring System - Android App

#### Changeset 1
SupplierSensormapActivity
* [x] added 'addSensorMode' which is toggled by a right-bottom FAB
* [x] short press action adds a sensor only if 'addSensorMode' is true
* [x] saved map's camera position in SharedPreferences onPause and onDestroy (lat, lon, zoom)
* [x] restored map's camera position if former position is available 
* [x] added "Reset camera" FAB which animates camera to a position in such way all available markers will fit in screen

#### Changeset 2
* [x] onMarkerClick opens sensors' popup with info (not on long click)
* [x] added SensorId on popup info view
* [x] sensors without data (channels) are no longer showed on the map

#### Changeset 3
* [x] Handle sensors with same coords:let GoogleMaps handle that; open sensor channels on InfoBox click and not on marker click 
* [x] Added `Toggle view` button to allow displaying of sensors without data channels (usefull if you want to change the customer code for a sensor which do not have data channels yet)
* [x] Added EditText for customer code `SensorsModuleInfoActivity` to allow updating the Sensor's Customer Code; SMIActivity sends a response back to Main activity to update map marker and local sensor data


#### Changeset 4
* [x] 3.addendum: should fill the available width (otherwise it's hard to tap on it) OR should have an edit button to the right
* [x] 3.addendum: should not be limited to 8 chars
* [x] input labels out of bound (SensorsChannelInfoActivity - limit label, LoginFragment, RegisterFragment)
* [x] bug when no data points are available in SensorsChannelInfoActivity
* [x] Log out not working under some circumstances (note: the app's navigation is poorly implemented; the fix is working, but definetly not the best practice)
* [x] Removed Navigation Drawer from SensorsChannelInfoActivity and SensorsModuleInfoActivity
* [x] Added popup to confirm customer code change
* [x] Customer Account - view attached sensors on the map: added new menu option
* [x] Adding clusters of markers on map


#### Changeset 5
* [ ] Test and fix add new sensor activity - it is not added right now


### Caveats
* API endpoints over HTTP
* authentication missing
* requests do not return per-client data
* code should be refactored completly
* database Customer Code update result is not checked while updating local app data (this resolves at first Map refresh)
