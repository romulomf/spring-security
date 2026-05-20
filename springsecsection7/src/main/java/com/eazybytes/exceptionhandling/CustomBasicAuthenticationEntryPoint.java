package com.eazybytes.exceptionhandling;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomBasicAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
		// Exemplo de como enviar um header personalizado
		response.setHeader("eazybank-error-reason", "Authentication failed");
		// Envia apenas o status da resposta
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		// Indica que a resposta é enviada no formato JSON
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		String message = (authException != null && authException.getMessage() != null) ? authException.getMessage() : "Unauthorized";
		String path = request.getRequestURI();
		String content = """
        {
            "timestamp": "%s",
            "status": "%d",
            "error": "%s",
            "message": "%s",
            "path", "%s"
        }
        """;
		// Escreve no buffer de saída que gera a resposta que aparece no corpo da mensagem de exceção da autorização
		response.getWriter().write(
			String.format(content, LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(), message, path)
		);
	}
}