import os
import glob
import pandas as pd
import argparse
from tqdm import tqdm
from androguard.misc import AnalyzeAPK

SUSPICIOUS_PERMISSIONS = [
    "android.permission.RECORD_AUDIO",
    "android.permission.CAMERA",
    "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.ACCESS_COARSE_LOCATION",
    "android.permission.READ_CONTACTS",
    "android.permission.READ_SMS",
    "android.permission.READ_CALL_LOG",
    "android.permission.RECEIVE_SMS",
    "android.permission.SEND_SMS",
    "android.permission.SYSTEM_ALERT_WINDOW",
    "android.permission.BIND_ACCESSIBILITY_SERVICE",
    "android.permission.BIND_DEVICE_ADMIN",
    "android.permission.INTERNET",
    "android.permission.RECEIVE_BOOT_COMPLETED"
]

SUSPICIOUS_API_CALLS = [
    "Landroid/telephony/TelephonyManager;->getDeviceId",
    "Landroid/telephony/TelephonyManager;->getSubscriberId",
    "Landroid/media/MediaRecorder;->start",
    "Landroid/hardware/Camera;->takePicture",
    "Landroid/location/LocationManager;->getLastKnownLocation",
    "Ljava/lang/Runtime;->exec",
    "Ldalvik/system/DexClassLoader;->loadClass"
]

def extract_features(apk_path, label):
    try:
        a, d, dx = AnalyzeAPK(apk_path)
        
        if a is None:
            return None
            
        features = {
            "sha256": a.get_android_manifest_xml().get("package") if a.get_android_manifest_xml() is not None else os.path.basename(apk_path),
            "package": a.get_package(),
            "label": label
        }
        
        # 1. Extract Permissions
        requested_permissions = a.get_permissions()
        for perm in SUSPICIOUS_PERMISSIONS:
            features[f"perm_{perm.split('.')[-1]}"] = 1 if perm in requested_permissions else 0
            
        # 2. Total permissions count
        features["total_permissions"] = len(requested_permissions)
        
        # 3. Extract Suspicious API Calls (if Dex analysis is available)
        if dx is not None:
            for api in SUSPICIOUS_API_CALLS:
                api_name = api.split("->")[1]
                class_name = api.split("->")[0]
                
                # Search for cross-references to the API method
                method_analysis = dx.find_methods(classname=class_name, methodname=api_name)
                count = sum(1 for _ in method_analysis)
                
                features[f"api_{api_name}"] = count
        else:
            for api in SUSPICIOUS_API_CALLS:
                api_name = api.split("->")[1]
                features[f"api_{api_name}"] = 0
                
        # 4. Receivers count
        features["receivers_count"] = len(a.get_receivers())
        
        # 5. Services count
        features["services_count"] = len(a.get_services())
        
        return features
        
    except Exception as e:
        print(f"Error processing {apk_path}: {e}")
        return None

def main():
    parser = argparse.ArgumentParser(description="Extract static features from APKs using Androguard.")
    parser.add_argument("--malware-dir", default="dataset/apks/malware", help="Directory with malware APKs")
    parser.add_argument("--clean-dir", default="dataset/apks/clean", help="Directory with clean APKs")
    parser.add_argument("--output", default="dataset/static_features.csv", help="Output CSV file")
    args = parser.parse_args()

    os.makedirs(os.path.dirname(args.output), exist_ok=True)
    
    results = []
    
    malware_apks = glob.glob(os.path.join(args.malware_dir, "*.apk"))
    clean_apks = glob.glob(os.path.join(args.clean-dir, "*.apk"))
    
    print(f"Found {len(malware_apks)} malware APKs and {len(clean_apks)} clean APKs.")
    
    for apk in tqdm(malware_apks, desc="Processing Malware"):
        feats = extract_features(apk, label=1)
        if feats:
            results.append(feats)
            
    for apk in tqdm(clean_apks, desc="Processing Clean"):
        feats = extract_features(apk, label=0)
        if feats:
            results.append(feats)
            
    if results:
        df = pd.DataFrame(results)
        df.to_csv(args.output, index=False)
        print(f"Extracted {len(results)} samples with {len(df.columns)} features. Saved to {args.output}")
    else:
        print("No features extracted.")

if __name__ == "__main__":
    main()
