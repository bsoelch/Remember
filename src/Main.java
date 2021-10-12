import java.io.*;
import java.util.*;
import java.util.regex.Pattern;


public class Main {
    static private final Scanner inScanner =new Scanner(System.in);

    /**maximum number of digits that will be read from an integer*/
    private static final int MAX_INT_LEN = 8;

    /**maximal number of distinct values that program can remember*/
    private static final int MEM_SIZE = 8;
    //8 since it is the smallest number for which if found a working multiplication program

    private static final Pattern VAR_REGEX = Pattern.compile("[a-zA-Z][a-zA-Z_0-9]*");
    private static final Pattern INT_PATTERN = Pattern.compile("0|(-?[1-9][0-9]*)");

    private static final LinkedList<MemEntry> memory=new LinkedList<>();

    static private class MemException extends RuntimeException{
        MemException(String msg){
            super(msg);
        }
    }
    static private class MemEntry{
        final String name;
        final int value;

        private MemEntry(String name, int value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MemEntry)) return false;
            MemEntry memEntry = (MemEntry) o;
            return Objects.equals(name, memEntry.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value);
        }
    }

    static void forget(String name) {
        memory.remove(new MemEntry(name,0));
    }
    static int remember(String name){
        if(!VAR_REGEX.matcher(name).matches())
            throw new MemException(name+" ?");
        int index=memory.indexOf(new MemEntry(name,0));
        if(index!=-1){
            MemEntry e=memory.remove(index);
            int i0=0;
            while(i0<memory.size()&&memory.get(i0).name.startsWith("&")){
                i0++;
            }
            //move remembered entries directly under stack#
            if(i0==memory.size()){
                memory.addLast(e);
            }else{
                memory.add(i0,e);
            }
            return e.value;
        }else{
            throw new MemException(name+" ?");
        }
    }
    static void remember(String name,int value){
        MemEntry e=new MemEntry(name,value);
        if(!name.startsWith("&")) {
            memory.remove(e);//remove old value for name, (if not on stack)
        }
        memory.addFirst(e);
        if(memory.size()>MEM_SIZE){
            if(memory.removeLast().name.startsWith("&"))
                throw new MemException("Stack Overflow");
        }
    }

    public static void main(String[] args) {
        String name;
        if(args.length==0){
            name=getFileName();
        }else {
            name = args[0];
        }
        File target= fileFromName(name);
        while(!target.exists()){
            System.out.println("File \""+name+"\" does not exist");
            name=getFileName();
            target= fileFromName(name);
        }
        try(BufferedReader read=new BufferedReader(new FileReader(target))){
            ArrayList<String> lines=new ArrayList<>();
            String tmp;
            while((tmp=read.readLine())!=null){
                lines.add(tmp);
            }
            int l=0;
            while(l<lines.size()){
                l=runLine(lines.get(l),l);
            }
        }catch (IOException io){
            System.out.println(io.toString());
        }
    }
    private static String getFileName() {
        System.out.println("Input a Filename:");
        return inScanner.nextLine();
    }
    private static File fileFromName(String name) {
        if(name.startsWith("."))
            name=System.getProperty("user.dir")+File.separator+name.substring(1);
        return new File(name);
    }

    private static int runLine(String line,int lineNumber) {
        String[] parts=line.split(" ");
        try {
            if (parts.length > 0) {
                int rem=0,i=1;
                switch (parts[0].toUpperCase(Locale.ROOT)) {
                    case "REMEMBER":
                    case "FORGET":
                    case "JGT":
                    case "JGE":
                    case "JNE":
                    case "JUMP":
                        remember("&"+parts[0].toUpperCase(Locale.ROOT),0);//push Remember on stack
                        if (parts.length > 1 && VAR_REGEX.matcher(parts[1]).matches()) {
                            if(parts[0].equalsIgnoreCase("REMEMBER")){
                                remember("&&"+parts[1],0);
                                rem=1;
                                i=2;
                                break;//skip free stack part
                            }else if(parts[0].equalsIgnoreCase("FORGET")){
                                forget(parts[1]);//forgetValue
                                rem=0;
                                forget("&FORGET");//free stack
                            }else if(parts[0].equalsIgnoreCase("JUMP")){
                                forget("&JUMP");//free stack
                                return remember(parts[1]);//jump
                            }else{
                                remember("&VALUE",remember(parts[1]));
                                rem=1;
                                i=2;
                            }
                        } else {
                            System.out.println(line + " ?");
                            rem=0;
                            forget("&"+parts[0].toUpperCase(Locale.ROOT));//free stack
                        }
                        break;
                    case "PRINT":
                        remember("&"+parts[0].toUpperCase(Locale.ROOT),0);//push Print on stack
                        rem=1;
                        break;
                }
                while (i<parts.length&&rem>0){
                    if(INT_PATTERN.matcher(parts[i]).matches()){
                        int val= readInt(parts[i]);
                        rem--;
                        val=reduceValue(line, lineNumber, val);
                        if(val>=0){
                            return val;
                        }
                    }else{
                        switch (parts[i].toUpperCase(Locale.ROOT)){
                            case "NOT":
                                remember("&NOT",0);//push NOT on stack
                                break;
                            case "NEG":
                            case "NEGATE":
                                remember("&NEG",0);//push NEG on stack
                                break;
                            case "AND":
                                remember("&AND",0);//push ADD on stack
                                rem++;
                                break;
                            case "ADD":
                                remember("&ADD",0);//push AND on stack
                                rem++;
                                break;
                            case "LINE": {
                                    rem--;
                                    int val = reduceValue(line, lineNumber, lineNumber);
                                    if (val >= 0) {
                                        return val;
                                    }
                                    rem--;
                                }break;
                            case "READ":
                                System.out.print("READ:");
                                String input=inScanner.nextLine().trim();
                                if(INT_PATTERN.matcher(input).matches()){
                                    int val= readInt(input);
                                    rem--;
                                    val=reduceValue(line, lineNumber, val);
                                    if(val>=0){
                                        return val;
                                    }
                                }
                                break;
                            default:
                                int val= remember(parts[i]);
                                rem--;
                                val=reduceValue(line, lineNumber, val);
                                if(val>=0){
                                    return val;
                                }
                                break;

                        }
                    }
                    i++;
                }
                if(rem>0){
                    System.out.println(line + " ?");
                    clearStack();
                    return lineNumber + 1;
                }else {
                    MemEntry first = memory.peekFirst();
                    while (first != null && first.name.startsWith("&")) {
                        memory.removeFirst();
                        if (first.name.startsWith("&&")) {//remember without value
                            remember(first.name.substring(2));
                            clearStack();
                            return lineNumber + 1;
                        } else if (first.name.equals("&VALUE")) {
                            int val = first.value;
                            val = reduceValue(line, lineNumber, val);
                            if (val >= 0) {
                                return val;
                            }
                        } else {
                            System.out.println(line + " ?");
                            clearStack();
                            return lineNumber + 1;
                        }
                        first = memory.peekFirst();
                    }
                }
            }
        }catch (MemException ise){
            clearStack();
            System.out.println(ise.getMessage());
        }
        return lineNumber+1;
    }

    private static int readInt(String input) {
        boolean sgn=input.startsWith("-");
        if(sgn){
            input=input.substring(1);
        }
        int val=Integer.parseInt(input.length() <= MAX_INT_LEN ? input :
                input.substring(input.length() - MAX_INT_LEN));
        return sgn?-val:val;
    }

    private static int reduceValue(String line, int lineNumber, int val) {
        if(memory.isEmpty())
            throw new MemException("Out of Memory");
        MemEntry prev=memory.getFirst();
        if(prev.name.startsWith("&&")){//remember
            memory.removeFirst();
            if(memory.isEmpty())
                throw new MemException("Out of Memory");
            MemEntry op=memory.removeFirst();
            if(op.name.equals("&REMEMBER")){
                remember(prev.name.substring(2), val);
            }else{
                System.out.println(line +" ?");
            }
            clearStack();
            return lineNumber +1;
        }else if(prev.name.equals("&VALUE")){
            memory.removeFirst();
            if(memory.isEmpty())
                throw new MemException("Out of Memory");
            MemEntry op=memory.removeFirst();
            switch (op.name){
                case "&JGT":
                    clearStack();
                    if(val>0){
                        return prev.value&0x7fffffff;
                    }
                    break;
                case "&JGE":
                    clearStack();
                    if(val>=0){
                        return prev.value&0x7fffffff;
                    }
                    break;
                case "&JNE":
                    clearStack();
                    if(val!=0){
                        return prev.value&0x7fffffff;
                    }
                    break;
                case "&AND":
                    remember("&VALUE",prev.value& val);
                    break;
                case "&ADD":
                    remember("&VALUE",prev.value+ val);
                    break;
                default:
                    System.out.println(line +" ?");
                    clearStack();
                    return lineNumber +1;
            }
        }else{
            switch (prev.name){
                case "&JUMP":
                    memory.removeFirst();
                    clearStack();
                    return val&0x7fffffff;
                case "&PRINT":
                    memory.removeFirst();
                    System.out.println("PRINT: "+ val);
                    clearStack();
                    return lineNumber +1;
                case "&NOT":
                    memory.removeFirst();
                    return reduceValue(line, lineNumber, val ==0?1:0);
                case "&NEG":
                    memory.removeFirst();
                    return reduceValue(line, lineNumber, -val);
                case "&JGT":
                case "&JNE":
                case "&AND":
                case "&ADD":
                    remember("&VALUE", val);
                    break;
            }
        }
        return -1;
    }

    private static void clearStack() {
        while (memory.size() > 0 && memory.getFirst().name.startsWith("&")) {
            memory.removeFirst();//clear stack
        }
    }

}
