
import java.util.ArrayList;
import java.util.Stack;

public class Row {
    public Stack<Seat> seats = new Stack<>();
    public int remainingSeats = 0;
    public int totalSeatsAtStart = 0;
    public ArrayList<Seat> seatsFromStart;



    //This is a "bucket" for seats. This is so I can get close to O(numSeats) when requesting
    //simple constructor
    public Row(ArrayList<Seat> someRow)
    {

        this.seats.addAll(someRow);
        remainingSeats = someRow.size();
        totalSeatsAtStart = someRow.size();
        seatsFromStart = someRow;
    }
    //if the stack is not empty, pop an remove remaining seats counter.
    public Seat getSeat()
    {
        if (!seats.isEmpty())
        {
            Seat someSeat = seats.pop();
            remainingSeats--;
            return someSeat;
        }
        return null;

    }

    //return seat if it exists
    public Seat getSeat(int col)
    {
        if (seats.contains(seatsFromStart.get(col)))
        {
            return seatsFromStart.get(col);
        }
        return null;
    }





}
