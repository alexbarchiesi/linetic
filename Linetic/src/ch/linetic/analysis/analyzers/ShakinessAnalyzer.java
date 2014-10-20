package ch.linetic.analysis.analyzers;

import java.util.Collection;

import ch.linetic.analysis.Analyzer;
import ch.linetic.gesture.Joint;
import ch.linetic.gesture.MovementInterface;
import ch.linetic.gesture.PoseInterface.JointType;

public final class ShakinessAnalyzer extends Analyzer {

	public final static float MIN_VALUE = 0;
	public final static float MAX_VALUE = 1; // TODO PK ? (180)

	public ShakinessAnalyzer(int index, String name) {
		super(index, name);
	}

	@Override
	protected float compute(MovementInterface movement) {
		if (movement.size() < 2) {
			return 0;
		}
		
		float accumulator = 0;
		for (Collection<Joint> joints : movement.getMovements()) {
			accumulator += operationPerFrameAVG(joints);
		}
		accumulator /= JointType.values().length;
		return accumulator;
	}

	@Override
	protected float rescale(float x) {
		return rescale(x, MIN_VALUE, MAX_VALUE);
	}
	
	@Override
	protected float operationPerFrame(Joint a, Joint b) {
		return a.angle(b);
	}

}
