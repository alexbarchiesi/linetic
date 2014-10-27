package ch.linetic.analysis.analyzers;

import java.util.Iterator;

import ch.linetic.analysis.Analyzer;
import ch.linetic.gesture.Joint;
import ch.linetic.gesture.MovementInterface;
import ch.linetic.gesture.PoseInterface.JointType;

public final class ReverseAnalyzer extends Analyzer {

	public final static float MIN_VALUE = 0;
	public final static float MAX_VALUE = 90;
	private static final int NBR_LAST_POSES = 5;

	public ReverseAnalyzer(int index) {
		super(index, MIN_VALUE, MAX_VALUE, false);
	}

	@Override
	protected float compute(MovementInterface movement) {
		if (movement.size() < 1) {
			return 0;
		}
		
		float accumulator = 0;
		
		Iterator<Joint> leftE = movement.getJointMovement(JointType.ELBOW_LEFT, NBR_LAST_POSES).iterator();
		Iterator<Joint> rightE = movement.getJointMovement(JointType.ELBOW_RIGHT, NBR_LAST_POSES).iterator();

		Joint horizontal = new Joint(-1, 0, 0);
		Joint elbows;
		while(leftE.hasNext() && rightE.hasNext()) {
			elbows = leftE.next().sub(rightE.next());
			accumulator += horizontal.angle(elbows);
		}
		accumulator /= NBR_LAST_POSES;
		return accumulator;
	}

	@Override
	public String name() {
		return "Reverse";
	}

}
