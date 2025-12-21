import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.io.*;
import java.util.*;

public class Coba33 extends JFrame {
    private JTextArea textArea;
    private JTextField searchField;
    private JList<String> folderList;
    private JList<String> tagList;
    private DefaultListModel<String> folderListModel;
    private DefaultListModel<String> tagListModel;
    private File currentFile;
    private ArrayList<File> fileList;
    private ArrayList<String> tagListData;
    private HashMap<String, ArrayList<File>> folderMap;
    private Stack<String> versionHistory;

    public Coba33() {
        super("Note App");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem newNoteItem = new JMenuItem("New Note");
        JMenuItem openNoteItem = new JMenuItem("Open Note");
        JMenuItem saveNoteItem = new JMenuItem("Save Note");
        JMenuItem attachFileItem = new JMenuItem("Attach File");
        JMenuItem viewHistoryItem = new JMenuItem("View History");
        JMenuItem exitItem = new JMenuItem("Exit");
        fileMenu.add(newNoteItem);
        fileMenu.add(openNoteItem);
        fileMenu.add(saveNoteItem);
        fileMenu.add(attachFileItem);
        fileMenu.add(viewHistoryItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Create toolbar
        JToolBar toolBar = new JToolBar();
        JButton boldButton = new JButton("Bold");
        JButton italicButton = new JButton("Italic");
        JButton underlineButton = new JButton("Underline");
        JButton bulletButton = new JButton("Bullet");
        toolBar.add(boldButton);
        toolBar.add(italicButton);
        toolBar.add(underlineButton);
        toolBar.add(bulletButton);
        add(toolBar, BorderLayout.NORTH);

        // Create text area
        textArea = new JTextArea(20, 40);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // Create search field
        searchField = new JTextField(20);
        searchField.addActionListener(new SearchActionListener());
        add(searchField, BorderLayout.SOUTH);

        // Create folder and tag lists
        folderListModel = new DefaultListModel<>();
        folderList = new JList<>(folderListModel);
        folderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        folderList.addListSelectionListener(new FolderSelectionListener());

        tagListModel = new DefaultListModel<>();
        tagList = new JList<>(tagListModel);
        tagList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tagList.addListSelectionListener(new TagSelectionListener());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, folderList, tagList);
        add(splitPane, BorderLayout.EAST);

        // Initialize data structures
        fileList = new ArrayList<>();
        tagListData = new ArrayList<>();
        folderMap = new HashMap<>();
        versionHistory = new Stack<>();

        // Add action listeners
        newNoteItem.addActionListener(new NewNoteActionListener());
        openNoteItem.addActionListener(new OpenNoteActionListener());
        saveNoteItem.addActionListener(new SaveNoteActionListener());
        attachFileItem.addActionListener(new AttachFileActionListener());
        viewHistoryItem.addActionListener(new ViewHistoryActionListener());
        exitItem.addActionListener(new ExitActionListener());
        boldButton.addActionListener(new BoldActionListener());
        italicButton.addActionListener(new ItalicActionListener());
        underlineButton.addActionListener(new UnderlineActionListener());
        bulletButton.addActionListener(new BulletActionListener());
    }

    private class NewNoteActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            textArea.setText("");
            currentFile = null;
        }
    }

    private class OpenNoteActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(Coba33.this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    textArea.setText(readFile(selectedFile));
                    currentFile = selectedFile;
                    versionHistory.push(textArea.getText());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(Coba33.this, "Error reading file: " + ex.getMessage());
                }
            }
        }
    }

    private class SaveNoteActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (currentFile == null) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showSaveDialog(Coba33.this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        writeFile(selectedFile, textArea.getText());
                        currentFile = selectedFile;
                        versionHistory.push(textArea.getText());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(Coba33.this, "Error saving file: " + ex.getMessage());
                    }
                }
            } else {
                try {
                    writeFile(currentFile, textArea.getText());
                    versionHistory.push(textArea.getText());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(Coba33.this, "Error saving file: " + ex.getMessage());
                }
            }
        }
    }

    private class AttachFileActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(Coba33.this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                fileList.add(selectedFile);
                JOptionPane.showMessageDialog(Coba33.this, "File attached: " + selectedFile.getName());
            }
        }
    }

    private class ViewHistoryActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (versionHistory.isEmpty()) {
                JOptionPane.showMessageDialog(Coba33.this, "No version history available.");
                return;
            }
            JList<String> versionList = new JList<>(versionHistory.toArray(new String[0]));
            JScrollPane scrollPane = new JScrollPane(versionList);
            JOptionPane.showMessageDialog(Coba33.this, scrollPane, "Version History", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class ExitActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    private class BoldActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            textArea.setFont(textArea.getFont().deriveFont(Font.BOLD));
        }
    }

    private class ItalicActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            textArea.setFont(textArea.getFont().deriveFont(Font.ITALIC));
        }
    }

    private class UnderlineActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            SimpleAttributeSet attributes = new SimpleAttributeSet();
            StyleConstants.setUnderline(attributes, true);
            textArea.setCharacterAttributes(attributes, true);
        }
    }

    private class BulletActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            textArea.append("\n• ");
        }
    }

    private class SearchActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String keyword = searchField.getText();
            if (keyword.isEmpty()) return;

            for (File file : fileList) {
                try {
                    String content = readFile(file);
                    if (content.contains(keyword)) {
                        textArea.setText(content);
                        currentFile = file;
                        return;
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(Coba33.this, "Error reading file: " + ex.getMessage());
                }
            }
            JOptionPane.showMessageDialog(Coba33.this, "No notes found with the keyword: " + keyword);
        }
    }

    private class FolderSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            String selectedFolder = folderList.getSelectedValue();
            if (selectedFolder == null) return;

            ArrayList<File> filesInFolder = folderMap.get(selectedFolder);
            tagListModel.clear();
            if (filesInFolder != null) {
                for (File file : filesInFolder) {
                    tagListModel.addElement(file.getName());
                }
            }
        }
    }

    private class TagSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            String selectedTag = tagList.getSelectedValue();
            if (selectedTag == null) return;

            for (File file : fileList) {
                if (file.getName().equals(selectedTag)) {
                    try {
                        textArea.setText(readFile(file));
                        currentFile = file;
                        versionHistory.push(textArea.getText());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(Coba33.this, "Error reading file: " + ex.getMessage());
                    }
                    return;
                }
            }
        }
    }

    private String readFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }
        reader.close();
        return content.toString();
    }

    private void writeFile(File file, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(content);
        writer.close();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Coba33().setVisible(true);
        });
    }
}
