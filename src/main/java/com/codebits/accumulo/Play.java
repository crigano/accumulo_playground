package com.codebits.accumulo;

public class Play {

    public static void main(String[] args) {
        System.out.println(getPlatform());
      }
      
      public static String getPlatform() {
        return (System.getProperty("os.name") + "-" + System.getProperty("os.arch") + "-" + System.getProperty("sun.arch.data.model")).replace(' ', '_');
      }

}
