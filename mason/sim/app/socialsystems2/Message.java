/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sim.app.socialsystems2;

/**
 *
 * @author epokh
 */
public class Message {
    //the sender ID must be unique
    private int id_sender;
    //the receiver ID can be broadcast type
    private int id_receiver;

    //the value of the message is a double because represents the weight
    //of the synapse
    private double weight;

    //the type of the value is used as a reward for the other agent
    //to know if the action of trying the weight was succesful or not
    private int type;
    
    private double timestamp=0.0;

    public Message(int sender,double weight,int type)
    {
        this.id_sender=sender;
        //we put this one to -1 because we suppose the agent push
        //the message directly into the receiver inbox
        this.id_receiver=-1;
        this.weight=weight;
        this.type=type;
    }

    public int getType() {
        return type;
    }

    public double getValue() {
        return weight;
    }

    public int getSenderID()
    {
        return id_sender;
    }

    double getTime() {
        return timestamp;
    }

    void setTime(double last_time_try) {
        double timestamp=last_time_try;
    }

}
