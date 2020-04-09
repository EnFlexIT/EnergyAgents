package de.enflexit.ea.lib.powerFlowCalculation.parameter;

public class Complex {
private double real=0;
private double imag=0;

public double getReal() {
	return real;
}
public void setReal(double real) {
	this.real = real;
}
public double getImag() {
	return imag;
}
public void setImag(double imag) {
	this.imag = imag;
}

public Complex(double real, double imag){
	this.real=real;
	this.imag=imag;
}
}
