#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h> // nghien cuu thu vien nay de dong goi du lieu dang JSON cho no chuyen nghiep :))

// info of wifi
const char* ssid     = "OPPO_K3";
const char* password = "@123456789";

// authentication credentials
const char* clientID = "Wemos_ESP8266_D1";
const char* username = "anomyous";
const char* password = "unknown"

// info of broker
const char* server = "192.168.43.89";
const uint16_t port = 1883;

// other info
const char* TOPIC = "hello/world";
const unsigned delayTime = 500;

WiFiClient wl;
PubSubClient client(server, port, wl);

void setup() {
  Serial.begin(9600);
  
  // setting hostname for esp
  WiFi.hostname(clientID); // luu y modem wifi luu tru cache ip address vs mac address
  Serial.print("Host Name: ");
  Serial.println(WiFi.hostname());
  
  // connecting to a WiFi network
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.mode(WIFI_STA);
  WiFi.hostname(clientID);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(delayTime);
    Serial.write(".");
  }

  Serial.println("Connected");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());

  // connecting to HiveMQ broker
  Serial.write("Connecting to HiveMQ at ");
  Serial.print(server);
  Serial.print(':');
  Serial.println(port);

  while (!client.connected()) {
      Serial.print(".");
      client.connect(clientID);
      delay(delayTime);
  }
  Serial.println("Ccnnected");
}

void loop() {
  delay(delayTime);
  char *payload = getDataFromSensors();
  client.publish(TOPIC, payload);
  client.loop();
}

const int minimum = 0;
const int maximum = 100;
char* getDataFromSensors() {
  // fields
  int temp = random(minimum, maximum); // temperature
  int hum = random(minimum, maximum); // humanity
  int O2 = random(minimum, maximum); // concentration of O2
  int CO2 = random(minimum, maximum); // concentration of CO2
  int CO = random(minimum, maximum); // concentration of CO
  int PM25 = random(minimum, maximum); // concentration of PM25
  int PM5 = random(minimum, maximum); // concentration of PM5
  int gas = random(minimum, maximum); // concentration of gas

  // todo: dung thu vien AduinoJson dong goi du lieu JSON cho no ngau thay vi code chay nhu the nay :))
  String res = "";
  res += "{temp: " + String(temp) + ",";
  res += "hum: " + String(hum) + ",";
  res += "O2: " + String(O2) + ",";
  res += "CO2: " + String(CO2) + ",";
  res += "CO: " + String(CO) + ",";
  res += "PM25: " + String(PM25) + ",";
  res += "PM5: " + String(PM5) + ",";
  res += "gas: " + String(gas) + "}";

  char ans[res.length() + 1];
  strcpy(ans, res.c_str());
  return ans;
}
