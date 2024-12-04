package org.example;

import javax.swing.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        // GeradorDeArquivosDeClientes
        Scanner scanner = new Scanner(System.in);

        System.out.print("Digite o nome do arquivo de saída: ");
        String nomeArquivo = scanner.next();

        System.out.print("Digite a quantidade de clientes a serem gerados: ");
        int qtdClientes = scanner.nextInt();

        GeradorDeArquivosDeClientes gerador = new GeradorDeArquivosDeClientes();

        gerador.gerarArquivosClientes(nomeArquivo, qtdClientes);

        // ClienteGUI2
        SwingUtilities.invokeLater(() -> {
            ClienteGUI2 gui = new ClienteGUI2();
            gui.setVisible(true);
        });
    }
}
