import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;

public class JakstabGUIForm extends JFrame {
    private JTextField sourceFileInput;
    private JPanel jakstabRootPanel;
    private JButton chooseFileButton;
    private JTextPane outputTextPane;
    private JButton runButton;
    private JScrollPane outputScrollPane;
    private JRadioButton graphvizRadioButton;
    private JRadioButton graphmlRadioButton;
    private JButton stopButton;
    private JTabbedPane outputTabbedPane;
    private JButton graphButton;
    private JTextField graphFileInput;
    private JButton chooseGraphFileButton;
    private JScrollPane graphScrollPane;
    private JPanel graphPane;
    private JPanel graphZoomPanel;
    private JButton zoomInButton;
    private JButton zoomOutButton;
    private JSlider zoomSlider;
    private JTextField jakstabFileInput;
    private JButton chooseJakstabFileButton;
    private JLabel zoomPercent;
    private JPanel inputSourcePanel;
    private JLabel sourceFileInputLabel;
    private JPanel graphWriterPanel;
    private JPanel graphSourcePanel;
    private JLabel graphFileInputLabel;
    private JPanel graphTypePanel;
    private JPanel jakstabPanel;
    private JPanel jakstabSourcePanel;
    private JLabel jakstabFileInputLabel;
    private JPanel inputPanel;
    private JPanel controlPanel;
    private JPanel optionsPanel;
    private JPanel graphRepresentationPanel;
    private JRadioButton cfgRadioButton;
    private JRadioButton cfaRadioButton;
    private JButton autofillButton;
    private JCheckBox autofillCheckBox;
    private ImagePanel graphImagePanel;

    private Process currentProcess = null;

    // TODO: system scaling
    public JakstabGUIForm() {
        // This uses the form designer form
        add(jakstabRootPanel);

        setTitle("Jakstab GUI");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        graphScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        graphScrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        graphImagePanel = new ImagePanel();

        cfgRadioButton.setSelected(true);
        graphvizRadioButton.setSelected(true);

        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChoose(sourceFileInput, null, true);
            }
        });

        chooseJakstabFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChoose(jakstabFileInput, null, false);
            }
        });

        chooseGraphFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graphmlRadioButton.isSelected())
                    fileChoose(graphFileInput, new FileNameExtensionFilter("graphml", "graphml"), true);
                else if (graphvizRadioButton.isSelected())
                    fileChoose(graphFileInput, new FileNameExtensionFilter("dot", "dot"), true);
                else
                    fileChoose(graphFileInput, null, true);
            }
        });

        autofillButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String source = sourceFileInput.getText();
                if (!source.isEmpty()) {
                    // Remove extension if present
                    if (source.contains(".")) {
                        int dotIndex = source.lastIndexOf('.');
                        if (dotIndex > 0)
                            source = source.substring(0, dotIndex);
                    }
                    if (cfgRadioButton.isSelected())
                        source += "_asmcfg";
                    else
                        source += "_cfa";
                    if (graphvizRadioButton.isSelected())
                        source += ".dot";
                    else
                        source += ".graphml";
                    graphFileInput.setText(source);
                }
            }
        });

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jakstabFileInput.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(jakstabRootPanel, "Jakstab executable path is empty!", "Error", JOptionPane.ERROR_MESSAGE);
                } else if (sourceFileInput.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(jakstabRootPanel, "Source path is empty!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    if (graphmlRadioButton.isSelected())
                        JOptionPane.showMessageDialog(jakstabRootPanel, "GraphML file type is not supported yet, generating .dot instead...", "Warning", JOptionPane.WARNING_MESSAGE);

                    String command = jakstabFileInput.getText() + File.separator + "jakstab";
                    String OS = System.getProperty("os.name").toLowerCase();
                    if (OS.contains("win"))
                        command = command + ".bat";

                    ProcessBuilder processBuilder = new ProcessBuilder(command, "-m", sourceFileInput.getText());
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
                } else if (graphmlRadioButton.isSelected()) {
                    JOptionPane.showMessageDialog(jakstabRootPanel, "GraphML file type is not supported yet!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        String graphFilePath = graphFileInput.getText();
                        Graphviz g = Graphviz.fromFile(new File(graphFilePath));
                        zoomSlider.setValue(50);
                        zoomPercent.setText("100%");
                        graphImagePanel = new ImagePanel(g.render(Format.PNG).toImage(), zoomSlider, zoomPercent);
                        graphScrollPane.setViewportView(graphImagePanel);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        zoomInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graphImagePanel.zoomIn())
                    graphScrollPane.repaint();
            }
        });

        zoomOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graphImagePanel.zoomOut())
                    graphScrollPane.repaint();
            }
        });

        zoomSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (graphImagePanel.zoom(e))
                    graphScrollPane.repaint();
            }
        });
    }

    private static void fileChoose(JTextField textField, javax.swing.filechooser.FileFilter fileFilter, boolean filesOnly) {
        JFileChooser fileopen = new JFileChooser();
        fileopen.setCurrentDirectory(new File("."));

        if (filesOnly)
            fileopen.setFileSelectionMode(JFileChooser.FILES_ONLY);
        else
            fileopen.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (fileFilter != null)
            fileopen.setFileFilter(fileFilter);
        int ret = fileopen.showDialog(null, "Choose");
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
                    Document doc = textPane.getDocument();
                    while ((line = input.readLine()) != null) {
                        try {
                            doc.insertString(doc.getLength(), line + System.lineSeparator(), null);
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
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
