package extra.Logger;

public class Logger {
    public static void red(String message){
        System.out.println(Colors.ANSI_RED + " <<!>> " + message + " <<!>> " + Colors.ANSI_RESET);
    }

    public static void blue(String message){
        System.out.println(Colors.ANSI_BLUE + " <<?>> " + message + " <<?>> " + Colors.ANSI_RESET);
    }

    public static void yellow(String message){
        System.out.println(Colors.ANSI_YELLOW + " <<§>> " + message + " <<§>> " + Colors.ANSI_RESET);
    }

    public static void purple(String message){
        System.out.println(Colors.ANSI_PURPLE + " <<~>> " + message + " <<~>> " + Colors.ANSI_RESET);
    }

    public static void green(String message) {
        System.out.println(Colors.ANSI_GREEN + " <<@>> " + message + " <<@>> " + Colors.ANSI_RESET);
    }

    public static void cyan(String message){
        System.out.println(Colors.ANSI_CYAN + " <<ß>> " + message + " <<ß>> " + Colors.ANSI_RESET);
    }
}
