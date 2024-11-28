package org.example;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ArquivoCliente implements ArquivoSequencial<Cliente> {
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private File file;

    @Override
    public void abrirArquivo(String nomeDoArquivo, String modoDeLeitura, Class<Cliente> classeBase) throws IOException{
        this.file = new File(nomeDoArquivo);

        if (modoDeLeitura.equals("leitura")) {
            // Inicia o arquivo em modo de leitura

            if (file.exists()) {
            // Verifica se o arquivo existe

                inputStream = new ObjectInputStream(new FileInputStream(file));

            } else {
                throw new FileNotFoundException("Arquivo não encontrado.");
            }
        } else if (modoDeLeitura.equals("escrita")) {
            // Inicia o arquivo em modo de escrita

            outputStream = new ObjectOutputStream(new FileOutputStream(file));

        } else if (modoDeLeitura.equals("leitura/escrita")) {
            // Para leitura/escrita, abrir ambos os stream

            if (file.exists()){
            // Verifica se o arquivo existe

               inputStream = new ObjectInputStream(new FileInputStream(file));

            }

            outputStream =  new ObjectOutputStream(new FileOutputStream(file, true));

        } else{

            throw new IllegalArgumentException();

        }
    }

    @Override
    public List<Cliente> lerRegistrosArquivo(int numeroRegistros) throws IOException, ClassNotFoundException {
        List<Cliente> registros = new ArrayList<>();

        try {
            for (int i = 0; i < numeroRegistros; i++) {
                Cliente cliente = (Cliente) inputStream.readObject();
                registros.add(cliente);
            }
        } catch (EOFException e) {

        }

        return registros;
    }

    @Override
    public void escreveNoArquivo(List<Cliente> dados) throws IOException {
        for (Cliente cliente : dados) {
            outputStream.writeObject(cliente);
        }
    }

    @Override
    public void fecharArquivo() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }

        if (outputStream != null) {
            outputStream.close();
        }
    }
}
