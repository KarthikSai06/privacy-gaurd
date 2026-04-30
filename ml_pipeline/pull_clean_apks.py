import os
import subprocess
import argparse
from tqdm import tqdm

def run_cmd(cmd):
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    return result.stdout.strip()

def main():
    parser = argparse.ArgumentParser(description="Pull clean APKs from a connected Android device via ADB.")
    parser.add_argument("--dest-dir", default="dataset/apks/clean", help="Destination directory")
    args = parser.parse_args()

    os.makedirs(args.dest_dir, exist_ok=True)

    print("Checking ADB devices...")
    devices = run_cmd("adb devices")
    devices_lines = devices.split('\n')
    if len(devices_lines) < 2 or "device" not in devices_lines[1]:
        print(f"No ADB device found or ADB error.\nADB Output:\n{devices}\nPlease connect a device or start an emulator.")
        return

    print("Fetching list of installed third-party packages...")
    packages_output = run_cmd("adb shell pm list packages -3")
    packages = [line.replace("package:", "") for line in packages_output.split("\n") if line.startswith("package:")]
    
    print(f"Found {len(packages)} third-party packages.")

    success_count = 0
    for pkg in tqdm(packages, desc="Pulling APKs"):
        try:
            # Get the path to the APK on the device
            path_output = run_cmd(f"adb shell pm path {pkg}")
            if not path_output:
                print(f"\nCould not find path for {pkg}")
                continue
            
            # The output could be multiple lines (split APKs), we take the first base.apk
            apk_path = path_output.split("\n")[0].replace("package:", "")
            
            dest_file = os.path.join(args.dest_dir, f"{pkg}.apk")
            
            # Pull the APK
            pull_result = run_cmd(f"adb pull {apk_path} {dest_file}")
            if "pulled" in pull_result or "1 file pulled" in pull_result:
                success_count += 1
            else:
                print(f"\nFailed to pull {pkg}: {pull_result}")
        except Exception as e:
            print(f"\nError pulling {pkg}: {e}")

    print(f"\nFinished. Successfully pulled {success_count}/{len(packages)} clean APKs.")

if __name__ == "__main__":
    main()
