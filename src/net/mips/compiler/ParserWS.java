package net.mips.compiler;

import net.mips.interpreter.Instruction;
import net.mips.interpreter.Mnemonique;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ParserWS extends Parser {


    private List<Instruction> pcode = new ArrayList<>();

    private PrintWriter fluxCible;



    //    private ScannerWS scanner ;
    public ParserWS(String filename) throws FileNotFoundException {
        super(filename);
        this.pcode = new ArrayList<>();
    }

    public static void main(String[] args) throws IOException, ErreurSyntaxique, ErreurSemantique {
        ParserWS sc = new ParserWS("C:\\Users\\user\\Documents\\EMSI\\4IIR\\S3\\compilation 2\\TP3\\Compilation\\src\\net\\mips\\compiler\\sc.txt");
        sc.scanner.initMotsCles();
        sc.scanner.lireCar();

        sc.scanner.symSuivant();
        sc.Program();


    }

    public void test_Insere(Tokens t, ClasseIdf cdi, CodesErr err) throws IOException, ErreurSemantique {
        if (this.scanner.getSymbCour().getToken().equals(t)) {
            this.scanner.cherche_Symb();
            if (this.scanner.getPlaceSymb() != -1)
                throw new ErreurSemantique(CodesErr.DOUBLEDECLARATION_ERR.getMessage());
            this.scanner.entrerSymb(cdi);
            this.getScanner().symSuivant();
        } else {
            throw new ErreurSemantique(err.getMessage());
        }
    }

    public void test_Cherche(Tokens t, CodesErr err) throws IOException, ErreurSemantique {
        if (this.scanner.getSymbCour().getToken().equals(t)) {
            this.scanner.cherche_Symb();
            if (this.scanner.getPlaceSymb() == -1) {
                throw new ErreurSemantique(CodesErr.IDNOTFOUND_ERR.getMessage());
            } else if (this.scanner.getTableSymb().get(this.scanner.getPlaceSymb()).getClasseIdf() == ClasseIdf.PROGRAMME) {
                throw new ErreurSemantique(CodesErr.PROGRAMMEID_ERR.getMessage());
            }
            this.getScanner().symSuivant();
        } else {
            throw new ErreurSemantique(err.getMessage());
        }
    }
    public void generer1(Mnemonique m) {
        Instruction ins = new Instruction();
        ins.setMne(m);
        this.pcode.add(ins);
    }
    public void generer2(Mnemonique m, int i) {
        Instruction ins = new Instruction(m,i);
        this.pcode.add(ins);
    }
    public void savepcode() throws IOException{
        this.fluxCible = new PrintWriter("pcode.pp");
        for(Instruction i :this.pcode) {
            if(i.getMne() == Mnemonique.INT || i.getMne() == Mnemonique.LDA || i.getMne() == Mnemonique.LDI || i.getMne() == Mnemonique.BZE || i.getMne() == Mnemonique.BRN ){
                this.fluxCible.println(i.getMne()+"\t"+i.getSuite());
            }
            else {
                this.fluxCible.print(i.getMne());
            }
        }
        this.fluxCible.close();
    }
    @Override
    public void Program() throws IOException, ErreurSyntaxique, ErreurSemantique {
        testAccept(Tokens.PROGRAM_TOKEN, CodesErr.PROGRAM_ERR);
        test_Insere(Tokens.ID_TOKEN, ClasseIdf.PROGRAMME, CodesErr.ID_ERR);
        testAccept(Tokens.PVIR_TOKEN, CodesErr.PVIR_ERR);
        Block();
        this.generer1(Mnemonique.HLT);
        testAccept(Tokens.PNT_TOKEN, CodesErr.PNT_ERR);
    }

    @Override
    public void Consts() throws IOException, ErreurSyntaxique, ErreurSemantique {
        if ((this.scanner.getSymbCour().getToken().equals(Tokens.CONST_TOKEN))) {
            testAccept(Tokens.CONST_TOKEN, CodesErr.CONST_ERR);
            while (this.scanner.getSymbCour().getToken().equals(Tokens.ID_TOKEN)) {
                generer2(Mnemonique.LDA,this.scanner.getSymbCour().getAdresse());
                test_Insere(Tokens.ID_TOKEN, ClasseIdf.CONSTANTE, CodesErr.CONST_ERR);
                testAccept(Tokens.AFFEC_TOKEN, CodesErr.AFFEC_ERR);
                generer2(Mnemonique.LDI, Integer.parseInt(this.scanner.getSymbCour().getNom()));
                testAccept(Tokens.NUM_TOKEN, CodesErr.NUM_ERR);
                testAccept(Tokens.PVIR_TOKEN, CodesErr.PVIR_ERR);
            }

        }
    }

    @Override
    public void Vars() throws IOException, ErreurSyntaxique, ErreurSemantique {
        if ((this.scanner.getSymbCour().getToken().equals(Tokens.VAR_TOKEN))) {
            testAccept(Tokens.VAR_TOKEN, CodesErr.VAR_ERR);
            generer2(Mnemonique.LDA,this.scanner.getSymbCour().getAdresse());
            this.test_Insere(Tokens.ID_TOKEN, ClasseIdf.VARIALE, CodesErr.VAR_ERR);
            while (this.scanner.getSymbCour().getToken().equals(Tokens.VIR_TOKEN)) {
                testAccept(Tokens.VIR_TOKEN, CodesErr.VIR_ERR);
                generer2(Mnemonique.LDA,this.scanner.getSymbCour().getAdresse());
                this.test_Insere(Tokens.ID_TOKEN, ClasseIdf.VARIALE, CodesErr.VAR_ERR);
            }
            testAccept(Tokens.PVIR_TOKEN, CodesErr.PVIR_ERR);
        }
    }

    @Override
    public void AFFECT() throws IOException, ErreurSyntaxique, ErreurSemantique {
        this.scanner.cherche_Symb();
        if (this.scanner.getPlaceSymb() != -1 && this.scanner.getTableSymb().get(this.scanner.getPlaceSymb()).getClasseIdf() == ClasseIdf.CONSTANTE) {
            throw new ErreurSemantique(CodesErr.CONST_ERR.getMessage());
        }
        generer2(Mnemonique.LDA,this.scanner.getSymbCour().getAdresse());
        generer1(Mnemonique.LDV);
        test_Cherche(Tokens.ID_TOKEN, CodesErr.ID_ERR);
        testAccept(Tokens.AFFEC_TOKEN, CodesErr.AFFEC_ERR);
        EXPR();
    }

    @Override
    public void Block() throws IOException, ErreurSyntaxique, ErreurSemantique {
        Consts();
        Vars();
        this.generer2(Mnemonique.INT,this.scanner.getOffset());
        Insts();
    }

    @Override
    public void Lire() throws IOException, ErreurSyntaxique, ErreurSemantique {
        testAccept(Tokens.READ_TOKEN, CodesErr.READ_ERR);
        testAccept(Tokens.PARG_TOKEN, CodesErr.PARG_ERR);
        this.scanner.cherche_Symb();
        if (this.scanner.getPlaceSymb() != -1 && this.scanner.getTableSymb().get(this.scanner.getPlaceSymb()).getClasseIdf() == ClasseIdf.CONSTANTE) {
            throw new ErreurSemantique(CodesErr.CONST_ERR.getMessage());
        }
        generer2(Mnemonique.LDA,this.scanner.getSymbCour().getAdresse());
        generer1(Mnemonique.LDV);
        generer1(Mnemonique.INN);
        test_Cherche(Tokens.ID_TOKEN, CodesErr.ID_ERR);

        while (this.scanner.getSymbCour().getToken().equals(Tokens.VIR_TOKEN)) {
            testAccept(Tokens.VIR_TOKEN, CodesErr.VIR_ERR);
            generer2(Mnemonique.LDA,this.scanner.getSymbCour().getAdresse());
            generer1(Mnemonique.LDV);
            generer1(Mnemonique.INN);
            test_Cherche(Tokens.ID_TOKEN, CodesErr.ID_ERR);
        }
        testAccept(Tokens.PARD_TOKEN, CodesErr.PARD_ERR);
    }

    @Override
    public void EXPR() throws IOException, ErreurSyntaxique, ErreurSemantique {
        Term();
        while (this.scanner.getSymbCour().getToken().equals(Tokens.PLUS_TOKEN) || this.scanner.getSymbCour().getToken().equals(Tokens.MOINS_TOKEN)) {
            Addop();
            Term();
        }
    }

    @Override
    public void Fact() throws IOException, ErreurSyntaxique, ErreurSemantique {
        switch (this.scanner.getSymbCour().getToken()) {
            case ID_TOKEN:
                test_Cherche(Tokens.ID_TOKEN, CodesErr.ID_ERR);
                break;
            case NUM_TOKEN:
                testAccept(Tokens.NUM_TOKEN, CodesErr.NUM_ERR);
                break;
            case PARG_TOKEN:
                testAccept(Tokens.PARG_TOKEN, CodesErr.PARG_ERR);
                EXPR();
                testAccept(Tokens.PARD_TOKEN, CodesErr.PARD_ERR);
                break;
            default:
                Erreur(CodesErr.DEFAULT_ERR);
        }


    }

    public List<Instruction> getPcode() {
        return pcode;
    }

    public void setPcode(List<Instruction> pcode) {
        this.pcode = pcode;
    }

    public PrintWriter getFluxCible() {
        return fluxCible;
    }

    public void setFluxCible(PrintWriter fluxCible) {
        this.fluxCible = fluxCible;
    }

}