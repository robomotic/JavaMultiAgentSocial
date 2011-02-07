/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sim.app.socialsystem;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;
/**
 *
 * @author epokh
 */
public class QBrain {
    public final double learningRate=0.5;
    private Matrix Q;

    private Matrix lookupState;
    private int lookupActions[];
    private int previousAction;
    private int previousState;
    private int previousReward;
    public int lastReward;

    public QBrain(int nstates,int nactions)
    {

        //create a sparse matrix Q containing the best pair of states-actions
        Q = MatrixFactory.dense(nstates, nactions);
        lookupState=MatrixFactory.dense(3,3);
        for(int col=0;col<nactions;col++)
            for(int row=0;row<nstates;row++)
                Q.setAsDouble(0.0, row,col);

    }

    public void setInitialAction(int orientation)
    {
        this.previousAction=orientation;
    }
    public void setInitialState(int error_reflex,int error_distal)
    {
    this.previousState=getStateIndex(error_reflex, error_distal);
    }
    //in the agent model there are 8 possible directions to be chosen
    //we code them simply from 0 to 7
    public void initActions(int ndirections)
    {
        lookupActions=new int[ndirections];
        lookupActions[0]=Agent.N;
        lookupActions[1]=Agent.NE;
        lookupActions[2]=Agent.E;
        lookupActions[3]=Agent.SE;
        lookupActions[4]=Agent.S;
        lookupActions[5]=Agent.SW;
        lookupActions[6]=Agent.W;
        lookupActions[7]=Agent.NW;

    }

    //there are 3x3 possible states we code them from 0 to 8
    public void initStates()
    {
        int code=0;
        for(int col=0;col<2;col++)
        {    for (int row=0;row<2;row++)
             {
                  lookupState.setAsInt(code, row,col);
                  code++;
             }
        }
    }

    public int getStateIndex(int error_reflex,int error_distal)
    {

        int col=error_reflex++;
        int row=error_distal++;
        return lookupState.getAsInt(row,col);
    }

    //return the best action according to the current state
    public int chooseAction(int error_reflex,int error_distal)
    {
        int present_state=getStateIndex(error_reflex, error_distal);
        int bestaction=findMaxAction(present_state,1);
        this.previousState=present_state;
        this.previousAction=bestaction;
        return bestaction;
    }
    //lookup for the future actions to get the best reward
    //we can choose the depth of prediction!
    public int findMaxAction(int state,int npredictions)
    {

        int maxQ=-1000;
 
        for(int k=0;k<Q.COLUMN;k++)
        {
            maxQ=(int) Math.max(Q.getAsDouble(state,k), maxQ);
        }
        int k=0;
        while(k<Q.COLUMN)
        {
            if(Q.getAsDouble(state,k)==maxQ)
                break;
        }
        return k;

    }
    //update the Q matrix using the reward received from the new state,
    //according to the last state and action chosen
    public void updateQvalues(int error_reflex,int error_distal)
    {
        double previousQ=Q.getAsDouble(previousState, previousAction);
        int newstate=getStateIndex(error_reflex, error_distal);
        int bestaction=findMaxAction(newstate,1);
        double nextQ=previousQ+learningRate*(lastReward+bestaction-previousQ);
        Q.setAsDouble(nextQ,previousState, previousAction);
        
    }

}
