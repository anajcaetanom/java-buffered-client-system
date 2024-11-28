package org.example;

import java.io.IOException;
import java.util.List;

public interface ArquivoSequencial<T> {

    void abrirArquivo(String nomeArquivo, String modoLeitura, Class<T> classeBase)
        throws IOException;

    List<T> lerRegistrosArquivo(int numeroRegistros)
        throws IOException, ClassNotFoundException;

    void escreveNoArquivo(List<T> dados) throws IOException;

    void fecharArquivo()
        throws IOException;
}
