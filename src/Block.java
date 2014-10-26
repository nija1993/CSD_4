
public class Block {
	State state;
	int mem_index;
	long value;
	public Block(int mem, long value){
		state = State.INVALID;
		mem_index = mem;
		this.value = value;
	}
	
}
