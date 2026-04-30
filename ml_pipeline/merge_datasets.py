import pandas as pd
import argparse

def main():
    parser = argparse.ArgumentParser(description="Merge static and dynamic features.")
    parser.add_argument("--static", default="dataset/static_features.csv")
    parser.add_argument("--dynamic", default="dataset/dynamic_features.csv")
    parser.add_argument("--output", default="dataset/full_dataset.csv")
    args = parser.parse_args()

    print(f"Loading {args.static}...")
    df_static = pd.read_csv(args.static)
    
    try:
        print(f"Loading {args.dynamic}...")
        df_dynamic = pd.read_csv(args.dynamic)
        
        print("Merging datasets on SHA256...")
        # Left merge to keep static features even if dynamic failed
        df_merged = pd.merge(df_static, df_dynamic, on=["sha256", "package"], how="left")
        
        # Fill missing dynamic features with 0 (assuming no activity was observed)
        dynamic_cols = ["audio_record_starts", "camera_opens", "location_requests", "shell_execs", "network_bytes_sent"]
        for col in dynamic_cols:
            if col in df_merged.columns:
                df_merged[col] = df_merged[col].fillna(0)
                
        df_merged.to_csv(args.output, index=False)
        print(f"Saved merged dataset to {args.output} with shape {df_merged.shape}")
        
    except FileNotFoundError:
        print(f"Could not find dynamic dataset. Copying static to {args.output} directly.")
        # Ensure dynamic cols exist even if 0, so ML model doesn't crash
        dynamic_cols = ["audio_record_starts", "camera_opens", "location_requests", "shell_execs", "network_bytes_sent"]
        for col in dynamic_cols:
            df_static[col] = 0
            
        df_static.to_csv(args.output, index=False)
        print(f"Saved dataset to {args.output} with shape {df_static.shape}")

if __name__ == "__main__":
    main()
