
public class Bus {
	int bus_operations = 0;
	long bus_operation(Request request, int mem_index, long proc_index){
		switch(request){
		case RFO:		// When modifying in curr processor, 
							// to invalidate given memory address in other caches
			Main.proc[(int) (1-proc_index)].invalidate(mem_index);
			bus_operations++;
			break;
		case MEM_W:
			Main.memory.memory_operations++;
		//	System.out.println("writing to memory");
			Main.memory.values[mem_index] = proc_index;
			break;
		case MEM_R:
			Main.memory.memory_operations++;
		//	System.out.println("reading from memory");
			return Main.memory.values[mem_index];
		case CHECK_SHARE:	// check if the block is present in other processor's cache /s
			bus_operations++;
			int x = (int) Main.proc[(int) (1-proc_index)].share(mem_index); 
			if(x != -1) return 1;
		}
		return -1;
	}
	
	// This function gets a block from the other processor if it is modified or owned
	Block get_block_moesi(int mem_index, long proc_index){
		bus_operations++;
		int other_proc = (int)(1-proc_index);
		int index = Main.proc[other_proc].search(mem_index);
		Block bloc = null;
		if(index != -1)
			bloc = Main.proc[other_proc].cache.get(index);
		else{
			bus_operations--;
			return null;
		}
		if(bloc.state == State.MODIFIED || bloc.state == State.OWNED){
			Main.proc[other_proc].get_ownership(index);
			bloc = new Block(mem_index, bloc.value);
			bloc.state = State.SHARED;
			return bloc;
		}
		bus_operations--;
		return null;
	}
	
	void send_block_moesi(int mem_index, long proc_index, long value) {
		bus_operations++;
		int other_proc = (int)(1-proc_index);
		int index = Main.proc[other_proc].search(mem_index);
		if(index != -1 && (Main.proc[other_proc].cache.get(index).state == State.SHARED)) {
			Main.proc[other_proc].cache.get(index).value = value;
		}
	}
}
