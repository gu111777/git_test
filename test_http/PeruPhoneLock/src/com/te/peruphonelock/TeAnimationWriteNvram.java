package com.te.peruphonelock;


public class TeAnimationWriteNvram  {

    static {
          System.loadLibrary("teanimationwritenvram_jni");
      }
    
    public static void te_animation_wirte_bulid_pro_nvram(int defaultValue){
    	
    	native_teanimation_writenvram(defaultValue);
    }
    
    private static native void native_teanimation_writenvram(int defaultValue);
 
     public static int te_animation_get_bulid_pro_nvram(){//hzq add int and (String defaultValue)
    	
    	return native_teanimation_getnvram(); //hzq add int and (defaultValue)
    }
    
    private static native int native_teanimation_getnvram(); // void (String defaultValue)
}
