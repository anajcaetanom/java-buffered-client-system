package org.example;

import com.github.javafaker.Faker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GeradorDeArquivosDeClientes {
    public final ArquivoCliente arquivoCliente;
    private final Faker faker;

    public GeradorDeArquivosDeClientes() {
        this.arquivoCliente = new ArquivoCliente();
        this.faker = new Faker();
    }

    private Cliente gerarClienteFicticio() {
        String nome = faker.name().firstName();
        String sobrenome = faker.name().lastName();
        String endereco = faker.address().fullAddress();
        String telefone = faker.phoneNumber().cellPhone();
        int creditScore = faker.number().numberBetween(0, 100);

        return new Cliente(nome, sobrenome, endereco, telefone, creditScore);
    }


    public void gerarArquivosClientes(String nomeArquivo, int qtdClientes) {
        try {
            arquivoCliente.abrirArquivo(nomeArquivo, "escrita", Cliente.class);

            List<Cliente> clientes = new ArrayList<>();

            for (int i = 0; i < qtdClientes; i++) {
                Cliente cliente = gerarClienteFicticio();
                clientes.add(cliente);
            }
            
            arquivoCliente.escreveNoArquivo(clientes);
            arquivoCliente.fecharArquivo();
            System.out.print("Arquivo de clientes gerado.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void gerarGrandeDatasetClientes (String nomeArquivo, int qtdClientes) {
        final int TAMANHO_LOTE = 1_000_000;

        try {
            arquivoCliente.abrirArquivo(nomeArquivo, "escrita", Cliente.class);

            int totalClientesGerados = 0;

            while (totalClientesGerados < qtdClientes) {
                int clientesRestantes = qtdClientes - totalClientesGerados;
                int tamanhoLote = Math.min(TAMANHO_LOTE, clientesRestantes);
                List<Cliente> clientes = new ArrayList<>();

                for (int i = 0; i < tamanhoLote; i++) {
                    Cliente cliente = gerarClienteFicticio();
                    clientes.add(cliente);
                }

                arquivoCliente.escreveNoArquivo(clientes);
                totalClientesGerados += tamanhoLote;

                System.out.print("Gerados e gravados " + totalClientesGerados + " clientes.");
            }

            arquivoCliente.fecharArquivo();
            System.out.print("Arquivo com " + qtdClientes + " clientes gerados.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Digite o nome do arquivo de saída: ");
        String nomeArquivo = scanner.next();

        System.out.print("Digite a quantidade de clientes a serem gerados: ");
        int qtdClientes = scanner.nextInt();

        GeradorDeArquivosDeClientes gerador = new GeradorDeArquivosDeClientes();

        gerador.gerarArquivosClientes(nomeArquivo, qtdClientes);
    }
}
