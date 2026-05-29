package com.davi.vocash.interfaces.web;

/**
 * Corpo padrão de resposta de erro retornado pela API.
 *
 * @param codigo   código legível por máquina (ex.: {@code AUDIO_INVALIDO})
 * @param mensagem mensagem amigável para exibição ao usuário
 */
public record ErrorResponse(String codigo, String mensagem) {}
