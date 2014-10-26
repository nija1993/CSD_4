import java.util.Random;


public class Main {
	static int size_cache;
	static int size_memory;
	static int size_block;
	static int size_bus_width;
	static int no_requests_p1; 
	static int no_requests_p2; // in bits
	static Processor proc[];
	static Memory memory;
	static Bus bus;
	static Random r;
	public static void getInitParam() {
		size_cache = 1024;
		size_memory = 32*1024;
		size_block = 32;
		size_bus_width = 32;
	}
	public static void main(String args[]) {
		getInitParam();
		no_requests_p1 = 10;
		no_requests_p2 = 10;
		proc = new Processor[2];
		proc[0] = new Processor(size_cache, size_block, 0);
		proc[1] = new Processor(size_cache, size_block, 1);
		bus = new Bus();
		memory = new Memory(size_memory, size_block);
		r = new Random();
		int proc_num;
		int oper;
		int block_num;
	//	System.out.println("*******************************************************\nMESI\n"
	//			+ "******************************************************");
		for(int i = 0; i < 1000000; i++){
			proc_num = r.nextInt(2);
			oper = r.nextInt(2);
			block_num = r.nextInt(1024);
		//	System.out.println("--------------------------------------------------------");
			if(oper == 1){
		//		System.out.println("proc " + proc_num + "  reading from block " + block_num);
				proc[proc_num].proc_read(block_num);
			}else{
				int value = r.nextInt(100);
		//		System.out.println("proc " + proc_num + "  writing to block " + block_num + " " + value);
				proc[proc_num].proc_write(block_num, value);
			}
//			System.out.println("--------------------------------------------------------");
//			for(int k = 0; k < 2; k++){
//				System.out.println("proc " + k);
//				for(int j = 0; j < proc[k].cache.size(); j++){
//					System.out.print("block " + proc[k].cache.get(j).mem_index
//							+ "  " + proc[k].cache.get(j).state + "  " + proc[k].cache.get(j).value
//							 + "    ");
//				}
//				System.out.println();
//			}
		}
		for(int proc_no = 0; proc_no < 2; proc_no++) {
			for(int i = 0; i < proc[proc_no].cache.size(); i++){
				if(proc[proc_no].cache.get(i).state == State.MODIFIED)
					bus.bus_operation(Request.MEM_W, proc[proc_no].cache.get(i).mem_index,
							proc[proc_no].cache.get(i).value);
			}
		}
		System.out.println("MESI : \n\tbus operations : " + bus.bus_operations + "    memory operations : "
					+ memory.memory_operations);
	//	System.out.println("************************************************************"
	//			+ "\nMOESI\n***********************************************************");
		proc[0].cache.clear();
		proc[1].cache.clear();
		bus.bus_operations = 0;
		memory.memory_operations = 0;
		for(int i = 0; i < 1000000; i++){
			proc_num = r.nextInt(2);
			oper = r.nextInt(2);
			block_num = r.nextInt(1024);
		//	System.out.println("--------------------------------------------------------");
			if(oper == 1){
		//		System.out.println("proc " + proc_num + "  reading from block " + block_num);
				proc[proc_num].proc_read_MOESI(block_num);
			}else{
				int value = r.nextInt(100);
		//		System.out.println("proc " + proc_num + "  writing to block " + block_num + " " + value);
				proc[proc_num].proc_write_MOESI(block_num, value);
			}
//			System.out.println("--------------------------------------------------------");
//			for(int k = 0; k < 2; k++){
//				System.out.println("proc " + k);
//				for(int j = 0; j < proc[k].cache.size(); j++){
//					System.out.print("block " + proc[k].cache.get(j).mem_index
//							+ "  " + proc[k].cache.get(j).state + "  " + proc[k].cache.get(j).value 
//							+ "    ");
//				}
//				System.out.println();
//			}
		}
		for(int proc_no = 0; proc_no < 2; proc_no++) {
			for(int i = 0; i < proc[proc_no].cache.size(); i++){
				if(proc[proc_no].cache.get(i).state == State.MODIFIED || proc[proc_no].cache.get(i).state == State.OWNED)
					bus.bus_operation(Request.MEM_W, proc[proc_no].cache.get(i).mem_index,
							proc[proc_no].cache.get(i).value);
			}
		}
		System.out.println("MOESI : \n\tbus operations : " + bus.bus_operations + "    memory operations : "
				+ memory.memory_operations);
	}
}



