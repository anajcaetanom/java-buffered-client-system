package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Arrays;
import java.util.Comparator;

public class ClienteGUI2 extends JFrame {
    private final int TAMANHO_BUFFER = 10000;

    private JTable table;
    private DefaultTableModel tableModel;
    private BufferDeClientes bufferDeClientes;
    private int registrosCarregados = 0;
    private String arquivoSelecionado;
    private boolean arquivoCarregado = false;

    public ClienteGUI2() {
        setTitle("Gerenciamento de Clientes");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        bufferDeClientes = new BufferDeClientes();
        criarInterface();
    }

    private void carregarArquivo(){
        JFileChooser fileChooser = new JFileChooser();
        int retorno = fileChooser.showOpenDialog(this);
        if (retorno == JFileChooser.APPROVE_OPTION){
            arquivoSelecionado = fileChooser.getSelectedFile().getAbsolutePath();
            bufferDeClientes.associaBuffer(new ArquivoCliente());
            bufferDeClientes.inicializaBuffer("leitura", arquivoSelecionado); // Passa o nome do arquivo aqui
            registrosCarregados = 0; // Reseta o contador
            tableModel.setRowCount(0); // Limpa a tabela
            carregarMaisClientes(); // Carrega os primeiros clientes
            arquivoCarregado = true; // Marca que o arquivo foi carregado
        }
    }

    private void carregarMaisClientes() {
        Cliente[] clientes = bufferDeClientes.proximosClientes(TAMANHO_BUFFER); // Chama o método com o tamanho do buffer
        if (clientes != null && clientes.length > 0){
            for(Cliente cliente : clientes){
                if(cliente != null){
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


    private void criarInterface() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton btnCarregar = new JButton("Carregar Clientes");
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

        panel.add(btnCarregar, BorderLayout.NORTH);
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
