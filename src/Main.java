import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        StringBuilder sourceCode = new StringBuilder();
        Scanner fileReader = new Scanner(new File("src\\test_source_code.txt"));
        while (fileReader.hasNext())
            sourceCode.append(fileReader.nextLine()).append('\n');
        fileReader.close();

        /* *
         * System.out.println(sourceCode.toString());
         * System.out.println("--------------------");
         */

        Lexer lexer = new Lexer(sourceCode.toString());
        List<Token> tokens = lexer.getTokens();

        /* *
         * for (Token token : tokens)
         *     System.out.println("" + (char)token.getAttribute() + " " + token.getValue() + " " + token.getLine());
         * System.out.println("--------------------");
         */

        Parser parser = new Parser(tokens);
        List<IR> code = parser.getCode();

        PrintWriter fileWriter = new PrintWriter(new File("src\\intermediate_representation.txt"));
        for(IR ir : code)
            fileWriter.println(ir.toString());
        fileWriter.println(code.size() + ":");
        fileWriter.close();
    }
}
