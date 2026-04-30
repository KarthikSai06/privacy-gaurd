import os
import glob
import subprocess
import time
import json
import frida
import pandas as pd
import argparse
from tqdm import tqdm

def run_cmd(cmd):
    subprocess.run(cmd, shell=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

def get_package_name(apk_path):
    result = subprocess.run(f"aapt dump badging {apk_path} | findstr package", shell=True, capture_output=True, text=True)
    if result.stdout:
        # package: name='com.example.app'
        try:
            return result.stdout.split("name='")[1].split("'")[0]
        except:
            return None
    return None

def on_message(message, data, stats_dict):
    if message['type'] == 'send':
        payload = message['payload']
        if payload.get('type') == 'stats':
            stats_dict.update(payload['data'])
    elif message['type'] == 'error':
        print(f"[*] Frida error: {message['description']}")

def run_dynamic_analysis(apk_path, avd_name, frida_script_path, timeout=120):
    pkg_name = get_package_name(apk_path)
    if not pkg_name:
        return None
        
    stats_dict = {
        "audio_record_starts": 0,
        "camera_opens": 0,
        "location_requests": 0,
        "shell_execs": 0,
        "network_bytes_sent": 0
    }

    print(f"[*] Analyzing {pkg_name}...")
    
    # Revert AVD to clean snapshot
    run_cmd(f"adb -e emu avd snapshot load default_boot")
    time.sleep(5) # Wait for UI
    
    # Install APK
    run_cmd(f"adb install -r {apk_path}")
    
    # Start app via Monkey
    run_cmd(f"adb shell monkey -p {pkg_name} -c android.intent.category.LAUNCHER 1")
    time.sleep(3) # Wait for app to launch
    
    # Attach Frida
    try:
        device = frida.get_usb_device()
        pid = device.get_process(pkg_name).pid
        session = device.attach(pid)
        
        with open(frida_script_path, "r") as f:
            script_code = f.read()
            
        script = session.create_script(script_code)
        script.on('message', lambda msg, data: on_message(msg, data, stats_dict))
        script.load()
        
        # Wait for timeout
        time.sleep(timeout)
        
        session.detach()
    except Exception as e:
        print(f"[*] Frida attachment failed: {e}")
        time.sleep(timeout) # Let it run anyway just in case
        
    # Uninstall
    run_cmd(f"adb uninstall {pkg_name}")
    
    features = {
        "sha256": os.path.basename(apk_path).replace(".apk", ""),
        "package": pkg_name
    }
    features.update(stats_dict)
    
    return features

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--malware-dir", default="dataset/apks/malware")
    parser.add_argument("--clean-dir", default="dataset/apks/clean")
    parser.add_argument("--avd-name", default="Pixel_4_API_28", help="Name of AVD to revert snapshot")
    parser.add_argument("--frida-script", default="hook_apis.js")
    parser.add_argument("--output", default="dataset/dynamic_features.csv")
    parser.add_argument("--timeout", type=int, default=120, help="Runtime per app in seconds")
    args = parser.parse_args()

    results = []
    
    malware_apks = glob.glob(os.path.join(args.malware_dir, "*.apk"))
    clean_apks = glob.glob(os.path.join(args.clean_dir, "*.apk"))
    
    all_apks = malware_apks + clean_apks
    print(f"Starting dynamic analysis on {len(all_apks)} APKs...")
    
    for apk in tqdm(all_apks):
        feats = run_dynamic_analysis(apk, args.avd_name, args.frida_script, args.timeout)
        if feats:
            results.append(feats)
            
    if results:
        df = pd.DataFrame(results)
        df.to_csv(args.output, index=False)
        print(f"Saved dynamic features to {args.output}")

if __name__ == "__main__":
    main()
