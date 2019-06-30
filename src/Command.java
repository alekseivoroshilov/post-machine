public class Command {
    Type type;
    int addr1;
    int addr2;

    public Command (Type type, int addr1, int addr2){
        this.type = type;
        this.addr1 = addr1;
        this.addr2 = addr2;
    }

    public Type getType() {
        return type;
    }

    public int getAddr1() {
        return addr1;
    }

    public int getAddr2() {
        return addr2;
    }
}