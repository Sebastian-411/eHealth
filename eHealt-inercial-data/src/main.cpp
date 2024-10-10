#include <ArduinoBLE.h>
#include <Arduino_LSM6DS3.h>
#include <Wire.h>
#include <ArduinoJson.h>


BLEService imuService("12345678-1234-5678-1234-56789abcdef0"); // UUID del servicio
BLECharacteristic imuDataCharacteristic("12345678-1234-5678-1234-56789abcdef1", BLERead | BLENotify, 20); // UUID de la característica

void setup() {
    Serial.begin(9600);

    pinMode(LED_BUILTIN, OUTPUT);
    digitalWrite(LED_BUILTIN, LOW);

    // Iniciar el BLE
    if (!BLE.begin()) {
        Serial.println("Failed to initialize BLE!");
        while (1);
    }

    // Añadir servicio y característica
    BLE.setLocalName("Nano 33 IoT");
    BLE.setAdvertisedService(imuService);
    imuService.addCharacteristic(imuDataCharacteristic);
    BLE.addService(imuService);
    
    BLE.advertise();
    Serial.println("BLE device is now advertising...");

    if (!IMU.begin()) {
      Serial.println("IMU no detectado, revisa las conexiones.");
      while (1);
    }

}

void loop() {
    BLE.poll();

    BLEDevice central = BLE.central();
    if (central) {
        Serial.print("Connected to central: ");
        Serial.println(central.address());

        float accelX, accelY, accelZ;
        float gyroX, gyroY, gyroZ;
        while (central.connected()) {
          if (IMU.accelerationAvailable() && IMU.gyroscopeAvailable()) {
            IMU.readAcceleration(accelX, accelY, accelZ);
            IMU.readGyroscope(gyroX, gyroY, gyroZ);

            digitalWrite(LED_BUILTIN, HIGH);
            StaticJsonDocument<256> jsonDoc;
            jsonDoc["id"] = millis();

            JsonObject accel = jsonDoc.createNestedObject("accelerometer");
            accel["x"] = accelX;
            accel["y"] = accelY;
            accel["z"] = accelZ;

            JsonObject gyro = jsonDoc.createNestedObject("gyroscope");
            gyro["x"] = gyroX;
            gyro["y"] = gyroY;
            gyro["z"] = gyroZ;

            digitalWrite(LED_BUILTIN, LOW);

            String jsonString;
            serializeJson(jsonDoc, jsonString);

            imuDataCharacteristic.writeValue(jsonString.c_str(), jsonString.length());
            Serial.println(jsonString);
            delay(25);
          }
        }
        Serial.print("Disconnected from central: ");
        Serial.println(central.address());

    }
}
