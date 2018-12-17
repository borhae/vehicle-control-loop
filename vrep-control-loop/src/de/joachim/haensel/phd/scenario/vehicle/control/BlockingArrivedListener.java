package de.joachim.haensel.phd.scenario.vehicle.control;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public class BlockingArrivedListener implements IArrivedListener
{
    private CountDownLatch _countDownLatch;
    private long _timeout;
    private TimeUnit _timeUnit;

    public BlockingArrivedListener(long timeout, TimeUnit timeUnit)
    {
        _timeout = timeout;
        _timeUnit = timeUnit;
    }
    
    public void waitForArrival()
    {
        _countDownLatch = new CountDownLatch(1);
        try
        {
            boolean noTimout = _countDownLatch.await(_timeout, _timeUnit);
            if(noTimout)
            {
                System.out.println("clean arrival");
            }
            else
            {
                String maybeS = _timeout == 1 ? "" : "s";
                System.out.println("stopped because we couldn't reach target within " + _timeout + " " + _timeUnit.toString() + maybeS);
            }
        }
        catch (InterruptedException exc)
        {
            //we were interrupted and couldn't wait. This should not happen really, so we report
            exc.printStackTrace();
            System.out.println("I was interrupted");
        }
    }

    @Override
    public void arrived(Position2D frontWheelCenterPosition)
    {
        System.out.println("got arrived event");
        _countDownLatch.countDown();
    }
}
