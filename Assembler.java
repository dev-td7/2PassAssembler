
import java.io.*;
import java.util.*;

/**
 * License Header:
 *
 * @author Tejas Dastane
 * TE B1 1411072
 * 
 * This is a Pass 2 Assembler implemented for IBM 360/370 machine. This only supports a few instructions as of now.
 * This is capable of handling complex operations such as calculating operands with expressions and indexes.
 * 
 * The output files are mentioned at the end of the program.
 */

public class Assembler {
    //These two tables are fixed and won't change!
    //------------------------------------------------------------------------------------------
    static final Vector<MOT> mot = new Vector<MOT>();
    static POT pot;
    //------------------------------------------------------------------------------------------
    //These tables are dynamic
    //------------------------------------------------------------------------------------------
    static Vector<ST> st = new Vector<ST>();
    static Vector<LT> lt = new Vector<LT>();
    static Vector<BT> bt = new Vector<BT>();
    //------------------------------------------------------------------------------------------
    //These strings hold the outputs of Pass and Pass 2 respectively to be printed to their corresponding output files.
    //------------------------------------------------------------------------------------------
    static String intermediate_code="";
    static String assembled_code = "";
    //------------------------------------------------------------------------------------------
    //################### Update these 4 constants OR ASSEMBLER WON'T RUN! #####################
    //------------------------------------------------------------------------------------------
    static String FILE_PATH = "E:\\Tejas\\Java Projects\\2 Pass Assembler\\src\\Files\\";
    static String INPUT_FILE = "Input.txt";
    static String IC_FILE = "IC.txt";
    static String OUTPUT_FILE = "ObjectCode.txt";
    //##########################################################################################
    static long executionTimeBothPassAssembling,executionTimePass1,executionTimePass2;
    static{
        mot.add(new MOT("L",4,0x58,"RX"));
        mot.add(new MOT("AR",2,0x1A,"RR"));
        mot.add(new MOT("A",4,0x5A,"RX"));
        mot.add(new MOT("ST",4,0x50,"RX"));
        mot.add(new MOT("C",4,0x59,"RX"));
        mot.add(new MOT("BC",4,0x47,"RX"));
        mot.add(new MOT("LR",2,0x18,"RR"));
        mot.add(new MOT("SR",2,0x1B,"RR"));
        mot.add(new MOT("LA",4,0x41,"RX"));
        mot.add(new MOT("BR",2,0x7f,"RR"));
        mot.add(new MOT("BNE",4,0x477,"RX"));
    }
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        System.out.println("Assembler: Assembling started\n\nAssembler: Assembler is starting its Pass 1...\n\nThe Intermediate Code generated for Pass 2:\n");
        doPass1();
        doPass2();
        executionTimeBothPassAssembling = executionTimePass1 + executionTimePass2;
        System.out.println("\nAssembler: Assembling completed. Total time = "+(executionTimeBothPassAssembling/1000000)+"ms");
    }
    
    //--------------------------------------------------------------------- Pass 1 ---------------------------------------------------------------------------
    private static void doPass1() throws FileNotFoundException, IOException{
        File f = new File(FILE_PATH+INPUT_FILE);
        FileInputStream fis = new FileInputStream(f);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader b = new BufferedReader(isr);
        
        long startTimePass1 = System.nanoTime();
        String line;
        int lc = 0;
        while((line = b.readLine())!=null){
            
            String currLabel = "";
            StringTokenizer tokenizer = new StringTokenizer(line, "\t ,+-", true);
            boolean firstWord = true,nextLineInsertFlag = true;
            
            while(tokenizer.hasMoreTokens()){
                String word = tokenizer.nextToken();
                word = word.toUpperCase();
                
                if(word.equals(" ")||word.equals("\t"))  continue;
                if(word.equals(",") || word.equals("+") || word.equals("-")){
                    System.out.print(word);
                    intermediate_code += word;
                    continue;
                }
                try{
                    int regno = Integer.parseInt(word);
                    System.out.print(regno+"");
                    intermediate_code += regno+"";
                    continue;
                }
                catch(NumberFormatException nfe){
                    
                }
                if(pot.find(word)){
                    //This is a pseudo op
                    switch (word) {
                        case "EQU":
                            //Add value to this symbol. Value is located next to EQU.
                            
                            nextLineInsertFlag = false;
                            int index = ST.getIndex(st, currLabel);
                            word = tokenizer.nextToken();
                            while(word.equals(" ")|| word.equals("\t")) word = tokenizer.nextToken();
                            if(word.equals("*")){
                                st.get(index).update(currLabel, lc, 1, 'A');
                            }
                            else{
                                int val = Integer.parseInt(word);
                                st.get(index).update(currLabel, val, 1, 'A');
                            }
                            break;
                        case "START":
                            nextLineInsertFlag = false;
                            st.lastElement().update(currLabel, 0, 1, 'R');
                            lc=0;
                            word = tokenizer.nextToken();
                            while(word.equals(" ") || word.equals("\t")) word = tokenizer.nextToken();
                            break;
                        case "USING":
                        	System.out.print(word+" ");
                        	intermediate_code += word + " ";
                                word = tokenizer.nextToken();
                                while(word.equals(" ") || word.equals("\t")) word = tokenizer.nextToken();
                                if(ST.find(st, word)){
                                    int symindex = ST.getIndex(st, word);
                                    String id = st.get(symindex).getId();
                                    intermediate_code += id+" ";
                                    System.out.print(id);
                                }
                                else{
                                    try{
                                        int x = Integer.parseInt(word);
                                        intermediate_code += word;
                                        System.out.print(word);
                                    }
                                    catch(NumberFormatException nfe){
                                        if(word.equals("*")){
                                            intermediate_code += word;
                                            System.out.print(word);
                                        }
                                        else{
                                            ST s = new ST(word,-1,-1,'R');
                                            st.add(s);
                                            String id = st.lastElement().getId();
                                            intermediate_code += id+"";
                                            System.out.print(id);
                                        }
                                    }
                                    
                                }
                                word = tokenizer.nextToken();
                                while(word.equals(" ") || word.equals("\t") || word.equals(",")) word = tokenizer.nextToken();
                                System.out.print(",");
                        	intermediate_code += ",";
                                if(ST.find(st, word)){
                                    int symindex = ST.getIndex(st, word);
                                    String id = st.get(symindex).getId();
                                    intermediate_code += id+" ";
                                    System.out.print(id);
                                }
                                else{
                                    try{
                                        int x = Integer.parseInt(word);
                                        intermediate_code += word;
                                        System.out.print(word);
                                    }
                                    catch(NumberFormatException nfe){
                                        ST s = new ST(word,-1,-1,'R');
                                        st.add(s);
                                        String id = st.lastElement().getId();
                                        intermediate_code += id+"";
                                        System.out.print(id);
                                    }
                                }
                            continue;
                        case "LTORG":
                            System.out.print("LTORG ");
                            intermediate_code += "LTORG ";
                            int incrementValue = lc%8;
                            if(incrementValue!=0) incrementValue = 8 - incrementValue;
                            lc+=incrementValue;
                            for (LT lt1 : lt) {
                                if(lt1.getValue()==-1) lt1.setValue(lc);
                                lc+=4;
                            }
                            break;
                        case "DC":
                            System.out.print(lc+" ");
                            intermediate_code += lc+" ";
                            System.out.print("DC ");
                            intermediate_code += "DC ";
                            //Ignore spaces and reach to the value of value.
                            word = tokenizer.nextToken();
                            while(word.equals(" ") || word.equals("\t")) word = tokenizer.nextToken();
                            //Get the value
                            System.out.print(word);
                            int symbol_val_length = 4;
                            lc+=4;
                            intermediate_code += word;
                            //This is an array
                            while(tokenizer.hasMoreTokens()){
                                word = tokenizer.nextToken();
                                System.out.print(word);
                                intermediate_code += word;
                                if(word.equals(",")){
                                    lc+=4;
                                    symbol_val_length+=4;
                                }
                                if(word.endsWith("\'")) break;
                            }
                            //Update with latest values
                            index = ST.getIndex(st, currLabel);
                            st.get(index).update(currLabel, lc-symbol_val_length, symbol_val_length, 'R');
                            break;
                        case "DS":
                            System.out.print(lc+" ");
                            System.out.print("DS ");
                            intermediate_code += lc+" DS ";
                            word = tokenizer.nextToken();
                            while(word.equals(" ") || word.equals("\t")) word = tokenizer.nextToken();
                            System.out.print(word);
                            intermediate_code += word;
                            int val = Integer.parseInt(word.substring(0,word.length()-1));
                            index = ST.getIndex(st, currLabel);
                            st.get(index).update(currLabel, lc, 4, 'R');
                            lc+=val*4;
                            break;
                        case "END":
                        	incrementValue = lc%8;
                            if(incrementValue!=0) incrementValue = 8 - incrementValue;
                            lc+=incrementValue;
                            for (LT lt1 : lt) {
                                if(lt1.getValue()==-1) lt1.setValue(lc);
                                lc+=4;
                            }
                            System.out.print(lc+" ");
                            System.out.print("END");
                            intermediate_code += lc+" END";
                            break;
                    }
                }
                else if(MOT.find(mot, word)){
                    System.out.print(lc+" ");
                    //This is a machine instruction
                    firstWord = false;
                    System.out.print(word+" ");
                    intermediate_code += lc+" "+word+" ";
                    if(!currLabel.equals("")){
                        //The previous symbol was a label
                        st.lastElement().update(currLabel, lc, 1, 'R');
                    }
                    lc+=mot.get(MOT.getIndex(mot, word)).getLength();
                }
                else if(ST.find(st, word)){
                    //This is a previously stored symbol
                    if(firstWord){
                        //Dont print id of labels!
                        currLabel = word;
                        firstWord = false;
                        continue;
                    }
                    int index = ST.getIndex(st, word);
                    String id = st.get(index).getId();
                    System.out.print(id);
                    intermediate_code += id+"";
                }
                else if(LT.find(lt, word)){
                    //This is a previously stored literal
                    int index = LT.getIndex(lt, word);
                    String id = lt.get(index).getId();
                    System.out.print(id);
                    intermediate_code += id+"";
                }
                else{
                    //This is a new symbol or a literal
                    
                    if(word.startsWith("=")){
                        //This is a literal
                        
                        word = word.substring(1);
                        LT l = new LT(word,-1,4,'R');
                        lt.add(l);
                        System.out.print(lt.lastElement().getId());
                        intermediate_code += lt.lastElement().getId()+"";
                    }
                    else{
                        //This is a new symbol
                        
                        if(word.contains("(") && word.contains(")")){
                            StringTokenizer indexSplitter = new StringTokenizer(word,"()",true);
                            
                            String label = indexSplitter.nextToken();
                            if(ST.find(st,label)){
                                int index = ST.getIndex(st,label);
                                String id = st.get(index).getId();
                                System.out.print(id);
                                intermediate_code += id+"";
                            }
                            else{
                                ST s = new ST(label,-1,-1,'R');
                                st.add(s);
                                String id = st.lastElement().getId();
                                System.out.print(id);
                                intermediate_code += id+"";
                            }
                            label = indexSplitter.nextToken();
                            while(label.equals(" ")) label = indexSplitter.nextToken();
                            label = indexSplitter.nextToken();
                            System.out.print("(");
                            intermediate_code+="(";
                            while(label.equals(" ")) label = indexSplitter.nextToken();
                            //Index is here
                            int index = ST.find(st, label, true);
                            String id = st.get(index).getId();
                            System.out.print(id);
                            intermediate_code += id+"";
                            System.out.print(")");
                            intermediate_code+=")";
                        }
                        else{
                            currLabel = word;
                            ST s = new ST(currLabel,-1,-1,'R');
                            st.add(s);
                            if(!firstWord){
                                String id = st.lastElement().getId();
                                System.out.print(id);
                                intermediate_code += id+"";
                                firstWord = false;
                            }
                        }
                    }
                }
            }
            
            if(nextLineInsertFlag){
                System.out.println();
                intermediate_code += " \n";
            }
        }
        
        System.out.println("\n\nSymbol Table: ");
        System.out.println("ID\tSymbol\t\tValue\tLength\tRelocation\n");
        for(ST s : st){
            System.out.println(s);
        }
        System.out.println("\n\nLiteral Table: ");
        System.out.println("ID\tLiteral\t\tValue\tLength\tRelocation\n");
        for(LT l : lt){
            System.out.println(l);
        }
        
        File intemediate_code_file = new File(FILE_PATH+IC_FILE);
        PrintWriter out = new PrintWriter(intemediate_code_file);
        for(String ic: intermediate_code.split("\n")){
            out.println(ic);
        }
        out.close();
        
        long endTimePass1 = System.nanoTime();
        executionTimePass1 = endTimePass1 - startTimePass1;
        System.out.println("\n\nAssembler: Pass 1 finished in "+(double)(executionTimePass1/1000000)+"ms");
    }
    
    //--------------------------------------------------------------------- Pass 2 ---------------------------------------------------------------------------
    private static void doPass2() throws FileNotFoundException, IOException{
        File f = new File(FILE_PATH+IC_FILE);
        FileInputStream fis = new FileInputStream(f);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader b = new BufferedReader(isr);
        
        System.out.println("Assembler: Pass 2 started. Processing Intermediate code..\n\n");
        
        String thisIcLine;
        long startTimePass2 = System.nanoTime();
        int litctr=0;
        while((thisIcLine = b.readLine()) != null){
            StringTokenizer icLine = new StringTokenizer(thisIcLine,"\t ,",true);
            
            int lc = 0;
            while(icLine.hasMoreTokens()){
                String thisWord = icLine.nextToken();
                if(thisWord.equals(" ") || thisWord.equals("\n") || thisWord.equals("\t")) continue;
                
                //This statement comes first because I need to check for statements like USING which don't precede by Location Counter value.
                if(pot.find(thisWord)){
                    switch(thisWord){
                        
                        case "LTORG":
                            
                            //Prints literals
                            for(int i=litctr;i<lt.size();i++){
                                lc = lt.get(i).getValue();
                                int litsize = lt.get(i).getLength();
                                System.out.print(lc+"\t");
                                assembled_code += lc+"\t";
                                
                                String literal = lt.get(i).getLiteral();
                                StringTokenizer litTokens = new StringTokenizer(literal,"()\'",true);
                                while(litTokens.hasMoreTokens()){
                                    String litWord = litTokens.nextToken();
                                    switch(litWord){
                                        case "A":
                                            //Put the address of the literal.
                                            litWord = litTokens.nextToken();
                                            while(litWord.equals(" ") || litWord.equals("(")) litWord = litTokens.nextToken();
                                            if(ST.find(st, litWord)){
                                                int index = ST.getIndex(st, litWord);
                                                int addrOfSymbol = st.get(index).getValue();
                                                System.out.println(addrOfSymbol);
                                                assembled_code += addrOfSymbol;
                                                while(litWord.equals(" ")) litWord = litTokens.nextToken();
                                                litWord = litTokens.nextToken();
                                            }
                                            break;
                                        case "F": System.out.print("X"); assembled_code+="X"; break;
                                        case "\'": System.out.print("\'"); assembled_code+="\'"; break;
                                        default:
                                            int x = Integer.parseInt(litWord);
                                            int noOfDigits = (int) Math.log10(x) + 1;
                                            for(int digits=0;digits<8-noOfDigits;digits++){
                                                System.out.print("0");
                                                assembled_code+="0";
                                            }
                                            assembled_code+=x+"";
                                            System.out.print(x);
                                            break;
                                    }
                                }
                                
                                litctr = i+1;
                                //Stopping condition. Stops at literals till the point ltorg statement processed it.
                                if(i < lt.size() - 1 && lc+litsize != lt.get(i+1).getValue()) {
                                    break;
                                }
                                assembled_code+="\n";
                                System.out.println("");
                            }
                            break;
                            
                        case "USING":
                            
                            //Input in Base Table.
                            thisWord = icLine.nextToken();
                            while(thisWord.equals(" ") || thisWord.equals("\t")) thisWord = icLine.nextToken();
                            
                            int baseVal;
                            if(thisWord.equals("*")) baseVal = lc;
                            else baseVal = evaluate(thisWord);
                            
                            thisWord = icLine.nextToken();
                            while(thisWord.equals(" ") || thisWord.equals("\t") || thisWord.equals(",")) thisWord = icLine.nextToken();
                            int basereg = evaluate(thisWord);
                            
                            int baseEntry = BT.hasEntered(bt, basereg);
                            if(baseEntry == -1){
                                BT newBaseEntry = new BT(basereg, baseVal);
                                bt.add(newBaseEntry);
                            }
                            else{
                                bt.get(baseEntry).setValue(baseVal);
                            }
                        
                            break;
                    }
                }
                else{
                    //Other statements. Starts with Location Counter Value.
                    lc = Integer.parseInt(thisWord);
                    thisWord = icLine.nextToken();
                    while(thisWord.equals(" ") || thisWord.equals("\t")) thisWord = icLine.nextToken();
                    
                    //This is a machine statement
                    int index = MOT.find(mot, thisWord, true);
                    if(index == -1){
                        //This might be DS or DC
                        switch(thisWord){
                            case "DS":
                                System.out.print(lc);
                                assembled_code+=lc;
                                thisWord = icLine.nextToken();
                                while(thisWord.equals(" ")) thisWord = icLine.nextToken();
                                thisWord = thisWord.substring(0,thisWord.length()-1);
                                int limit = Integer.parseInt(thisWord);
                                System.out.println("\t_");
                                assembled_code+="\t_\n";
                                for(int i=lc+4;i<lc+limit*4;i+=4){
                                    System.out.println(i+"\t_");
                                    assembled_code+=i+"\t_\n";
                                }
                                lc+=limit*4;
                                break;
                            case "DC":
                                System.out.print(lc+"\t");
                                assembled_code+=lc+"\t";
                                thisWord = icLine.nextToken();
                                while(thisWord.equals(" ")) thisWord = icLine.nextToken();
                                
                                if(!thisWord.contains("\'")) thisWord = thisWord.substring(2);
                                else thisWord = thisWord.substring(2,thisWord.length()-1);
                                
                                String hex = Integer.toHexString(Integer.parseInt(thisWord));
                                int h = hex.length();
                                int noOfDigits = (int) Math.log10(h) + 1;
                                for(int i=0;i<8-h;i++){
                                    hex = "0"+hex;
                                }
                                System.out.println(hex);
                                assembled_code+=hex+"\n";
                                lc+=4;
                                thisWord = icLine.nextToken();
                                if(!icLine.hasMoreTokens()) break;
                                while(thisWord.equals(" ") || thisWord.equals(",")) thisWord = icLine.nextToken();
                                while(!thisWord.contains("\'")){
                                    hex = Integer.toHexString(Integer.parseInt(thisWord));
                                    h = hex.length();
                                    for(int i=0;i<8-h;i++){
                                        hex = "0"+hex;
                                    }
                                    System.out.println(lc+"\t"+hex);
                                    assembled_code+=lc+"\t"+hex+"\n";
                                    thisWord = icLine.nextToken();
                                    while(thisWord.equals(" ") || thisWord.equals(",")) thisWord = icLine.nextToken();
                                }
                                thisWord = thisWord.substring(0,thisWord.length()-1);
                                hex = Integer.toHexString(Integer.parseInt(thisWord));
                                h = hex.length();
                                for(int i=0;i<8-h;i++){
                                    hex = "0"+hex;
                                }
                                System.out.println(lc+"\tX'"+hex+"'");
                                assembled_code+=lc+"\tX'"+hex+"'\n";
                                break;
                                
                            case "END":
                                //This will print all the remaining literals.
                                for(int i=litctr;i<lt.size();i++){
                                    lc = lt.get(i).getValue();
                                    System.out.print(lc+" ");
                                    assembled_code+=lc+" ";

                                    String literal = lt.get(i).getLiteral();
                                    StringTokenizer litTokens = new StringTokenizer(literal,"\'",true);
                                    while(litTokens.hasMoreTokens()){
                                        String litWord = litTokens.nextToken();
                                        switch(litWord){
                                            case "F": System.out.print("X"); assembled_code+="X"; break;
                                            case "\'": System.out.print("\'"); assembled_code+="\'";break;
                                            default:
                                                int x = Integer.parseInt(litWord);
                                                noOfDigits = (int) Math.log10(x) + 1;
                                                for(int digits=0;digits<8-noOfDigits;digits++){
                                                    System.out.print("0");
                                                    assembled_code+="0";
                                                }
                                                System.out.print(x);
                                                assembled_code+=x;
                                                break;
                                        }
                                    }
                                    System.out.println("");
                                    assembled_code += "\n";
                                }
                                break;
                        }
                    }
                    else{
                        System.out.print(lc+"\t");
                        assembled_code+=lc+"\t";
                        int reg1;
                        String type = mot.get(index).getType();

                        if(thisWord.equals("BNE")){
                            //Requires Special Processing
                            System.out.print("BC\t");
                            assembled_code+="BC\t";
                            //First operand: Type = register
                            reg1 = 7;                           //Fixed Value
                        }
                        else if(thisWord.equals("BR")){
                            //Requires Special Processing
                            System.out.print("BCR\t");
                            assembled_code+="BCR\t";
                            //First operand: Type = register
                            reg1 = 15;                           //Fixed Value
                        }
                        else{
                            System.out.print(thisWord+"\t");
                            assembled_code+=thisWord+"\t";
                            thisWord = icLine.nextToken();
                            while(thisWord.equals(" ") || thisWord.equals("\t")) thisWord = icLine.nextToken();

                            //First operand: Type = register
                            reg1 = evaluate(thisWord);
                        }
                        thisWord = icLine.nextToken();
                        while(thisWord.equals(" ") || thisWord.equals("\t") || thisWord.equals(",")) thisWord = icLine.nextToken();

                        switch(type){
                            case "RR":
                                //Second operand: Type = register
                                int reg2 = evaluate(thisWord);
                                System.out.print(reg1+","+reg2);
                                assembled_code += reg1+","+reg2;
                                break;
                            case "RX":
                                //Second operand: Type = Address

                                //Second operand is address of literal
                                if(thisWord.startsWith("lt#")){
                                    int indexVal=0,baseRegister=0,offset=0;
                                    thisWord = thisWord.substring(3);
                                    int litindex = Integer.parseInt(thisWord);
                                    int ea = lt.get(litindex).getValue();
                                    int baseRegisterIndex = BT.getBaseRegister(bt, ea);
                                    int baseRegisterVal = bt.get(baseRegisterIndex).getValue();
                                    baseRegister = bt.get(baseRegisterIndex).getBase_register();
                                    offset = ea - baseRegisterVal;
                                    indexVal=0;
                                    String addrFormat = reg1+","+offset+"("+indexVal+","+baseRegister+")";
                                    System.out.print(addrFormat+" ");
                                    assembled_code += addrFormat + " ";
                                }
                                //Second operand is address of symbol
                                else{
                                    //Processing for symbol.
                                    int indexVal=0,baseRegister=0,offset=0;
                                    StringTokenizer bracket = new StringTokenizer(thisWord," ()",true);
                                    while(bracket.hasMoreTokens()){
                                        String elem = bracket.nextToken();

                                        if(elem.equals("(")){
                                            //Get index value here
                                            elem = bracket.nextToken();
                                            while(elem.equals(" ")) elem = bracket.nextToken();
                                            indexVal = evaluate(elem);
                                            elem = bracket.nextToken();
                                            while(elem.equals(" ")) elem = bracket.nextToken();
                                        }
                                        else{
                                            //Consider DATA(INDEX) example. DATA field being processed
                                            int Symbolindex = Integer.parseInt(elem.substring(3));
                                            int ea = st.get(Symbolindex).getValue();
                                            int baseRegisterIndex = BT.getBaseRegister(bt, ea);
                                            if(baseRegisterIndex == -1){
                                                System.err.println("CRITICAL ERROR! Base reg value is -1");
                                                return;
                                            }
                                            int baseRegisterVal = bt.get(baseRegisterIndex).getValue();
                                            baseRegister = bt.get(baseRegisterIndex).getBase_register();
                                            offset = ea - baseRegisterVal;

                                            indexVal = 0;
                                        }
                                    }
                                    String addrFormat = reg1+","+offset+"("+indexVal+","+baseRegister+")";
                                    System.out.print(addrFormat+" ");
                                    assembled_code += addrFormat + " ";
                                }
                                break;
                        }
                        lc += mot.get(index).getLength();
                        }

                }
                
                System.out.println("");
                assembled_code += "\n";
            }
        }
        
        System.out.println("Base Table Entries:\nBase Register\tValue");
        for(BT base: bt){
            System.out.println(base.getBase_register()+"\t\t"+base.getValue());
        }
        
        File assembled_file = new File(FILE_PATH+OUTPUT_FILE);
        PrintWriter out = new PrintWriter(assembled_file);
        for(String ac: assembled_code.split("\n")){
            out.append(ac);
            out.println();
        }
        out.close();
        
        long endTimePass2 = System.nanoTime();
        executionTimePass2 = endTimePass2 - startTimePass2;
        System.out.println("\nAssembler: Pass 2 completed in "+(executionTimePass2/1000000)+"ms");
        
    }
    
    //This function evaluates expressions.
    static int evaluate(String thisWord){
        int result = 0;
        char op = '+';
        StringTokenizer elements = new StringTokenizer(thisWord,"+-*/ ",true);
        while(elements.hasMoreTokens()){
            String element = elements.nextToken();
            if(element.equals(" ")) continue;
            
            switch(element){
                case "+": op = '+'; break;
                case "-": op = '-'; break;
                case "*": op = '*'; break;
                case "/": op = '/'; break;
                default:
                    int val;
                    if(element.startsWith("st#")){
                        int index = Integer.parseInt(element.substring(3));
                        val = st.get(index).getValue();
                    }
                    else val = Integer.parseInt(element);

                    switch(op){
                        case '+':
                            result += val; break;
                        case '-':
                            result -= val; break;
                        case '*':
                            result *= val; break;
                        case '/':
                            if(val!=0) result /= val; 
                            else result = -1;
                            break;
                    }
                    break;
            }
        }
        return result;
    }
}


//-------------------------All the necessary data structures start here--------------------------------

//Machine Opcode Table class
class MOT{
    String instruction;
    int length;
    int opcode;
    String type;

    public MOT(String instruction, int length, int opcode, String type) {
        this.instruction = instruction;
        this.length = length;
        this.opcode = opcode;
        this.type = type;
    }

    public int getLength() {
        return length;
    }
    
    public static boolean find(Vector<MOT> v,String instr){
        for(MOT m : v){
            if(m.instruction.equals(instr)) return true;
        }
        return false;
    }
    
    public static int find(Vector<MOT> v,String instr,boolean returnValue){
        int i=-1;
        for(MOT m : v){
            i++;
            if(m.instruction.equals(instr)) return i;
        }
        return -1;
    }
    
    public static int getIndex(Vector<MOT> v,String instr){
        for(int i=0;i<v.size();i++){
            if(v.get(i).instruction.equals(instr)) return i;
        }
        return -1;
    }

    public String getType() {
        return type;
    }
}

//Pseudo-opcode Table class
class POT{
    private static Hashtable<String,String> table = new Hashtable();

    static{
        table.put("START", "start()");
        table.put("DS", "ds()");
        table.put("DC", "dc()");
        table.put("USING", "using()");
        table.put("EQU", "equ()");
        table.put("LTORG", "ltorg()");
        table.put("END", "end()");
    }
    
    public static boolean find(String pseudoOp){
        if(table.containsKey(pseudoOp)) return true;
        return false;
    }
}

//Symbol Table class
class ST{
    private String id;
    private static int idno = 0;
    private String symbol;
    private int value;
    private int length;
    private char relocation;

    public ST(String symbol, int value, int length, char relocation) {
        this.id = "st#"+idno;
        idno++;
        this.symbol = symbol;
        this.value = value;
        this.length = length;
        this.relocation = relocation;
    }

    public void setValue(int value) {
        this.value = value;
    }
    
    public void update(String symbol, int value, int length, char relocation) {
        this.symbol = symbol;
        this.value = value;
        this.length = length;
        this.relocation = relocation;
    }

    public String getId() {
        return id;
    }
    
    public static int getIndex(Vector<ST> v,String name){
        for(int i=0;i<v.size();i++){
            if(v.get(i).symbol.equals(name)) return i;
        }
        return -1;
    }
    
    public static boolean find(Vector<ST> v,String name){
        for(ST s : v){
            if(s.symbol.equals(name)) return true;
        }
        return false;
    }
    
    public static int find(Vector<ST> v,String name,boolean resultReqd){
        int i=-1;
        for(ST s : v){
            i++;
            if(s.symbol.equals(name)) return i;
        }
        return -1;
    }
    
    public String toString(){
        return(id+"\t"+symbol+"\t\t"+value+"\t"+length+"\t"+relocation);
    }
    
    public int getValue(){
    	return value;
    }
    
}

//Literal Table class
class LT{
    private String id;
    private static int idno = 0;
    private String literal;
    private int value;
    private int length;
    private char relocation;

    public LT(String literal, int value, int length, char relocation) {
        this.id = "lt#"+idno;
        idno++;
        this.literal = literal;
        this.value = value;
        this.length = length;
        this.relocation = relocation;
    }
    
    public void update(String literal, int value, int length, char relocation) {
        this.literal = literal;
        this.value = value;
        this.length = length;
        this.relocation = relocation;
    }

    public void setValue(int value) {
        this.value = value;
    }
    
    public String getId() {
        return id;
    }
    
    public static int getIndex(Vector<LT> v,String name){
        for(int i=0;i<v.size();i++){
            if(v.get(i).literal.equals(name)) return i;
        }
        return -1;
    }
    
    public static boolean find(Vector<LT> v,String name){
        for(LT l : v){
            if(l.literal.equals(name)) return true;
        }
        return false;
    }
    
    public String toString(){
        return(id+"\t"+literal+"\t\t"+value+"\t"+length+"\t"+relocation);
    }
    
    public int getValue(){
    	return value;
    }

    public String getLiteral() {
        return literal;
    }

    public int getLength() {
        return length;
    }
}

//--------------------------------------Base Table needed only in Pass 2-------------------------------------------
class BT{
    private int base_register;
    private int value;
    
    public BT(int br, int val){
        base_register = br;
        value = val;
    }
    
    public static int getBaseRegister(Vector<BT> v,int ea){
        int i=-1;
        int min_i=-1;
        int min = 99999999;
        for(BT b : v){
            i++;
            int val = Math.abs(ea - b.value);
            if(val < min){
                min = val;
                min_i = i;
            }
        }
        if(min_i == -1) return -1;  //  !!!!!THIS SHOULD NOT BE POSSIBLE!!!!!!!!
        return min_i;
    }
    
    public static int hasEntered(Vector<BT> v,int br){
        int i=-1;
        for(BT b: v){
            i++;
            if(b.base_register == br) return i;
        }
        return -1;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public int getBase_register() {
        return base_register;
    }
}




/**
 * Output Files are as Follows:
 * For Input 1:
 * 
 *      Pass 1: Files/Formatted_IC_Input_1.txt
 *      Pass 2: Files/ObjectCode_Input_1.txt
 * For Input 2:
 * 
 *      Pass 1: Files/Formatted_IC_Input_2.txt
 *      Pass 2: Files/ObjectCode_Input_2.txt
 * 
 * Please refer them for the expected outputs! 
 * ---------------------------------------------------------THANK YOU--------------------------------------------------------------
 * 
 */