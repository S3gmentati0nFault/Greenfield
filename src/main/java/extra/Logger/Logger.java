package extra.Logger;

import java.time.LocalDateTime;

import static utilities.Variables.DEBUGGING;

public class Logger {
    static LocalDateTime now;
    public static void red(String message){
        now = LocalDateTime.now();
        System.out.println(Colors.ANSI_RED + " <<!>> " + now.getHour() + ":" + now.getMinute() + ":" + now.getSecond() + " <<!>> " + message + " <<!>> " + Colors.ANSI_RESET);
    }

    public static void red(String message, Exception e){
        now = LocalDateTime.now();
        System.out.println(Colors.ANSI_RED + " <<!>> " + now.getHour() + ":" + now.getMinute() + ":" + now.getSecond() + " <<!>> " + message + " <<!>> " + Colors.ANSI_RESET);
        e.printStackTrace();
    }

    public static void red(String message, Throwable t){
        now = LocalDateTime.now();
        System.out.println(Colors.ANSI_RED + " <<!>> " + now.getHour() + ":" + now.getMinute() + ":" + now.getSecond() + " <<!>> " + message + " <<!>> " + Colors.ANSI_RESET);
        t.printStackTrace();
    }

    public static void blue(String message){
        now = LocalDateTime.now();
        System.out.println(Colors.ANSI_BLUE + " <<?>> " + now.getHour() + ":" + now.getMinute() + ":" + now.getSecond() + " <<?>> " + message + " <<?>> " + Colors.ANSI_RESET);
    }

    public static void yellow(String message){
        now = LocalDateTime.now();
        System.out.println(Colors.ANSI_YELLOW + " <<§>> " + now.getHour() + ":" + now.getMinute() + ":" + now.getSecond() + " <<§>> " + message + " <<§>> " + Colors.ANSI_RESET);
    }

    public static void purple(String message){
        now = LocalDateTime.now();
        System.out.println(Colors.ANSI_PURPLE + " <<~>> " + now.getHour() + ":" + now.getMinute() + ":" + now.getSecond() + " <<~>> " + message + " <<~>> " + Colors.ANSI_RESET);
   }

    public static void green(String message) {
        now = LocalDateTime.now();
        System.out.println(Colors.ANSI_GREEN + " <<@>> " + now.getHour() + ":" + now.getMinute() + ":" + now.getSecond() + " <<@>> " + message + " <<@>> " + Colors.ANSI_RESET);
    }

    public static void cyan(String message){
        now = LocalDateTime.now();
        System.out.println(Colors.ANSI_CYAN + " <<ß>> " + message + " <<ß>> " + Colors.ANSI_RESET);
    }

    public static void whiteDebuggingPrint(String message, Boolean debuggingFlag) {
        if(debuggingFlag) {
            now = LocalDateTime.now();
            System.out.println("< " + now.getHour() + ":" + now.getMinute() + ":" + now.getSecond() + " > ->-> " + message + " <-<-");
        }
    }
}

