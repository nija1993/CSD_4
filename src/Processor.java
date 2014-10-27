import java.util.ArrayList;
import java.util.Iterator;

public class Processor {
	int no_blocks;
	int proc_index;
	int state_changes;
	ArrayList<Block> cache;
	Processor(int size_c, int size_b, int proc_i){
		no_blocks = size_c/size_b;
		proc_index = proc_i;
		cache = new ArrayList<>();
		state_changes = 0;
	}
	int search(int mem_index){
		Block b; 
		for(int i = 0; i < cache.size(); i++){
			if(cache.get(i).mem_index == mem_index){
				return i;
			}
		}
		return -1;
	}
	
	void invalidate(int mem_index){
		int index = search(mem_index);
		if(index != -1){
			Block b = cache.get(index);
			if(b.state == State.MODIFIED || b.state == State.OWNED)
				Main.bus.bus_operation(Request.MEM_W, mem_index, b.value);
			b.state = State.INVALID;
			state_changes++;
			cache.set(index, b);
		}
	}
	int share(int mem_index){
		int index = search(mem_index);
		if(index != -1){
			if(cache.get(index).state == State.MODIFIED)
				Main.bus.bus_operation(Request.MEM_W, mem_index, cache.get(index).value);
			if(cache.get(index).state != State.SHARED)
				state_changes++;
			cache.get(index).state = State.SHARED;
			return index;
		}
		return -1;
	}
	
	int spill_update(){
		// Is it better to remove a block that is invalid instead of random one ? 
		if(cache.size() == no_blocks) {
			int evict_index = Main.r.nextInt(no_blocks);
			Block b = cache.remove(evict_index);
			if(b.state == State.MODIFIED || b.state == State.OWNED){
				// System.out.println("writing evicted block to memory");
				Main.bus.bus_operation(Request.MEM_W, b.mem_index, b.value);
				state_changes++;
			}
		}
		return -1;
	}
	
	long proc_read(int mem_index){
		int index = search(mem_index);
		if(index != -1 && cache.get(index).state != State.INVALID){
			return cache.get(index).value;
		}
		int shared = (int) Main.bus.bus_operation(Request.CHECK_SHARE, mem_index, proc_index);
		long value = Main.bus.bus_operation(Request.MEM_R, mem_index, proc_index);
		if(index != -1){
			cache.get(index).value = value;
			if(shared == 1)
				cache.get(index).state = State.SHARED;
			else
				cache.get(index).state = State.EXCLUSIVE;
		}
		else{
			spill_update();
			Block b = new Block(mem_index, value);
			if(shared == 1)
				b.state = State.SHARED;
			else
				b.state = State.EXCLUSIVE;
			cache.add(b);
		}
		state_changes++;
		return value;
	}
	
	long proc_read_MOESI(int mem_index){
		int index = search(mem_index);
		if(index != -1 && cache.get(index).state != State.INVALID){
			return cache.get(index).value;
		}
		Block bloc = Main.bus.get_block_moesi(mem_index, proc_index);
		state_changes++;
		long value;
		State state;
		if(bloc == null){
			value = Main.bus.bus_operation(Request.MEM_R, mem_index, proc_index);
			bloc = new Block(mem_index, value);
			int shared = (int) Main.bus.bus_operation(Request.CHECK_SHARE, mem_index, proc_index);
			if(shared == 1)
				state = State.SHARED;
			else
				state = State.EXCLUSIVE;
		}else{
			value = bloc.value;
			state = State.SHARED;
		}
		if(index != -1){
			cache.get(index).value = value;
			cache.get(index).state = state;
		}
		else{
			bloc.state = state;
			bloc.value = value;
			spill_update();
			cache.add(bloc);
		}
		return value;
	}
	
	void get_ownership(int index){
		if(cache.get(index).state != State.OWNED)
			state_changes++;
		cache.get(index).state = State.OWNED;
	}
	
	void proc_write(int mem_index, long value){
		int index = search(mem_index);
		if(index == -1 || cache.get(index).state == State.INVALID){
			proc_read(mem_index);
			proc_write(mem_index, value);
			return;
		}
		if(cache.get(index).state == State.MODIFIED){
			Main.bus.bus_operation(Request.MEM_W, mem_index, cache.get(index).value);
		}
		else if(cache.get(index).state == State.SHARED)
			Main.bus.bus_operation(Request.RFO, mem_index, proc_index);
		if(cache.get(index).state != State.MODIFIED)
			state_changes++;
		cache.get(index).state = State.MODIFIED;
		cache.get(index).value = value;
	}
	
	void proc_write_MOESI(int mem_index, long value) {
		int index = search(mem_index);
		if(index == -1 || cache.get(index).state == State.INVALID){
			proc_read_MOESI(mem_index);
			proc_write_MOESI(mem_index, value);
			return;
		}
		if(cache.get(index).state == State.MODIFIED){
			Main.bus.bus_operation(Request.MEM_W, mem_index, cache.get(index).value);
		}
		else if(cache.get(index).state == State.OWNED) {
			Main.bus.send_block_moesi(mem_index, proc_index, value);
		}
		else if(cache.get(index).state == State.SHARED){
			Main.bus.bus_operation(Request.RFO, mem_index, proc_index);
			cache.get(index).state = State.MODIFIED;
			state_changes++;
		}else{
			cache.get(index).state = State.MODIFIED;
			state_changes++;
		}
		cache.get(index).value = value;
		
	}
}



