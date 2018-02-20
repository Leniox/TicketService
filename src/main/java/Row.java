
import java.util.ArrayList;
import java.util.Stack;

public class Row {
    public Stack<Seat> seats = new Stack<>();
    public int remainingSeats = 0;
    public int totalSeatsAtStart = 0;
    public ArrayList<Seat> seatsFromStart;



    public Row(ArrayList<Seat> someRow)
    {

        this.seats.addAll(someRow);
        remainingSeats = someRow.size();
        totalSeatsAtStart = someRow.size();
        seatsFromStart = someRow;
    }
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
    public Seat getSeat(int col)
    {
        if (seats.contains(seatsFromStart.get(col)))
        {
            return seatsFromStart.get(col);
        }
        return null;
    }





}
