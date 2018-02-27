
import java.util.ArrayList;
import java.util.Timer;
import java.util.UUID;

public class SeatHold {
    public ArrayList<Seat> seatList = new ArrayList<>();
    public String customerEmail = "";
    public Row someRow;
    public String seatHoldID = "";
    public  boolean didContinue;
    public boolean doesContainWeighted = false;
    public SeatHold(ArrayList<Seat> seatList, String custEmail, Row someRow, Venue venue)
    {

        this.seatList = seatList;
        this.customerEmail = custEmail;
        this.someRow = someRow;
        //use UUID to generate a customer ID for the order confirmation
        seatHoldID = UUID.randomUUID().toString();
        //this is a timer that gets kicked off everytime you make a reservation. We update the boolean value if the user
        //moves forward with their request. If they have not moved forward, we release the seats back to the row.


        new Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        //didContinue is updated in the driver based on whether or not the user went forward with the transaction.
                        if (didContinue == false)
                        {
                            if (!doesContainWeighted)
                            {
                                System.out.println("Oh no! Time has expired! We're allowing these seats to be unreserved. Try again by pressing Enter");
                                venue.myLock.lock();
                                someRow.seats.addAll(seatList);
                                venue.myLock.unlock();
                                venue.displayVenue();

                            }
                            else {
                                System.out.println("Oh no! Time has expired! We're allowing these seats to be unreserved. Try again by pressing Enter");
                                venue.myLock.lock();
                                for (Seat someSeat : seatList)
                                {
                                    someSeat.available = true;
                                }
                                venue.remainingSeats = venue.remainingSeats + seatList.size();
                                venue.displayWeightedVenue();
                                venue.myLock.unlock();

                            }


                        }

                    }
                },
                10000
        );

    }
    @Override
    public String toString(){
        return seatList.toString();
    }

}
