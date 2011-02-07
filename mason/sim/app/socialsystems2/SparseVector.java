/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sim.app.socialsystems2;

/**
 *
 * @author epokh
 */
/*************************************************************************
 *  Compilation:  javac SparseVector.java
 *  Execution:    java SparseVector
 *  
 *  A sparse vector, implementing using a symbol table.
 *
 *  [Not clear we need the instance variable N except for error checking.]
 *
 *************************************************************************/

public class SparseVector {
    private final int wordsize;             //wordsize in bits
    private int N;                          //number of samples
    private int Bstar;                      //number of bins with enmpty values
    public ST<String, Double> st;  // the vector, represented by index-value pairs

    // initialize the all 0s vector of length N
    public SparseVector(int wordsize) {
        this.wordsize  = wordsize;
        this.st = new ST<String, Double>();
        this.N=0;
        this.Bstar=0;
    }

    // put st[i] = value
    public void put(String word, double value) {
        if (word.length()<= 0 || word.length() > wordsize) throw new RuntimeException("Illegal index");
        if (value == 0.0) st.remove(word);
        else              st.put(word, value);
    }
    
    // put st[i] = value
    public void add(String word, double value) {
        if (word.length()<= 0 || word.length() > wordsize) throw new RuntimeException("Illegal index");
        if(st.contains(word))
        {    
            double prev=st.get(word);
            st.put(word,prev +value);
        }
        else st.put(word,value);
        N=N+1;
    }

    public double getFrequency(String word)
    {
        if (word.length()<= 0 || word.length() > wordsize) throw new RuntimeException("Illegal index");
        if (st.contains(word)){
            return st.get(word)/nnz();
        }
        else{
            return 0.0;
        }     
    }
    // return st[i]
    public double get(String word) {
        if (word.length()<= 0 || word.length() > wordsize) throw new RuntimeException("Illegal index");
        if (st.contains(word)) return st.get(word);
        else{
            return 0.0;
        }
    }
       public double getFrequency(long code) {
        if (code< 0 || code > (Math.pow(2,wordsize)-1)) throw new RuntimeException("Illegal index");
        String word=toLZ(code, wordsize);
        if (st.contains(word)) 
        {
            return st.get(word)/nnz();
        }
        else                return 0.0;
    } 
        // return st[i]
    public double get(long code) {
        if (code< 0 || code > (Math.pow(2,wordsize)-1)) throw new RuntimeException("Illegal index");
        String word=toLZ(code, wordsize);
        if (st.contains(word)) return st.get(word);
        else                return 0.0;
    }
public static String toLZ( long discretized, int len )
   {
   // converts integer to left-zero padded string, len  chars long.
   String s = Long.toBinaryString(discretized);
   if ( s.length() > len ) return s.substring(0,len);
   else if ( s.length() < len ) // pad on left with zeros
      return "000000000000000000000000000000000000000000000000000000000000000000000000".substring(0, len - s.length ()) + s;
   else return s;
   } // end toLZ

    // return the number of nonzero entries
    public int nnz() {
        return st.size();
    }

    // return the size of the vector
    public int size() {
        return wordsize;
    }

    // return the dot product of this vector a with b
    public double dot(SparseVector b) {
        SparseVector a = this;
        if (a.wordsize != b.wordsize) throw new RuntimeException("Vector lengths disagree");
        double sum = 0.0;

        // iterate over the vector with the fewest nonzeros
        if (a.st.size() <= b.st.size()) {
            for (String i : a.st)
                if (b.st.contains(i)) sum += a.get(i) * b.get(i);
        }
        else  {
            for (String i : b.st)
                if (a.st.contains(i)) sum += a.get(i) * b.get(i);
        }
        return sum;
    }

    // return the 2-norm
    public double norm() {
        SparseVector a = this;
        return Math.sqrt(a.dot(a));
    }

    // return alpha * a
    public SparseVector scale(double alpha) {
        SparseVector a = this;
        SparseVector c = new SparseVector(wordsize);
        for (String i : a.st) c.put(i, alpha * a.get(i));
        return c;
    }

    // return a + b
    public SparseVector plus(SparseVector b) {
        SparseVector a = this;
        if (a.wordsize != b.wordsize) throw new RuntimeException("Vector lengths disagree");
        SparseVector c = new SparseVector(wordsize);
        for (String i : a.st) c.put(i, a.get(i));                // c = a
        for (String i : b.st) c.put(i, b.get(i) + c.get(i));     // c = c + b
        return c;
    }

    // return a string representation
    public String toString() {
        String s = "";
        for (String i : st) {
            s += "(" + i + ", " + st.get(i) + ") ";
        }
        return s;
    }


    // test client
    public static void main(String[] args) {
        SparseVector a = new SparseVector(4);
        SparseVector b = new SparseVector(4);
        a.add("1010", 0.50);
        a.add("1010", 0.75);
        a.add("1011", 0.11);
        a.add("1100", 0.00);
        
        b.put("1010", 0.60);
        b.put("0000", 0.90);
        System.out.println("a = " + a);
        System.out.println("b = " + b);
        System.out.println("a dot b = " + a.dot(b));
        System.out.println("a + b   = " + a.plus(b));
    }

    public double getN() {
        return (double)N;
    }

    public void setN(double N) {
        this.N = (int)N;
    }

    public double getBstar() {
        //the number of bins with empty frequencies=
        //total number of bins-number of non zero bins
        Bstar=nnz();
        return Bstar;
    }

    public void setBstar(int Bstar) {
        this.Bstar = Bstar;
    }

}

