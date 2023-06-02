package extra.Logger;

public class Logger {
    public static void error(String message){
        System.out.println(Colors.ANSI_RED + " <<!>> " + message + " <<!>> " + Colors.ANSI_RESET);
    }

    public static void notice(String message){
        System.out.println(Colors.ANSI_BLUE + " <<?>> " + message + " <<?>> " + Colors.ANSI_RESET);
    }

    public static void interact(String message){
        System.out.print(Colors.ANSI_YELLOW + " <<U>> " + message + " >> " + Colors.ANSI_RESET);
    }

    public static void request(String message){
        System.out.println(Colors.ANSI_PURPLE + " <<~>> " + message + " <<~>> " + Colors.ANSI_RESET);
    }

    public static void message(String message, String name){
        System.out.println(Colors.ANSI_CYAN + name + Colors.ANSI_RESET + Colors.ANSI_YELLOW + " >> " + Colors.ANSI_RESET + message + Colors.ANSI_YELLOW + " <<.>> " + Colors.ANSI_RESET);
    }
}
