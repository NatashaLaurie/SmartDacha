#include <Arduino.h>
#if defined(ESP32)
#include <WiFi.h>
#elif defined(ESP8266)
#include <ESP8266WiFi.h>
#endif
#include <Firebase_ESP_Client.h>
#include <OneWire.h>
#include <DallasTemperature.h>

// Provide the token generation process info.
#include "addons/TokenHelper.h"
// Provide the RTDB payload printing info and other helper functions.
#include "addons/RTDBHelper.h"

// Insert your network credentials
#define WIFI_SSID "WIFI_SSID"
#define WIFI_PASSWORD "WIFI_PASSWORD"

// Insert Authorized Username and Corresponding Password
#define USER_EMAIL "USER_EMAIL"
#define USER_PASSWORD "USER_PASSWORD"

// Insert Firebase project API Key
#define API_KEY "API_KEY"

// Insert RTDB URLefine the RTDB URL */
#define DATABASE_URL "DATABASE_URL"

// Define Firebase objects
FirebaseData stream;
FirebaseAuth auth;
FirebaseConfig config;
// Define Firebase Data object
FirebaseData fbdo;
unsigned long myTimer1, myTimer2;

// Variables to save database paths
String listenerPath = "heating/switch";

// Declare outputs
const int relay = 12;
const int led = 14;

#define ONE_WIRE_BUS 0               
OneWire oneWire(ONE_WIRE_BUS);       
DallasTemperature sensors(&oneWire); 

float temperature = 0;
float requiredTemperature = 0;
bool GLOBAL_ON = false;
bool IS_PAUSED = false;
bool FIRST_WIFI_CONNECTION = false;

void setTemperature()
{
  float tmp = 85.0;
  do
  {
    sensors.requestTemperatures();
    tmp = sensors.getTempCByIndex(0);
    Serial.print(": ");
    Serial.println(temperature);
  } while (tmp == 85.0 || tmp == (-127.0));

  temperature = tmp;

  if (Firebase.RTDB.setFloat(&fbdo, "temperature", temperature))
  {
    Serial.println("PASSED");
    Serial.println("PATH: " + fbdo.dataPath());
    Serial.println("TYPE: " + fbdo.dataType());
  }
  else
  {
    Serial.println("FAILED");
    Serial.println("REASON: " + fbdo.errorReason());
  }
}

void setReadyStatus(bool status)
{

  if (Firebase.RTDB.setBool(&fbdo, "redyStatus", status))
  {
    Serial.println("PASSED");
    Serial.println("PATH: " + fbdo.dataPath());
    Serial.println("TYPE: " + fbdo.dataType());
  }
  else
  {
    Serial.println("FAILED");
    Serial.println("REASON: " + fbdo.errorReason());
  }
}

void readRequiredTemp()
{
  if (Firebase.RTDB.getInt(&fbdo, "heating/requiredTemperature"))
  {
    if (fbdo.dataType() == "int")
    {
      requiredTemperature = fbdo.intData();
      Serial.print("READ: ");
      Serial.println(requiredTemperature);
    }
  }
  else
  {
    Serial.println(fbdo.errorReason());
  }
}

void turnOnHeating(int state)
{

  if (state)
  {
    readRequiredTemp();
    GLOBAL_ON = true;
    digitalWrite(relay, 1);
    digitalWrite(led, 1);
  }
  else
  {
    GLOBAL_ON = false;
    IS_PAUSED = false;
    setReadyStatus(false);
    digitalWrite(relay, 0);
    digitalWrite(led, 0);
  }
}

// Callback function that runs on database changes
void streamCallback(FirebaseStream data)
{
  Serial.printf("stream path, %s\nevent path, %s\ndata type, %s\nevent type, %s\n\n",
                data.streamPath().c_str(),
                data.dataPath().c_str(),
                data.dataType().c_str(),
                data.eventType().c_str());
  printResult(data); // see addons/RTDBHelper.h
  Serial.println();

  // Get the path that triggered the function
  String streamPath = String(data.dataPath());

  // if the data returned is an integer, there was a change on the GPIO state on the following path /{gpio_number}
  if (data.dataTypeEnum() == fb_esp_rtdb_data_type_integer)
  {
    String mode = streamPath.substring(1);
    int state = data.intData();
    Serial.print("Mode: ");
    Serial.println(mode);
    Serial.print("STATE: ");
    Serial.println(state);
    turnOnHeating(state);
  }

  /* When it first runs, it is triggered on the root (/) path and returns a JSON with all keys
  and values of that path. So, we can get all values from the database and updated the GPIO states*/
  if (data.dataTypeEnum() == fb_esp_rtdb_data_type_json)
  {
    FirebaseJson json = data.to<FirebaseJson>();

    // To iterate all values in Json object
    size_t count = json.iteratorBegin();
    Serial.println("\n---------");
    for (size_t i = 0; i < count; i++)
    {
      FirebaseJson::IteratorValue value = json.valueAt(i);
      String mode = value.key;
      int state = value.value.toInt();
      Serial.print("STATE: ");
      Serial.println(state);
      Serial.print("MODE:");
      Serial.println(mode);
      turnOnHeating(state);
      Serial.printf("Name: %s, Value: %s, Type: %s\n", value.key.c_str(), value.value.c_str(), value.type == FirebaseJson::JSON_OBJECT ? "object" : "array");
    }
    Serial.println();
    json.iteratorEnd(); // required for free the used memory in iteration (node data collection)
  }

  // This is the size of stream payload received (current and max value)
  // Max payload size is the payload size under the stream path since the stream connected
  // and read once and will not update until stream reconnection takes place.
  // This max value will be zero as no payload received in case of ESP8266 which
  // BearSSL reserved Rx buffer size is less than the actual stream payload.
  Serial.printf("Received stream payload size: %d (Max. %d)\n\n", data.payloadLength(), data.maxPayloadLength());
}

void streamTimeoutCallback(bool timeout)
{
  if (timeout)
    Serial.println("stream timeout, resuming...\n");
  if (!stream.httpConnected())
    Serial.printf("error code: %d, reason: %s\n\n", stream.httpCode(), stream.errorReason().c_str());
}

void extraOff()
{
  digitalWrite(relay, 0);
  digitalWrite(led, 0);
  GLOBAL_ON = false;
  IS_PAUSED = false;
}

// Initialize WiFi
int WiFiCon()
{
  // Check if we have a WiFi connection, if we don't, connect.
  int xCnt = 0;

  if (WiFi.status() != WL_CONNECTED)
  {

    Serial.println();
    Serial.println();
    Serial.print("Connecting to ");
    Serial.println(WIFI_SSID);

    WiFi.mode(WIFI_STA);

    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

    while (WiFi.status() != WL_CONNECTED && xCnt < 50)
    {
      delay(500);
      Serial.print(".");
      xCnt++;
    }

    if (WiFi.status() != WL_CONNECTED)
    {
      Serial.println("WiFiCon=0");
      return 0; // never connected
    }
    else
    {
      Serial.println("WiFiCon=1");
      Serial.println("");
      Serial.println("WiFi connected");
      Serial.println("IP address: ");
      Serial.println(WiFi.localIP());
      FIRST_WIFI_CONNECTION = true;
      return 1; // 1 is initial connection
    }
  }
  else
  {
    Serial.println("WiFiCon=2");
    return 2; // 2 is already connected
  }
}

void setup()
{
  Serial.begin(115200);
  WiFiCon();
  sensors.begin(); 

  // Initialize Outputs
  pinMode(relay, OUTPUT);
  pinMode(led, OUTPUT);

  // Assign the api key (required)
  config.api_key = API_KEY;

  // Assign the user sign in credentials
  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;

  // Assign the RTDB URL (required)
  config.database_url = DATABASE_URL;

  Firebase.reconnectWiFi(true);

  // Assign the callback function for the long running token generation task */
  config.token_status_callback = tokenStatusCallback; // see addons/TokenHelper.h

  // Assign the maximum retry of token generation
  config.max_token_generation_retry = 5;

  // Initialize the library with the Firebase authen and config
  Firebase.begin(&config, &auth);

  // Streaming (whenever data changes on a path)
  // Begin stream on a database path
  if (!Firebase.RTDB.beginStream(&stream, listenerPath.c_str()))
    Serial.printf("stream begin error, %s\n\n", stream.errorReason().c_str());

  // Assign a calback function to run when it detects changes on the database
  Firebase.RTDB.setStreamCallback(&stream, streamCallback, streamTimeoutCallback);

  delay(100);
}

void loop()
{
  if (millis() - myTimer2 >= 2000)
  {
    myTimer2 = millis();

    if (WiFiCon() > 0)
    {
      if (Firebase.ready())
      {

        if (FIRST_WIFI_CONNECTION)
        {
          if (!Firebase.RTDB.beginStream(&stream, listenerPath.c_str()))
            Serial.printf("stream begin error, %s\n\n", stream.errorReason().c_str());

          // Assign a calback function to run when it detects changes on the database
          Firebase.RTDB.setStreamCallback(&stream, streamCallback, streamTimeoutCallback);
        }

        setTemperature();
        if (GLOBAL_ON)
        {
          if (temperature >= requiredTemperature)
          {
            setReadyStatus(true);
            digitalWrite(relay, 0);
            digitalWrite(led, 0);
            IS_PAUSED = true;
          }
          if (IS_PAUSED && (requiredTemperature - temperature >= 1))
          {
            digitalWrite(relay, 1);
            digitalWrite(led, 1);
            IS_PAUSED = false;
          }
        }
        FIRST_WIFI_CONNECTION = false;
      }

      else
      {
        extraOff();
      }
    }
    else
    {
      extraOff();
    }
  }
}