package com.mycompany.analyseurlexicalminiphp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class AnalyseurSyntaxiqueMiniPHP {

    static ArrayList<String> tokens;
    static int i;
    static String tc;
    static boolean r = true;

    static void prochainToken() {
        i++;
        if (i < tokens.size()) {
            tc = tokens.get(i);
        } else {
            tc = "#";
        }
    }

    static void erreur(String message) {
        System.err.println("ERREUR SYNTAXIQUE (Index " + i + ") : " + message + " (Trouve : " + tc + ")");
        r = false;
    }
    
    static boolean estOpComparaison() {
        return tc.startsWith("OPERATEUR(<)") || tc.startsWith("OPERATEUR(>)") || 
               tc.startsWith("OPERATEUR(!)") || tc.equals("OPERATEUR(==)") ||
               tc.equals("OPERATEUR(!=)") || tc.equals("OPERATEUR(<=)") ||
               tc.equals("OPERATEUR(>=)");
    }

    static boolean estOpArithmetique() {
        return tc.equals("OPERATEUR(+)") || tc.equals("OPERATEUR(-)") || 
               tc.equals("OPERATEUR(*)") || tc.equals("OPERATEUR(/)") ||
               tc.equals("OPERATEUR(%)");
    }

    static boolean estOpLogique() {
        return tc.equals("ET_LOGIQUE") || tc.equals("OU_LOGIQUE");
    }

    static void ignorerJusquAInstruction() {
        while (!tc.equals("#") && 
               !tc.startsWith("VARIABLE(") &&
               !tc.startsWith("MOT_CLE")) {
            prochainToken();
        }
    }
    
    static void ignorerInstruction() {
        prochainToken();

        if (tc.equals("LPAREN")) {
            int niveauParen = 1;
            prochainToken();
            while (niveauParen > 0 && !tc.equals("#")) {
                if (tc.equals("LPAREN")) niveauParen++;
                else if (tc.equals("RPAREN")) niveauParen--;
                prochainToken();
            }
        }

        if (tc.equals("LBRACE")) {
            int niveauAccolade = 1;
            prochainToken();
            while (niveauAccolade > 0 && !tc.equals("#")) {
                if (tc.equals("LBRACE")) niveauAccolade++;
                else if (tc.equals("RBRACE")) niveauAccolade--;
                prochainToken();
            }
        }
    }

    public static void Z() {
        P();
        
        if (tc.equals("#") && i == tokens.size() && r) {
            System.out.println("Analyse syntaxique reussie ! Le programme est syntaxiquement correct.");
        } else {
            System.err.println("Analyse syntaxique echouee.");
        }
    }

    static void P() {
        while (!tc.equals("#")) {
            I();
        }
    }

    static void I() {
        if (tc.startsWith("VARIABLE(")) {
            AFF();
        } else if (tc.startsWith("MOT_CLE")) {
            if (tc.equals("MOT_CLE(while)") || 
                tc.equals("MOT_CLE(if)") || 
                tc.equals("MOT_CLE(for)") ||
                tc.equals("MOT_CLE(switch)") ||
                tc.equals("MOT_CLE(do)")) {
                ignorerInstruction();
            } else {
                prochainToken();
                while (!tc.equals("SEMICOLON") && !tc.equals("#")) {
                    prochainToken();
                }
                if (tc.equals("SEMICOLON")) {
                    prochainToken();
                }
            }
        } else if (!tc.equals("#")) {
            erreur("Debut d'instruction non valide");
            ignorerJusquAInstruction();
        }
    }

    static void AFF() {
        if (tc.startsWith("VARIABLE(")) {
            prochainToken();
        } else {
            erreur("Variable ($var) attendue");
            return;
        }

        if (tc.equals("OPERATEUR(=)")) {
            prochainToken();
        } else {
            erreur("'=' attendu");
            ignorerJusquAInstruction();
            return;
        }

        EXP();

        if (tc.equals("SEMICOLON")) {
            prochainToken();
        } else {
            erreur("';' attendu a la fin de l'affectation");
            ignorerJusquAInstruction();
        }
    }

    static void EXP() {
        OP_TERNAIRE();
    }

    static void OP_TERNAIRE() {
        LOGIQUE();

        if (tc.equals("TERNAIRE_SI")) {
            prochainToken();
            LOGIQUE();
            
            if (tc.equals("TERNAIRE_SINON")) {
                prochainToken();
                LOGIQUE();
            } else {
                erreur("':' attendu dans l'operateur ternaire");
            }
        }
    }
    
    static void LOGIQUE() {
        COMP();

        while (estOpLogique()) {
            prochainToken();
            COMP();
        }
    }
    
    static void COMP() {
        ARITH();
        
        if (estOpComparaison()) {
            prochainToken();
            ARITH();
        }
    }

    static void ARITH() {
        INCR_DECR();

        while (estOpArithmetique()) {
            prochainToken();
            INCR_DECR();
        }
    }

    static void INCR_DECR() {
        if (tc.equals("INCR") || tc.equals("DECR")) {
            prochainToken();
            TERME();
        } else {
            TERME();
            if (tc.equals("INCR") || tc.equals("DECR")) {
                prochainToken();
            }
        }
    }

    static void TERME() {
        if (tc.startsWith("NOMBRE(") || 
            tc.startsWith("VARIABLE(") ||
            tc.startsWith("CHAINE(")) {
            prochainToken();
        } else if (tc.equals("LPAREN")) {
            prochainToken();
            EXP();
            if (tc.equals("RPAREN")) {
                prochainToken();
            } else {
                erreur("')' attendu");
            }
        } else {
            erreur("Terme attendu (nombre, variable, chaine ou expression)");
            prochainToken();
        }
    }
    
    public static void main(String[] args) {
        String cheminFichier = "";
        String code = "";
        
        if (args.length == 0) {
            System.err.println("\nERREUR : Aucun fichier specifie.");
            System.err.println("Utilisation : java AnalyseurSyntaxiqueMiniPHP <nom_fichier.txt>");
            System.err.println("Exemple : java AnalyseurSyntaxiqueMiniPHP test.txt");
            return;
        }
        
        cheminFichier = args[0];
        System.out.println("Fichier a analyser : " + cheminFichier);
        System.out.println("========================================");

        try {
            Scanner fileScanner = new Scanner(new File(cheminFichier));
            while (fileScanner.hasNextLine()) {
                code += fileScanner.nextLine() + "\n";
            }
            fileScanner.close();
            
            if (!code.trim().endsWith("#")) {
                code += " #";
            }
        } catch (FileNotFoundException e) {
            System.err.println("\nERREUR FATALE : Le fichier '" + cheminFichier + "' n'existe pas.");
            System.err.println("Verifiez le chemin et le nom du fichier.");
            return;
        }
        
        System.out.println("\nCODE SOURCE LU :");
        System.out.println("----------------------------------------");
        System.out.println(code.trim());
        System.out.println("----------------------------------------");

        System.out.println("\nETAPE 1 : ANALYSE LEXICALE");
        System.out.println("========================================");
        tokens = AnalyseurLexicalminiPHP.analyser(code);
        
        System.out.println("\nTOKENS GENERES :");
        System.out.println("----------------------------------------");
        for (int idx = 0; idx < tokens.size(); idx++) {
            System.out.println("[" + idx + "] " + tokens.get(idx));
        }
        System.out.println("----------------------------------------");

        System.out.println("\nETAPE 2 : ANALYSE SYNTAXIQUE");
        System.out.println("========================================");
        if (!tokens.isEmpty()) {
            i = 0;
            tc = tokens.get(0);
            r = true;
            Z();
        } else {
            System.err.println("Aucun token a analyser (fichier vide ou erreurs lexicales graves).");
        }
        
        System.out.println("\n========================================");
        System.out.println("FIN DE L'ANALYSE");
        System.out.println("========================================\n");
    }
}