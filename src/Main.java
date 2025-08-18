import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        Scanner input = new Scanner(System.in);
        System.out.println("请输入源代码文件路径: ");
        String sourceFilePath = input.nextLine();
        System.out.println("请输入保存中间代码的文件路径: ");
        String desFilePath = input.nextLine();

        StringBuilder sourceCode = new StringBuilder();
        Scanner fileReader = new Scanner(new File(sourceFilePath));
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
        IROptimizer optimizer = new IROptimizer(code);
        List<IR> code2 = optimizer.getCode();

        PrintWriter fileWriter = new PrintWriter(new File(desFilePath));
        for(IR ir : code2)
            fileWriter.println(ir.toString());
        fileWriter.println(code2.size() + ":");
        fileWriter.close();
    }
}
