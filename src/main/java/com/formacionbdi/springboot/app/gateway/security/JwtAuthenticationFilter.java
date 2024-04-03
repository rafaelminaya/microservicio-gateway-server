package com.formacionbdi.springboot.app.gateway.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/*
 * Implementación de la clase para el "filtro de autenticación"
 * Este será inyectado en nuestra clase de configuración de spring security "SpringSecurityConfig"
 */

@Component
public class JwtAuthenticationFilter implements WebFilter {
	
	// Esta interfaz fue implementada en nuestra clase personalizada "AuthenticationManagerJwt"
	@Autowired
	private ReactiveAuthenticationManager authenticationManager;
	
	/*
	 * Mediante el argumento "exchange" podemos obtener el request del cliente y dentro de este en el header con el JWT.
	 */
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		/*
		 * Mono.justOrEmpty()
		 * Método necesario para retornar un generic "Mono<Void>"
		 */
		return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION)) // obtenemos el "Authorization" del header del request y lo convertismo en un "flujo reactivo" del tipo "Mono<Void>"
				.filter(authHeader -> authHeader.startsWith("Bearer ")) // validacion del formato "Bearer " del JWT
				.switchIfEmpty(chain.filter(exchange).then(Mono.empty())) // Indicamos que si no se cumple la condición del formato del JWT, devuelva "flujo reactivo" de "Mono.empty()" y salimos del "flujo reactivo".
				.map(token -> token.replace("Bearer", "")) // Acá continuamos con el "flujo reactivo", en caso sí cumpla la condición del formato de JWT. Removemos la palabra "Bearer " para dejar únicamente el JWT.
				.flatMap(token -> this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(null, token))) // flatMap() ya que este método "authenticate()" devuelve un "flujo reactivo" de "Mono<Authentication>". Acá asignamos el rol a nuestro "authentication manager" utilizando este método "authenticate()" al inyectar esta dependencia. 
				.flatMap(authentication -> 
				chain.filter(exchange) // enviamos el argumento "exchange" para continuar con la ejecución de los demás filtros y del request 
						.contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)));// asignamos nuestro objeto "authentication" (que ya estaría autenticado) al contexto de spring security
	}
	

}
