
import java.io.*;
import java.net.*;
import java.util.*;

class JoinProcessService implements Callable<List<Integer>>{

    private ServerSocket serverSocket;

    public JoinProcessService(ServerSocket socket) {
        this.serverSocket = socket;
    }

    @Override
    public List<Integer> call() throws Exception {
        try {
            Socket clientSocket = this.serverSocket.accept();
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            DataInputStream in = new DataInputStream(client.getInputStream());

            String request = in.readUTF();

            //  REPONSE/JOIN:APPROVED:4:5
            String r1 = "RESPONSE/JOIN:APPROVED" + fS + ":" + sS;
            //  REPONSE/JOIN:DELEGATE:4
            String r2 = "RESPONSE/JOIN:DELEGATE:" + ID;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
