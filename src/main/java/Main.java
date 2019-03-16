import javax.swing.*;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JakstabGUIForm jakstabGUIForm = new JakstabGUIForm();
                jakstabGUIForm.setVisible(true);
            }
        });

    }
}
