package ch.linetic.camera;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import SimpleOpenNI.SimpleOpenNI;
import ch.linetic.gesture.Joint;
import ch.linetic.gesture.Pose;
import ch.linetic.gesture.PoseInterface;
import ch.linetic.gesture.PoseInterface.JointType;
import ch.linetic.user.UserInterface;

public class Kinect implements CameraInterface {
	
	SimpleOpenNI kinect;
	PApplet parrent;
	PGraphics pg;
	
	public static CameraInterface getNextKinect(PApplet parrent) throws NoCameraConnectedException {

		SimpleOpenNI k = new SimpleOpenNI(parrent, SimpleOpenNI.RUN_MODE_MULTI_THREADED);

		if (k.isInit() == false) {
			System.out.println("Can't init SimpleOpenNI, maybe the camera is not connected!");
			throw new NoCameraConnectedException();
		}

		// Enable depthMap generation & skeleton for particular joints
		k.enableDepth();
		k.enableUser();
		//k.enableRGB();

		return new Kinect(parrent, k);
	}
	
	private Kinect(PApplet parrent, SimpleOpenNI kinect) {
		this.kinect = kinect;
		this.parrent = parrent;
		this.pg = parrent.createGraphics(kinect.depthWidth(), kinect.depthHeight());
	}

	@Override
	public void update() {
		// Update the camera
		kinect.update();

		// Draw depthImageMap
		pg.beginDraw();
		pg.image(kinect.depthImage(), 0, 0);
		pg.endDraw();
	}

	@Override
	public PoseInterface capture(UserInterface user) {
		if (user == null)
			throw new IllegalArgumentException();

	    Pose pose = new Pose();

	    PVector jointHead3D = new PVector();
	    PVector jointNeck3D = new PVector();
	    PVector jointLeftShoulder3D = new PVector();
	    PVector jointLeftElbow3D = new PVector();
	    PVector jointLeftHand3D = new PVector();
	    PVector jointRightShoulder3D = new PVector();
	    PVector jointRightElbow3D = new PVector();
	    PVector jointRightHand3D = new PVector();
	    PVector jointTorso3D = new PVector();

	    PVector jointHead2D = new PVector();  
	    PVector jointNeck2D = new PVector();  
	    PVector jointLeftShoulder2D = new PVector();
	    PVector jointLeftElbow2D = new PVector();
	    PVector jointLeftHand2D = new PVector();
	    PVector jointRightShoulder2D = new PVector();
	    PVector jointRightElbow2D = new PVector();
	    PVector jointRightHand2D = new PVector();
	    PVector jointTorso2D = new PVector();

	    // Get the joint positions
	    kinect.getJointPositionSkeleton(user.getId(), SimpleOpenNI.SKEL_HEAD, jointHead3D);  
	    kinect.getJointPositionSkeleton(user.getId(), SimpleOpenNI.SKEL_NECK, jointNeck3D);  
	    kinect.getJointPositionSkeleton(user.getId(), SimpleOpenNI.SKEL_LEFT_SHOULDER, jointLeftShoulder3D);
	    kinect.getJointPositionSkeleton(user.getId(), SimpleOpenNI.SKEL_RIGHT_SHOULDER, jointRightShoulder3D);
	    kinect.getJointPositionSkeleton(user.getId(), SimpleOpenNI.SKEL_LEFT_ELBOW, jointLeftElbow3D);
	    kinect.getJointPositionSkeleton(user.getId(), SimpleOpenNI.SKEL_RIGHT_ELBOW, jointRightElbow3D);
	    kinect.getJointPositionSkeleton(user.getId(), SimpleOpenNI.SKEL_LEFT_HAND, jointLeftHand3D);
	    kinect.getJointPositionSkeleton(user.getId(), SimpleOpenNI.SKEL_RIGHT_HAND, jointRightHand3D);
	    kinect.getJointPositionSkeleton(user.getId(), SimpleOpenNI.SKEL_TORSO, jointTorso3D);

	    kinect.convertRealWorldToProjective(jointHead3D, jointHead2D);
	    kinect.convertRealWorldToProjective(jointNeck3D, jointNeck2D);
	    kinect.convertRealWorldToProjective(jointLeftShoulder3D, jointLeftShoulder2D);
	    kinect.convertRealWorldToProjective(jointLeftElbow3D, jointLeftElbow2D);
	    kinect.convertRealWorldToProjective(jointLeftHand3D, jointLeftHand2D);
	    kinect.convertRealWorldToProjective(jointRightShoulder3D, jointRightShoulder2D);
	    kinect.convertRealWorldToProjective(jointRightElbow3D, jointRightElbow2D);
	    kinect.convertRealWorldToProjective(jointRightHand3D, jointRightHand2D);
	    kinect.convertRealWorldToProjective(jointTorso3D, jointTorso2D);

	    // Set the new pose with those joints  
	    pose.setJoint(JointType.NECK, new Joint(jointNeck3D));
	    pose.setJoint(JointType.SHOULDER_LEFT, new Joint(jointLeftShoulder3D));
	    pose.setJoint(JointType.SHOULDER_RIGHT, new Joint(jointRightShoulder3D));
	    pose.setJoint(JointType.ELBOW_LEFT, new Joint(jointLeftElbow3D));
	    pose.setJoint(JointType.ELBOW_RIGHT, new Joint(jointRightElbow3D));
	    pose.setJoint(JointType.HAND_LEFT, new Joint(jointLeftHand3D));
	    pose.setJoint(JointType.HAND_RIGHT, new Joint(jointRightHand3D));
	    pose.setJoint(JointType.TORSO, new Joint(jointTorso3D));

	    // Draw
	    pg.beginDraw();
	    pg.stroke(user.getColor());
	    pg.strokeWeight(5);

	    drawLine(jointHead2D, jointNeck2D);
	    drawLine(jointNeck2D, jointLeftShoulder2D);
	    drawLine(jointLeftShoulder2D, jointLeftElbow2D);
	    drawLine(jointLeftElbow2D, jointLeftHand2D);  
	    drawLine(jointNeck2D, jointRightShoulder2D);
	    drawLine(jointRightShoulder2D, jointRightElbow2D);
	    drawLine(jointRightElbow2D, jointRightHand2D);
	    drawLine(jointNeck2D, jointTorso2D);
	    
	    pg.endDraw();

	    // Warnings
	    //user.updateWarning(jointNeck2D, jointNeck3D);

	    return pose;
	}

	@Override
	public PImage getImage() {
		return pg.get();
	}

	@Override
	public boolean isConnected() {
		return true;
	}
	
	private void drawLine(PVector a, PVector b) {
		pg.line(a.x, a.y, b.x, b.y);
	}

}
