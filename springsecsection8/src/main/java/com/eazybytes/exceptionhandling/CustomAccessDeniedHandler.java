package com.eazybytes.exceptionhandling;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
		// Exemplo de como enviar um header personalizado
		response.setHeader("eazybank-denied-reason", "Authorization failed");
		// Envia apenas o status da resposta
		response.setStatus(HttpStatus.FORBIDDEN.value());
		// Indica que a resposta é enviada no formato JSON
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		String message = (accessDeniedException != null && accessDeniedException.getMessage() != null) ? accessDeniedException.getMessage() : "Authorization failed";
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
			String.format(content, LocalDateTime.now(), HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), message, path)
		);
	}
}