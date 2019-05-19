package com.rideaustin.utils.geometry;

import com.rideaustin.utils.MathUtils;

/**
 * Line is defined by starting point and ending point on 2D dimension.<br>
 *
 * @author Roman Kushnarenko (sromku@gmail.com)
 */
public class Line {
    private final Point start;
    private final Point end;
    private double a = Double.NaN;
    private double b = Double.NaN;
    private boolean vertical = false;

    public Line(Point start, Point end) {
        this.start = start;
        this.end = end;

        if (!MathUtils.almostEqual(this.end.x, this.start.x, 0.001)) {
            a = ((this.end.y - this.start.y) / (this.end.x - this.start.x));
            b = this.start.y - a * this.start.x;
        } else {
            vertical = true;
        }
    }

    /**
     * Indicate whereas the point lays on the line.
     *
     * @param point - The point to check
     * @return <code>True</code> if the point lays on the line, otherwise return <code>False</code>
     */
    public boolean isInside(Point point) {
        double maxX = start.x > end.x ? start.x : end.x;
        double minX = start.x < end.x ? start.x : end.x;
        double maxY = start.y > end.y ? start.y : end.y;
        double minY = start.y < end.y ? start.y : end.y;

        if ((point.x >= minX && point.x <= maxX) && (point.y >= minY && point.y <= maxY)) {
            return true;
        }
        return false;
    }

    /**
     * Indicate whereas the line is vertical. <br>
     * For example, line like x=1 is vertical, in other words parallel to axis Y. <br>
     * In this case the A is (+/-)infinite.
     *
     * @return <code>True</code> if the line is vertical, otherwise return <code>False</code>
     */
    public boolean isVertical() {
        return vertical;
    }

    /**
     * y = <b>A</b>x + B
     *
     * @return The <b>A</b>
     */
    public double getA() {
        return a;
    }

    /**
     * y = Ax + <b>B</b>
     *
     * @return The <b>B</b>
     */
    public double getB() {
        return b;
    }

    /**
     * Get start point
     *
     * @return The start point
     */
    public Point getStart() {
        return start;
    }

    /**
     * Get end point
     *
     * @return The end point
     */
    public Point getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return String.format("%s-%s", start.toString(), end.toString());
    }
}
