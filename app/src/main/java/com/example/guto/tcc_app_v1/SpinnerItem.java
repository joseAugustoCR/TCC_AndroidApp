package com.example.guto.tcc_app_v1;

/**
 * Classe utilizada para preencher spinners de modo a salvar o nome e id, mas só exibir o nome
 */
public class SpinnerItem{
     public  String  nome;
    public   String id;

    // Apenas o nome fica visível
    @Override
    public String toString() {
        return nome;
    }
}
