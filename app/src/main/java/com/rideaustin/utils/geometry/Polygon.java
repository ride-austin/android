package com.rideaustin.utils.geometry;

import com.rideaustin.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The 2D polygon. <br>
 *
 * @author Roman Kushnarenko (sromku@gmail.com)
 * @see {@link Builder}
 */
public class Polygon {
    private final BoundingBox boundingBox;
    private final List<Line> sides;

    private Polygon(List<Line> sides, BoundingBox boundingBox) {
        this.sides = sides;
        this.boundingBox = boundingBox;
    }

    /**
     * Get the builder of the polygon
     *
     * @return The builder
     */
    public static Builder Builder() {
        return new Builder();
    }

    /**
     * Builder of the polygon
     *
     * @author Roman Kushnarenko (sromku@gmail.com)
     */
    public static class Builder {
        private List<Point> vertexes = new ArrayList<Point>();
        private List<Line> sides = new ArrayList<Line>();
        private BoundingBox boundingBox = null;

        private boolean firstPoint = true;
        private boolean isClosed = false;

        /**
         * Add vertex points of the polygon.<br>
         * It is very important to add the vertexes by order, like you were drawing them one by one.
         *
         * @param point The vertex point
         * @return The builder
         */
        public Builder addVertex(Point point) {
            if (isClosed) {
                // each hole we start with the new array of vertex points
                vertexes = new ArrayList<Point>();
                isClosed = false;
            }

            updateBoundingBox(point);
            vertexes.add(point);

            // add line (edge) to the polygon
            if (vertexes.size() > 1) {
                Line Line = new Line(vertexes.get(vertexes.size() - 2), point);
                sides.add(Line);
            }

            return this;
        }

        /**
         * Close the polygon shape. This will create a new side (edge) from the <b>last</b> vertex point to the <b>first</b> vertex point.
         *
         * @return The builder
         */
        public Builder close() {
            validate();

            // add last Line
            sides.add(new Line(vertexes.get(vertexes.size() - 1), vertexes.get(0)));
            isClosed = true;

            return this;
        }

        /**
         * Build the instance of the polygon shape.
         *
         * @return The polygon
         */
        public Polygon build() {
            validate();

            // in case you forgot to close
            if (!isClosed) {
                // add last Line
                sides.add(new Line(vertexes.get(vertexes.size() - 1), vertexes.get(0)));
            }

            Polygon polygon = new Polygon(sides, boundingBox);
            return polygon;
        }

        /**
         * Update bounding box with a new point.<br>
         *
         * @param point New point
         */
        private void updateBoundingBox(Point point) {
            if (firstPoint) {
                boundingBox = new BoundingBox();
                boundingBox.xMax = point.x;
                boundingBox.xMin = point.x;
                boundingBox.yMax = point.y;
                boundingBox.yMin = point.y;

                firstPoint = false;
            } else {
                // set bounding box
                if (point.x > boundingBox.xMax) {
                    boundingBox.xMax = point.x;
                } else if (point.x < boundingBox.xMin) {
                    boundingBox.xMin = point.x;
                }
                if (point.y > boundingBox.yMax) {
                    boundingBox.yMax = point.y;
                } else if (point.y < boundingBox.yMin) {
                    boundingBox.yMin = point.y;
                }
            }
        }

        private void validate() {
            if (vertexes.size() < 3) {
                throw new RuntimeException("Polygon must have at least 3 points");
            }
        }
    }

    /**
     * Check if the the given point is inside of the polygon.<br>
     *
     * @param point The point to check
     * @return <code>True</code> if the point is inside the polygon, otherwise return <code>False</code>
     */
    public boolean contains(Point point) {
        if (inBoundingBox(point)) {
            Line ray = createRay(point);
            int intersection = 0;
            for (Line side : sides) {
                if (intersect(ray, side)) {
                    // System.out.println("intersection++");
                    intersection++;
                }
            }

			/*
             * If the number of intersections is odd, then the point is inside the polygon
			 */
            if (intersection % 2 == 1) {
                return true;
            }
        }
        return false;
    }

    public List<Line> getSides() {
        return sides;
    }

    /**
     * By given ray and one side of the polygon, check if both lines intersect.
     *
     * @param ray
     * @param side
     * @return <code>True</code> if both lines intersect, otherwise return <code>False</code>
     */
    private boolean intersect(Line ray, Line side) {
        Point intersectPoint = null;

        // if both vectors aren't from the kind of x=1 lines then go into
        if (!ray.isVertical() && !side.isVertical()) {
            // check if both vectors are parallel. If they are parallel then no intersection point will exist
            if (MathUtils.almostEqual(ray.getA(), side.getA(), 0.001)) {
                return false;
            }

            double x = ((side.getB() - ray.getB()) / (ray.getA() - side.getA())); // x = (b2-b1)/(a1-a2)
            double y = side.getA() * x + side.getB(); // y = a2*x+b2
            intersectPoint = new Point(x, y);
        } else if (ray.isVertical() && !side.isVertical()) {
            double x = ray.getStart().x;
            double y = side.getA() * x + side.getB();
            intersectPoint = new Point(x, y);
        } else if (!ray.isVertical() && side.isVertical()) {
            double x = side.getStart().x;
            double y = ray.getA() * x + ray.getB();
            intersectPoint = new Point(x, y);
        } else {
            return false;
        }

        // System.out.println("Ray: " + ray.toString() + " ,Side: " + side);
        // System.out.println("Intersect point: " + intersectPoint.toString());

        if (side.isInside(intersectPoint) && ray.isInside(intersectPoint)) {
            return true;
        }

        return false;
    }

    /**
     * Create a ray. The ray will be created by given point and on point outside of the polygon.<br>
     * The outside point is calculated automatically.
     *
     * @param point
     * @return
     */
    private Line createRay(Point point) {
        // create outside point
        double epsilon = (boundingBox.xMax - boundingBox.xMin) / 100f;
        Point outsidePoint = new Point(boundingBox.xMin - epsilon, boundingBox.yMin);

        Line vector = new Line(outsidePoint, point);
        return vector;
    }

    /**
     * Check if the given point is in bounding box
     *
     * @param point
     * @return <code>True</code> if the point in bounding box, otherwise return <code>False</code>
     */
    private boolean inBoundingBox(Point point) {
        if (point.x < boundingBox.xMin || point.x > boundingBox.xMax || point.y < boundingBox.yMin || point.y > boundingBox.yMax) {
            return false;
        }
        return true;
    }

    private static class BoundingBox {
        public double xMax = Double.NEGATIVE_INFINITY;
        public double xMin = Double.NEGATIVE_INFINITY;
        public double yMax = Double.NEGATIVE_INFINITY;
        public double yMin = Double.NEGATIVE_INFINITY;
    }
}
