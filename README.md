心宿 · Xinsu Heartrate

Minimal medical HUD style heart rate monitor for Android.

Real-time BLE heart rate visualization with ECG rendering, ambient particles, monitor audio, and immersive health UI.


---

Preview

> OLED black · Neon green · Medical HUD · Ambient motion



A lightweight Android heart rate visualization experience inspired by:

ICU monitoring systems

Cyber-medical HUD interfaces

Apple Health aesthetics

Minimal ambient UI design



---

Features

BLE Heart Rate Monitor

Bluetooth Low Energy heart rate connection

Real-time heart rate synchronization

Automatic reconnect support

Heart rate service filtering


ECG Visualization Engine

Dynamic ECG waveform rendering

Real-time scrolling buffer

Medical monitor inspired grid system

Pulse synchronized animation


Medical HUD Interface

OLED optimized pure black UI

Neon green glow rendering

Minimal medical dashboard layout

Ambient motion system


Audio Engine

Real-time heartbeat monitor sound

Pulse synchronized playback

ICU monitor inspired sound profile

Lightweight PCM synthesis


Dynamic Background System

Floating particle engine

Ambient glow effects

Scanline overlay

Low-frequency breathing animation



---

Design Language

Minimal Medical UI

OLED Black

Neon Green HUD

Calm Interaction Design

Ambient Motion

ICU Monitor Inspired



---

Architecture

core/
bluetooth/
audio/
ecg/
particles/
ui/
settings/
model/
utils/

Core Modules

bluetooth/

BLE scanning, GATT connection, heart rate parsing, and device management.

ecg/

ECG rendering engine, waveform buffer system, grid renderer, and animation logic.

audio/

Heartbeat audio engine, PCM synthesis, and sound profile management.

particles/

Ambient particle rendering and background motion engine.

ui/

HUD widgets, glow effects, pulse animations, and screen components.


---

Planned Features

Real ECG waveform modeling

Historical heart rate statistics

Device battery display

Signal quality visualization

Advanced particle shaders

Adaptive audio profiles

Multi-device support

AMOLED ultra-low-power mode



---

Technology Stack

Kotlin

Android Canvas

Bluetooth LE

Custom View Rendering

AudioTrack PCM Engine

GitHub Actions CI



---

Project Goal

Xinsu Heartrate is not designed as a traditional utility app.

The goal of this project is to explore:

health visualization

ambient medical interfaces

real-time sensor rendering

immersive HUD interaction systems


through native Android rendering and modular architecture.


---

Disclaimer

This project is NOT a medical device.

ECG visualization and heart rate display are simulated or entertainment-oriented visual effects and should not be used for medical diagnosis or treatment.


---

License

MIT License


---

Author

XinWKX
