package rnn.model;
import java.io.Serializable;
import java.util.List;

import rnn.matrix.Matrix;
import rnn.autodiff.Graph;


public interface Model extends Serializable {
	Matrix forward(Matrix input, Graph g) throws Exception;
	void resetState();
	List<Matrix> getParameters();
}
