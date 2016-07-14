package GUI;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
                                //Server for Chat
public class ChatServer {
    ArrayList<ObjectOutputStream> clientOutputStream;
                                                    //start Server
    public static void main(String[] args) {
        new ChatServer().go();
    }
                                                    //all main Server actions
    private void go() {
        clientOutputStream = new ArrayList<>();

        try {                                           //create ServerSocket
            ServerSocket serverSocket = new ServerSocket(58478);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                                                        //stream for sending massages to clients
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                clientOutputStream.add(out);

                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();

                System.out.println("got a connection");
            }
        } catch (IOException e) {e.printStackTrace();}
    }
                                            //return massage to all connected clients
    public void tellEveryone(Object one){
     Iterator it = clientOutputStream.iterator();

     while (it.hasNext()) {
         try {
             ObjectOutputStream out = (ObjectOutputStream) it.next();
             out.writeObject(one);
         } catch (IOException e) {e.printStackTrace();}
     }
 }
                                            // waiting for massages and than send it to all connected clients
    public class ClientHandler implements Runnable {
        ObjectInputStream in;
        Socket clientSocket;

        public ClientHandler(Socket socket){
            try {
                clientSocket = socket;
                                            //stream for getting massages
                in = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException e) {e.printStackTrace();}
        }

        @Override
        public void run() {
            Object o1 = null;

            try {
                while ((o1 = in.readObject()) != null) {
                    System.out.println("read object");
                    tellEveryone(o1);
                }
            } catch (IOException e) {e.printStackTrace();
            } catch (ClassNotFoundException e) {e.printStackTrace();}
        }
    }

}
