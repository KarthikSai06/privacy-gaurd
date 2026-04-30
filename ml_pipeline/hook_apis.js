// Frida hook script to monitor suspicious API calls during dynamic analysis

var dynamicFeatures = {
    "audio_record_starts": 0,
    "camera_opens": 0,
    "location_requests": 0,
    "shell_execs": 0,
    "network_bytes_sent": 0
};

// Send data back to python orchestrator every 5 seconds
setInterval(function() {
    send({type: "stats", data: dynamicFeatures});
}, 5000);

Java.perform(function () {
    console.log("[*] Frida Hooks injected successfully");

    // Hook AudioRecord.startRecording
    try {
        var AudioRecord = Java.use('android.media.AudioRecord');
        AudioRecord.startRecording.overload().implementation = function () {
            dynamicFeatures["audio_record_starts"]++;
            console.log("[*] AudioRecord started");
            return this.startRecording();
        };
    } catch(err) { console.log("AudioRecord hook failed: " + err); }

    // Hook Camera.open
    try {
        var Camera = Java.use('android.hardware.Camera');
        Camera.open.overload('int').implementation = function (cameraId) {
            dynamicFeatures["camera_opens"]++;
            console.log("[*] Camera.open called");
            return this.open(cameraId);
        };
        Camera.open.overload().implementation = function () {
            dynamicFeatures["camera_opens"]++;
            console.log("[*] Camera.open called");
            return this.open();
        };
    } catch(err) { console.log("Camera hook failed: " + err); }
    
    // Hook CameraDevice (Camera2 API)
    try {
        var CameraManager = Java.use('android.hardware.camera2.CameraManager');
        CameraManager.openCamera.overload('java.lang.String', 'android.hardware.camera2.CameraDevice$StateCallback', 'android.os.Handler').implementation = function(cameraId, callback, handler) {
            dynamicFeatures["camera_opens"]++;
            console.log("[*] CameraManager.openCamera called");
            return this.openCamera(cameraId, callback, handler);
        };
    } catch(err) { console.log("Camera2 hook failed: " + err); }

    // Hook LocationManager.requestLocationUpdates
    try {
        var LocationManager = Java.use('android.location.LocationManager');
        LocationManager.requestLocationUpdates.overload('java.lang.String', 'long', 'float', 'android.location.LocationListener').implementation = function(provider, minTime, minDistance, listener) {
            dynamicFeatures["location_requests"]++;
            console.log("[*] LocationManager.requestLocationUpdates called");
            return this.requestLocationUpdates(provider, minTime, minDistance, listener);
        };
    } catch(err) { console.log("LocationManager hook failed: " + err); }

    // Hook Runtime.exec
    try {
        var Runtime = Java.use('java.lang.Runtime');
        Runtime.exec.overload('java.lang.String').implementation = function(cmd) {
            dynamicFeatures["shell_execs"]++;
            console.log("[*] Runtime.exec called: " + cmd);
            return this.exec(cmd);
        };
    } catch(err) { console.log("Runtime hook failed: " + err); }
    
    // Hook URLConnection for outgoing traffic (simplified proxy for trafficstats)
    try {
        var URL = Java.use('java.net.URL');
        URL.openConnection.overload().implementation = function() {
            // Roughly estimate network activity
            dynamicFeatures["network_bytes_sent"] += 100;
            return this.openConnection();
        };
    } catch(err) { console.log("URL hook failed: " + err); }
});
