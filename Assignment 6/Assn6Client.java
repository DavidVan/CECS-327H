import java.rmi.*;

public class Assn6Client {
    public static void main(String[] args) {
        MethodInterface methodInterface;
        try {
            methodInterface = (MethodInterface) Naming.lookup("rmi://" + args[0] + "/cecs327");
            int answer = -1;
            switch(args[1].toLowerCase()) {
                case "fibonacci":
                    answer = methodInterface.fibonacci(Integer.parseInt(args[2]));
                    break;
                case "factorial":
                    answer = methodInterface.factorial(Integer.parseInt(args[2]));
                    break;
                default:
                    System.err.println("You need to enter either \"fibonacci\" or \"factorial\"!");
                    break;
            }
            System.out.println("The answer for " + args[1] + "(" + args[2] + ") is: " + answer);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}