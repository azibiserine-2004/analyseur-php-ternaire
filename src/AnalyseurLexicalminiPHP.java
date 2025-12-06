package com.mycompany.analyseurlexicalminiphp;

import java.util.ArrayList;

public class AnalyseurLexicalminiPHP {
 
    // MATRICE D'AUTOMATE FINI DÉTERMINISTE 
    
    static int[][] mat = {
        // $   lettre  chiffre  opérateur  espace  paren  autre
        // 0     1        2         3         4       5     6
        { 1,   -3,      3,       4,        0,      5,   -1  }, // État 0: initial
        {-1,    2,      2,      -2,       -2,     -2,   -2  }, // État 1: après $
        {-1,    2,      2,      -2,       -2,     -2,   -2  }, // État 2: variable
        {-1,   -2,      3,      -2,       -2,     -2,   -2  }, // État 3: nombre
        {-2,   -2,     -2,      -2,       -2,     -2,   -2  }, // État 4: opérateur
        {-2,   -2,     -2,      -2,       -2,     -2,   -2  }  // État 5: parenthèse
    };
    
    
    // FONCTION numColonne() 
   
    public static int numColonne(char c) {
        if (c == '$') return 0;                     // colonne 0: $
        if (Character.isLetter(c) || c == '_') return 1; // colonne 1: lettre
        if (Character.isDigit(c)) return 2;         // colonne 2: chiffre
        if ("+-*%=!<>&|?:;".indexOf(c) != -1) return 3; // colonne 3: opérateur
        if (Character.isWhitespace(c) || c == '#' || c == '\n') return 4; // colonne 4: espace
        if ("(){}".indexOf(c) != -1) return 5;     // colonne 5: parenthèse
        return 6;                                  // colonne 6: autre 
    }

   
    // LISTE DES MOTS-CLÉS
   
    public static boolean estMotCle(String s) {
        return s.equals("if") || s.equals("else") || s.equals("elseif") || 
               s.equals("while") || s.equals("do") || s.equals("for") || 
               s.equals("foreach") || s.equals("switch") || s.equals("case") || 
               s.equals("break") || s.equals("return") || 
               s.equals("echo") || s.equals("function") ||
               s.equals("azibi") || s.equals("serine");
    }
    
    
    public static int calculerLigne(String programme, int position) {
        int ligne = 1;
        for (int i = 0; i < position; i++) {
            if (programme.charAt(i) == '\n') {
                ligne++;
            }
        }
        return ligne;
    }
    
    
    // ANALYSEUR LEXICAL PRINCIPAL 
    public static ArrayList<String> analyser(String programme) {
        programme = programme + "#";
        ArrayList<String> tokens = new ArrayList<>();
        boolean erreurDetectee = false;
        
        int i = 0;
        
        while (programme.charAt(i) != '#') {
            int ec = 0;
            String lexeme = "";
            int debutLexeme = i;
            
            // Ignorer les espaces
            while (programme.charAt(i) != '#' && numColonne(programme.charAt(i)) == 4) {
                i++;
            }
            
            debutLexeme = i;
            if (programme.charAt(i) == '#') break;

            // Gestion des chaînes de caractères
            if (programme.charAt(i) == '"') {
                lexeme += '"';
                i++;
                while (programme.charAt(i) != '#' && programme.charAt(i) != '"') {
                    lexeme += programme.charAt(i);
                    i++;
                }
                if (programme.charAt(i) == '"') {
                    lexeme += '"';
                    i++;
                    tokens.add("CHAINE(" + lexeme + ")");
                } else {
                    System.out.println("ERREUR ligne " + calculerLigne(programme, debutLexeme) + " : Chaine non terminee");
                    erreurDetectee = true;
                }
                continue;
            }
            
            // ALGORITHME DE L'AUTOMATE 
            while (programme.charAt(i) != '#') {
                int colonne = numColonne(programme.charAt(i));
                
                // Cas spécial: vérifier si c'est un mot-clé
                if (mat[ec][colonne] == -3) { 
                    while (programme.charAt(i) != '#' && 
                           (numColonne(programme.charAt(i)) == 1 || numColonne(programme.charAt(i)) == 2)) {
                        lexeme += programme.charAt(i);
                        i++;
                    }
                    
                    if (estMotCle(lexeme)) {
                        tokens.add("MOT_CLE(" + lexeme + ")");
                    } else {
                        int ligne = calculerLigne(programme, debutLexeme);
                        System.out.println("ERREUR ligne " + ligne + " : La variable '" + lexeme + "' doit commencer par $");
                        erreurDetectee = true;
                    }
                    ec = -2;
                    break;
                }
                
                if (mat[ec][colonne] < 0) break;
                
                ec = mat[ec][colonne];
                lexeme += programme.charAt(i);
                i++;
            }
            
            // Création du token
            if (ec != 0 && ec != -2) {
                String tok = lexeme.trim();
                
                if (!tok.isEmpty()) {
                    if (tok.startsWith("$")) {
                        tokens.add("VARIABLE(" + tok + ")");
                    } 
                    else if (Character.isDigit(tok.charAt(0))) {
                        tokens.add("NOMBRE(" + tok + ")");
                    } 
                    else if ("+-*/%=!<>&|:;?".contains(tok)) {
                        char nextChar = programme.charAt(i);
                        String combined = tok + nextChar;
                        
                        if (combined.equals("++")) {
                            tokens.add("INCR");
                            i++;
                        } 
                        else if (combined.equals("--")) {
                            tokens.add("DECR");
                            i++; 
                        }
                        else if (combined.equals("==") || combined.equals("!=") || 
                                 combined.equals("<=") || combined.equals(">=") || 
                                 combined.equals("&&") || combined.equals("||")) {
                            
                            if (combined.equals("&&")) tokens.add("ET_LOGIQUE");
                            else if (combined.equals("||")) tokens.add("OU_LOGIQUE");
                            else tokens.add("OPERATEUR(" + combined + ")");
                            i++;
                        }
                        else if (tok.equals("?")) tokens.add("TERNAIRE_SI");
                        else if (tok.equals(":")) tokens.add("TERNAIRE_SINON");
                        else if (tok.equals(";")) tokens.add("SEMICOLON");
                        else tokens.add("OPERATEUR(" + tok + ")");
                    } 
                    else if (tok.equals("(")) tokens.add("LPAREN");
                    else if (tok.equals(")")) tokens.add("RPAREN");
                    else if (tok.equals("{")) tokens.add("LBRACE");
                    else if (tok.equals("}")) tokens.add("RBRACE");
                    else if (estMotCle(tok)) tokens.add("MOT_CLE(" + tok + ")");
                    else tokens.add("IDENTIFIANT(" + tok + ")");
                }
            } 
            else if (ec == 0 && programme.charAt(i) != '#') {
                char charErr = programme.charAt(i);
                int ligne = calculerLigne(programme, i);
                System.out.println("ERREUR ligne " + ligne + " : Caractere non reconnu '" + charErr + "'");
                erreurDetectee = true;
                i++;
            }
        }
        
        if (erreurDetectee) {
            System.out.println("\nAnalyse lexicale terminee avec des erreurs");
        } else {
            System.out.println("\nAnalyse lexicale reussie ! Le programme est lexicalement correct.");
        }
        
        return tokens;
    }
}