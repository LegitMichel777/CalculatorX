import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class CalculatorX extends JFrame implements ActionListener {
    JButton btn0, btnd, btneq, btnac;
    Integer nstx, nsty, szx, szy, spx, spy, ostx, osty;
    JLabel eqs;
    String exp;
    JTextField res = new JTextField();
    java.util.List<JButton> btnn = new ArrayList<>();
    java.util.List<JButton> btnb = new ArrayList<>();
    java.util.List<JButton> btnf = new ArrayList<>();
    java.util.List<String> exph = new ArrayList<>();
    Map<String,Integer>opls=new HashMap<>();
    Map<String,Integer>funcID=new HashMap<>();
    Map<Integer,String>IDfunc=new HashMap<>();
    Map<String,Double>consts=new HashMap<>();
    boolean superiorHardware=true;
    class propbtn {
        String payload;
        boolean isBig;
        boolean needPren;
        public propbtn(String payload, boolean isBig, boolean needPren) {
            this.payload = payload;
            this.isBig = isBig;
            this.needPren=needPren;
        }
    }
    Map<Integer, propbtn> pbtnb = new HashMap<>();
    Map<Integer,String> pbtnf = new HashMap<>();
    void Eval() {
        if (exph.size()==0) {
            eqs.setText("=0");
            return;
        }
        funcID.forEach((i,j) -> {
            IDfunc.put(j,i);
        });
        Stack<String> opr = new Stack<>();
        Stack<String> funs=new Stack<>();
        java.util.List<String> ss=new ArrayList<>();
        String[] ftmp=new String[exph.size()+1];
        Integer ftmpsz=-1;
        String acc="";
        for (int i=0;i<exph.size();i++) {
            if (exph.get(i).length()==1&&((exph.get(i).charAt(0)>='0'&&exph.get(i).charAt(0)<='9')||exph.get(i).charAt(0)=='.')) {
                acc+=exph.get(i);
            } else {
                if (!acc.isEmpty()) {
                    ftmp[++ftmpsz]=acc;
                    acc="";
                }
                if (i>0) {
                    if (opls.containsKey(exph.get(i-1))&&exph.get(i)=="-") ftmp[++ftmpsz]="U-";
                    else ftmp[++ftmpsz]=exph.get(i);
                } else {
                    if (exph.get(i)=="-") ftmp[++ftmpsz]="U-";
                    else ftmp[++ftmpsz]=exph.get(i);
                }
                /*
                if (i>0) {
                    if (exph.get(i - 1) == "(" && exph.get(i) == "-") acc = "-";
                    else ftmp[++ftmpsz]=exph.get(i);
                } else {
                    if (exph.get(i)=="-") acc="-";
                    else ftmp[++ftmpsz]=exph.get(i);
                }
                */
            }
        }
        if (!acc.isEmpty()) ftmp[++ftmpsz]=acc;
        String[] fexp=new String[4*ftmpsz+1];
        Integer fexpsz=-1;
        for (int i=0;i<ftmpsz;i++) {
            fexp[++fexpsz]=ftmp[i];
            if (((ftmp[i].charAt(0)>='0'&&ftmp[i].charAt(0)<='9')||consts.containsKey(ftmp[i]))&&consts.containsKey(ftmp[i+1])) fexp[++fexpsz]="x";
            else if (consts.containsKey(ftmp[i])&&(ftmp[i+1].charAt(0)>='0'&&ftmp[i+1].charAt(0)<='9')) fexp[++fexpsz]="x";
            else if (ftmp[i]==")"&&(!opls.containsKey(ftmp[i+1])||ftmp[i+1]=="(")&&ftmp[i+1]!=")") fexp[++fexpsz]="x"; //detect seg+anything
            else if (((ftmp[i].charAt(0)>='0'&&ftmp[i].charAt(0)<='9')||consts.containsKey(ftmp[i]))&&funcID.containsKey(ftmp[i+1])) fexp[++fexpsz]="x";
            else if ((ftmp[i].charAt(0)>='0'&&ftmp[i].charAt(0)<='9')&&ftmp[i+1]=="(") fexp[++fexpsz]="x";
            else if (ftmp[i+1]=="("&&(ftmp[i].charAt(0)>='0'&&ftmp[i].charAt(0)<='9')) fexp[++fexpsz]="x";
        }
        fexp[++fexpsz]=ftmp[ftmpsz];
        int verfBrak=0;
        for (int i=0;i<=fexpsz;i++) {
            if (fexp[i]==")") verfBrak++;
            else if (fexp[i]=="(") verfBrak--;
        }
        if (verfBrak>0) {
            eqs.setText("Syntax error!");
            return;
        } else {
            for (int i=0;i<-verfBrak;i++) fexp[++fexpsz]=")";
        }
        for (int i=0;i<=fexpsz;i++) {
            if (funcID.containsKey(fexp[i])) {
                Integer mtch=0;
                for (int j=i+2;j<=fexpsz;j++) {
                    if (fexp[j]==")") {
                        if (mtch==0) {
                            fexp[j]="]";
                            break;
                        }
                        mtch--;
                    } else if (fexp[j]=="(") {
                        mtch++;
                    }
                }
            }
        }
        for (int i=0;i<=fexpsz;i++) {
            String token=fexp[i];
            System.out.println(token);
            if (token=="(") {
                opr.push("(");
            } else if (opls.containsKey(token)) {
                while (!opr.empty()) {
                    Integer prec1 = opls.get(token);
                    Integer prec2 = opls.get(opr.peek());
                    if (prec2 > prec1 || (prec2 == prec1 && token != "^")) {
                        ss.add(opr.pop());
                    } else break;
                }
                opr.push(token);
            } else if (funcID.containsKey(token)) {
                funs.push(token);
            } else if (token==")"||token=="]") {
                while (opr.peek() != "(") {
                    ss.add(opr.pop());
                    if (opr.size() == 0) {
                        eqs.setText("Syntax error!");
                        return;
                    }
                }
                if (opr.size() == 0) {
                    eqs.setText("Syntax error!");
                    return;
                }
                opr.pop();
                if (token == "]") {
                    if (funs.empty()) {
                        eqs.setText("Syntax error!");
                        return;
                     }
                    ss.add(funs.pop());
                }
            } else if (consts.containsKey(token)) {
                ss.add(String.valueOf(consts.get(token)));
            } else {
                ss.add(token);
            }
        }
        while (!opr.empty()) ss.add(opr.pop());
        if (funs.size()!=0) {
            eqs.setText("Syntax error!");
            return;
        }
        ListIterator it2=ss.listIterator();
        Stack<Double>num = new Stack<>();
        while (it2.hasNext()) {
            String cur= (String) it2.next();
            if (cur=="U-") {
                if (num.size()==0) {
                    eqs.setText("Syntax error!");
                    return;
                }
                double num1=num.pop();
                num.push(-num1);
            } else if (opls.containsKey(cur)) {
                if (num.size()==0) {
                    eqs.setText("Syntax error!");
                    return;
                }
                double num1 = num.pop();
                if (num.size()==0) {
                    eqs.setText("Syntax error!");
                    return;
                }
                double num2 = num.pop();
                if (cur == "+") num.push(num1 + num2);
                else if (cur == "-") num.push(num2 - num1);
                else if (cur == "^") num.push(Math.pow(num2, num1));
                else {
                    if (superiorHardware) {
                        if (cur=="x") num.push(num1 * num2);
                        else if (cur == "÷") {
                            if (num1==0) {
                                eqs.setText("=Error! Division by 0.");
                                return;
                            }
                            num.push(num2 / num1);
                        }
                    } else {
                        if (cur=="x") num.push(num1 * num2);
                        else if (cur == "/") {
                            if (num1==0) {
                                eqs.setText("=Error! Division by 0.");
                                return;
                            }
                            num.push(num2 / num1);
                        }
                    }
                }
            } else if (funcID.containsKey(cur)) {
                if (num.size()==0) {
                    eqs.setText("Syntax error!");
                    return;
                }
                double num1=num.pop();
                if (cur=="ln") num.push(Math.log(num1));
                else if (cur=="sin") num.push(Math.sin(num1));
                else if (cur=="asin") num.push(Math.asin(num1));
                else if (cur=="cos") num.push(Math.cos(num1));
                else if (cur=="acos") num.push(Math.acos(num1));
                else if (cur=="tan") num.push(Math.tan(num1));
                else if (cur=="atan") num.push(Math.atan(num1));
                else {
                    if (superiorHardware) {
                        if (cur=="√") {
                            if (num1<0) {
                                eqs.setText("=Error! Square root of negative number.");
                                return;
                            }
                            num.push(Math.sqrt(num1));
                        }
                    } else {
                        if (cur=="sqrt") {
                            if (num1<0) {
                                eqs.setText("=Error! Square root of negative number.");
                                return;
                            }
                            num.push(Math.sqrt(num1));
                        }
                    }
                }
            } else num.push(Double.valueOf(cur));
        }
        if (num.size()!=1) {
            eqs.setText("Syntax error!");
            return;
        }
        eqs.setText("="+num.peek());
    }
    public CalculatorX() {
        opls.put("-",1);
        opls.put("+",1);
        opls.put("U-",3);
        opls.put("^",4);
        opls.put("(",-1);
        funcID.put("ln",1);
        funcID.put("sin",2);
        funcID.put("asin",3);
        funcID.put("cos",4);
        funcID.put("acos",5);
        funcID.put("tan",7);
        funcID.put("atan",8);
        consts.put("e",Math.E);
        pbtnb.put(1,new propbtn("ln", false,true));
        pbtnb.put(2,new propbtn("sin", false,true));
        pbtnb.put(3,new propbtn("asin", false,true));
        pbtnb.put(4,new propbtn("^", true,true));
        pbtnb.put(5,new propbtn("cos", false,true));
        pbtnb.put(6,new propbtn("acos", false,true));
        pbtnb.put(8,new propbtn("tan", false,true));
        pbtnb.put(9,new propbtn("atan", false,true));
        pbtnb.put(10,new propbtn("DEL", false,false));
        pbtnb.put(12,new propbtn("e", true,false));
        pbtnf.put(1,"(");
        pbtnf.put(2,")");
        pbtnf.put(3,"+");
        pbtnf.put(4,"-");
        if (superiorHardware) {
            pbtnb.put(7,new propbtn("√", true,true));
            pbtnb.put(11,new propbtn("π", true,false));
            consts.put("π",Math.PI);
            funcID.put("√",6);
            opls.put("÷",2);
            opls.put("x",2);
            pbtnf.put(5,"x");
            pbtnf.put(6,"÷");
        } else {
            pbtnb.put(7,new propbtn("sqrt", false,true));
            pbtnb.put(11,new propbtn("pi", false,false));
            consts.put("pi",Math.PI);
            funcID.put("sqrt",6);
            opls.put("/",2);
            opls.put("x",2);
            pbtnf.put(5,"x");
            pbtnf.put(6,"/");
        }
        exp = "";
        nstx = 60;
        nsty = 210;
        szx = szy = 100;
        spx = spy = 20;
        ostx = 450;
        osty = 210;
        Font ft32,ft48,ft64,ftexp;
        if (superiorHardware) {
            ft32=new Font("SF Pro Display", Font.BOLD, 32);
            ft48=new Font("SF Pro Display",Font.BOLD,48);
            ft64=new Font("SF Pro Display",Font.BOLD,64);
            ftexp=new Font("SF Pro Display", Font.PLAIN, 32);
        } else {
            ft32=new Font("Helvetica", Font.BOLD, 32);
            ft48=new Font("Helvetica",Font.BOLD,48);
            ft64=new Font("Helvetica",Font.BOLD,64);
            ftexp=new Font("Helvetica", Font.PLAIN, 32);
        }
        setTitle("CalculatorX");
        setBounds(50, 50, 1090, 750);
        setLayout(null); //very useful
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        eqs=new JLabel("=");
        eqs.setFont(ft48);
        eqs.setBounds((int) (getWidth() / 2 - 900 / 2), 100, 900, 100);
        add(eqs);
        res.setFont(ftexp);
        res.setBounds((int) (getWidth() / 2 - 900 / 2), 20, 900, 70);
        res.setEditable(false);
        res.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String[][] a;
                if (superiorHardware) a = new String[][]{{"+", "-", "*", "/", ".", "(", ")", "p", "e", "r", "^", "l", "s", "S", "c", "C", "t", "T"}, {"+", "-", "x", "÷", ".", "(", ")", "π", "e", "√", "^", "ln", "sin", "asin", "cos", "acos", "tan", "atan"}};
                else a = new String[][]{{"+", "-", "*", "/", ".", "(", ")", "p", "e", "r", "^", "l", "s", "S", "c", "C", "t", "T"}, {"+", "-", "x", "/", ".", "(", ")", "pi", "e", "sqrt", "^", "ln", "sin", "asin", "cos", "acos", "tan", "atan"}};
                if (e.getKeyChar() >= '0' && e.getKeyChar() <= '9') exph.add(String.valueOf(e.getKeyChar()));
                for (int i=0;i<a[0].length;i++) if(e.getKeyChar()==a[0][i].charAt(0)) { exph.add(a[1][i]); if (i>=9) exph.add("("); break;}
                if (e.getKeyCode()==KeyEvent.VK_BACK_SPACE&&exph.size()>=1) {
                    if (exph.size()>=2&&exph.get(exph.size()-1)=="("&&funcID.containsKey(exph.get(exph.size()-2))) exph.remove(exph.size()-1);
                    exph.remove(exph.size()-1);
                }
                if (e.getKeyCode()==KeyEvent.VK_ENTER) Eval();
                if (e.getKeyCode()==KeyEvent.VK_A) exph.clear();
                exp="";
                for (int i=0;i<exph.size();i++) exp=exp+exph.get(i);
                res.setText(exp);
            }
        });
        res.grabFocus();
        add(res);
        for (int i=0;i<=8;i++) {
            btnn.add(new JButton(String.valueOf(i+1)));
            btnn.get(i).setFont(ft64);
            btnn.get(i).addActionListener(this);
            btnn.get(i).setBounds(nstx+(i%3)*(szx+spx),(nsty)+(Integer)(i/3)*(szy+spy),szx,szy);
            add(btnn.get(i));
        }
        for (int i=0;i<=11;i++) {
            btnb.add(new JButton(pbtnb.get(i+1).payload));
            btnb.get(i).setFont(pbtnb.get(i+1).isBig?ft64:ft32);
            btnb.get(i).addActionListener(this);
            btnb.get(i).setBounds(ostx+(i%3+2)*(szx+spx),osty+(i/3)*(szy+spy),szx,szy);
            add(btnb.get(i));
        }
        for (int i=0;i<=5;i++) {
            btnf.add(new JButton(pbtnf.get(i+1)));
            btnf.get(i).setFont(ft64);
            btnf.get(i).addActionListener(this);
            btnf.get(i).setBounds(ostx+(i%2)*(szx+spx),osty+(i/2)*(szy+spy),szx,szy);
            add(btnf.get(i));
        }
        btn0 = new JButton("0");
        btn0.setBounds(nstx, nsty + 3 * szy + 3 * spy, 2 * szx + spx, szy);
        btn0.setFont(ft64);
        btn0.addActionListener(this);
        add(btn0);
        btnd = new JButton(".");
        btnd.setBounds(nstx + 2 * szx + 2 * spx, nsty + 3 * szy + 3 * spy, szx, szy);
        btnd.setFont(ft64);
        btnd.addActionListener(this);
        add(btnd);
        btneq = new JButton("=");
        btneq.setBounds(ostx, osty + 3 * spx + 3 * szx, szx, szy);
        btneq.setFont(ft64);
        btneq.addActionListener(this);
        add(btneq);
        btnac = new JButton("AC");
        btnac.setBounds(ostx + spx+ szx, osty + 3 * spx + 3 * szx, szx, szy);
        btnac.setFont(ft32);
        btnac.addActionListener(this);
        add(btnac);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Map<JButton, String> btnRes = new HashMap<>();
        for (int i=0;i<=8;i++) btnRes.put(btnn.get(i),String.valueOf(i+1));
        if (btnRes.containsKey(e.getSource())) exph.add(btnRes.get(e.getSource()));
        Integer whe;
        if ((whe=btnb.indexOf(e.getSource()))>-1) {
            if (whe==9) {
                if (exph.size()>=1) {
                    if (exph.size()>=2&&exph.get(exph.size()-1)=="("&&funcID.containsKey(exph.get(exph.size()-2))) exph.remove(exph.size()-1);
                    exph.remove(exph.size()-1);
                }
            } else {
                exph.add(pbtnb.get(whe + 1).payload);
                if (pbtnb.get(whe + 1).needPren) exph.add("(");
            }
        }
        if ((whe=btnf.indexOf(e.getSource()))>-1) exph.add(pbtnf.get(whe+1));
        if (e.getSource()==btn0) exph.add("0");
        else if (e.getSource()==btnd) exph.add(".");
        else if (e.getSource()==btneq) Eval();
        else if (e.getSource()==btnac) exph.clear();
        exp="";
        for (int i=0;i<exph.size();i++) exp=exp+exph.get(i);
        res.setText(exp);
        res.grabFocus();
    }
    public static void main(String argv[]) {
        new CalculatorX();
    }
}
