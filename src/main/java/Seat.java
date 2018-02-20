

public class Seat implements Comparable<Seat>{
    public int row;
    public int col;
    public int weight;
    public boolean available = true;


    public Seat(int row, int col)
    {
        this.row = row;
        this.col = col;
    }
    @Override
    public String toString()
    {
        return "(" + row + "," + col + ")";
    }




    @Override
    public int compareTo(Seat o) {
        return this.col - o.col;
    }
}
