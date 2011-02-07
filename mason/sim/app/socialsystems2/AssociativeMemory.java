/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sim.app.socialsystems2;

import org.jfree.util.HashNMap;

/**
 *
 * @author epokh
 */
public class AssociativeMemory {

    //the memory is an hashnmap because the same agent ID
    //can have multilpe reward associated
    private HashNMap memory;
    public AssociativeMemory()
    {
        memory=new HashNMap();
    }

    //after the agent has tried the message add it to its memory
    public void addEntry(Message amessage,int reward)
    {
        Entry temp=new Entry();
        //if the memory is not empty we need to find out where to put the message
        if(!memory.keySet().isEmpty())
        {
            //find the entry of the same type from the same agent and add the reward
            if(memory.containsKey(amessage.getSenderID()))
            {
                int numbers=memory.getValueCount(amessage.getSenderID());
                for(int k=0;k<numbers;k++)
                {
                    Entry current=(Entry)memory.get(amessage.getSenderID(), k);

                    //if is the same type of message from the same agent we only update
                    //the reward
                    if(temp.type==amessage.getType())
                    {
                        temp.reward=current.reward+reward;
                        temp.type=amessage.getType();
                        temp.value=amessage.getValue();
                        memory.remove(amessage.getSenderID(), current);
                        memory.add(amessage.getSenderID(), temp);
                    }
                }
            }
            else{
            temp.type=amessage.getType();
            temp.value=amessage.getValue();
            temp.reward=reward;
            memory.add(amessage.getSenderID(), temp);
            }

        }
        else
        {
            temp.type=amessage.getType();
            temp.value=amessage.getValue();
            memory.add(amessage.getSenderID(), temp);
        }


    }

    public int getRewardByType(int agent_id,int type)
    {
        return 1;
    }

    public int getAgentTrust(int agent_id,int type)
    {
        return 0;
    }

    public class Entry{
        //the topic of the message
        public int type;
        //the value associated with that message
        public double value;
        //the reward obtained when the agent tried that weight
        public int reward;
        public Entry()
        {
            //type=DecisionMaker.B_FOOD;
            value=0.0;
            //a reward of zero means the value has not been tried yet
            reward=0;
        }

    }
}
