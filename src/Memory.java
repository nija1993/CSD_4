
public class Memory {
	long[] values;
	int memory_operations = 0;
	public Memory(int size_m, int size_b){
		values = new long[size_m/size_b];
		for(int i = 0; i < size_m/size_b; i++){
			values[i] = i;
		}
	}
}
