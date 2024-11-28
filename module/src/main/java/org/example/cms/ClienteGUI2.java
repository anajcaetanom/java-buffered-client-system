package org.example.cms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

public class ClienteGUI2 extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private BufferDeClientes bufferDeClientes;
    private final int TAMANHO_BUFFER = 10000;
    private int registrosCarregados = 0; // Contador de registros já carregados
    private String arquivoSelecionado;
    private boolean arquivoCarregado = false; // Para verificar se o arquivo foi carregado

    public ClienteGUI2() {
        setTitle("Gerenciamento de Clientes");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        bufferDeClientes = new BufferDeClientes();
        criarInterface();
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

    private void ordemAlfabetica() {


    }

    private void ordenarTabelaPorNome() {
        Vector<Vector> data = tableModel.getDataVector(); // Obtém os dados da tabela
        data.sort(new Comparator<Vector>() { // Ordena os dados
            @Override
            public int compare(Vector o1, Vector o2) {
                String nome1 = (String) o1.get(1); // Nome está na coluna 1
                String nome2 = (String) o2.get(1);
                return nome1.compareToIgnoreCase(nome2); // Comparação alfabética
            }
        });
        tableModel.fireTableDataChanged(); // Atualiza a exibição da tabela
    }



    private void criarInterface() {
        // Faz os botões
        JPanel panel = new JPanel(new BorderLayout());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10,10));
        JButton btnCarregar = new JButton("Carregar Clientes");
        JButton btnOrdAlfabetica = new JButton("Ordem Alfabética");
        JButton btnPesquisar = new JButton("Pesquisar Clientes");
        JButton btnInserir = new JButton("Inserir cliente");
        JButton btnRemover = new JButton("Remover cliente");

        // Faz as tabelas
        tableModel = new DefaultTableModel(new String[]{"#", "Nome", "Sobrenome", "Telefone", "Endereço", "Credit Score"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);


        // Adiciona um listener ao JScrollPane para carregar mais clientes ao rolar
        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!scrollPane.getVerticalScrollBar().getValueIsAdjusting()) {
                    // Verifica se estamos no final da tabela e se o arquivo foi carregado
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

        btnOrdAlfabetica.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ordenarTabelaPorNome();
            }
        });

        btnPanel.add(btnCarregar);
        btnPanel.add(btnPesquisar);
        btnPanel.add(btnInserir);
        btnPanel.add(btnRemover);
        btnPanel.add(btnOrdAlfabetica);
        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        add(panel);
    }

    private void carregarMaisClientes() {
        // Carrega apenas 10.000 registros de cada vez
        Cliente[] clientes = bufferDeClientes.proximosClientes(TAMANHO_BUFFER); // Chama o método com o tamanho do buffer
        if (clientes != null && clientes.length > 0) {
            for (Cliente cliente : clientes) {
                if (cliente != null) { // Verifica se o cliente não é nulo
                    tableModel.addRow(new Object[]{
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

    private void carregarMaisClientesOrdenados() {
        Cliente[] clientes = bufferDeClientes.proximosClientes(TAMANHO_BUFFER);
        if (clientes != null && clientes.length > 0) {
            // Ordena alfabeticamente o bloco
            Arrays.sort(clientes, Comparator.comparing(Cliente::getNome));
        }
        registrosCarregados += clientes.length;
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClienteGUI2 gui = new ClienteGUI2();
            gui.setVisible(true);
        });
    }
}
