import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class w {

    public static void main()
    {
        final CyclicBarrier gate = new CyclicBarrier(5);
        Venue someVenue = new Venue(9,30, true);



        try
        {
            Thread t1 = new Thread(){
                public void run(){
                    try {
                        gate.await();
                        System.out.println("thread 1: Before seatHold");
                        SeatHold someSeatHold = someVenue.findWeightedSeats(4,"thread1");
                        System.out.println(someVenue.remainingSeats);
                        someVenue.displayWeightedVenue();
                        someVenue.reserveSeats(someSeatHold.seatHoldID, "thread1");
                        someVenue.displayWeightedVenue();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    //do stuff
                }};
            //not reserving seats here
            Thread t2 = new Thread(){
                public void run(){
                    try {
                        gate.await();
                        System.out.println("thread 2: Before seatHold");

                        SeatHold someSeatHold = someVenue.findWeightedSeats(5,"thread2");
                        System.out.println("thread 2: After seatHold");
                        someVenue.displayWeightedVenue();
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

                        SeatHold someSeatHold = someVenue.findWeightedSeats(3,"thread3");
                        System.out.println("thread 3: After seatHold");

                        someVenue.displayWeightedVenue();
                        someVenue.reserveSeats(someSeatHold.seatHoldID, "thread3");
                        someVenue.displayWeightedVenue();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                    //do stuff
                }};
            //assuming our wait time is 10 seconds, this will test if we can get the same 5 seats in t2 as we can in t4.
            Thread t4 = new Thread(){
                public void run(){
                    try {
                        sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        gate.await();
                        System.out.println("thread 4: Before seatHold");

                        SeatHold someSeatHold = someVenue.findWeightedSeats(5,"thread4");
                        System.out.println("thread 4: After seatHold");

                        someVenue.displayWeightedVenue();
                        someVenue.reserveSeats(someSeatHold.seatHoldID, "thread4");
                        someVenue.displayWeightedVenue();
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
            t4.start();


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
