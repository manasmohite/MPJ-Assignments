package lca_02;

class Shapes {

    // Instance variables
    double length, breadth, radius;

    //  Constructor Overloading

    // Rectangle
    Shapes(double length, double breadth) {
        this.length = length;
        this.breadth = breadth;
    }

    // Circle
    Shapes(double radius) {
        this.radius = radius;
    }

    // Default constructor
    Shapes() {
        length = breadth = radius = 0;
    }

    //  Method Overloading

    // Area of Rectangle
    double area(double length, double breadth) {
        return length * breadth;
    }

    // Area of Circle
    double area(double radius) {
        return Math.PI * radius * radius;
    }

    // Area of Square
    double area(int side) {
        return side * side;
    }

    public static void main(String[] args) {

        Shapes shape = new Shapes();

        // Rectangle
        double rectArea = shape.area(10.5, 5.2);
        System.out.println("Area of Rectangle: " + rectArea);

        // Circle
        double circleArea = shape.area(7.0);
        System.out.println("Area of Circle: " + circleArea);

        // Square
        int squareArea = (int) shape.area(4);
        System.out.println("Area of Square: " + squareArea);
    }
}
