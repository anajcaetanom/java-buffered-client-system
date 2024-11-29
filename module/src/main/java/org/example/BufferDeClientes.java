package org.example;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BufferDeClientes implements Buffer<Cliente> {
    
    private final int TAMANHO_BUFFER = 100;
    
    public ArquivoSequencial<Cliente> arquivoSequencial;
    private Queue<Cliente> buffer;
    private String modo;
    
    public BufferDeClientes() {
        this.buffer = new LinkedList<>();
    }
    
    @Override
    // Associa o buffer a um arquivo sequencial especifico
    public void associaBuffer(ArquivoSequencial<Cliente> arquivoSequencial){
        this.arquivoSequencial = arquivoSequencial;
    }

    @Override
    public void inicializaBuffer(String modo, String nomeArquivo){
        this.modo = modo;
        try {
            if (modo.equals("leitura")) {
                arquivoSequencial.abrirArquivo(nomeArquivo, "leitura", Cliente.class);
            } else if (modo.equals("escrita")) {
                arquivoSequencial.abrirArquivo(nomeArquivo, "escrita", Cliente.class);
            } else {
                throw new IllegalArgumentException("Modo: inválido: deve ser 'leitura' ou 'escrita'");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    @Override
    public void carregaBuffer() {
        if (!modo.equals("leitura")) {
            throw new IllegalStateException("Buffer deve estar em modo de leitura.");
        }
        
        try {
            List<Cliente> clientesLidos = arquivoSequencial.lerRegistrosArquivo(TAMANHO_BUFFER);
            
            if (clientesLidos != null) {
                for (Object obj : clientesLidos) {
                    if (obj instanceof Cliente) {
                        buffer.add((Cliente) obj);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Classe não encontrada: " + e.getMessage());
        }
    }


    @Override
    public void escreveBufferNoArquivo() {
        if (!modo.equals("escrita")) {
            throw new IllegalStateException("Buffer deve estar em modo de escrita.");
        }

        try {
            arquivoSequencial.escreveNoArquivo(new LinkedList<>(buffer));
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void fechaBuffer() {
        try {
            arquivoSequencial.fecharArquivo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void adicionaNoBuffer(Cliente cliente){
        if(!modo.equals("escrita")){
            throw new IllegalStateException("Buffer não está em modo de escrita!");
        }

        buffer.add(cliente);

        if (buffer.size() >= TAMANHO_BUFFER){
            escreveBufferNoArquivo();
        }
    }


    public Cliente proximoCliente() {
        if (!modo.equals("leitura")) {
            throw new IllegalStateException("Buffer deve estar em modo de leitura.");
        }

        if (buffer.isEmpty()) {
            carregaBuffer();
        }

        if (!buffer.isEmpty()) {
            return buffer.poll(); // Remove o próximo cliente da fila e o retorna.
        }

        return null;
    }

    public String getModo() {
        return modo;
    }

    public Cliente[] proximosClientes(int qtd) {
        Cliente[] clientes = new Cliente[qtd];
        int i = 0;

        while (i < qtd) {
            Cliente cliente = proximoCliente();
            if (cliente == null) {
                break;
            }

            clientes[i] = cliente;
            i++;
        }

        return Arrays.copyOf(clientes, i);
    }
    
}
