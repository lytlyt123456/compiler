public class ArrayType extends Type {
    private int num;
    private Type elem;

    public ArrayType(int num, Type elem) {
        this.num = num;
        this.elem = elem;
        this.type = 'a';
        this.width = num * elem.getWidth();
    }

    public int getNum() { return num; }

    public Type getElem() { return elem; }
}
