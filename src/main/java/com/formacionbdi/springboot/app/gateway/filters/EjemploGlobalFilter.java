package com.formacionbdi.springboot.app.gateway.filters;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/*
 * Con la interfaz "GlobalFilter" implementaremos el filtro global.
 * Este filtro será ejecutado tanto para todo "pre" y "post" de cada request y response que pase por el API Gateway.
 * 
 * Con "Ordered" implementaremos el método "getOrder()" el cual tiene un valor "-1"
 * Esto con el fin de indicar que esta clase que implementa un "filtro global", se ejecutará antes que cualquier otra clase con otro "filtro global".
 */
@Component
public class EjemploGlobalFilter implements GlobalFilter, Ordered {
	
	private final Logger logger = LoggerFactory.getLogger(EjemploGlobalFilter.class);
	/*
	 * Método es sobre escrito por la implementación de la interfaz "GlobalFilter"
	 * 
	 * exchange : 
	 * Objeto que permite acceder al request y al response para poder modificarlos.
	 * Por ejemplo, podríamos condicionar los valores de estos request o response, y rechazar su acceso a un servicio.
	 */

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		logger.info("---- Ejecutando el filtro Global PRE ---- ");
		/*
		 * Todo el código previo al "return" representa el "pre"(request).
		 * Y lo que se encuentre dentro del return representa el "post"(response).
		 * 
		 * .mutate()
		 * Permite mutar la información, en este caso del request.
		 * 
		 * .headers()
		 * Permite modificar el header del request, maneja una expresión lambda.
		 * Por lo que agregaremos token con su propio valor.
		 * Este token, podría ser manipulado en el controller o service.
		 */
		exchange.getRequest().mutate().headers(h -> h.add("token", "123456")); 
		
		/*
		 * "filter()" 
		 * Contiene un lambda con el que manejaremos el "response" / "post filter"
		 * 
		 * then(): 
		 * Operador que se ejecuta una vez finalizado el proceso previo y obtenido una respuesta.
		 * Dentro de este método manipularemos el response.
		 * Como argumento se le envía un objeto reactivo, del tipo "Mono", llamando a un función implementada, el cual tendrá nuestro código "post".
		 * 
		 * fromRunnable()
		 * Método de la clase "Mono" que utilizaremos. 
		 * El cual permite ejecutar desde una expresión lambda un "Runneable"(interfaz funcional) el cual es un hilo de Java.
		 * Permite crear un "Mono<Void>", tipo de dato de la firma del método, que es un objeto reactivo para poder implementar el "post"
		 */
		return chain.filter(exchange).then(Mono.fromRunnable(() -> {
			logger.info("---- Ejecutando el filtro Global POST ---- ");
			/*
			 * .getRequest()
			 * Obtiene el request
			 * 
			 * .getHeaders()
			 * Obtiene los headers del request
			 * 
			 * .getFirst("token")
			 * Obtiene un solo valor, buscándolo según el argumento dado, en este caso el "token".
			 */
			
			// Verificación de la existencia del "token" obtenido del "request"
			Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("token")).ifPresent(valueToken -> {
				// Envío del "token" obtenido del "request" hacia el "response".
				exchange.getResponse().getHeaders().add("token", valueToken);
			});
			
			/*
			 * Vamos a corroborar el funcionamiento del post con un ejemplo.
			 * Así que enviaremos una "cookie", con un color y su valor que modificaremos en el response.
			 * Además modificaremos el formato del response de JSON a TEXT_PLAIN
			 * El método build() es para generar el objeto esperado.
			 */
			
			// Modificación del cookie del response - Añadiendo una cookie y su valor
			/*
			exchange
			.getResponse()
			.getCookies()
			.add("color", ResponseCookie.from("color", "rojo").build());
			*/
			
			// Modificación del "header" del response - Modificando el response a formato "texto plano"
			/*
			exchange
			.getResponse()
			.getHeaders()
			.setContentType(MediaType.TEXT_PLAIN);
			*/
		}));
	}
	
	@Override
	public int getOrder() {
		// -1 para que sea el primer filtro global en ejecutarse. 
		// Y 1 o más para que no sea el primero en ejecutarse.
		// 100 para darle baja prioridad y que funciona la configuración con tolerancia a fallos del "application.yml" sin conflictos
		return 100;
	}
	

}
