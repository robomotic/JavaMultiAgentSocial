/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sim.app.socialsystems2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author epokh
 * This class is responsible to track the trust between every agent
 */
public class MemoryTrust {
 private HashMap memory;
 private int idmaxtrust;
 private int maxtrustvalue;
 public int positive_trust=0;
 public int negative_trust=0;
 
 public MemoryTrust()
 {
     this.memory=new HashMap();
 }
 public MemoryTrust(int npeers)
 {
     this.memory=new HashMap(npeers);
 }
 
  public int getMinimumTrust()
 {
      if(memory.size()==0)
          return 0;
    Iterator iterator = memory.keySet().iterator();
    int local_min=10000000;
    while( iterator. hasNext() ){
        Integer trust_value=(Integer)iterator.next();
        if(trust_value<=local_min)
            local_min=trust_value;
    }
    return local_min;
 }
  
 public int getMaximumTrust()
 {
     if(memory.size()==0) return 0;
    Iterator iterator = memory.keySet().iterator();
    int local_max=-1;
    while( iterator. hasNext() ){
        Integer trust_value=(Integer)iterator.next();
        if(trust_value>=local_max)
            local_max=trust_value;
    }
    return local_max;
 }
 public boolean decideTrust(int agentid)
 {
     //get the min-max range and use a probabilistic approach
     int min_trust=getMinimumTrust();
     int max_trust=getMaximumTrust();
     //nothing to decide we give trust
     if(min_trust==max_trust)
         return true;
     else{
     Random decide=new Random();
     //[TO DO]: check why min is > max! WEIRDDDDDDD!
     int breakpoint=min_trust+decide.nextInt(max_trust-min_trust);
     if(breakpoint<getTrust(agentid))
         return true;
     else return false;
     }
 }
 
 public int getTrust(int agentid)
 {
     if(memory.containsKey(agentid))
     {
         Integer old_points=(Integer)memory.get(agentid);
         return old_points;
     }     
     else return 0;
 }
 public boolean addTrust(int agentid, int points)
 {
     positive_trust++;
     if(memory.containsKey(agentid))
     {
         Integer old_points=(Integer)memory.get(agentid);
         memory.put(agentid, old_points+points);
         return true;
     }
     else
     {
         memory.put(agentid, new Integer(points));
         return false;
     }
 }
 
 public boolean removeTrust(int agentid,int points)
 {
     negative_trust++;
     if(memory.containsKey(agentid))
     {
         Integer old_points=(Integer)memory.get(agentid);
         if(points>=old_points)
             memory.remove(agentid);
         else
            memory.put(agentid, old_points-points);
         return true;
     }
     else return false;   
 }
}
