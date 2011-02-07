/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sim.app.socialsystems2;

/**
 *
 * @author epokh
 */
import java.util.TreeMap;
import java.util.Iterator;

public class ST<Key extends Comparable<Key>, Val> implements Iterable<Key> {
    private TreeMap<Key, Val> st;

    public ST() {
        st = new TreeMap<Key, Val>();
    }

    public void put(Key key, Val val) {
        if (val == null) st.remove(key);
        else             st.put(key, val);
    }
    public Val get(Key key)             { return st.get(key);            }
    public Val remove(Key key)          { return st.remove(key);         }
    public boolean contains(Key key)    { return st.containsKey(key);    }
    public int size()                   { return st.size();              }
    public Iterator<Key> iterator()     { return st.keySet().iterator(); }


   /***********************************************************************
    * Test routine.
    **********************************************************************/
    public static void main(String[] args) {
        ST<String, String> st = new ST<String, String>();

        // insert some key-value
        st.put("www.cs.princeton.edu", "128.112.136.11");
        st.put("www.princeton.edu",    "128.112.128.15");
        st.put("www.yale.edu",         "130.132.143.21");
        st.put("www.amazon.com",       "208.216.181.15");
        st.put("www.simpsons.com",     "209.052.165.60");

        // search for IP addresses given URL
        System.out.println(st.get("www.cs.princeton.edu"));
        System.out.println(st.get("www.amazon.com"));
        System.out.println(st.get("www.amazon.edu"));
        System.out.println();

        // print out all key-value pairs
        for (String s : st)
            System.out.println(s + " " + st.get(s));
    }

}
