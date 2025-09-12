import java.util.*;

public class Lexer {
    private List<Token> tokens;

    public Lexer(String sourceCode) {
        tokens = new ArrayList<>();

        int n = sourceCode.length();
        for (int i = 0; i < n - 2; i++) {
            if (sourceCode.charAt(i) == '/' && sourceCode.charAt(i + 1) == '/') {
                int j = i + 2;
                for (; j <= n - 1; j++)
                    if (sourceCode.charAt(j) == '\n')
                        break;
                // 0 ~ i-1 & j ~ n-1
                if (j >= n - 1) {
                    sourceCode = sourceCode.substring(0, i);
                    break;
                }
                else {
                    sourceCode = sourceCode.substring(0, i) + sourceCode.substring(j, n);
                    n = sourceCode.length();
                }
            }
            else if (sourceCode.charAt(i) == '/' && sourceCode.charAt(i + 1) == '*') {
                int cnt_endl = 0;
                int j = i + 2;
                for (; j <= n - 2; j++) {
                    if (sourceCode.charAt(j) == '\n')
                        ++cnt_endl;
                    else if (sourceCode.charAt(j) == '*' && sourceCode.charAt(j + 1) == '/')
                        break;
                }
                // 0 ~ i-1  &  cnt_endl * '\n'  &  j+2 ~ n-1
                if (j > n - 2)
                    throw new LexicalError("Multi-line annotation is not closed.");
                if (j == n - 2) {
                    sourceCode = sourceCode.substring(0, i);
                    break;
                }
                else {
                    StringBuilder endls = new StringBuilder();
                    for (int k = 0; k < cnt_endl; k++)
                        endls.append('\n');
                    sourceCode = sourceCode.substring(0, i) + endls.toString() + sourceCode.substring(j + 2, n);
                    n = sourceCode.length();
                    i = i + cnt_endl - 1;
                }
            }
        }

        int line = 1;
        int k = 0;
        while (k < sourceCode.length()) {
            if (sourceCode.charAt(k) == ' ' || sourceCode.charAt(k) == '\t'
                    || sourceCode.charAt(k) == '\r') {
                ++k;
                continue;
            }
            else if (sourceCode.charAt(k) == '\n') {
                ++k;
                ++line;
                continue;
            }
            else if (sourceCode.charAt(k) == ';' || sourceCode.charAt(k) == '('
                    || sourceCode.charAt(k) == ')' || sourceCode.charAt(k) == '['
                    || sourceCode.charAt(k) == ']' || sourceCode.charAt(k) == '{'
                    || sourceCode.charAt(k) == '}' || sourceCode.charAt(k) == '+'
                    || sourceCode.charAt(k) == '-' || sourceCode.charAt(k) == '*'
                    || sourceCode.charAt(k) == '/') {
                tokens.add(new Token(sourceCode.charAt(k), "", line));
                ++k;
            }
            else if (sourceCode.charAt(k) == '=') {
                if (k + 1 < sourceCode.length() && sourceCode.charAt(k + 1) == '=') {
                    tokens.add(new Token(Token.RELOP, "==", line));
                    k += 2;
                }
                else {
                    tokens.add(new Token('=', "", line));
                    ++k;
                }
            }
            else if (sourceCode.charAt(k) == '!') {
                if (k + 1 < sourceCode.length() && sourceCode.charAt(k + 1) == '=') {
                    tokens.add(new Token(Token.RELOP, "!=", line));
                    k += 2;
                }
                else {
                    tokens.add(new Token('!', "", line));
                    k++;
                }
            }
            else if (sourceCode.charAt(k) == '>') {
                if (k + 1 < sourceCode.length() && sourceCode.charAt(k + 1) == '=') {
                    tokens.add(new Token(Token.RELOP, ">=", line));
                    k += 2;
                }
                else {
                    tokens.add(new Token(Token.RELOP, ">", line));
                    k++;
                }
            }
            else if (sourceCode.charAt(k) == '<') {
                if (k + 1 < sourceCode.length() && sourceCode.charAt(k + 1) == '=') {
                    tokens.add(new Token(Token.RELOP, "<=", line));
                    k += 2;
                }
                else {
                    tokens.add(new Token(Token.RELOP, "<", line));
                    k++;
                }
            }
            else if (sourceCode.charAt(k) == '&') {
                if (k + 1 < sourceCode.length() && sourceCode.charAt(k + 1) == '&') {
                    tokens.add(new Token('&', "", line));
                    k += 2;
                }
                else throw new LexicalError("\"&&\" is not matched in line" + line);
            }
            else if (sourceCode.charAt(k) == '|') {
                if (k + 1 < sourceCode.length() && sourceCode.charAt(k + 1) == '|') {
                    tokens.add(new Token('|', "", line));
                    k += 2;
                }
                else throw new LexicalError("\"||\" is not matched in line" + line);
            }
            else if (sourceCode.charAt(k) >= '0' && sourceCode.charAt(k) <= '9') {
                boolean isInteger = true;
                StringBuilder num = new StringBuilder();
                num.append(sourceCode.charAt(k));
                int k1 = k + 1;
                while (k1 < sourceCode.length() && sourceCode.charAt(k1) >= '0' && sourceCode.charAt(k1) <= '9') {
                    num.append(sourceCode.charAt(k1));
                    ++k1;
                }
                if (k1 < sourceCode.length() && sourceCode.charAt(k1) == '.') {
                    num.append('.');
                    isInteger = false;
                    ++k1;
                }
                while (k1 < sourceCode.length() && sourceCode.charAt(k1) >= '0' && sourceCode.charAt(k1) <= '9') {
                    num.append(sourceCode.charAt(k1));
                    ++k1;
                }
                if (isInteger)
                    tokens.add(new Token(Token.CONST_INT, num.toString(), line));
                else tokens.add(new Token(Token.CONST_REAL, num.toString(), line));
                k = k1;
            }
            else if (sourceCode.charAt(k) >= 'a' && sourceCode.charAt(k) <= 'z'
                    || sourceCode.charAt(k) >= 'A' && sourceCode.charAt(k) <= 'Z'
                    || sourceCode.charAt(k) == '_') {
                StringBuilder word = new StringBuilder();
                word.append(sourceCode.charAt(k));
                int k1 = k + 1;
                while (k1 < sourceCode.length() &&
                        (sourceCode.charAt(k1) >= 'a' && sourceCode.charAt(k1) <= 'z'
                                || sourceCode.charAt(k1) >= 'A' && sourceCode.charAt(k1) <= 'Z'
                                || sourceCode.charAt(k1) >= '0' && sourceCode.charAt(k1) <= '9'
                                || sourceCode.charAt(k1) == '_')) {
                    word.append(sourceCode.charAt(k1));
                    ++k1;
                }
                String s = word.toString();
                switch (s) {
                    case "int" -> tokens.add(new Token('i', "", line));
                    case "real" -> tokens.add(new Token('r', "", line));
                    case "if" -> tokens.add(new Token(Token.IF, "", line));
                    case "then" -> tokens.add(new Token(Token.THEN, "", line));
                    case "else" -> tokens.add(new Token(Token.ELSE, "", line));
                    case "while" -> tokens.add(new Token(Token.WHILE, "", line));
                    case "for" -> tokens.add(new Token(Token.FOR, "", line));
                    case "do" -> tokens.add(new Token(Token.DO, "", line));
                    case "true" -> tokens.add(new Token(Token.TRUE, "", line));
                    case "false" -> tokens.add(new Token(Token.FALSE, "", line));
                    case "break" -> tokens.add(new Token(Token.BREAK, "", line));
                    case "continue" -> tokens.add(new Token(Token.CONTINUE, "", line));
                    default -> tokens.add(new Token('d', s, line));
                }
                k = k1;
            }
            else throw new LexicalError("Lexical error in line " + line + ".");
        }

        tokens.add(new Token(-100, "", -100)); // $
    }

    public List<Token> getTokens() { return tokens; }
}
