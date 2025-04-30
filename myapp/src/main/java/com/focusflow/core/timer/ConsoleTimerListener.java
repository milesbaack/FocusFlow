/**
 * ConsoleTimerListener implements TimerListener, used for initial debugging
 * @author  Emilio Lopez
 * @version 1.0.0
 */

package com.focusflow.core.timer;

public class ConsoleTimerListener implements TimerListener{
    /**
     * Print seconds remanining
     */
    @Override
    public void onTick(long secondsRemaining){
        System.out.println("Time remaining: " + secondsRemaining + " seconds.");
    }

    /**
     * Print when finsihed
     */
    @Override
    public void onFinish(){
        System.out.println("Timer finished!");
    }

}