/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
  This code is generated by JAC version 5.6 by
  Agent Oriented Software. http://www.agent-software.com.au

    DO NOT ALTER THIS CODE AND DO NOT REMOVE THIS COMMENT
 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/
import aos.jack.jak.plan.Plan;
import aos.jack.jak.plan.PlanFSM;
import aos.jack.jak.plan.ExMap;
import aos.jack.jak.agent.NameSpace;
import aos.jack.jak.event.Event;
import aos.jack.jak.task.Task;
import aos.jack.jak.core.Generator;
import aos.jack.jak.logic.Signature;
import aos.jack.jak.behaviors.bdi.BDIAchieveBehavior;
import java.lang.Object;
import aos.jack.jak.cursor.Cursor;
import aos.jack.jak.fsm.FSM;
import aos.jack.jak.core.Jak;

/**
 * The Move plan implements the action of the game. Namely, to pick a
 * possible move and attempt it. Note that the range of possible moves
 * induces the set of applicable plans, so that if the picked move
 * fails, then an alternative possibility will be tried next.
 */

public class Move extends aos.jack.jak.plan.Plan {
    aos.jack.jak.logic.ObjectVariable $hole;
    aos.jack.jak.logic.ObjectVariable $piece;
    private ISokrates sok;
    public NextMove ev;
    public NextMove nm;
    public Board board;
    private static aos.jack.jak.plan.ExMap[] __exMap_body;
    private static java.lang.String[] __tt__body = {
            "Move.plan",
            "body",
            "45",
            "46",
            "47",
            "48",
            "42"};
    private static aos.jack.jak.plan.ExMap[] __exMap_fail;
    private static java.lang.String[] __tt__fail = {
            "Move.plan",
            "fail",
            "54",
            "55",
            "51"};
    private static aos.jack.jak.plan.ExMap[] __exMap_move;
    private static java.lang.String[] __tt__move = {
            "Move.plan",
            "move",
            "61",
            "62",
            "64",
            "58"};
    private final static java.lang.String[] __planVariableNames = {
            "$hole",
            "$piece",
            "sok",
            "ev",
            "nm",
            "board"};
    private final static java.lang.String[] __planVariableTypes = {
            "logical SquareIndex",
            "logical SquareIndex",
            "ISokrates",
            "NextMove",
            "NextMove",
            "Board"};
    private final static java.lang.String[] __reasoningMethods = {
            "body",
            "fail",
            "move"};
    private final static java.lang.String[] __logSignatureVariableNames = {
            "$hole",
            "$piece"};
    private final static java.lang.String[] __logSignatureVariableTypes = {
            "logical SquareIndex",
            "logical SquareIndex"};
    private final static java.lang.String[] __fsmVariableNames_move = {
            "$from",
            "$to",
            "back",
            "from",
            "to"};
    private final static java.lang.String[] __fsmTypes_move = {
            "logical SquareIndex",
            "logical SquareIndex",
            "boolean",
            "SquareIndex",
            "SquareIndex"};
    private final static java.lang.String[] __fsmLocalNames_move = {
            "$from",
            "$to",
            "back",
            "__local__2_3",
            "__local__2_4"};
    void indent(java.lang.String m)
    {
        for (int x = 0; x < ev.depth; x++ )
            java.lang.System.err.print(" ");
        java.lang.System.err.println(m);
    }
    
    static boolean relevant(NextMove ev)
    {
        return true;
    }
    
    public java.lang.String getDocumentation()
    {
        return "/**\n * The Move plan implements the action of the game. Namely, to pick a\n * possible move and attempt it. Note that the range of possible moves\n * induces the set of applicable plans, so that if the picked move\n * fails, then an alternative possibility will be tried next.\n */\n";
    }
    
    public Move()
    {
    }
    
    private Move(aos.jack.jak.task.Task __t, Move __env)
    {
        __agent = __env.__agent;
        __ns = __env.__ns;
        __planTask = __t;
        __logic = __t.logic;
        sok = (ISokrates) __agent;
        ev = __env.ev;
        nm = __env.nm;
        board = __env.board;
        $hole = (aos.jack.jak.logic.ObjectVariable) __logic.new_variable(SquareIndex.class);
        $piece = (aos.jack.jak.logic.ObjectVariable) __logic.new_variable(SquareIndex.class);
    }
    
    public boolean init_sentinel(aos.jack.jak.agent.NameSpace __a)
    {
        ev = (NextMove) __a.findEvent("NextMove");
        if (ev == null) {
            warning("NextMove ev: is not found in the capability/agent this plan comes from");
            return false;
        }
        nm = (NextMove) __a.findEvent("NextMove");
        if (nm == null) {
            warning("NextMove nm: is not found in the capability/agent this plan comes from");
            return false;
        }
        board = (Board) lookupNamedObject("board","Board",0);
        if (board == null) {
            warning("Board board: is not found in the capability/agent this plan comes from");
            return false;
        }
        return true;
    }
    
    public aos.jack.jak.plan.Plan freeze()
    {
        return this;
    }
    
    public aos.jack.jak.plan.Plan unfreeze()
    {
        return this;
    }
    
    public java.lang.Object genObject(int __index)
    {
        switch (__index) {
        }
        aos.jack.jak.core.Jak.error("illegal Object Construction");
        return null;
    }
    
    public aos.jack.jak.cursor.Cursor genCursor(int __index)
        throws java.lang.Exception
    {
        switch (__index) {
            case 0: 
            {
                return (board.possibleMove($piece,$hole));
            }
        }
        aos.jack.jak.core.Jak.error("illegal Cursor Construction");
        return null;
    }
    
    public aos.jack.jak.fsm.FSM genFSM(int __index)
        throws java.lang.Exception
    {
        switch (__index) {
        }
        aos.jack.jak.core.Jak.error("illegal FSM Construction");
        return null;
    }
    
    public boolean testCondition(int __index)
        throws java.lang.Exception
    {
        switch (__index) {
        }
        aos.jack.jak.core.Jak.error("illegal test Construction");
        return false;
    }
    
    public aos.jack.jak.plan.PlanFSM body()
    {
        return new Move.__bodyFSM();
    }
    
    public aos.jack.jak.plan.PlanFSM fail()
    {
        return new Move.__failFSM();
    }
    
    public aos.jack.jak.plan.PlanFSM move(aos.jack.jak.logic.ObjectVariable $from, aos.jack.jak.logic.ObjectVariable $to, boolean back)
    {
        return new Move.__moveFSM($from,$to,back);
    }
    
    private Move(NextMove __ev, aos.jack.jak.task.Task __t, Move __env)
    {
        this(__t,__env);
        this.ev = __ev;
    }
    
    protected aos.jack.jak.logic.Signature eventSignature(int __log)
    {
        return ev.getSignature(__log);
    }
    
    public java.lang.String handledEvent()
    {
        return "NextMove";
    }
    
    public boolean __relevant(aos.jack.jak.event.Event __e)
    {
        return __ns.isEnabled() && relevant(((NextMove) __e));
    }
    
    public aos.jack.jak.plan.Plan createPlan(aos.jack.jak.event.Event __e, aos.jack.jak.task.Task __t)
    {
        if (!(__e instanceof NextMove)) 
            return null;
        NextMove __e1 = (NextMove) __e;
        return new Move(__e1,__t,this);
    }
    
    protected aos.jack.jak.logic.Signature initSignature(int __log)
    {
        aos.jack.jak.logic.Signature __s = super.initSignature(__log + 2);
        __s.addLogical($hole);
        __s.addLogical($piece);
        return __s;
    }
    
    public void setFromSignature(aos.jack.jak.logic.Signature __s)
    {
        super.setFromSignature(__s);
        $hole = (aos.jack.jak.logic.ObjectVariable) __s.getLogical();
        $piece = (aos.jack.jak.logic.ObjectVariable) __s.getLogical();
    }
    
    public java.lang.String[] variableNames()
    {
        return __planVariableNames;
    }
    
    public java.lang.String[] variableTypes()
    {
        return __planVariableTypes;
    }
    
    public java.lang.Object getVariable(int n)
    {
        switch (n) {
            case 0: 
            {
                return aos.util.ToObject.box($hole);
            }
            case 1: 
            {
                return aos.util.ToObject.box($piece);
            }
            case 2: 
            {
                return aos.util.ToObject.box(sok);
            }
            case 3: 
            {
                return aos.util.ToObject.box(ev);
            }
            case 4: 
            {
                return aos.util.ToObject.box(nm);
            }
            case 5: 
            {
                return aos.util.ToObject.box(board);
            }
            default: 
            {
                throw new java.lang.IndexOutOfBoundsException("Plan " + this + " does not have variable number " + n);
            }
        }
    }
    
    public java.lang.String[] reasoningMethods()
    {
        return mergeReasoningMethods(__reasoningMethods,super.reasoningMethods());
    }
    
    public java.lang.String[] logSignatureVariableNames()
    {
        return __logSignatureVariableNames;
    }
    
    public java.lang.String[] logSignatureVariableTypes()
    {
        return __logSignatureVariableTypes;
    }
    
    public aos.jack.jak.cursor.Cursor context()
    {
        try {
            return (genCursor(0));
        }
        catch (java.lang.Exception e) {
            e.printStackTrace();
            return aos.jack.jak.cursor.Cursor.falseCursor;
        }
    }
    
    class __bodyFSM extends aos.jack.jak.plan.PlanFSM implements aos.jack.jak.core.Generator {
        private int __breakLevel = 0;
        public int run(int __status)
            throws java.lang.Throwable
        {
            do {
                try {
                    if (__tothrow != null) 
                        throw __tothrow;
                    if ((aos.jack.jak.core.Jak.debugging & aos.jack.jak.core.Jak.LOG_PLANS) != 0) 
                        aos.util.logging.LogMsg.log(this,aos.jack.jak.core.Jak.LOG_PLANS,__task + "-Move.body:" + java.lang.Integer.toString(__state));
                    if (__task.nsteps > 0) {
                        __task.nsteps-- ;
                        if (__task.nsteps == 0) 
                            agent.changeFocus();
                    }
                    if (__state < 10) {
                        __status = super.stdrun(Move.this,__status);
                        if (__status != CONTINUE || agent.changing_focus) 
                            return __status;
                        continue;
                    }
                    __curstate = __state;
                    switch (__state) {
                        default: 
                        {
                            aos.jack.jak.core.Jak.error("Move.body: Illegal state");
                            return FAILED_STATE;
                        }
                        //* (45)         indent( "Trying " + $piece + sok.getTriesCnt());
                        case 10: 
                        {
                            __breakLevel = 0;
                            __state = 11;
                            indent("Trying " + $piece + sok.getTriesCnt());
                            break;
                        }
                        //* (46)         move( $piece, $hole, false );
                        case 11: 
                        {
                            __task.push(move($piece,$hole,false));
                            __state = -__state;
                            __subtask_pass = 12;
                            __subtask_fail = 4;
                            return SUBTASK;
                        }
                        //* (47)         @achieve( board.solution(), nm.next( ev.depth ) );
                        case 12: 
                        {
                            __task.push(aos.jack.jak.behaviors.bdi.BDIAchieveBehavior.process(__task,this,0,0,true));
                            __state = -__state;
                            __subtask_pass = 13;
                            __subtask_fail = 4;
                            return SUBTASK;
                        }
                        //* (48)         indent( "SUCCESS " + $piece );
                        case 13: 
                        {
                            __state = 14;
                            indent("SUCCESS " + $piece);
                            break;
                        }
                        //* (42)     #reasoning method
                        case 14: 
                        {
                            if (__pending == null) 
                                __state = PASSED_STATE;
                            __tothrow = __pending;
                            break;
                        }
                    }
                }
                catch (java.lang.Throwable e) {
                    handleException(e,__exMap_body);
                }
            }
             while (!agent.changing_focus);
            return CONTINUE;
        }
        
        public java.lang.String methodName()
        {
            return "body";
        }
        
        __bodyFSM()
        {
        }
        
        public java.lang.String stateInfo()
        {
            int n = __curstate;
            java.lang.String file = __tt__body[0];
            java.lang.String method = __tt__body[1];
            if (n < 0) 
                n = -n;
            n -= (10 - 2);
            java.lang.String line = (n < 2)?"??":__tt__body[n];
            return file + ":" + line + " " + method + " [" + __curstate + "]";
        }
        
        public java.lang.Object genObject(int __index)
        {
            switch (__index) {
            }
            aos.jack.jak.core.Jak.error("illegal Object Construction");
            return null;
        }
        
        public aos.jack.jak.cursor.Cursor genCursor(int __index)
            throws java.lang.Exception
        {
            switch (__index) {
            }
            aos.jack.jak.core.Jak.error("illegal Cursor Construction");
            return null;
        }
        
        public aos.jack.jak.fsm.FSM genFSM(int __index)
            throws java.lang.Exception
        {
            switch (__index) {
                case 0: 
                {
                    return (nm.next(ev.depth));
                }
            }
            aos.jack.jak.core.Jak.error("illegal FSM Construction");
            return null;
        }
        
        public boolean testCondition(int __index)
            throws java.lang.Exception
        {
            switch (__index) {
                case 0: 
                {
                    return (board.solution());
                }
            }
            aos.jack.jak.core.Jak.error("illegal test Construction");
            return false;
        }
        
        public aos.jack.jak.plan.Plan getPlan()
        {
            return Move.this;
        }
        
        protected aos.jack.jak.fsm.FSM fail()
        {
            return getPlan().fail();
        }
        
        protected aos.jack.jak.fsm.FSM pass()
        {
            return getPlan().pass();
        }
        
        public void enter()
        {
            __trace = agent.trace("Move.body");
        }
        
    }
    class __failFSM extends aos.jack.jak.plan.PlanFSM implements aos.jack.jak.core.Generator {
        private int __breakLevel = 0;
        public int run(int __status)
            throws java.lang.Throwable
        {
            do {
                try {
                    if (__tothrow != null) 
                        throw __tothrow;
                    if ((aos.jack.jak.core.Jak.debugging & aos.jack.jak.core.Jak.LOG_PLANS) != 0) 
                        aos.util.logging.LogMsg.log(this,aos.jack.jak.core.Jak.LOG_PLANS,__task + "-Move.fail:" + java.lang.Integer.toString(__state));
                    if (__task.nsteps > 0) {
                        __task.nsteps-- ;
                        if (__task.nsteps == 0) 
                            agent.changeFocus();
                    }
                    if (__state < 10) {
                        __status = super.stdrun(Move.this,__status);
                        if (__status != CONTINUE || agent.changing_focus) 
                            return __status;
                        continue;
                    }
                    __curstate = __state;
                    switch (__state) {
                        default: 
                        {
                            aos.jack.jak.core.Jak.error("Move.fail: Illegal state");
                            return FAILED_STATE;
                        }
                        //* (54)         indent( "Failed " + $piece );
                        case 10: 
                        {
                            __breakLevel = 0;
                            __state = 11;
                            indent("Failed " + $piece);
                            break;
                        }
                        //* (55)         move( $hole, $piece, true );
                        case 11: 
                        {
                            __task.push(move($hole,$piece,true));
                            __state = -__state;
                            __subtask_pass = 12;
                            __subtask_fail = 4;
                            return SUBTASK;
                        }
                        //* (51)     #reasoning method
                        case 12: 
                        {
                            if (__pending == null) 
                                __state = PASSED_STATE;
                            __tothrow = __pending;
                            break;
                        }
                    }
                }
                catch (java.lang.Throwable e) {
                    handleException(e,__exMap_fail);
                }
            }
             while (!agent.changing_focus);
            return CONTINUE;
        }
        
        public java.lang.String methodName()
        {
            return "fail";
        }
        
        __failFSM()
        {
        }
        
        public java.lang.String stateInfo()
        {
            int n = __curstate;
            java.lang.String file = __tt__fail[0];
            java.lang.String method = __tt__fail[1];
            if (n < 0) 
                n = -n;
            n -= (10 - 2);
            java.lang.String line = (n < 2)?"??":__tt__fail[n];
            return file + ":" + line + " " + method + " [" + __curstate + "]";
        }
        
        public java.lang.Object genObject(int __index)
        {
            switch (__index) {
            }
            aos.jack.jak.core.Jak.error("illegal Object Construction");
            return null;
        }
        
        public aos.jack.jak.cursor.Cursor genCursor(int __index)
            throws java.lang.Exception
        {
            switch (__index) {
            }
            aos.jack.jak.core.Jak.error("illegal Cursor Construction");
            return null;
        }
        
        public aos.jack.jak.fsm.FSM genFSM(int __index)
            throws java.lang.Exception
        {
            switch (__index) {
            }
            aos.jack.jak.core.Jak.error("illegal FSM Construction");
            return null;
        }
        
        public boolean testCondition(int __index)
            throws java.lang.Exception
        {
            switch (__index) {
            }
            aos.jack.jak.core.Jak.error("illegal test Construction");
            return false;
        }
        
        public aos.jack.jak.plan.Plan getPlan()
        {
            return Move.this;
        }
        
        public void enter()
        {
            __trace = agent.trace("Move.fail");
        }
        
    }
    class __moveFSM extends aos.jack.jak.plan.PlanFSM implements aos.jack.jak.core.Generator {
        aos.jack.jak.logic.ObjectVariable $from;
        aos.jack.jak.logic.ObjectVariable $to;
        boolean back;
        SquareIndex __local__2_3;
        SquareIndex __local__2_4;
        private int __breakLevel = 0;
        public int run(int __status)
            throws java.lang.Throwable
        {
            do {
                try {
                    if (__tothrow != null) 
                        throw __tothrow;
                    if ((aos.jack.jak.core.Jak.debugging & aos.jack.jak.core.Jak.LOG_PLANS) != 0) 
                        aos.util.logging.LogMsg.log(this,aos.jack.jak.core.Jak.LOG_PLANS,__task + "-Move.move:" + java.lang.Integer.toString(__state));
                    if (__task.nsteps > 0) {
                        __task.nsteps-- ;
                        if (__task.nsteps == 0) 
                            agent.changeFocus();
                    }
                    if (__state < 10) {
                        __status = super.stdrun(Move.this,__status);
                        if (__status != CONTINUE || agent.changing_focus) 
                            return __status;
                        continue;
                    }
                    __curstate = __state;
                    switch (__state) {
                        default: 
                        {
                            aos.jack.jak.core.Jak.error("Move.move: Illegal state");
                            return FAILED_STATE;
                        }
                        //* (61)         SquareIndex from = (SquareIndex)$from.getValue();
                        case 10: 
                        {
                            __breakLevel = 0;
                            __local__2_3 = (SquareIndex) $from.getValue();
                            __state = 11;
                            break;
                        }
                        //* (62)         SquareIndex to = (SquareIndex)$to.getValue();
                        case 11: 
                        {
                            __local__2_4 = (SquareIndex) $to.getValue();
                            __state = 12;
                            break;
                        }
                        //* (64)         board.move( $from, $to , back);
                        case 12: 
                        {
                            if (board.move($from,$to,back)) 
                                __state = 13;
                             else 
                                throw planfailed;
                            break;
                        }
                        //* (58)     #reasoning method
                        case 13: 
                        {
                            if (__pending == null) 
                                __state = PASSED_STATE;
                            __tothrow = __pending;
                            break;
                        }
                    }
                }
                catch (java.lang.Throwable e) {
                    handleException(e,__exMap_move);
                }
            }
             while (!agent.changing_focus);
            return CONTINUE;
        }
        
        public java.lang.String methodName()
        {
            return "move";
        }
        
        __moveFSM(aos.jack.jak.logic.ObjectVariable $from, aos.jack.jak.logic.ObjectVariable $to, boolean back)
        {
            this.$from = $from;
            this.$to = $to;
            this.back = back;
        }
        
        public java.lang.String stateInfo()
        {
            int n = __curstate;
            java.lang.String file = __tt__move[0];
            java.lang.String method = __tt__move[1];
            if (n < 0) 
                n = -n;
            n -= (10 - 2);
            java.lang.String line = (n < 2)?"??":__tt__move[n];
            return file + ":" + line + " " + method + " [" + __curstate + "]";
        }
        
        public java.lang.Object genObject(int __index)
        {
            switch (__index) {
            }
            aos.jack.jak.core.Jak.error("illegal Object Construction");
            return null;
        }
        
        public aos.jack.jak.cursor.Cursor genCursor(int __index)
            throws java.lang.Exception
        {
            switch (__index) {
            }
            aos.jack.jak.core.Jak.error("illegal Cursor Construction");
            return null;
        }
        
        public aos.jack.jak.fsm.FSM genFSM(int __index)
            throws java.lang.Exception
        {
            switch (__index) {
            }
            aos.jack.jak.core.Jak.error("illegal FSM Construction");
            return null;
        }
        
        public boolean testCondition(int __index)
            throws java.lang.Exception
        {
            switch (__index) {
            }
            aos.jack.jak.core.Jak.error("illegal test Construction");
            return false;
        }
        
        public aos.jack.jak.plan.Plan getPlan()
        {
            return Move.this;
        }
        
        public void enter()
        {
            __trace = agent.trace("Move.move");
        }
        
        public java.lang.Object getVariable(int n)
        {
            switch (n) {
                case 0: 
                {
                    return aos.util.ToObject.box($from);
                }
                case 1: 
                {
                    return aos.util.ToObject.box($to);
                }
                case 2: 
                {
                    return aos.util.ToObject.box(back);
                }
                case 3: 
                {
                    return aos.util.ToObject.box(__local__2_3);
                }
                case 4: 
                {
                    return aos.util.ToObject.box(__local__2_4);
                }
                default: 
                {
                    throw new java.lang.IndexOutOfBoundsException("Reasoning Method " + methodName() + " does not have variable number " + n);
                }
            }
        }
        
        public java.lang.String[] variableNames()
        {
            return __fsmVariableNames_move;
        }
        
        public java.lang.String[] variableTypes()
        {
            return __fsmTypes_move;
        }
        
        public java.lang.String[] variableLocalNames()
        {
            return __fsmLocalNames_move;
        }
        
    }
}
