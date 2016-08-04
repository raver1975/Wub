package rnn.loss;

import java.io.Serializable;

import rnn.matrix.Matrix;

public interface Loss extends Serializable {
	void backward(Matrix actualOutput, Matrix targetOutput) throws Exception;
	double measure(Matrix actualOutput, Matrix targetOutput) throws Exception;
}
