import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;

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
    private JButton graphButton;
    private JPanel graphSourcePanel;
    private JTextField graphFileInput;
    private JLabel graphFileInputLabel;
    private JButton chooseGraphFileButton;
    private JPanel graphTypePanel;
    private JLabel graphImageLabel;
    private JScrollPane graphScrollPane;
    private JPanel graphPane;
    private JPanel graphZoomPanel;
    private JButton zoomInButton;
    private JButton zoomOutButton;

    private Process currentProcess = null;

    private Image graphImage = null;
    private final double zoomFactor = 0.2;
    private double graphWidthFactor = 1;
    private double graphHeightFactor = 1;

    public JakstabGUIForm() {
        // This uses the form designer form
        add(jakstabRootPanel);

        setTitle("Jakstab GUI");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChoose(sourceFileInput, null);
            }
        });

        chooseGraphFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graphmlRadioButton.isSelected())
                    fileChoose(graphFileInput, new FileNameExtensionFilter("graphml", "graphml"));
                else if (graphvizRadioButton.isSelected())
                    fileChoose(graphFileInput, new FileNameExtensionFilter("dot", "dot"));
                else
                    fileChoose(graphFileInput, null);
            }
        });

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sourceFileInput.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(jakstabRootPanel, "Source path is empty!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    if (graphmlRadioButton.isSelected())
                        JOptionPane.showMessageDialog(jakstabRootPanel, "GraphML file type is not supported yet, generating .dot instead...", "Warning", JOptionPane.WARNING_MESSAGE);
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
                } else if (graphmlRadioButton.isSelected()) {
                    JOptionPane.showMessageDialog(jakstabRootPanel, "GraphML file type is not supported yet!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        String graphFilePath = graphFileInput.getText();
                        Graphviz g = Graphviz.fromFile(new File(graphFilePath));
                        graphImage = g.render(Format.PNG).toImage();
                        graphWidthFactor = 1;
                        graphHeightFactor = 1;
                        graphImageLabel.setIcon(new ImageIcon(graphImage));
                        //BufferedImage image = g.render(Format.PNG).toImage();
                        //graphImageLabel.setIcon(new ImageIcon(image.getScaledInstance(graphDrawPanel.getWidth(), graphDrawPanel.getHeight(), Image.SCALE_SMOOTH)));
                        //g.render(Format.PNG).toFile(new File(graphFilePath + ".png"));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        zoomInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graphImage == null) return;

                graphWidthFactor += zoomFactor;
                graphHeightFactor += zoomFactor;
                int width = (int) (graphImage.getWidth(null) * graphWidthFactor);
                int height = (int) (graphImage.getHeight(null) * graphHeightFactor);
                graphImageLabel.setIcon(new ImageIcon(graphImage.getScaledInstance(width, height, Image.SCALE_SMOOTH)));
            }
        });

        zoomOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graphImage == null || graphWidthFactor < zoomFactor || graphHeightFactor < zoomFactor)
                    return;

                graphWidthFactor -= zoomFactor;
                graphHeightFactor -= zoomFactor;
                int width = (int) (graphImage.getWidth(null) * graphWidthFactor);
                int height = (int) (graphImage.getHeight(null) * graphHeightFactor);
                if (width != 0 && height != 0)
                    graphImageLabel.setIcon(new ImageIcon(graphImage.getScaledInstance(width, height, Image.SCALE_SMOOTH)));
                else{
                    graphWidthFactor += zoomFactor;
                    graphHeightFactor += zoomFactor;
                }
            }
        });
    }

    private static void fileChoose(JTextField textField, javax.swing.filechooser.FileFilter fileFilter) {
        JFileChooser fileopen = new JFileChooser();
        fileopen.setFileSelectionMode(JFileChooser.FILES_ONLY);
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
