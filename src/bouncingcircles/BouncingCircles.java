package bouncingcircles;

import com.jogamp.opengl.util.Animator;
import control.Keyboard;
import control.Mouse;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import gameobject.BoundingBox;
import gameobject.Circle;
import quadtree.QuadTree;

public class BouncingCircles implements GLEventListener {

    private GLCanvas canvas;
    private GLU glu;
    private QuadTree quadTree;
    private ArrayList<BoundingBox> gameObjects = new ArrayList<>();
    private final int MAX_LEVEL = 6;
    private final int MAX_CAPACITY = 4;
    private int numberOfGameObjects = 0;
    private JLabel statusLabel = new JLabel("");
    private CollisionHandler collisionHandler = new CollisionHandler();

    public BouncingCircles() {
        System.setProperty("sun.awt.noerasebackground", "true");
        JFrame frame = new JFrame("Test Quad Tree");
        Animator animator;

        Mouse mouseEventHandler;

        // Create the initial quad tree node
        quadTree = new QuadTree(0, new BoundingBox(0, 600, 600, 600));

        // Setup the canvas
        canvas = new GLCanvas();
        canvas.setPreferredSize(new Dimension(600, 600));
        canvas.addGLEventListener(this);

        // Setup the frame
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(canvas, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Setup the status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        frame.add(statusPanel, BorderLayout.SOUTH);
        statusPanel.add(statusLabel);

        // Finish setting up the frame
        frame.pack();
        frame.setVisible(true);
        frame.requestFocus();
        canvas.requestFocusInWindow();

        // Add keyboard and mouse listener
        canvas.addKeyListener(new Keyboard(this));
        mouseEventHandler = new Mouse(this, canvas.getHeight());
        canvas.addMouseListener(mouseEventHandler);
        canvas.addMouseMotionListener(mouseEventHandler);

        // Start the animator
        animator = new Animator(canvas);
        animator.start();
    }

    public static void main(String[] args) {
        new BouncingCircles();
    }

    public synchronized void reset() {
        quadTree.clear();
        gameObjects.clear();
        numberOfGameObjects = 0;
    }

    public void insertGameObject(BoundingBox b) {
        b.setContainerBounds(new BoundingBox(0, 600, 600, 600));
        gameObjects.add(b);
        numberOfGameObjects++;
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        glu.gluLookAt(0, 0, 1, 0, 0, 0, 0f, 1f, 0f);


        quadTree = new QuadTree(0, new BoundingBox(0, 600, 600, 600));

        for (BoundingBox b : gameObjects)
            quadTree.insert(b);


        //quadTree.draw(gl);

        for (BoundingBox b : gameObjects) {
            
            ((Circle) b).move();
            b.draw(gl);
            ((Circle) b).color = Circle.NORMAL_COLOR;
        }


        List<BoundingBox> returnObjects;
        for (int i = 0; i < gameObjects.size(); i++) {
            returnObjects = new ArrayList();
            quadTree.retrieve(returnObjects, gameObjects.get(i));

            for (BoundingBox b : returnObjects) {
                if (gameObjects.get(i) != b && gameObjects.get(i).intersects(b.x, b.y, b.width, b.height))
                    collisionHandler.handleCollision(gameObjects.get(i), b);
            }
        }



        // Update fps label
        statusLabel.setText(String.format("Number of particles: %d | FPS: %.2f", numberOfGameObjects, drawable.getAnimator().getLastFPS()));

        // Check for errors
        int error = gl.glGetError();
        if (error != GL.GL_NO_ERROR)
            System.out.println(
                    "OpenGL Error: " + error);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.setSwapInterval(1);
        gl.glClearColor(0, 0, 0, 0);        // Black background
        gl.glLineWidth(1f);
        gl.glPointSize(3f);
        gl.glShadeModel(GL2.GL_SMOOTH);         // smooth or flat 		
        gl.glClearDepth(1.0f);			// depth handling routines	
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glDepthFunc(GL2.GL_LEQUAL);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
        glu = new GLU();

        drawable.getAnimator().setUpdateFPSFrames(50, null);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        //width = height = Math.min(width, height); 	//this avoids different x,y scaling
        //gl.glViewport(0, 0, width, height);
        gl.glOrtho(-1, 601, -1, 601, -1, 601);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(0, 0, 1, 0, 0, 0, 0f, 1f, 0f);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    }
}
