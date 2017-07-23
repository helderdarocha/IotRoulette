#include <ESP8266WiFi.h>
#include <PubSubClient.h>

// Se a rede WiFi mudar, atualize os dados abaixo
// e faca upload do programa novamente
const char* ssid = "surfing-iot";
const char* password = "iotiotiot";

// Se o servidor onde roda o MQTT mudar de IP
// atualize e faca upload deste programa novamente
const char* mqtt_server = "192.168.1.139";

WiFiClient espClient;
PubSubClient client(espClient);
char data[1] = {'1'}; // pulso enviado

// Conecte o Reed em D3, e o LED em D4
const int REED  = 0; // 3
const int LED   = 2; // 4

// ultimo pulso
int last = HIGH; // pulsos acontecem em LOW

// Para conectar em WiFi
void setup_wifi() {
  delay(10);
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.print("WiFi connected - ESP IP address: ");
  Serial.println(WiFi.localIP());
}

// Para conectar em MQTT (requer WiFi antes)
void reconnect() {
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    if (client.connect("ESP8266Client")) {
      Serial.println("connected");  
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      delay(5000);
    }
  }
}

// Configuracao e conexao
void setup() {
  pinMode(LED, OUTPUT);
  pinMode(REED, INPUT_PULLUP);
  Serial.begin(115200);

  // Na primeira tentativa, olhe no Serial Monitor
  // para ver se está conectando em WiFi corretamente
  setup_wifi(); 

  // Conecta no servidor MQTT (IP está hardwired acima)
  // rodando na porta 1883.
  client.setServer(mqtt_server, 1883);
  reconnect();
}

void loop() {
  if(!client.loop()) {
    reconnect();
  }

  int state = digitalRead(REED);

  // Consideramos apenas o momento em que um estado muda
  // de HIGH para LOW para enviar o pulso
  // Isto acontece quando REED é ligado a GND
  if(state == LOW) {
    if(last == HIGH) {
        // subscribers precisam se conectar no canal abaixo
        client.publish("/esp8266/pulse", data);
        Serial.println("pulse");
        digitalWrite(LED, HIGH);
    } else { 
      //Serial.println("-");
    }
    last = LOW;
  } else { // state = HIGH
    digitalWrite(LED, LOW);
    last = HIGH;
    //Serial.println(">>>"); 
  }

  // delay(10);
  
}
