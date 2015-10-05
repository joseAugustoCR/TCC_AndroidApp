package com.example.guto.tcc_app_v1;

/**
 * Interface para declaração de um método para manipular a resposta do servidor
 * Todas as classes que se comunicam com o servidor irão implementar um método próprio para tratar a resposta recebida do servidor
 */
public interface AsyncResponse {
    void sendFinish(String output);
}
