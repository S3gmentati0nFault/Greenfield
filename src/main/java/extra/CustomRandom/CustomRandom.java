package extra.CustomRandom;

import java.util.Random;

public class CustomRandom{
   private Random random;
   private static CustomRandom instance;

   private CustomRandom(){
      random = new Random(System.currentTimeMillis());
   }

   public static CustomRandom getInstance(){
      if(instance == null){
         instance = new CustomRandom();
      }
      return instance;
   }

   public double rnDouble(int lwBound, int upBound){
      double randomValue = Math.floor(random.nextDouble() * (upBound - lwBound + 1)) + lwBound;
      return randomValue;
   }
}
