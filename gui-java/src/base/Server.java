package base;
import java.net.*;
import java.io.*;
import java.util.concurrent.Semaphore;

public class Server
{
    private static final String OUTPUT = "<html>Received</html>";
    private static final String OUTPUT_HEADERS = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/html\r\n" +
            "Content-Length: ";
    private static final String OUTPUT_END_OF_HEADERS = "\r\n\r\n";
    private int port;
    private ServerSocket server;

    public Server (int port)
    {
        this.port = port;
        if(!startServer())
            System.err.println("Errore durante la creazione del Server");
    }

    private boolean startServer()
    {
        try
        {
            server = new ServerSocket(port);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            return false;
        }
//        System.out.println("Server creato con successo!");
        return true;
    }

    public void runServer()
    {
        while(true)
        {
            Sem.tWait();
            try {
                /* waiting a new client connection */
                Socket clientSocket = server.accept();

                /* new connection activated */
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                out.write(OUTPUT_HEADERS + OUTPUT.length() + OUTPUT_END_OF_HEADERS + OUTPUT);
                out.flush();
                out.close();
                clientSocket.close();
                /*connection closed */
            } catch (IOException e) {
                e.printStackTrace();
            }
            Sem.sPost();
        }
    }
}

class Sem {
    /* implemented to synchronize server and refresh request */
    static private Semaphore s = new Semaphore(0);;
    static private Semaphore t= new Semaphore(1);;

    public Sem() {

    }

    static public void sWait(){
        try{
            s.acquire();
        } catch (InterruptedException e){
            e.printStackTrace();
            System.err.println("sWait - Error");
        }
    }

    static public void sPost(){
        s.release();
    }

    static public void tWait(){
        try{
            t.acquire();
        } catch (InterruptedException e){
            e.printStackTrace();
            System.err.println("tWait - Error");
        }
    }

    static public void tPost(){
        t.release();
    }
}
