
public class Main {
	static int size_cache;
	static int size_memory;
	static int size_block;
	static int size_bus_width;			// in bits
	public static void getInitParam() {
		size_cache = 1024;
		size_memory = 32*1024;
		size_block = 32;
		size_bus_width = 32;
	}
	public static void main(String args[]) {
		getInitParam();
		
//		System.out.println(1);
	}
}
