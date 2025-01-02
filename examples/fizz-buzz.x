using io;

fn main() -> void {

	loop v in 100 {
		if v % 15 == 0 {
			println("fizzbuzz");
		}
		else if v % 5 == 0 {
			println("buzz");
		}	
		else if v % 3 == 0 {
			println("fizz");
		}	
		else {
			println(v);
		}
	}

}
