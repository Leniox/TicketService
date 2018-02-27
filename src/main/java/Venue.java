
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
        //create a weighted venue depending on the use case. A regular venue creates a grid of Seat objects
        //while a weighted venue also does this but assigns a weight to them based off of a weighting system.
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
        //if we have an odd number of rows or columns, we get an off by one error
        //resulting in percentages over 100%. I think this still occurs in some cases but the
        //distribution of numbers is correct for the BFS implementation
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
                //calculate total cost. This is row cost + col cost where row cost is equal to a distrubtion on a number line
                //where instead of distributing numbers evenly across your field of numbers, you distribute over half of the number line
                //and reach the pinnacle of your distribution at the center of the line. Mirror the latter of the line.

                //create new seat object and assign it a weight
                //I also put the weight into a weightMap. This is important because when I want to start my BFS
                //I need to know where to start and expand from. Thus, I have a tree map (tree because I can order input a reverse
                //comparator to order based off of highest weighted seats first) that can query seats and can BFS from there.
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
    //This was used for testing to ensure the distribution looked correct. You can call this after you create a venue to mamke sure it looks right
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
    //Find weighted Seat: find seat with highest weight. BFS until and increment a counter once you find a valid seat.

    public SeatHold findWeightedSeats(int numSeats, String customerEmail)
    {
        ArrayList<Integer> potentialWeightsToRemove = new ArrayList<>();
        SeatHold seatHold = null;
        try
        {
            myLock.lock();

            //acquire lock and find the highest weight
            for (Integer weight : weightMap.keySet())
            {
                ArrayList<Seat> someList = weightMap.get(weight);
                ArrayList<Seat> availableSeats = new ArrayList<>();

                //more optimization could see me ordering the seats of available seats by proximity to row/2 and col /2 (center most)
                //if the size of the list is empty, add it to a list of weights to remove. I can't remove while iterating without concurrent modification error
                if (someList.size() == 0)
                {
                    potentialWeightsToRemove.add(weight);
                    continue;
                }
                //once we find a valid seat, add all seats
                for (Seat aSeat : someList)
                {
                    if (aSeat.available)
                    {
                        availableSeats.add(aSeat);
                    }
                }
                //This may be redundant.
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

    //simple ascii representation. make the stage a third of the size. It looks good for large numbers
    //but a bit wonky for smaller numbers.
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
    //breadth first search on seats. O(V + E)
    public ArrayList<Seat> BFS(Seat initialSeat, int numSeats)
    {
        Queue<Seat> myQueue = new LinkedList<>();
        ArrayList<Seat> finalSeatList = new ArrayList<>();
        myQueue.add(initialSeat);
        //

        while (!myQueue.isEmpty())
        {
            Seat someSeat = myQueue.poll();
            if ( someSeat.available )
            {
//                visited[someSeat.row][someSeat.col] = true;
                someSeat.available = false;
                finalSeatList.add(someSeat);

                //once we find the number of seats, we're good to go.
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
        //This is based on a bucket system. Go through buckets, see if that buckets available seats have enough seats requested. Then pop from the stack
        //of seats inside that bucket. This is the cloest to O(numSeats) time that I can get.
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
    //get seats based off of row and col. Run time of O(1)
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
                //catch index out of bounds if row is not good input. Same thing with col.
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
            //add seat to a single seat hold and decrement counters for the row bucket and the total venue.
            someSeatHold = new SeatHold(seatList, customerEmail, someRow, this);
            remainingSeats--;
            //not popping from stack so we have to remove manually.
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
    //Since we're  not sure if there are consecutive seats, we have average O(row) because we may have to search the entire row. However,
    //if the seats are staggered in a way where there are available seats but not available seats concsecutive, we could have worst case
    //O(grid) to search through every row and column to see if there are available seats AND they were consecutive.
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
                            //we have to remove seats here since we're not popping from stack.
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
            //seatHold is a map of the current transaction.
            //Reserved Seats is a map of all transactions.
            SeatHold someSeatHold = seatHoldMap.get(customerEmail);
            someSeatHold.didContinue = true;
            reservedSeats.put(someSeatHold.seatHoldID, someSeatHold.seatList);
            return someSeatHold.seatHoldID;
        }
        return "COULD NOT FIND RESERVED SEATS WITH EMAIL PROVIDED";


    }
}
