public class Token {
    private int attribute;
    private String value;
    private int line;

    //属性值
    public static int INT = 'i', REAL = 'r', ID = 'd',
            CONST_INT = '0',    CONST_REAL = '.',
            OR = '|', AND = '&', NOT = '!',
            TRUE = 't', FALSE = 'f',
            RELOP = '>',
            IF = 'I', ELSE = 'E', THEN = 'T',
            WHILE = 'W', DO = 'D', FOR = 'F',
            BREAK = 'B', CONTINUE = 'C';

    public Token(int attribute, String value, int line) {
        this.attribute = attribute;
        this.value = value;
        this.line = line;
    }

    public int getAttribute() { return attribute; }

    public String getValue() { return value; }

    public int getLine() {return line;}
}
