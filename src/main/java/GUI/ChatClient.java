package GUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.io.*;
import java.net.Socket;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.awt.event.*;
import java.util.Date;


public class ChatClient {
    JFrame theFrame;
    JPanel mainPanel;
    JList incomingList;
    JTextField userMessage;
    JTable contactsTable;
    JFrame contactsFrame;
    JPanel contactsPanel;
    JScrollPane contactsScroll;
    Container container;
    JTextField searchField;
    TableRowSorter<TableModel> rowSorter;

    Vector<String> listVector = new Vector<>();
    String userName;
    ObjectOutputStream out;
    ObjectInputStream in;


    public static void main(String[] args) {
                                                //start ChatClient with user name "Ron7aldo" as example
        new ChatClient().startUp("Ron7aldo");
    }
                                                        //start connection and call for  main GUI
    public void startUp(String name) {
        userName = name;

        try {                             //create connection with ChatServer
            Socket sock = new Socket("127.0.0.1", 58478);
            out = new ObjectOutputStream(sock.getOutputStream());
            in = new ObjectInputStream(sock.getInputStream());
            Thread remote = new Thread(new RemoteReader());
            remote.start();
        } catch (IOException e) {System.out.println("couldn't connect.");}

        buildGUI();
    }
                                                        //send massages to the server
    public class MySendListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String messageToSend = null;
            SimpleDateFormat dt = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

            try {
                out.writeObject(userName + " (" + dt.format(new Date()) + ") : " + userMessage.getText());
            } catch (IOException e1) {System.out.println("Sorry. Could not send it to the server.");}
            userMessage.setText("");
        }
    }
                                                        //call for creating Contacts window
    public class ContactsButtonListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            buildContactsGUI();
        }
    }
                                                        //add new Contact to mySQL DB
    public class AddContactsButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            buildAddContactsGUI();
        }
    }
                                                        //delete selected contact from mySQL DB
    public class DeleteContactButtonListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            int id = (int) contactsTable.getValueAt(contactsTable.getSelectedRow(), 0);
            try {
                new MyDB().deleteContact(id);
            } catch (SQLException e1) {e1.printStackTrace();}
        }
    }
                                                        //update Contacts window after changes
    public class UpdateContactsButtonListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                contactsTable = new MyDB().getContactsTable();
                contactsFrame.dispose();
                buildContactsGUI();
            } catch (SQLException e1) {e1.printStackTrace();}

        }
    }
                                                        //search over contacts table
    public class SearchDocumentListener implements DocumentListener{

        @Override
        public void insertUpdate(DocumentEvent e) {
            String text = searchField.getText();

            if (text.trim().length() == 0) {
                rowSorter.setRowFilter(null);
            } else {
                rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            String text = searchField.getText();

            if (text.trim().length() == 0) {
                rowSorter.setRowFilter(null);
            } else {
                rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        }

        @Override
        public void changedUpdate(DocumentEvent e) {}
    }
                                                        //select example word in SearchField
    public class SearchFieldFocusListener implements FocusListener{

        @Override
        public void focusGained(FocusEvent e) {
            searchField.selectAll();
        }

        @Override
        public void focusLost(FocusEvent e) {}
    }
                                            //build main window for chatting all-to-all
    public void buildGUI() {
        theFrame = new JFrame("Chat");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainPanel = new JPanel(new FlowLayout());
        mainPanel.setPreferredSize(new Dimension(420,350));

        userMessage = new JTextField(28);
        userMessage.setHorizontalAlignment(SwingConstants.LEFT);
        mainPanel.add(userMessage);

        JButton sendItButton = new JButton("Send");
        sendItButton.addActionListener(new MySendListener());
        mainPanel.add(sendItButton);

        incomingList = new JList();
        incomingList.setListData(listVector);
        JScrollPane theList = new JScrollPane(incomingList);
        theList.setPreferredSize(new Dimension(380,200));
        mainPanel.add(theList);

        JButton contactsButton = new JButton("Contacts");
        contactsButton.addActionListener(new ContactsButtonListener());
        mainPanel.add(contactsButton);


        theFrame.getContentPane().add(mainPanel);
        theFrame.setBounds(50, 50, 300, 300);
        theFrame.pack();
        theFrame.setVisible(true);
    }
                                            //build window for working with Contacts DataBase
    public void buildContactsGUI(){
        contactsFrame = new JFrame("Contacts");
        contactsFrame.getContentPane().setLayout(new FlowLayout());
        contactsFrame.setSize(500, 400);

        contactsPanel = new JPanel();
        contactsPanel.setLayout(new FlowLayout(10));
        contactsPanel.setPreferredSize(new Dimension(370,70));
        contactsFrame.add(contactsPanel);

        JButton addContactButton = new JButton("Add Contact");
        addContactButton.addActionListener(new AddContactsButtonListener());
        contactsPanel.add(addContactButton);

        JButton deleteContactButton = new JButton("Delete Contact");
        deleteContactButton.addActionListener(new DeleteContactButtonListener());
        contactsPanel.add(deleteContactButton);

        JButton updateContactsButton = new JButton("Update");
        updateContactsButton.addActionListener(new UpdateContactsButtonListener());
        contactsPanel.add(updateContactsButton);

        searchField = new JTextField("Search", 25);
        searchField.addFocusListener(new SearchFieldFocusListener());
        contactsPanel.add(searchField);

        try {
            contactsTable = new MyDB().getContactsTable();
        } catch (SQLException e) {e.printStackTrace();}

        contactsScroll = new JScrollPane(contactsTable);
        contactsTable.setPreferredScrollableViewportSize(new Dimension(465, 250));
        contactsFrame.getContentPane().add(contactsScroll);

        rowSorter = new TableRowSorter<>(contactsTable.getModel());
        contactsTable.setRowSorter(rowSorter);
        searchField.getDocument().addDocumentListener(new SearchDocumentListener());

        container = contactsFrame.getContentPane();

        contactsFrame.setVisible(true);
    }
                                            //build window for adding contacts
    public void buildAddContactsGUI(){
        final JFrame addContactFrame = new JFrame("Add Contact");
        addContactFrame.setLayout(new BorderLayout());

        JLabel addLabel = new JLabel("Enter new contact's data:");
        addLabel.setFont(new Font("Arial", 1, 22));
        addLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        addContactFrame.getContentPane().add(addLabel, BorderLayout.NORTH);

        JPanel addContactPanel = new JPanel(new FlowLayout());
        addContactPanel.setSize(475,370);
        addContactFrame.add(addContactPanel, BorderLayout.CENTER);
        addContactFrame.setSize(475, 370);


        JLabel addNameLabel = new JLabel("Name");
        addContactPanel.add(addNameLabel);
        final JTextField addNameField = new JTextField(15);
        addContactPanel.add(addNameField);

        JLabel addSurnameLabel = new JLabel("Surname");
        addContactPanel.add(addSurnameLabel);
        final JTextField addSurnameField = new JTextField(15);
        addContactPanel.add(addSurnameField);

        JLabel addEmailLabel = new JLabel("E-mail");
        addContactPanel.add(addEmailLabel);
        final JTextField addEmailField = new JTextField(15);
        addContactPanel.add(addEmailField);

        JLabel addPhoneLabel = new JLabel("Phone #");
        addContactPanel.add(addPhoneLabel);
        final JTextField addPhoneField = new JTextField(15);
        addContactPanel.add(addPhoneField);

        JLabel addPositionLabel = new JLabel("Position");
        addContactPanel.add(addPositionLabel);
        final JTextField addPositionField = new JTextField(15);
        addContactPanel.add(addPositionField);

        JLabel addCompanyLabel = new JLabel("Company");
        addContactPanel.add(addCompanyLabel);
        final JTextField addCompanyField = new JTextField(15);
        addContactPanel.add(addCompanyField);

        JButton addContactToDB = new JButton("Add New Contact");
        addContactToDB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    new MyDB().addContactToDB(addNameField.getText(), addSurnameField.getText(),
                            addEmailField.getText(), addPhoneField.getText(),
                            addPositionField.getText(),addCompanyField.getText());
                } catch (SQLException e1) {e1.printStackTrace();}
                addContactFrame.dispose();
            }
        });
        addContactPanel.add(addContactToDB);

        addContactFrame.setVisible(true);
    }

    public class RemoteReader implements Runnable {         //get messages from server
        String nameToShow = null;
        Object obj = null;

        public void run() {

            try {
                while ((obj = in.readObject()) != null) {
                    System.out.println("got an object from server");
                    System.out.println(obj.getClass());
                    String nameToShow = (String) obj;
                    listVector.add(nameToShow);
                    incomingList.setListData(listVector);
                }
            } catch (IOException e) {e.printStackTrace();
            } catch (ClassNotFoundException e) {e.printStackTrace();}
        }
    }
}
