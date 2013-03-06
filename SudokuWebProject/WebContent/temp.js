function Fred() {
	this.a = 1;

	function foo() {
		if (a == 1) {
			a++;
		}
		ooh()
		duh();
	}
	
	function ooh() {
		var f = 0;
	}
	
	this.bar = function () {
		var a = 3;
		var b = 4;
		foo.call(this);
	};
	
	this.c = 3;
	this.d = 4;
	
}

Fred.prototype.duh = function() {
	alert(this.a);
};


//Fred.foo = function() {
//	if (a == 1) {
//		var c = 0;
//	} else {
//		var d = 0;
//	}
//	var e = 0;
//};