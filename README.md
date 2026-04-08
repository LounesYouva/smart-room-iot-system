# 🏠 Smart Room IoT System

Smart Room IoT System is a full-stack IoT project designed to monitor
and control a connected environment in real time.

The system integrates hardware (Arduino + sensors), a backend (Spring
Boot), a communication bridge (Python), and a desktop dashboard
(JavaFX).

------------------------------------------------------------------------

## 🚀 Features

-   🌡️ Real-time temperature monitoring
-   💧 Humidity monitoring
-   💡 Light control (ON / OFF)
-   🔄 REST communication between frontend and backend
-   🔌 Serial communication with Arduino
-   🖥️ Desktop dashboard (JavaFX)

------------------------------------------------------------------------

## 🏗️ Architecture Overview

JavaFX Dashboard\
↓ (HTTP REST API)\
Spring Boot Backend\
↓ (HTTP / polling)\
Python Serial Bridge\
↓ (Serial USB)\
Arduino (Sensors + Actuators)

------------------------------------------------------------------------

## 📁 Project Structure

smart-room-iot-system/ ├── backend/ ├── dashboard/ ├── Arduino/ ├──
SerialBridge/

------------------------------------------------------------------------

## 🛠️ Technologies Used

-   Java 17
-   JavaFX
-   Spring Boot
-   Python 3
-   Arduino (C/C++)
-   REST API
-   Jackson (JSON parsing)
-   Serial communication

------------------------------------------------------------------------

## ⚙️ Installation & Setup

### Clone

git clone https://github.com/LounesYouva/smart-room-iot-system.git cd
smart-room-iot-system

------------------------------------------------------------------------

## Backend

cd backend mvn spring-boot:run

------------------------------------------------------------------------

## Python Bridge

pip install pyserial requests

cd SerialBridge python testBridge.py

------------------------------------------------------------------------

## Arduino

Open Arduino/sketch_mar12a.ino and upload to board.

------------------------------------------------------------------------

## Dashboard

cd dashboard mvn javafx:run

------------------------------------------------------------------------

## 💡 What I Learned

-   Full IoT architecture
-   JavaFX UI
-   REST APIs
-   JSON parsing
-   Serial communication

------------------------------------------------------------------------

## 📄 License

MIT License
