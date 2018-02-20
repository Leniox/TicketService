
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Venue implements TicketService {
    int remainingSeats = 0;
    static int rows = 0;
    static int cols = 0;
    int totalSeats = 0;
    static ArrayList<Row> grid = new ArrayList<>();
    //mapping customers email to potential, reserved seats
    HashMap<String, SeatHold> seatHoldMap = new HashMap<>();
    //mapping customer email to their seats
    HashMap<String, ArrayList<Seat>> reservedSeats = new HashMap<>();
    static ReentrantLock myLock = new ReentrantLock();
    //this map is going to tell me where to do my breadth-first search from. I order it by the weights
    //in reverse order and thus can BFS from that seat location.
    TreeMap<Integer, ArrayList<Seat>> weightMap = new TreeMap<>(Collections.reverseOrder());

    //creates seat objects of the size of the grid and creates the grid, which is an arraylist of row objects.
    public Venue(int rows, int cols, boolean createdWeighted)
    {
        this.rows = rows;
        this.cols = cols;
        if (createdWeighted)
        {
            createWeightedVenue();
        }
        else
        {
            totalSeats = rows * cols;
            remainingSeats = totalSeats;
            for (int i = 0; i < rows; i++)
            {
                ArrayList<Seat> someRow = new ArrayList<>();
                for (int j = 0; j < cols; j++)
                {
                    Seat someSeat = new Seat(i, j);
                    someRow.add(someSeat);
                }
                Row venueRow = new Row(someRow);
                grid.add(venueRow);
            }
        }

    }

    public void createWeightedVenue()
    {

        int halfOfRow = rows/2;
        int halfOfcol = cols/2;
        if (rows % 2 != 0)
        {
            halfOfRow++;
        }
        if (cols % 2 != 0)
        {
            halfOfcol++;
        }
        int colCost = 0;
        int rowCost = 0;
        totalSeats = rows * cols;
        remainingSeats = totalSeats;
        for (int i = 0; i < rows; i++)
        {
            ArrayList<Seat> someRow = new ArrayList<>();
            if (i < halfOfRow)
            {
                rowCost = (i*100 / halfOfcol);

            }
            else
            {
                rowCost = ((rows - i)*100/halfOfcol);
            }
            for (int j = 0; j < cols; j++)
            {

                if (j < halfOfcol)
                {
                    colCost = (j*100 / halfOfcol);
                }
                else
                {
                    colCost = ((cols - j)*100 / halfOfcol);
                }
                int totalCost = ((colCost + rowCost))/2;


                Seat someSeat = new Seat(i, j);
                if (weightMap.get(totalCost) == null)
                {
                    ArrayList<Seat> seatList = new ArrayList();
                    seatList.add(someSeat);
                    weightMap.put(totalCost, seatList);
                }
                else
                {
                    ArrayList<Seat> seatList = weightMap.get(totalCost);
                    seatList.add(someSeat);
                    weightMap.put(totalCost, seatList);
                }

                someSeat.weight = totalCost;
                someRow.add(someSeat);
            }
            Row venueRow = new Row(someRow);
            grid.add(venueRow);
        }
//        printWeightedGraph();

    }
    public void printWeightedGraph()
    {
        for (Row someRow : grid)
        {
            for (Seat someSeat : someRow.seats)
            {
                System.out.print(someSeat.weight + " ");

            }
            System.out.println();
        }
    }
    public SeatHold findWeightedSeats(int numSeats, String customerEmail)
    {
        ArrayList<Integer> potentialWeightsToRemove = new ArrayList<>();
        SeatHold seatHold = null;
        try
        {
            myLock.lock();

            for (Integer weight : weightMap.keySet())
            {
                ArrayList<Seat> someList = weightMap.get(weight);
                ArrayList<Seat> availableSeats = new ArrayList<>();

                //more optimization could see me ordering the seats of available seats by proximity to row/2 and col /2 (center most)
                if (someList.size() == 0)
                {
                    potentialWeightsToRemove.add(weight);
                    continue;
                }
                for (Seat aSeat : someList)
                {
                    if (aSeat.available)
                    {
                        availableSeats.add(aSeat);
                    }
                }
                if (availableSeats.size() == 0)
                {
                    potentialWeightsToRemove.add(weight);
                    continue;
                }

                //get first item
                Seat someSeat = availableSeats.get(0);
                //remove the seat from the map
                //doBFS
                ArrayList<Seat> seats = BFS(someSeat, numSeats);
                seatHold = new SeatHold(seats, customerEmail, null, this);
                seatHold.doesContainWeighted = true;
                remainingSeats = remainingSeats - seatHold.seatList.size();
                break;


            }
            if (potentialWeightsToRemove.size() != 0)
            {
                for (Integer weight : potentialWeightsToRemove)
                {
                    weightMap.remove(weight);
                }
            }
            return seatHold;

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            myLock.unlock();
        }
        return null;

    }
    public void displayWeightedVenue()
    {
        try
        {
            myLock.lock();
            int j = 0;

            while (j < cols/3)
            {
                System.out.print("-");
                j++;

            }
            System.out.print("[[ STAGE ]]");
            j = 0;
            while (j < cols/3)
            {
                System.out.print("-");
                j++;

            }
            System.out.println();
            j = 0;
            while (j < cols)
            {
                System.out.print("-");
                j++;

            }
            System.out.println();
            for (Row someRow : grid)
            {
                Seat[] rowSeats = Arrays.stream(someRow.seats.toArray()).toArray(Seat[]::new);
                ArrayList<Seat> remainingSeats = new ArrayList<>();
                for (Seat seat : rowSeats)
                {
                    remainingSeats.add(seat);
                }
                remainingSeats.sort(new Comparator<Seat>() {
                    @Override
                    public int compare(Seat o1, Seat o2) {
                        return o1.col - o2.col;
                    }
                });


                for (Seat someSeat : remainingSeats)
                {
                    if (someSeat.available)
                    {
                                                System.out.print("s");

                    }
                    else
                    {
                                                System.out.print("x");

                    }

                }
                System.out.println();



//                for (int i = 0; i < cols; i++)
//                {
//                    //THIS IS BOTTLENECK. COLS^2 for contains.
//                    if (remainingSeats)
//                    {
//                        System.out.print("s");
//
//                    }
//                    else
//                    {
//                        System.out.print("x");
//
//                    }
//
//                }
//                System.out.println();

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            myLock.unlock();
        }
    }
    public ArrayList<Seat> BFS(Seat initialSeat, int numSeats)
    {
        Queue<Seat> myQueue = new LinkedList<>();
        ArrayList<Seat> finalSeatList = new ArrayList<>();
        myQueue.add(initialSeat);
        boolean [][] visited = new boolean[rows][cols];

        while (!myQueue.isEmpty())
        {
            Seat someSeat = myQueue.poll();
            if (!visited[someSeat.row][someSeat.col] && someSeat.available )
            {
                visited[someSeat.row][someSeat.col] = true;
                someSeat.available = false;
                finalSeatList.add(someSeat);

                if (finalSeatList.size() == numSeats)
                {
                    break;
                }

                //now get surrounding seats left, right, behind, and ahead.
                //left
                if (someSeat.col-1 >= 0)
                {
                    myQueue.add(grid.get(someSeat.row).getSeat(someSeat.col-1));
                }
                //right
                if (someSeat.col + 1 < cols)
                {
                    myQueue.add(grid.get(someSeat.row).getSeat(someSeat.col+1));
                }
                //behind
                if (someSeat.row+1 < rows)
                {
                    myQueue.add(grid.get(someSeat.row+1).getSeat(someSeat.col));
                }
                //ahead
                if (someSeat.row-1 >= 0)
                {
                    myQueue.add(grid.get(someSeat.row-1).getSeat(someSeat.col));
                }
            }

        }

        return finalSeatList;
        //at the end of all of this, we need to make all nodes visited back to false for further searches

    }



    @Override
    public int numSeatsAvailable() {
        return remainingSeats;
    }

    //for the number of seats, it creates seat objects by calling Row.GetSeat which pops seats from a stack of remaining seats. This is called
    //when locality is not an issue. Gets the best seat available which is the closest to the front of the stage
    @Override
    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        //iterat e through the grid, find rows that have enough num seats, reserve those seats for 1 minute, then release them.
        try
        {
            myLock.lock();
            ArrayList<Seat> seats = new ArrayList<>();

            for (Row someRow : grid)
            {
                if (someRow.remainingSeats >= numSeats)
                {
                    for (int i = 0; i < numSeats; i++)
                    {
                        Seat someSeat = someRow.getSeat();
                        remainingSeats--;
                        seats.add(someSeat);
                    }
                    SeatHold seatHold = new SeatHold(seats,customerEmail, someRow, this);
                    seatHoldMap.put(customerEmail, seatHold);
                    //find a way to set a timer here.
                    return seatHold;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            myLock.unlock();
        }


        return null;
    }
    //to be called after x seconds passes
    //simply adds all the seats back to the stack of seats that the row has.
    public void releaseSeats(SeatHold reservation){
        try
        {
            myLock.lock();
            reservation.someRow.seats.addAll(reservation.seatList);
            reservation.someRow.remainingSeats = reservation.someRow.remainingSeats + reservation.seatList.size();
            this.remainingSeats+=reservation.seatList.size();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            myLock.unlock();
        }


    }
    public SeatHold pickIndividualseats(int row, int col, String customerEmail)
    {
        Seat someSeat;
        Row someRow;
        SeatHold someSeatHold = null;
        try
        {
            myLock.lock();

            try
            {
                someRow = grid.get(row);
                someSeat = someRow.getSeat(col);
                if (someSeat == null)
                {
                    return null;
                }
            }
            catch (IndexOutOfBoundsException e)
            {
                return null;
            }
            ArrayList<Seat> seatList = new ArrayList<>();
            seatList.add(someSeat);
            someSeatHold = new SeatHold(seatList, customerEmail, someRow, this);
            remainingSeats--;
            someRow.remainingSeats--;
            //this is how we can check if its "active" or not. We just remove it.
            someRow.seats.remove(someSeat);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            myLock.unlock();
        }

        return someSeatHold;
    }
    //bit of a tricky one. Runs an loop that tries to find consecutive seats by checking the seat ahead of it and making sure
    //that it is one column number off. If works, then we set a loop that goes from that index i + the num of seats and adds those
    //consecutive seats.
    public SeatHold findAndHoldConsecutiveSeats(int numSeats, String customerEmail)
    {
        try
        {
            myLock.lock();
            ArrayList<Seat> seats = new ArrayList<>();

            for (Row someRow : grid)
            {
                //fixes an error because if you try to get one seat with only one seat remaining, the else block will try to get
                //the seat and check the seat ahead of it, which there is not one to check and it will not return anything.
                if (someRow.remainingSeats == 1 && numSeats == 1)
                {
                    seats.add(someRow.getSeat());
                }
                else
                {
                    if (someRow.remainingSeats >= numSeats)
                    {

                        Seat[] rowSeats = Arrays.stream(someRow.seats.toArray()).toArray(Seat[]::new);


                        Arrays.sort(rowSeats);
                        mainLoop:
                        for (int i = 0; i < rowSeats.length-numSeats; i++)
                        {
                            //if find consecutive
                            int j = i;
                            while (j < i + numSeats-1){
                                if (rowSeats[j].col == rowSeats[j+1].col-1){
                                    j++;

                                }
                                else
                                {
                                    continue mainLoop;
                                }
                            }
                            //if it makes it to here: meaning we did a loop for num seats and checked if the next index is one off from each other, then we can add all of them
                            j = i;
                            while (j < i + numSeats)
                            {
                                seats.add(rowSeats[j]);
                                someRow.seats.remove(rowSeats[j]);
                                j++;
                            }
                            SeatHold seatHold = new SeatHold(seats,customerEmail, someRow, this);
                            seatHoldMap.put(customerEmail, seatHold);
                            //find a way to set a timer here.
                            someRow.remainingSeats = someRow.remainingSeats - numSeats;
                            remainingSeats = remainingSeats - numSeats;
                            return seatHold;


                        }

                    }


                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            myLock.unlock();
        }

        return null;
    }

    public void displayVenue()
    {
        try
        {
            myLock.lock();
            int j = 0;

            while (j < cols/3)
            {
                System.out.print("-");
                j++;

            }
            System.out.print("[[ STAGE ]]");
            j = 0;
            while (j < cols/3)
            {
                System.out.print("-");
                j++;

            }
            System.out.println();
            j = 0;
            while (j < cols)
            {
                System.out.print("-");
                j++;

            }
            System.out.println();
            for (Row someRow : grid)
            {
                Seat[] rowSeats = Arrays.stream(someRow.seats.toArray()).toArray(Seat[]::new);
                ArrayList<Integer> remainingCols = new ArrayList<>();
                for (Seat seat : rowSeats)
                {
                    remainingCols.add(seat.col);
                }
                remainingCols.sort(new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1 - o2;
                    }
                });


                for (int i = 0; i < cols; i++)
                {
                    //THIS IS BOTTLENECK. COLS^2 for contains.
                    if (remainingCols.contains(i))
                    {
                        System.out.print("s");

                    }
                    else
                    {
                        System.out.print("x");

                    }

                }
                System.out.println();

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            myLock.unlock();
        }

    }


    //returns the confirmation number from Java.Util.UUID.
    @Override
    public String reserveSeats(String seatHoldId, String customerEmail) {
        //I could try to find a better way of creating a seat holdID rather than using UUID

        if (seatHoldMap.get(customerEmail) != null)
        {
            SeatHold someSeatHold = seatHoldMap.get(customerEmail);
            someSeatHold.didContinue = true;
            reservedSeats.put(someSeatHold.seatHoldID, someSeatHold.seatList);
            return someSeatHold.seatHoldID;
        }
        return "COULD NOT FIND RESERVED SEATS WITH EMAIL PROVIDED";


    }
}
