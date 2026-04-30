import pandas as pd
import numpy as np
import json
import argparse
import os
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import classification_report, roc_auc_score, precision_score, recall_score, f1_score
from sklearn.utils import class_weight
import tensorflow as tf
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, Dropout, Input
from tensorflow.keras.callbacks import EarlyStopping

def main():
    parser = argparse.ArgumentParser(description="Train ML model for Spyware Detection and export to TFLite.")
    parser.add_argument("--dataset", default="dataset/full_dataset.csv")
    parser.add_argument("--tflite-out", default="../app/src/main/assets/spyware_detector.tflite")
    parser.add_argument("--scaler-out", default="../app/src/main/assets/scaler_params.json")
    args = parser.parse_args()

    print(f"Loading dataset from {args.dataset}...")
    try:
        df = pd.read_csv(args.dataset)
    except Exception as e:
        print(f"Failed to load dataset: {e}")
        print("Creating a dummy dataset aligned with Android native features...")
        np.random.seed(42)
        dummy_size = 500
        df = pd.DataFrame({
            "sha256": [f"dummy_{i}" for i in range(dummy_size)],
            "package": [f"com.dummy.{i}" for i in range(dummy_size)],
            "label": np.random.randint(0, 2, dummy_size),
            "perm_RECORD_AUDIO": np.random.randint(0, 2, dummy_size),
            "perm_CAMERA": np.random.randint(0, 2, dummy_size),
            "perm_ACCESS_FINE_LOCATION": np.random.randint(0, 2, dummy_size),
            "total_permissions": np.random.randint(1, 50, dummy_size),
            "receivers_count": np.random.randint(0, 10, dummy_size),
            "services_count": np.random.randint(0, 20, dummy_size),
            "audio_record_starts": np.random.randint(0, 10, dummy_size),
            "camera_opens": np.random.randint(0, 10, dummy_size),
            "location_requests": np.random.randint(0, 10, dummy_size),
            "network_bytes_sent": np.random.randint(0, 1000000, dummy_size),
            "night_activity_count": np.random.randint(0, 5, dummy_size),
            "trigger_count": np.random.randint(0, 3, dummy_size),
            "is_keylogger": np.random.randint(0, 2, dummy_size)
        })
        
        # Spyware profile
        df.loc[df['label'] == 1, 'is_keylogger'] = np.random.choice([0, 1], p=[0.2, 0.8], size=(df['label'] == 1).sum())
        df.loc[df['label'] == 1, 'night_activity_count'] += np.random.randint(0, 5, size=(df['label'] == 1).sum())
        df.loc[df['label'] == 1, 'trigger_count'] += np.random.randint(0, 3, size=(df['label'] == 1).sum())
        df.loc[df['label'] == 1, 'network_bytes_sent'] += 500000

    # Drop non-feature columns
    feature_cols = [
        "perm_RECORD_AUDIO", "perm_CAMERA", "perm_ACCESS_FINE_LOCATION", 
        "total_permissions", "receivers_count", "services_count", 
        "audio_record_starts", "camera_opens", "location_requests", 
        "network_bytes_sent", "night_activity_count", "trigger_count", "is_keylogger"
    ]
    
    # Ensure dataset has all required columns
    for col in feature_cols:
        if col not in df.columns:
            df[col] = 0

    X = df[feature_cols].values.astype(np.float32)
    y = df["label"].values.astype(np.float32)
    
    print(f"Dataset shape: X={X.shape}, y={y.shape}")

    # Train/Test Split
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42, stratify=y)

    # Normalize features
    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)

    # Save scaler parameters to JSON
    os.makedirs(os.path.dirname(args.scaler_out), exist_ok=True)
    scaler_params = {
        "mean": scaler.mean_.tolist(),
        "scale": scaler.scale_.tolist(),
        "features": feature_cols
    }
    with open(args.scaler_out, "w") as f:
        json.dump(scaler_params, f, indent=4)
    print(f"Saved scaler parameters to {args.scaler_out}")

    # Class weights
    classes = np.unique(y_train)
    weights = class_weight.compute_class_weight(class_weight="balanced", classes=classes, y=y_train)
    class_weights = {classes[i]: weights[i] for i in range(len(classes))}

    # Keras MLP
    model = Sequential([
        Input(shape=(len(feature_cols),)),
        Dense(64, activation='relu'),
        Dropout(0.3),
        Dense(32, activation='relu'),
        Dropout(0.2),
        Dense(1, activation='sigmoid')
    ])

    model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy', tf.keras.metrics.AUC(name='auc')])

    print("Starting training...")
    early_stop = EarlyStopping(monitor='val_auc', patience=10, mode='max', restore_best_weights=True)
    
    model.fit(
        X_train_scaled, y_train,
        validation_split=0.2,
        epochs=100,
        batch_size=32,
        class_weight=class_weights,
        callbacks=[early_stop],
        verbose=1
    )

    print("\nEvaluating on test set...")
    y_pred_prob = model.predict(X_test_scaled)
    y_pred = (y_pred_prob > 0.5).astype(int)
    
    auc = roc_auc_score(y_test, y_pred_prob)
    f1 = f1_score(y_test, y_pred, zero_division=0)
    print(f"AUC-ROC:  {auc:.4f}")
    print(f"F1-Score: {f1:.4f}")

    # Export to TFLite
    print(f"\nExporting model to TFLite...")
    os.makedirs(os.path.dirname(args.tflite_out), exist_ok=True)
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    tflite_model = converter.convert()
    
    with open(args.tflite_out, "wb") as f:
        f.write(tflite_model)
    print(f"Successfully saved TFLite model to {args.tflite_out}")

if __name__ == "__main__":
    main()
