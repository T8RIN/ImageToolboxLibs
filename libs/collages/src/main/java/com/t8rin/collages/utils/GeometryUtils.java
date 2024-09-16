package com.t8rin.collages.utils;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * All points of polygon must be ordered by clockwise along<br>
 * Created by admin on 5/4/2016.
 */
public class GeometryUtils {
    public static boolean isInCircle(PointF center, float radius, PointF p) {
        return (Math.sqrt((center.x - p.x) * (center.x - p.x) + (center.y - p.y) * (center.y - p.y)) <= radius);
    }

    /**
     * Return true if the given point is contained inside the boundary.
     * See: http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
     *
     * @param test The point to check
     * @return true if the point is inside the boundary, false otherwise
     */
    public static boolean contains(List<PointF> points, PointF test) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = points.size() - 1; i < points.size(); j = i++) {
            if ((points.get(i).y > test.y) != (points.get(j).y > test.y) &&
                    (test.x < (points.get(j).x - points.get(i).x) * (test.y - points.get(i).y) / (points.get(j).y - points.get(i).y) + points.get(i).x)) {
                result = !result;
            }
        }
        return result;
    }

    public static void createRectanglePath(Path outPath, float width, float height, float corner) {
        List<PointF> pointList = new ArrayList<>();
        pointList.add(new PointF(0, 0));
        pointList.add(new PointF(width, 0));
        pointList.add(new PointF(width, height));
        pointList.add(new PointF(0, height));
        createPathWithCircleCorner(outPath, pointList, corner);
    }

    public static void createRegularPolygonPath(Path outPath, float size, int vertexCount, float corner) {
        createRegularPolygonPath(outPath, size, size / 2, size / 2, vertexCount, corner);
    }

    public static void createRegularPolygonPath(Path outPath, float size, float centerX, float centerY, int vertexCount, float corner) {
        final float section = (float) (2.0 * Math.PI / vertexCount);
        float radius = size / 2;
        List<PointF> pointList = new ArrayList<>();
        pointList.add(new PointF((float) (centerX + radius * Math.cos(0)), (float) (centerY + radius * Math.sin(0))));
        for (int i = 1; i < vertexCount; i++) {
            pointList.add(new PointF((float) (centerX + radius * Math.cos(section * i)), (float) (centerY + radius * Math.sin(section * i))));
        }

        createPathWithCircleCorner(outPath, pointList, corner);
    }

    public static List<PointF> shrinkPathCollageUsingMap(List<PointF> pointList, float space, HashMap<PointF, PointF> map) {
        List<PointF> result = new ArrayList<>();
        for (PointF p : pointList) {
            PointF add = map.get(p);
            result.add(new PointF(p.x + add.x * space, p.y + add.y * space));
        }
        return result;
    }

    /**
     * Resolve case frame collage 3_3
     *
     * @param pointList
     * @param space
     * @param bound
     * @return shrank points
     */
    public static List<PointF> shrinkPathCollage_3_3(List<PointF> pointList, int centerPointIdx, float space, RectF bound) {
        List<PointF> result = new ArrayList<>();
        PointF center = pointList.get(centerPointIdx);
        PointF left = null;
        PointF right = null;
        if (centerPointIdx > 0) {
            left = pointList.get(centerPointIdx - 1);
        } else {
            left = pointList.get(pointList.size() - 1);
        }

        if (centerPointIdx < pointList.size() - 1) {
            right = pointList.get(centerPointIdx + 1);
        } else {
            right = pointList.get(0);
        }

        float spaceX, spaceY;
        for (PointF p : pointList) {
            PointF pointF = new PointF();
            spaceX = space;
            spaceY = space;
            if (bound != null) {
                if ((bound.left == 0 && p.x < center.x) || (bound.right == 1 && p.x >= center.x)) {
                    spaceX = 2 * space;
                }

                if ((bound.top == 0 && p.y < center.y) || (bound.bottom == 1 && p.y >= center.y)) {
                    spaceY = 2 * space;
                }
            }

            if (left.x == right.x) {
                if (left.x < center.x) {
                    if (p.x <= center.x) {
                        pointF.x = p.x + spaceX;
                    } else {
                        pointF.x = p.x - spaceX;
                    }
                } else {
                    if (p.x < center.x) {
                        pointF.x = p.x + spaceX;
                    } else {
                        pointF.x = p.x - spaceX;
                    }
                }

                if (p != left && p != right && p != center) {
                    if (p.y < center.y) {
                        pointF.y = p.y + spaceY;
                    } else {
                        pointF.y = p.y - spaceY;
                    }
                } else if (p == left || p == right) {
                    if (p.y < center.y) {
                        pointF.y = p.y - space;
                    } else {
                        pointF.y = p.y + space;
                    }
                } else {
                    pointF.y = p.y;
                }
            }

            result.add(pointF);
        }

        return result;
    }

    public static List<PointF> shrinkPath(List<PointF> pointList, float space, RectF bound) {
        List<PointF> result = new ArrayList<>();
        if (space == 0) {
            result.addAll(pointList);
        } else {
            PointF center = new PointF(0, 0);
            for (PointF p : pointList) {
                center.x += p.x;
                center.y += p.y;
            }

            center.x = center.x / pointList.size();
            center.y = center.y / pointList.size();
            float spaceX, spaceY;
            for (PointF p : pointList) {
                PointF pointF = new PointF();
                spaceX = space;
                spaceY = space;
                if (bound != null) {
                    if ((bound.left == 0 && p.x < center.x) || (bound.right == 1 && p.x >= center.x)) {
                        spaceX = 2 * space;
                    }

                    if ((bound.top == 0 && p.y < center.y) || (bound.bottom == 1 && p.y >= center.y)) {
                        spaceY = 2 * space;
                    }
                }

                if (Math.abs(center.x - p.x) >= 1) {
                    if (p.x < center.x) {
                        pointF.x = p.x + spaceX;
                    } else if (p.x > center.x) {
                        pointF.x = p.x - spaceX;
                    }
                } else {
                    pointF.x = p.x;
                }

                if (Math.abs(center.y - p.y) >= 1) {
                    if (p.y < center.y) {
                        pointF.y = p.y + spaceY;
                    } else if (p.y > center.y) {
                        pointF.y = p.y - spaceY;
                    }
                } else {
                    pointF.y = p.y;
                }

                result.add(pointF);
            }
        }
        return result;
    }

    public static List<PointF> commonShrinkPath(List<PointF> pointList, float space, Map<PointF, PointF> shrunkPointLeftRightDistances) {
        List<PointF> result = new ArrayList<>();
        if (space == 0) {
            result.addAll(pointList);
        } else {
            final ArrayList<PointF> convexHull = jarvis(pointList);
            for (int i = 0; i < pointList.size(); i++) {
                PointF center = pointList.get(i);
                boolean concave = true;
                for (PointF point : convexHull)
                    if (center == point) {
                        concave = false;
                        break;
                    }

                PointF left;
                PointF right;
                if (i == 0) {
                    left = pointList.get(pointList.size() - 1);
                } else {
                    left = pointList.get(i - 1);
                }

                if (i == pointList.size() - 1) {
                    right = pointList.get(0);
                } else {
                    right = pointList.get(i + 1);
                }

                PointF leftRightDistance = shrunkPointLeftRightDistances.get(center);
                PointF pointF = shrinkPoint(center, left, right, leftRightDistance.x * space, leftRightDistance.y * space, !concave, !concave);
                if (pointF != null) {
                    result.add(pointF);
                } else {
                    result.add(new PointF(0, 0));
                }
            }
        }
        return result;
    }

    public static void createPathWithCubicCorner(Path path, List<PointF> pointList, float corner) {
        path.reset();
        for (int i = 0; i < pointList.size(); i++) {
            if (corner == 0 || pointList.size() < 3) {
                if (i == 0) {
                    path.moveTo(pointList.get(i).x, pointList.get(i).y);
                } else {
                    path.lineTo(pointList.get(i).x, pointList.get(i).y);
                }
            } else {
                PointF center = new PointF(pointList.get(i).x, pointList.get(i).y);
                PointF left = new PointF();
                PointF right = new PointF();
                if (i == 0) {
                    left.x = pointList.get(pointList.size() - 1).x;
                    left.y = pointList.get(pointList.size() - 1).y;
                } else {
                    left.x = pointList.get(i - 1).x;
                    left.y = pointList.get(i - 1).y;
                }

                if (i == pointList.size() - 1) {
                    right.x = pointList.get(0).x;
                    right.y = pointList.get(0).y;
                } else {
                    right.x = pointList.get(i + 1).x;
                    right.y = pointList.get(i + 1).y;
                }

                PointF middleA = findPointOnSegment(center, left, corner);
                PointF middleB = findPointOnSegment(center, right, corner);
                PointF middle = findMiddlePoint(middleA, middleB, center);
                if (i == 0) {
                    path.moveTo(middleA.x, middleA.y);
                } else {
                    path.lineTo(middleA.x, middleA.y);
                }
                path.cubicTo(middleA.x, middleA.y, middle.x, middle.y, middleB.x, middleB.y);
            }
        }
    }

    private static boolean containPoint(List<PointF> points, PointF p) {
        for (PointF pointF : points)
            if (pointF == p || (pointF.x == p.x && pointF.y == p.y)) {
                return true;
            }
        return false;
    }

    public static Map<PointF, PointF[]> createPathWithCircleCorner(Path path, List<PointF> pointList, List<PointF> cornerPointList, float corner) {
        if (pointList == null || pointList.isEmpty()) {
            return null;
        }
        Map<PointF, PointF[]> cornerPointMap = new HashMap<>();
        path.reset();
        PointF[] firstPoints = new PointF[]{pointList.get(0), pointList.get(0), pointList.get(0)};
        ArrayList<PointF> convexHull = jarvis(pointList);
        for (int i = 0; i < pointList.size(); i++) {
            if (corner == 0 || pointList.size() < 3) {
                if (i == 0) {
                    path.moveTo(pointList.get(i).x, pointList.get(i).y);
                } else {
                    path.lineTo(pointList.get(i).x, pointList.get(i).y);
                }
            } else {
                boolean isCornerPoint = true;
                if (cornerPointList != null && cornerPointList.size() > 0) {
                    isCornerPoint = containPoint(cornerPointList, pointList.get(i));
                }

                if (!isCornerPoint) {
                    if (i == 0) {
                        path.moveTo(pointList.get(i).x, pointList.get(i).y);
                    } else {
                        path.lineTo(pointList.get(i).x, pointList.get(i).y);
                    }
                    if (i == pointList.size() - 1) {
                        path.lineTo(firstPoints[1].x, firstPoints[1].y);
                    }
                } else {
                    boolean concave = true;
                    for (PointF p : convexHull)
                        if (p == pointList.get(i)) {
                            concave = false;
                            break;
                        }
                    PointF center = new PointF(pointList.get(i).x, pointList.get(i).y);
                    PointF left = new PointF();
                    PointF right = new PointF();
                    if (i == 0) {
                        left.x = pointList.get(pointList.size() - 1).x;
                        left.y = pointList.get(pointList.size() - 1).y;
                    } else {
                        left.x = pointList.get(i - 1).x;
                        left.y = pointList.get(i - 1).y;
                    }

                    if (i == pointList.size() - 1) {
                        right.x = pointList.get(0).x;
                        right.y = pointList.get(0).y;
                    } else {
                        right.x = pointList.get(i + 1).x;
                        right.y = pointList.get(i + 1).y;
                    }

                    PointF[] pointFs = new PointF[3];
                    double[] angles = new double[2];
                    createArc(center, left, right, corner, angles, pointFs, concave);
                    if (i == 0) {
                        path.moveTo(pointFs[1].x, pointFs[1].y);
                    } else {
                        path.lineTo(pointFs[1].x, pointFs[1].y);
                    }

                    RectF oval = new RectF(pointFs[0].x - corner, pointFs[0].y - corner, pointFs[0].x + corner, pointFs[0].y + corner);
                    path.arcTo(oval, (float) angles[0], (float) angles[1], false);

                    if (i == 0) {
                        firstPoints = pointFs;
                    }

                    if (i == pointList.size() - 1) {
                        path.lineTo(firstPoints[1].x, firstPoints[1].y);
                    }

                    cornerPointMap.put(pointList.get(i), pointFs);
                }
            }
        }

        return cornerPointMap;
    }

    public static void createPathWithCircleCorner(Path path, List<PointF> pointList, float corner) {
        path.reset();
        PointF[] firstPoints = null;
        ArrayList<PointF> convexHull = jarvis(pointList);
        for (int i = 0; i < pointList.size(); i++) {
            if (corner == 0 || pointList.size() < 3) {
                if (i == 0) {
                    path.moveTo(pointList.get(i).x, pointList.get(i).y);
                } else {
                    path.lineTo(pointList.get(i).x, pointList.get(i).y);
                }
            } else {
                boolean concave = true;
                for (PointF p : convexHull)
                    if (p == pointList.get(i)) {
                        concave = false;
                        break;
                    }

                PointF center = new PointF(pointList.get(i).x, pointList.get(i).y);
                PointF left = new PointF();
                PointF right = new PointF();
                if (i == 0) {
                    left.x = pointList.get(pointList.size() - 1).x;
                    left.y = pointList.get(pointList.size() - 1).y;
                } else {
                    left.x = pointList.get(i - 1).x;
                    left.y = pointList.get(i - 1).y;
                }

                if (i == pointList.size() - 1) {
                    right.x = pointList.get(0).x;
                    right.y = pointList.get(0).y;
                } else {
                    right.x = pointList.get(i + 1).x;
                    right.y = pointList.get(i + 1).y;
                }

                PointF[] pointFs = new PointF[3];
                double[] angles = new double[2];
                createArc(center, left, right, corner, angles, pointFs, concave);
                if (i == 0) {
                    path.moveTo(pointFs[1].x, pointFs[1].y);
                } else {
                    path.lineTo(pointFs[1].x, pointFs[1].y);
                }

                RectF oval = new RectF(pointFs[0].x - corner, pointFs[0].y - corner, pointFs[0].x + corner, pointFs[0].y + corner);
                path.arcTo(oval, (float) angles[0], (float) angles[1], false);

                if (i == 0) {
                    firstPoints = pointFs;
                }

                if (i == pointList.size() - 1) {
                    path.lineTo(firstPoints[1].x, firstPoints[1].y);
                }
            }
        }
    }

    public static PointF findPointOnSegment(PointF A, PointF B, double dA) {
        if (dA == 0) {
            return new PointF(A.x, A.y);
        } else {
            PointF result = new PointF();
            float dAB = (float) (Math.sqrt((A.x - B.x) * (A.x - B.x) + (A.y - B.y) * (A.y - B.y)));
            double dx = Math.abs(A.x - B.x) * dA / dAB;
            double dy = Math.abs(A.y - B.y) * dA / dAB;
            if (A.x > B.x) {
                result.x = (float) (A.x - dx);
            } else {
                result.x = (float) (A.x + dx);
            }

            if (A.y > B.y) {
                result.y = (float) (A.y - dy);
            } else {
                result.y = (float) (A.y + dy);
            }

            return result;
        }
    }

    public static PointF findMiddlePoint(PointF A, PointF B, PointF D) {
        float d = (float) (Math.sqrt((A.x - B.x) * (A.x - B.x) + (A.y - B.y) * (A.y - B.y)) / 2);
        return findMiddlePoint(A, B, d, D);
    }

    public static PointF findMiddlePoint(PointF A, PointF B, float d, PointF D) {
        float a = B.y - A.y;
        float b = A.x - B.x;
        float c = B.x * A.y - A.x * B.y;
        PointF[] middlePoints = findMiddlePoint(A, B, d);
        float f = a * D.x + b * D.y + c;
        float f1 = a * middlePoints[0].x + b * middlePoints[0].y + c;
        if (f * f1 > Float.MIN_VALUE) {
            return middlePoints[0];
        } else {
            return middlePoints[1];
        }
    }


    public static boolean createArc(PointF A, PointF B, PointF C, float dA, double[] outAngles, PointF[] outPoints, boolean isConcave) {
        outPoints[0] = findPointOnBisector(A, B, C, dA);
        double d = ((A.x - outPoints[0].x) * (A.x - outPoints[0].x) + (A.y - outPoints[0].y) * (A.y - outPoints[0].y)) - dA * dA;
        d = Math.sqrt(d);
        outPoints[1] = findPointOnSegment(A, B, d);
        outPoints[2] = findPointOnSegment(A, C, d);
        //find angles
        double dMA = Math.sqrt((A.x - outPoints[0].x) * (A.x - outPoints[0].x) + (A.y - outPoints[0].y) * (A.y - outPoints[0].y));
        double halfSweepAngle = Math.acos(dA / dMA);
        double startAngle = Math.atan2(outPoints[1].y - outPoints[0].y, outPoints[1].x - outPoints[0].x);
        double endAngle = Math.atan2(outPoints[2].y - outPoints[0].y, outPoints[2].x - outPoints[0].x);
        double sweepAngle = endAngle - startAngle;
        if (!isConcave) {
            sweepAngle = 2 * halfSweepAngle;
        }

        outAngles[0] = Math.toDegrees(startAngle);
        outAngles[1] = Math.toDegrees(sweepAngle);
        double tmp = Math.toDegrees(2 * halfSweepAngle);
        if (Math.abs(tmp - Math.abs(outAngles[1])) > 1) {
            outAngles[1] = -tmp;
        }

        return false;
    }

    /**
     * @param A
     * @param B
     * @param C
     * @param dA
     * @return null if does not have solution, return PointF(Float.MaxValue, Float.MaxValue) if have infinite solution, other return the solution
     */
    public static PointF findPointOnBisector(PointF A, PointF B, PointF C, float dA) {
        double[] lineAB = getCoefficients(A, B);
        double[] lineAC = getCoefficients(A, C);
        double vB = lineAC[0] * B.x + lineAC[1] * B.y + lineAC[2];
        double vC = lineAB[0] * C.x + lineAB[1] * C.y + lineAB[2];
        double square1 = Math.sqrt(lineAB[0] * lineAB[0] + lineAB[1] * lineAB[1]);
        double square2 = Math.sqrt(lineAC[0] * lineAC[0] + lineAC[1] * lineAC[1]);
        if (vC > 0) {
            if (vB > 0) {
                return findIntersectPoint(lineAB[0], lineAB[1], dA * square1 - lineAB[2],
                        lineAC[0], lineAC[1], dA * square2 - lineAC[2]);
            } else {
                return findIntersectPoint(lineAB[0], lineAB[1], dA * square1 - lineAB[2],
                        -lineAC[0], -lineAC[1], dA * square2 + lineAC[2]);
            }
        } else {
            if (vB > 0) {
                return findIntersectPoint(-lineAB[0], -lineAB[1], dA * square1 + lineAB[2],
                        lineAC[0], lineAC[1], dA * square2 - lineAC[2]);
            } else {
                return findIntersectPoint(-lineAB[0], -lineAB[1], dA * square1 + lineAB[2],
                        -lineAC[0], -lineAC[1], dA * square2 + lineAC[2]);
            }
        }

    }

    public static double distanceToLine(double[] line, PointF P) {
        double bottom = Math.sqrt(line[0] * line[0] + line[1] * line[1]);
        return Math.abs((line[0] * P.x + line[1] * P.y + line[2]) / bottom);
    }

    /**
     * @param A
     * @param B
     * @param C
     * @param dAB is the distance from shrunk point to AB line
     * @param dAC is the distance from shrunk point to AC line
     * @param b   is true if shrunk point and point B located on same half-plane
     * @param c   is true if shrunk point and point C located on same half-plane
     * @return shrunk point of point A
     */
    public static PointF shrinkPoint(PointF A, PointF B, PointF C, float dAB, float dAC, boolean b, boolean c) {
        double[] ab = getCoefficients(A, B);
        double[] ac = getCoefficients(A, C);
        double m = dAB * Math.sqrt(ab[0] * ab[0] + ab[1] * ab[1]) - ab[2];
        double n = dAC * Math.sqrt(ac[0] * ac[0] + ac[1] * ac[1]) - ac[2];
        double p = -dAB * Math.sqrt(ab[0] * ab[0] + ab[1] * ab[1]) - ab[2];
        double q = -dAC * Math.sqrt(ac[0] * ac[0] + ac[1] * ac[1]) - ac[2];
        PointF P1 = findIntersectPoint(ab[0], ab[1], m, ac[0], ac[1], n);
        PointF P2 = findIntersectPoint(ab[0], ab[1], m, ac[0], ac[1], q);
        PointF P3 = findIntersectPoint(ab[0], ab[1], p, ac[0], ac[1], n);
        PointF P4 = findIntersectPoint(ab[0], ab[1], p, ac[0], ac[1], q);
        if (testShrunkPoint(ab, ac, B, C, P1, b, c)) {
            return P1;
        } else if (testShrunkPoint(ab, ac, B, C, P2, b, c)) {
            return P2;
        } else if (testShrunkPoint(ab, ac, B, C, P3, b, c)) {
            return P3;
        } else if (testShrunkPoint(ab, ac, B, C, P4, b, c)) {
            return P4;
        } else {
            return null;
        }
    }

    private static boolean testShrunkPoint(double[] ab, double[] ac, PointF B, PointF C, PointF P, boolean b, boolean c) {
        if (P != null && P.x < Float.MAX_VALUE && P.y < Float.MAX_VALUE) {
            double signC = (ab[0] * P.x + ab[1] * P.y + ab[2]) * (ab[0] * C.x + ab[1] * C.y + ab[2]);
            double signB = (ac[0] * P.x + ac[1] * P.y + ac[2]) * (ac[0] * B.x + ac[1] * B.y + ac[2]);
            boolean testC = signC > Double.MIN_VALUE;
            boolean testB = signB > Double.MIN_VALUE;
            return testC == c && testB == b;
        }
        return false;
    }

    /**
     * Solve equations
     * ax + by = c
     * dx + ey = f
     *
     * @param a
     * @param b
     * @param c
     * @param d
     * @param e
     * @param f
     * @return null if this equations does not has solution.
     * return PointF(Float.MaxValue, Float.MaxValue) if this equations has infinite solutions
     * other return the solution of this equations.
     */
    public static PointF findIntersectPoint(double a, double b, double c, double d, double e, double f) {
        double Dx, Dy, D;
        D = a * e - b * d;
        Dx = c * e - b * f;
        Dy = a * f - c * d;
        if (D == 0 && Dx == 0) {
            return new PointF(Float.MAX_VALUE, Float.MAX_VALUE);
        } else if (D == 0 && Dx != 0) {
            return null;
        } else {
            return new PointF((float) (Dx / D), (float) (Dy / D));
        }
    }

    /**
     * Find bisector of angle <BAC
     *
     * @param A
     * @param B
     * @param C
     * @return the Coefficients of bisector
     */
    public static double[] findBisector(PointF A, PointF B, PointF C) {
        double[] ab = getCoefficients(A, B);
        double[] ac = getCoefficients(A, C);
        double sqrt1 = Math.sqrt(ab[0] * ab[0] + ab[1] * ab[1]);
        double sqrt2 = Math.sqrt(ac[0] * ac[0] + ac[1] * ac[1]);
        double a1 = ab[0] / sqrt1 + ac[0] / sqrt2;
        double b1 = ab[1] / sqrt1 + ac[1] / sqrt2;
        double c1 = ab[2] / sqrt1 + ac[2] / sqrt2;

        double a2 = ab[0] / sqrt1 - ac[0] / sqrt2;
        double b2 = ab[1] / sqrt1 - ac[1] / sqrt2;
        double c2 = ab[2] / sqrt1 - ac[2] / sqrt2;

        double fB = a1 * B.x + b1 * B.y + c1;
        double fC = a1 * C.x + b1 * C.y + c1;
        if (fB * fC > Double.MIN_VALUE) {
            return new double[]{a2, b2, c2};
        } else {
            return new double[]{a1, b1, c1};
        }
    }

    public static double[] getCoefficients(PointF A, PointF B) {
        double a = B.y - A.y;
        double b = A.x - B.x;
        double c = B.x * A.y - A.x * B.y;
        return new double[]{a, b, c};
    }

    public static PointF[] findMiddlePoint(PointF A, PointF B, float d) {
        PointF[] result = new PointF[2];
        float dx = B.x - A.x;
        float dy = B.y - A.y;
        float sx = (B.x + A.x) / 2.0f;
        float sy = (B.y + A.y) / 2.0f;
        if (dx == 0) {
            result[0] = new PointF(A.x + d, sy);
            result[1] = new PointF(A.x - d, sy);
        } else if (dy == 0) {
            result[0] = new PointF(sx, A.y + d);
            result[1] = new PointF(sx, A.y - d);
        } else {
            float deltaY = (float) (d / Math.sqrt(1 + (dy * dy) / (dx * dx)));
            result[0] = new PointF(sx - dy / dx * deltaY, sy + deltaY);
            result[1] = new PointF(sx + dy / dx * deltaY, sy - deltaY);
        }

        return result;
    }

    public static boolean CCW(PointF p, PointF q, PointF r) {
        int val = ((int) q.y - (int) p.y) * ((int) r.x - (int) q.x) - ((int) q.x - (int) p.x) * ((int) r.y - (int) q.y);
        return val < 0;
    }

    /**
     * Implement Jarvis Algorithm. Jarvis algorithm or the gift wrapping algorithm is an algorithm for computing the convex hull of a given set of points.
     *
     * @param points
     * @return the convex hull of a given set of points
     */
    public static ArrayList<PointF> jarvis(List<PointF> points) {
        ArrayList<PointF> result = new ArrayList<>();
        int n = points.size();
        /** if less than 3 points return **/
        if (n < 3) {
            for (PointF p : points)
                result.add(p);
            return result;
        }

        int[] next = new int[n];
        Arrays.fill(next, -1);

        /** find the leftmost point **/
        int leftMost = 0;
        for (int i = 1; i < n; i++)
            if ((int) (points.get(i).x) < (int) (points.get(leftMost).x))
                leftMost = i;
        int p = leftMost, q;
        /** iterate till p becomes leftMost **/
        do {
            /** wrapping **/
            q = (p + 1) % n;
            for (int i = 0; i < n; i++)
                if (CCW(points.get(p), points.get(i), points.get(q)))
                    q = i;

            next[p] = q;
            p = q;
        } while (p != leftMost);

        for (int i = 0; i < next.length; i++)
            if (next[i] != -1)
                result.add(points.get(i));
        return result;
    }
}
