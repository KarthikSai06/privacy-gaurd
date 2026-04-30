import os
import requests
import argparse
import time
from tqdm import tqdm

ANDROZOO_API_URL = "https://androzoo.uni.lu/api/download"

def download_apk(sha256, api_key, dest_dir):
    try:
        url = f"{ANDROZOO_API_URL}?apikey={api_key}&sha256={sha256}"
        response = requests.get(url, stream=True, timeout=30)
        
        if response.status_code == 200:
            file_path = os.path.join(dest_dir, f"{sha256}.apk")
            total_size = int(response.headers.get('content-length', 0))
            
            with open(file_path, 'wb') as f, tqdm(
                desc=sha256[:8],
                total=total_size,
                unit='iB',
                unit_scale=True,
                unit_divisor=1024,
            ) as bar:
                for data in response.iter_content(chunk_size=1024):
                    size = f.write(data)
                    bar.update(size)
            return True
        else:
            print(f"Failed to download {sha256}: HTTP {response.status_code}")
            return False
    except Exception as e:
        print(f"Error downloading {sha256}: {e}")
        return False

def main():
    parser = argparse.ArgumentParser(description="Download malware APKs from AndroZoo.")
    parser.add_argument("--api-key", required=True, help="AndroZoo API Key")
    parser.add_argument("--hash-file", required=True, help="Text file containing SHA256 hashes (one per line)")
    parser.add_argument("--dest-dir", default="dataset/apks/malware", help="Destination directory")
    parser.add_argument("--delay", type=float, default=1.0, help="Delay between downloads (seconds)")
    args = parser.parse_args()

    os.makedirs(args.dest_dir, exist_ok=True)

    with open(args.hash_file, 'r') as f:
        hashes = [line.strip() for line in f if line.strip()]

    print(f"Loaded {len(hashes)} hashes from {args.hash_file}.")
    
    success_count = 0
    for sha256 in hashes:
        print(f"Downloading {sha256}...")
        if download_apk(sha256, args.api_key, args.dest_dir):
            success_count += 1
        time.sleep(args.delay)

    print(f"Finished. Successfully downloaded {success_count}/{len(hashes)} APKs.")

if __name__ == "__main__":
    main()
