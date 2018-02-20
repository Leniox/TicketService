
import org.junit.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class ConcurrenyTest {
    @Test
    public void Test1()
    {
        final CyclicBarrier gate = new CyclicBarrier(4);
        Venue someVenue = new Venue(9,30, false);



        try
        {
            Thread t1 = new Thread(){
                public void run(){
                    try {
                        gate.await();
                        System.out.println("thread 1: Before seatHold");
                        SeatHold someSeatHold = someVenue.findAndHoldConsecutiveSeats(4,"thread1");
                        System.out.println(someVenue.remainingSeats);
                        someVenue.displayVenue();
                        someVenue.reserveSeats(someSeatHold.seatHoldID, "thread1");
                        someVenue.displayVenue();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    //do stuff
                }};
            Thread t2 = new Thread(){
                public void run(){
                    try {
                        gate.await();
                        System.out.println("thread 2: Before seatHold");

                        SeatHold someSeatHold = someVenue.findAndHoldConsecutiveSeats(5,"thread2");
                        System.out.println("thread 2: After seatHold");
                        someVenue.displayVenue();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                    //do stuff
                }};

            Thread t3 = new Thread(){
                public void run(){
                    try {
                        gate.await();
                        System.out.println("thread 3: Before seatHold");

                        SeatHold someSeatHold = someVenue.findAndHoldSeats(3,"thread3");
                        System.out.println("thread 3: After seatHold");

                        someVenue.displayVenue();
                        someVenue.reserveSeats(someSeatHold.seatHoldID, "thread3");
                        someVenue.displayVenue();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                    //do stuff
                }};

            t1.start();
            t2.start();
            t3.start();

// At this point, t1 and t2  and t3 are blocking on the gate.
// Since we gave "4" as the argument, gate is not opened yet.
// Now if we block on the gate from the main thread, it will open
// and all threads will start to do stuff!

            gate.await();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
