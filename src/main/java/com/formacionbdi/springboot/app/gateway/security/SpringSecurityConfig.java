package com.formacionbdi.springboot.app.gateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/*
 * @EnableWebFluxSecurity
 * Permite habilitar la seguridad con webflux
 * Esto con el fin de tener un método "bean" encargado de hacer toda la configuración 
 */
@EnableWebFluxSecurity
@Configuration
public class SpringSecurityConfig {
	
	// Inyectamos nuestro "filtro de autenticación"
	@Autowired
	private JwtAuthenticationFilter authenticationFilter;
	
	/*
	 * Método encargado de la configuración del spring security.
	 * El nombre del método es indistinto.
	 * 
	 * .csrf().disable().build()
	 * Deshabilita el token csrf obtenido de los formularios de las vistas que no serán usadas, ya que trabajamos con REST. 
	 */
	@Bean
	SecurityWebFilterChain configure(ServerHttpSecurity httpSecurity) {
		/*
		 * authorizeExchange()
		 * Representa configuración de las rutas protegidas 
		 */
		return httpSecurity.authorizeExchange()
				.pathMatchers("/api/security/oauth/**").permitAll() // ruta publica para el acceso a rutas del oauth
				.pathMatchers(HttpMethod.GET, "/api/productos/listar", "/api/items/listar", "/api/usuarios/usuarios", "/api/productos/ver/{id}", "/api/items/ver/{id}/cantidad/{cantidad}").permitAll()
				.pathMatchers(HttpMethod.GET, "/api/usuarios/usuarios/{id}").hasAnyRole("ADMIN", "USER")
				.pathMatchers("/api/productos/**", "/api/items/**", "/api/usuarios/**").hasRole("ADMIN")
				.anyExchange().authenticated()
				.and()
				.addFilterAt(authenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION) // Registramos nuestro filtro y le damos el correspondiente orden de "autenticación"
				.csrf().disable().build();
	}
}
