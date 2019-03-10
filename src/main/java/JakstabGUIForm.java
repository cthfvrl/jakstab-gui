import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import guru.nidi.graphviz.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;

public class JakstabGUIForm extends JFrame {
    private JPanel optionsPanel;
    private JTextField sourceFileInput;
    private JLabel sourceFileInputLabel;
    private JPanel jakstabRootPanel;
    private JButton chooseFileButton;
    private JTextPane outputTextPane;
    private JPanel sourcePanel;
    private JPanel graphWriterPanel;
    private JButton runButton;
    private JScrollPane outputScrollPane;
    private JPanel controlPanel;
    private JRadioButton graphvizRadioButton;
    private JRadioButton graphmlRadioButton;
    private JButton stopButton;
    private JTabbedPane outputTabbedPane;
    private JPanel graphDrawPanel;
    private JButton graphButton;
    private JPanel graphSourcePanel;
    private JTextField graphFileInput;
    private JLabel graphFileInputLabel;
    private JButton chooseGraphFileButton;
    private JPanel graphTypePanel;

    private Process currentProcess = null;

    public JakstabGUIForm() {
        // This uses the form designer form
        add(jakstabRootPanel);

        setTitle("Jakstab GUI");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChoose(sourceFileInput);
            }
        });

        chooseGraphFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChoose(graphFileInput);
            }
        });

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sourceFileInput.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(jakstabRootPanel, "Source path is empty!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "jakstab", "-m", sourceFileInput.getText());
                    processBuilder.redirectErrorStream(true);
                    try {
                        final Process process = processBuilder.start();
                        currentProcess = process;
                        watch(process, outputTextPane);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        // TODO: add process queue
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentProcess != null && currentProcess.isAlive())
                    processKill(currentProcess);
            }
        });

        graphButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graphFileInput.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(jakstabRootPanel, "Graph path is empty!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        String graphFilePath = graphFileInput.getText();
                        Graphviz g = Graphviz.fromFile(new File(graphFilePath));
                        g.render(Format.PNG).toFile(new File(graphFilePath + ".png"));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

    }

    private static void fileChoose(JTextField textField) {
        JFileChooser fileopen = new JFileChooser();
        int ret = fileopen.showDialog(null, "Choose source");
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = fileopen.getSelectedFile();
            textField.setText(file.getAbsolutePath());
        }
    }

    private static void watch(final Process process, JTextPane textPane) {
        Thread printer = new Thread() {
            public void run() {
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = null;
                try {
                    StyledDocument doc = textPane.getStyledDocument();
                    SimpleAttributeSet attributeSet = new SimpleAttributeSet();
                    while ((line = input.readLine()) != null) {
                        try {
                            doc.insertString(doc.getLength(), line + System.lineSeparator(), attributeSet);
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                        //System.out.println(line);
                    }
                    // Destroy, as there's nothing else to read
                    processKill(process);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        printer.setDaemon(true);
        printer.start();
    }

    private static void processKill(final Process process) {
        process.descendants().forEach(descendant -> {
            descendant.destroy();
        });
        process.destroy();
    }
}
