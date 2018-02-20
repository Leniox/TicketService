
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Driver {
    static boolean passedAdminRow = true;
    static boolean passedAdminCols = true;
    static boolean passedEmail = true;
    static AtomicBoolean passedNumSeats = new AtomicBoolean(true);
    static Scanner scan;
    static boolean didMoveForward = false;
    static Venue ticketVenue;
    static boolean didReleaseSeats = false;
    static Timer timer = new Timer();
    public static void main(String [] args)
    {


         scan = new Scanner(System.in);

        int venueRow = 0;
        int venueCol = 0;

        while (passedAdminRow)
        {
            System.out.println("ADMINISTRATOR SETUP: How many rows for the venue:\n ");
            try
            {
                venueRow = Integer.parseInt(scan.nextLine());
                passedAdminRow = false;

            }
            catch (Exception e)
            {
                System.out.println("Must enter an integer!");
            }

        }

        while (passedAdminCols)
        {
            System.out.println("ADMINISTRATOR SETUP: How many seats for each row within the venue:\n ");
            try
            {
                venueCol = Integer.parseInt(scan.nextLine());
                passedAdminCols = false;

            }
            catch (Exception e)
            {
                System.out.println("Must enter an integer!");
            }

        }

        ticketVenue = new Venue(venueRow, venueCol, true);
        ticketVenue.displayVenue();
        String customerEmail = "";
        System.out.println("ADMINISTRATOR SETUP COMPLETE. ENTERING USER MODE\n ");

        boolean passedvenueSelect = true;
        Integer venueSelect = 0;
        while (passedvenueSelect)
        {
            System.out.println("IS THIS A REGULAR VENUE (1) OR A CENTRALLY WEIGHTED VENUE (MOVIE THEATER) (2)? Press 1 or 2");
            try
            {
                venueSelect = Integer.parseInt(scan.nextLine());
                if (venueSelect != 1 && venueSelect != 2)
                {
                    System.out.println("Must enter 1 or 2!");
                    continue;

                }
                passedvenueSelect = false;

            }
            catch (Exception e)
            {
                System.out.println("Must enter an integer!");
            }

        }

        //big break here. If we select one, then we do only BFS intuitive search. Else, we give more options for regular venues without a weighted graph
        if (venueSelect == 2) {

            weightRestart:
            while (true) {
                while (passedEmail) {
                    System.out.println("Please enter your email: \n");
                    try {
                        //do error checking for email here
                        customerEmail = (scan.nextLine());
                        passedEmail = false;

                    } catch (Exception e) {
                        System.out.println("Must enter a valid email address!");
                    }

                }

                System.out.println("Hi! Welcome to the automated ticketing system.\n" +
                        "We base our system under the initial assumption that the seats you want are centrally focused. How many seats do you want? \n Press anything else to quit\n");
                Integer numSeats = getNumSeats();
                SeatHold someHold = ticketVenue.findWeightedSeats(numSeats, customerEmail);
                if (someHold == null) {
                    System.out.println("It looks like we ran out of seats! Try again another time");
                    System.exit(0);
                }


                System.out.println("Here are the seats we found for you. We will hold these seats for the next 10 seconds. After that time, someone else can grab them. \n");
                //list seatHold information here
                System.out.println(someHold);


                System.out.println("If you would like to move forward with these seats, enter C for 'continue. Enter anything else for quit: \n");
                String moveForward = scan.nextLine();
                if (moveForward.equalsIgnoreCase("c")) {
                    someHold.didContinue = true;
                    didMoveForward = true;
                    System.out.println("Thank you for booking with us");
                    String confirmation = ticketVenue.reserveSeats(someHold.seatHoldID, customerEmail);
                    System.out.println("Here is your confirmation number: " + confirmation);
                    ticketVenue.displayWeightedVenue();

                } else {
                    continue weightRestart;
                }


            }
        }
        else
        {
            System.out.println("Hi! Welcome to the automated ticketing system.\n" +
                    "We base our system under the initial assumption that the seats you reserve will be on the same row but " +
                    "not together. \nIf you would like to find seats located beside each other, please press 1. \nPress 2 for best seats without locality awareness.\n" +
                    "Press 3 to pick your own seats.\n Press anything else to quit\n");

            String seatsBesideAns = scan.nextLine();
            restart:
            while (seatsBesideAns.equals("1") || seatsBesideAns.equals("2") || seatsBesideAns.equalsIgnoreCase("3"))
            {
                while (passedEmail)
                {
                    System.out.println("Please enter your email: \n");
                    try
                    {
                        //do error checking for email here
                        customerEmail = (scan.nextLine());
                        passedEmail = false;

                    }
                    catch (Exception e)
                    {
                        System.out.println("Must enter a valid email address!");
                    }

                }
//            //must reset to false if they come back
//            //this is to cover up to make sure that you can't complete requests too fast. Not taking care of race conditions right now.
//            System.out.println("pulling up information. A few seconds. . . ");
//            try
//            {
//                Thread.sleep(5000);
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//




                if (Integer.parseInt(seatsBesideAns) == 1)
                {
                    Integer numSeats = getNumSeats();

                    SeatHold someSeatHold = ticketVenue.findAndHoldConsecutiveSeats(numSeats, customerEmail);
                    while (someSeatHold == null)
                    {
                        System.out.print("Could not find the requested number of seats. Please try again");
                        numSeats = getNumSeats();
                        someSeatHold = ticketVenue.findAndHoldSeats(numSeats, customerEmail);
                    }
                    System.out.println("Here are the seats we found for you. We will hold these seats for the next 10 seconds. After that time, someone else can grab them. \n");
                    //list seatHold information here
                    System.out.println(someSeatHold);


                    System.out.println("If you would like to move forward with these seats, enter C for 'continue.' If you want to pick your own seats, enter P. " +
                            "for 'pick.' Enter Q for quit: \n");
                    String moveForward = scan.nextLine();

                    if (moveForward.equalsIgnoreCase("C"))
                    {
                        //lock
                        someSeatHold.didContinue = true;
                        didMoveForward = true;
                        System.out.println("Thank you for booking with us");
                        String confirmation = ticketVenue.reserveSeats(someSeatHold.seatHoldID, customerEmail);
                        System.out.println("Here is your confirmation number: " + confirmation);
                        //unlock



                    }
                    else if (moveForward.equalsIgnoreCase("Q"))
                    {
                        System.exit(0);
                    }
                    //this is what gets called if they don't enter anything after 10 seconds.
                    else
                    {
                        System.out.println("Hi! Welcome to the automated ticketing system.\n" +
                                "We base our system under the initial assumption that the seats you reserve will be on the same row but " +
                                "not together. \nIf you would like to find seats located beside each other, please press 1. \nPress 2 for best seats without locality awareness.\n" +
                                "Press 3 to pick your own seats.\n Press anything else to quit\n");
                        seatsBesideAns = scan.nextLine();
                        continue restart;
                    }
                }
                else if (Integer.parseInt(seatsBesideAns) == 2)
                {
                    //get seats
                    Integer numSeats = getNumSeats();
                    SeatHold someSeatHold = ticketVenue.findAndHoldSeats(numSeats, customerEmail);
                    while (someSeatHold == null)
                    {
                        System.out.print("Could not find the requested number of seats. Please try again");
                        numSeats = getNumSeats();
                        someSeatHold = ticketVenue.findAndHoldSeats(numSeats, customerEmail);
                    }
                    SeatHold [] innerClassFix = new SeatHold[]{someSeatHold};
                    System.out.println("Here are the seats we found for you. We will hold these seats for the next 10 seconds. After that time, someone else can grab them. ");
                    //list seatHold information here
                    System.out.println(someSeatHold);


                    System.out.println("If you would like to move forward with these seats, enter C for 'continue.'  Enter Q for quit: \n");
                    String moveForward = scan.nextLine();

                    if (moveForward.equalsIgnoreCase("C"))
                    {
                        someSeatHold.didContinue = true;
                        didMoveForward = true;
                        System.out.println("Thank you for booking with us");
                        String confirmation = ticketVenue.reserveSeats(someSeatHold.seatHoldID, customerEmail);
                        System.out.println("Here is your confirmation number: " + confirmation);

                    }

                    else
                    {

                        System.out.println("Hi! Welcome to the automated ticketing system.\n" +
                                "We base our system under the initial assumption that the seats you reserve will be on the same row but " +
                                "not together. \nIf you would like to find seats located beside each other, please press 1. \nPress 2 for best seats without locality awareness.\n" +
                                "Press 3 to pick your own seats.\n Press anything else to quit\n");;
                        seatsBesideAns = scan.nextLine();
                        continue restart;
                    }



                }
                else if (Integer.parseInt(seatsBesideAns) == 3)
                {
                    int rowNum = 0;
                    int colnum = 0;
                    boolean gotValidRow = true;
                    while (gotValidRow)
                    {
                        System.out.println("Please enter a valid row number: \n");
                        try
                        {
                            //do error checking for email here
                            rowNum = Integer.parseInt(scan.nextLine());
                            gotValidRow = false;

                        }
                        catch (Exception e)
                        {
                            System.out.println("Must enter a valid row number!");
                        }

                    }

                    boolean gotValidCOl = true;
                    while (gotValidCOl)
                    {
                        System.out.println("Please enter a valid col number: \n");
                        try
                        {
                            //do error checking for email here
                            colnum = Integer.parseInt(scan.nextLine());
                            gotValidCOl = false;

                        }
                        catch (Exception e)
                        {
                            System.out.println("Must enter a valid col number!");
                        }
                    }


                    SeatHold getIndividualSeat = ticketVenue.pickIndividualseats(rowNum, colnum, customerEmail);

                    if ( getIndividualSeat == null)
                    {
                        System.out.println("NOT A VALID SEAT NUMBER. Try again");
                        System.out.println("Hi! Welcome to the automated ticketing system.\n" +
                                "We base our system under the initial assumption that the seats you reserve will be on the same row but " +
                                "not together. \nIf you would like to find seats located beside each other, please press 1. \nPress 2 for best seats without locality awareness.\n" +
                                "Press 3 to pick your own seats.\n Press anything else to quit\n");
                        seatsBesideAns = scan.nextLine();
                        continue restart;
                    }

                    //################## reusing old code. Should put in a method in the future

                    System.out.println("Here are the seats we found for you. We will hold these seats for the next 10 seconds. After that time, someone else can grab them. ");
                    //list seatHold information here
                    System.out.println(getIndividualSeat);



                    System.out.println("If you would like to move forward with these seats, enter C for 'continue.'  Enter Q for quit: \n");
                    String moveForward = scan.nextLine();


                    if (moveForward.equalsIgnoreCase("C"))
                    {
                        didMoveForward = true;
                        getIndividualSeat.didContinue = true;
                        //have to decrement since we're not calling a venueFunction that would automatically do it for us.
                        System.out.println("Thank you for booking with us");
                        String confirmation = ticketVenue.reserveSeats(getIndividualSeat.seatHoldID, customerEmail);
                        System.out.println("Here is your confirmation number: " + confirmation);

                    }


                }


                ticketVenue.displayVenue();
                System.out.println("Thank you for reserving seats. Would you like to reserve more? Y or N: \n");
                String moreReservations = scan.nextLine();
                if(moreReservations.equalsIgnoreCase("Y"))
                {
                    System.out.println("Hi! Welcome to the automated ticketing system.\n" +
                            "We base our system under the initial assumption that the seats you reserve will be on the same row but " +
                            "not together. \nIf you would like to find seats located beside each other, please press 1. \nPress 2 for best seats without locality awareness.\n" +
                            "Press 3 to pick your own seats.\n Press anything else to quit\n");
                    seatsBesideAns = scan.nextLine();
                    continue restart;


                }
                else
                {
                    System.out.println("Thank you! Quitting. . . ");
                    System.exit(0);
                }


            }
        }


        System.out.println("Thank you! Quitting. . . ");
        System.exit(0);



    }



    public static int  getNumSeats()
    {
        int numSeats = 0;
        while (passedNumSeats.get())
        {
            System.out.println("Please enter number of seats or Q to quit: \n");
            try
            {
                //do error checking for email here
                String ans = scan.nextLine();
                if (ans.equalsIgnoreCase("Q"))
                {
                    System.exit(0);
                }
                numSeats = Integer.parseInt(ans);
                passedNumSeats.set(false);

            }
            catch (Exception e)
            {
                System.out.println("Must enter a number or Q!");
            }


        }
        passedNumSeats.set(true);
        return numSeats;

    }


}
