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

        String[] sourceCodes = sourceCode.split("\n");

        int line = 0;
        for (int i = 0; i < sourceCodes.length; i++) {
            ++line;
            if (sourceCodes[i].length() == 0)
                continue;
            String[] singleLineCodes = sourceCodes[i].split("[ \t]");
            for (int j = 0; j < singleLineCodes.length; ++j) {
                String target = singleLineCodes[j];
                if (target.length() == 0)
                    continue;
                int k = 0;
                while (k < target.length()) {
                    if (target.charAt(k) == ';' || target.charAt(k) == '('
                            || target.charAt(k) == ')' || target.charAt(k) == '['
                            || target.charAt(k) == ']' || target.charAt(k) == '{'
                            || target.charAt(k) == '}' || target.charAt(k) == '+'
                            || target.charAt(k) == '-' || target.charAt(k) == '*'
                            || target.charAt(k) == '/') {
                        tokens.add(new Token(target.charAt(k), "", line));
                        ++k;
                    }
                    else if (target.charAt(k) == '=') {
                        if (k + 1 < target.length() && target.charAt(k + 1) == '=') {
                            tokens.add(new Token(Token.RELOP, "==", line));
                            k += 2;
                        }
                        else {
                            tokens.add(new Token('=', "", line));
                            ++k;
                        }
                    }
                    else if (target.charAt(k) == '!') {
                        if (k + 1 < target.length() && target.charAt(k + 1) == '=') {
                            tokens.add(new Token(Token.RELOP, "!=", line));
                            k += 2;
                        }
                        else {
                            tokens.add(new Token('!', "", line));
                            k++;
                        }
                    }
                    else if (target.charAt(k) == '>') {
                        if (k + 1 < target.length() && target.charAt(k + 1) == '=') {
                            tokens.add(new Token(Token.RELOP, ">=", line));
                            k += 2;
                        }
                        else {
                            tokens.add(new Token(Token.RELOP, ">", line));
                            k++;
                        }
                    }
                    else if (target.charAt(k) == '<') {
                        if (k + 1 < target.length() && target.charAt(k + 1) == '=') {
                            tokens.add(new Token(Token.RELOP, "<=", line));
                            k += 2;
                        }
                        else {
                            tokens.add(new Token(Token.RELOP, "<", line));
                            k++;
                        }
                    }
                    else if (target.charAt(k) == '&') {
                        if (k + 1 < target.length() && target.charAt(k + 1) == '&') {
                            tokens.add(new Token('&', "", line));
                            k += 2;
                        }
                        else throw new LexicalError("\"&&\" is not matched in line" + line);
                    }
                    else if (target.charAt(k) == '|') {
                        if (k + 1 < target.length() && target.charAt(k + 1) == '|') {
                            tokens.add(new Token('|', "", line));
                            k += 2;
                        }
                        else throw new LexicalError("\"||\" is not matched in line" + line);
                    }
                    else if (target.charAt(k) >= '0' && target.charAt(k) <= '9') {
                        boolean isInteger = true;
                        StringBuilder num = new StringBuilder();
                        num.append(target.charAt(k));
                        int k1 = k + 1;
                        while (k1 < target.length() && target.charAt(k1) >= '0' && target.charAt(k1) <= '9') {
                            num.append(target.charAt(k1));
                            ++k1;
                        }
                        if (k1 < target.length() && target.charAt(k1) == '.') {
                            num.append('.');
                            isInteger = false;
                            ++k1;
                        }
                        while (k1 < target.length() && target.charAt(k1) >= '0' && target.charAt(k1) <= '9') {
                            num.append(target.charAt(k1));
                            ++k1;
                        }
                        if (isInteger)
                            tokens.add(new Token(Token.CONST_INT, num.toString(), line));
                        else tokens.add(new Token(Token.CONST_REAL, num.toString(), line));
                        k = k1;
                    }
                    else if (target.charAt(k) >= 'a' && target.charAt(k) <= 'z'
                            || target.charAt(k) >= 'A' && target.charAt(k) <= 'Z'
                            || target.charAt(k) == '_') {
                        StringBuilder word = new StringBuilder();
                        word.append(target.charAt(k));
                        int k1 = k + 1;
                        while (k1 < target.length() &&
                                (target.charAt(k1) >= 'a' && target.charAt(k1) <= 'z'
                                || target.charAt(k1) >= 'A' && target.charAt(k1) <= 'Z'
                                || target.charAt(k1) >= '0' && target.charAt(k1) <= '9'
                                || target.charAt(k1) == '_')) {
                            word.append(target.charAt(k1));
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
            }
        }

        tokens.add(new Token(-100, "", -100)); // $
    }

    public List<Token> getTokens() { return tokens; }
}
