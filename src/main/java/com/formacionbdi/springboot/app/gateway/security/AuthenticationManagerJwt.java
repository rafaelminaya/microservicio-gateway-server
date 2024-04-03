package com.formacionbdi.springboot.app.gateway.security;

import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

/*
 * Clase que representa al "authentication manager"
 * Importante para el proceo de autenticación para verificar el jwt y poder acceder a las rutas protegidas
 * La finalidad de esta clase es para inyectarse en el "filtro de autenticación" y através de este método "authenticate()" registrar la autenticación.
 */
@Component
public class AuthenticationManagerJwt implements ReactiveAuthenticationManager {
		
	@Value("${config.security.oauth.jwt.key}")
	private String jwtKey;

	// Métdo implementado de la interfaz. Retorna un "Mono", es decir un flujo que contiene 1 solo elemento.
	// Dentro del argumento "authentication"(inyectado por el filtro) obtenemos el JWT para poder validarlo.
	//Este método por detrás será ejecutado con el método "subscribe()".
	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {
		
		/*		 
		 * just()
		 * Transforma un objeto común en un "Mono" (objeto reactivo) para que sea parte de un flujo.
		 * En este caso se obtiene y convierte el JWT del "authentication manager" en un "Mono".
		 * 
		 * map()
		 * Transformamos el JWT al tipo "Authentication" que requiere el generic de la firma del método a retornar.
		 * La secret key para ser compatible con la librería JJWT, la codificaremos a base64
		 */
		return Mono.just(authentication.getCredentials().toString())
				.map(token -> {
					SecretKey secretKey = Keys.hmacShaKeyFor(Base64.getEncoder().encode(jwtKey.getBytes()));
					// validacion del JWT y retorno de los "claims" de este JWT
					return Jwts.parserBuilder()
							.setSigningKey(secretKey).build() // Construye un argumento dado para validar el "secret" de la firma
							.parseClaimsJws(token).getBody();  // Obtiene los "claims"/"payload" del JWT
				})
				.map(claims -> {
					// Obtenemos el username del usuario. 
					//"user_name" es una propiedad del JSON del JWT obtenido.
					// String.class : Es el tipo de dato del valor de "user_name"
					String username = claims.get("user_name", String.class);
					@SuppressWarnings("unchecked")
					List<String> roles = claims.get("authorities", List.class);
					Collection<GrantedAuthority> authorities = roles.stream()
							.map(role -> new SimpleGrantedAuthority(role))
							.collect(Collectors.toList());
					
					// Retornamos el tipo Mono<Authentication> esperado por la firma del método
					return new UsernamePasswordAuthenticationToken(username, null, authorities);
				});
				
	}
	

}
