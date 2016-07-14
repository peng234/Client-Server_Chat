package GUI;

import com.mysql.fabric.jdbc.FabricMySQLDriver;
import javax.swing.*;
import java.sql.*;
                                    //working with mySQL DB class
public class MyDB {
    Connection connection = null;
    Statement statement;
    ResultSet result;
    JTable contactsTable;
    Object[] headers;
    Object[][] data;


    public MyDB() throws SQLException {                         //create connection to DB
        String url = "jdbc:mysql://localhost:3306/mydbtest";
        String name = "root";
        String password = "root";
        Driver driver = new FabricMySQLDriver();
        connection = DriverManager.getConnection(url, name, password);
        statement = connection.createStatement();
    }
                                                        //add contact to DB
    public void addContactToDB(String name, String surname, String email, String phone, String position, String company)
            throws SQLException {

        statement.executeUpdate("INSERT INTO `mydbtest`.`contacts` (`Name`, `Surname`, `e-mail`, `Phone #`, `Position`, `Company`)" +
                " VALUES ('" + name + "', '"
                            + surname + "', '"
                            + email + "', '"
                            + phone + "', '"
                            + position + "', '"
                            + company + "');");
    }
                                                        //delete contact from DB
    public void deleteContact(int id) throws SQLException {
        statement.executeUpdate("DELETE FROM `mydbtest`.`contacts` WHERE `ID`='" + Integer.toString(id) + "'");
    }
                                                        //create table from DB
    public JTable getContactsTable() throws SQLException {
        int tableLength;                        //rows quantity
        int columnCount;                        //columns quantity

        result = statement.executeQuery("SELECT * FROM contacts");

        try {
            columnCount = result.getMetaData().getColumnCount();
            tableLength = getTableLength(result);
            result.next();

            headers = new Object[columnCount];              //fill array of headers
            for (int i = 0; i < columnCount; i++) {
                headers[i] = result.getMetaData().getColumnName(i + 1);
            }

            data = new Object[tableLength][columnCount];    //fill arrat of cells
            for (int i = 0; i < tableLength; i++) {
                for (int j = 0; j < columnCount; j++) {
                    if (j == 0)
                        data[i][j] = result.getInt("ID");
                    else
                        data[i][j] = result.getString(j + 1);
                }
                result.next();
            }
            contactsTable = new JTable(data, headers);      //create table of contacts

        } catch (Exception ex) {ex.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();                     //закрываем соединение
                } catch (SQLException ex) {ex.printStackTrace();}
            }
        }
        return contactsTable;                               //return created table
    }
                                                            //count quantity of rows in table
    private int getTableLength(ResultSet rs) throws SQLException {
        rs.last();
        int tL = rs.getRow();
        rs.beforeFirst();
        return tL;
    }

}
