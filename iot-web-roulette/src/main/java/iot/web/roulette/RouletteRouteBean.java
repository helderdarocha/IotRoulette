/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package iot.web.roulette;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;

/**
 * 1) Rode o Mosquitto no mesmo computador 2) Configure o firmware do ESP8266
 * com dados da rede WiFi (IP, nome, senha) e faça upload 3) Ligue o ESP8266 e
 * instale na roleta 4) Copie o arquivo de dados para este pacote (dados.xslx) e
 * configure coluna a usar 5) Monte e rode o projeto 6) Gire a roleta
 *
 */
public class RouletteRouteBean extends RouteBuilder implements Serializable {

	int count = 0; // contador de vezes que a roda foi girada
	int pos = 0;   // marcador da posição do item na lista de sorteados

	String[] numerosSorteados;

	public RouletteRouteBean() {
		init();
	}

	// @PostConstruct
	public void init() {
		DataLoader loader;
		try {
			loader = new XLSXDataLoader();
			numerosSorteados = loader.getData();
			
			// verificar itens lidos
			System.out.println("Números: " + numerosSorteados.length);
			for(String item : numerosSorteados) {
				System.out.println(item);
			}
			
		} catch (IOException e) {
			System.out.println("Problema com o XLSX: veja se existe e tem permissão de acesso, se está no lugar correto, se está no formato correto, se coluna de dados está informada corretamente!");
			new RuntimeException(e);
		}
	}

	/**
	 * Este método opera junto com o cliente MQTT ESP8266 (código em Arduino Processing)
	 * e com o cliente WebSocket (código de index.html em HTML5/CSS3/JavaScript)
	 */
	@Override
	public void configure() throws Exception {
		
		// A rota abaixo é executada a cada pulso recebido no canal "/esp8266/pulse" do servidor MQTT localhost:1883
		final RouteDefinition route = 
                 from("timer://foo?fixedRate=true&period=300"); // para testes (sem pulsos MQTT)
				 // from("mqtt:test?subscribeTopicName=/esp8266/pulse&host=tcp://127.0.0.1:1883");
				
        route.routeId("roleta")
		.doTry()
		.process((exchange) -> {
			exchange.getOut().setBody(numerosSorteados[pos]);
			++count; // contador de vezes que a roda foi girada
			++pos;   // marcador da posição do item na lista de sorteados
			if (pos >= numerosSorteados.length) {
				pos = 0; // reinicia pelo inicio da lista
			}
		})
		.to("websocket://localhost:9292/ws?sendToAll=true")
		
		// Se houver exceções abaixo, tivemos problemas com a leitura dos dados - veja o stack trace
		.doCatch(Throwable.class)
		.process((exchange)->{
			Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
			exception.printStackTrace();
			exchange.getContext().stopRoute("roleta", 300, TimeUnit.MILLISECONDS, false); 
		})
		.endDoTry();
	}

}
