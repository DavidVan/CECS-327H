import java.rmi.*;

public class Assn6Server {
    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", args[0]);
            Method method = new Method();
            Naming.rebind("rmi://localhost/cecs327", method);
            System.out.println("The server is ready.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}