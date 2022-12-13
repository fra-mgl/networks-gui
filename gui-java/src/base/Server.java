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
        System.out.println("Server creato con successo!");
        return true;
    }

    public void runServer()
    {
        while(true)
        {
            Sem.tWait();
            try {
//                System.out.println("Server acquired");
//                System.out.println("Server in attesa di richieste...");
                Socket clientSocket = server.accept();
//                System.out.println("Un client si e' connesso...");
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                out.write(OUTPUT_HEADERS + OUTPUT.length() + OUTPUT_END_OF_HEADERS + OUTPUT);
                out.flush();
                out.close();
                clientSocket.close();
//                    TimeUnit.SECONDS.sleep(5);
//                System.out.println("Chiusura connessione effettuata");
            } catch (IOException e) {
                e.printStackTrace();
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
            }
            Sem.sPost();
        }
    }
}

class Sem {
    static private Semaphore s = new Semaphore(0);;
    static private Semaphore t= new Semaphore(1);;

    public Sem() {

    }

    static public void sWait(){
        try{
            s.acquire();
        } catch (InterruptedException e){
            e.printStackTrace();
            System.err.println("Error");
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
            System.err.println("Error");
        }
    }

    static public void tPost(){
        t.release();
    }
}
