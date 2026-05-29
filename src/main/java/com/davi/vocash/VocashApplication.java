package com.davi.vocash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicação Vocash.
 *
 * <p>Vocash é um assistente financeiro por voz que recebe arquivos de áudio,
 * transcreve a fala com o modelo Whisper (via Groq), interpreta o texto com o
 * LLM {@code llama-3.3-70b-versatile} (via Groq) e executa ações de registro
 * ou consulta de gastos no PostgreSQL por meio do mecanismo de <em>tool calling</em>
 * do Spring AI.
 *
 * <p><b>Stack principal:</b> Java 21 · Spring Boot 3.5 · Spring AI 1.0 · PostgreSQL.
 *
 * <p><b>Pipeline resumido:</b>
 * <pre>
 *   Áudio (multipart) → Whisper (transcrição) → LLM (intenção) → Tool (@Tool) → Resposta em texto
 * </pre>
 */
@SpringBootApplication
public class VocashApplication {

	public static void main(String[] args) {
		SpringApplication.run(VocashApplication.class, args);
	}
}
