package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClienteGUI2 extends JFrame {
    private final int TAMANHO_BUFFER = 10;

    private JTable table;
    private DefaultTableModel tableModel;
    private BufferDeClientes bufferDeClientes;
    private int registrosCarregados = 0;
    private String arquivoSelecionado;
    private boolean arquivoCarregado = false;

    private List<String> blocos = new ArrayList<>();

    public ClienteGUI2() {
        setTitle("Gerenciamento de Clientes");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        bufferDeClientes = new BufferDeClientes();
        criarInterface();
    }

    private void carregarMaisClientes() {
        Cliente[] clientes = bufferDeClientes.proximosClientes(TAMANHO_BUFFER); // Chama o metodo com o tamanho do buffer
        if (clientes != null && clientes.length > 0) {
            for (Cliente cliente : clientes) {
                if (cliente != null) {
                    tableModel.addRow(new Object[] {
                            tableModel.getRowCount() + 1,
                            cliente.getNome(),
                            cliente.getSobrenome(),
                            cliente.getTelefone(),
                            cliente.getEndereco(),
                            cliente.getCreditScore()
                    });
                }
            }
            registrosCarregados += clientes.length; // Atualiza o contador
        }
    }


    private void ordenaClientes() {
        // verifica se o arquivo a ser ordenado já foi selecionado antes no "Carregar Clientes",
        // se não, abre a janelinha pra selecionar
        if (arquivoSelecionado == null) {
            JFileChooser fileChooser = new JFileChooser();
            int retorno = fileChooser.showOpenDialog(this);

            if (retorno == JFileChooser.APPROVE_OPTION) {
                arquivoSelecionado = fileChooser.getSelectedFile().getAbsolutePath();
            }
        }

        GeradorDeArquivosDeClientes gerador = new GeradorDeArquivosDeClientes();

        bufferDeClientes.associaBuffer(new ArquivoCliente());
        bufferDeClientes.inicializaBuffer("leitura", arquivoSelecionado);

        int qtdBlocos = 1;
        registrosCarregados = 0;
        tableModel.setRowCount(0);
        Cliente[] clientes = bufferDeClientes.proximosClientes(TAMANHO_BUFFER);

        while (clientes != null && clientes.length > 0) {
            // ordena o bloco
            Arrays.sort(clientes);

            // escreve o bloco num arquivo temporário
            String nomeBloco = "bloco" + qtdBlocos;
            try {
                gerador.arquivoCliente.abrirArquivo(nomeBloco, "escrita", Cliente.class);
                List<Cliente> clientesList = new ArrayList<>(Arrays.asList(clientes));
                gerador.arquivoCliente.escreveNoArquivo(clientesList);
                gerador.arquivoCliente.fecharArquivo();

                blocos.add(nomeBloco);

                // atualiza os contadores
                qtdBlocos++;
                registrosCarregados += clientes.length;

            } catch (IOException e) {
                e.printStackTrace();
            }

            clientes = bufferDeClientes.proximosClientes(TAMANHO_BUFFER);
        }
    }

    private void merge(GeradorDeArquivosDeClientes gerador) {
        String nomeArquivo = "clientes_ordenados";

        List<BufferedReader> leitores = new ArrayList<>();
        List<String> linhaAtual = new ArrayList<>();

        try {
            for (String arquivo : blocos) {
                BufferedReader leitor = new BufferedReader(new FileReader(arquivo));
                leitores.add(leitor);
                linhaAtual.add(leitor.readLine());
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            gerador.arquivoCliente.abrirArquivo(nomeArquivo, "escrita", Cliente.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (true) {
            String menorValor = null;
            int indice = -1;


        }
    }

    private void carregarArquivo() {
        JFileChooser fileChooser = new JFileChooser();
        int retorno = fileChooser.showOpenDialog(this);

        if (retorno == JFileChooser.APPROVE_OPTION) {
            arquivoSelecionado = fileChooser.getSelectedFile().getAbsolutePath();
            bufferDeClientes.associaBuffer(new ArquivoCliente());
            bufferDeClientes.inicializaBuffer("leitura", arquivoSelecionado); // Passa o nome do arquivo aqui
            registrosCarregados = 0; // Reseta o contador
            tableModel.setRowCount(0); // Limpa a tabela
            carregarMaisClientes(); // Carrega os primeiros clientes
            arquivoCarregado = true; // Marca que o arquivo foi carregado
        }
    }


    private void criarInterface() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10,10));

        JButton btnCarregar = new JButton("Carregar Clientes");
        JButton btnAlfabetica = new JButton("Ordem Alfabetica");
        JButton btnPesquisar = new JButton("Pesquisar");
        JButton btnInserir = new JButton("Inserir Cliente");
        JButton btnRemover = new JButton("Remover Cliente");
        tableModel = new DefaultTableModel(
                new String[]{
                    "#",
                    "Nome",
                    "Sobrenome",
                    "Endereço",
                    "Telefone",
                    "CreditScore"
                }, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!scrollPane.getVerticalScrollBar().getValueIsAdjusting()) {
                    if (arquivoCarregado &&
                            scrollPane.getVerticalScrollBar().getValue() +
                            scrollPane.getVerticalScrollBar().getVisibleAmount() >=
                                    scrollPane.getVerticalScrollBar().getMaximum()) {
                        carregarMaisClientes();
                    }
                }
            }
        });

        btnCarregar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                carregarArquivo();
            }
        });

        btnAlfabetica.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ordenaClientes();
            }
        });

        btnPanel.add(btnCarregar);
        btnPanel.add(btnAlfabetica);
        btnPanel.add(btnPesquisar);
        btnPanel.add(btnInserir);
        btnPanel.add(btnRemover);
        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        add(panel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClienteGUI2 gui = new ClienteGUI2();
            gui.setVisible(true);
        });
    }
}
