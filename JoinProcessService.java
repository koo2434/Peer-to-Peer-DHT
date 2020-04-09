
import java.io.*;
import java.net.*;
import java.util.*;

class JoinProcessService implements Callable<List<Integer>>{

    private Socket socket;

    public JoinProcessService(Socket socket) {
        this.socket = socket;
    }

    @Override
    public List<Integer> call() throws Exception {
        
    }

}
