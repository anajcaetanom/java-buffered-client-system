package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
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
    private String arquivoOriginal;
    private boolean arquivoCarregado = false;
    private JTextField searchField;

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

        bufferDeClientes.associaBuffer(new ArquivoCliente());
        bufferDeClientes.inicializaBuffer("leitura", arquivoSelecionado);

        int qtdBlocos = 1;
        registrosCarregados = 0;
        tableModel.setRowCount(0);

        Cliente[] clientes = bufferDeClientes.proximosClientes(TAMANHO_BUFFER);

        BufferDeClientes bufferDeClientesOrdenados = new BufferDeClientes(); // outro buffer pros clientes ordenados
        bufferDeClientesOrdenados.associaBuffer(new ArquivoCliente());

        while (clientes != null && clientes.length > 0) {
            // ordena o bloco
            Arrays.sort(clientes);

            // inicializa arquivo temporário
            String nomeBloco = "bloco" + qtdBlocos;
            bufferDeClientesOrdenados.inicializaBuffer("escrita", nomeBloco);

            // adiciona os clientes ordenados no buffer de escrita
            for (Cliente cliente : clientes) {
                bufferDeClientesOrdenados.adicionaNoBuffer(cliente);
            }

            // escreve o bloco no arquivo temporário
            bufferDeClientesOrdenados.escreveBufferNoArquivo();
            bufferDeClientesOrdenados.fechaBuffer();

            blocos.add(nomeBloco); // adiciona o nome do bloco na array que vai ser usada no merge

            // atualiza os contadores e o loop
            qtdBlocos++;
            registrosCarregados += clientes.length;
            clientes = bufferDeClientes.proximosClientes(TAMANHO_BUFFER);
        }
    }

    private void merge() {
        String nomeArquivo = "clientes_ordenados";

        File arquivoFinal = new File(nomeArquivo);
        if (arquivoFinal.exists()) {
            if (arquivoFinal.delete()) {
                System.out.println("arquivo deletado.");
            }
        }

        List<BufferDeClientes> leitores = new ArrayList<>();
        List<Cliente> clienteAtual = new ArrayList<>();

        for (String bloco : blocos) {
            BufferDeClientes leitor = new BufferDeClientes();
            leitor.associaBuffer(new ArquivoCliente());
            leitor.inicializaBuffer("leitura", bloco);
            leitores.add(leitor);
            clienteAtual.add(leitor.proximoCliente());
        }

        BufferDeClientes escritor = new BufferDeClientes();
        escritor.associaBuffer(new ArquivoCliente());
        escritor.inicializaBuffer("escrita", nomeArquivo);

        while (true) {
            Cliente menorValor = null;
            int indiceMenorValor = -1;

            for (int i = 0; i < leitores.size(); i++) {
                Cliente cliente = clienteAtual.get(i);

                if (cliente == null) {
                    continue;
                }

                if (menorValor == null || cliente.compareTo(menorValor) < 0) {
                    menorValor = cliente;
                    indiceMenorValor = i;
                }
            }

            if (menorValor == null) {
                break;
            }

            escritor.adicionaNoBuffer(menorValor);
            escritor.escreveBufferNoArquivo();

            BufferDeClientes leitorMenor = leitores.get(indiceMenorValor);
            Cliente proximoCliente = leitorMenor.proximoCliente();
            clienteAtual.set(indiceMenorValor, proximoCliente);
        }

        for (BufferDeClientes leitor : leitores) {
            leitor.fechaBuffer();
        }

        escritor.fechaBuffer();

        // apaga os arquivos temporários (blocos)
        for (String bloco : blocos) {
            File file = new File(bloco);
            if (file.exists()) {
                file.delete();
            }
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

    private void carregarClientesOrdenados() {
        arquivoOriginal = arquivoSelecionado;

        String diretorio = System.getProperty("user.dir");
        String nomeArquivo = "clientes_ordenados";

        File file = new File(diretorio, nomeArquivo);
        arquivoSelecionado = file.getAbsolutePath();

        bufferDeClientes.associaBuffer(new ArquivoCliente());
        bufferDeClientes.inicializaBuffer("leitura", arquivoSelecionado); // Passa o nome do arquivo aqui
        registrosCarregados = 0; // Reseta o contador
        tableModel.setRowCount(0); // Limpa a tabela
        carregarMaisClientes(); // Carrega os primeiros clientes
        arquivoCarregado = true; // Marca que o arquivo foi carregado
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
                    "Telefone",
                    "Endereço",
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
                merge();
                carregarClientesOrdenados();
                arquivoSelecionado = arquivoOriginal;
            }
        });

        btnPesquisar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Cria um JDialog como a mini tela
                JDialog searchDialog = new JDialog();
                searchDialog.setTitle("Pesquisar Cliente");
                searchDialog.setSize(550, 200);
                searchDialog.setLocationRelativeTo(null); // Centraliza a janela
                searchDialog.setModal(true);

                // Painel para os componentes
                JPanel dialogPanel = new JPanel(new BorderLayout(10, 10));
                JPanel inputPanel = new JPanel(new FlowLayout());
                JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10)); // Usando GridLayout para organizar os botões

                searchField = new JTextField(20);

                // Botões para realizar a pesquisa
                JButton btnPesquisarNome = new JButton("Pesquisar por Nome");
                JButton btnPesquisarSobrenome = new JButton("Pesquisar por Sobrenome");
                JButton btnPesquisarEndereco = new JButton("Pesquisar por Endereço");
                JButton btnPesquisarTelefone = new JButton("Pesquisar por Telefone");
                JButton btnPesquisarScore =  new JButton("Pesquisar por Score");

                btnPesquisarNome.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        pesquisarNomeClientes();
                        searchDialog.dispose(); // Fecha o diálogo após a pesquisa
                    }
                });

                btnPesquisarSobrenome.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        pesquisarSobrenomeClientes();
                        searchDialog.dispose();
                    }
                });

                btnPesquisarEndereco.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        pesquisarEnderecoClientes();
                        searchDialog.dispose();
                    }
                });

                btnPesquisarTelefone.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        pesquisarTelefoneClientes();
                        searchDialog.dispose();
                    }
                });

                btnPesquisarScore.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        pesquisarScoreClientes();
                        searchDialog.dispose();
                    }
                });

                inputPanel.add(new JLabel("Nome de Pesquisa:"));
                inputPanel.add(searchField);
                buttonPanel.add(btnPesquisarNome);
                buttonPanel.add(btnPesquisarSobrenome);
                buttonPanel.add(btnPesquisarEndereco);
                buttonPanel.add(btnPesquisarTelefone);
                buttonPanel.add(btnPesquisarScore);

                dialogPanel.add(inputPanel, BorderLayout.CENTER);
                dialogPanel.add(buttonPanel, BorderLayout.SOUTH);

                searchDialog.add(dialogPanel);
                searchDialog.setVisible(true);
            }
        });

        btnInserir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create a JDialog to input new client details
                JDialog insertDialog = new JDialog();
                insertDialog.setTitle("Inserir Novo Cliente");
                insertDialog.setSize(400, 300);
                insertDialog.setLocationRelativeTo(null);
                insertDialog.setModal(true);

                JPanel dialogPanel = new JPanel(new GridLayout(6, 2, 10, 10));
                JTextField nomeField = new JTextField();
                JTextField sobrenomeField = new JTextField();
                JTextField enderecoField = new JTextField();
                JTextField telefoneField = new JTextField();
                JTextField creditScoreField = new JTextField();

                dialogPanel.add(new JLabel("Nome:"));
                dialogPanel.add(nomeField);
                dialogPanel.add(new JLabel("Sobrenome:"));
                dialogPanel.add(sobrenomeField);
                dialogPanel.add(new JLabel("Endereço:"));
                dialogPanel.add(enderecoField);
                dialogPanel.add(new JLabel("Telefone:"));
                dialogPanel.add(telefoneField);
                dialogPanel.add(new JLabel("Credit Score:"));
                dialogPanel.add(creditScoreField);

                JButton btnSalvar = new JButton("Salvar");
                btnSalvar.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String nome = nomeField.getText();
                        String sobrenome = sobrenomeField.getText();
                        String endereco = enderecoField.getText();
                        String telefone = telefoneField.getText();
                        int creditScore = Integer.parseInt(creditScoreField.getText());

                        Cliente novoCliente = new Cliente(nome, sobrenome, endereco, telefone, creditScore);
                        inserirCliente(novoCliente);
                        insertDialog.dispose();
                    }
                });

                dialogPanel.add(new JLabel());
                dialogPanel.add(btnSalvar);

                insertDialog.add(dialogPanel);
                insertDialog.setVisible(true);
            }
        });

        btnRemover.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Verificar se algum cliente foi selecionado
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null, "Selecione um cliente para remover.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Obter os dados do cliente selecionado
                String nome = (String) tableModel.getValueAt(selectedRow, 1);
                String sobrenome = (String) tableModel.getValueAt(selectedRow, 2);
                String telefone = (String) tableModel.getValueAt(selectedRow, 3);
                String endereco = (String) tableModel.getValueAt(selectedRow, 4);
                int creditScore = (int) tableModel.getValueAt(selectedRow, 5);
                System.out.println(nome + sobrenome + telefone + endereco + creditScore);

                // Criar uma instância do cliente com os dados da linha selecionada
                Cliente clienteSelecionado = new Cliente(nome, sobrenome, endereco, telefone, creditScore);

                // Confirmar remoção
                int confirmacao = JOptionPane.showConfirmDialog(
                        null,
                        "Tem certeza que deseja remover o cliente selecionado?",
                        "Confirmar Remoção",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirmacao == JOptionPane.YES_OPTION) {
                    // Remover cliente
                    removerCliente(clienteSelecionado);
                }
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

    //                                                  //
    //                                                  //
    // Adiciona o método de excluir o cliente da tabela //
    //                                                  //
    //                                                  //

    private void removerCliente(Cliente cliente) {
        if (!arquivoCarregado) {
            JOptionPane.showMessageDialog(this, "Nenhum arquivo carregado.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Inicializa o buffer em modo de leitura para carregar os clientes existentes
            bufferDeClientes.inicializaBuffer("leitura", arquivoSelecionado);
            List<Cliente> clientesExistentes = new ArrayList<>();
            Cliente clienteExistente;
            while ((clienteExistente = bufferDeClientes.proximoCliente()) != null) {
                // Remove o cliente
                if(!cliente.equals(clienteExistente)){
                    clientesExistentes.add(clienteExistente);
                }
            }
            bufferDeClientes.fechaBuffer();

            // Inicializa o buffer em modo de escrita para reescrever todos os clientes
            bufferDeClientes.inicializaBuffer("escrita", arquivoSelecionado);
            for (Cliente c : clientesExistentes) {
                bufferDeClientes.adicionaNoBuffer(c);
            }
            bufferDeClientes.escreveBufferNoArquivo();
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar cliente: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } finally {
            tableModel.setRowCount(0);
            bufferDeClientes.inicializaBuffer("leitura", arquivoSelecionado);
        }
    }

    //                                                  //
    //                                                  //
    // Adiciona o método de inserir o cliente na tabela //
    //                                                  //
    //                                                  //

    private void inserirCliente(Cliente cliente) {
        if (!arquivoCarregado) {
            JOptionPane.showMessageDialog(this, "Nenhum arquivo carregado.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Inicializa o buffer em modo de leitura para carregar os clientes existentes
            bufferDeClientes.inicializaBuffer("leitura", arquivoSelecionado);
            List<Cliente> clientesExistentes = new ArrayList<>();
            Cliente clienteExistente;
            while ((clienteExistente = bufferDeClientes.proximoCliente()) != null) {
                clientesExistentes.add(clienteExistente);
            }
            bufferDeClientes.fechaBuffer();

            // Adiciona o novo cliente à lista de clientes existentes
            clientesExistentes.add(cliente);

            // Inicializa o buffer em modo de escrita para reescrever todos os clientes
            bufferDeClientes.inicializaBuffer("escrita", arquivoSelecionado);
            for (Cliente c : clientesExistentes) {
                bufferDeClientes.adicionaNoBuffer(c);
            }
            bufferDeClientes.escreveBufferNoArquivo();
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar cliente: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } finally {
            tableModel.setRowCount(0);
            bufferDeClientes.inicializaBuffer("leitura", arquivoSelecionado);
        }
    }

    //                                              //
    //                                              //
    // Adiciona o métodos de pesquisas dos clientes //
    //                                              //
    //                                              //

    private void pesquisarNomeClientes() {
        String termoPesquisa = searchField.getText().trim().toLowerCase();
        if (termoPesquisa.isEmpty()) {
            return;
        }

        bufferDeClientes.associaBuffer(new ArquivoCliente());
        bufferDeClientes.inicializaBuffer("leitura", arquivoSelecionado);

        tableModel.setRowCount(0);

        Cliente cliente;
        while ((cliente = bufferDeClientes.proximoCliente()) != null) {
            if (cliente.getNome().toLowerCase().contains(termoPesquisa)){
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
    }

    private void pesquisarSobrenomeClientes() {
        String termoPesquisa = searchField.getText().trim().toLowerCase();
        if (termoPesquisa.isEmpty()) {
            return;
        }

        bufferDeClientes.associaBuffer(new ArquivoCliente());
        bufferDeClientes.inicializaBuffer("leitura", arquivoSelecionado);

        tableModel.setRowCount(0);

        Cliente cliente;
        while ((cliente = bufferDeClientes.proximoCliente()) != null) {
            if (cliente.getSobrenome().toLowerCase().startsWith(termoPesquisa)){
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
    }

    private void pesquisarEnderecoClientes() {
        String termoPesquisa = searchField.getText().trim().toLowerCase();
        if (termoPesquisa.isEmpty()) {
            return;
        }

        bufferDeClientes.associaBuffer(new ArquivoCliente());
        bufferDeClientes.inicializaBuffer("leitura", arquivoSelecionado);

        tableModel.setRowCount(0);

        Cliente cliente;
        while ((cliente = bufferDeClientes.proximoCliente()) != null) {
            if (cliente.getEndereco().toLowerCase().contains(termoPesquisa)){
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
    }

    private void pesquisarTelefoneClientes() {
        String termoPesquisa = searchField.getText().trim().toLowerCase();
        if (termoPesquisa.isEmpty()) {
            return;
        }

        bufferDeClientes.associaBuffer(new ArquivoCliente());
        bufferDeClientes.inicializaBuffer("leitura", arquivoSelecionado);

        tableModel.setRowCount(0);

        Cliente cliente;
        while ((cliente = bufferDeClientes.proximoCliente()) != null) {
            if (cliente.getTelefone().toLowerCase().startsWith(termoPesquisa)){
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
    }
    private void pesquisarScoreClientes() {
        String termoPesquisa = searchField.getText().trim().toLowerCase();
        if (termoPesquisa.isEmpty()) {
            return;
        }

        bufferDeClientes.associaBuffer(new ArquivoCliente());
        bufferDeClientes.inicializaBuffer("leitura", arquivoSelecionado);

        tableModel.setRowCount(0);

        Cliente cliente;
        while ((cliente = bufferDeClientes.proximoCliente()) != null) {
            String scoreCliente = String.valueOf(cliente.getCreditScore());
            if (scoreCliente.toLowerCase().startsWith(termoPesquisa)){
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
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClienteGUI2 gui = new ClienteGUI2();
            gui.setVisible(true);
        });
    }
}
