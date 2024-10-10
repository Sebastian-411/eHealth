#include <Arduino_LSM6DS3.h>
#include <ArduinoBLE.h>

BLEService imuService("180D"); // UUID de ejemplo para el servicio
BLECharacteristic imuDataCharacteristic("2A37", BLERead | BLENotify, 24); // Un solo característico para todos los datos

void setup() {
  Serial.begin(9600);
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, LOW);

  // Inicializar BLE
  if (!BLE.begin()) {
    Serial.println("Iniciando Bluetooth® Low Energy fallido!");
    while (1);
  }

  BLE.setLocalName("IMU Sensor");
  BLE.setAdvertisedService(imuService);
  imuService.addCharacteristic(imuDataCharacteristic);
  BLE.addService(imuService);
  BLE.advertise();

  Serial.println("Bluetooth® device active, waiting for connections...");

  // Inicializar IMU
  if (!IMU.begin()) {
    Serial.println("IMU no detectado, revisa las conexiones.");
    while (1);
  }
  Serial.println("IMU iniciado correctamente.");
}

void loop() {
  BLEDevice central = BLE.central(); // Esperar conexión del central

  if (central) {
    Serial.print("Conectado a central: ");
    Serial.println(central.address());

    while (central.connected()) {
      float ax, ay, az;
      float gx, gy, gz;

      if (IMU.accelerationAvailable() && IMU.gyroscopeAvailable()) {
        IMU.readAcceleration(ax, ay, az);
        IMU.readGyroscope(gx, gy, gz);

        // Crear una cadena de datos en formato adecuado
        String imuData = String(ax) + "," + String(ay) + "," + String(az) + "," +
                         String(gx) + "," + String(gy) + "," + String(gz);

        // Enviar datos a través de BLE
        imuDataCharacteristic.setValue(imuData.c_str()); // Cambiar a formato de cadena

        // Parpadeo del LED para indicar actividad
        digitalWrite(LED_BUILTIN, HIGH);
        delay(100);
        digitalWrite(LED_BUILTIN, LOW);
        delay(100);

        // Mostrar datos en el Serial Monitor
        Serial.print("Aceleración - X: ");
        Serial.print(ax);
        Serial.print(", Y: ");
        Serial.print(ay);
        Serial.print(", Z: ");
        Serial.println(az);
        Serial.print("Giroscopio - X: ");
        Serial.print(gx);
        Serial.print(", Y: ");
        Serial.print(gy);
        Serial.print(", Z: ");
        Serial.println(gz);
      }
    }

    Serial.print("Desconectado de central: ");
    Serial.println(central.address());
  }
}