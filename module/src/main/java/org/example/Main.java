package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    public static void main(String[] args) {

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1, 10, 10));

        JButton btnGerarArquivo = new JButton("Gerar arquivo de clientes");
        btnGerarArquivo.setBorder(new EmptyBorder(10, 10, 10, 10));
        JButton btnClienteGui = new JButton("Iniciar GUI");
        btnClienteGui.setBorder(new EmptyBorder(10, 10, 10, 10));

        btnGerarArquivo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                // GeradorDeArquivosDeClientes
                GeradorDeArquivosDeClientes.main(new String[0]);
            }
        });

        btnClienteGui.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ClienteGUI2
                SwingUtilities.invokeLater(() -> {
                    ClienteGUI2 gui = new ClienteGUI2();
                    gui.setVisible(true);
                });
            }
        });

        panel.add(btnClienteGui);
        panel.add(btnGerarArquivo);

        frame.add(panel, BorderLayout.CENTER);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }
}
