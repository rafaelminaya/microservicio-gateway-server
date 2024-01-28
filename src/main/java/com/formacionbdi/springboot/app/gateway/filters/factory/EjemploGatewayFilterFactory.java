package com.formacionbdi.springboot.app.gateway.filters.factory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

/*
 * ...GatewayFilterFactory : 
 * Este "sufijo" se le suele dar a clases que representan un "filtro personalizado".
 * Es un "sufijo" muy importante para detectar a la clase de forma automática com un "filtro personalizado"
 * Aunque podríamos darle un nombre para luego configurarlo en el archivo "application.properties" o "application.yml"
 * 
 * AbstractGatewayFilterFactory
 * Para los filtros por cada ruta, necesitamos extender de esta clase abstracta.
 * El genérico será la clase de configuración que enviaremos al filtro
 */
@Component
public class EjemploGatewayFilterFactory extends AbstractGatewayFilterFactory<EjemploGatewayFilterFactory.Configuracion> {
	
	private final Logger logger = LoggerFactory.getLogger(EjemploGatewayFilterFactory.class);
	
	
	// Constructor enviando nuestra clase personalizada que representará una configuración.
	public EjemploGatewayFilterFactory() {
		super(Configuracion.class);		
	}

	// Esta clase clase anidada a la clase actual es estática y representa la configuración, el cual
	// la usaremos en el generic de la clase abstracta "AbstractGatewayFilterFactory"
	public static class Configuracion {
		
		private String mensaje;
		private String cookieValor;
		private String cookieNombre;
		
		public String getMensaje() {
			return mensaje;
		}
		public void setMensaje(String mensaje) {
			this.mensaje = mensaje;
		}
		public String getCookieValor() {
			return cookieValor;
		}
		public void setCookieValor(String cookieValor) {
			this.cookieValor = cookieValor;
		}
		public String getCookieNombre() {
			return cookieNombre;
		}
		public void setCookieNombre(String cookieNombre) {
			this.cookieNombre = cookieNombre;
		}
				
	}

	/*
	 *  Método sobre escrito por la herencia de la clase abstracta.
	 *  Este método aplicará el filtro con nuestra configuración.
	 *  Implementaremos la interfaz funcional "GatewayFilter" el cual tiene el método reactivo "filter()" que retorna un "Mono<Void>",
	 *  además este método "filter()" recibe de argumentos un "exchange" y una cadena "chain"
	 */
	@Override
	public GatewayFilter apply(Configuracion config) {
		/*
		 * new OrderedGatewayFilter : 
		 * Permite indicar el orden de la ejecucion, 
		 * Pero este orden es opcional, es decir, podriamos no retonar una instancia de "OrderedGatewayFilter" y su orden "2", 
		 * sino retornar únicamente  el lambda "(exchange, chain)".
		 * El primer argumento es la expresion lambda y el segundo el orden.
		 */
		
		return new OrderedGatewayFilter((exchange, chain) -> {
			/*
			 * Lo que venga antes del return es el "pre" y lo que viene después el "post"
			 */
			
			logger.info("---- Ejecutando el filtro factory PRE: " + config.mensaje + " ---- ");
			

			return chain.filter(exchange).then(Mono.fromRunnable(() -> {
				// Verificación de la existencia del "cookieValor" obtenido del argumento "config"
				// Con eta condición, si no está presente un valor, la "cookie" ni siquiera se creará.
				Optional.ofNullable(config.cookieValor).ifPresent(cookieValor -> {					
					//Envío de una "cookie"(representado por un name y un value) al "response",
					//cuyos valores del "name" y "value" son los valores de "config.cookieNombre" y "config.cookieValor" respectivamente
					exchange.getResponse().addCookie(ResponseCookie.from(config.cookieNombre, cookieValor).build());
				});
				
				logger.info("---- Ejecutando el filtro factory POST: " + config.mensaje + "  ---- ");
			}));
			// Este "2" representa el orden, puede ser desde el número 1 en adelante.
		}, 2);
	}
 
	// Sobre escribimos este método que proviene de la interfaz "ShortcutConfigurable"
	// Este método retornara un lista con los nombres de los campos con el orden en que queremos que se ejecuten
	@Override
	public List<String> shortcutFieldOrder() {
		// Este orden debe coincidir con lo escrito en el archivo "application.yml"
		return Arrays.asList("mensaje", "cookieNombre", "cookieValor");
	}

	/*
	 *  Método implementado de la interfaz "GatewayFilterFactory"
	 *  Método que permite modificar el nombre del filtro, 
	 *  que por defecto toma el nombre de la clase dejando de la el  "GatewayFilterFactory",
	 *  es decir, se llamaría "Ejemplo", por el nombre completo de "EjemploGatewayFilterFactory"
	 */
	// 
	@Override
	public String name() {
		// Este nombre debe coincidir con el "filter" en el archivo "application.yml"
		return "EjemploCookie";
	}
	
	
	

}
