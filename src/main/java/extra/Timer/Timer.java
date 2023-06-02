package extra.Timer;

public class Timer{
   /**
    * Timer function
    * <p>
    * Function that puts the thread to sleep for a certain amount of seconds to
    * enable a very stupid timer.
    * </p>
    * @param amount ~ the amount of time that will elapse in seconds
    */
   public static void startTimer(long amount){
      try{
         Thread.sleep(amount * 1000);
         System.out.println("I have woken up again!");
      }
      catch(Exception e){
         System.out.println(e.getMessage());
      }
   }
}
